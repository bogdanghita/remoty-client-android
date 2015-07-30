package com.example.dcwa.networking;

import com.example.dcwa.auxclasses.ToastFactory;
import com.example.dcwa.settings.AboutActivity;
import com.example.dcwa.settings.DetectorSettingsActivity;
import com.example.dcwa.settings.SettingsActivity;
import com.example.desktopcontrolwithandroid.R;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ServerDetectionActivity extends PreferenceActivity {

	private boolean backPressedOnce = false;
	private final int TOAST_LENGTH_SHORT_DURATION = 2000;
	private ToastFactory toast = new ToastFactory();
	
	public static String settings_values_file;
	
	public final static String SERVER_IP_MESSAGE = "SERVER_IP_MSG";
	
	// Discovery port value
	public static final int DEFAULT_UDP_PORT = 8088;
	public static volatile int CURRENT_UDP_PORT;
	
	// Default settings values indexes
	public static final int DEFAULT_POINTER_SENSITIVITY_IDX = 5;
	public static final int DEFAULT_SCROLL_SENSITIVITY_IDX = 6;
	public static final int DEFAULT_SINGLE_REQUEST_TIMEOUT_IDX = 8;
	public static final int DEFAULT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX = 4;
	
	// Current settings values indexes
	public static int CURRENT_POINTER_SENSITIVITY_IDX;
	public static int CURRENT_SCROLL_SENSITIVITY_IDX;
	public static int CURRENT_SINGLE_REQUEST_TIMEOUT_IDX;
	public static int CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX;
	
	// Default check box state
	public static boolean CHECKBOX_SINGLE_REQUEST_TIMEOUT;
	public static boolean CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_server_detector);
		
		// Setting action bar title
    	setTitle(getString(R.string.title_server_discovery));
		
        // Reading settings_values_file
        settings_values_file = getString(R.string.settings_values_file);
		
		// Reading settings values
		SettingsActivity.ReadSettingsValues(this);
        
		// Start fragment
		// Creating fragment
		ServerDetectionFragment serverDetectionFragment = new ServerDetectionFragment();
		
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(android.R.id.content, serverDetectionFragment);
//		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server_detector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        
    	int id = item.getItemId();
        switch (id) {
        case R.id.action_settings:
        	openSettings();
            return true;
        case R.id.action_about:
        	openAbout();
        	return true;
        case R.id.action_exit:
        	Exit();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // Implementation of the safe exit procedure ('Press back button twice to exit' toast) 
    @Override
    public void onBackPressed() {
    	
    	if( backPressedOnce == true ) {
    		toast.Cancel();
    		super.onBackPressed();
    		return;
    	}
    	
    	backPressedOnce = true;
        String message = "Press Back button twice to exit";
        toast.Create(message,Toast.LENGTH_SHORT, this.getApplicationContext());
        toast.Show();
    	
    	// Resetting backPressedOnce flag to false
    	Runnable resetBackFlag = new Runnable() {
			@Override
			public void run() {
				backPressedOnce = false;
			}
		};
		new Handler().postDelayed(resetBackFlag, TOAST_LENGTH_SHORT_DURATION);
    }
    
    // Method that closes the app
    public void Exit() {
    	super.onBackPressed();
    }
    
    // Method that opens the 'About' activity
    private void openAbout() {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }
    
    // Method that opens the 'Settings' activity
    public void openSettings() {
    	Intent intent = new Intent(this, DetectorSettingsActivity.class);
    	startActivity(intent);
    }
    
}
