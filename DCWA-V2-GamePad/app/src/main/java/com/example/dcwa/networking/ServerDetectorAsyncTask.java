package com.example.dcwa.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.example.dcwa.auxclasses.ServerInfo;

import android.os.AsyncTask;
import android.util.Log;

public class ServerDetectorAsyncTask extends AsyncTask<Void, Void, ArrayList<ServerInfo>> {

	public static int discoveryPort;
	
	private final int RECEIVE_TIMEOUT = 200;
	
	private final String MSG_CODE = "REQUEST_RECEIVED";
	private final String MSG_DELIM = "%-%";
	
	private DatagramSocket c;
	private DatagramPacket sendPacket;
	
	private boolean receiveTimeoutExceeded = false;
	
	// Setting data that will be sent
	private final byte[] TO_SEND_DATA = "DISCOVER_SERVER_REQUEST".getBytes();
	
	public ServerDetectorAsyncTask(int discoveryPort) {
		// Setting UDP port value
		ServerDetectorAsyncTask.discoveryPort = discoveryPort;
	}
	
	@Override
	protected ArrayList<ServerInfo> doInBackground(Void...voids) {
		// Executing broadcast request
		return RequestServerIP();
	}
	
	// Method that does the discovery
	private ArrayList<ServerInfo> RequestServerIP() {
    	
		// SEND PART
		Log.d("FIRST_STEP","Starting server discovery");
		
		// Opening a random port to send the packet (initializing socket)
		try {
			c = new DatagramSocket();
			c.setBroadcast(true);
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
    	    Log.d("INTERFACES","Current interface: " + networkInterface.getName());
    	    
    	    // Checking if current interface is a loopback interface and if it is up
    	    if( CheckInterface(networkInterface) == false ) {
    	    	continue;
    	    }

    	    // Processing each address of the current interface and obtaining its broadcast address
    	    // If the broadcast address is valid, sending request
    	    for( InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses() ) {
    	    	
    	    	Log.d("INTERFACE_ADDRESS_ITERATION","Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString());
    	    	
    	    	// Obtaining the broadcast address of the current interface
    	    	InetAddress broadcast = interfaceAddress.getBroadcast();
    	    	
    	    	// Checking if the broadcast address is valid
    	    	if( broadcast == null ) {
    	    	  Log.d("ADDRESS_CHECK","Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() + " - Broadcast address is null");
    	    	  continue;
    	    	}

    	    	// Sending message to the current address
    	    	SendBroadcastMessage(networkInterface, interfaceAddress, broadcast);
    	    }
		}

		// RECEIVE PART
		Log.d("SECOND_STEP","Done looping over all network interfaces. Waiting for a reply...");

		ArrayList<ServerInfo> serverList = new ArrayList<ServerInfo>();
		
		// Resetting receive timeout flag
		receiveTimeoutExceeded = false;
		
		// Waiting for server responses until one receive exceeds its timeout
		while( receiveTimeoutExceeded == false ) {
			
			// Processing one response
			ServerInfo server = Receive();
			
			// Appending server to list if it does not already exist
			if( server != null && serverList.contains(server) == false ) {
				serverList.add(server);
			}
		}
		
		//Closing the port
		c.close();
		return serverList;
    }
	
	public void SendMessageOnDefaultAddress() {
		
		// Creating packet that will be sent through the DatagramSocket
		try {
			sendPacket = new DatagramPacket(TO_SEND_DATA, TO_SEND_DATA.length, InetAddress.getByName("255.255.255.255"), discoveryPort);
			c.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("EXCEPTION","Unable to send to: 255.255.255.255 (DEFAULT)");
		}
		Log.d("DEFAULT_BROADCAST","Request packet sent to: 255.255.255.255 (DEFAULT)");
	}
	
	public boolean CheckInterface(NetworkInterface networkInterface) {
		
	    boolean interfaceIsLoopback = false,  interfaceIsUp = false;
		try {
			interfaceIsLoopback = networkInterface.isLoopback();
			interfaceIsUp = networkInterface.isUp();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	    
		// If current interface is down or if it is loopback: continue
	    if ( interfaceIsLoopback || !interfaceIsUp ) {
	    	Log.d("INTERFACE_CHECK", "Intrface: " + networkInterface.getName() + " - is loopback or down");
	    	
	    	return false;
	    }
	    else {
	    	return true;
	    }
	}
	
	public void SendBroadcastMessage(NetworkInterface networkInterface, InterfaceAddress interfaceAddress, InetAddress broadcast) {
		
		// Sending the broadcast packet
    	// Creating packet that will be sent through the DatagramSocket on the current broadcast address
        sendPacket = new DatagramPacket(TO_SEND_DATA, TO_SEND_DATA.length, broadcast, discoveryPort);
        try {
			c.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("SENDING_PACKET","Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() + 
					" - Unable to send packet to" + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
		}
        
        // Displaying success message
        Log.d("SENDING_PACKET","Interface: " + networkInterface.getName() + " - current address: " + interfaceAddress.getAddress().toString() + 
        		" - Request packet sent to: " + broadcast.getHostAddress() + " - Interface: " + networkInterface.getDisplayName());
	}
	
	public ServerInfo Receive() {
		
		byte[] recvBuf = new byte[15000];
		DatagramPacket receivedPacket = new DatagramPacket(recvBuf, recvBuf.length);
		try {
			// Setting socket timeout
			c.setSoTimeout(RECEIVE_TIMEOUT);
			c.receive(receivedPacket);
		} catch (IOException e) {
//			e.printStackTrace();
			
			// Setting flag to true
			receiveTimeoutExceeded = true;
			
			Log.d("RECEIVE_TIMEOUT","Receive timeout reached");
			return null;
		}
		
		// Obtaining server address (if it exist)
		InetAddress senderAddress =  receivedPacket.getAddress();
		
		// Checking if any packet was received
		if( senderAddress != null ) {
			
			// We have a response
			String response = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
			Log.d("RESPONSE_RECEIVED","Received response: " + response + " - from: " + senderAddress.getHostAddress());
			
			// Obtaining message from packet
			String message = new String(receivedPacket.getData()).trim();
			
			// Separating message from port
			String[] strTokens = message.split(MSG_DELIM);
			if( strTokens.length != 3 ) {
				return null;
			}
			String msg_code = strTokens[0];
			
			int serverPort = Integer.valueOf(strTokens[1]);
			String serverName = strTokens[2];
			
			// Checking if the message is the expected one
			if( msg_code.equals(MSG_CODE) ) {
				
				// Creating URL
				InetAddress inetAddress = receivedPacket.getAddress();
				
				String serverIp = inetAddress.getHostAddress();
				
				Log.d("SERVER_FOUND","Server address: " + serverIp + ":" + serverPort + " - " + serverName);
				
				// Creating new ServerInfo object
				return new ServerInfo(serverIp, serverPort, serverName);
			}
		}
		return null;
	}
	
}