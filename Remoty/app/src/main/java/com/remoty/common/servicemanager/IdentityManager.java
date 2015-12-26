package com.remoty.common.servicemanager;

import com.remoty.common.datatypes.UserInfo;


public class IdentityManager {

	private UserInfo userInfo;

	public synchronized void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public synchronized UserInfo getUserInfo() {
		return userInfo;
	}

	public synchronized boolean hasUserInfo() {
		return userInfo != null;
	}

	public synchronized void clear() {
		userInfo = null;
	}
}
