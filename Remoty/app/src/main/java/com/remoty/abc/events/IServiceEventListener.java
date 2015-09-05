package com.remoty.abc.events;

/**
 * Created by Bogdan on 9/5/2015.
 */
public interface IServiceEventListener<T extends ServiceEvent> {

	void notify(T event);
}
