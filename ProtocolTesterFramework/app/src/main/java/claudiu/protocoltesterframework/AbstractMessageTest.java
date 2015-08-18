package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public class AbstractMessageTest extends AbstractTest {

    @Override
    protected void changeParams() throws SocketException {
        mRepeats = 1;
        mSleepingTime = new int[]{0};
        mTcpSocket.setKeepAlive(true);
        mTcpSocket.setTcpNoDelay(true);
    }

    @Override
    public void test() throws IOException, ClassNotFoundException {
        LargeMessage message = mTcpSocket.receiveObject(LargeMessage.class);
        Log.d(MainActivity.FRAMEWORK, "Message sent.");

        //message.data[0]=0;

        mTcpSocket.sendObject(message);
        Log.d(MainActivity.FRAMEWORK, "Message received.");
    }

    @Override
    public void getResults() {
    }
}
