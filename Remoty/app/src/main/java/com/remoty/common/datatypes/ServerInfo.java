package com.remoty.common.datatypes;

import android.os.Bundle;


public class ServerInfo implements Comparable<ServerInfo> {

	private final static String KEY_IP = "KEY_SERVERINFO_IP";
	private final static String KEY_PORT = "KEY_SERVERINFO_PORT";
	private final static String KEY_NAME = "KEY_SERVERINFO_NAME";

	public String ip;
	public int port;
	public String name;
	public int iconResource;

	public ServerInfo(String ip, int port, String name) {

		this.ip = ip;
		this.port = port;
		this.name = name;
	}

	@Override
	public boolean equals(Object object) {

		if (object == null) {
			throw new NullPointerException();
		}

		if (!(object instanceof ServerInfo)) {
			return false;
		}
		if (object == this) {
			return true;
		}

		ServerInfo ref = (ServerInfo) object;

		return ip.equals(ref.ip) && port == ref.port && name.equals(ref.name);
	}

	// TODO: see if this is a perfect implementation of compareTo or it has leaks
	@Override
	public int compareTo(ServerInfo object) {

		if (equals(object)) {
			return 0;
		}

		return name.compareTo(object.name);
	}

	/**
	 * @param info   - the info to be saved.
	 * @param bundle - the Bundle where the info will be saved.
	 */
	public static void saveToBundle(ServerInfo info, Bundle bundle) {

		bundle.putString(KEY_IP, info.ip);
		bundle.putInt(KEY_PORT, info.port);
		bundle.putString(KEY_NAME, info.name);
	}

	/**
	 * @param bundle - the Bundle to retrieve the info from.
	 * @return - the info retrieved from the Bundle and null if the Bundle does not contain the ServerInfo data.
	 */
	public static ServerInfo retrieveFromBundle(Bundle bundle) {

		// Checking if the bundle has the required data
		if (!bundle.containsKey(KEY_IP) || !bundle.containsKey(KEY_PORT) || !bundle.containsKey(KEY_NAME)) {
			return null;
		}

		String ip = bundle.getString(KEY_IP);
		int port = bundle.getInt(KEY_PORT);
		String name = bundle.getString(KEY_NAME);

		return new ServerInfo(ip, port, name);
	}
}
