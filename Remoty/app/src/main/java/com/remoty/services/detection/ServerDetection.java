package com.remoty.services.detection;

import com.remoty.gui.MainActivity;
import com.remoty.services.threading.TaskScheduler;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerDetection {

	private List<IDetectionListener> listeners;

	TaskScheduler timer;

	DetectionRunnable detectionRunnable;

	public ServerDetection() {

		listeners = new LinkedList<>();

		timer = new TaskScheduler();

		detectionRunnable = null;
	}

	public void subscribe(IDetectionListener listener) {

		listeners.add(listener);
	}

	public void unsubscribe(IDetectionListener listener) {

		listeners.remove(listener);
	}

	public void init() {

		if (detectionRunnable != null) {
			detectionRunnable.clear();
		}

		detectionRunnable = new DetectionRunnable(listeners);
	}

	public void start() {

		if (!timer.isRunning()) {
			timer.start(detectionRunnable, MainActivity.DETECTION_INTERVAL);
		}
	}

	public void stop() {

		if (timer.isRunning()) {
			timer.stop();
		}
	}

	public void clear() {

		stop();

		if (detectionRunnable != null) {
			detectionRunnable.clear();
		}

		detectionRunnable = null;
	}
}
