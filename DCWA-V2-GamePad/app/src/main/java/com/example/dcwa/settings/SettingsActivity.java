package com.example.dcwa.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.example.dcwa.auxclasses.DisplayOrientation;
import com.example.dcwa.mainfeatures.MainActivity;
import com.example.dcwa.mainfeatures.ShortcutControlFragment;
import com.example.dcwa.networking.ServerDetectionActivity;
import com.example.desktopcontrolwithandroid.R;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	
	public static boolean settingsConfigurationChanged = false;

	public static Vector<String> reservedFileNames = new Vector<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Log.d("SETTINGS_ACTIVITY","ON_CREATE called");
		
		MainActivity.mainConfigurationChanged = true;
		
		// Reading reserved files list
		ReadReservedFileNames();
		
		// This thing solves a lot of problems, don't forget about it please
		// Checks if the activity is restored from a previous state
		// If true, there is no need to create and add a new fragment
		// (this avoids the case of overlapping fragments)
		if (savedInstanceState != null) {
			return;
		}
		
		// Creating fragment
		SettingsFragment settingsFragment = new SettingsFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(android.R.id.content, settingsFragment);
		transaction.commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		Log.d("SETTINGS_ACTIVITY","ON_RESUME called");
//		Log.d("SETTINGS_ACTIVITY", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		Log.d("SETTINGS_ACTIVITY","ON_PAUSE called");
//		Log.d("SETTINGS_ACTIVITY", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
		
//		 // Writing default values to file
//		 WriteCurrentSettingValues(this);
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
	        switch (id) {
	        case R.id.action_about:
	        	openAbout();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		settingsConfigurationChanged = true;
	     
//	    // Checks the orientation of the screen
//	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
//	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
//	    }
	}
	
	// Reads the reserved filenames
	private void ReadReservedFileNames() {
		reservedFileNames.add(getString(R.string.config_files_list_file));
		reservedFileNames.add(getString(R.string.current_config_file));
		reservedFileNames.add(getString(R.string.flag_file));
		reservedFileNames.add(getString(R.string.last_saved_config_file));
		reservedFileNames.add(getString(R.string.live_sending_state_file));
		reservedFileNames.add(getString(R.string.settings_values_file));
		reservedFileNames.add(getString(R.string.help_file));
	}
	
	// Computes the parameters of the map: cellsOnRow/Column, realCellHeight/Width and x/yEps
	public static void ComputeMapParamteters() {
		
		if( ShortcutControlFragment.parentHeight > ShortcutControlFragment.parentWidth ) {
			// Setting orientation
			ShortcutControlFragment.orientation = DisplayOrientation.portrait;
			ShortcutControlFragment.cellsOnColumn = 20;
			ShortcutControlFragment.cellsOnRow = 12;
		}
		else {
			// Setting orientation
			ShortcutControlFragment.orientation = DisplayOrientation.landscape;
			ShortcutControlFragment.cellsOnColumn = 12;
			ShortcutControlFragment.cellsOnRow = 20;
		}
		
		ShortcutControlFragment.realCellHeight = ShortcutControlFragment.parentHeight/ShortcutControlFragment.cellsOnColumn;
		ShortcutControlFragment.realCellWidth = ShortcutControlFragment.parentWidth/ShortcutControlFragment.cellsOnRow;
		
		ShortcutControlFragment.xEps = (ShortcutControlFragment.parentWidth - 
				(ShortcutControlFragment.cellsOnRow*ShortcutControlFragment.realCellWidth) )/2;
		ShortcutControlFragment.yEps = (ShortcutControlFragment.parentHeight - 
				(ShortcutControlFragment.cellsOnColumn*ShortcutControlFragment.realCellHeight) )/2;
	}
	
	// Reading current settings values from file
	public static void ReadSettingsValues(Activity activity) {
		
		String filename = ServerDetectionActivity.settings_values_file;
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = activity.openFileInput(filename);
		} catch (FileNotFoundException e) {
			Log.d("READ_SETTINGS_VALUES", filename + " NOT found");

			// The file dose not exist. Setting default values
			ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX = ServerDetectionActivity.DEFAULT_POINTER_SENSITIVITY_IDX;
			ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX = ServerDetectionActivity.DEFAULT_SCROLL_SENSITIVITY_IDX;
			ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX = ServerDetectionActivity.DEFAULT_SINGLE_REQUEST_TIMEOUT_IDX;
			ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT = true;
			ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX = ServerDetectionActivity.DEFAULT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX;
			ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY = true;
			ServerDetectionActivity.CURRENT_UDP_PORT = ServerDetectionActivity.DEFAULT_UDP_PORT;
			
			// Writing default values to file
			WriteCurrentSettingValues(activity);
			
			return;
		}
		 
		// Reading file in a String variable
		String fileContent="";
		int content;
		try {
			while( (content = inputStream.read()) != -1 ) {
				fileContent += (char)content;
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Separating fileContent into lines/tokens
		String delims = "[\n]+";
		String[] strTokens = fileContent.split(delims);
		
		ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX = Integer.valueOf(strTokens[0]);
		ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX = Integer.valueOf(strTokens[1]);
		ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX = Integer.valueOf(strTokens[2]);
		ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT = Boolean.valueOf(strTokens[3]);
		ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX = Integer.valueOf(strTokens[4]);
		ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY = Boolean.valueOf(strTokens[5]);
		ServerDetectionActivity.CURRENT_UDP_PORT = Integer.valueOf(strTokens[6]);
	}
	
	// Writing current settings values from file
	public static void WriteCurrentSettingValues(Activity activity) {
		
		String filename = ServerDetectionActivity.settings_values_file;
		
		FileOutputStream outputStream = null;
		try {
			outputStream = activity.openFileOutput(filename, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String strPointerSensitivity = String.valueOf(ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX) + "\n";
		String strScrollSensitivity = String.valueOf(ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX) + "\n";
		String strSingleRequestTimeout = String.valueOf(ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX) + "\n";
		String strCheckBoxSingleRequestTimeout = String.valueOf(ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT) + "\n";
		String strKeepAliveRequestFrequency = String.valueOf(ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX) + "\n";
		String strCheckBoxKeepAliveFrequency = String.valueOf(ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY) + "\n";
		String strUdpPort = String.valueOf(ServerDetectionActivity.CURRENT_UDP_PORT) + "\n";
		
		try {
			outputStream.write(strPointerSensitivity.getBytes());
			outputStream.write(strScrollSensitivity.getBytes());
			outputStream.write(strSingleRequestTimeout.getBytes());
			outputStream.write(strCheckBoxSingleRequestTimeout.getBytes());
			outputStream.write(strKeepAliveRequestFrequency.getBytes());
			outputStream.write(strCheckBoxKeepAliveFrequency.getBytes());
			outputStream.write(strUdpPort.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    // Method that opens the 'About' activity
    private void openAbout() {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
}