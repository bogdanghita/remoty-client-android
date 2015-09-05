package com.remoty.abc.servicemanager;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.remoty.common.AccelerometerService;
import com.remoty.common.RemoteControlService;
import com.remoty.services.detection.DetectionService;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ActionManager {

	// TODO: This class is similar to a factory class. Think about this...

	private DetectionService serverDetection;

	private ConnectionCheckService connectionCheck;

	private RemoteControlService remoteControl;

	public ActionManager(EventManager eventManager) {

		serverDetection = new DetectionService(eventManager);
		connectionCheck = new ConnectionCheckService();
		remoteControl = new RemoteControlService();
	}

	public DetectionService getServerDetectionService() {

		return serverDetection;
	}

	public ConnectionCheckService getConnectionCheckService() {

		return connectionCheck;
	}

	public RemoteControlService getRemoteControlService() {

		return remoteControl;
	}

	public AccelerometerService getAccelerometerService(SensorManager sensorManager, Sensor accelerometerSensor) {

		return new AccelerometerService(sensorManager, accelerometerSensor);
	}
}
