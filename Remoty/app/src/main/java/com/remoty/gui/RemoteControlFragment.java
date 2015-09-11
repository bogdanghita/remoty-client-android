package com.remoty.gui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.RemoteControlEvent;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.remotecontrol.AccelerometerService;
import com.remoty.remotecontrol.ConfigurationInfo;
import com.remoty.remotecontrol.KeysButtonInfo;
import com.remoty.remotecontrol.KeysService;
import com.remoty.remotecontrol.Message;
import com.remoty.remotecontrol.RemoteControlService;
import com.remoty.common.ServerInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class RemoteControlFragment extends DebugFragment {

	private final static String KEY_FILE = "KEY_CONFIGURATION_FILE";
	private final static String KEY_NAME = "KEY_CONFIGURATION_NAME";

	ServiceManager serviceManager;
	RemoteControlService remoteControlService;

	AccelerometerService accService;
	KeysService keysService;

	RelativeLayout keysLayout;

	/**
	 * TODO: Think about moving this in a separate Factory class, so that the instances don not have access to it
	 * TODO: Put a proper description here
	 * If you don't know why is this needed see this link:
	 * http://stackoverflow.com/questions/10798489/proper-way-to-give-initial-data-to-fragments
	 *
	 * @param configuration
	 * @return
	 */
	public static RemoteControlFragment newInstance(ConfigurationInfo configuration) {

		RemoteControlFragment instance = new RemoteControlFragment();

		Bundle args = new Bundle();

		args.putString(KEY_NAME, configuration.getName());
		args.putString(KEY_FILE, configuration.getFile());
		instance.setArguments(args);

		return instance;
	}

	public ConfigurationInfo getConfiguration() {

		Bundle args = getArguments();

		String name = args.getString(KEY_NAME);
		String file = args.getString(KEY_FILE);

		return new ConfigurationInfo(name, file);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_drive, container, false);

		// TODO: Somewhere around here you should:
		/* - getConfiguration() from bundle
		 * - read modules from file
		 * - add each module
		 */

		keysLayout = (RelativeLayout) parentView.findViewById(R.id.configuration_holder_layout);

		return parentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serviceManager = ServiceManager.getInstance();
		remoteControlService = serviceManager.getActionManager().getRemoteControlService();

		// Accelerometer initialization
		// TODO: Think if we want this to be done by the accelerometer service
		// TODO: Also think if this is ok to be done in onCreate() or it should be done in onStart()
		// TODO: Do this things the right way (check if the sensor is there etc.)
		// Obtaining accelerometer sensor
		SensorManager mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		accService = serviceManager.getActionManager().getAccelerometerService(mSensorManager, mAccelerometerSensor);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Triggering start event
		serviceManager.getEventManager().triggerEvent(new RemoteControlEvent(RemoteControlEvent.Action.START));
	}

	@Override
	public void onResume() {
		super.onResume();

		// Keys initialization
		List<KeysButtonInfo> buttonInfoList = generateNFS2012Buttons();
		keysService = serviceManager.getActionManager().getKeysService(buttonInfoList, keysLayout);

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
	public void onStop() {
		super.onStop();

		// Triggering start event
		serviceManager.getEventManager().triggerEvent(new RemoteControlEvent(RemoteControlEvent.Action.STOP));
	}

	private void startServices() {

		Toast.makeText(getActivity(), "Starting connection services", Toast.LENGTH_LONG).show();

		// This fragment is only launched if there is a connection selected. If it is recreated and
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

		Toast.makeText(getActivity(), "Stopping connection services", Toast.LENGTH_LONG).show();

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

		final View coordinatorLayoutView = getActivity().findViewById(R.id.snackbar_position);

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
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Toast.makeText(getActivity(), "Connection connectionState changed: " + connectionState.toString(), Toast.LENGTH_LONG).show();

					if (connectionState == ConnectionManager.ConnectionState.LOST || connectionState == ConnectionManager.ConnectionState.SLOW) {

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

	// Generates the buttons config for bla bla bla
	private List<KeysButtonInfo> generateNFS2012Buttons() {

		List<KeysButtonInfo> list = new LinkedList<>();

		// TODO: Claudiu

		KeysButtonInfo buttonInfo;

		// Main buttons
		buttonInfo = new KeysButtonInfo("NOS", "ButtonA_", (float) 0, (float) 0, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("HandBr", "ButtonX_", (float) 0, (float) 0.5, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("Acc", "ButtonRT_", (float) 0.7, (float) 0, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("Br/Rev", "ButtonLT_", (float) 0.7, (float) 0.5, (float) 0.3, (float) 0.5);
		list.add(buttonInfo);

		// Medium buttons
		buttonInfo = new KeysButtonInfo("swap", "ButtonY_", (float) 0.35, (float) 0, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("back", "ButtonB_", (float) 0.35, (float) 0.25, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("start", "ButtonStart_", (float) 0.5, (float) 0, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("enter", "ButtonA_", (float) 0.5, (float) 0.25, (float) 0.15, (float) 0.25);
		list.add(buttonInfo);

		// Small buttons
		buttonInfo = new KeysButtonInfo("^", "ButtonUp_", (float) 0.45, (float) 0.5, (float) 0.10, (float) (1/6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("v", "ButtonDown_", (float) 0.45, (float) (0.5+2/6.), (float) 0.10, (float) (1/6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo("<", "ButtonLeft_", (float) 0.35, (float) (0.5+1/6.), (float) 0.10, (float) (1/6.));
		list.add(buttonInfo);
		buttonInfo = new KeysButtonInfo(">", "ButtonRight_", (float) 0.55, (float) (0.5+1/6.), (float) 0.10, (float) (1/6.));
		list.add(buttonInfo);

		return list;
	}
}
