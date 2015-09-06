package com.remoty.abc.events;

/**
 * Created by Bogdan on 9/5/2015.
 */
public interface IEventListener<T extends BaseEvent> {

	void notify(T event);
}
