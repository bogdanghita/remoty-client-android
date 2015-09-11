package com.remoty.gui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.DetectionEvent;
import com.remoty.common.events.DetectionEventListener;
import com.remoty.common.events.RemoteControlEvent;
import com.remoty.common.events.RemoteControlEventListener;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.ConnectionCheckService;
import com.remoty.common.servicemanager.ServiceManager;
import com.remoty.common.ServerInfo;
import com.remoty.common.ViewFactory;
import com.remoty.services.detection.DetectionService;
import com.remoty.services.threading.TaskScheduler;

import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


// TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"
// See this: http://stackoverflow.com/questions/10798489/proper-way-to-give-initial-data-to-fragments

// TODO: Don't forget about the join that blocks the UI when detection closes (see if it is still doing it and make a decision)

// TODO: When the app starts both MyConfigurationsFragment and MarketFragment start. Solve it!

public class MainActivity extends DebugActivity {

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;
	public final static int INIT_REMOTE_CONTROL_TIMEOUT = 2000;

	// TODO: rename and see if it is relevant since at this moment only send operations are performed
	public final static int ACCELEROMETER_TIMEOUT = 50;
	public final static int CONNECT_TIMEOUT = 500;

	public final static long DETECTION_INTERVAL = 2000;
	public final static long CONNECTION_CHECK_INTERVAL = 2000;
	public final static long ACCELEROMETER_INTERVAL = 20;

	public final static int LOCAL_DETECTION_RESPONSE_PORT = 10000;
	public final static int REMOTE_DETECTION_PORT = 9001;

	public final static int MSG_SCHEDULE = 1000;

	// Logging tags
	public static final String TAG_SERVICES = "SERV-";
	public final static String APP = "APP-";
	public final static String LIFECYCLE = "LIFEC-";
	public static final String DETECTION = "DET-";
	public static final String BROADCAST = "BROAD-";
	public static final String RESPONSE = "RESP-";
	public static final String PING_SERVICE = "PING-";
	public static final String PING_TASK = "PING_TASK";
	public final static String KEYS = "KEYS-";

	private ActionBarDrawerToggle mDrawerToggle;
    private ActionBarDrawerToggle mBackDrawerToggle;
	private LinearLayout container;

	// TODO: Talk with Alina about this.
	public static MainActivity Instance;
	public static Boolean homeAsBack = false;

	private ServiceManager serviceManager;
	private ConnectionManager connectionManager;
	private DetectionService serverDetection;
	private ConnectionCheckService connectionCheck;

// =================================================================================================
// 	APP STATES

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set current instance
		Instance = this;

		serviceManager = ServiceManager.getInstance();
		connectionManager = serviceManager.getConnectionManager();
		serverDetection = serviceManager.getActionManager().getServerDetectionService();
		connectionCheck = serviceManager.getActionManager().getConnectionCheckService();

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		configureToolbar(toolbar);

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		configureNavigationDrawer(drawerLayout, toolbar);

		createUserPofile();

        // keep the home icon as a back button
        if (MainActivity.Instance.homeAsBack){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        }


