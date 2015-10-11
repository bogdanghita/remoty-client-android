package com.remoty.common.events;

import com.remoty.common.servicemanager.EventManager;


public class BaseEvent {

	EventManager.EventType type;

	protected BaseEvent(EventManager.EventType type) {

		this.type = type;
	}

	public EventManager.EventType getType() {

		return type;
	}
}
