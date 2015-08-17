package claudiu.protocoltesterframework;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public void runTests() throws IOException, ClassNotFoundException {
        Log.d(MainActivity.FRAMEWORK, "Running tests...");

        for (AbstractTest itTest : mTests) {
            Log.d(MainActivity.FRAMEWORK, "Connecting...");
            Socket socket;
            while (true) {
                try {
                    socket = new Socket(MainActivity.REMOTE_IP, MainActivity.REMOTE_PORT);
                } catch (IOException e) {
                    continue;
                }
                break;
            }
            Log.d(MainActivity.FRAMEWORK, "Connected!");

            itTest.runTests(socket);
            itTest.getResults();
        }
    }

    @Override
    public void run() {
        try {
            runTests();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
