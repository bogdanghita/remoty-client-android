package com.remoty.services.detection;

import android.util.Log;

import com.google.gson.Gson;
import com.remoty.common.other.Constants;
import com.remoty.common.datatypes.Message;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;


public class DetectionBroadcastService {

	private DatagramSocket datagramSocket;
	private DatagramPacket datagramPacket;

	public void sendDetectionMessage() {

		Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "New detection cycle.");

		// Opening a random port to send the packet (initializing socket)
		try {
			datagramSocket = new DatagramSocket();
		}
		catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		try {
			datagramSocket.setBroadcast(true);

			// Obtaining all network interfaces
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

			// Broadcasting the message over all the network interfaces
			processNetworkInterfaces(interfaces);
		}
		catch (SocketException e) {
			e.printStackTrace();
			return;
		}
		finally {
			datagramSocket.close();
		}
	}

	private void processNetworkInterfaces(List<NetworkInterface> interfaces) {

		for (NetworkInterface networkInterface : interfaces) {

			Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Current interface: " +
					networkInterface.getName());

			// Checking if current interface is a loopback interface and if it is up
			if (checkInterface(networkInterface) == false) {
				continue;
			}

			processInterfaceAddresses(networkInterface);
		}
	}

	private void processInterfaceAddresses(NetworkInterface networkInterface) {

		// Processing each address of the current interface and obtaining its sendDetectionMessage address
		// If the sendDetectionMessage address is valid, sending request
		for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

			Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Interface: " +
					networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString());

			// Obtaining the sendDetectionMessage address of the current interface
			InetAddress broadcast = interfaceAddress.getBroadcast();

			// Checking if the sendDetectionMessage address is valid
			if (broadcast == null) {
				Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Interface: " +
						networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
						" - Broadcast address is null");
				continue;
			}

			// Sending message to the current address
			sendBroadcastMessage(networkInterface, interfaceAddress, broadcast);
		}
	}

	private boolean checkInterface(NetworkInterface networkInterface) {

		boolean interfaceIsLoopback = false, interfaceIsUp = false;
		try {
			interfaceIsLoopback = networkInterface.isLoopback();
			interfaceIsUp = networkInterface.isUp();
		}
		catch (SocketException e1) {
			e1.printStackTrace();
		}

		// If current interface is down or if it is loopback: continue
		if (interfaceIsLoopback || !interfaceIsUp) {
			Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Interface: " +
					networkInterface.getName() + " - is loopback or down");

			return false;
		}
		else {
			return true;
		}
	}

	private void sendBroadcastMessage(NetworkInterface networkInterface, InterfaceAddress interfaceAddress, InetAddress broadcastAddress) {

		// Sending the sendDetectionMessage packet
		// Creating packet that will be sent through the DatagramSocket on the current sendDetectionMessage address
		Message.PortMessage portMessage = new Message.PortMessage();
		portMessage.port = Constants.LOCAL_DETECTION_RESPONSE_PORT;

		Gson gson = new Gson();
		String jsonContent = gson.toJson(portMessage);
		byte[] content;
		try {
			content = jsonContent.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Abort. Serialization exception.");
			return;
		}

		datagramPacket = new DatagramPacket(content, content.length, broadcastAddress, Constants.REMOTE_DETECTION_PORT);
		try {
			datagramSocket.send(datagramPacket);
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Interface: " +
					networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
					" - Unable to send packet to" + broadcastAddress.getHostAddress() + " - Interface: " +
					networkInterface.getDisplayName());
		}

		// Displaying success message
		Log.d(Constants.APP + Constants.DETECTION + Constants.BROADCAST, "Interface: " +
				networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
				" - Request packet sent to: " + broadcastAddress.getHostAddress() + " - Interface: " +
				networkInterface.getDisplayName());
	}

	// TODO: Think if this is useful
//	private void SendMessageOnDefaultAddress() {
//
//		byte[] TO_SEND_DATA = "DISCOVER_SERVER_REQUEST".getBytes();
//
//		// Creating packet that will be sent through the DatagramSocket
//		try {
//			datagramPacket = new DatagramPacket(TO_SEND_DATA, TO_SEND_DATA.length, InetAddress.getByName("255.255.255.255"), REMOTE_DETECTION_PORT);
//			datagramSocket.send(datagramPacket);
//		} catch (Exception e) {
//			e.printStackTrace();
//			Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.BROADCAST, "Unable to send to: 255.255.255.255 (DEFAULT)");
//		}
//		Log.d(MainActivity.APP + MainActivity.DETECTION + MainActivity.BROADCAST, "Request packet sent to: 255.255.255.255 (DEFAULT)");
//	}
}
