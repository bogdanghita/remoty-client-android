package com.remoty.common;

import android.support.v4.app.Fragment;

/**
 * Created by alina on 9/4/2015.
 */
public class Configuration {

	private String name;
	private Fragment fragment;

	public Configuration() {

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public Fragment getFragment() {
		return fragment;
	}
}
