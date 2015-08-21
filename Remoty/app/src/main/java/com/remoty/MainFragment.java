package com.remoty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Bogdan on 8/16/2015.
 */
public class MainFragment extends DebugFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View parentView = inflater.inflate(R.layout.fragment_main, container, false);

        return parentView;
    }

}