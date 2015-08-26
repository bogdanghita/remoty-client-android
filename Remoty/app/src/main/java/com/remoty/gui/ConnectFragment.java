package com.remoty.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.remoty.R;
import com.remoty.common.ConnectionManager;
import com.remoty.common.ServerInfo;
import com.remoty.services.IDetectionListener;
import com.remoty.services.ServerDetection;

import java.util.LinkedList;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View parentView = inflater.inflate(R.layout.fragment_connect, container, false);

        serversLayout = (LinearLayout) parentView.findViewById(R.id.servers_layout);

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

        serverDetection.subscribe(this);
        serverDetection.init();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start sending detection and state check messages
        serverDetection.start();

//        buttonTest();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop sending detection and state check messages
        serverDetection.stop();
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
    public void update(final List<ServerInfo> servers) {

        // This is called from another thread so we need to ensure it is executed on the UI thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                serversLayout.removeAllViews();

                for (ServerInfo server : servers) {

                    Button button = createServerButton(server.name, server.ip, server.port);

                    serversLayout.addView(button);
                }
            }
        });
    }

    private void serverSelected(ServerInfo server) {

        // Notifying the ConnectionManager
        ConnectionManager.setConnection(server);

        // TODO: maybe close the fragment and open a pending fragment? Don't know, it's about UX.
    }

    private Button createServerButton(String hostname, String ip, int port) {

        Button button = new Button(serversLayout.getContext());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        button.setLayoutParams(params);

        String text = hostname + " - " + ip + ":" + port;

        button.setText(text);

        return button;
    }

    // TEST

    private void buttonTest() {

        List<ServerInfo> servers = new LinkedList<>();
        servers.add(new ServerInfo("192.168.1.1", 3, "Server1"));
        servers.add(new ServerInfo("192.168.1.2", 5, "Server2"));
        servers.add(new ServerInfo("666.666.666.666", 9999, "THIS IS SPARTA!"));

        update(servers);
    }

    // END TEST
}
