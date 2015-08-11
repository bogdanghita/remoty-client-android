package com.example.bogdan.protocoltester;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

	int mLocalport = 9000;
	int mId = 0;

	ServerSocket mServerSocket;
	TcpSocket mSocket;

	Activity mActivity;

	public TCPRunnableApiTest(Activity activity) {
		this.mActivity = activity;
	}

	public byte[] generateBigData(int size) {
		byte[] result = new byte[size];

		Random rand = new Random();

		rand.nextBytes(result);

		return result;
	}

	private void init() throws IOException {

		mServerSocket = new ServerSocket(mLocalport);

		Socket s = mServerSocket.accept();

		mSocket = new TcpSocket(s);
	}

	private byte[] initPacket() throws SocketException {

		byte[] content = new byte[CHUNK_LEN];

		byte[] message = ("Hello Roxy - " + mId).getBytes();

		for (int i = 0; i < message.length; i++) {
			content[i] = message[i];
		}

		mId++;

		return content;
	}

	private void send(byte[] data) throws IOException {

		long sendStart = System.currentTimeMillis();

		mSocket.send(data);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private byte[] receive() throws IOException {

		Log.d(TIME, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();

		byte[] content = mSocket.receive();

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

//		objectTest();

		imageTest();
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

			LargeMessage message;

			try {
				message = mSocket.receiveObject(LargeMessage.class);
			} catch (IOException | ClassNotFoundException e) {
				Log.d(TIME, "RECEIVE ERROR");
				continue;
			}

			long totalInterval = System.currentTimeMillis() - totalStart;

			try {
				mSocket.sendObject(message);
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

		try {
			mSocket.send("mama".getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		while(true) {

			if (MainActivity.stopFlag) {
				return;
			}

			long start = System.currentTimeMillis();

			byte[] data;

			try {
				data = receive();
			} catch (IOException e) {
				Log.d(TIME, "RECEIVE ERROR");
				return;
			}

			try {
				mSocket.send("mama".getBytes("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			long start2 = System.currentTimeMillis();

			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

//			Bitmap bitmap2 = mSocket.receiveBitmap();

			displayImage(bitmap);

			long duration = System.currentTimeMillis() - start2;
			Log.d(TIME, "Decode + display: " + duration + " ms | " + data.length + " bytes");

			duration = System.currentTimeMillis() - start;
			Log.d(TIME, "Duration: " + duration + " ms | " + data.length + " bytes");

//			Log.d(TIME, "Duration: " + duration + " ms");
		}

	}

	public void displayImage(final Bitmap bitmap) {

		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				MainActivity.mBallDrawerView.updateScreen(bitmap);
			}
		});
	}
}
