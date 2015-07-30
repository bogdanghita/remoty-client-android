package com.example.dcwa.settings;

import com.example.dcwa.mainfeatures.MainActivity;
import com.example.dcwa.networking.ServerDetectionActivity;
import com.example.desktopcontrolwithandroid.R;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsFragment extends PreferenceFragment {
	
	// Measurement units
	private final String TIMEOUT_AND_FRQUENCY_MEASUREMENT_UNIT = "(ms)";
	
	// IDs
	private final int POINTER_SENSITIVITY_SEEKBAR_ID = 4000;
	private final int SCROLL_SENSITIVITY_SEEKBAR_ID = 4001;
	private final int SINGLE_REQUEST_TIMEOUT_SEEKBAR_ID = 4010;
	private final int SINGLE_REQUEST_TIMEOUT_TEXT_VIEW_ID = 4011;
	private final int KEEP_ALIVE_FREQUENCY_SEEKBAR_ID = 4020;
	private final int KEEP_ALIVE_FREQUENCY_TEXT_VIEW_ID = 4021;
	
	// Layouts
	private LinearLayout pointerSensitivityLayout;
	private LinearLayout scrollSensitivityLayout;
	private LinearLayout singleRequestTimeoutLayout;
	private LinearLayout keepAliveRequestFrequencyLayout;
	
	// Unsaved seek bar and check box values
	private int pointerSensitivityUnsavedProgress = ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX;
	private int scrollSensitivityUnsavedProgress = ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX;
	private int singleReqSeekBarUnsavedProgress = ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX;
	private boolean singleReqCheckBoxUnsavedState = ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT;
	private int keepAliveSeekBarUnsavedProgress = ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX;
	private boolean keepAliveCheckBoxUnsavedState = ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY;
	
	// Defining the PointerSensitivity SeekBar listener
	OnSeekBarChangeListener pointerSensitivitySeekBarListener = new OnSeekBarChangeListener() {

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) { }
	    @Override
	    public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) { }
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	    	// Setting unsaved progress
	    	pointerSensitivityUnsavedProgress = seekBar.getProgress();
	    }
	};
	
	// Defining the ScrollSensitivity SeekBar listener
	OnSeekBarChangeListener scrollSensitivitySeekBarListener = new OnSeekBarChangeListener() {

	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) { }
	    @Override
	    public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) { }
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	    	// Setting unsaved progress
	    	scrollSensitivityUnsavedProgress = seekBar.getProgress();
	    }
	};
	
	// Defining the SingleRequestTimeout SeekBar listener
	OnSeekBarChangeListener singleRequestTimeoutSeekBarListener = new OnSeekBarChangeListener() {
		
		int value;
		
	    @Override
	    public void onStartTrackingTouch(SeekBar seekBar) { }
	    @Override
	    public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
	    	
	    	value = MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[progress];
	    	
	    	// Updating value in the text view
	    	TextView valueView = (TextView) singleRequestTimeoutLayout.findViewById(SINGLE_REQUEST_TIMEOUT_TEXT_VIEW_ID);
	    	valueView.setText(String.valueOf(value) + "\n" + TIMEOUT_AND_FRQUENCY_MEASUREMENT_UNIT );
	    }
	    @Override
	    public void onStopTrackingTouch(SeekBar seekBar) {
	    	// Setting unsaved progress
	    	singleReqSeekBarUnsavedProgress = seekBar.getProgress();
	    }
	 };
	 
	 // Defining the SingleRequestTimeout CheckBox listener
	 OnClickListener singleRequestTimeoutCheckBoxListener = new OnClickListener() {
		  @Override
		  public void onClick(View v) {
			  
			  SeekBar seekBar = (SeekBar) singleRequestTimeoutLayout.findViewById(SINGLE_REQUEST_TIMEOUT_SEEKBAR_ID);
			  
			  if( ((CheckBox)v).isChecked() ) {
				  // Disabling seek bar
				  seekBar.setEnabled(false);
				  
				  // Setting default seek bar value
				  seekBar.setProgress(ServerDetectionActivity.DEFAULT_SINGLE_REQUEST_TIMEOUT_IDX);
				  
				  // Setting unsaved check box 
				  singleReqCheckBoxUnsavedState = true;
				  singleReqSeekBarUnsavedProgress = ServerDetectionActivity.DEFAULT_SINGLE_REQUEST_TIMEOUT_IDX;
			  }
			  else {
				  // Enabling seek bar
				  seekBar.setEnabled(true);
				  
				  // Setting unsaved check box and seek bar state
				  singleReqCheckBoxUnsavedState = false;
				  singleReqSeekBarUnsavedProgress = seekBar.getProgress();
			  }
		  }
	};
	
	// Defining the SingleRequestTimeout SeekBar listener
	OnSeekBarChangeListener keepAliveSeekBarListener = new OnSeekBarChangeListener() {
		
		int value;
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) { }
		@Override
		public void onProgressChanged(SeekBar seekBark, int progress, boolean fromUser) {
			
			value = MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[progress];
			
			// Updating value in the text view
			TextView valueView = (TextView) keepAliveRequestFrequencyLayout.findViewById(KEEP_ALIVE_FREQUENCY_TEXT_VIEW_ID);
			valueView.setText(String.valueOf(value) + "\n" + TIMEOUT_AND_FRQUENCY_MEASUREMENT_UNIT );
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// Setting unsaved progress
			keepAliveSeekBarUnsavedProgress = seekBar.getProgress();
		}
	};
	
	// Defining the SingleRequestTimeout CheckBox listener
	OnClickListener keepAliveCheckBoxListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			SeekBar seekBar = (SeekBar) keepAliveRequestFrequencyLayout.findViewById(KEEP_ALIVE_FREQUENCY_SEEKBAR_ID);
			
			if( ((CheckBox)v).isChecked() ) {
				// Disabling seek bar
				seekBar.setEnabled(false);
				
				// Setting default seek bar value
				seekBar.setProgress(ServerDetectionActivity.DEFAULT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX);
				
				// Setting unsaved check box 
				keepAliveCheckBoxUnsavedState = true;
				keepAliveSeekBarUnsavedProgress = ServerDetectionActivity.DEFAULT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX;
			}
			else {
				// Enabling seek bar
				seekBar.setEnabled(true);
				
				// Setting unsaved check box and seek bar state
				keepAliveCheckBoxUnsavedState = false;
				keepAliveSeekBarUnsavedProgress = seekBar.getProgress();
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	 	addPreferencesFromResource(R.xml.settings_fragment);
	 	
	 	// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_activity_settings));
	 	
//	 	Log.d("SETTINGS_FRAGMENT","ON_CREATE called");
//	 	Log.d("SETTINGS_FRAGMENT", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
	 	
	 	// Setting on click listeners for the settings items
	 	SetOnClickListenersForPrefereceneItems();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// TODO: See if this is ok here or it just happens to work fine
		// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_activity_settings));
		
//		Log.d("SETTINGS_FRAGMENT","ON_RESUME called");
//		Log.d("SETTINGS_FRAGMENT", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
//		Log.d("SETTINGS_FRAGMENT","ON_PAUSE called");
//		Log.d("SETTINGS_FRAGMENT", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
		
//		 // Writing default values to file
//		 WriteCurrentSettingValues(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
//		Log.d("SETTINGS_FRAGMENT","ON_DESTROY called");
//		Log.d("SETTINGS_FRAGMENT", MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX + " - " + MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[MainActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX]);
	}
	
	// Method that sets the on click listeners for each settings item
	private void SetOnClickListenersForPrefereceneItems() {

	 	Preference pref = (Preference) findPreference("layout_configuration");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","button_activity click");
	 			
	 			// Starting selection map fragment
	 			SelectionMapFragment buttonFragment = new SelectionMapFragment();
	 			FragmentTransaction transaction = getFragmentManager().beginTransaction();
	 			transaction.replace(android.R.id.content, buttonFragment);
	 			transaction.addToBackStack(null);
	 			transaction.commit();
	 			
	 			return true;
	 		}
	 	});
	 	
	 	pref = (Preference) findPreference("single_request_timeout");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","single_request_timeout called");
	 			// Displaying single request timeout dialog
	 			ShowSingleRequestTimeoutDialog();
	 			return true;
	 		}
	 	});
	 	
	 	pref = (Preference) findPreference("keep_alive_request_frequency");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","keep_alive_request_frequency called");
	 			// Displaying keep alive request frequency dialog
	 			ShowKeepAliveRequestFrequencyDialog();
	 			return true;
	 		}
	 	});
	 	
	 	pref = (Preference) findPreference("pointer_sensitivity");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","pointer_sensitivity");
	 			// Displaying pointer sensitivity dialog
	 			ShowPointerSensitivityDialog();
	 			return true;
	 		}
	 	});
	 	
	 	pref = (Preference) findPreference("scroll_sensitivity");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","scroll_sensitivity");
	 			// Displaying scroll sensitivity dialog
	 			ShowScrollSensitivityDialog();
	 			return true;
	 		}
	 	});
	 	
	 	pref = (Preference) findPreference("udp_port");
	 	pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
	 		@Override
	 		public boolean onPreferenceClick (Preference preference) {
	 			Log.d("PREFERENCE","udp_port");
	 			// Displaying UDP port dialog
	 			ShowUdpPortDialog();
	 			return true;
	 		}
	 	});
	}
	
	// Creates and displays the PointerSensitivity dialog
	private void ShowPointerSensitivityDialog() {
		
		// Saving initial value
		final int initialSeekbarProgress = pointerSensitivityUnsavedProgress;
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Pointer sensitivity");
		
		LinearLayout dialogLayout = CreateSimpleDialogLayout(ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX, 
				MainActivity.POINTER_SENSITIVITY_VALUES.length, 0, POINTER_SENSITIVITY_SEEKBAR_ID, pointerSensitivitySeekBarListener);
		
		// Adding dialog layout to the builder
		builder.setView(dialogLayout);
	
		// Adding PositiveButton to dialog
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {

        		ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX = pointerSensitivityUnsavedProgress;
        		
        		// Writing updated settings value to file
        		SettingsActivity.WriteCurrentSettingValues(getActivity());
        	}
        });
        
        // Adding negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		// Resetting the seek bar and check box values
        		pointerSensitivityUnsavedProgress = initialSeekbarProgress;
        	}
        });
        
        // Creating and displaying dialog
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Creates and displays the ScrollSensitivity dialog
	private void ShowScrollSensitivityDialog() {
		
		// Saving initial value
		final int initialSeekbarProgress = scrollSensitivityUnsavedProgress;
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Scroll sensitivity");
		
		LinearLayout dialogLayout = CreateSimpleDialogLayout(ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX, 
				MainActivity.SCROLL_SENSITIVITY_VALUES.length, 1, SCROLL_SENSITIVITY_SEEKBAR_ID, scrollSensitivitySeekBarListener);
		
		// Adding dialog layout to the builder
		builder.setView(dialogLayout);
		
		// Adding PositiveButton to dialog
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {

        		ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX = scrollSensitivityUnsavedProgress;
        		
        		// Writing updated settings value to file
        		SettingsActivity.WriteCurrentSettingValues(getActivity());
        	}
        });
        
        // Adding negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		// Resetting the seek bar and check box values
        		scrollSensitivityUnsavedProgress = initialSeekbarProgress;
        	}
        });
        
        // Creating and displaying dialog
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Creates and displays the SingleRequestTimeout dialog
	private void ShowSingleRequestTimeoutDialog() {
		
		// Saving initial values
		final int initialSeekbarProgress = singleReqSeekBarUnsavedProgress;
		final boolean initialCheckBoxState = singleReqCheckBoxUnsavedState;
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Single request timeout");
		
		// Creating dialog layout
		LinearLayout dialogLayout = CreateDialogLayoutWithCheckbox(
				ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX, ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT,
				MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES.length, TIMEOUT_AND_FRQUENCY_MEASUREMENT_UNIT,
				0, SINGLE_REQUEST_TIMEOUT_TEXT_VIEW_ID, SINGLE_REQUEST_TIMEOUT_SEEKBAR_ID,
				singleRequestTimeoutSeekBarListener, singleRequestTimeoutCheckBoxListener);

		// Adding dialog layout to the builder
		builder.setView(dialogLayout);
		
		// Adding PositiveButton to dialog
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		
        		ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX = singleReqSeekBarUnsavedProgress;
        		ServerDetectionActivity.CHECKBOX_SINGLE_REQUEST_TIMEOUT = singleReqCheckBoxUnsavedState;
        		
        		// Writing updated settings value to file
        		SettingsActivity.WriteCurrentSettingValues(getActivity());
        	}
        });
        
        // Adding negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		// Resetting the seek bar and check box values
        		singleReqSeekBarUnsavedProgress = initialSeekbarProgress;
        		singleReqCheckBoxUnsavedState = initialCheckBoxState;
        	}
        });
        
        // Creating and displaying dialog
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Creates and displays the SingleRequestTimeout dialog
	private void ShowKeepAliveRequestFrequencyDialog() {
		
		// Saving initial values
		final int initialSeekbarProgress = keepAliveSeekBarUnsavedProgress;
		final boolean initialCheckBoxState = keepAliveCheckBoxUnsavedState;
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Keep alive request timeout (think of a title that actually has a meaning for the user)");
		
		// Creating dialog layout
		LinearLayout dialogLayout = CreateDialogLayoutWithCheckbox(
				ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX, ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY, 
				MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES.length, TIMEOUT_AND_FRQUENCY_MEASUREMENT_UNIT,
				1, KEEP_ALIVE_FREQUENCY_TEXT_VIEW_ID, KEEP_ALIVE_FREQUENCY_SEEKBAR_ID,
				keepAliveSeekBarListener, keepAliveCheckBoxListener);

		// Adding dialog layout to the builder
		builder.setView(dialogLayout);
		
		// Adding PositiveButton to dialog
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		
        		ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX = keepAliveSeekBarUnsavedProgress;
        		ServerDetectionActivity.CHECKBOX_KEEP_ALIVE_REQUEST_FREQUENCY = keepAliveCheckBoxUnsavedState;
        		
        		// Writing updated settings value to file
        		SettingsActivity.WriteCurrentSettingValues(getActivity());
        	}
        });
        
        // Adding negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		// Resetting the seek bar and check box values
        		keepAliveSeekBarUnsavedProgress = initialSeekbarProgress;
        		keepAliveCheckBoxUnsavedState = initialCheckBoxState;
        	}
        });
        
        // Creating and displaying dialog
        AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Creates and displays the UDP port dialog
	private void ShowUdpPortDialog() {
		
		// Creating dialog
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	builder.setTitle("UDP port");
    	
    	final EditText editText = new EditText(getActivity());
    	editText.setText(String.valueOf(ServerDetectionActivity.CURRENT_UDP_PORT));
    	editText.selectAll();
    	
    	builder.setView(editText);
    	
    	builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {

        		String strPort = editText.getText().toString();
        		
        		ServerDetectionActivity.CURRENT_UDP_PORT = Integer.valueOf(strPort);
        		
        		// Writing updated settings value to file
        		SettingsActivity.WriteCurrentSettingValues(getActivity());
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
	
	// Creating simple seek bar dialog layout
	private LinearLayout CreateSimpleDialogLayout(int seekBarValue, int numberOfSteps, 
			int settingsEntryType, int seekId, OnSeekBarChangeListener seekBarListener) {
	
		// Initializing dialog layout
		LinearLayout dialogLayout = null;
		
		if( settingsEntryType == 0 ) {
			pointerSensitivityLayout = new LinearLayout(getActivity());
			dialogLayout = pointerSensitivityLayout;
		}
		else if( settingsEntryType == 1 ) {
			scrollSensitivityLayout = new LinearLayout(getActivity());
			dialogLayout = scrollSensitivityLayout;
		}
		
		// Setting dialog layout orientation
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		
		// Creating above and below empty views which help center the seekBar layout
		TextView aboveView = new TextView(getActivity());
		aboveView.setHeight(40);
		TextView belowView = new TextView(getActivity());
		belowView.setHeight(40);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		dialogLayout.setLayoutParams(params);
		
		// Creating seekBar
		final SeekBar seekBar = new SeekBar(getActivity());
		seekBar.setId(seekId);
		
		// Setting seek bar progress and state
		seekBar.setMax(numberOfSteps-1);
		seekBar.setProgress(seekBarValue);
		
		// Setting seekBar params
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(10, 0, 10, 0);
		seekBar.setLayoutParams(params);
		
		// Setting seek bar listener
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		
		// Adding views to dialog layout
		dialogLayout.addView(aboveView);
		dialogLayout.addView(seekBar);
		dialogLayout.addView(belowView);
		
		return dialogLayout;
	}
	
	// Creating check box and value text view dialog layout
	private LinearLayout CreateDialogLayoutWithCheckbox(int seekBarValue, boolean checkBoxState, int numberOfSteps, String measurementUnit,
			int settingsEntryType, int textId, int seekId, OnSeekBarChangeListener seekBarListener, OnClickListener checkBoxListener) {
		
		final int VERTICAL_SEPARATOR_ID = textId + 1;
		
		// Initializing layout and value range array
		LinearLayout layout = null;
		int[] valuesArray = null;
		
		if( settingsEntryType == 0 ) {
			singleRequestTimeoutLayout = new LinearLayout(getActivity());
			layout = singleRequestTimeoutLayout;
			valuesArray = MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES;
		}
		else if( settingsEntryType == 1 ) {
			keepAliveRequestFrequencyLayout = new LinearLayout(getActivity());
			layout = keepAliveRequestFrequencyLayout;
			valuesArray = MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES;
		}
		
		// Creating dialog layout
		LinearLayout dialogLayout = new LinearLayout(getActivity());
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		
		// Creating above and below empty views which help center the seekBar layout
		TextView aboveView = new TextView(getActivity());
		aboveView.setHeight(20);
		TextView belowView = new TextView(getActivity());
		belowView.setHeight(20);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		
		// Creating value text view
		String textValue = String.valueOf(valuesArray[seekBarValue]) + "\n" + measurementUnit;
		final TextView seekValueView = new TextView(getActivity());
		seekValueView.setText(textValue);
		seekValueView.setId(textId);
		
		// Setting value text params
		params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 20);
		seekValueView.setLayoutParams(params);
		seekValueView.setGravity(Gravity.CENTER);
		
		// Creating vertical separator
		TextView verticalSeparator = new TextView(getActivity());
		verticalSeparator.setBackgroundColor(Color.LTGRAY);
		verticalSeparator.setId(VERTICAL_SEPARATOR_ID);
		
		// Creating seekBar
		final SeekBar seekBar = new SeekBar(getActivity());
		seekBar.setId(seekId);
		
		// Setting seek bar progress and state
		seekBar.setMax(numberOfSteps-1);
		seekBar.setProgress(seekBarValue);
		seekBar.setEnabled(!checkBoxState);
		
		// Setting vertical separator params
		params = new LinearLayout.LayoutParams(1, LayoutParams.MATCH_PARENT, 0);
		params.setMargins(10, 0, 0, 0);
		verticalSeparator.setLayoutParams(params);
		
		// Setting seekBar params
		params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 80);
		params.setMargins(5, 0, 0, 0);
		seekBar.setLayoutParams(params);
		
		// Setting seek bar listener
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		 
		// Adding seekBar, separator and value text view to the seekBar layout
		layout.addView(seekBar);
		layout.addView(verticalSeparator);
		layout.addView(seekValueView);

		// Creating check box
		CheckBox checkBox = new CheckBox(getActivity());
		checkBox.setText("Use default value");
		checkBox.setChecked(checkBoxState);
		
		checkBox.setOnClickListener(checkBoxListener);
		
		// Creating separator
		TextView separator = SelectionMapFragment.CreateHorizontalSeparator(dialogLayout,20,1,Color.LTGRAY);
		
		// Adding views to dialog layout
		dialogLayout.addView(aboveView);
		dialogLayout.addView(layout);
		dialogLayout.addView(belowView);
		dialogLayout.addView(separator);
		dialogLayout.addView(checkBox);
		
		return dialogLayout;
	}
	
}
