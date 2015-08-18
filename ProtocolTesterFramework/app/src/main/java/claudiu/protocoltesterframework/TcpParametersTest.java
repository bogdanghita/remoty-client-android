package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public class TcpParametersTest extends AbstractTest {
    private final int parallels = 5;
    // 5 MB
    private final int BUFFER_SIZE = 5243000;
    // 10 KB
    //private final int BUFFER_SIZE = 10240;
    // 500 KB
    //private final int BUFFER_SIZE = 512000;

    @Override
    protected void changeParams() throws SocketException {
        mRepeats = 10;
        //mSleepingTime = new int[]{5000};
        mTcpSocket.setKeepAlive(true);
        mTcpSocket.setTcpNoDelay(true);
        mTcpSocket.setTimeout(10000);

        Log.d(MainActivity.FRAMEWORK, "Send buffer before: " + mSocket.getSendBufferSize());
        mTcpSocket.setSendBufferSize(BUFFER_SIZE);
        Log.d(MainActivity.FRAMEWORK, "Send buffer after: " + mSocket.getSendBufferSize());

        Log.d(MainActivity.FRAMEWORK, "Receive buffer before: " + mSocket.getReceiveBufferSize());
        mTcpSocket.setReceiveBufferSize(BUFFER_SIZE);
        Log.d(MainActivity.FRAMEWORK, "Receive buffer after: " + mSocket.getReceiveBufferSize());
    }

    @Override
    public void test() throws IOException {
        byte[] message = new byte[0];

        for (int i = 0; i < parallels; i++)
            message = mTcpSocket.receive();
        Log.d(MainActivity.FRAMEWORK, "Message sent.");

        for (int i = 0; i < parallels; i++)
            mTcpSocket.send(message);
        Log.d(MainActivity.FRAMEWORK, "Message received.");
    }

    @Override
    public void getResults() {
    }
}
