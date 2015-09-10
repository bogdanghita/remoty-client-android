package com.remoty.gui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.RemoteControlEvent;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.remotecontrol.AccelerometerService;
import com.remoty.remotecontrol.ConfigurationInfo;
import com.remoty.remotecontrol.Message;
import com.remoty.remotecontrol.RemoteControlService;
import com.remoty.common.ServerInfo;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class RemoteControlFragment extends DebugFragment {

	private final static String KEY_FILE = "KEY_CONFIGURATION_FILE";
	private final static String KEY_NAME = "KEY_CONFIGURATION_NAME";

	ServiceManager serviceManager;
	AccelerometerService accService;
	RemoteControlService remoteControlService;

	/**
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

		return parentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Accelerometer initialization
		// TODO: Think if we want this to be done by the accelerometer service
		// TODO: Also think if this is ok to be done in onCreate() or it should be done in onStart()
		// TODO: Do this things the right way (check if the sensor is there etc.)
		// Obtaining accelerometer sensor
		SensorManager mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		serviceManager = ServiceManager.getInstance();
		accService = serviceManager.getActionManager().getAccelerometerService(mSensorManager, mAccelerometerSensor);
		remoteControlService = serviceManager.getActionManager().getRemoteControlService();
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

		// Starting Accelerometer
		// Initializing accelerometer service
		accService.init(server.ip, ports.accelerometerPort);

		// Starting accelerometer service
		if (accService.isReady()) {

			accService.start();
		}

		// Starting Keys
	}

	private void stopServices() {

		Toast.makeText(getActivity(), "Stopping connection services", Toast.LENGTH_LONG).show();

		// Stopping Accelerometer
		// Stopping accelerometer service
		if (accService.isRunning()) {
			accService.stop();
		}

		// Clearing accelerometer service
		accService.clear();

		// Stopping Keys

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

		Snackbar.make(coordinatorLayoutView, "Connection state: TODO", Snackbar.LENGTH_LONG)
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
}
