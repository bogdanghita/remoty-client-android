package com.remoty.services.remotecontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.other.Constant;
import com.remoty.common.other.ServerInfo;
import com.remoty.gui.pages.MainActivity;
import com.remoty.common.other.Message;
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
				socket.connect(new InetSocketAddress(info.ip, info.port), Constant.CONNECT_TIMEOUT);
				TcpSocket tcpSocket = new TcpSocket(socket);

				Message.RemoteControlPortsMessage message;
				message = tcpSocket.receiveObject(Message.RemoteControlPortsMessage.class);

				tcpSocket.close();

				return message;
			}
			catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();

				Log.d(Constant.SERVICES, "Unable to get acc port.");

				// TODO: do something here
				return null;
			}
		}
	}

	public Message.RemoteControlPortsMessage getRemoteControlPorts(ServerInfo server) {

		InitRemoteControlAsyncTask initTask = new InitRemoteControlAsyncTask();

		initTask.execute(server);

		try {
			return initTask.get(Constant.INIT_REMOTE_CONTROL_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();

			return null;
		}
	}

}
