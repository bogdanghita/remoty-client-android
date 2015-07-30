package com.example.dcwa.networking;

import android.os.Handler;
import android.os.Looper;

public class NetworkingThread extends Thread {

	public Handler handler; 
	public boolean stopFlag = true;
	
	@Override
	public void run() {
		
		android.os.Process.setThreadPriority(MAX_PRIORITY);
		this.setPriority(MAX_PRIORITY);
		
		Looper.prepare();
		handler = new Handler();
		Looper.loop();
	}
	
	public void RemoveHandlerCallbacksAndMessages() {
		handler.removeCallbacksAndMessages(null);
	}
	public void Stop() {
		handler.removeCallbacksAndMessages(null);
		handler.getLooper().quit();
	}
	
}
