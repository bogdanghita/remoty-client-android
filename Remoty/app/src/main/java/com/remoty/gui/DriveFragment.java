package com.remoty.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.remoty.R;
import com.remoty.common.AccelerometerService;

/**
 * Created by Bogdan on 8/17/2015.
 */
public class DriveFragment extends LiveDataTransferFragment {

	AccelerometerService accService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View parentView = inflater.inflate(R.layout.fragment_drive, container, false);

		return parentView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		accService = new AccelerometerService();
	}

	@Override
	public void onStart() {
		super.onStart();

		// NOTE: onResume() is always called immediately after onStart()

		accService.init();
	}

	@Override
	public void onResume() {
		super.onResume();

		accService.start();
	}

	@Override
	public void onPause() {
		super.onPause();

		accService.stop();
	}

	@Override
	public void onStop() {
		super.onStop();

		// NOTE: activity might be destroyed (and might also be recreated -> savedInstanceState) after this,
		// or it might be restarted (onRestart -> on Start)

		accService.clear();
	}

}
