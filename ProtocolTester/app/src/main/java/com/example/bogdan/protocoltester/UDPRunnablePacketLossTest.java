package com.example.bogdan.protocoltester;

import android.util.Log;

import com.example.bogdan.apis.UDPMessenger;

import java.net.SocketException;

/**
 * Created by Bogdan on 7/19/2015.
 */
public class UDPRunnablePacketLossTest implements Runnable {

	private final static long SAMPLE_CNT = 1000;

	private final static String UDP_RUNNABLE = "UDP_RUNNABLE";
	private final static String TIME = "TIME";
	private final static String STATISTICS = "STATISTICS";

	private final static String ACK = "ACK";
	private final static String NACK = "NACK";

	private final static int BUFF_LEN = 1000000;
	private final static int RECEIVE_TIMEOUT = 30;

	String ip = "192.168.0.104";
	int port = 8000;

	byte[] content;
	byte[] receiveBuffer = receiveBuffer = new byte[BUFF_LEN];

	UDPMessenger udpMessenger;

	private void init() throws SocketException {

		udpMessenger = new UDPMessenger(ip, port, RECEIVE_TIMEOUT, 9000);
		udpMessenger.setSocketTimeout(RECEIVE_TIMEOUT);
	}

	private void close() {
		udpMessenger.close();
	}

	private void send() {

		long sendStart = System.currentTimeMillis();

		udpMessenger.send(content);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private int receive() {

		Log.d(UDP_RUNNABLE, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();

		int size = udpMessenger.receive(receiveBuffer);

		long receiveInterval = System.currentTimeMillis() - receiveStart;
		Log.d(TIME, "Receive: " + receiveInterval + " ms | " + size + " bytes");

		return size;
	}

	@Override
	public void run() {

		try {
			init();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}

		long totalCnt = 0, lostCnt = 0, goodCnt = 0, meanTime = 0, maxTime = 0, minTime = 0;

		while (true) {

			if (MainActivity.stopFlag) {
				return;
			}

			if(totalCnt == SAMPLE_CNT) {
				totalCnt = lostCnt = goodCnt = meanTime = maxTime = minTime = 0;
			}

			totalCnt ++;

			long totalStart = System.currentTimeMillis();

			content = ACK.getBytes();

			int size = receive();

			if(size == -1) {
				Log.d(TIME, "Error receiving.");
				content = NACK.getBytes();
			}

			send();

			long totalInterval = System.currentTimeMillis() - totalStart;
			Log.d(TIME, "Total: " + totalInterval + " ms");


			// Sleeping "interval" milliseconds
			try {
				Thread.sleep(MainActivity.interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

