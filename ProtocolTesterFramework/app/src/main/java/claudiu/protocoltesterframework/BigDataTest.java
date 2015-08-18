package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public class BigDataTest extends AbstractTest {
    @Override
    protected void changeParams() throws SocketException {
        mRepeats = 5;
        mTcpSocket.setKeepAlive(false);
        //mTcpSocket.setKeepAlive(true);
        //mTcpSocket.setTcpNoDelay(false);
        mTcpSocket.setTcpNoDelay(true);
        mTcpSocket.setTimeout(10000);
    }

    @Override
    public void test() throws IOException {
        byte[] message = mTcpSocket.receive();
        Log.d(MainActivity.FRAMEWORK, "Message sent.");
        mTcpSocket.send(message);
        Log.d(MainActivity.FRAMEWORK, "Message received.");
    }

    @Override
    public void getResults() {
    }
}
