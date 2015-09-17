package com.remoty.remotecontrol;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;

import com.remoty.common.servicemanager.EventManager;
import com.remoty.gui.MainActivity;
import com.remoty.services.threading.TaskScheduler;

/**
 * Created by Bogdan on 8/30/2015.
 */
public class AccelerometerService implements SensorEventListener {

	EventManager eventManager;

	private SensorManager mSensorManager;
	private Sensor mAccelerometerSensor;

	Vibrator mVibrator;

	TaskScheduler timer;

	MessageDispatchRunnable accRunnable;

	public AccelerometerService(EventManager eventManager, SensorManager sensorManager, Sensor accelerometerSensor, Vibrator vibrator) {

		this.eventManager = eventManager;

		mSensorManager = sensorManager;
		mAccelerometerSensor = accelerometerSensor;

		mVibrator = vibrator;

		timer = new TaskScheduler();

		accRunnable = null;
	}

	// TODO: think the port and ip should be passed here or in other method (ex. constructor)
	public void init(String ip, int port) {

		if (accRunnable != null) {
			accRunnable.clear();
		}

		Message.AccelerometerMessage emptyMessage = new Message.AccelerometerMessage();
		accRunnable = new MessageDispatchRunnable(eventManager, ip, port, emptyMessage);
	}

	public void start() {

		if (!timer.isRunning()) {

			// Registering sensor listener
			mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

			timer.start(accRunnable, MainActivity.ACCELEROMETER_INTERVAL);
		}
	}

	public void stop() {

		if (timer.isRunning()) {
			timer.stop();
		}

		// TODO: see if this can be called multiple times and what is the behavior after it is canceled (can it be restarted after?)
		// Stopping vibrations
		mVibrator.cancel();

		// Unregistering sensor listener
		mSensorManager.unregisterListener(this);
	}

	public void clear() {

		stop();

		if (accRunnable != null) {
			accRunnable.clear();
		}

		accRunnable = null;
	}

	public boolean isReady() {
		return accRunnable != null;
	}

	public boolean isRunning() {
		return timer.isRunning();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		Message.AccelerometerMessage message = new Message.AccelerometerMessage();
		message.x = event.values[0];
		message.y = event.values[1];
		message.z = event.values[2];

		accRunnable.setMessage(message);

		vibrate(event.values[1]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private void vibrate(float yValue) {

		long dutyCycle = (long) yValue;

		dutyCycle = Math.abs(dutyCycle);

		if (dutyCycle > 8) dutyCycle = 8;

		long[] pattern = {(10 - dutyCycle)*1, 1*dutyCycle*dutyCycle};

		mVibrator.vibrate(pattern, 0);
	}
}
