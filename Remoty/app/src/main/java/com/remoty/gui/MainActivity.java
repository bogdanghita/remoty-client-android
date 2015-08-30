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
import com.remoty.services.threading.TaskScheduler;


public class MainActivity extends DebugActivity {

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;

	public final static long DETECTION_INTERVAL = 2000;
	public final static long ACCELEROMETER_INTERVAL = 20;

	public final static int LOCAL_DETECTION_RESPONSE_PORT = 10000;
	public final static int REMOTE_DETECTION_PORT = 9001;

	public final static int MSG_SCHEDULE = 1000;

	public static final String TAG_SERVICES = "SERVICES";

	// TODO: Don't forget about the join that blocks the UI when detection closes (see if it is still doing it and make a decision)

	// TODO: think if we want the action bar in all fragments
	// TODO: also do some research on the action bar and AppCompatActivity

	// TODO: think of a status entry in connect page (so that the user knows its connection state and host if connected)

	// TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"

	// TODO: create a Communication Manager somewhere, and ensure the info about the connection state is not lost on activity restart

	// TODO: Solve the addToBackStack() problem

	// TODO: Implement logic for opening the connect page when connection is lost (see openConnectPage())

	// TODO: Investigate and solve the problem of the static members (ConnectionManager) not destroyed after onDestroy()

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_container);

		// Checking if activity was restored from a previous state
		if (savedInstanceState == null) {

			// Adding main fragment
			MainFragment mainFragment = new MainFragment();
			getFragmentManager().beginTransaction().add(R.id.activity_main, mainFragment).commit();
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restoring the connection info from the saved instance. If there was no connection then
		// the it is set to null (returned by retrieveFromBundle())
		ServerInfo connectionInfo = ServerInfo.retrieveFromBundle(savedInstanceState);
		ConnectionManager.setConnection(connectionInfo);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Saving the connection info to the bundle
		if (ConnectionManager.hasConnection()) {

			ServerInfo connectionInfo = ConnectionManager.getConnection();
			ServerInfo.saveToBundle(connectionInfo, savedInstanceState);
		}

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

		// Clearing connection so that it is not kept in memory as a static object until the OS
		// decides to stop the process and clear the RAM
		ConnectionManager.clearConnection();
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
		}
		else {
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

		openConnectPage();
	}

	public void buttonSubscribe(View view) {

	}

	public void buttonHelp(View view) {

	}

	/**
	 * NOTE: Maybe sometimes we will not want to disconnect from the server but just warn the user that
	 * something is not ok with the connection
	 * TODO: Open connect page when the connection is lost
	 * TODO: Subscribe MainActivity to the ConnectionManager and do the job there
	 * Opens the connect page. It is called either when the user navigates to it or when the connection is lost.
	 * For future uses: adapt the content of this method to the component type of the connect page.
	 */
	private void openConnectPage() {

		ConnectFragment connectFragment = new ConnectFragment();

		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		transaction.replace(R.id.activity_main, connectFragment);

		transaction.addToBackStack(null);

		transaction.commit();
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
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		timer.setInterval(200);
		Log.d(TaskScheduler.TAG_TIMER, "Changed interval to: " + 200);
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		timer.stop();
	}

	// END TEST METHODS
}
