package com.remoty.gui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remoty.R;
import com.remoty.common.AccelerometerService;
import com.remoty.common.ConnectionManager;
import com.remoty.common.Message;
import com.remoty.common.ServerInfo;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class DriveFragment extends LiveDataTransferFragment {

	AccelerometerService accService;

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
	}

	@Override
	public void onStart() {
		super.onStart();

		// NOTE: onResume() is always called immediately after onStart()

		// TODO: This case should be treated with very much attention because it is important
		// Checking if there is a connection
		if(!ConnectionManager.hasConnection()) {

			Log.d(MainActivity.TAG_SERVICES, "Not connected.");

			// TODO: Do something here...
			// TODO: It is important the acc service not to be started

			return;
		}

		// Retrieving info about accelerometer simulation port
		String ip = ConnectionManager.getConnection().ip;

		setAccelerometerPort();

		if(port == -1) {
			return;
		}

		// Initializing simulation
		accService.init(ip, port);
	}

	@Override
	public void onResume() {
		super.onResume();

		if(accService.isReady()) {
			accService.start();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if(accService.isRunning()) {
			accService.stop();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		// NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
		// or it might be restarted (onRestart -> on Start)

		accService.clear();
	}

	private int port;

	private void setAccelerometerPort() {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				ServerInfo info = ConnectionManager.getConnection();

				try {
					// TODO: put a timeout for connection
					Socket socket = new Socket(info.ip, info.port);
					TcpSocket tcpSocket = new TcpSocket(socket);

					Message.SimulationInfoMessage message;
					message = tcpSocket.receiveObject(Message.SimulationInfoMessage.class);

					tcpSocket.close();

					port = message.accelerometerPort;
				}
				catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();

					Log.d(MainActivity.TAG_SERVICES, "Unable to get acc port.");

					// TODO: do something here

					port = -1;
				}
			}
		});

		t.start();

		try {
			t.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
