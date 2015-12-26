package com.remoty.services.identity;


import android.net.Uri;

public class IdentityService {

	private UserInfo userInfo;

	public synchronized void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public synchronized UserInfo getUserInfo() {
		return userInfo;
	}
}
