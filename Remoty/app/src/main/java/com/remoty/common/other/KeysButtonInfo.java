package com.remoty.common.other;


public class KeysButtonInfo {

	public String name;
	public String action;

	public float startXPercent;
	public float startYPercent;

	public float widthPercent;
	public float heightPercent;

	public KeysButtonInfo() {

	}

	public KeysButtonInfo(String name, String action, float startXPercent, float startYPercent, float widthPercent, float heightPercent) {
		this.name = name;
		this.action = action;
		this.startXPercent = startXPercent;
		this.startYPercent = startYPercent;
		this.widthPercent = widthPercent;
		this.heightPercent = heightPercent;
	}
}
