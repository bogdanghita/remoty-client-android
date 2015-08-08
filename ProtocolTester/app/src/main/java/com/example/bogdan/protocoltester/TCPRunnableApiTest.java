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

	public final static String TIME = "TIME";

	private final static int RECEIVE_TIMEOUT = 2000;
	private final static int CHUNK_LEN = 20;

	int localport = 9000;
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

		serverSocket = new ServerSocket(localport);

		Socket s = serverSocket.accept();

		socket = new TcpSocket(s);
	}

	private byte[] initPacket() throws SocketException {

		byte[] content = new byte[CHUNK_LEN];

		byte[] message = ("Hello Roxy - " + id).getBytes();

		for (int i = 0; i < message.length; i++) {
			content[i] = message[i];
		}

		id++;

		return content;
	}

	private void send(byte[] data) throws IOException {

		long sendStart = System.currentTimeMillis();

		socket.send(data);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private byte[] receive() throws IOException {

		Log.d(TIME, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();

		byte[] content = socket.receive();

		long receiveInterval = System.currentTimeMillis() - receiveStart;

		Log.d(TIME, "Receive: " + receiveInterval + " ms | size: " + content.length + " bytes");

		return content;
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

//		bytesTest();

		objectTest();

//		imageTest();
	}

	private void bytesTest() {

		int averageDuration, totalDuration = 0;
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
				byte[] content = initPacket();
				send(content);
			} catch (IOException e) {
				Log.d(TIME, "SEND ERROR");
				continue;
			}

			byte[] data;

			try {
				data = receive();
			} catch (IOException e) {
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			long totalInterval = System.currentTimeMillis() - totalStart;

			try {
				send(data);
			} catch (IOException e) {
				Log.d(TIME, "SEND ERROR");
				continue;
			}

			sampleCnt++;
			totalDuration += totalInterval;
			averageDuration = totalDuration / sampleCnt;

			Log.d(TIME, "Total: " + totalInterval + " ms | Average: " + averageDuration + " ms");
		}
	}

	private void objectTest() {

		int averageDuration, totalDuration = 0;
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
				byte[] content = initPacket();
				send(content);
			} catch (IOException e) {
				Log.d(TIME, "SEND ERROR");
				continue;
			}

			AbstractMessage message;

			try {
				message = socket.receiveObject(LargeMessage.class);
			} catch (IOException | ClassNotFoundException e) {
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			long totalInterval = System.currentTimeMillis() - totalStart;

			try {
				socket.sendObject(message);
			} catch (IOException e) {
				Log.d(TIME, "SEND ERROR");
				continue;
			}

			sampleCnt++;
			totalDuration += totalInterval;
			averageDuration = totalDuration / sampleCnt;

			Log.d(TIME, "Total: " + totalInterval + " ms | Average: " + averageDuration + " ms");
		}
	}

	public void imageTest() {

	}
}
