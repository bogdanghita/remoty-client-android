package com.example.dcwa.auxclasses;

import java.net.URI;

public interface ISendableAction {
	
	public void SetUri(URI uri);
	public URI GetUri();
	
	public void SetPackageId(int id);
	
	public ISendableAction clone();
}
