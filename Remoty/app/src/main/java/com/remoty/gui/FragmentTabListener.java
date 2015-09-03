package com.remoty.gui;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
                DriveFragment tab1 = new DriveFragment();
                return tab1;
            case 1:
                ConnectFragment tab2 = new ConnectFragment();
                return tab2;
            case 2:
                ScoreFragment tab3 = new ScoreFragment();
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