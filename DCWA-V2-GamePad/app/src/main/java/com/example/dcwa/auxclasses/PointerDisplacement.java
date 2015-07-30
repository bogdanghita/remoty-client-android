package com.example.dcwa.auxclasses;

import java.net.URI;

public class PointerDisplacement implements ISendableAction {
	
	private int X, Y, packageId;
	private URI uri;
	
	public PointerDisplacement(URI uri) {
		this.uri = uri;
	}
	
	public void SetDisplacement(int X, int Y) {
		this.X = X;
		this.Y = Y;
	}
	public int GetX() {
		return X;
	}
	public int GetY() {
		return Y;
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
	public PointerDisplacement clone() {
		
		PointerDisplacement newObject = new PointerDisplacement(this.uri);
		
		newObject.SetDisplacement(this.X,  this.Y);
		newObject.SetPackageId(this.packageId);
		
		return newObject;
	}
}
