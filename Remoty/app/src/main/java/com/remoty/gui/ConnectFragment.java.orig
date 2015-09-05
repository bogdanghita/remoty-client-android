package com.remoty.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.remoty.R;
import com.remoty.common.ConnectionManager;
import com.remoty.common.IConnectionListener;
import com.remoty.common.ViewFactory;
import com.remoty.common.ServerInfo;
import com.remoty.common.IDetectionListener;
import com.remoty.services.detection.ServerDetection;

import java.util.List;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class ConnectFragment extends DebugFragment implements IDetectionListener, View.OnClickListener, IConnectionListener {

	/* There are two possible ways of handling connection lost events:
	* 1. (current one) The fragment that is currently active is responsible to report if the
	* connection is lost or something is working bad on the network
	* 2. Have a separate thread that periodically checks if the connection to the selected server
	* is active and good
	*/

    /*
	There will be two types of messages:
    1. UDP sendDetectionMessage messages; the response of the server should be a TCP connect
    2. TCP state check for servers already discovered (using the connection created on the response)
        - response should be the data for future communication (the port it listens for data transfer
     start notifications - same for all clients)
        - if a server does not respond remove it from the list and close the connection

    When user chooses a server to connect to:
    1. notify the Communication Manager and set the connection info
    2. close fragment?

    If the connection to the network is lost:
    - not sure if UDP knows about it
    - TCP should behave like a server did not respond and the list will become empty
     */

    /*
	TODO: see what's with this comment
        3. receive form server the data that will be used further
     - when such a notification is received, the server creates sockets for data transfer messages
     and sends the info about them back to the client (this will happen when the client wants to start
     data transfer - in LiveDataTransferFragment's onStart() method)
     - for every client there will be a different set of sockets (as the connection in TCP is 1 to 1)
     */

	ServerDetection serverDetection;

	LinearLayout serversLayout;
	TextView currentConnectionTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_connect, container, false);

		// TODO: think if this should be moved to another place or wrapped inside a method
		serversLayout = (LinearLayout) parentView.findViewById(R.id.servers_layout);
		currentConnectionTextView = (TextView) parentView.findViewById(R.id.current_selection_text_view);

		// TODO: think if this should be moved to another place or wrapped inside a method
		// Subscribe class as OnClickListener for the buttons in its layout
		parentView.findViewById(R.id.button_manual_connection).setOnClickListener(this);
		parentView.findViewById(R.id.button_disconnect).setOnClickListener(this);

		return parentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: Make a factory for all services like this one
		serverDetection = new ServerDetection();
	}

	@Override
	public void onStart() {
		super.onStart();

		// NOTE: onResume() is always called immediately after onStart()

		serverDetection.init();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Updating connection status
		// NOTE: This is needed here because when the connection is set by MainActivity in
		// onSaveInstanceState() this fragment is not subscribed yet and will not be notified
		updateConnectionStatus();

		// Subscribing to events
		ConnectionManager.subscribe(this);
		serverDetection.subscribe(this);

		// Start sending detection and state check messages
		serverDetection.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stop sending detection and state check messages
		serverDetection.stop();

		// Unsubscribing from events
		ConnectionManager.unsubscribe(this);
		serverDetection.unsubscribe(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
		// or it might be restarted (onRestart -> on Start)

		serverDetection.clear();
	}

	@Override
	public void connectionLost() {

		// This may be called from another thread so we need to ensure it is executed on the UI thread
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				currentConnectionTextView.setText("Selection: NONE");
			}
		});
	}

	@Override
	public void connectionEstablished(ServerInfo server) {

		// This may be called from another thread so we need to ensure it is executed on the UI thread
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				updateConnectionStatus();
			}
		});
	}

	@Override
	public void update(final List<ServerInfo> servers) {

		// This may be called from another thread so we need to ensure it is executed on the UI thread
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {

				serversLayout.removeAllViews();

				for (ServerInfo server : servers) {

					Button button = createServerButton(server);

					serversLayout.addView(button);
				}
			}
		});
	}

	private Button createServerButton(final ServerInfo server) {

		Button button = ViewFactory.getButton(ViewFactory.ButtonType.BUTTON_SERVER_INFO, getActivity());

		String text = server.name + " - " + server.ip + ":" + server.port;

		button.setText(text);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				serverSelected(server);
			}
		});

		return button;
	}

	private void serverSelected(ServerInfo server) {

		// Notifying the ConnectionManager
		ConnectionManager.setConnection(server);

		// There is no need to update the UI here since this class implements IConnectionListener
		// and will handle the event in connectionEstablished() when it will be notified

		// TODO: maybe close the fragment and open a pending fragment? Don't know, it's about UX.
	}

	/**
	 * Retrieves the current connection status from the ConnectionManager and updates the GUI components accordingly.
	 * For future uses: adapt the content of this method to the GUI type.
	 */
	private void updateConnectionStatus() {

		String text = "Selection: ";

		if (ConnectionManager.hasConnection()) {

			ServerInfo info = ConnectionManager.getConnection();

			text += info.name;
		}
		else {
			text += "NONE";
		}

		currentConnectionTextView.setText(text);
	}

	/**
	 * Listens to onClick events and calls the corresponding method to handle the event.
	 *
	 * @param v - the view that was clicked.
	 */
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.button_manual_connection:

				buttonManualConnection(v);
				break;
			case R.id.button_disconnect:

				buttonDisconnect(v);
				break;
			default:
				return;
		}
	}

	/**
	 * Handles the manual connection button click event.
	 *
	 * @param view - the manual connection button.
	 */
	public void buttonManualConnection(View view) {

	}

	/**
	 * Handles the disconnect button click event.
	 *
	 * @param view - the disconnect button.
	 */
	public void buttonDisconnect(View view) {

		ConnectionManager.clearConnection();

		// There is no need to do anything else here since this class implements IConnectionListener
		// and will handle the event in connectionLost() when it will be notified
	}
}
