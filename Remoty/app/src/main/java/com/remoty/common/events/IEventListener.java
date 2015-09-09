package com.remoty.common.events;

/**
 * Created by Bogdan on 9/5/2015.
 */
public interface IEventListener<T extends BaseEvent> {

	void notify(T event);
}
