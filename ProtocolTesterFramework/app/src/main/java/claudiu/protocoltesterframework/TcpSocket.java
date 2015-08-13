package claudiu.protocoltesterframework;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Bogdan on 8/6/2015.
 */
public class TcpSocket {

    public final static String JSON = "JSON";

    Socket mSocket;
    DataInputStream mReader;
    DataOutputStream mWriter;

    Gson mGson;

    // TODO: add functionality for setting the timeout
	/*
		NOTE: mSocket must be already connected!
	 */
    public TcpSocket(Socket socket) throws IOException {

        this.mSocket = socket;

        mGson = new Gson();

        socket.setKeepAlive(true);
        socket.setTcpNoDelay(true);

        mReader = new DataInputStream(socket.getInputStream());
        mWriter = new DataOutputStream(socket.getOutputStream());
    }

    // TODO: think of the flush call
    public void send(byte[] content) throws IOException {

        mWriter.writeInt(content.length);
        mWriter.flush();

        mWriter.write(content, 0, content.length);
        mWriter.flush();
    }

    public byte[] receive() throws IOException {

        int size = mReader.readInt();

        byte[] buffer = new byte[size];

        receive(buffer, size);

        return buffer;
    }

    private void receive(byte[] buffer, int size) throws IOException {

        int cnt = 0;
        int currentSize = 0;

        // TODO: run a runTests and check when mReader returns -1
        while (currentSize != size) {

            cnt++;

            int bytes_read = mReader.read(buffer, currentSize, size - currentSize);

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

        String jsonMessage = mGson.toJson(message);

        long duration = System.currentTimeMillis() - start;
        Log.d(JSON, "Serialization : " + duration + " ms");

        byte[] content = jsonMessage.getBytes("UTF-8");

        send(content);
    }

    public <T extends AbstractMessage> T receiveObject(Class<T> type) throws IOException, ClassNotFoundException {

        byte[] content = receive();

        String jsonContent = new String(content, "UTF-8");

        long start = System.currentTimeMillis();

        T message = mGson.fromJson(jsonContent, type);

        long duration = System.currentTimeMillis() - start;
        Log.d(JSON, "Deserialization : " + duration + " ms");

        return message;
    }

    // Receives image directly from the network (it seems Android can receive only images in .png format using this method)
    public Bitmap receiveBitmap() {

        Bitmap bitmap = BitmapFactory.decodeStream(mReader);

        return bitmap;
    }

    public void close() throws IOException {

        mSocket.close();
    }
}
