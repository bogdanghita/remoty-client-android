package com.remoty.abc.events;

import com.remoty.abc.EventManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ServiceEvent {

	EventManager.EventType type;

	protected ServiceEvent(EventManager.EventType type) {

		this.type = type;
	}

	public EventManager.EventType getType() {

		return type;
	}
}
