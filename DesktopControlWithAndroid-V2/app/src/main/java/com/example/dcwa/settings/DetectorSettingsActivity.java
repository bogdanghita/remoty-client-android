package com.example.dcwa.settings;

import com.example.desktopcontrolwithandroid.R;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class DetectorSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// This thing solves a lot of problems, don't forget about it please
		// Checks if the activity is restored from a previous state
		// If true, there is no need to create and add a new fragment
		// (this avoids the case of overlapping fragments)
		if (savedInstanceState != null) {
			return;
		}
		
		// Creating fragment
		DetectorSettingsFragment detectorSettingsFragment = new DetectorSettingsFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(android.R.id.content, detectorSettingsFragment);
		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_about) {
			openAbout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// Method that opens the 'About' activity
    private void openAbout() {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
}
