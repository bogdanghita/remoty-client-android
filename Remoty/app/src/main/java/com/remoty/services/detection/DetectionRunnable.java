package com.remoty.services.detection;

import android.util.Log;

import com.remoty.common.events.DetectionEvent;
import com.remoty.common.other.Constant;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.common.other.ServerInfo;
import com.remoty.gui.pages.MainActivity;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class DetectionRunnable implements Runnable {

	private EventManager eventManager;

	private List<TcpSocket> serverSockets;

	private DetectionBroadcastService broadcastService;
	private DetectionResponseService responseService;
	private DetectionMaintenanceService detectionMaintenanceService;

	private boolean firstDetectionCycle;

	public DetectionRunnable(EventManager eventManager) {

		this.eventManager = eventManager;

		serverSockets = new ArrayList<>();

		broadcastService = new DetectionBroadcastService();
		responseService = new DetectionResponseService();
		detectionMaintenanceService = new DetectionMaintenanceService();

		firstDetectionCycle = true;
	}

	@Override
	public void run() {
		Log.d(Constant.APP + Constant.DETECTION, "New detection cycle.");

		// Sending detection messages on the sendDetectionMessage address
		broadcastService.sendDetectionMessage();

		// Initializing detection response service (opening accept socket)
		if (!responseService.isOpen()) {

			if (!responseService.init()) {

				// Bad luck... init() did not succeed.
				return;
			}
		}

		// Receiving detection responses from new servers and adding them to the list of already detected servers
		List<TcpSocket> newSockets = responseService.receiveDetectionResponse();
		serverSockets.addAll(newSockets);

		// Sending server state update messages to all the servers in the list and update it by removing the ones that did not respond
		List<ServerInfo> results = detectionMaintenanceService.updateServersState(serverSockets);

		// Trigger event if the list was updated
		if (results != null) {

			triggerEvent(results);
		}
		// Triggering first detection event
		else if(firstDetectionCycle) {
			triggerEvent(new LinkedList<ServerInfo>());
		}

		// Resetting first run flag
		if(firstDetectionCycle) {
			firstDetectionCycle = false;
		}

		Log.d(Constant.APP + Constant.DETECTION, "Detection cycle finished.");
	}

	/**
	 * Clears the list of serverSockets connected to the discovered servers.
	 */
	public void clear() {

		// Closing serverSockets
		for (TcpSocket server : serverSockets) {

			try {
				server.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Clearing list
		serverSockets.clear();

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
