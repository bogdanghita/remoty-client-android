package com.remoty.services.remotecontrol;

import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.other.Constants;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.common.datatypes.Message;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class MessageDispatchService {

	EventManager eventManager;

	TcpSocket tcpSocket = null;

	private int timeout;

	public MessageDispatchService(EventManager eventManager, int timeout) {

		this.eventManager = eventManager;

		this.timeout = timeout;
	}

	public void setTimeout(int timeout) {

		this.timeout = timeout;
	}

	// NOTE: This must not be called multiple times. init() and close() must be called alternatively
	public boolean init(String ip, int port) {

		try {

			// Initializing connection to server and creating socket that will be used to send messages
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), Constants.CONNECT_TIMEOUT);

			tcpSocket = new TcpSocket(socket);
			tcpSocket.setTimeout(timeout);
			tcpSocket.setTcpNoDelay(true);
			tcpSocket.setKeepAlive(true);

			triggerEvent(ConnectionManager.ConnectionState.ACTIVE);
		}
		catch (IOException e) {
			e.printStackTrace();

			// NOTE: the runnable (caller) does not perform send if socket is not active
			triggerEvent(ConnectionManager.ConnectionState.NONE);

			tcpSocket = null;
			return false;
		}

		return true;
	}

	public boolean isOpen() {

		return tcpSocket != null;
	}

	// NOTE: This must not be called multiple times or before init()
	public void close() {

		try {
			tcpSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();

			// Nothing to be done here...
		}
		finally {
			tcpSocket = null;
		}
	}

	public void send(Message.AbstractMessage message) {

		try {
			tcpSocket.sendObject(message);
		}
		catch (IOException e) {
			e.printStackTrace();

			// Triggering event
			triggerEvent(ConnectionManager.ConnectionState.NONE);
		}
	}

	private void triggerEvent(ConnectionManager.ConnectionState connectionState) {

		eventManager.triggerEvent(new ConnectionStateEvent(connectionState));
	}
}
