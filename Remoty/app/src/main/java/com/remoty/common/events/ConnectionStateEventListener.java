package com.remoty.common.events;

import com.remoty.common.servicemanager.ConnectionManager;


public abstract class ConnectionStateEventListener implements IEventListener<ConnectionStateEvent> {

	public void notify(ConnectionStateEvent event) {

		stateChanged(event.getConnectionState());
	}

	public abstract void stateChanged(ConnectionManager.ConnectionState connectionState);
}
