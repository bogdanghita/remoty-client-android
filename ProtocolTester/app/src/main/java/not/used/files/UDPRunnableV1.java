package not.used.files;

import android.util.Log;

import com.example.bogdan.protocoltester.MainActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by Bogdan on 7/18/2015.
 */
public class UDPRunnableV1 implements Runnable {

	private final static String UDP_RUNNABLE = "UDP_RUNNABLE";
	private final static String TIME = "TIME";

	private final static int BUFF_LEN = 1000000;
	private final static int RECEIVE_TIMEOUT = 10000;

	String ip = "192.168.1.143";
	int port = 8000;

	private DatagramSocket mSocket;
	private DatagramPacket mPacket;
	SocketAddress socketAddress;
//	SocketAddress localAddress;

	byte[] content;
	byte[] receiveBuffer = receiveBuffer = new byte[BUFF_LEN];
	int id = 0;

	private void init() throws SocketException {

		mSocket = new DatagramSocket(9000);
		mSocket.setSoTimeout(RECEIVE_TIMEOUT);

//		localAddress = new InetSocketAddress("0.0.0.0", 9000);

//		mSocket.bind(localAddress);

		socketAddress = new InetSocketAddress(ip, port);

//		mSocket.connect(socketAddress);
	}

	private void close() {
//		mSocket.disconnect();
	}

	private void initPacket() throws SocketException {

		content = ("Hello Roxy - " + id).getBytes();
		id++;

		mPacket = new DatagramPacket(content, content.length, socketAddress);

//		mPacket = new DatagramPacket(content, content.length);
	}

	private void send() throws IOException {

		initPacket();

		long sendStart = System.currentTimeMillis();

		mSocket.send(mPacket);

		long sendInterval = System.currentTimeMillis() - sendStart;
		Log.d(TIME, "Send: " + sendInterval + " ms");
	}

	private DatagramPacket receive() throws IOException {

		Log.d(UDP_RUNNABLE, "Waiting to receive...");

		long receiveStart = System.currentTimeMillis();
//		for(byte b : receiveBuffer) {
//			b = 0;
//		}
//		receiveBuffer = new byte[BUFF_LEN];

		DatagramPacket receivedPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

//		long receiveStart = System.currentTimeMillis();

		mSocket.receive(receivedPacket);

		long receiveInterval = System.currentTimeMillis() - receiveStart;
		Log.d(TIME, "Receive: " + receiveInterval + " ms | " + receivedPacket.getLength() + " bytes");

		return receivedPacket;
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
