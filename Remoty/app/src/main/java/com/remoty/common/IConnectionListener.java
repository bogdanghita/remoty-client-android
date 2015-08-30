package com.remoty.common;

/**
 * Created by Bogdan on 8/30/2015.
 */
public interface IConnectionListener {

	void connectionLost();

	void connectionEstablished(ServerInfo server);
}
