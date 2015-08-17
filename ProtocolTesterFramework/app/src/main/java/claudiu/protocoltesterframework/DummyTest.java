package claudiu.protocoltesterframework;

import java.io.IOException;

/**
 * Created by Claudiu on 13/Aug/2015.
 */
public class DummyTest extends AbstractTest {
    @Override
    public void test() throws IOException {
        byte[] message= mTcpSocket.receive();
        mTcpSocket.send(message);
    }

    @Override
    public void getResults() {
    }
}
