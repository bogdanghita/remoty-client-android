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

	// TODO: if this is called multiple times it is not good. Handle this inside or put a NOTE
	public void init() {

		acceptTimeoutExceeded = false;

		// Start the ServerSocket. Remote servers will connect to this Android device via this ServerSocket.
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Initializing the ServerSocket...");

			acceptSocket = new ServerSocket(MainActivity.LOCAL_DETECTION_RESPONSE_PORT);
			acceptSocket.setSoTimeout(MainActivity.DETECTION_RESPONSE_TIMEOUT);

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Initialized the ServerSocket.");
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to initialize the ServerSocket!");

			e.printStackTrace();

			acceptSocket = null;
			// TODO: do something... This is very important!
		}
	}

	public boolean isOpen() {
		return acceptSocket != null;
	}

	// TODO: if this is called multiple times or before init() it is not good. Handle this inside or put a NOTE
	public void close() {

		// Close the ServerSocket.
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closing the ServerSocket...");

			acceptSocket.close();

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closed the ServerSocket.");
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to close the ServerSocket!");

			e.printStackTrace();

			// Nothing to be done here...
		}
		finally {
			acceptSocket = null;
		}
	}

	public List<TcpSocket> receiveDetectionResponse() {

		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Done looping over all network interfaces. Waiting for replies...");

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
					Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to set a timeout to the ServerSocket.");

					e.printStackTrace();

					// TODO: Make this prettier.
					// Closing socket
					try {
						Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closing the ServerSocket...");

						server.close();

						Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closed the ServerSocket.");
					}
					catch (IOException e1) {
						Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to close the ServerSocket after a failed timeout set!");

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

		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Stopped waiting for replies. Returning servers list.");

		return socketList;
	}

	private TcpSocket accept(ServerSocket serverSocket) {

		Socket socket;
		// Accept a new client. If there are no more pending connections, this ServerSocket.Accept()
		// will timeout thus finishing the "accepting" cycle.
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Accepting new client...");

			socket = serverSocket.accept();

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Accepted new client.");
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to accept new client. Stopped accepting new clients.");

			e.printStackTrace();

			acceptTimeoutExceeded = true;
			return null;
		}

		TcpSocket tcpSocket;
		// Create a TcpSocket for the new client. This will be given as the return value.
		try {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Creating TcpSocket for new client...");

			tcpSocket = new TcpSocket(socket);
			tcpSocket.setKeepAlive(true);
			tcpSocket.setTcpNoDelay(true);

			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Created TcpSocket for new client.");
		}
		catch (IOException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to create a new TcpSocket.");

			e.printStackTrace();

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
