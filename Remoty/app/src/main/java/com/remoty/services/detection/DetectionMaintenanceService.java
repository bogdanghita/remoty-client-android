package com.remoty.services.detection;

import android.util.Log;

import com.remoty.common.other.ServerInfo;
import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.pages.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DetectionMaintenanceService {

	List<TcpSocket> serverSockets;
	List<ServerStateUpdateAsyncTask> serverStateUpdateTaskList;
	HashMap<String, ServerInfo> serverInfoMap = new HashMap<>();

	/**
	 * Sends a server state request to all the servers in the list and update it by removing the ones that did not respond.
	 *
	 * @param serverSockets - the list of serverSockets to perform server state request and wait for responses on.
	 * @return - a list containing info about the servers that responded and null if the list did not suffer any changes (add/remove)
	 */
	public List<ServerInfo> updateServersState(List<TcpSocket> serverSockets) {

		// No change if the list is empty as only this method is responsible for removing items from the list:
		// - if the list never had elements then there is no change
		// - if the list had elements then they were removed by this method and that change was registered
		if (serverSockets.isEmpty()) {
			return null;
		}

		this.serverSockets = serverSockets;
		serverStateUpdateTaskList = new ArrayList<>();

		Log.d(MainActivity.SERVICES, "Started updateServersState().");

		// Executing server state request on each socket in the list
		executeServersStateUpdateTasks();

		// Waiting for each AsyncTask to finish and retrieve the result.
		return retrieveServerStateUpdateTasksResults();
	}

	private void executeServersStateUpdateTasks() {
		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Setting up all the server state update tasks.");

		for (TcpSocket socket : serverSockets) {

			ServerStateUpdateAsyncTask serverStateUpdateAsyncTask = new ServerStateUpdateAsyncTask(socket);
			serverStateUpdateAsyncTask.execute();

			serverStateUpdateTaskList.add(serverStateUpdateAsyncTask);
		}

		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "All the state update tasks have started.");
	}

	private List<ServerInfo> retrieveServerStateUpdateTasksResults() {
		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Start receiving server state responses.");

		boolean listChangedFlag = false;

		for (ServerStateUpdateAsyncTask task : serverStateUpdateTaskList) {

			ServerInfo serverInfo;

			// Retrieving the result of the task
			try {
				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Receiving server state response from " + task.getSocket().getInetAddress().getHostAddress());

				serverInfo = task.get(MainActivity.ASYNC_TASK_GET_TIMEOUT, TimeUnit.MILLISECONDS);

				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Received server state response from " + task.getSocket().getInetAddress().getHostAddress());
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Failed to receive server state response from " + task.getSocket().getInetAddress().getHostAddress());

				e.printStackTrace();

				// Marking this connection as lost if a timeout or other exception occurs
				serverInfo = null;
			}

			// Checking if there is a response
			// NOTE: ServerStateUpdateAsyncTask will return null if there is a receive problem.
			if (serverInfo == null) {

				// Removing server and notifying the ConnectionManager if necessary
				handleNoResponse(task);

				listChangedFlag = true;

				continue;
			}

			// If this is a new server (it should be as the servers do not respond to broadcast messages
			// from already discovered clients) add it to the list.
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
	private void handleNoResponse(ServerStateUpdateAsyncTask task) {

		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Receiving server state response from " + task.getSocket().getInetAddress().getHostAddress());

		TcpSocket socket = task.getSocket();
		String serverIp = socket.getInetAddress().getHostAddress();

		// Removing socket and server info from the lists
		serverSockets.remove(socket);
		serverInfoMap.remove(serverIp);

		// Closing socket
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Closing the server that did not respond to server state request.");

			socket.close();

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Closed the server that did not respond.");
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.SERVERS_STATE_UPDATE_SERVICE, "Failed to close the server that did not respond!");

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
