package com.remoty.services;

import android.os.Handler;
import android.os.Message;

import com.remoty.common.ServerInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerDetection {

    private final static int DETECT_SERVER_MSG = 1000;

    private List<IDetectionListener> listeners;

    private List<ServerInfo> servers;

    public void ServerDetection() {

        listeners = new LinkedList<>();

        servers = new LinkedList<>();
    }

    public void subscribe(IDetectionListener listener) {

        listeners.add(listener);
    }

    public void unsubscribe(IDetectionListener listener) {

        listeners.remove(listener);
    }

    private void notifyListeners(List<ServerInfo> servers) {

        for (IDetectionListener listener : listeners) {
            listener.update(servers);
        }
    }

    public void start() {

    }

    public void stop() {

    }
}
