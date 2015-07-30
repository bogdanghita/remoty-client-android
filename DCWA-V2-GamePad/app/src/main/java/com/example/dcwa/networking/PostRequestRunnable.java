package com.example.dcwa.networking;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.example.dcwa.auxclasses.EmptyPackage;
import com.example.dcwa.auxclasses.ISendableAction;
import com.example.dcwa.auxclasses.ObjectToEntity;
import com.example.dcwa.mainfeatures.MainActivity;

import android.app.Activity;
import android.util.Log;

public class PostRequestRunnable implements Runnable {
	
	public static volatile int USER_DEFINED_REQUEST_TIMEOUT;
	public final static int CONNECTION_LOST_REQUEST_TIMEOUT = 2000;
	public static volatile int IN_USE_REQUEST_TIMEOUT;
	private final long UNRELIABLE_CONNECTION_DECISION_TIMEOUT = 6000;
	
	public volatile boolean unreliableConnection = false;
	
	Activity activity;
	NetworkingThread thread;
	
	private ISendableAction object;
	DefaultHttpClient httpClient = new DefaultHttpClient();
	HttpPost post = new HttpPost();
	HttpParams params = new BasicHttpParams();
	ObjectToEntity objectToEntity = new ObjectToEntity();
	HttpEntity entity;
	HttpResponse response;
	
	boolean status;
	public int lostPacketCounter = 0;
	public long timeOfLastSuccessfullySentPacket;
	public long elapsedTimeSinceLastSuccessfullySentPacket = 0;
	
	public PostRequestRunnable(Activity activity, NetworkingThread thread) {
		this.activity = activity;
		this.thread = thread;
	}
	
	public void SetObject(ISendableAction object) {
		this.object = object.clone();
	}
	
	@Override
	public void run() {
		
		// Setting connection timeout
		HttpConnectionParams.setConnectionTimeout(params, IN_USE_REQUEST_TIMEOUT);
    	HttpConnectionParams.setSoTimeout(params, IN_USE_REQUEST_TIMEOUT);
		httpClient.setParams(params);
    	
		// Creating HttpEntity from object
		entity = objectToEntity.ToJsonHttpEntity(object);
		
		// Setting URI
		post.setURI(object.GetUri());
		// Setting the HttpEntity to the HttpPost
		post.setEntity(entity);
		// Setting header
		post.setHeader("Content-type", "application/json");
		
		long startTime = System.currentTimeMillis();
		
		Log.d("POST_REQ_RUNNABLE",">>> BEFORE sending packet - IN_USE_REQUEST_TIMEOUT: " + IN_USE_REQUEST_TIMEOUT + " - Type: " + object.getClass());
		
		// Executing request
		status = ExecutePost(post, httpClient);
		
		Log.d("POST_REQ_RUNNABLE",">>> AFTER sending packet. Type: " + object.getClass());
		
		long elapsedTime = System.currentTimeMillis()-startTime;
		
		// Connection check mechanism
		if( status == false ) {
			// Increasing lost packet counter
			++ lostPacketCounter;
			elapsedTimeSinceLastSuccessfullySentPacket = System.currentTimeMillis() - timeOfLastSuccessfullySentPacket;
		}
		else {
			// Resetting lost packet counter, unreliableConnectionFlag and elapsedTimeSinceLastSuccessfullySentPacket
			lostPacketCounter = 0;
			unreliableConnection = false;
			timeOfLastSuccessfullySentPacket = System.currentTimeMillis();
			elapsedTimeSinceLastSuccessfullySentPacket = 0;
			
			// Setting the request timeout and the keep alive frequency to the ones defined by the user
			IN_USE_REQUEST_TIMEOUT = USER_DEFINED_REQUEST_TIMEOUT;
			MainActivity.IN_USE_KEEP_ALIVE_FREQUENCY = MainActivity.USER_DEFINED_KEEP_ALIVE_FREQUENCY;
		}
		if( lostPacketCounter > 20 || elapsedTimeSinceLastSuccessfullySentPacket > UNRELIABLE_CONNECTION_DECISION_TIMEOUT ) {
			
			// Doing the connection lost actions
			SwitchToConnectionLostBehavior();
		}
		
//		// Printing status
		Log.d("POST_RUNNABLE","STATUS: " + status + " - REQ_TIMEOUT: " + IN_USE_REQUEST_TIMEOUT + " - KEEP_ALIVE: " + MainActivity.IN_USE_KEEP_ALIVE_FREQUENCY + 
				" - CNT: " + lostPacketCounter + " - elapsedTimeSinceLastSuccessfullySentPacket: " + elapsedTimeSinceLastSuccessfullySentPacket);
		
		// Printing request status
		if( object.getClass() != EmptyPackage.class ) {
			Log.d("POST_RUNNABLE","STATUS: " + status + " - DURATION: " + elapsedTime + " - TYPE: " + object.getClass());
		}
	}
	
	// Method that executes the post. Returns false if the request failed
	private boolean ExecutePost(HttpPost post,DefaultHttpClient httpClient) {
		
		try {
			// Executing request
			response = httpClient.execute(post);
			// Consuming response to avoid the warning: Invalid use of SingleClientConnManager: connection still allocated
			EntityUtils.toString(response.getEntity());
			return true;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void SwitchToConnectionLostBehavior() {
		
		Log.d("POST_REQUEST_RUNNABLE","FAILED TO SEND PACKET> DISPLAYING TOAST");
		
		// Clearing thread message queue
		thread.RemoveHandlerCallbacksAndMessages();
		
		// Setting the request timeout and the keep alive frequency to the connection lost value
		IN_USE_REQUEST_TIMEOUT = CONNECTION_LOST_REQUEST_TIMEOUT;
		MainActivity.IN_USE_KEEP_ALIVE_FREQUENCY = MainActivity.CONNECTION_LOST_KEEP_ALIVE_FREQUENCY;
		
		// Marking that the connection is unreliable
		unreliableConnection = true;
	}
	
}
