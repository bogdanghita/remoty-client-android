package com.remoty.gui;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.Button;
import android.widget.LinearLayout;

import com.remoty.R;
import com.remoty.common.ConnectionManager;
import com.remoty.common.IDetectionListener;
import com.remoty.common.ISendErrorListener;
import com.remoty.common.ServerInfo;
import com.remoty.services.threading.TaskScheduler;

import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements IDetectionListener, ISendErrorListener {

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;
	public final static int INIT_REMOTE_CONTROL_TIMEOUT = 2000;

	// TODO: rename and see if it is relevant since at this moment only send operations are performed
	public final static int ACCELEROMETER_TIMEOUT = 50;

	public final static long DETECTION_INTERVAL = 2000;
	public final static long ACCELEROMETER_INTERVAL = 20;

	public final static int LOCAL_DETECTION_RESPONSE_PORT = 10000;
	public final static int REMOTE_DETECTION_PORT = 9001;

	public final static int MSG_SCHEDULE = 1000;

	// Logging tags
	public static final String TAG_SERVICES = "SERVICES";

	public final static String LIFECYCLE = "LIFECYCLE";

	public static final String DETECTION = "DETECTION";

	public static final String BROADCAST = "BROADCAST";

	public static final String RESPONSE = "RESPONSE";

	public static final String PING_SERVICE = "PING_SERVICE";

	public static final String PING_TASK = "PING_TASK";

	private ActionBarDrawerToggle mDrawerToggle;
	public static LinearLayout container;

	public static MainActivity Instance;

	// TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"

	// TODO: Don't forget about the join that blocks the UI when detection closes (see if it is still doing it and make a decision)

	// TODO: Implement logic for opening the connect page when connection is lost (see openConnectPage())

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set current instance
		Instance = this;

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		configureToolbar(toolbar);

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		configureNavigationDrawer(drawerLayout, toolbar);
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

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		switch (id) {
			case R.id.home: {
				if (mDrawerLayout.isDrawerOpen(container)) {
					mDrawerLayout.closeDrawer(container);
				}
				else {
					mDrawerLayout.openDrawer(container);
				}
				break;
			}
			case R.id.action_help:
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
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void configureToolbar(Toolbar toolbar) {

		// Setting up ActionBar and FragmentTabs in Toolbar
		setSupportActionBar(toolbar);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.addTab(tabLayout.newTab().setText("My Configurations"));
		tabLayout.addTab(tabLayout.newTab().setText("Market"));
		tabLayout.addTab(tabLayout.newTab().setText("Social"));
		tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

		// Setting Tab logic and fragment container
		final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		final PagerAdapter adapter = new FragmentTabListener(getSupportFragmentManager(), tabLayout.getTabCount());
		viewPager.setAdapter(adapter);
		viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
	}

	private void configureNavigationDrawer(DrawerLayout mDrawerLayout, Toolbar toolbar) {

		// Setting up Navigation Drawer
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

			@Override
			public void onDrawerClosed(View v) {
				super.onDrawerClosed(v);

				invalidateOptionsMenu();
				syncState();
			}

			@Override
			public void onDrawerOpened(View v) {
				super.onDrawerOpened(v);

				// This is just for testing
//				MainActivity.Instance.update(generateTestList());

				invalidateOptionsMenu();
				syncState();
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle.syncState();
	}

	/**
	 * TODO: Review this.
	 * NOTE: Maybe sometimes we will not want to disconnect from the server but just warn the user that
	 * something is not ok with the connection
	 * TODO: Open connect page when the connection is lost
	 * TODO: Subscribe MainActivity to the ConnectionManager and do the job there
	 * Opens the connect page. It is called either when the user navigates to it or when the connection is lost.
	 * For future uses: adapt the content of this method to the component type of the connect page.
	 */
	private void openConnectPage() {

	}

	public void buttonHelp(View view) {

	}

	public void buttonManualConnection(View view) {

	}

	private Button createServerButton(String hostname, String ip, int port) {

		Button button = new Button(this.getApplicationContext());

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		button.setLayoutParams(params);

		// Commented this so that we have a visual feedback when pressing the buttons
		// The buttons appearance will be set when the design will be ready
//        button.setBackgroundColor(Color.TRANSPARENT);
//        button.setTextColor(Color.DKGRAY);

		String text = hostname + " - " + ip + ":" + port;

		button.setText(text);

		return button;
	}

	@Override
	public void update(final List<ServerInfo> servers) {

		// This is called from another thread so we need to ensure it is executed on the UI thread
		MainActivity.Instance.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				container = (LinearLayout) findViewById(R.id.connections_layout);

				container.removeAllViews();

				for (ServerInfo server : servers) {

					Button button = createServerButton(server.name, server.ip, server.port);

					container.addView(button);
				}
			}
		});
	}

	@Override
	public void notifySendError() {


	}

// =================================================================================================

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

	private List<ServerInfo> generateTestList() {

		List<ServerInfo> servers = new LinkedList<>();

		servers.add(new ServerInfo("192.168.1.1", 8000, "Server1"));
		servers.add(new ServerInfo("192.168.1.132", 9000, "Server2"));

		return servers;
	}

	// END TEST METHODS
}
