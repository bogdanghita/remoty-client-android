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
import com.remoty.abc.events.ConnectionStateEvent;
import com.remoty.abc.events.ConnectionStateEventListener;
import com.remoty.abc.events.RemoteControlEvent;
import com.remoty.abc.servicemanager.ConnectionManager;
import com.remoty.abc.servicemanager.ServiceManager;
import com.remoty.common.AccelerometerService;
import com.remoty.common.Message;
import com.remoty.common.RemoteControlService;
import com.remoty.common.ServerInfo;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class RemoteControlFragment extends DebugFragment {

	ServiceManager serviceManager;
	AccelerometerService accService;
	RemoteControlService remoteControlService;

	String remoteIp;
	Message.RemoteControlPortsMessage remoteControlPorts;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_drive, container, false);

		return parentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		// This fragment is only launched if there is a connection selected. If it is recreated and
		// launched from a previous state then the selected connection will also be persisted
		ServerInfo server = serviceManager.getConnectionManager().getSelection();
		Message.RemoteControlPortsMessage ports = remoteControlService.getRemoteControlPorts(server);

		if (ports == null) {

			// Triggering connection LOST event
			serviceManager.getEventManager().triggerEvent(new ConnectionStateEvent(ConnectionManager.ConnectionState.LOST));

			// TODO: Open connect page???

			return;
		}

		// Setting info for reconnect action.
		remoteIp = server.ip;
		remoteControlPorts = ports;

		// Starting services
		startServices(remoteIp, remoteControlPorts);
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stopping services
		stopServices();
	}

	@Override
	public void onStop() {
		super.onStop();

		// Triggering start event
		serviceManager.getEventManager().triggerEvent(new RemoteControlEvent(RemoteControlEvent.Action.STOP));
	}

	private void startServices(String ip, Message.RemoteControlPortsMessage ports) {

		Toast.makeText(getActivity(), "Starting connection services", Toast.LENGTH_LONG).show();

		// Initializing accelerometer service
		accService.init(ip, ports.accelerometerPort);

		// Starting accelerometer service
		if (accService.isReady()) {

			accService.start();

			// Subscribing to connection state events
			serviceManager.getEventManager().subscribe(connectionStateEventListener);
		}
	}

	private void stopServices() {

		Toast.makeText(getActivity(), "Stopping connection services", Toast.LENGTH_LONG).show();

		// Stopping accelerometer service
		if (accService.isRunning()) {
			accService.stop();
		}

		// Clearing accelerometer service
		accService.clear();

		// Unsubscribing to connection state events
		serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
	}

// =================================================================================================
//	GUI... see main activity for better description

	private void displaySnackbar() {

		final View coordinatorLayoutView = getActivity().findViewById(R.id.snackbar_position);

		final View.OnClickListener clickListener = new View.OnClickListener() {
			public void onClick(View v) {

				startServices(remoteIp, remoteControlPorts);
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
}
