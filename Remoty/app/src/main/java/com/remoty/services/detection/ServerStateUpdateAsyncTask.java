package com.remoty.services.detection;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.other.Constants;
import com.remoty.common.datatypes.Message;
import com.remoty.common.datatypes.ServerInfo;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;


public class ServerStateUpdateAsyncTask extends AsyncTask<Void, Void, ServerInfo> {

	private TcpSocket server;

	public ServerStateUpdateAsyncTask(TcpSocket server) {

		this.server = server;
	}

	public TcpSocket getSocket() {

		return server;
	}

	@Override
	protected ServerInfo doInBackground(Void... params) {

		// Send server state request.
		try {
			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Sending state request message to " + server.getInetAddress());

			Message.AbstractMessage message = new Message.AbstractMessage();
			server.sendObject(message);

			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Sent state request message to " + server.getInetAddress());
		}
		catch (IOException e) {
			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Failed to send state request message to " + server.getInetAddress() + " Server will be deleted.");

			return null;
		}

		// Receive state request message response.
		Message.HostInfoMessage response;
		try {

			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Waiting to receive state request message response from " + server.getInetAddress().getHostAddress());

			response = server.receiveObject(Message.HostInfoMessage.class);

			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "State request message response received successfully from " + server.getInetAddress().getHostAddress());
		}
		catch (IOException e) {
			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Failed to receive state request message response from " + server.getInetAddress().getHostAddress() + " Server will be deleted.");

			e.printStackTrace();
			return null;
		}
		catch (ClassNotFoundException e) {
			Log.d(Constants.APP + Constants.DETECTION + Constants.SERVERS_STATE_UPDATE_SERVICE, "Received INVALID state request message response from " + server.getInetAddress().getHostAddress() + " Server will be deleted.");

			e.printStackTrace();
			return null;
		}

		// Building ServerInfo using the state request message response.
		String ip = server.getInetAddress().getHostAddress();
		int tcpPort = response.port;
		String hostName = response.hostname;

		return new ServerInfo(ip, tcpPort, hostName);
	}
}
