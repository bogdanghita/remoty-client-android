package com.remoty.services.remotecontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.other.Constants;
import com.remoty.common.datatypes.ServerInfo;
import com.remoty.common.datatypes.Message;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RemoteControlService {

	private class InitRemoteControlAsyncTask extends AsyncTask<ServerInfo, Void, Message.RemoteControlPortsMessage> {

		@Override
		protected Message.RemoteControlPortsMessage doInBackground(ServerInfo... params) {

			ServerInfo info = params[0];

			try {
				// TODO: put a timeout for connection
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(info.ip, info.port), Constants.CONNECT_TIMEOUT);
				TcpSocket tcpSocket = new TcpSocket(socket);

				Message.RemoteControlPortsMessage message;
				message = tcpSocket.receiveObject(Message.RemoteControlPortsMessage.class);

				tcpSocket.close();

				return message;
			}
			catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();

				Log.d(Constants.SERVICES, "Unable to get acc port.");

				return null;
			}
			catch (NullPointerException e) {
				e.printStackTrace();

				/*
				Dear Android developer,

				This is the story of how you got here:
				There is a very small chance that the connection is lost after the activity is launched
				but before this code is executed. So you end up with null server.

				How you can fix this:
				1. Leave it like this. This catch block does its job.
				2. Clone the server in a synchronized block so that even if the connection is lost, the
				server will be saved. The final result will be the same with the result of solution 1.
				So I wouldn't bother if I were you.

				P.S. Please leave you comment below if you disagree. If the problem persists, someone will
				hopefully see it...

				Kind regards,
				Bogdan
				*/

				Log.d(Constants.SERVICES, "Known issue. Server info is probably null.");

				return null;
			}
		}
	}

	public Message.RemoteControlPortsMessage getRemoteControlPorts(ServerInfo server) {

		InitRemoteControlAsyncTask initTask = new InitRemoteControlAsyncTask();

		initTask.execute(server);

		try {
			return initTask.get(Constants.INIT_REMOTE_CONTROL_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();

			return null;
		}
	}

}
