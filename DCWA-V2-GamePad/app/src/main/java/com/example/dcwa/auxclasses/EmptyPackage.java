package com.example.dcwa.auxclasses;

import java.net.URI;

public class EmptyPackage implements ISendableAction {

	private URI uri;
	private int packageId;
	@SuppressWarnings("unused")
	private int emptyObject = 0;
	
	public EmptyPackage(URI uri) {
		this.uri = uri;
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
	public EmptyPackage clone() {
		
		EmptyPackage newObject = new EmptyPackage(this.uri);
		
		newObject.emptyObject = 0;
		newObject.SetPackageId(this.packageId);
		
		return newObject;
	}
	
}
