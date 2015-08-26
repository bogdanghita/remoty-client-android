package com.remoty.services;

import android.util.Log;

import com.google.gson.Gson;
import com.remoty.common.Message;
import com.remoty.common.TcpSocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class Broadcaster {

	private final static int tcpPort = 10000;

	public final static int udpPortServer = 9001;

	private final static int BROADCAST_RESPONSE_TIMEOUT = 500;
	private final static int PING_RESPONSE_TIMEOUT = 500;

	private final String MSG_CODE = "REQUEST_RECEIVED";
	private final String MSG_DELIM = "%-%";

	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;

	private boolean receiveTimeoutExceeded = false;

	private final byte[] TO_SEND_DATA = "DISCOVER_SERVER_REQUEST".getBytes();

	public void broadcast() {

		// SEND PART
		Log.d("FIRST_STEP", "Starting server discovery");

		// Opening a random port to send the packet (initializing socket)
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.setBroadcast(true);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

//        // Sending packet on the 255.255.255.255 address
//        SendMessageOnDefaultAddress();

		// Broadcasting the message over all the network interfaces
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		// Iterating through all interfaces of the device
		while (interfaces.hasMoreElements()) {

			// Obtaining current interface
			NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
			Log.d("INTERFACES", "Current interface: " + networkInterface.getName());

			// Checking if current interface is a loopback interface and if it is up
			if (CheckInterface(networkInterface) == false) {
				continue;
			}

			// Processing each address of the current interface and obtaining its broadcast address
			// If the broadcast address is valid, sending request
			for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

				Log.d("INTERFACE_ADDRESS_ITER", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString());

				// Obtaining the broadcast address of the current interface
				InetAddress broadcast = interfaceAddress.getBroadcast();

				// Checking if the broadcast address is valid
				if (broadcast == null) {
					Log.d("ADDRESS_CHECK", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() + " - Broadcast address is null");
					continue;
				}

				// Sending message to the current address
				SendBroadcastMessage(networkInterface, interfaceAddress, broadcast);
			}
		}

		datagramSocket.close();
	}

	public List<TcpSocket> acceptServers() {

		// RECEIVE PART
		Log.d("SECOND_STEP", "Done looping over all network interfaces. Waiting for replies...");

		ArrayList<TcpSocket> serverList = new ArrayList<>();

		// Resetting receive timeout flag
		receiveTimeoutExceeded = false;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(tcpPort);
			serverSocket.setSoTimeout(BROADCAST_RESPONSE_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Waiting for server responses until one receive exceeds its timeout
		while (receiveTimeoutExceeded == false) {

			// Processing one response
			TcpSocket server = Accept(serverSocket);

			// Appending server to list
			if (server != null) {
				serverList.add(server);

				// Setting timeout. TODO: Think if this should be done in other part of the code
				try {
					server.setTimeout(PING_RESPONSE_TIMEOUT);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Closing the port
		datagramSocket.close();

		Log.d("SECOND_STEP", "Stopped waiting for replies. Returning servers list.");

		return serverList;
	}

	private TcpSocket Accept(ServerSocket serverSocket) {

		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			receiveTimeoutExceeded = true;
			return null;
		}

		TcpSocket tcpSocket = null;
		try {
			tcpSocket = new TcpSocket(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tcpSocket;
	}

	private void SendMessageOnDefaultAddress() {

		// Creating packet that will be sent through the DatagramSocket
		try {
			datagramPacket = new DatagramPacket(TO_SEND_DATA, TO_SEND_DATA.length, InetAddress.getByName("255.255.255.255"), udpPortServer);
			datagramSocket.send(datagramPacket);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION", "Unable to send to: 255.255.255.255 (DEFAULT)");
		}
		Log.d("DEFAULT_BROADCAST", "Request packet sent to: 255.255.255.255 (DEFAULT)");
	}

	private boolean CheckInterface(NetworkInterface networkInterface) {

		boolean interfaceIsLoopback = false, interfaceIsUp = false;
		try {
			interfaceIsLoopback = networkInterface.isLoopback();
			interfaceIsUp = networkInterface.isUp();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		// If current interface is down or if it is loopback: continue
		if (interfaceIsLoopback || !interfaceIsUp) {
			Log.d("INTERFACE_CHECK", "Interface: " + networkInterface.getName() + " - is loopback or down");

			return false;
		} else {
			return true;
		}
	}

	private void SendBroadcastMessage(NetworkInterface networkInterface, InterfaceAddress interfaceAddress, InetAddress broadcast) {

		// Sending the broadcast packet
		// Creating packet that will be sent through the DatagramSocket on the current broadcast address
		Message.PortMessage portMessage = new Message.PortMessage();
		portMessage.port = tcpPort;

		Gson gson = new Gson();
		String jsonContent = gson.toJson(portMessage);
		byte[] content;
		try {
			content = jsonContent.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.d("SENDING_PACKET", "Abort. Serialization exception.");
			return;
		}

		datagramPacket = new DatagramPacket(content, content.length, broadcast, udpPortServer);
		try {
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("SENDING_PACKET", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
					" - Unable to send packet to" + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
		}

		// Displaying success message
		Log.d("SENDING_PACKET", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
				" - Request packet sent to: " + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
	}
}
