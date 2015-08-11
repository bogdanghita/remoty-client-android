package com.example.bogdan.protocoltester;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

/**
 * Created by Bogdan on 8/3/2015.
 */
public class TCPRunnableSimpleTest implements Runnable {

	private final static String TCP_RUNNABLE = "TCP_RUNNABLE";
	private final static String TIME = "TIME";

	private final static int BUFF_LEN = 1 * 20;
	private final static int RECEIVE_TIMEOUT = 2000;
	private final static int CHUNK_LEN = 20;

	//	String ip = "192.168.1.143";
	int port = 8000;
	int localport = 9000;

	byte[] content;
	byte[] receiveBuffer = new byte[BUFF_LEN];
	byte[] largeData;
	int id = 0;

	//	UDPMessenger udpMessenger;
	ServerSocket serverSocket;
	Socket socket;
	DataInputStream reader;
	DataOutputStream writer;

	public byte[] generateBigData(int size) {
		byte[] result = new byte[size];

		Random rand = new Random();

		rand.nextBytes(result);

		return result;
	}

	private void init() throws IOException {

		largeData = generateBigData(BUFF_LEN);

		serverSocket = new ServerSocket(localport);

		socket = serverSocket.accept();

		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);

//		mSocket = new Socket(ip, port);
//		mSocket.setSoTimeout(RECEIVE_TIMEOUT);

//		udpMessenger = new UDPMessenger(ip, port, RECEIVE_TIMEOUT, 9000);
//		udpMessenger.setSocketTimeout(RECEIVE_TIMEOUT);

//		reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		reader = new DataInputStream(socket.getInputStream());
		writer = new DataOutputStream(socket.getOutputStream());
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

		writer.flush();
//		udpMessenger.send(content);
		writer.write(content);
		writer.flush();

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private byte[] receive() throws IOException {

		Log.d(TCP_RUNNABLE, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();
//		for(byte b : receiveBuffer) {
//			b = 0;
//		}
//		receiveBuffer = new byte[BUFF_LEN];

//		int size = udpMessenger.receive(receiveBuffer);

		int size = 0;
//		String response = reader.readLine();
//		int size = response.length();

		while (size != BUFF_LEN) {
			size += reader.read(receiveBuffer, 0, BUFF_LEN);
		}

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
				receive();
			} catch (IOException e) {
//				e.printStackTrace();
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			long totalInterval = System.currentTimeMillis() - totalStart;

			sampleCnt++;
			cumulatedDuration += totalInterval;
			averageDuration = cumulatedDuration / sampleCnt;

			Log.d(TIME, "Total: " + totalInterval + " ms | Average: " + averageDuration + " ms");
		}
	}
}
