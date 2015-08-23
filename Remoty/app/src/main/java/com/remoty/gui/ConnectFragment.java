package com.remoty.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remoty.R;
import com.remoty.common.ServerInfo;
import com.remoty.services.IDetectionListener;

import java.util.List;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class ConnectFragment extends DebugFragment implements IDetectionListener {

    /*
    There will be two types of messages:
    1. UDP broadcast messages; the response of the server should be a TCP connect
    2. TCP state check for servers already discovered (using the connection created on the response)
        - response should be the data for future communication (the port it listens for data transfer
     start notifications - same for all clients)
        - if a server does not respond remove it from the list and close the connection

    When user chooses a server to connect to:
    1. stop detection and state check messages
    2. close the connected TCP socket with the server
    3. notify the Communication Manager and set the connection info
    4. close fragment?

    If the connection to the network is lost:
    - not sure if UDP knows about it
    - TCP should behave like a server did not respond and the list will become empty
     */


    // TODO: ...
    /*
        3. receive form server the data that will be used further
     - when such a notification is received, the server creates sockets for data transfer messages
     and sends the info about them back to the client (this will happen when the client wants to start
     data transfer - in LiveDataTransferFragment's onStart() method)
     - for every client there will be a different set of sockets (as the connection in TCP is 1 to 1)
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View parentView = inflater.inflate(R.layout.fragment_connect, container, false);

        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // NOTE: onResume() is always called immediately after onStart()

        // UDP sockets should be ready to send
        // TCP sockets do not exist at this point (because they are cleared in onStop)
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start sending detection and state check messages
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop sending detection and state check messages
        // All sockets should remain open; TCP sockets should remain connected
    }

    @Override
    public void onStop() {
        super.onStop();

        // NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
        // or it might be restarted (onRestart -> on Start)

        // TCP connections should be closed
        // All sockets should be closed and cleaned (TCP and UDP)
    }

    @Override
    public void update(final List<ServerInfo> servers) {

        // This is called from another thread so we need to ensure it is executed on the UI thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // TODO: update available servers list
            }
        });
    }
}
