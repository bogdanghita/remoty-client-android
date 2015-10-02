package com.remoty.services.detection;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.other.Message;
import com.remoty.common.other.ServerInfo;
import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.pages.MainActivity;

import java.io.IOException;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class PingAsyncTask extends AsyncTask<Void, Void, ServerInfo> {

	private TcpSocket server;

	public PingAsyncTask(TcpSocket server) {

		this.server = server;
	}

	public TcpSocket getSocket() {

		return server;
	}

	// TODO: split this
	@Override
	protected ServerInfo doInBackground(Void... params) {

		// Send ping.
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Sending ping to " + server.getInetAddress());

			Message.AbstractMessage pingMessage = new Message.AbstractMessage();
			server.sendObject(pingMessage);

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Sent ping to " + server.getInetAddress());
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Failed to send ping to " + server.getInetAddress() + " Server will be deleted.");

			return null;
		}

		Message.HostInfoMessage pingResponseMessage;
		// Receive ping response.
		try {

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Waiting to receive ping response from " + server.getInetAddress().getHostAddress());

			pingResponseMessage = server.receiveObject(Message.HostInfoMessage.class);

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Ping response received successfully from " + server.getInetAddress().getHostAddress());
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Failed to receive ping response from " + server.getInetAddress().getHostAddress() + " Server will be deleted.");

			e.printStackTrace();
			return null;
		}
		catch (ClassNotFoundException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.PING_SERVICE + MainActivity.PING_TASK, "Receiced INVALID ping response from " + server.getInetAddress().getHostAddress() + " Server will be deleted.");

			e.printStackTrace();
			return null;
		}

		// Building ServerInfo using the ping response.
		String ip = server.getInetAddress().getHostAddress();
		int tcpPort = pingResponseMessage.port;
		String hostName = pingResponseMessage.hostname;

		return new ServerInfo(ip, tcpPort, hostName);
	}
}
