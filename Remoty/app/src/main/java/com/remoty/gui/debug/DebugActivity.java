package com.remoty.gui.debug;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;

import com.remoty.common.other.Constant;
import com.remoty.gui.pages.MainActivity;


public class DebugActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onRestoreInstanceState");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onSaveInstanceState");
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onBackPressed");
	}

	@Override
	public boolean onMenuOpened(final int featureId, final Menu menu) {
		boolean result = super.onMenuOpened(featureId, menu);

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onMenuOpened");

		return result;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		Log.d(Constant.APP + Constant.LIFECYCLE, this.getClass().getName() + " - " + "onConfigurationChanged");
	}
}
