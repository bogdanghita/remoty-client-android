package com.remoty.services.networking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.remoty.common.datatypes.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;


public class TcpSocket {

	public final static String JSON = "JSON";
	private final static String IMPOSSIBLE = "Impossible just happened.";

	private Socket mSocket;
	private DataInputStream mReader;
	private DataOutputStream mWriter;

	private Gson mGson;

	/**
	 * Default values for a {@link Socket} are:<p>
	 * - setTimeout(0); it means infinite timeout<p>
	 * - setKeepAlive(false); connection can enter an idle state<p>
	 * - setTcpNoDelay(false); packages are compressed into a larger package before they are sent
	 * - setSentBufferSize(1048576); send buffer has 1 MB<p>
	 * - setReceiveBufferSize(1048576); receive buffer has 1 MB<p>
	 *
	 * @param socket socket connected to a remote host address and port
	 * @throws IOException
	 */
	public TcpSocket(Socket socket) throws IOException {

		this.mSocket = socket;

		mReader = new DataInputStream(socket.getInputStream());
		mWriter = new DataOutputStream(socket.getOutputStream());

		mGson = new Gson();
	}

	/**
	 * Gets this socket's {@link InetAddress}.
	 *
	 * @return internal {@link InetAddress}
	 */
	public InetAddress getInetAddress() {
		return mSocket.getInetAddress();
	}

	/**
	 * Sets this socket's read timeout.
	 *
	 * @param timeout read timeout in milliseconds (0 means no timeout)
	 * @throws SocketException
	 */
	public void setTimeout(int timeout) throws SocketException {
		mSocket.setSoTimeout(timeout);
	}

	/**
	 * Sets this socket's SO_KEEPALIVE option.
	 *
	 * @param keepAlive
	 * @throws SocketException
	 */
	public void setKeepAlive(boolean keepAlive) throws SocketException {

		mSocket.setKeepAlive(keepAlive);
	}

	/**
	 * Sets this socket's TCP_NODELAY option.
	 *
	 * @param on
	 * @throws SocketException
	 */
	public void setTcpNoDelay(boolean on) throws SocketException {

		mSocket.setTcpNoDelay(true);
	}

	/**
	 * Sets this socket's send buffer size.
	 *
	 * @param bufferSize
	 */
	public void setSendBufferSize(int bufferSize) throws SocketException {

		mSocket.setSendBufferSize(bufferSize);
	}

	/**
	 * Sets this socket's receive buffer size.
	 *
	 * @param bufferSize
	 */
	public void setReceiveBufferSize(int bufferSize) throws SocketException {

		mSocket.setReceiveBufferSize(bufferSize);
	}

	/**
	 * Closes the socket. All data is transferred before closing the connection.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {

		// This is the proper way to fully close a TCP Socket.
		mSocket.shutdownInput();
		mSocket.shutdownOutput();

		// TODO: See if isClosed() is necessary.
		// isClosed() tells you whether you have closed this socket. Until you have, it returns false.
		if (!mSocket.isClosed()) {
			mSocket.close();
		}

		mReader.close();
		mWriter.close();
	}

	/**
	 * Sends a message in the form of byte array.
	 *
	 * @param content the content to be sent
	 * @throws IOException
	 */
	public void send(byte[] content) throws IOException {

		mWriter.writeInt(content.length);
		mWriter.flush();

		mWriter.write(content, 0, content.length);
		mWriter.flush();
	}

	/**
	 * Receives a message in the form of byte array.
	 *
	 * @return the content received
	 * @throws IOException
	 */
	public byte[] receive() throws IOException {

		int size = mReader.readInt();

		byte[] buffer = new byte[size];

		receive(buffer, size);

		return buffer;
	}

	private void receive(byte[] buffer, int size) throws IOException {

		int cnt = 0;
		int currentSize = 0;

		while (currentSize != size) {

			cnt++;

			int bytes_read = mReader.read(buffer, currentSize, size - currentSize);

			/* This should not happen. Even Android Studio marks any breakpoint inside this if statement as invalid. */
			if (bytes_read == -1) {
				throw new IOException(IMPOSSIBLE);
			}

			currentSize += bytes_read;
		}

		//Log.d("Tag", "Fragments: " + cnt);
	}

	/**
	 * Sends an object of type AbstractMessage or any subclass of it.
	 *
	 * @param message the object to be sent
	 * @throws IOException
	 */
	public void sendObject(Message.AbstractMessage message) throws IOException {

		long start = System.currentTimeMillis();

		String jsonMessage = mGson.toJson(message);

		long duration = System.currentTimeMillis() - start;
		Log.d(JSON, "Serialization : " + duration + " ms");

		byte[] content = jsonMessage.getBytes("UTF-8");

		send(content);
	}

	/**
	 * Receives a message of type AbstractMessage or any subclass of it.
	 *
	 * @param type the type of the message that is received
	 * @param <T>  the type of the message that is received
	 * @return the object received
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public <T extends Message.AbstractMessage> T receiveObject(Class<T> type) throws IOException, ClassNotFoundException {

		byte[] content = receive();

		String jsonContent = new String(content, "UTF-8");

		long start = System.currentTimeMillis();

		T message = mGson.fromJson(jsonContent, type);

		long duration = System.currentTimeMillis() - start;
		Log.d(JSON, "Deserialization : " + duration + " ms");

		return message;
	}

	/**
	 * Receives image directly from the network (it seems Android can receive only images in .png format using this method).
	 *
	 * @return the image received
	 */
	public Bitmap receiveBitmap() {

		Bitmap bitmap = BitmapFactory.decodeStream(mReader);

		return bitmap;
	}
}
