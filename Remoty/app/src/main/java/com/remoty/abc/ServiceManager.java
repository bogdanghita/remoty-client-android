package com.remoty.abc;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ServiceManager {

	private EventManager eventManager;

	private ActionManager actionManager;

	private StateManager stateManager;

	public ServiceManager() {

		eventManager = new EventManager();
		actionManager = new ActionManager(eventManager);
		stateManager = new StateManager();
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

	public StateManager getStateManager() {
		return stateManager;
	}
}
