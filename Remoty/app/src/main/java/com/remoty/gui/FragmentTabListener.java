package com.remoty.gui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by alina on 9/2/2015.
 */

public class FragmentTabListener extends FragmentStatePagerAdapter {

	int mNumOfTabs;

	public FragmentTabListener(FragmentManager fm, int NumOfTabs) {
		super(fm);
		this.mNumOfTabs = NumOfTabs;
	}

	@Override
	public Fragment getItem(int position) {

		switch (position) {
			case 0:
				MyConfigurationsFragment tab1 = new MyConfigurationsFragment();
				return tab1;
			case 1:
				MarketFragment tab2 = new MarketFragment();
				return tab2;
			case 2:
				SocialFragment tab3 = new SocialFragment();
				return tab3;
			default:
				return null;
		}
	}

	@Override
	public int getCount() {
		return mNumOfTabs;
	}
}