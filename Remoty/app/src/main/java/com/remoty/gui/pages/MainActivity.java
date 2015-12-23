package com.remoty.gui.pages;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remoty.R;
import com.remoty.common.events.ConnectionStateEvent;
import com.remoty.common.events.ConnectionStateEventListener;
import com.remoty.common.events.DetectionEvent;
import com.remoty.common.events.DetectionEventListener;
import com.remoty.common.servicemanager.ConnectionManager;
import com.remoty.common.other.ServerInfo;
import com.remoty.gui.items.ConnectionsListAdapter;
import com.remoty.gui.items.ServerSelectionListener;
import com.remoty.services.detection.DetectionService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


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
	public static final String SERVICES = "SERV-";
	public final static String APP = "APP-";
	public final static String LIFECYCLE = "LIFEC-";
	public static final String DETECTION = "DET-";
	public static final String BROADCAST = "BROAD-";
	public static final String RESPONSE = "RESP-";
	public static final String SERVERS_STATE_UPDATE_SERVICE = "SRVUP-";
	public static final String SERVERS_STATE_UPDATE_TASK = "SRVUPTASK-";
	public final static String KEYS = "KEYS-";

	private ActionBarDrawerToggle mDrawerToggle;
	private ConnectionsListAdapter mAdapter;

	private DetectionService serverDetection;

// =================================================================================================
// 	APP STATES
// =================================================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Configure toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Configure sidebar
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		configureNavigationDrawer(drawerLayout, toolbar);

		// Checking if we are restored from a previous state to avoid the overlapping fragments problem
		if(savedInstanceState == null) {
			// Adding initial fragment
			addInitialFragment();
		}

		// User profile
		createUserProfile();

		// Services & backend
		serverDetection = serviceManager.getActionManager().getServerDetectionService();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Updating connection and selection status
		updateConnectionStatusIndicators();
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
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				}
				else {
					mDrawerLayout.openDrawer(Gravity.LEFT);
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
//	GUI
// =================================================================================================

	private void addInitialFragment() {

		// Create a new Fragment to be placed in the activity layout
		MyConfigurationsFragment fragment = new MyConfigurationsFragment();

		// In case this activity was started with special instructions from an
		// Intent, pass the Intent's extras to the fragment as arguments
		fragment.setArguments(getIntent().getExtras());

		// Add the fragment to the 'fragment_container' FrameLayout
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
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

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.connections_layout);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);

		mAdapter = new ConnectionsListAdapter(this, serverSelectionListener);
		recyclerView.setAdapter(mAdapter);
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
// =================================================================================================

	private void startServerDetection() {

		serviceManager.getEventManager().subscribe(detectionEventListener);

		serverDetection.init();
		serverDetection.start();
	}

	private void stopServerDetection() {

		serverDetection.stop();
		serverDetection.clear();

		serviceManager.getEventManager().unsubscribe(detectionEventListener);
	}

// =================================================================================================
//	EVENT-TRIGGERED GUI UPDATES
// =================================================================================================

	/**
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
	 * Updates the connection status indicators
	 */
	private void updateConnectionStatusIndicators() {

		ConnectionManager.ConnectionState connectionState = connectionManager.getConnectionState();

		// Simple text view showing this status. To be replaced with actual indicators
		TextView currentConnectionTextView = (TextView) findViewById(R.id.connection_state_text_view);

		String text = "Connection: ";

		text += connectionState.toString();

		currentConnectionTextView.setText(text);
	}

	// Updates the available servers list
	private void updateAvailableServersList(List<ServerInfo> servers) {

		mAdapter.updateServerList(servers);
	}

// =================================================================================================
//	LISTENERS
// =================================================================================================

	DetectionEventListener detectionEventListener = new DetectionEventListener() {
		@Override
		public void update(final List<ServerInfo> servers) {

			// This is called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

//					Toast.makeText(getApplicationContext(), "Detection Event", Toast.LENGTH_LONG).show();

					// Update servers list
					updateAvailableServersList(servers);

					// Handling special case when the connection to the selected server is lost
					checkConnectionLostWithSelectedServer(servers);
				}
			});
		}
	};

	private void checkConnectionLostWithSelectedServer(List<ServerInfo> servers) {

		ConnectionManager connectionManager = serviceManager.getConnectionManager();
		ServerInfo currentSelection = connectionManager.getSelection();
		if (connectionManager.hasSelection() && !servers.contains(currentSelection)) {

			// Trigger connection lost event
			ConnectionStateEvent event = new ConnectionStateEvent(ConnectionManager.ConnectionState.NONE);
			serviceManager.getEventManager().triggerEvent(event);

			// Updating selection
			connectionManager.clearSelection();
			updateSelectionStatusIndicators();

			// Unsubscribing from connection state events
			serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
		}
	}

	ConnectionStateEventListener connectionStateEventListener = new ConnectionStateEventListener() {

		@Override
		public void stateChanged(final ConnectionManager.ConnectionState connectionState) {

			// This may be called from another thread so we need to ensure it is executed on the UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

//					Toast.makeText(getApplicationContext(), "Connection connectionState changed: " + connectionState.toString(), Toast.LENGTH_LONG).show();

					connectionManager.setConnectionState(connectionState);

					updateConnectionStatusIndicators();
				}
			});
		}
	};

	ServerSelectionListener serverSelectionListener = new ServerSelectionListener() {

		@Override
		public void serverSelected(ServerInfo server) {

//		    Toast.makeText(getApplicationContext(), "Server selected", Toast.LENGTH_LONG).show();

			// Notifying the ConnectionManager
			connectionManager.setSelection(server);
			connectionManager.setConnectionState(ConnectionManager.ConnectionState.ACTIVE);

			updateSelectionStatusIndicators();
			updateConnectionStatusIndicators();

			serviceManager.getEventManager().subscribe(connectionStateEventListener);
		}

		@Override
		public void serverDeselected() {

//		    Toast.makeText(getApplicationContext(), "Server deselected", Toast.LENGTH_LONG).show();

			connectionManager.clearSelection();
			connectionManager.setConnectionState(ConnectionManager.ConnectionState.NONE);

			updateConnectionStatusIndicators();
			updateSelectionStatusIndicators();

			serviceManager.getEventManager().unsubscribe(connectionStateEventListener);
		}
	};

// =================================================================================================
//	ADDITIONAL ITEMS
// =================================================================================================

}
