package com.remoty.services.detection;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.datatypes.Message;
import com.remoty.common.datatypes.ServerInfo;
import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.MainActivity;

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

	@Override
	protected ServerInfo doInBackground(Void... params) {

		Log.d(MainActivity.TAG_SERVICES, "Sending ping to " + server.getInetAddress());

		try {
			Message.AbstractMessage pingMessage = new Message.AbstractMessage();
			server.sendObject(pingMessage);
		}
		catch (IOException e) {
			return null;
		}

		Log.d(MainActivity.TAG_SERVICES, "Waiting to receive ping response from " + server.getInetAddress().getHostAddress());

		Message.HostInfoMessage pingResponseMessage;
		try {
			pingResponseMessage = server.receiveObject(Message.HostInfoMessage.class);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		Log.d(MainActivity.TAG_SERVICES, "Ping response received successfully from " + server.getInetAddress().getHostAddress());

		String ip = server.getInetAddress().getHostAddress();
		int tcpPort = pingResponseMessage.port;
		String hostName = pingResponseMessage.hostname;

		return new ServerInfo(ip, tcpPort, hostName);
	}
}
