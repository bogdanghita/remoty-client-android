package com.example.dcwa.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.dcwa.networking.ServerDetectionActivity;
import com.example.desktopcontrolwithandroid.R;

public class DetectorSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	 	addPreferencesFromResource(R.xml.detector_settings_fragment);

	 	Preference pref = (Preference) findPreference("detector_udp_port");
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
	
}
