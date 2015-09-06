package com.remoty.abc.events;


import com.remoty.abc.servicemanager.ConnectionManager;
import com.remoty.abc.servicemanager.EventManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ConnectionStateEvent extends BaseEvent {

	private ConnectionManager.ConnectionState connectionState;

	public ConnectionStateEvent(ConnectionManager.ConnectionState connectionState) {
		super(EventManager.EventType.CONNECTION_STATE);

		this.connectionState = connectionState;
	}

	public ConnectionManager.ConnectionState getConnectionState() {
		return connectionState;
	}
}
