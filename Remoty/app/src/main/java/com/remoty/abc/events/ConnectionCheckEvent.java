package com.remoty.abc.events;


import com.remoty.abc.servicemanager.EventManager;
import com.remoty.abc.servicemanager.StateManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ConnectionCheckEvent extends ServiceEvent {

	private StateManager.State state;

	public ConnectionCheckEvent(StateManager.State state) {
		super(EventManager.EventType.CONNECTION_CHECK);

		this.state = state;
	}

	public StateManager.State getState() {
		return state;
	}
}
