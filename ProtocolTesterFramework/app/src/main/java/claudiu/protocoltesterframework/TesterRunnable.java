package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Claudiu on 09/Aug/2015.
 */
public class TesterRunnable implements Runnable {

    private List<AbstractTest> mTests = new ArrayList<AbstractTest>();

    public void addTest(AbstractTest abstractTest) {
        mTests.add(abstractTest);
    }

    public void runTests() throws IOException {
        Log.d(MainActivity.FRAMEWORK, "Running tests...");

        for (AbstractTest itTest : mTests) {

            Socket socket = new Socket(MainActivity.REMOTE_IP, MainActivity.REMOTE_PORT);

            itTest.runTests(socket);

            itTest.GetResults();
        }
    }

    @Override
    public void run() {
        try {
            runTests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
