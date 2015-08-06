package com.example.bogdan.apis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created by Bogdan on 7/19/2015.
 */
public class UDPMessenger {

	private DatagramSocket mSocket;

	public UDPMessenger(String remoteIp, int remotePort, int socketTimeout) throws SocketException {

		DatagramSocket socket = new DatagramSocket();

		init(socket, remoteIp, remotePort, socketTimeout);
	}

	public UDPMessenger(String remoteIp, int remotePort, int socketTimeout, int localPort) throws SocketException {

		DatagramSocket socket = new DatagramSocket(localPort);

		init(socket, remoteIp, remotePort, socketTimeout);
	}

	private void init(DatagramSocket socket, String remoteIp, int remotePort, int socketTimeout) throws SocketException {

		this.mSocket = socket;

		mSocket.setSoTimeout(socketTimeout);

		mSocket.connect(new InetSocketAddress(remoteIp, remotePort));
	}

	public void close() {

		mSocket.disconnect();
	}

	public void setSocketTimeout(int socketTimeout) throws SocketException {

		mSocket.setSoTimeout(socketTimeout);
	}

	public void send(byte[] buffer) {

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		try {

			mSocket.send(packet);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int receive(byte[] buffer) {

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		try {

			mSocket.receive(packet);

			return packet.getLength();

		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
