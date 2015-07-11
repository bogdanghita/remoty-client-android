package com.example.dcwa.auxclasses;

import java.net.URI;

public class ScrollClicks implements ISendableAction {

	public int packageId, numberOfClicks;
	private URI uri;
	
	public ScrollClicks(URI uri) {
		this.uri = uri;
	}
	
	public void SetNumberOfClicks(int numberOfClicks) {
		this.numberOfClicks = numberOfClicks;
	}
	public int GetNumberOfClicks() {
		return numberOfClicks;
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
	public ScrollClicks clone() {
		
		ScrollClicks newObject = new ScrollClicks(this.uri);
		
		newObject.SetNumberOfClicks(this.numberOfClicks);
		newObject.SetPackageId(this.packageId);
		
		return newObject;
	}
}
