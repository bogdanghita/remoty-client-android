package com.remoty;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends DebugActivity {

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
        if(savedInstanceState == null) {

            // Adding main fragment
            MainFragment mainFragment = new MainFragment();
            getFragmentManager().beginTransaction().add(R.id.activity_main, mainFragment).addToBackStack(null).commit();
        }
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

        // TODO: see what the comment is saying
        //noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}

        switch (id) {
            case R.id.action_love:
                break;
            case R.id.action_share:
                break;
            case R.id.action_settings:
                break;
        }

        // TODO: see what return value means
        // EDIT: I think it tells that if the item click was handled by this method (it is useful since
        // fragments also have this method and is is also called after this one is). Research and confirm this.

        // TODO: See if this is ok, and why I put it like this
        // EDIT: I think that this is ok as the method should return true in the switch cases and if no
        // case matches, then the method should return whatever the super implementation wants. Not sure,
        // confirm this.
        return super.onOptionsItemSelected(item);
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

}
