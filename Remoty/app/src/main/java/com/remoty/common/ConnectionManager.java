package com.remoty.common;

import com.remoty.common.ServerInfo;

/**
 * Created by Bogdan on 8/23/2015.
 */
public class ConnectionManager {

    private static ServerInfo connectionInfo = null;

    /**
     * @return - true if there is connection info available and false otherwise
     */
    public static boolean hasConnection() {
        return connectionInfo != null;
    }

    /**
     * Sets the connection info. If there is no connection it can be cleared by giving a null
     * parameter to this method or by calling clearConnection().
     *
     * @param connection - info about the server.
     */
    public static void setConnection(ServerInfo connection) {
        connectionInfo = connection;
    }

    /**
     * Has the same effect as calling setConnection(null).
     */
    public static void clearConnection() {
        connectionInfo = null;
    }

    /**
     * @return - the connection info or null if there is no connection.
     */
    public static ServerInfo getConnection() {
        return connectionInfo;
    }
}
