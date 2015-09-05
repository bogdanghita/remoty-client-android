package com.remoty.common;

import com.remoty.abc.events.ConnectionCheckEvent;
import com.remoty.abc.servicemanager.EventManager;
import com.remoty.abc.servicemanager.StateManager;


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
		// trigger the event

	}

	private void triggerEvent(StateManager.State state) {

		eventManager.triggerEvent(new ConnectionCheckEvent(state));
	}

	// TODO: Think if this method is needed. Delete it if not
	public void clear() {

	}
}