        // This is and not in onStart() because it needs to happen before the fragment's onStart()
		// and if the activity is recreated the fragment's onStart() is called before this onStart()
		// Subscribing to remote control start/stop events
		serviceManager.getEventManager().subscribe(remoteControlEventListener);

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restoring the connection info from the saved instance. If there was no connection then
		// the it is set to null (returned by retrieveFromBundle())
		ServerInfo connectionInfo = ServerInfo.retrieveFromBundle(savedInstanceState);
		connectionManager.setSelection(connectionInfo);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		// Saving the connection info to the bundle
		if (connectionManager.hasSelection()) {

			ServerInfo connectionInfo = connectionManager.getSelection();
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

		// Updating connection status
		updateSelectionStatusIndicators();

		// Starting connection check if necessary
		if (connectionManager.hasSelection()) {
			serviceManager.getEventManager().subscribe(connectionStateEventListener);
			startConnectionCheck();
		}

		// Starting server detection
		startServerDetection();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stopping connection check if necessary
		if (connectionManager.hasSelection()) {
			stopConnectionCheck();
			serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
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

		// For explanations about why this is done here, see comment in onCreate()
		// Unsubscribing from remote control start/stop events
		serviceManager.getEventManager().unsubscribe(remoteControlEventListener);

		// Clearing this so that it is not kept in memory as a static object until the OS
		// decides to stop the process and clear the RAM
		serviceManager.clear();
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

        Log.d("MAIN","onconfigurationchanged");

		mDrawerToggle.onConfigurationChanged(newConfig);
        mDrawerToggle.syncState();
	}

	@Override
	public void onBackPressed() {
		if(!MainActivity.Instance.homeAsBack)
			super.onBackPressed();
	}

	@Override
	public boolean onMenuOpened(final int featureId, final Menu menu) {

		if(MainActivity.Instance.homeAsBack) {
			super.onMenuOpened(featureId, menu);
			return false;
		} else {
			return true;
		}
	}

	public void disableToolbar() {
        // home button as back flag
        homeAsBack = true;

        // disable unwanted views
		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.setVisibility(View.GONE);

		LinearLayout configurationsLayout = (LinearLayout) findViewById(R.id.configurations_layout);
		configurationsLayout.setVisibility(View.GONE);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // lock side menu
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // set back functionality for home button
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
				R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(false);

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeAsBack = false;

                onBackPressed();

                enableToolbar();
            }
        });

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

		drawer.setDrawerListener(mDrawerToggle);
	}

	public void enableToolbar() {
        // enable previously disabled views
        MainActivity.Instance.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

		configureNavigationDrawer(drawer, toolbar);

		mDrawerToggle.setDrawerIndicatorEnabled(true);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.setVisibility(View.VISIBLE);

		LinearLayout configurationsLayout = (LinearLayout) findViewById(R.id.configurations_layout);
		configurationsLayout.setVisibility(View.VISIBLE);

        MainActivity.Instance.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

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

	private void createUserPofile() {
		TextView name = (TextView) findViewById(R.id.name);
		TextView email = (TextView) findViewById(R.id.email);
		CircleImageView image = (CircleImageView) findViewById(R.id.circleView);
		image.setImageResource(R.drawable.ic_account_circle_white_48dp);
		image.setBorderColor(Color.BLACK);
	}

// =================================================================================================
//	SERVICES AND EVENTS

	private void startServerDetection() {

		serviceManager.getEventManager().subscribe(detectionEventListener);

		serverDetection.init();
		serverDetection.start();
	}

	private void stopServerDetection() {

		serverDetection.stop();
		serverDetection.clear();

		// Triggering event to clear available servers list
		serviceManager.getEventManager().triggerEvent(new DetectionEvent(null));

		serviceManager.getEventManager().unsubscribe(detectionEventListener);
	}

	private void startConnectionCheck() {

		connectionCheck.init();
		connectionCheck.start();
	}

	private void stopConnectionCheck() {

		connectionCheck.stop();
		connectionCheck.clear();
	}

	// This is called when a server is chosen as the current connection
	private void serverSelected(ServerInfo server) {

		Toast.makeText(getApplicationContext(), "Server selected", Toast.LENGTH_LONG).show();

		// Notifying the ConnectionManager
		connectionManager.setSelection(server);

		updateSelectionStatusIndicators();

		serviceManager.getEventManager().subscribe(connectionStateEventListener);
		startConnectionCheck();
	}

	// TODO: call this method when the user chooses to disconnect from the current server
	private void serverDeselected() {

		Toast.makeText(getApplicationContext(), "Server deselected", Toast.LENGTH_LONG).show();

		connectionManager.clearSelection();

		updateSelectionStatusIndicators();

		stopConnectionCheck();
		serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
	}

// =================================================================================================
//	EVENT-TRIGGERED GUI UPDATES

	/**
	 * TODO: Be sure that this works when the side bar is closed
	 * Retrieves and updates the current selected server indicator (currently the indicator is a text view)
	 * For future uses: adapt the content of this method to the indicator type
	 */
	private void updateSelectionStatusIndicators() {

		TextView currentSelectionTextView = (TextView) findViewById(R.id.selection_state_text_view);

		String text = "Selection: ";

		if (connectionManager.hasSelection()) {

			ServerInfo info = connectionManager.getSelection();

			text += info.name;
		}
		else {
			text += "NONE";
		}

		currentSelectionTextView.setText(text);
	}

	/**
	 * CAUTION: This may be called when a remote control fragment is running.
	 * TODO: This must be reviewed after the GUI behavior for remote control fragment is defined
	 * <p/>
	 * Updates the connection status indicators (color of the side menu toggle button and maybe something in the connect area).
	 * <p/>
	 * For future uses: adapt the content of this method to the indicators type.
	 */
	private void updateConnectionStatusIndicators(ConnectionManager.ConnectionState connectionState) {

		// TODO: update connection status
		// - update toggle button icon color
		// - update connectionState in the connect area of the side menu

		// Simple text view showing this status. To be replaced with actual indicators
		TextView currentConnectionTextView = (TextView) findViewById(R.id.connection_state_text_view);

		String text = "Connection: ";

		text += connectionState.toString();

		currentConnectionTextView.setText(text);
	}

	private void updateAvailableServersList(List<ServerInfo> servers) {

		Toast.makeText(getApplicationContext(), "Detection Event", Toast.LENGTH_LONG).show();

		container = (LinearLayout) findViewById(R.id.connections_layout);

		container.removeAllViews();

		if (servers == null) {
			return;
		}

		for (ServerInfo server : servers) {

			Button button = createServerButton(server.name, server.ip, server.port);

			container.addView(button);
		}
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

				if (serviceManager.getConnectionManager().hasSelection()) {
					serverDeselected();
				}
				else {
					serverSelected(new ServerInfo(ip, port, hostname));
				}
			}
		});

		return button;
	}

