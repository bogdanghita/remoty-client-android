package claudiu.protocoltesterframework;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;


public class MainActivity extends ActionBarActivity {

    public static final String FRAMEWORK = "frametag";
    public static final String REMOTE_IP = "192.168.1.3";
    public static final int REMOTE_PORT = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(FRAMEWORK, "Starting app...");

        TesterRunnable runnable = new TesterRunnable();
        Thread thread = new Thread(runnable);

        //Add as many tests as you like.
        runnable.addTest(new DummyTest());

        thread.start();
    }
}
