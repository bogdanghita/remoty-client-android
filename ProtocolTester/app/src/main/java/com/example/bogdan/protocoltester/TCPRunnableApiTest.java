package com.example.bogdan.protocoltester;

import android.util.Log;

import com.example.bogdan.apis.TcpSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

/**
 * Created by Bogdan on 8/3/2015.
 */
public class TCPRunnableApiTest implements Runnable {

	private final static String TCP_RUNNABLE = "TCP_RUNNABLE";
	private final static String TIME = "TIME";

	private final static int BUFF_LEN = 100 * 1000;
	private final static int RECEIVE_TIMEOUT = 2000;
	private final static int CHUNK_LEN = 20;

	int localport = 9000;

	byte[] content;
	byte[] receiveBuffer = new byte[BUFF_LEN];
	byte[] largeData;
	int id = 0;

	ServerSocket serverSocket;
	TcpSocket socket;

	public byte[] generateBigData(int size) {
		byte[] result = new byte[size];

		Random rand = new Random();

		rand.nextBytes(result);

		return result;
	}

	private void init() throws IOException {

		largeData = generateBigData(BUFF_LEN);

		serverSocket = new ServerSocket(localport);

		Socket s = serverSocket.accept();

		socket = new TcpSocket(s);
	}

	private void close() throws IOException {

		socket.close();
	}

	private void initPacket() throws SocketException {

		content = new byte[CHUNK_LEN];

		byte[] message = ("Hello Roxy - " + id).getBytes();

		for (int i = 0; i < message.length; i++) {
			content[i] = message[i];
		}

		id++;
	}

	private void send() throws IOException {

		initPacket();

		long sendStart = System.currentTimeMillis();

		socket.send(content, 0, content.length);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private void sendReceivedLargeData() throws IOException {

		long sendStart = System.currentTimeMillis();

		socket.send(receiveBuffer, 0, receiveBuffer.length);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private byte[] receiveLargeData() throws IOException {

		Log.d(TIME, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();

		socket.receive(receiveBuffer, BUFF_LEN);

		long receiveInterval = System.currentTimeMillis() - receiveStart;
		Log.d(TIME, "Receive: " + receiveInterval + " ms | size: " + BUFF_LEN + " bytes");

		return receiveBuffer;
	}

	@Override
	public void run() {

		try {
			init();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}

		int averageDuration, cumulatedDuration = 0;
		int sampleCnt = 0;

		while (true) {

			if (MainActivity.stopFlag) {
				return;
			}

			// Sleeping "interval" milliseconds
			try {
				Thread.sleep(MainActivity.interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			long totalStart = System.currentTimeMillis();

			try {
				send();
			} catch (IOException e) {
//				e.printStackTrace();
				Log.d(TIME, "SEND ERROR");
				continue;
			}

			try {
				receiveLargeData();
			} catch (IOException e) {
//				e.printStackTrace();
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			long totalInterval = System.currentTimeMillis() - totalStart;

			try {
				sendReceivedLargeData();
			} catch (IOException e) {
//				e.printStackTrace();
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			sampleCnt++;
			cumulatedDuration += totalInterval;
			averageDuration = cumulatedDuration / sampleCnt;

			Log.d(TIME, "Total: " + totalInterval + " ms | Average: " + averageDuration + " ms");
		}
	}
}
