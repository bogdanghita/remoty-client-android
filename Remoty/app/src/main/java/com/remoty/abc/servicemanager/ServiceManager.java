package com.remoty.abc.servicemanager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ServiceManager {

	private static ServiceManager instance = null;

	private EventManager eventManager;

	private ActionManager actionManager;

	private StateManager stateManager;

	private ServiceManager() {

		eventManager = new EventManager();
		actionManager = new ActionManager(eventManager);
		stateManager = new StateManager();
	}

	public synchronized static ServiceManager getInstance() {

		if (instance == null) {
			instance = new ServiceManager();
		}
		return instance;
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
