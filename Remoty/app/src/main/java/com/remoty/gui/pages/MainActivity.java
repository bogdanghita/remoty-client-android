package com.remoty.gui.pages;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.remoty.R;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.DetectionEvent;
import com.remoty.common.events.DetectionEventListener;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.other.ServerInfo;
import com.remoty.gui.items.ConnectionsListAdapter;
import com.remoty.gui.items.FragmentTabListener;
import com.remoty.services.detection.DetectionService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


// TODO: get more details on the thing with "in some cases the fragment is called with the empty constructor"
// See this: http://stackoverflow.com/questions/10798489/proper-way-to-give-initial-data-to-fragments

// TODO: Don't forget about the join that blocks the UI when detection closes (see if it is still doing it and make a decision)

// TODO: When the app starts both MyConfigurationsFragment and MarketFragment start. Solve it!

// TODO: The accelerometer axis are different on the Galaxy Tab 2. Investigate this case and try to find a pattern for all devices.

// TODO: See TODOs in Message.java.

// TODO: Connection and selection states are lost when user starts and closes the remote control activity. See onDestroy() for more details.

public class MainActivity extends BaseActivity {

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;
	public final static int INIT_REMOTE_CONTROL_TIMEOUT = 2000;

	// TODO: rename and see if it is relevant since at this moment only send operations are performed
	public final static int ACCELEROMETER_TIMEOUT = 100;
	public final static int CONNECT_TIMEOUT = 500;

	public final static long DETECTION_INTERVAL = 2000;
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
	public static final String SERVERS_STATE_UPDATE_SERVICE = "SRVUP-";
	public static final String SERVERS_STATE_UPDATE_TASK = "SRVUPTASK";
	public final static String KEYS = "KEYS-";

	private ActionBarDrawerToggle mDrawerToggle;

	// TODO: Talk with Alina about this. This is never assigned but it is used. Why is it needed?
	private LinearLayout container;

	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;
	public ConnectionsListAdapter mAdapter;

	// TODO: Talk with Alina about this, and see if we can get ridd of it
	public static MainActivity Instance;

	private DetectionService serverDetection;

// =================================================================================================
// 	APP STATES

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set current instance
		Instance = this;

		serverDetection = serviceManager.getActionManager().getServerDetectionService();

		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		configureToolbar(toolbar);

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		configureNavigationDrawer(drawerLayout, toolbar);

		createUserProfile();
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
		}

		// Starting server detection
		startServerDetection();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stopping connection check if necessary
		if (connectionManager.hasSelection()) {
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

		// TODO: this is clearing the state when the user starts the remote control activity
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

		mDrawerToggle.onConfigurationChanged(newConfig);
		mDrawerToggle.syncState();
	}

