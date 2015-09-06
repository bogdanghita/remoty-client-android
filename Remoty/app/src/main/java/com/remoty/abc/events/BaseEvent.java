package com.remoty.abc.events;

import com.remoty.abc.servicemanager.EventManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class BaseEvent {

	EventManager.EventType type;

	protected BaseEvent(EventManager.EventType type) {

		this.type = type;
	}

	public EventManager.EventType getType() {

		return type;
	}
}
