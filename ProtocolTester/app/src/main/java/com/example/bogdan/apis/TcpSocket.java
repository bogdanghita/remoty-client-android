package com.example.bogdan.apis;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Bogdan on 8/6/2015.
 */
public class TcpSocket {

	Socket socket;
	DataInputStream reader;
	DataOutputStream writer;

	// TODO: add functionality for setting the timeout
	/*
		NOTE: socket must be already connected!
	 */
	public TcpSocket(Socket socket) throws IOException {

		this.socket = socket;

		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);

		reader = new DataInputStream(socket.getInputStream());
		writer = new DataOutputStream(socket.getOutputStream());
	}

	public void send(byte[] content, int offset, int count) throws IOException {

		writer.flush();
		writer.write(content, offset, count);
		writer.flush();
	}

	public void receive(byte[] buffer, int size) throws IOException {

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

	public void close() throws IOException {

		socket.close();
	}
}
