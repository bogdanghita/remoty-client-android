package com.example.dcwa.auxclasses;

import java.net.URI;

/**
 * Created by Bogdan on 7/30/2015.
 */
public class Point3D implements ISendableAction {

	private float X, Y, Z;
	private int packageId;
	private URI uri;

	public Point3D(URI uri) {
		this.uri = uri;
	}

	public void SetDisplacement(float X, float Y, float Z) {
		this.X = X;
		this.Y = Y;
		this.Z = Z;
	}

	public float GetX() {
		return X;
	}

	public float GetY() {
		return Y;
	}

	public float GetZ() {
		return Z;
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
	public Point3D clone() {

		Point3D newObject = new Point3D(this.uri);

		newObject.SetDisplacement(this.X, this.Y, this.Z);
		newObject.SetPackageId(this.packageId);

		return newObject;
	}
}
