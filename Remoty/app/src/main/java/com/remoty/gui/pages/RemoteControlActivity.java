package com.remoty.gui.pages;

import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.remoty.R;
import com.remoty.common.datatypes.ConfigurationData;
import com.remoty.common.datatypes.ServerInfo;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.other.Constants;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.services.remotecontrol.AccelerometerService;
import com.remoty.common.datatypes.KeysButtonInfo;
import com.remoty.services.remotecontrol.ButtonService;
import com.remoty.common.datatypes.Message;
import com.remoty.services.remotecontrol.RemoteControlService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class RemoteControlActivity extends BaseActivity {

	public final static String KEY_FILE = "KEY_CONFIGURATION_FILE";
	public final static String KEY_NAME = "KEY_CONFIGURATION_NAME";

	private RemoteControlService remoteControlService;

	private AccelerometerService accService;
	private ButtonService buttonService;

	private ConfigurationData configurationData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);

		// Creating toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Retrieving configuration name and file from intent
		Intent intent = getIntent();
		String configurationName = intent.getStringExtra(KEY_NAME);
		String configurationFile = intent.getStringExtra(KEY_FILE);

		// Setting configuration title in the action bar
		setTitle(configurationName);

		// Reading configuration data from file
		loadConfigurationData(configurationFile);

		// Initializing services
		serviceManager = ServiceManager.getInstance();
		connectionManager = serviceManager.getConnectionManager();
		remoteControlService = serviceManager.getActionManager().getRemoteControlService();
		initializeServices();

		// Adding layout listener
		setOnGlobalLayoutListener();
	}

	@Override
	public void onResume() {
		super.onResume();

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

	private void initializeServices() {

		// Button service initialization
		if(configurationData.hasButtons) {
			buttonService = serviceManager.getActionManager().getKeysService();
		}

		// Accelerometer initialization
		if(configurationData.hasSteeringWheel) {
			// TODO: Think if we want this to be done by the accelerometer service
			// TODO: Also think if this is ok to be done in onCreate() or it should be done in onStart()
			// TODO: Do this things the right way (check if the sensor is there etc.)
			// Obtaining accelerometer sensor
			SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			accService = serviceManager.getActionManager().getAccelerometerService(mSensorManager, mAccelerometerSensor, vibrator);
		}
	}

	private void setOnGlobalLayoutListener() {

		// Adding layout change listener
		final View layoutHolder = findViewById(R.id.configuration_holder_layout);
		layoutHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {

//				Toast.makeText(getActivity(), "onGlobalLayout()" /*+ cnt++*/, Toast.LENGTH_SHORT).show();

				RelativeLayout keysLayout = (RelativeLayout) findViewById(R.id.configuration_holder_layout);

				// Now we can retrieve the width and height
				int buttonLayoutWidth = keysLayout.getWidth();
				int buttonLayoutHeight = keysLayout.getHeight();

				// Removing listener
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					layoutHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				else {
					layoutHolder.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}

				// Adding buttons to layout
				if (configurationData.hasButtons) {
					populateLayout(keysLayout, buttonLayoutWidth, buttonLayoutHeight);
				}
			}
		});
	}

	private void populateLayout(RelativeLayout keysLayout, int buttonLayoutWidth, int buttonLayoutHeight) {

		List<KeysButtonInfo> buttons = configurationData.buttons;

		// TODO: refine this, think of it and make it safe (check if key service is ok etc.)
		buttonService.populateLayout(getApplicationContext(), buttons, keysLayout, buttonLayoutWidth, buttonLayoutHeight);
	}

	private void startServices() {

//		Toast.makeText(getActivity(), "Starting connection services", Toast.LENGTH_LONG).show();

		// This activity is only launched if there is a connection selected. If it is recreated and
		// launched from a previous state then the selected connection will also be persisted
		// For more details see RemoteControlService -> InitRemoteControlAsyncTask -> doInBackground()
		ServerInfo server = serviceManager.getConnectionManager().getSelection();
		Message.RemoteControlPortsMessage ports = remoteControlService.getRemoteControlPorts(server);

		// Checking if the data was successfully retrieved. If not, then the connection is lost
		if (ports == null) {
			// Triggering connection LOST event
			serviceManager.getEventManager().triggerEvent(new ConnectionStateEvent(ConnectionManager.ConnectionState.NONE));
			return;
		}

		if(configurationData.hasButtons) {
			buttonService.init(server.ip, ports.buttonPort);
			if (buttonService.isReady()) {
				buttonService.start();
			}
		}

		if(configurationData.hasSteeringWheel) {
			accService.init(server.ip, ports.accelerometerPort);
			if (accService.isReady()) {
				accService.start();
			}
		}
	}

	private void stopServices() {

//		Toast.makeText(getActivity(), "Stopping connection services", Toast.LENGTH_LONG).show();

		if (configurationData.hasSteeringWheel && accService.isRunning()) {
			accService.stop();
			accService.clear();
		}
		if (configurationData.hasButtons && buttonService.isRunning()) {
			buttonService.stop();
			buttonService.clear();
		}
	}

// =================================================================================================
//	GUI
// =================================================================================================

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
// =================================================================================================

	ConnectionStateEventListener connectionStateEventListener = new ConnectionStateEventListener() {

		@Override
		public void stateChanged(final ConnectionManager.ConnectionState connectionState) {

			// This may be called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

//					Toast.makeText(getActivity(), "Connection connectionState changed: " + connectionState.toString(), Toast.LENGTH_LONG).show();

					// Updating connection state
					connectionManager.setConnectionState(connectionState);

					if (connectionState == ConnectionManager.ConnectionState.NONE) {

						stopServices();

						displaySnackbar();
					}
				}
			});
		}
	};

// =================================================================================================
//	CONFIGURATION LOADER
// =================================================================================================

	private void loadConfigurationData(String configurationFile) {

		configurationData = readConfigurationFile(configurationFile);

		if(configurationData == null) {

			// TODO: if this happens the configuration can't be loaded
			// show a message, and stop the app from crashing
		}
	}

	private ConfigurationData readConfigurationFile(String filename) {

		ConfigurationData result = null;

		try {
			InputStream is = getAssets().open(filename);

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			Gson gson = new Gson();

			result = gson.fromJson(reader, ConfigurationData.class);
		}
		catch (IOException e) {
			e.printStackTrace();

			Log.d(Constants.APP + Constants.CONFIG, "Error reading configuration file: " + filename);
		}

		return result;
	}
}
