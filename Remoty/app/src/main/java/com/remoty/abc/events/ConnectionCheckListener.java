package com.remoty.abc.events;

import com.remoty.abc.servicemanager.StateManager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public abstract class ConnectionCheckListener implements IServiceEventListener<ConnectionCheckEvent> {

	public void notify(ConnectionCheckEvent event) {

		stateChanged(event.getState());
	}

	public abstract void stateChanged(StateManager.State state);
}
