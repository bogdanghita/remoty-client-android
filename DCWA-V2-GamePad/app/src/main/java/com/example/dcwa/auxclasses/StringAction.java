package com.example.dcwa.auxclasses;

import java.net.URI;

public class StringAction implements ISendableAction {

	private int packageId;
	private URI uri;
	private String text;
	
	public StringAction(URI uri) {
		this.uri = uri;
	}
	
	public void SetText(String text) {
		this.text = text;
	}
	
	@Override
	public void SetPackageId(int id) {
		packageId = id;
	}

	@Override
	public void SetUri(URI uri) {
		this.uri = uri;
	}
	@Override
	public URI GetUri() {
		return this.uri;
	}
	
	@Override
	public StringAction clone() {
		
		StringAction newObject = new StringAction(this.uri);
		
		newObject.SetText(this.text);
		newObject.SetPackageId(this.packageId);
		
		return newObject;
	}
}
