package com.remoty.common;

import com.remoty.gui.MainActivity;
import com.remoty.services.detection.DetectionRunnable;
import com.remoty.services.threading.TaskScheduler;

/**
 * Created by Bogdan on 8/30/2015.
 */
public class AccelerometerService {

	TaskScheduler timer;

	AccelerometerRunnable accRunnable;

	public AccelerometerService() {

		timer = new TaskScheduler();

		accRunnable = null;
	}

	public void init() {

		if (accRunnable != null) {
			accRunnable.clear();
		}

		accRunnable = new AccelerometerRunnable();
	}

	public void start() {

		if (!timer.isRunning()) {
			timer.start(accRunnable, MainActivity.ACCELEROMETER_INTERVAL);
		}
	}

	public void stop() {

		if (timer.isRunning()) {
			timer.stop();
		}
	}

	public void clear() {

		stop();

		if (accRunnable != null) {
			accRunnable.clear();
		}

		accRunnable = null;
	}
}
