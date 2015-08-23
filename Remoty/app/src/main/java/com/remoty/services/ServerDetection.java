package com.remoty.services;

import com.remoty.common.ServerInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerDetection {

    private final static long DETECTION_INTERVAL = 1000;

    private List<IDetectionListener> listeners;

    TaskScheduler timer;

    DetectionRunnable detectionRunnable;

    public void ServerDetection() {

        listeners = new LinkedList<>();

        timer = new TaskScheduler();

        detectionRunnable = new DetectionRunnable();
    }

    public void subscribe(IDetectionListener listener) {

        listeners.add(listener);
    }

    public void unsubscribe(IDetectionListener listener) {

        listeners.remove(listener);
    }

//    private void notifyListeners(List<ServerInfo> servers) {
//
//        for (IDetectionListener listener : listeners) {
//            listener.update(servers);
//        }
//    }

    public void init() {

        timer.start(detectionRunnable, DETECTION_INTERVAL);
    }

    public void start() {

    }

    public void stop() {

    }

    public void clear() {

        timer.stop();
    }

}

class DetectionRunnable implements Runnable {

    @Override
    public void run() {

        // Broadcast (UDP)

        // New connections (fresh TCP)

        // Ping all (TCP)

        // notify

    }

    private void notifyListeners(List<ServerInfo> servers) {

        // TODO: ...
    }
}
