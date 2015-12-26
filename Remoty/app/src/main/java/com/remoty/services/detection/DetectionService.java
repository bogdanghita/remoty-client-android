package com.remoty.services.detection;

import com.remoty.common.other.Constants;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.services.threading.TaskScheduler;


public class DetectionService {

	EventManager eventManger;

	TaskScheduler timer;

	DetectionRunnable detectionRunnable;

	public DetectionService(EventManager eventManger) {

		this.eventManger = eventManger;

		timer = new TaskScheduler();

		detectionRunnable = null;
	}

	public void init() {

		if (detectionRunnable != null) {
			detectionRunnable.clear();
		}

		detectionRunnable = new DetectionRunnable(eventManger);
	}

	public void start() {

		if (!timer.isRunning()) {
			timer.start(detectionRunnable, Constants.DETECTION_INTERVAL);
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
