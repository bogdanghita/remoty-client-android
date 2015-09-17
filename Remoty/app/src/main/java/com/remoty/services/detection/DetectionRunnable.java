package com.remoty.services.detection;

import android.util.Log;

import com.remoty.common.events.DetectionEvent;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.common.other.ServerInfo;
import com.remoty.gui.pages.MainActivity;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class DetectionRunnable implements Runnable {

	private EventManager eventManager;

	List<TcpSocket> pingSockets;

	DetectionBroadcastService broadcastService;
	DetectionResponseService responseService;
	PingService pingService;

	public DetectionRunnable(EventManager eventManager) {

		this.eventManager = eventManager;

		pingSockets = new ArrayList<>();

		broadcastService = new DetectionBroadcastService();
		responseService = new DetectionResponseService();
		pingService = new PingService();
	}

	@Override
	public void run() {
		Log.d(MainActivity.APP + MainActivity.DETECTION, "New detection cycle.");

		// Sending detection messages on the sendDetectionMessage address
		broadcastService.sendDetectionMessage();

		// Initializing detection response service (opening accept socket)
		if (!responseService.isOpen()) {
			responseService.init();
		}

		// Checking if init() succeeded
		if(!responseService.isOpen()) {

			// Bad luck ... init() did not succeed.
			// TODO: Do something intelligent here
			return;
		}

		// Receiving detection responses from new servers and adding them to the list of already detected servers
		List<TcpSocket> newSockets = responseService.receiveDetectionResponse();
		pingSockets.addAll(newSockets);

		// Sending ping messages to all the servers in the list and update it by removing the ones that did not respond
		List<ServerInfo> results = pingService.pingServers(pingSockets);

		// Trigger event if the list was updated
		if (results != null) {

			triggerEvent(results);
		}

		Log.d(MainActivity.APP + MainActivity.DETECTION, "Detection cycle finished.");
	}

	/**
	 * Clears the list of sockets connected to the discovered servers.
	 */
	public void clear() {

		// Closing ping sockets
		for (TcpSocket server : pingSockets) {

			try {
				server.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Clearing list
		pingSockets.clear();

		// Closing response service
		if (responseService.isOpen()) {
			responseService.close();
		}
	}

	/**
	 * Notifies all listeners.
	 *
	 * @param servers - the list containing info about the active servers.
	 */
	private void triggerEvent(List<ServerInfo> servers) {

		eventManager.triggerEvent(new DetectionEvent(servers));
	}
}
