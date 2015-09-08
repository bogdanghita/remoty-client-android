package com.remoty.common;

import android.util.Log;
import android.widget.Toast;

import com.remoty.abc.events.ConnectionStateEvent;
import com.remoty.abc.servicemanager.ConnectionManager;
import com.remoty.abc.servicemanager.EventManager;
import com.remoty.gui.MainActivity;
import com.remoty.services.networking.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Bogdan on 8/30/2015.
 */
public class MessageDispatchService {

	EventManager eventManager;

	TcpSocket tcpSocket = null;

	private int timeout;

	public MessageDispatchService(EventManager eventManager, int timeout) {

		this.eventManager = eventManager;

		this.timeout = timeout;
	}

	// TODO: think if there are also needed some other timeouts (for connect for example)
	public void setTimeout(int timeout) {

		this.timeout = timeout;
	}

	// TODO: think if the ip and port should be passed here or in the constructor and/or in a setServer() method
	// TODO: if this is called multiple times it is not good. Handle this inside or put a NOTE
	public void init(String ip, int port) {

		try {

			// Initializing connection to server and creating socket that will be used to send messages
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), MainActivity.CONNECT_TIMEOUT);

			tcpSocket = new TcpSocket(socket);
			tcpSocket.setTimeout(timeout);
			tcpSocket.setTcpNoDelay(true);
			tcpSocket.setKeepAlive(true);
		}
		catch (IOException e) {
			e.printStackTrace();

			// TODO: do something... This is very important! (think if you should do something else here)
			// NOTE: the runnable (caller) does not perform send if socket is not active
			triggerEvent(ConnectionManager.ConnectionState.LOST);

			tcpSocket = null;
		}
	}

	public boolean isOpen() {

		return tcpSocket != null;
	}

	// TODO: if this is called multiple times or before init() it is not good. Handle this inside or put a NOTE
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

		Log.d("MESSAGE", "Sending message...");

		try {
			tcpSocket.sendObject(message);
		}
		catch (IOException e) {
			e.printStackTrace();

			// TODO: do something... This is also very important!
			// Or maybe this should be handled by the caller... In any case, the thing that is responsible
			// for adjusting the interval and the thing that is responsible with connection lost issues
			// need to know that this exception occurred

			Log.d("MESSAGE", "Error on send...");

			// TODO: Decide what type of events you want to trigger here: LOST or SLOW.
			// Currently triggering connection LOST event.
			triggerEvent(ConnectionManager.ConnectionState.LOST);
		}

		Log.d("MESSAGE", "Message sent...");
	}

	private void triggerEvent(ConnectionManager.ConnectionState connectionState) {

		eventManager.triggerEvent(new ConnectionStateEvent(connectionState));
	}
}
