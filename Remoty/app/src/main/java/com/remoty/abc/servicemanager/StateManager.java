package com.remoty.abc.servicemanager;

import com.remoty.common.ServerInfo;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class StateManager {

	public enum State {
		CONNECTED,
		DISCONNECTED
	}

	private ServerInfo selectedConnection = null;

	private State state = State.DISCONNECTED;

	/**
	 * TODO: ...
	 *
	 * @param state
	 */
	public synchronized void setState(State state) {

		this.state = state;
	}

	/**
	 * TODO: ...
	 *
	 * @return
	 */
	public synchronized State getState() {

		return state;
	}

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
		state = State.DISCONNECTED;
	}

	/**
	 * @return - the connection info or null if there is no connection.
	 */
	public synchronized ServerInfo getSelection() {
		return selectedConnection;
	}
}
