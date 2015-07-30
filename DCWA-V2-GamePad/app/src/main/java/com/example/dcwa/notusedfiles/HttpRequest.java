package com.example.dcwa.notusedfiles;
//package com.example.desktopcontrolwithandroid;
//
//import java.io.IOException;
//import java.net.URI;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.params.HttpParams;
//
//public class HttpRequest {
//	
//		//Method that makes the http POST request
//		public <T> String POST(URI uri, T object)
//		{
//			HttpPost post = new HttpPost(uri);
//			
//			int connectionTimeout = 3000, socketTimeout = 5000;
//	    	HttpParams params = new BasicHttpParams();
//	    	HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
//	    	HttpConnectionParams.setSoTimeout(params, socketTimeout);
//			
//			//getting HttpEntity from object
//			HttpEntity entity = new ObjectToEntity<T>().ToJsonHttpEntity(object);
//			
//			//setting the HttpEntity to the HttpPost
//			post.setEntity(entity);
//			
//			post.setHeader("Accept", "application/json");
//			post.setHeader("Content-type", "application/json");
//			
//			//creating client and setting parameters
//			DefaultHttpClient httpClient = new DefaultHttpClient();
//			httpClient.setParams(params);
//			try {
//				httpClient.execute(post);
//				return "Displaying server response is currently unavailable.";
//			} catch (ClientProtocolException e) {
//				e.printStackTrace();
//				return null;
//			} catch (IOException e) {
//				e.printStackTrace();
//				return null;
//			}
//		}
//	
//}
