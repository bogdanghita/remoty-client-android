package com.remoty.common;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class ConnectionManager {

	private static ServerInfo connectionInfo = null;

	private static final List<IConnectionListener> listeners = new LinkedList<>();

	public static void subscribe(IConnectionListener listener) {
		listeners.add(listener);
	}

	public static void unsubscribe(IConnectionListener listener) {
		listeners.remove(listener);
	}

	private static void notifyConnectionLost() {

		for (IConnectionListener listener : listeners) {
			listener.connectionLost();
		}
	}

	private static void notifyConnectionEstablished() {

		for (IConnectionListener listener : listeners) {
			listener.connectionEstablished(connectionInfo);
		}
	}

	/**
	 * @return - true if there is connection info available and false otherwise
	 */
	public static synchronized boolean hasConnection() {
		return connectionInfo != null;
	}

	/**
	 * Sets the connection info. If there is no connection it can be cleared by giving a null
	 * parameter to this method or by calling clearConnection().
	 *
	 * @param connection - info about the server.
	 */
	public static synchronized void setConnection(ServerInfo connection) {
		connectionInfo = connection;

		if (connection == null) {
			notifyConnectionLost();
		}
		else {
			notifyConnectionEstablished();
		}
	}

	/**
	 * Has the same effect as calling setConnection(null).
	 */
	public static synchronized void clearConnection() {
		connectionInfo = null;

		notifyConnectionLost();
	}

	/**
	 * @return - the connection info or null if there is no connection.
	 */
	public static synchronized ServerInfo getConnection() {
		return connectionInfo;
	}
}
