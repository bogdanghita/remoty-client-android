package com.remoty.common;

import com.remoty.abc.events.ConnectionStateEvent;
import com.remoty.abc.servicemanager.ConnectionManager;
import com.remoty.abc.servicemanager.EventManager;


/**
 * Created by Bogdan on 9/6/2015.
 */
public class ConnectionCheckRunnable implements Runnable {

	EventManager eventManager;

	public ConnectionCheckRunnable(EventManager eventManager) {

		this.eventManager = eventManager;
	}

	@Override
	public void run() {

		// TODO ...
		// - do the work
		// - trigger the event
	}

	private void triggerEvent(ConnectionManager.ConnectionState connectionState) {

		eventManager.triggerEvent(new ConnectionStateEvent(connectionState));
	}

	// TODO: Think if this method is needed. Delete it if not
	public void clear() {

	}
}
