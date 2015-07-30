package com.example.dcwa.auxclasses;

import java.net.URI;
import java.net.URISyntaxException;

import android.util.Log;

public class UriFactory {

	String URL;
	
	public UriFactory(String URL) {
		this.URL = URL;
	}
	
	public URI Create(String serverUriTemplate) {
		URI uri;
		try {
			uri = new URI( URL + serverUriTemplate );
			Log.d("URI_FACTORY",uri.getPath());
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
		return uri;
	}
	
}
