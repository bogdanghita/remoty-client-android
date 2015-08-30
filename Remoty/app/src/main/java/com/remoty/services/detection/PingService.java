package com.remoty.services.detection;

import android.util.Log;

import com.remoty.common.ConnectionManager;
import com.remoty.common.ServerInfo;
import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
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

				// Marking this connection as lost if a timeout or other exception occurs
				serverInfo = null;
			}

			// Checking if there is a response
			// NOTE: PingAsyncTask will return null if there is a receive problem.
			if(serverInfo == null) {

				// Removing server and notifying the ConnectionManager if necessary
				handleNoResponse(ping);

				listChangedFlag = true;

				continue;
			}

			// If this is a new server (it should be as the servers do not respond to broadcast messages
			// from already discovered clients) adding it to the list.
			if (!serverInfoMap.containsKey(serverInfo.ip)) {

				// Adding server to the list
				serverInfoMap.put(serverInfo.ip, serverInfo);

				listChangedFlag = true;
			}
		}

		// If there are changes returning a sorted list with the info about the servers. Otherwise returning null.
		return listChangedFlag ? createServerInfoList() : null;
	}

	/**
	 * Closing socket and removing server from the list. Also checking if this server was the current
	 * selected one (selected by the user in the connect page) and notifying the ConnectionManager.
	 */
	private void handleNoResponse(PingAsyncTask ping) {

		TcpSocket socket = ping.getSocket();
		String serverIp = socket.getInetAddress().getHostAddress();

		// Removing socket and server info from the lists
		sockets.remove(socket);
		serverInfoMap.remove(serverIp);

		//  Notifying the ConnectionManager if this server was the current selected one
		if(ConnectionManager.hasConnection() && ConnectionManager.getConnection().ip.equals(serverIp)) {
			ConnectionManager.clearConnection();
		}

		// Closing socket
		try {
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();

			// Nothing to be done here...
		}
	}

	/**
	 * Creating a sorted list with the info about the servers
	 */
	private List<ServerInfo> createServerInfoList() {

		List<ServerInfo> serverInfoList = new LinkedList<>();

		for (ServerInfo info : serverInfoMap.values()) {

			serverInfoList.add(info);
		}

		Collections.sort(serverInfoList);

		return serverInfoList;
	}
}
