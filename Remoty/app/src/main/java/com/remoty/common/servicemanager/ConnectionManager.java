package com.remoty.common.servicemanager;

import com.remoty.common.other.ServerInfo;


public class ConnectionManager {

// =================================================================================================
//	Connection state

	public enum ConnectionState {
		ACTIVE,
		SLOW,
		LOST,
		NONE
	}

	private ServerInfo selectedConnection = null;

	private ConnectionState connectionState = ConnectionState.NONE;

	/**
	 * TODO: ...
	 *
	 * @param connectionState
	 */
	public synchronized void setConnectionState(ConnectionState connectionState) {

		this.connectionState = connectionState;
	}

	/**
	 * TODO: ...
	 *
	 * @return
	 */
	public synchronized ConnectionState getConnectionState() {

		return connectionState;
	}

// =================================================================================================
//	Server selection state

	/**
	 * @return - true if there is connection info available and false otherwise
	 */
	public synchronized boolean hasSelection() {
		return selectedConnection != null;
	}

	/**
	 * Sets the connection info. If there is no connection it can be cleared by giving a null
	 * parameter to this method or by calling clearSelection().
	 *
	 * @param connection - info about the server.
	 */
	public synchronized void setSelection(ServerInfo connection) {

		selectedConnection = connection;
	}

	/**
	 * Has the same effect as calling setSelection(null).
	 */
	public synchronized void clearSelection() {

		selectedConnection = null;
		connectionState = ConnectionState.LOST;
	}

	/**
	 * @return - the connection info or null if there is no connection.
	 */
	public synchronized ServerInfo getSelection() {
		return selectedConnection;
	}
}
