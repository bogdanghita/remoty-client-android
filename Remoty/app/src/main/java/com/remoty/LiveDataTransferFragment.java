package com.remoty;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class LiveDataTransferFragment extends DebugFragment {

    @Override
    public void onAttach (Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View parentView = super.onCreateView(inflater, container, savedInstanceState);



        return parentView;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        // NOTE: onResume() is always called immediately after onStart()

        // 1. ask the Communication manager about the connection info (port to notify the server that
        // it wants to start data transfer - let's call it notification port/socket)
        // 2. if there is info about a selected (and potentially connected) server, send the notification
        // you want to start data transfer
        // 3. receive the info about the ports that will be used for data transfer
        // 4. close the notification socket (server will wait for connections on the data transfer sockets)
        // 5. open sockets for each communication type and connect to the remote sockets
    }

    @Override
    public void onResume() {
        super.onResume();

        // sensors should start

        // start sending live messages (sockets are already connected)
    }

    @Override
    public void onPause() {
        super.onPause();

        // sensors should stop

        // stop sending live messages
    }

    @Override
    public void onStop() {
        super.onStop();

        // NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
        // or it might be restarted (onRestart -> on Start)

        // sockets should be closed (if activity will be resumed, the notification port will still be
        // available and the process will be restarted on onStart())
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDetach () {
        super.onDetach();

    }

}
