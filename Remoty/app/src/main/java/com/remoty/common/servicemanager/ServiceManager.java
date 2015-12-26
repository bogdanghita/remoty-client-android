package com.remoty.common.servicemanager;


import com.remoty.services.identity.IdentityService;

public class ServiceManager {

	private static ServiceManager instance = null;

	private EventManager eventManager;

	private ActionManager actionManager;

	private ConnectionManager connectionManager;

	private IdentityService identityService;

	private ServiceManager() {

		eventManager = new EventManager();
		actionManager = new ActionManager(eventManager);
		connectionManager = new ConnectionManager();
		identityService = new IdentityService();
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

	public IdentityService getIdentityService() {
		return identityService;
	}

	// TODO: Recheck this implementation. It should be ok, but this is important...
	public void clear() {

		eventManager.clear();
		connectionManager.clearSelection();

		// NOTE: This is not needed. Still, you can check again and maybe do a clear for safety...
//		actionManager.clear();
	}
}
