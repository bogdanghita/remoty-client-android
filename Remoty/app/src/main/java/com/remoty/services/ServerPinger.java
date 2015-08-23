package com.remoty.services;

import android.os.AsyncTask;

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
