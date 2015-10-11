package com.remoty.common.events;


public interface IEventListener<T extends BaseEvent> {

	void notify(T event);
}
