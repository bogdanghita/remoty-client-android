package com.remoty.abc.servicemanager;

import com.google.common.collect.HashMultimap;
import com.remoty.abc.events.ConnectionCheckListener;
import com.remoty.abc.events.DetectionListener;
import com.remoty.abc.events.IServiceEventListener;
import com.remoty.abc.events.ServiceEvent;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class EventManager {

	HashMultimap<EventType, IServiceEventListener> listeners;

	public EventManager() {

		listeners = HashMultimap.create();
	}

	public enum EventType {
		CONNECTION_CHECK,
		DETECTION
	}

	public void subscribe(ConnectionCheckListener l) {

		listeners.put(EventType.CONNECTION_CHECK, l);
	}

	public void unsubscribe(ConnectionCheckListener l) {
		listeners.put(EventType.CONNECTION_CHECK, l);
	}

	public void subscribe(DetectionListener l) {
		listeners.put(EventType.DETECTION, l);
	}

	public void unsubscribe(DetectionListener l) {
		listeners.put(EventType.DETECTION, l);
	}

	public void triggerEvent(ServiceEvent event) {

		for (IServiceEventListener l : listeners.get(event.getType())) {
			l.notify(event);
		}
	}

}
