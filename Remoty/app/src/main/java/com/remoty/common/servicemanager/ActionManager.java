package com.remoty.common.servicemanager;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Vibrator;

import com.remoty.remotecontrol.AccelerometerService;
import com.remoty.remotecontrol.KeysService;
import com.remoty.remotecontrol.RemoteControlService;
import com.remoty.services.detection.DetectionService;

import java.util.List;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ActionManager {

	// TODO: This class is similar to a factory class. Think about this...

	private EventManager eventManager;

	private DetectionService serverDetection;

	private RemoteControlService remoteControl;

	public ActionManager(EventManager eventManager) {

		this.eventManager = eventManager;

		serverDetection = new DetectionService(eventManager);
		remoteControl = new RemoteControlService();
	}

	public DetectionService getServerDetectionService() {

		return serverDetection;
	}

	public RemoteControlService getRemoteControlService() {

		return remoteControl;
	}

	public AccelerometerService getAccelerometerService(SensorManager sensorManager, Sensor accelerometerSensor, Vibrator vibrator) {

		return new AccelerometerService(eventManager, sensorManager, accelerometerSensor, vibrator);
	}

	public KeysService getKeysService() {

		return new KeysService(eventManager);
	}
}
