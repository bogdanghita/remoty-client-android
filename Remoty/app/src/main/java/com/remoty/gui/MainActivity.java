package com.remoty.gui;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.remoty.R;
import com.remoty.common.ConnectionManager;
import com.remoty.common.ServerInfo;
import com.remoty.services.TaskScheduler;


public class MainActivity extends DebugActivity {

    public static final String TAG_SERVICES = "SERVICES";

    // TODO: think if we want the action bar in all fragments
    // TODO: also do some research on the action bar and AppCompatActivity

    // TODO: think of a status entry in connect page (so that the user knows its connection state and host if connected)

    // TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"

    // TODO: create a Communication Manager somewhere, and ensure the info about the connection state is not lost on activity restart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        // Checking if activity was restored from a previous state
        if (savedInstanceState == null) {

            // Adding main fragment
            MainFragment mainFragment = new MainFragment();
            getFragmentManager().beginTransaction().add(R.id.activity_main, mainFragment).addToBackStack(null).commit();
        }

        // TEST

//        testTaskScheduler();

        // END_TEST
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // TODO: handle the situation when there is no connection info.

        // Restoring the connection info from the saved instance
        ServerInfo connectionInfo = ServerInfo.retrieveFromBundle(savedInstanceState);
        ConnectionManager.setConnection(connectionInfo);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // TODO: handle the situation when there is no connection info.

        // Saving the connection info to the bundle
        ServerInfo connectionInfo = ConnectionManager.getConnection();
        ServerInfo.saveToBundle(connectionInfo, savedInstanceState);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_love:
                break;
            case R.id.action_share:
                break;
            case R.id.action_settings:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        // This tells that the item click was handled by this method (it is useful since fragments
        // also have this method and it is also called). If the action was not handled then the
        // default case of the switch returned the result of the super implementation.
        return true;
    }

    @Override
    public void onBackPressed() {

        /* Forcing back stack pop (it seems that the back stack is not popped automatically, although
        it should be). TODO: check how this behaves on multiple devices */
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void buttonDrive(View view) {

        DriveFragment driveFragment = new DriveFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.activity_main, driveFragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void buttonScore(View view) {

        ScoreFragment scoreFragment = new ScoreFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.activity_main, scoreFragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void buttonConnect(View view) {

        ConnectFragment connectFragment = new ConnectFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        transaction.replace(R.id.activity_main, connectFragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void buttonSubscribe(View view) {

    }

    public void buttonHelp(View view) {

    }

    public void buttonManualConnection(View view) {

    }

    // TEST METHODS

    public void testTaskScheduler() {

        TaskScheduler timer = new TaskScheduler();

        timer.start(new Runnable() {
            @Override
            public void run() {

                Log.d(TaskScheduler.TAG_TIMER, "Message executed. Time: " + System.currentTimeMillis());
            }
        }, 10);

        for (int i = 1; i <= 100; i += 10) {

            timer.setInterval(i);
            Log.d(TaskScheduler.TAG_TIMER, "Changed interval to: " + i);

            if (i == 1) {
                i = 0;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        timer.setInterval(200);
        Log.d(TaskScheduler.TAG_TIMER, "Changed interval to: " + 200);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.stop();
    }

    // END TEST METHODS
}
