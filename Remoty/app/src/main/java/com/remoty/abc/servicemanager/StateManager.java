package com.remoty.abc.servicemanager;

import com.remoty.common.ServerInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class StateManager {

	private ServerInfo connectionInfo = null;

	/**
	 * @return - true if there is connection info available and false otherwise
	 */
	public synchronized boolean hasConnection() {
		return connectionInfo != null;
	}

	/**
	 * Sets the connection info. If there is no connection it can be cleared by giving a null
	 * parameter to this method or by calling clearConnection().
	 *
	 * @param connection - info about the server.
	 */
	public synchronized void setConnection(ServerInfo connection) {
		connectionInfo = connection;

//		if (connection == null) {
//			notifyConnectionLost();
//		}
//		else {
//			notifyConnectionEstablished();
//		}
	}

	/**
	 * Has the same effect as calling setConnection(null).
	 */
	public synchronized void clearConnection() {
		connectionInfo = null;

//		notifyConnectionLost();
	}

	/**
	 * @return - the connection info or null if there is no connection.
	 */
	public synchronized ServerInfo getConnection() {
		return connectionInfo;
	}
}
