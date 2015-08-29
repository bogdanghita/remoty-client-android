package com.remoty.services.detection;

import com.remoty.common.datatypes.ServerInfo;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class DetectionRunnable implements Runnable {

	private List<IDetectionListener> listeners;

	List<TcpSocket> pingSockets;

	DetectionBroadcastService broadcastService;
	DetectionResponseService responseService;
	PingService pingService;

	public DetectionRunnable(List<IDetectionListener> listeners) {

		this.listeners = listeners;

		pingSockets = new ArrayList<>();

		broadcastService = new DetectionBroadcastService();
		responseService = new DetectionResponseService();
		pingService = new PingService();
	}

	@Override
	public void run() {

		// Sending detection messages on the sendDetectionMessage address
		broadcastService.sendDetectionMessage();

		// Initializing detection response service (opening accept socket)
		if (!responseService.isOpen()) {
			responseService.init();
		}

		// Receiving detection responses from new servers and adding them to the list of already detected servers
		List<TcpSocket> newSockets = responseService.receiveDetectionResponse();
		pingSockets.addAll(newSockets);

		// Sending ping messages to all the servers in the list and update it by removing the ones that did not respond
		List<ServerInfo> results = pingService.pingServers(pingSockets);

		// Notify all listeners if the list was updated
		if (results != null) {
			notifyListeners(results);
		}
	}

	/**
	 * Clears the list of sockets connected to the discovered servers.
	 */
	public void clear() {

		for (TcpSocket server : pingSockets) {

			try {
				server.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (responseService.isOpen()) {
			responseService.close();
		}

		pingSockets.clear();
	}

	/**
	 * Notifies all listeners.
	 *
	 * @param servers - the list containing info about the active servers.
	 */
	private void notifyListeners(List<ServerInfo> servers) {

		for (IDetectionListener listener : listeners) {
			listener.update(servers);
		}
	}
}
