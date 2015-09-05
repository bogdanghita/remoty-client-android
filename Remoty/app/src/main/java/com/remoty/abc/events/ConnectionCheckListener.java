package com.remoty.abc.events;

/**
 * Created by Bogdan on 9/5/2015.
 */
public abstract class ConnectionCheckListener implements IServiceEventListener<ConnectionCheckEvent> {

	public void notify(ConnectionCheckEvent event) {

		if(event.getState() == ConnectionCheckEvent.State.ESTABLISHED) {
			connectionEstablished();
		}
		else {
			connectionLost();
		}
	}

	public abstract void connectionEstablished();

	public abstract void connectionLost();
}
