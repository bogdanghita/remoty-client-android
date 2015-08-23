package com.remoty.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remoty.R;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class DriveFragment extends LiveDataTransferFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_drive, container, false);

		return parentView;
	}


}
