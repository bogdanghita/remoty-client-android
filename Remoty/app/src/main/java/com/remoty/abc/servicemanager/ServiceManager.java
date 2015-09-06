package com.remoty.abc.servicemanager;

/**
 * Created by Bogdan on 9/5/2015.
 */
public class ServiceManager {

	private static ServiceManager instance = null;

	private EventManager eventManager;

	private ActionManager actionManager;

	private ConnectionManager connectionManager;

	private ServiceManager() {

		eventManager = new EventManager();
		actionManager = new ActionManager(eventManager);
		connectionManager = new ConnectionManager();
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

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	// TODO: Recheck this implementation. It should be ok, but this is important...
	public void clear() {

		eventManager.clear();
		connectionManager.clearSelection();

		// NOTE: This is not needed. Still, you can check again and maybe do a clear for safety...
//		actionManager.clear();
	}
}
