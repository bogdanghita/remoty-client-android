package com.remoty.gui;

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
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.abc.events.ConnectionCheckListener;
import com.remoty.common.ConnectionCheckService;
import com.remoty.abc.servicemanager.ServiceManager;
import com.remoty.abc.servicemanager.StateManager;
import com.remoty.abc.events.DetectionListener;
import com.remoty.common.ServerInfo;
import com.remoty.common.ViewFactory;
import com.remoty.services.detection.DetectionService;
import com.remoty.services.threading.TaskScheduler;

import java.util.LinkedList;
import java.util.List;


// TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"

// TODO: Don't forget about the join that blocks the UI when detection closes (see if it is still doing it and make a decision)

// TODO: Implement logic for opening the connect page when connection is lost (see openConnectPage())

public class MainActivity extends AppCompatActivity {

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;
	public final static int INIT_REMOTE_CONTROL_TIMEOUT = 2000;

	// TODO: rename and see if it is relevant since at this moment only send operations are performed
	public final static int ACCELEROMETER_TIMEOUT = 50;

	public final static long DETECTION_INTERVAL = 2000;
	public final static long CONNECTION_CHECK_INTERVAL = 2000;
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
	private LinearLayout container;

	// TODO: Talk with Alina about this.
	public static MainActivity Instance;

	private ServiceManager serviceManager;
	private StateManager stateManager;
	private DetectionService serverDetection;
	private ConnectionCheckService connectionCheck;

// =================================================================================================
// 	LIFECYCLE STATES

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set current instance
		Instance = this;

		serviceManager = ServiceManager.getInstance();
		stateManager = serviceManager.getStateManager();
		serverDetection = serviceManager.getActionManager().getServerDetectionService();
		connectionCheck = serviceManager.getActionManager().getConnectionCheckService();

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
		stateManager.setSelection(connectionInfo);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Saving the connection info to the bundle
		if (stateManager.hasSelection()) {

			ServerInfo connectionInfo = stateManager.getSelection();
			ServerInfo.saveToBundle(connectionInfo, savedInstanceState);
		}

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Start detection
	}

	@Override
	public void onResume() {
		super.onResume();

		// Updating connection status
		updateSelectedConnection();

		// Starting connection check if necessary
		if (stateManager.hasSelection()) {
			startConnectionCheck();
		}

		// Starting server detection
		startServerDetection();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stopping connection check if necessary
		if (stateManager.hasSelection()) {
			stopConnectionCheck();
		}

		// Stopping server detection
		stopServerDetection();
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
		stateManager.clearSelection();
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

// =================================================================================================
//	TOOLBAR AND SIDE MENU

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
			public void onDrawerOpened(View v) {
				super.onDrawerOpened(v);

				// This is just for testing
//				MainActivity.Instance.update(generateTestList());

				invalidateOptionsMenu();
				syncState();
			}

			@Override
			public void onDrawerClosed(View v) {
				super.onDrawerClosed(v);

				invalidateOptionsMenu();
				syncState();
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mDrawerToggle.syncState();
	}

// =================================================================================================
//	SERVICES AND EVENTS

	private void startServerDetection() {

		serviceManager.getEventManager().subscribe(detectionListener);

		serverDetection.init();
		serverDetection.start();
	}

	private void stopServerDetection() {

		serverDetection.stop();
		serverDetection.clear();

		serviceManager.getEventManager().unsubscribe(detectionListener);
	}

	private void startConnectionCheck() {

		serviceManager.getEventManager().subscribe(connectionCheckListener);

		connectionCheck.start();
	}

	private void stopConnectionCheck() {

		connectionCheck.stop();

		serviceManager.getEventManager().unsubscribe(connectionCheckListener);
	}

	// This is called when a server is chosen as the current connection
	private void serverSelected(ServerInfo server) {

		// Notifying the StateManager
		stateManager.setSelection(server);

		updateSelectedConnection();
	}

	// TODO: call this method when the user chooses to disconnect from the current server
	private void serverDeselected(ServerInfo server) {

		stateManager.clearSelection();

		updateSelectedConnection();
	}

	/**
	 * TODO: Be sure that this works when the side bar is closed
	 * Retrieves and updates the current selected server indicator (currently the indicator is a text view)
	 * For future uses: adapt the content of this method to the indicator type
	 */
	private void updateSelectedConnection() {

		TextView currentConnectionTextView = (TextView) findViewById(R.id.current_selection_text_view);

		String text = "Selection: ";

		if (stateManager.hasSelection()) {

			ServerInfo info = stateManager.getSelection();

			text += info.name;
		}
		else {
			text += "NONE";
		}

		currentConnectionTextView.setText(text);
	}

	/**
	 * Updates the connection status indicators (color of the side menu toggle button and maybe something in the connect area).
	 * <p/>
	 * For future uses: adapt the content of this method to the indicators type.
	 */
	private void updateConnectionStatusIndicators(StateManager.State state) {

		// TODO: update connection status
		// - update Icon
		// - update state of the connect area in the side menu
	}

// =================================================================================================
//	ADDITIONAL ITEMS

	public void buttonManualConnection(View view) {

	}

	// TODO: move this in a better place (maybe in a view factory of something)
	private Button createServerButton(final String hostname, final String ip, final int port) {

		String text = hostname + " - " + ip + ":" + port;

		Button button = ViewFactory.getButton(getApplicationContext(), ViewFactory.ButtonType.BUTTON_SERVER_INFO, text);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Toast.makeText(getApplicationContext(), "Server selected", Toast.LENGTH_LONG).show();

				serverSelected(new ServerInfo(ip, port, hostname));
			}
		});

		return button;
	}

// =================================================================================================
//	LISTENERS

	DetectionListener detectionListener = new DetectionListener() {
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
	};

	ConnectionCheckListener connectionCheckListener = new ConnectionCheckListener() {

		@Override
		public void stateChanged(final StateManager.State state) {

			// This may be called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					stateManager.setState(state);

					updateConnectionStatusIndicators(state);

					Toast.makeText(getApplicationContext(), "Connection state changed: " + state.toString(), Toast.LENGTH_LONG).show();
				}
			});
		}
	};

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
