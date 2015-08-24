package com.remoty.services;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.AbstractMessage;
import com.remoty.common.PingMessage;
import com.remoty.common.PingResponseMessage;
import com.remoty.common.ServerInfo;
import com.remoty.common.TcpSocket;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Claudiu on 8/24/2015.
 */
public class ServerPinger extends AsyncTask<Void, Void, ServerInfo> {

    private TcpSocket server;

    public ServerPinger(TcpSocket server) {

        this.server = server;
    }

    public TcpSocket getTcpSocket() {

        return server;
    }

    @Override
    protected ServerInfo doInBackground(Void... params) {

        Log.d("DETECTION", "Sending ping.");

        try {
            PingMessage pingMessage = new PingMessage();
            // TODO: Set parameters when there are any.
            server.sendObject(pingMessage);
        } catch (IOException e) {
            return null;
        }

        Log.d("DETECTION", "Waiting to receive ping response.");

        PingResponseMessage pingResponseMessage = null;
        try {
            pingResponseMessage = server.receiveObject(PingResponseMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Log.d("DETECTION", "Ping response received successfully. Returning response.");

        InetAddress inetAddress = server.getInetAddress();
        String ip = inetAddress.getHostAddress();
        int tcpPort = pingResponseMessage.tcpPort;
        String hostName = pingResponseMessage.hostName;

        return new ServerInfo(ip, tcpPort, hostName);
    }
}
