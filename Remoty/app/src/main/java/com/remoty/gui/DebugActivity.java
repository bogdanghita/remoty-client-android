package com.remoty.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

/**
 * Created by Bogdan on 8/21/2015.
 */
public class DebugActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onCreate");
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onDestroy");
	}

	@Override
	public void onBackPressed() {
        if(!MainActivity.Instance.homeAsBack)
		    super.onBackPressed();

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onBackPressed");
	}


	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onRestoreInstanceState");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onSaveInstanceState");
	}

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        Log.d(MainActivity.APP + MainActivity.LIFECYCLE, this.getClass().getName() + " - " + "onMenuOpened");

        if(MainActivity.Instance.homeAsBack) {
            super.onMenuOpened(featureId, menu);
            return false;
        } else {
            return true;
        }
    }
}
