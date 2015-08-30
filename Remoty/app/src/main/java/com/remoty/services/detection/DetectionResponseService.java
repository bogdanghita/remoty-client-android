package com.remoty.services.detection;

import android.util.Log;

import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.MainActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogdan on 8/29/2015.
 */
public class DetectionResponseService {

	private boolean acceptTimeoutExceeded;

	private ServerSocket acceptSocket = null;

	public void init() {

		acceptTimeoutExceeded = false;

		try {
			acceptSocket = new ServerSocket(MainActivity.LOCAL_DETECTION_RESPONSE_PORT);
			acceptSocket.setSoTimeout(MainActivity.DETECTION_RESPONSE_TIMEOUT);
		}
		catch (IOException e) {
			e.printStackTrace();

			acceptSocket = null;
			// TODO: do something... This is very important!
		}
	}

	public boolean isOpen() {
		return acceptSocket != null;
	}

	public void close() {

		try {
			acceptSocket.close();
		}
		catch (IOException e) {
			e.printStackTrace();

			// Nothing to be done here...
		}
		finally {
			acceptSocket = null;
		}
	}

	public List<TcpSocket> receiveDetectionResponse() {

		Log.d("SECOND_STEP", "Done looping over all network interfaces. Waiting for replies...");

		ArrayList<TcpSocket> socketList = new ArrayList<>();

		// Resetting receive timeout flag
		acceptTimeoutExceeded = false;

		// Waiting for server responses until one receive exceeds its timeout
		while (acceptTimeoutExceeded == false) {

			// Processing one response
			TcpSocket server = accept(acceptSocket);

			// Appending server to list
			if (server != null) {
				// Setting timeout. TODO: Think if this should be done in other part of the code
				try {
					server.setTimeout(MainActivity.PING_RESPONSE_TIMEOUT);
				}
				catch (SocketException e) {
					e.printStackTrace();

					// TODO: Make this prettier.
					// Closing socket
					try {
						server.close();
					}
					catch (IOException e1) {
						e1.printStackTrace();

						// Nothing to be done here...
					}

					// This server is broken. Continue accepting other servers.
					continue;
				}

				// Adding server to the list.
				socketList.add(server);
			}
		}

		Log.d("SECOND_STEP", "Stopped waiting for replies. Returning servers list.");

		return socketList;
	}

	private TcpSocket accept(ServerSocket serverSocket) {

		Socket socket;
		try {
			socket = serverSocket.accept();
		}
		catch (IOException e) {
			e.printStackTrace();

			acceptTimeoutExceeded = true;
			return null;
		}

		TcpSocket tcpSocket;
		try {
			tcpSocket = new TcpSocket(socket);
			tcpSocket.setKeepAlive(true);
			tcpSocket.setTcpNoDelay(true);
		}
		catch (IOException e) {
			e.printStackTrace();

			// TODO: put log message here

			try {
				socket.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();

				// Nothing to be done here...
			}
			return null;
		}

		return tcpSocket;
	}
}