// =================================================================================================
//	LISTENERS

	DetectionEventListener detectionEventListener = new DetectionEventListener() {
		@Override
		public void update(final List<ServerInfo> servers) {

			// This is called from another thread so we need to ensure it is executed on the UI thread
			MainActivity.Instance.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					updateAvailableServersList(servers);
				}
			});
		}
	};

	ConnectionStateEventListener connectionStateEventListener = new ConnectionStateEventListener() {

		@Override
		public void stateChanged(final ConnectionManager.ConnectionState connectionState) {

			// This may be called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Toast.makeText(getApplicationContext(), "Connection connectionState changed: " + connectionState.toString(), Toast.LENGTH_LONG).show();

					connectionManager.setConnectionState(connectionState);

					updateConnectionStatusIndicators(connectionState);

					// TODO: if connection state LOST or SLOW do something intelligent (maybe stop sending)
				}
			});
		}
	};

	RemoteControlEventListener remoteControlEventListener = new RemoteControlEventListener() {
		@Override
		public void stateChanged(final RemoteControlEvent.Action action) {

			// Posting toast from the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Toast.makeText(getApplicationContext(), "Remote control: " + action.toString(), Toast.LENGTH_LONG).show();
				}
			});

			// NOTE: This is not necessary to be run on the UI thread because it does not interact with the GUI
			if (action == RemoteControlEvent.Action.START) {

				// Stopping detection and connection check services
				stopServerDetection();
				// NOTE: No need to call hasSelection(). At this point it is guaranteed that there is one.
				stopConnectionCheck();

				// Disabling toolbar
				disableToolbar();
			}
			else if (action == RemoteControlEvent.Action.STOP) {

				// Starting detection and connection check services
				startServerDetection();
				// NOTE: No need to call hasSelection(). At this point it is guaranteed that there is one.
				startConnectionCheck();

				// Enabling toolbar
			//	enableToolbar();
			}
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
