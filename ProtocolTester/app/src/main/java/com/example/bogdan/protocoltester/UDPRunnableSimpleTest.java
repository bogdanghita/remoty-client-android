package com.example.bogdan.protocoltester;

import android.util.Log;

import com.example.bogdan.apis.UDPMessenger;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Bogdan on 7/19/2015.
 */
public class UDPRunnableSimpleTest implements Runnable {

	private final static String UDP_RUNNABLE = "UDP_RUNNABLE";
	private final static String TIME = "TIME";

	private final static int BUFF_LEN = 65000;
	private final static int RECEIVE_TIMEOUT = 10000;

	String ip = "192.168.1.143";
	int port = 8000;

	byte[] content;
	byte[] receiveBuffer = receiveBuffer = new byte[BUFF_LEN];
	int id = 0;

	UDPMessenger udpMessenger;

	private void init() throws SocketException {

		udpMessenger = new UDPMessenger(ip, port, RECEIVE_TIMEOUT, 9000);
		udpMessenger.setSocketTimeout(RECEIVE_TIMEOUT);
	}

	private void close() {
		udpMessenger.close();
	}

	private void initPacket() throws SocketException {

		content = ("Hello Roxy - " + id).getBytes();
		id++;
	}

	private void send() throws IOException {

		initPacket();

		long sendStart = System.currentTimeMillis();

		udpMessenger.send(content);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private byte[] receive() throws IOException {

		Log.d(UDP_RUNNABLE, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();
//		for(byte b : receiveBuffer) {
//			b = 0;
//		}
//		receiveBuffer = new byte[BUFF_LEN];

		int size = udpMessenger.receive(receiveBuffer);

		long receiveInterval = System.currentTimeMillis() - receiveStart;
		Log.d(TIME, "Receive: " + receiveInterval + " ms | " + size + " bytes");

		return receiveBuffer;
	}

	@Override
	public void run() {

		try {
			init();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

//		initPacket();

		while (true) {

			try {

				if(MainActivity.stopFlag) {
					return;
				}

				long totalStart = System.currentTimeMillis();

//				send();

				receive();

//				send();

				long totalInterval = System.currentTimeMillis() - totalStart;
				Log.d(TIME, "Total: " + totalInterval + " ms");

			} catch (IOException e) {
				e.printStackTrace();
			}

			// Sleeping "interval" milliseconds
			try {
				Thread.sleep(MainActivity.interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
