package com.example.dcwa.networking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import com.example.dcwa.auxclasses.ServerInfo;
import com.example.dcwa.mainfeatures.MainActivity;
import com.example.desktopcontrolwithandroid.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

public class ServerDetectionFragment extends PreferenceFragment {

	private final static int DETECT_SERVER_MSG = 9375;
	private final static int SERVER_DETECTION_FREQUENCY = 3000;
	private static final String MANUAL_SELECTION_PREF_KEY = "MANUAL_SELECTION";
	
	private static ServerDetectorAsyncTask serverDetector;
	private static PreferenceFragment fragment;
	private static PreferenceScreen preferenceScreen;
	
	public static volatile boolean stopCheckingForServers = false;
	
	public static String serverURL;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		fragment = this;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Starting detection loop
		startDetectionLoop();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Stopping server detection loop
		stopDetectionLoop();
	}
	
	private static Handler serverDetectionHandler = new Handler() {
    	@Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case DETECT_SERVER_MSG: {
                	
                	Log.d("CREATING_ASYNC_TASK", "" + ServerDetectionActivity.CURRENT_UDP_PORT);
                	
                	// Executing one cycle of server detection
        			serverDetector = new ServerDetectorAsyncTask(ServerDetectionActivity.CURRENT_UDP_PORT);
        			serverDetector.execute();
        			
        			Log.d("CREATING_ASYNC_TASK", "" + ServerDetectionActivity.CURRENT_UDP_PORT);
        			
        			ArrayList<ServerInfo> serverList = null;
        			
        			// Waiting for task to finish and obtaining result
        			try {
						serverList = serverDetector.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
        			
        			if( serverList != null ) {
        				// Displaying servers list
        				preferenceScreen = fragment.getPreferenceManager().createPreferenceScreen(fragment.getActivity());
        				DisplayServersPrefList(serverList);
        				fragment.setPreferenceScreen(preferenceScreen);
        			}
        			
                    // Sending next message with the specified delay
                	serverDetectionHandler.sendMessageDelayed(serverDetectionHandler.obtainMessage(DETECT_SERVER_MSG), SERVER_DETECTION_FREQUENCY);
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
    private void startDetectionLoop() {
    	serverDetectionHandler.sendMessageDelayed(serverDetectionHandler.obtainMessage(DETECT_SERVER_MSG), 0);
    }
    public static void stopDetectionLoop() {
    	serverDetectionHandler.removeCallbacksAndMessages(null);
    }
    
    private static void DisplayServersPrefList(ArrayList<ServerInfo> serverList) {
		
		// Adding saved configurations list
		PreferenceCategory savedConfigurationsCategory = new PreferenceCategory(fragment.getActivity());
		savedConfigurationsCategory.setTitle("Available servers");
		
		preferenceScreen.addPreference(savedConfigurationsCategory);
		
		if( serverList.size() == 0 ) {
			
			Preference pref = new Preference(fragment.getActivity());
			
			String summary = fragment.getString(R.string.no_servers_available_summary);
			
			pref.setTitle("No servers available");
			pref.setSummary(summary);
			pref.setEnabled(false);
			
			savedConfigurationsCategory.addPreference(pref);
		}
		
		// Adding each configuration name to the preference list
		// cnt is used to assign each preference a different key
		int cnt=0;
		Iterator<ServerInfo> it = serverList.iterator();
		while( it.hasNext() ) {
			
			ServerInfo serverInfo = it.next();
			++ cnt;
			
			final Preference pref = new Preference(fragment.getActivity());
			
			String title = serverInfo.getName();
			String key = serverInfo.getName() + "-" + String.valueOf(cnt);
			
			pref.setTitle(title);
			pref.setSummary(serverInfo.getIp() + ":" + serverInfo.getPort());
			pref.setKey(key);
			
			savedConfigurationsCategory.addPreference(pref);
			
			pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick (Preference preference) {
					Log.d("PREFERENCE",(String) preference.getTitle());
					
					String key = preference.getKey();
					Log.d("PREFERENCE", key + " called");
					
					// Obtaining ip and port
					// CAUTION: This is very fragile. Work with extreme caution if you change the preference summary structure
					String url = (String) preference.getSummary();
					
					// Setting serverURL
					ServerDetectionFragment.serverURL = "http://" + url + "/";
					
					// Stopping detection loop
					ServerDetectionFragment.stopDetectionLoop();
					
					// Launching main activity
					LaunchMainActivity();
					
					return true;
				}
			});
		}
		
		PreferenceCategory manualSelectionCategory = new PreferenceCategory(fragment.getActivity());
		manualSelectionCategory.setTitle("Manual connection");
	
		preferenceScreen.addPreference(manualSelectionCategory);
		
		final Preference manualConnectionPref = new Preference(fragment.getActivity());
		
		String title = "Manual server connection";
		String key = MANUAL_SELECTION_PREF_KEY;
		
		String summary = fragment.getString(R.string.manual_server_selection_summary);
		
		manualConnectionPref.setTitle(title);
		manualConnectionPref.setSummary(summary);
		manualConnectionPref.setKey(key);
		
		manualSelectionCategory.addPreference(manualConnectionPref);
		
		manualConnectionPref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick (Preference preference) {
				Log.d("PREFERENCE",(String) preference.getTitle());
				
				// Creating and displaying dialog
				ShowIpSelectionDialog();
				
				return true;
			}
		});
    }

    // Creates and displays the manual IP selection dialog
    private static void ShowIpSelectionDialog() {
    	
    	// Creating dialog
    	AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
    	builder.setTitle("IP and Port selection");
    	
    	final EditText editText = new EditText(fragment.getActivity());
    	editText.setHint("<IP>:<Port>");
    	
    	builder.setView(editText);
    	
    	builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {

        		String ip = editText.getText().toString();
        		
        		if( ip.length() == 0 ) {
        			return;
        		}
        		
				// Setting serverURL
				ServerDetectionFragment.serverURL = "http://" + ip + "/";
				
				// Stopping detection loop
				ServerDetectionFragment.stopDetectionLoop();
				
				// Launching main activity
				LaunchMainActivity();
        	}
        });
    	
    	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {

        	}
        });
    	
    	// Creating dialog
    	AlertDialog dialog = builder.create();
    	
    	// Opening soft keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    	
        // Displaying dialog
    	dialog.show();
    	
    }
    
    //Launches MainActivity
	private static void LaunchMainActivity() {
	    
		// Starting MainActivity
		Intent intent = new Intent(fragment.getActivity(), MainActivity.class);
	    fragment.getActivity().startActivity(intent);
	    // Removing activity from back stack
	    fragment.getActivity().finish();
	}
}