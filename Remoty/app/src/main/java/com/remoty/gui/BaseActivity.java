package com.remoty.gui;

import android.os.Bundle;

import com.remoty.common.ServerInfo;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.ServiceManager;

/**
 * Created by Bogdan on 9/17/2015.
 */
public class BaseActivity extends DebugActivity {

	protected ServiceManager serviceManager;
	protected ConnectionManager connectionManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serviceManager = ServiceManager.getInstance();
		connectionManager = serviceManager.getConnectionManager();
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restoring the connection info from the saved instance. If there was no connection then
		// the it is set to null (returned by retrieveFromBundle())
		ServerInfo connectionInfo = ServerInfo.retrieveFromBundle(savedInstanceState);
		connectionManager.setSelection(connectionInfo);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Saving the connection info to the bundle
		if (connectionManager.hasSelection()) {

			ServerInfo connectionInfo = connectionManager.getSelection();
			ServerInfo.saveToBundle(connectionInfo, savedInstanceState);
		}

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

//		// Clearing this so that it is not kept in memory as a static object until the OS
//		// decides to stop the process and clear the RAM
//		serviceManager.clear();
	}
}
