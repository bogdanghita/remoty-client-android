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

    private final static int DETECT_SERVER_MSG = 1000;
    private final static long DETECTION_INTERVAL = 1000;

    private List<IDetectionListener> listeners;

    TaskScheduler timer;

    DetectionRunnable detectionRunnable;

    public void ServerDetection() {

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

//    private void notifyListeners(List<ServerInfo> servers) {
//
//        for (IDetectionListener listener : listeners) {
//            listener.update(servers);
//        }
//    }

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

class DetectionRunnable implements Runnable {

    List<TcpSocket> servers;
    Broadcaster broadcaster;

    public DetectionRunnable() {

        servers = new ArrayList<TcpSocket>();
        broadcaster = new Broadcaster();
    }

    @Override
    public void run() {

        // Broadcast (UDP)
        broadcaster.broadcast();

        // New connections (fresh TCP)
        List<TcpSocket> newServers = broadcaster.acceptServers();
        servers.addAll(newServers);

        // Ping all (TCP)
        List<ServerInfo> results = pingAll(servers);

        // notify
        notifyListeners(results);
    }

    private void notifyListeners(List<ServerInfo> servers) {

        // TODO: ...
    }

    public void clear() {

        for (TcpSocket server : servers) {

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        servers.clear();
    }

    private List<ServerInfo> pingAll(List<TcpSocket> servers) {

        List<ServerPinger> serverPingers = new ArrayList<ServerPinger>();
        List<ServerInfo> serverInfos = new ArrayList<ServerInfo>();

        for (TcpSocket server : servers) {
            ServerPinger serverPinger = new ServerPinger(server);
            serverPinger.execute();

            serverPingers.add(serverPinger);
        }

        for (ServerPinger serverPinger : serverPingers) {

            ServerInfo serverInfo = null;

            try {
                serverInfo = serverPinger.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (serverInfo != null) {
                serverInfos.add(serverInfo);
            } else {
                TcpSocket server = serverPinger.getTcpSocket();
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                servers.remove(server);
            }
        }

        return serverInfos;
    }
}

class ServerPinger extends AsyncTask<Void, Void, ServerInfo> {

    private TcpSocket server;

    public ServerPinger(TcpSocket server) {

        this.server = server;
    }

    public TcpSocket getTcpSocket() {

        return server;
    }

    @Override
    protected ServerInfo doInBackground(Void... params) {

        try {
            server.sendObject(new AbstractMessage());
        } catch (IOException e) {
            return null;
        }

        AbstractMessage message = null;
        try {
            message = server.receiveObject(AbstractMessage.class);
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }

        InetAddress inetAddress = null;
//            inetAddress = server.getInetAddress();
        String ip = "123.321.123.321";
//            ip = inetAddress.toString();
        String hostName = "Me";
//            hostName = message.hostName;

        return new ServerInfo(ip, (int) message.id, hostName);
    }
}

class Broadcaster {

    private static int tcpPort = 10000;

    public static int udpPortServer = 9001;

    private final int ACCEPT_TIMEOUT = 250;


    private final String MSG_CODE = "REQUEST_RECEIVED";
    private final String MSG_DELIM = "%-%";

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    private boolean receiveTimeoutExceeded = false;

    private final byte[] TO_SEND_DATA = "DISCOVER_SERVER_REQUEST".getBytes();

    public Broadcaster() {
        // TODO: What?
    }

    public void broadcast() {

        // SEND PART
        Log.d("FIRST_STEP", "Starting server discovery");

        // Opening a random port to send the packet (initializing socket)
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        // Sending packet on the 255.255.255.255 address
        SendMessageOnDefaultAddress();

        // Broadcasting the message over all the network interfaces
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        // Iterating through all interfaces of the device
        while (interfaces.hasMoreElements()) {

            // Obtaining current interface
            NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
            Log.d("INTERFACES", "Current interface: " + networkInterface.getName());

            // Checking if current interface is a loopback interface and if it is up
            if (CheckInterface(networkInterface) == false) {
                continue;
            }

            // Processing each address of the current interface and obtaining its broadcast address
            // If the broadcast address is valid, sending request
            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {

                Log.d("INTERFACE_ADDRESS_ITER", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString());

                // Obtaining the broadcast address of the current interface
                InetAddress broadcast = interfaceAddress.getBroadcast();

                // Checking if the broadcast address is valid
                if (broadcast == null) {
                    Log.d("ADDRESS_CHECK", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() + " - Broadcast address is null");
                    continue;
                }

                // Sending message to the current address
                SendBroadcastMessage(networkInterface, interfaceAddress, broadcast);
            }
        }

        datagramSocket.close();
    }

    public List<TcpSocket> acceptServers() {

        // RECEIVE PART
        Log.d("SECOND_STEP", "Done looping over all network interfaces. Waiting for a reply...");

        ArrayList<TcpSocket> serverList = new ArrayList<TcpSocket>();

        // Resetting receive timeout flag
        receiveTimeoutExceeded = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(tcpPort);
            serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Waiting for server responses until one receive exceeds its timeout
        while (receiveTimeoutExceeded == false) {

            // Processing one response
            TcpSocket server = Accept(serverSocket);

            // Appending server to list
            if (server != null) {
                serverList.add(server);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Closing the port
        datagramSocket.close();
        return serverList;
    }

    private TcpSocket Accept(ServerSocket serverSocket) {

        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch (IOException e) {
            receiveTimeoutExceeded = true;
            return null;
        }

        TcpSocket tcpSocket = null;
        try {
            tcpSocket = new TcpSocket(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tcpSocket;
    }

    private void SendMessageOnDefaultAddress() {

        // Creating packet that will be sent through the DatagramSocket
        try {
            datagramPacket = new DatagramPacket(TO_SEND_DATA, TO_SEND_DATA.length, InetAddress.getByName("255.255.255.255"), udpPortServer);
            datagramSocket.send(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("EXCEPTION", "Unable to send to: 255.255.255.255 (DEFAULT)");
        }
        Log.d("DEFAULT_BROADCAST", "Request packet sent to: 255.255.255.255 (DEFAULT)");
    }

    private boolean CheckInterface(NetworkInterface networkInterface) {

        boolean interfaceIsLoopback = false, interfaceIsUp = false;
        try {
            interfaceIsLoopback = networkInterface.isLoopback();
            interfaceIsUp = networkInterface.isUp();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        // If current interface is down or if it is loopback: continue
        if (interfaceIsLoopback || !interfaceIsUp) {
            Log.d("INTERFACE_CHECK", "Interface: " + networkInterface.getName() + " - is loopback or down");

            return false;
        } else {
            return true;
        }
    }

    private void SendBroadcastMessage(NetworkInterface networkInterface, InterfaceAddress interfaceAddress, InetAddress broadcast) {

        // Sending the broadcast packet
        // Creating packet that will be sent through the DatagramSocket on the current broadcast address
        byte[] tcpPortBytes = Integer.toString(tcpPort).getBytes();
        datagramPacket = new DatagramPacket(tcpPortBytes, tcpPortBytes.length, broadcast, udpPortServer);
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("SENDING_PACKET", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
                    " - Unable to send packet to" + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
        }

        // Displaying success message
        Log.d("SENDING_PACKET", "Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() +
                " - Request packet sent to: " + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
    }
}
