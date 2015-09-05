package com.remoty.common;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.gui.MainActivity;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Bogdan on 9/1/2015.
 */
public class RemoteControlService {

	private class InitRemoteControlAsyncTask extends AsyncTask<ServerInfo, Void, Message.RemoteControlPortsMessage> {

		@Override
		protected Message.RemoteControlPortsMessage doInBackground(ServerInfo... params) {

			ServerInfo info = params[0];

			try {
				// TODO: put a timeout for connection
				Socket socket = new Socket(info.ip, info.port);
				TcpSocket tcpSocket = new TcpSocket(socket);

				Message.RemoteControlPortsMessage message;
				message = tcpSocket.receiveObject(Message.RemoteControlPortsMessage.class);

				tcpSocket.close();

				return message;
			}
			catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();

				Log.d(MainActivity.TAG_SERVICES, "Unable to get acc port.");

				// TODO: do something here
				return null;
			}
		}
	}

	public Message.RemoteControlPortsMessage getRemoteControlPorts(ServerInfo server) {

		InitRemoteControlAsyncTask initTask = new InitRemoteControlAsyncTask();

		initTask.execute(server);

		try {
			return initTask.get(MainActivity.INIT_REMOTE_CONTROL_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();

			return null;
		}
	}

}
