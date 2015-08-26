package com.remoty.services;

import android.util.Log;

import com.remoty.common.ServerInfo;
import com.remoty.common.TcpSocket;
import com.remoty.gui.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class DetectionRunnable implements Runnable {

	private List<IDetectionListener> listeners;

	List<TcpSocket> serverSockets;
	Broadcaster broadcaster;

	public DetectionRunnable(List<IDetectionListener> listeners) {

		this.listeners = listeners;

		serverSockets = new ArrayList<>();
		broadcaster = new Broadcaster();
	}

	@Override
	public void run() {

		// TODO: This is not good... This method is called every 5 seconds. And every 5 seconds
		// a new socket is created in the accept servers method

		// Broadcast (UDP)
		broadcaster.broadcast();

		// New connections (fresh TCP)
		List<TcpSocket> newServers = broadcaster.acceptServers();
		serverSockets.addAll(newServers);

		// Ping all (TCP)
		List<ServerInfo> results = pingAll(serverSockets);

		// notify
		notifyListeners(results);
	}

	private List<ServerInfo> pingAll(List<TcpSocket> servers) {

		List<ServerPinger> serverPingers = new ArrayList<>();
		List<ServerInfo> serverInfos = new ArrayList<>();

		Log.d(MainActivity.TAG_SERVICES, "Started pingAll().");

		for (TcpSocket server : servers) {
			ServerPinger serverPinger = new ServerPinger(server);
			serverPinger.execute();

			serverPingers.add(serverPinger);
		}

		for (ServerPinger serverPinger : serverPingers) {

			ServerInfo serverInfo = null;

			try {
				serverInfo = serverPinger.get(MainActivity.ASYNC_TASK_GET_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				serverInfo=null;
				// Nothing to be done here. The following code solves the problem
			}

			if (serverInfo != null) {
				serverInfos.add(serverInfo);
			} else {
				TcpSocket server = serverPinger.getTcpSocket();
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
					// Nothing to be done here. The server should be removed.
				}
				servers.remove(server);
			}
		}

		return serverInfos;
	}

	public void clear() {

		for (TcpSocket server : serverSockets) {

			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		serverSockets.clear();
	}

	private void notifyListeners(List<ServerInfo> servers) {

		for (IDetectionListener listener : listeners) {
			listener.update(servers);
		}
	}
}
