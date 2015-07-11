package com.example.dcwa.notusedfiles;
//package com.example.desktopcontrolwithandroid;
//
//import java.net.URI;
//
//import android.app.Activity;
//import android.os.AsyncTask;
//import android.widget.Toast;
//
//public class POSTRequest<T> extends AsyncTask<URI, Void, String> {
//
//	T object;
//	Activity activity;
//	HttpRequest request;
//	ToastFactory toast;
//	
//	TouchAreaView view;
//	int x, y;
//	float pressure, size;
//	boolean debugFlag = false;
//	
//	POSTRequest(T object, Activity activity) {
//		this.object = object;
//		this.activity = activity;
//		this.request = new HttpRequest();
//		
//		// Initializing ToastFactory
//		toast = new ToastFactory();
//	}
//	
//	POSTRequest(T object, Activity activity, TouchAreaView view, int x, int y, float pressure, float size) {
//		this.object = object;
//		this.activity = activity;
//		this.request = new HttpRequest();
//		
//		this.view = view;
//		this.x = x;
//		this.y = y;
//		this.pressure = pressure;
//		this.size = size;
//		
//		debugFlag = true;
//		
//		// Initializing ToastFactory
//		toast = new ToastFactory();
//	}
//	
//	protected String doInBackground(URI... uris) {
//		return request.POST(uris[0],object);
//	}
//	
//	protected void onPostExecute(String response)
//	{
//		if( debugFlag == true ) {
//			view.debugDrawPoint(x, y, pressure, size);
//		}
//		
//		// Checking if response is valid
//		if( response == null )
//		{
//			activity.runOnUiThread(postErrorToast);
//		}
//	}
//	
//	// Error toast runnable
//	Runnable postErrorToast = new Runnable() {
//		@Override
//		public void run() {
//	    	String errorMessage = "Unable to connect to server or server failed to " +
//	    			"respond with a valid HTTP response.";
//	    	toast.Create(errorMessage,Toast.LENGTH_LONG, activity);
//	    	toast.Show();
//		}
//	};
//    
//}
