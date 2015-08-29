package com.remoty.services.detection;

import android.util.Log;

import com.remoty.common.datatypes.ServerInfo;
import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Bogdan on 8/29/2015.
 */
public class PingService {

	List<TcpSocket> sockets;
	List<PingAsyncTask> pingTaskList;
	HashMap<String, ServerInfo> serverInfoMap = new HashMap<>();

	/**
	 * Sends a ping messages to all the servers in the list and update it by removing the ones that did not respond.
	 *
	 * @param sockets - the list of sockets to perform ping and wait for responses on.
	 * @return - a list containing info about the servers that responded and null if the list did not suffer any changes (add/remove)
	 */
	public List<ServerInfo> pingServers(List<TcpSocket> sockets) {

		// No change if the list is empty as only this method is responsible for removing items from the list:
		// - if the list never had elements then there is no change
		// - if the list had elements then they were removed by this method and that change was registered
		if (sockets.isEmpty()) {
			return null;
		}

		this.sockets = sockets;
		pingTaskList = new ArrayList<>();

		Log.d(MainActivity.TAG_SERVICES, "Started pingServers().");

		// Executing ping on each socket in the list
		sendPingMessages();

		// Waiting for each AsyncTask to finish and retrieve the result.
		return receivePingResponses();
	}

	private void sendPingMessages() {

		for (TcpSocket socket : sockets) {

			PingAsyncTask ping = new PingAsyncTask(socket);
			ping.execute();

			pingTaskList.add(ping);
		}
	}

	private List<ServerInfo> receivePingResponses() {

		boolean listChangedFlag = false;

		for (PingAsyncTask ping : pingTaskList) {

			ServerInfo serverInfo;

			// Retrieving the result of the ping
			try {
				serverInfo = ping.get(MainActivity.ASYNC_TASK_GET_TIMEOUT, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();

				serverInfo = null;
			}

			// Checking if a response was received.
			// If true adding server info to the list, else removing it from the list.
			if (serverInfo == null) {

				TcpSocket socket = ping.getSocket();

				// Removing socket and server info from the lists
				sockets.remove(socket);
				serverInfoMap.remove(socket.getInetAddress().getHostAddress());

				// Closing socket
				try {
					socket.close();
				}
				catch (IOException e) {
					e.printStackTrace();

					// Nothing to be done here...
				}

				listChangedFlag = true;
			}
			else if (!serverInfoMap.containsKey(serverInfo.ip)) {

				// Adding server to the list
				serverInfoMap.put(serverInfo.ip, serverInfo);

				listChangedFlag = true;
			}
		}

		// If there are changes returning a sorted list with the info about the servers. Otherwise returning null.
		return listChangedFlag ? generateSortedList() : null;
	}

	private List<ServerInfo> generateSortedList() {

		List<ServerInfo> serverInfoList = new LinkedList<>();

		for (ServerInfo info : serverInfoMap.values()) {

			serverInfoList.add(info);
		}

		Collections.sort(serverInfoList);

		return serverInfoList;
	}
}
