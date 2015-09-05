package com.remoty.gui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.abc.servicemanager.ServiceManager;
import com.remoty.common.AccelerometerService;
import com.remoty.common.Message;
import com.remoty.common.RemoteControlService;
import com.remoty.common.ServerInfo;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class DriveFragment extends LiveDataTransferFragment {

	AccelerometerService accService;
	RemoteControlService remoteControlService;

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

		accService = new AccelerometerService(mSensorManager, mAccelerometerSensor);
		remoteControlService = new RemoteControlService();
	}

	@Override
	public void onStart() {
		super.onStart();

		// NOTE: onResume() is always called immediately after onStart()
	}

	@Override
	public void onResume() {
		super.onResume();

//		StateManager.subscribe(this);

		// This fragment is only launched if there is a connection selected. If it is recreated and
		// launched from a previous state then the selected connection will also be persisted
		ServerInfo server = ServiceManager.getInstance().getStateManager().getSelection();
		Message.RemoteControlPortsMessage ports = remoteControlService.getRemoteControlPorts(server);

		if(ports == null) {

			// TODO: Do something ...
			// TODO: Open connect page

			Toast.makeText(getActivity(), "Unable to get RC port. Should open ConnectPage.", Toast.LENGTH_LONG).show();

			return;
		}

		// All good. Starting remote control.

		// Initializing accelerometer simulation
		accService.init(server.ip, ports.accelerometerPort);

		accService.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		if(accService.isRunning()) {
			accService.stop();
		}

//		StateManager.unsubscribe(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
		// or it might be restarted (onRestart -> on Start)

		accService.clear();
	}

//	@Override
//	public void connectionLost() {
//
//		if(accService.isRunning()) {
//			accService.stop();
//		}
//	}
//
//	@Override
//	public void connectionEstablished(ServerInfo server) {
//
//		if(accService.isReady()) {
//			accService.start();
//		}
//	}
}
