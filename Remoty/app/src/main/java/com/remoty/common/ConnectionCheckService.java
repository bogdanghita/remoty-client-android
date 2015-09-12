//package com.remoty.common;
//
//import com.remoty.common.servicemanager.EventManager;
//import com.remoty.gui.MainActivity;
//import com.remoty.services.threading.TaskScheduler;
//
///**
// * Created by Bogdan on 9/5/2015.
// */
//public class ConnectionCheckService {
//
//	EventManager eventManger;
//
//	TaskScheduler timer;
//
//	ConnectionCheckRunnable connectionCheckRunnable;
//
//	public ConnectionCheckService(EventManager eventManger) {
//
//		this.eventManger = eventManger;
//
//		timer = new TaskScheduler();
//
//		connectionCheckRunnable = null;
//	}
//
//	public void init() {
//
//		if (connectionCheckRunnable != null) {
//			connectionCheckRunnable.clear();
//		}
//
//		connectionCheckRunnable = new ConnectionCheckRunnable(eventManger);
//	}
//
//	public void start() {
//
//		if (!timer.isRunning()) {
//			timer.start(connectionCheckRunnable, MainActivity.CONNECTION_CHECK_INTERVAL);
//		}
//	}
//
//	public void stop() {
//
//		if (timer.isRunning()) {
//			timer.stop();
//		}
//	}
//
//	public void clear() {
//
//		stop();
//
//		if (connectionCheckRunnable != null) {
//			connectionCheckRunnable.clear();
//		}
//
//		connectionCheckRunnable = null;
//	}
//}
