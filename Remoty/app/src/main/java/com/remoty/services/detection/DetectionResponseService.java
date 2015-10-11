package com.remoty.services.detection;

import android.util.Log;

import com.remoty.services.networking.TcpSocket;
import com.remoty.gui.pages.MainActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class DetectionResponseService {

	private boolean acceptTimeoutExceeded;

	private ServerSocket acceptSocket = null;

	public boolean init() {

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
			return false;
		}

		return true;
	}

	public boolean isOpen() {
		return acceptSocket != null;
	}

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
			TcpSocket serverSocket = accept(acceptSocket);

			// Appending server to list
			if (serverSocket != null) {

				processServerResponse(serverSocket, socketList);
			}
		}

		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Stopped waiting for replies. Returning servers list.");

		return socketList;
	}

	private TcpSocket accept(ServerSocket serverSocket) {

		// Accept a new client. If there are no more pending connections, this ServerSocket.Accept()
		// will timeout thus finishing the "accepting" cycle.
		Socket socket;
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

		// Create a TcpSocket for the new client.
		return createCommunicationSocket(socket);
	}

	private TcpSocket createCommunicationSocket(Socket socket) {

		TcpSocket tcpSocket;
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

	private void processServerResponse(TcpSocket serverSocket, List<TcpSocket> socketList) {

		// Setting timeout.
		try {
			serverSocket.setTimeout(MainActivity.PING_RESPONSE_TIMEOUT);
		}
		catch (SocketException e) {
			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to set a timeout to the ServerSocket.");

			e.printStackTrace();

			// Closing socket
			try {
				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closing the ServerSocket...");

				serverSocket.close();

				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Closed the ServerSocket.");
			}
			catch (IOException e1) {
				Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.RESPONSE, "Failed to close the ServerSocket after a failed timeout set!");

				e1.printStackTrace();

				// Nothing to be done here...
			}

			// This server is broken. Continue accepting other servers.
			return;
		}

		// Adding server to the list.
		socketList.add(serverSocket);
	}
}
