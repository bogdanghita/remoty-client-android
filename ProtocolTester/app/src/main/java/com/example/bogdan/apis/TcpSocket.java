package com.example.bogdan.apis;

import android.util.Log;

import com.example.bogdan.protocoltester.AbstractMessage;
import com.example.bogdan.protocoltester.TCPRunnableApiTest;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Bogdan on 8/6/2015.
 */
public class TcpSocket {

	Socket socket;
	DataInputStream reader;
	DataOutputStream writer;

	Gson gson;

	// TODO: add functionality for setting the timeout
	/*
		NOTE: socket must be already connected!
	 */
	public TcpSocket(Socket socket) throws IOException {

		this.socket = socket;

		gson = new Gson();

		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);

		reader = new DataInputStream(socket.getInputStream());
		writer = new DataOutputStream(socket.getOutputStream());
	}

	// TODO: think of the flush call
	public void send(byte[] content) throws IOException {

		writer.writeInt(content.length);
		writer.flush();

		writer.write(content, 0, content.length);
		writer.flush();
	}

	public byte[] receive() throws IOException {

		int size = reader.readInt();

		byte[] buffer = new byte[size];

		receive(buffer, size);

		return buffer;
	}

	private void receive(byte[] buffer, int size) throws IOException {

		int cnt = 0;
		int currentSize = 0;

		// TODO: run a test and check when reader returns -1
		while (currentSize != size) {

			cnt++;

			int bytes_read = reader.read(buffer, currentSize, size - currentSize);

			// TODO: think what to do in this case
			if (bytes_read == -1) {
				continue;
			}

			currentSize += bytes_read;
		}

		Log.d("TIME", "Fragments: " + cnt);
	}

	public void sendObject(AbstractMessage message) throws IOException {

		long start = System.currentTimeMillis();

		String jsonMessage = gson.toJson(message);

		long duration = System.currentTimeMillis() - start;
		Log.d(TCPRunnableApiTest.TIME, "Serialization : " + duration + " ms");

		byte[] content = jsonMessage.getBytes("UTF-8");

		send(content);
	}

	public AbstractMessage receiveObject(Class<? extends AbstractMessage> type) throws IOException, ClassNotFoundException {

		byte[] content = receive();

		String jsonContent = new String(content, "UTF-8");

		long start = System.currentTimeMillis();

		AbstractMessage message = gson.fromJson(jsonContent, type);

		long duration = System.currentTimeMillis() - start;
		Log.d(TCPRunnableApiTest.TIME, "Deserialization : " + duration + " ms");

		return message;
	}

	public void close() throws IOException {

		socket.close();
	}
}
