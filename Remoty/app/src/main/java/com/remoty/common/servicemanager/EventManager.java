package com.remoty.common.servicemanager;

import com.google.common.collect.HashMultimap;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.DetectionEventListener;
import com.remoty.common.events.IEventListener;
import com.remoty.common.events.BaseEvent;
import com.remoty.common.events.RemoteControlEventListener;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class EventManager {

	public enum EventType {
		DETECTION,
		CONNECTION_STATE,
		REMOTE_CONTROL
	}

	HashMultimap<EventType, IEventListener> listeners;

	public EventManager() {

		listeners = HashMultimap.create();
	}

	public void subscribe(ConnectionStateEventListener l) {

		listeners.put(EventType.CONNECTION_STATE, l);
	}

	public void unsubscribe(ConnectionStateEventListener l) {
		listeners.put(EventType.CONNECTION_STATE, l);
	}

	public void subscribe(DetectionEventListener l) {
		listeners.put(EventType.DETECTION, l);
	}

	public void unsubscribe(DetectionEventListener l) {
		listeners.put(EventType.DETECTION, l);
	}

	public void subscribe(RemoteControlEventListener l) {
		listeners.put(EventType.REMOTE_CONTROL, l);
	}

	public void unsubscribe(RemoteControlEventListener l) {
		listeners.put(EventType.REMOTE_CONTROL, l);
	}

	public void triggerEvent(BaseEvent event) {

		for (IEventListener l : listeners.get(event.getType())) {
			l.notify(event);
		}
	}

	public void clear() {

		listeners.clear();
	}
}
