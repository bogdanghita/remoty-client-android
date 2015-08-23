package com.remoty.services;

import android.os.AsyncTask;
import android.util.Log;

import com.remoty.common.AbstractMessage;
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
            server.sendObject(new AbstractMessage());
        } catch (IOException e) {
            return null;
        }

        Log.d("DETECTION", "Waiting to receive ping response.");

        AbstractMessage message = null;
        try {
            message = server.receiveObject(AbstractMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        Log.d("DETECTION", "Ping response received successfully. Returning response.");

        InetAddress inetAddress = null;
//            inetAddress = server.getInetAddress();
        String ip = "123.321.123.321";
//            ip = inetAddress.toString();
        String hostName = "Me";
//            hostName = message.hostName;

        return new ServerInfo(ip, (int) message.id, hostName);
    }
}
