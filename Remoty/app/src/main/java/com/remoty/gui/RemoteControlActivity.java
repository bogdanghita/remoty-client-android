package com.remoty.gui;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.remoty.R;
import com.remoty.common.ServerInfo;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.remotecontrol.AccelerometerService;
import com.remoty.remotecontrol.KeysButtonInfo;
import com.remoty.remotecontrol.KeysService;
import com.remoty.remotecontrol.Message;
import com.remoty.remotecontrol.RemoteControlService;

import java.util.LinkedList;
import java.util.List;

public class RemoteControlActivity extends DebugActivity {

	public final static String KEY_FILE = "KEY_CONFIGURATION_FILE";
	public final static String KEY_NAME = "KEY_CONFIGURATION_NAME";

	private ServiceManager serviceManager;
	private ConnectionManager connectionManager;
	RemoteControlService remoteControlService;

	AccelerometerService accService;
	KeysService keysService;

	// TODO: split the content of this method into smaller methods
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);

		// Creating toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Initializing services
		serviceManager = ServiceManager.getInstance();
		connectionManager = serviceManager.getConnectionManager();
		remoteControlService = serviceManager.getActionManager().getRemoteControlService();

		// Accelerometer initialization
		// TODO: Think if we want this to be done by the accelerometer service
		// TODO: Also think if this is ok to be done in onCreate() or it should be done in onStart()
		// TODO: Do this things the right way (check if the sensor is there etc.)
		// Obtaining accelerometer sensor
		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		accService = serviceManager.getActionManager().getAccelerometerService(mSensorManager, mAccelerometerSensor, vibrator);

		// Adding layout change listener
		final View layoutHolder = findViewById(R.id.configuration_holder_layout);
		layoutHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				View keysL = findViewById(R.id.configuration_holder_layout);

//				Toast.makeText(getActivity(), "onGlobalLayout()" /*+ cnt++*/, Toast.LENGTH_SHORT).show();

				// Now we can retrieve the width and height
				int keysLayoutWidth = keysL.getWidth();
				int keysLayoutHeight = keysL.getHeight();

				// Removing listener
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					layoutHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				else {
					layoutHolder.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}

				// TODO: refine this, think of it and make it safe (check if key service is ok etc.)
				RelativeLayout keysLayout = (RelativeLayout) findViewById(R.id.configuration_holder_layout);
				keysService.populateLayout(generateNFSMW2012Buttons(), keysLayout, keysLayoutWidth, keysLayoutHeight);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		// Keys initialization
		List<KeysButtonInfo> buttonInfoList = generateNFSMW2012Buttons();
		keysService = serviceManager.getActionManager().getKeysService();

		// Subscribing to connection state events
		serviceManager.getEventManager().subscribe(connectionStateEventListener);

		// Starting services
		startServices();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stopping services
		stopServices();

		// Unsubscribing to connection state events
		serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_remote_control, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {

		// Disabling the back button
	}

	@Override
	public boolean onMenuOpened(final int featureId, final Menu menu) {

		// Disabling the menu button
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		// Ignoring orientation|keyboard change
		super.onConfigurationChanged(newConfig);
	}

	private void startServices() {

//		Toast.makeText(getActivity(), "Starting connection services", Toast.LENGTH_LONG).show();

		// TODO: Review this
		// This activity is only launched if there is a connection selected. If it is recreated and
		// launched from a previous state then the selected connection will also be persisted
		ServerInfo server = serviceManager.getConnectionManager().getSelection();
		Message.RemoteControlPortsMessage ports = remoteControlService.getRemoteControlPorts(server);

		// Checking if the data was successfully retrieved. If not, then the connection is lost
		if (ports == null) {
			// Triggering connection LOST event
			serviceManager.getEventManager().triggerEvent(new ConnectionStateEvent(ConnectionManager.ConnectionState.LOST));
			return;
		}

		accService.init(server.ip, ports.accelerometerPort);
		keysService.init(server.ip, ports.buttonPort);

		if (accService.isReady()) {
			accService.start();
		}
		if (keysService.isReady()) {
			keysService.start();
		}
	}

	private void stopServices() {

//		Toast.makeText(getActivity(), "Stopping connection services", Toast.LENGTH_LONG).show();

		if (accService.isRunning()) {
			accService.stop();
		}
		if (keysService.isRunning()) {
			keysService.stop();
		}

		accService.clear();
		keysService.clear();
	}

// =================================================================================================
//	GUI... see main activity for better description

	private void displaySnackbar() {

		final View coordinatorLayoutView = findViewById(R.id.snackbar_position);

		final View.OnClickListener clickListener = new View.OnClickListener() {
			public void onClick(View v) {

				startServices();
			}
		};

		Snackbar.make(coordinatorLayoutView, "Connection state: TODO", Snackbar.LENGTH_INDEFINITE)
				.setAction("RETRY", clickListener)
				.show();
	}

// =================================================================================================
//	LISTENERS

	ConnectionStateEventListener connectionStateEventListener = new ConnectionStateEventListener() {

		@Override
		public void stateChanged(final ConnectionManager.ConnectionState connectionState) {

			// This may be called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

//					Toast.makeText(getActivity(), "Connection connectionState changed: " +
//							connectionState.toString(), Toast.LENGTH_LONG).show();

					// Updating connection state
					connectionManager.setConnectionState(connectionState);

					if (connectionState == ConnectionManager.ConnectionState.LOST ||
							connectionState == ConnectionManager.ConnectionState.SLOW) {

						stopServices();

						displaySnackbar();
					}
				}
			});
		}
	};

// =================================================================================================
//	TESTING

	private List<KeysButtonInfo> generateTestKeys() {

		List<KeysButtonInfo> list = new LinkedList<>();

		KeysButtonInfo buttonInfo = new KeysButtonInfo();
		buttonInfo.action = "ButtonRT_";
		buttonInfo.name = "NiceName";

		list.add(buttonInfo);

		return list;
	}

	// Generates the buttons configuration for NFS Most Wanted 2012
	private List<KeysButtonInfo> generateNFSMW2012Buttons() {

		List<KeysButtonInfo> list = new LinkedList<>();

		KeysButtonInfo buttonInfo;

		// Driving buttons
		buttonInfo = new KeysButtonInfo("NOS", "ButtonA_", (float) 0, (float) 0, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("Handbrake", "ButtonX_", (float) 0, (float) 0.5, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("Acceleration", "ButtonRT_", (float) 0.7, (float) 0, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("Brake/Reverse", "ButtonLT_", (float) 0.7, (float) 0.5, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);

		// Menu buttons
		buttonInfo = new KeysButtonInfo("swap", "ButtonY_", (float) 0.35, (float) 0, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("back", "ButtonB_", (float) 0.35, (float) 0.25, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("start", "ButtonStart_", (float) 0.5, (float) 0, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("enter", "ButtonA_", (float) 0.5, (float) 0.25, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);

		// Directional buttons
		buttonInfo = new KeysButtonInfo("^", "ButtonUp_", (float) 0.45, (float) 0.5, (float) 0.10, (float) (1 / 6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("v", "ButtonDown_", (float) 0.45, (float) (0.5 + 2 / 6.), (float) 0.10, (float) (1 / 6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("<", "ButtonLeft_", (float) 0.35, (float) (0.5 + 1 / 6.), (float) 0.10, (float) (1 / 6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo(">", "ButtonRight_", (float) 0.55, (float) (0.5 + 1 / 6.), (float) 0.10, (float) (1 / 6.));
		list.add(buttonInfo);

		return list;
	}
}

