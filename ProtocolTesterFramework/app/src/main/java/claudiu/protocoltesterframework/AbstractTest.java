package claudiu.protocoltesterframework;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public abstract class AbstractTest {

    private int mRepeats = 10;

    protected TcpSocket mTCPSocket;

    /**
     * @throws IOException for easy use of send and receive
     */
    public abstract void test() throws IOException;

    public void runTests(Socket socket) throws IOException {
        connect(socket);

        for (int i = 0; i < mRepeats; i++) {
            test();
        }

        disconnect();
    }

    public abstract void GetResults();

    public void connect(Socket socket) throws IOException {
        mTCPSocket = new TcpSocket(socket);
    }

    public void disconnect() throws IOException {
        mTCPSocket.close();
    }
}
