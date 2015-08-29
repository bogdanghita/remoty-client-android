package com.remoty.common.datatypes;

import android.os.Bundle;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerInfo implements Comparable<ServerInfo> {

	public String ip;
	public int port;
	public String name;

	public ServerInfo(String ip, int port, String name) {

		this.ip = ip;
		this.port = port;
		this.name = name;
	}

	/**
	 * @param info   - the info to be saved.
	 * @param bundle - the Bundle where the info will be saved.
	 */
	public static void saveToBundle(ServerInfo info, Bundle bundle) {

	}

	/**
	 * @param bundle - the Bundle to retrieve the info from.
	 * @return - the info retrieved from the Bundle and null if the Bundle does not contain the ServerInfo data.
	 */
	public static ServerInfo retrieveFromBundle(Bundle bundle) {

//		throw new UnsupportedOperationException("No implemented yet.");
		return null;
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
}
