package com.remoty.abc.events;


import com.remoty.abc.servicemanager.EventManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ConnectionCheckEvent extends ServiceEvent {

	public enum State {
		ESTABLISHED,
		LOST
	}

	private State state;

	public ConnectionCheckEvent(State state) {
		super(EventManager.EventType.CONNECTION_CHECK);

		this.state = state;
	}

	public State getState() {
		return state;
	}
}
