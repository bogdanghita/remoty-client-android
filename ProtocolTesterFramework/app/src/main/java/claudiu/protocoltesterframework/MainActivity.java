package claudiu.protocoltesterframework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends Activity {

    public static final String FRAMEWORK = "frametag";

    public static final String REMOTE_IP = "192.168.1.4";
    public static final int REMOTE_PORT = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(FRAMEWORK, "Starting app...");

        TesterRunnable runnable = new TesterRunnable();
        Thread thread = new Thread(runnable);

        //Add as many tests as you like.
        //runnable.addTest(new DummyTest());
        //runnable.addTest(new BigDataTest());
        //runnable.addTest(new ParallelTest());
        runnable.addTest(new TcpParametersTest());

        thread.start();
    }
}
