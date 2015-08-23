package com.remoty.services;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.AbstractMessage;
import com.remoty.common.ServerInfo;
import com.remoty.common.TcpSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Bogdan on 8/22/2015.
 */
public class ServerDetection {

    private final static long DETECTION_INTERVAL = 1000;

    private List<IDetectionListener> listeners;

    TaskScheduler timer;

    DetectionRunnable detectionRunnable;

    public ServerDetection() {

        listeners = new LinkedList<>();

        timer = new TaskScheduler();

        detectionRunnable = null;
    }

    public void subscribe(IDetectionListener listener) {

        listeners.add(listener);
    }

    public void unsubscribe(IDetectionListener listener) {

        listeners.remove(listener);
    }

    public void init() {

        if (detectionRunnable != null)
            detectionRunnable.clear();

        detectionRunnable = new DetectionRunnable();
    }

    public void start() {

//        if (!timer.isRunning())
        timer.start(detectionRunnable, DETECTION_INTERVAL);
    }

    public void stop() {

//        if (timer.isRunning())
        timer.stop();
    }

    public void clear() {

        stop();

        if (detectionRunnable != null)
            detectionRunnable.clear();

        detectionRunnable = null;
    }
}
