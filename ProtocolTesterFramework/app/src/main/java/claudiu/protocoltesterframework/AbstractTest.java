package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public abstract class AbstractTest {

    protected int mRepeats = 10;
    protected int[] sleepingTime = {1, 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500};
    protected final static int NO_TIMEOUT = 0;

    protected TcpSocket mTCPSocket;

    protected void changeParams() throws SocketException {
    }

    /**
     * @throws IOException for easy use of send and receive
     */
    public abstract void test() throws IOException;

    public void runTests(Socket socket) throws IOException {
        connect(socket);
        changeParams();
        for (int index = 0; index < sleepingTime.length; index++) {
            Log.d(MainActivity.FRAMEWORK, "Delay: " + sleepingTime[index] + " ms");
            for (int repeat = 0; repeat < mRepeats; repeat++) {
                test();
                Log.d(MainActivity.FRAMEWORK, "Test number: " + (repeat + 1));
//                try {
//                    Thread.sleep(sleepingTime[index]);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
        disconnect();
        Log.d(MainActivity.FRAMEWORK, "Tests [" + this.toString() + "] ended.");
    }

    public abstract void getResults();

    public void connect(Socket socket) throws IOException {
        Log.d(MainActivity.FRAMEWORK, "Creating TCP socket...");
        mTCPSocket = new TcpSocket(socket,NO_TIMEOUT);
        Log.d(MainActivity.FRAMEWORK, "TCP socket created!");
    }

    public void disconnect() throws IOException {
        mTCPSocket.close();
    }
}