// =================================================================================================
//	TOOLBAR AND SIDE MENU

	private void configureToolbar(Toolbar toolbar) {

		// Setting up ActionBar and FragmentTabs in Toolbar
		setSupportActionBar(toolbar);

		TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
		tabLayout.addTab(tabLayout.newTab().setText("Remoties"));
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

		configureConnectionsLayout();
	}

	private void configureConnectionsLayout() {

		// Setting up recycleView (Listview)
		mRecyclerView = (RecyclerView) findViewById(R.id.connections_layout);

		// use a linear layout manager
		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);

		// set an adapter
		mAdapter = new ConnectionsListAdapter(this, serviceManager, connectionManager);
		mRecyclerView.setAdapter(mAdapter);
	}

	private void createUserProfile() {

		// Setting up user profile
		TextView name = (TextView) findViewById(R.id.name);
		TextView email = (TextView) findViewById(R.id.email);
		CircleImageView image = (CircleImageView) findViewById(R.id.circleView);
		image.setImageResource(R.drawable.ic_account_circle_white_48dp);
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
		DetectionEvent clearEvent = new DetectionEvent(new LinkedList<ServerInfo>());
		serviceManager.getEventManager().triggerEvent(clearEvent);

		serviceManager.getEventManager().unsubscribe(detectionEventListener);
	}

	// This is called when a server is chosen as the current connection
	public void serverSelected(ServerInfo server) {

//		Toast.makeText(getApplicationContext(), "Server selected", Toast.LENGTH_LONG).show();

		// Notifying the ConnectionManager
		connectionManager.setSelection(server);
		connectionManager.setConnectionState(ConnectionManager.ConnectionState.ACTIVE);

		updateSelectionStatusIndicators();
		updateConnectionStatusIndicators(ConnectionManager.ConnectionState.ACTIVE);

		serviceManager.getEventManager().subscribe(connectionStateEventListener);
	}

	public void serverDeselected() {

//		Toast.makeText(getApplicationContext(), "Server deselected", Toast.LENGTH_LONG).show();

		connectionManager.clearSelection();
		connectionManager.setConnectionState(ConnectionManager.ConnectionState.NONE);

		updateConnectionStatusIndicators(ConnectionManager.ConnectionState.NONE);
		updateSelectionStatusIndicators();

		serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
	}

	/**
	 * Checking connection state changes and triggering appropriate events if needed
	 *
	 * @param servers
	 */
	private void handleConnectionStateChanges(List<ServerInfo> servers) {

		ConnectionManager connectionManager = serviceManager.getConnectionManager();

		if (connectionManager.hasSelection()) {

			ServerInfo currentSelection = connectionManager.getSelection();
			ConnectionManager.ConnectionState currentConnectionState = connectionManager.getConnectionState();

			// Checking if connection with selected server was reestablished and triggering event if true
			if (currentConnectionState == ConnectionManager.ConnectionState.LOST
					&& servers.contains(currentSelection)) {

				Log.d("ADAPTER", "handleConnectionStateChanges LOST & contains");
				ConnectionStateEvent event = new ConnectionStateEvent(ConnectionManager.ConnectionState.ACTIVE);
				serviceManager.getEventManager().triggerEvent(event);

				return;
			}

			// Checking if connection with selected server was lost and triggering event if true
			if (currentConnectionState != ConnectionManager.ConnectionState.LOST
					&& !servers.contains(currentSelection)) {

				Log.d("ADAPTER", "handleConnectionStateChanges LOST & !contains");
				ConnectionStateEvent event = new ConnectionStateEvent(ConnectionManager.ConnectionState.LOST);
				serviceManager.getEventManager().triggerEvent(event);

				return;
			}
		}
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
	 * TODO: This must be reviewed after the GUI behavior for remote control fragment is defined
	 * <p/>
	 * Updates the connection status indicators (color of the side menu toggle button and maybe something in the connect area).
	 * <p/>
	 * For future uses: adapt the content of this method to the indicators type.
	 */
	private void updateConnectionStatusIndicators(ConnectionManager.ConnectionState connectionState) {

		Log.d("ADAPTER", "updateConnectionStatusIndicators " + connectionState.toString());

		// Simple text view showing this status. To be replaced with actual indicators
		TextView currentConnectionTextView = (TextView) findViewById(R.id.connection_state_text_view);

		String text = "Connection: ";

		text += connectionState.toString();

		currentConnectionTextView.setText(text);
	}

	private void updateAvailableServersList(List<ServerInfo> servers) {

		Log.d("ADAPTER", "updateAvailableServersList " + servers.size());
		if (serviceManager.getConnectionManager().hasSelection()) {
			if (!servers.contains(serviceManager.getConnectionManager().getSelection())) {

				servers.add(serviceManager.getConnectionManager().getSelection());
				Collections.sort(servers);
			}
		}

		mAdapter.setServerList(servers);
		mAdapter.notifyDataSetChanged();

		servers.remove(serviceManager.getConnectionManager().getSelection());
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

//					Toast.makeText(getApplicationContext(), "Detection Event", Toast.LENGTH_LONG).show();

					handleConnectionStateChanges(servers);

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

//					Toast.makeText(getApplicationContext(), "Connection connectionState changed: " + connectionState.toString(), Toast.LENGTH_LONG).show();

					connectionManager.setConnectionState(connectionState);

					updateConnectionStatusIndicators(connectionState);
				}
			});
		}
	};

// =================================================================================================
//	ADDITIONAL ITEMS

	public void buttonManualConnection(View view) {

	}
}
