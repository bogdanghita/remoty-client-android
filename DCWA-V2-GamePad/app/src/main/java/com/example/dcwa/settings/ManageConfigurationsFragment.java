package com.example.dcwa.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.example.dcwa.auxclasses.ButtonInfo;
import com.example.desktopcontrolwithandroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.WindowManager;

public class ManageConfigurationsFragment extends PreferenceFragment {
	
	public boolean currentConfigIsSaved;
	
	private String lastSavedConfigName;
	
	private HashMap<Integer,ButtonInfo> buttonMap;
	private Vector<String> savedConfigVector;
	
	PreferenceScreen preferenceScreen;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_manage_configurations));
		
        // Reading currentConfigIsSaved flag from file
        ReadFlagFile();
        
        buttonMap = new HashMap<Integer,ButtonInfo>();
		savedConfigVector = new Vector<String>();
	 	
		ReadConfigFilesList();
		
	 	// Retrieving arguments (current configuration file name)
	 	Bundle bundle = getArguments();
	 	if( bundle != null ) {
	 		lastSavedConfigName = bundle.getString("CURRENT_CONFIG_FILE");
	 		Log.d("MANAGE_CONFIG_FRAGMENT","Arguments successfully to retreived: " + lastSavedConfigName);
	 	}
	 	else {
	 		Log.d("MANAGE_CONFIG_FRAGMENT","No arguments to retreive");
	 	}
		
		// Displaying configuration list
		preferenceScreen = getPreferenceManager().createPreferenceScreen(getActivity());
	 	PopulatePreferenceScreen();
	 	setPreferenceScreen(preferenceScreen);
	 	
	}
	
//	--- Preference screen related methods ---
	
	// Populates the preference screen with the configuration list
	private void PopulatePreferenceScreen() {
		
		// Adding current configuration category
		PreferenceCategory lastSavedConfigNameCategory = new PreferenceCategory(getActivity());
		lastSavedConfigNameCategory.setTitle("Current configuration");
		
		preferenceScreen.addPreference(lastSavedConfigNameCategory);
		
		String currentConfigName;
		if( currentConfigIsSaved ) {
			currentConfigName = lastSavedConfigName;
		}
		else {
			currentConfigName = "Unsaved configuration";
		}
		
		String currentConfigPrefKey = "currentConfigPref";
		
		Preference currentConfigPref = new Preference(getActivity());
		currentConfigPref.setTitle(currentConfigName);
		currentConfigPref.setKey(currentConfigPrefKey);
		currentConfigPref.setSelectable(false);
		currentConfigPref.setEnabled(false);
		
		lastSavedConfigNameCategory.addPreference(currentConfigPref);
		
		currentConfigPref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick (Preference preference) {
				Log.d("PREFERENCE",(String) preference.getTitle());
		 			
				String key = preference.getKey();
				Log.d("PREFERENCE", key + " called");
				
				return true;
			}
		});
		
		// Adding saved configurations list
		PreferenceCategory savedConfigurationsCategory = new PreferenceCategory(getActivity());
		savedConfigurationsCategory.setTitle("Saved configurations");
		
		preferenceScreen.addPreference(savedConfigurationsCategory);
		
		// Adding each configuration name to the preference list
		// cnt is used to assign each preference a different key
		int cnt=0;
		Iterator<String> it = savedConfigVector.iterator();
		while( it.hasNext() ) {
			
			String configFile = it.next();
			++ cnt;
			
			final Preference pref = new Preference(getActivity());
			
			String title = configFile;
			String summary = ReadButtons(configFile);
			
			String key = configFile + "-" + String.valueOf(cnt);
			
			pref.setTitle(title);
			pref.setSummary(summary);
			pref.setKey(key);
			
			savedConfigurationsCategory.addPreference(pref);
			
			pref.setOnPreferenceClickListener (new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick (Preference preference) {
					Log.d("PREFERENCE",(String) preference.getTitle());
			 			
					String key = preference.getKey();
					Log.d("PREFERENCE", key + " called");
					
					String title = (String) preference.getTitle();
					showRenameDeleteDialog(title);
					
					return true;
				}
			});
		}
	}

	
//	--- Dialog related methods
	
	// Method that creates and displays the Rename/Delete dialog
	private void showRenameDeleteDialog(final String title) {
		
		final String[] keyNames = {"Rename", "Delete"};
        
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());		
		
		builder.setItems(keyNames, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	Log.d("PREF", keyNames[which] + " called - on item: " + title + " - current configuration: " + lastSavedConfigName + " - currentConfigIsSaved: " + currentConfigIsSaved);
		    	
		    	if( which == 0 ) {
		    		showRenameDialog(title);
		    	}
		    	else if( which == 1 ){
		    		DeleteConfiguration(title);
		    	}
				
		    }
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	// Method that creates and displays the Rename dialog
	private void showRenameDialog(final String title) {
	
		// TODO: show all configurations except the one that is to be renamed
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Rename");
		        
		// Creating parent layout
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		        
	    // Adding EditText to dialog
	    final EditText editText = new EditText(getActivity());
//		editText.setHint("Enter new configuration name");
	    editText.setText(title);
	    editText.setSingleLine();
	    editText.selectAll();
//		editText.setMinimumHeight(myHeight);
//		editText.setHeight(myHeight);
		        
	    int textAppearece = android.R.attr.textAppearanceListItem;
		        
		editText.setTextAppearance(editText.getContext(), textAppearece);
		
		// Adding ListView to dialog
		ListView configList = new ListView(getActivity());
		int simpleListItemId = android.R.layout.simple_list_item_1;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), simpleListItemId, savedConfigVector);
		
		// Assign adapter to ListView
		configList.setAdapter(adapter);
		// Setting click listener to each item
        configList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d("RENAME_CONFIGURATION","Item click: " +  savedConfigVector.get(arg2) );
                editText.setText(savedConfigVector.get(arg2));
                editText.setSelection(editText.getText().length());
                editText.selectAll();
            }
        });
		
        TextView separator = SelectionMapFragment.CreateHorizontalSeparator(layout,0,4,Color.LTGRAY);
        TextView aboveView = new TextView(getActivity());
		aboveView.setHeight(20);
		TextView belowView = new TextView(getActivity());
		belowView.setHeight(20);
        
        // Adding views to layout
        layout.addView(aboveView);
        layout.addView(editText);
        layout.addView(belowView);
        layout.addView(separator);
		layout.addView(configList);

		// Adding layout to dialog
		builder.setView(layout);       
		        
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		 
		// Adding PositiveButton to dialog
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newTitle = editText.getText().toString();
				Log.d("RENAME_CONFIGURATION","DONE pressed. Name: " + newTitle);
				
                // Checking if the file name is reserved by the app
                if( SettingsActivity.reservedFileNames.contains(newTitle) ) {
                	// Displaying ReservedFileNameDialog
                	showReservedFileNameDialog(newTitle, title);
                	return;
                }
				
                // Checking if configuration file name already exists
				if( savedConfigVector.contains(newTitle) ) {
					// Displaying "Overwrite configuration?" question dialog
					showOverwriteConfigurationQuestionDialog(title, newTitle);
				}
				else {
					// Rename configuration
					RenameConfiguration(title, newTitle);
				}
			}
		});
		
		// Openeing soft keyboard
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		dialog.show(); 
	}
	
	// Displays the ReservedFileNameDialog
	private void showReservedFileNameDialog(final String newTitle, final String oldTitle) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Error");
		builder.setMessage("The file name " + newTitle + " is reserved by the application for internal usage. Please choose another name.");
		
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // Displaying rename dialog
				showRenameDialog(oldTitle);
            }
        });
        
		AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Displays the "Overwrite <configuration name>?" question dialog
	private void showOverwriteConfigurationQuestionDialog(final String title, final String newTitle) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Overwrite " + newTitle + "?");
		
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Displaying rename dialog
				showRenameDialog(newTitle);
			}
		});
		
		// Adding PositiveButton to dialog
		builder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Overriding configuration
				savedConfigVector.remove(newTitle);
				
				// Checking if the deleted configuration file is the current configuration
				if( newTitle.equals(lastSavedConfigName) && currentConfigIsSaved == true ) {
					// Setting currentConfigIsSaveFlag to false
					currentConfigIsSaved = false;
					WriteFlagFile();
					
					// Updating current configuration name in lastSavedConfigNameCategory
					ReloadPreference();
				}
				
				// Overriding configuration
				RenameConfiguration(title, newTitle);
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}
	
	
//	--- Rename/Delete configuration methods ---
	
	// Renames configuration and reloads the preference screen items
	private void RenameConfiguration(String title, String newTitle) {
		
		int idx = savedConfigVector.indexOf(title);
		savedConfigVector.set(idx, newTitle);
		
		// Writing the vector to file
		WriteConfigFilesList();
		
		// Updating lastSavedConfigName
		lastSavedConfigName = newTitle;
		WriteLastSavedConfigName();
		
		// write new configuration to the file named as the new configuration name
		ReadButtonConfig(title);
		WriteCurrentConfig(newTitle);
		
		// Reloading preference
		ReloadPreference();
	}
	
	// Contains the delete configuration mechanism
	private void DeleteConfiguration(String title) {
		
		// Deleting configuration file from vector
		savedConfigVector.remove(title);
		// Writing the vector to file
		WriteConfigFilesList();
		
		// Checking if the deleted configuration file is the current configuration
		if( title.equals(lastSavedConfigName) && currentConfigIsSaved == true ) {
			// Setting currentConfigIsSaveFlag to false
			currentConfigIsSaved = false;
			WriteFlagFile();
			
			// No last saved configuration exists
			lastSavedConfigName = "Unsaved configuration";
			WriteLastSavedConfigName();
		}
		
		// Reloading preference
		preferenceScreen.removeAll();
		PopulatePreferenceScreen();
		setPreferenceScreen(preferenceScreen);
	}

	// Reloads preference categories and items according to the savedConfigVector
	private void ReloadPreference() {
		preferenceScreen.removeAll();
		PopulatePreferenceScreen();
		setPreferenceScreen(preferenceScreen);
	}
	
//	--- Configuration list and Flag files read/write methods ---
	
	// Reads the saved configurations files names from the file to the vector
	private void ReadConfigFilesList() {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(SelectionMapFragment.savedConfigListFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_CONFIG_FILES_LIST", SelectionMapFragment.savedConfigListFile + " not found");
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
		
		// Reading the number of files
		int N = Integer.parseInt(strTokens[0]);
			
		for(int i=1; i<N+1; ++i) {
			savedConfigVector.add(strTokens[i]);
		}
	}
	
	// Write the saved configurations files names from the vector to the file
	private void WriteConfigFilesList() {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(SelectionMapFragment.savedConfigListFile, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
			
		int N = savedConfigVector.size();
			
		// Writing first line
		String firstLine = String.valueOf(N) + "\n";
		try {
			outputStream.write(firstLine.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		// Writing each file name to the savedConfigListFile
		Iterator<String> it = savedConfigVector.iterator();
		while( it.hasNext() ) {
			
			String configFile = it.next() + "\n";
			try {
				outputStream.write( configFile.getBytes() );
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Writes the currentConfigIsSavedFlag to the flag file
	private void WriteFlagFile() {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(SelectionMapFragment.flagFile, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String flagValue;
		if( currentConfigIsSaved == true ) {
			flagValue = "1";
		}
		else {
			flagValue = "0";
		}

		try {
			outputStream.write( flagValue.getBytes() );
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	// Reads the currentConfigIsSavedFlag from the flag file
	private void ReadFlagFile() {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(SelectionMapFragment.flagFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_FLAG_FILE", SelectionMapFragment.flagFile + " not found");
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
		
		// Setting currentConfigIsSaved flag
		if( Integer.valueOf(fileContent) == 1 ) {
			currentConfigIsSaved = true;
		}
		else {
			currentConfigIsSaved = false;
		}		
	}
	
	// Method that writes the last saved configuration name from file
	private void WriteLastSavedConfigName() {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(SelectionMapFragment.lastSavedConfigFile, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			outputStream.write( lastSavedConfigName.getBytes() );
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
//	--- Configuration files methods (read/write)
	
	// Reads the button configuration from the filename to the buttonMap
	private void ReadButtonConfig(String filename) {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_BUTTON_CONFIG", filename + " not found");
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
		
		// Clearing buttonMap
		buttonMap.clear();
		
		// Separating fileContent into lines/tokens
		String delims = "[\n]+";
		String[] strTokens = fileContent.split(delims);
		
		// Reading the number of buttons
		int N = Integer.parseInt(strTokens[0]);
		
		// Checking if there are no buttons to load
		if( N == 0 ) {
			return;
		}
		
		// Creating each button and adding it to the layout
		for(int i=1; i<14*N; i+=14) {
			// Reading info for the current button
			
			String name = strTokens[i];
			String action = strTokens[i+1];
			String actionName = strTokens[i+2];
			int layoutId = Integer.valueOf(strTokens[i+3]);
			int nameId = Integer.valueOf(strTokens[i+4]);
			int actionId = Integer.valueOf(strTokens[i+5]);
			int pRowStart = Integer.valueOf(strTokens[i+6]);
			int pRowStop = Integer.valueOf(strTokens[i+7]);
			int pColumnStart = Integer.valueOf(strTokens[i+8]);
			int pColumnStop = Integer.valueOf(strTokens[i+9]);
			int lRowStart = Integer.valueOf(strTokens[i+10]);
			int lRowStop = Integer.valueOf(strTokens[i+11]);
			int lColumnStart = Integer.valueOf(strTokens[i+12]);
			int lColumnStop = Integer.valueOf(strTokens[i+13]);
			
			ButtonInfo button = new ButtonInfo(name, action, actionName, layoutId, nameId, actionId, 
					pRowStart, pRowStop, pColumnStart, pColumnStop, lRowStart, lRowStop, lColumnStart, lColumnStop);
			
			// Adding current button to the layout
			buttonMap.put(layoutId, button);
		}
		
	}
	
	// Writes the current configuration from the buttonMap to the filename
	private void WriteCurrentConfig(String filename) {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int N = buttonMap.size();
		
		// Writing first line
		String firstLine = String.valueOf(N) + "\n";
		try {
			outputStream.write(firstLine.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		Log.d("WRITE_TO_FILE",firstLine);
			
		ButtonInfo button;
		Iterator<Integer> it = buttonMap.keySet().iterator();
		while( it.hasNext() ) {
			
		    int key = it.next();
		    button = buttonMap.get(key);
		    
		    String name = button.getName() + "\n";
			String action = button.getAction() + "\n";
			String actionName = button.getActionName() + "\n";
			String strLayoutId = button.getLayoutId() + "\n";
			String strNameId = button.getNameId() + "\n";
			String strActionId = button.getActionId() + "\n";
			String pRowStart = String.valueOf(button.getPRowStart()) + "\n";
			String pRowStop = String.valueOf(button.getPRowStop()) + "\n";
			String pColumnStart = String.valueOf(button.getPColumnStart()) + "\n";
			String pColumnStop = String.valueOf(button.getPColumnStop()) + "\n";
			String lRowStart = String.valueOf(button.getLRowStart()) + "\n";
			String lRowStop = String.valueOf(button.getLRowStop()) + "\n";
			String lColumnStart = String.valueOf(button.getLColumnStart()) + "\n";
			String lColumnStop = String.valueOf(button.getLColumnStop()) + "\n";
				
			String strButtonInfo = name + action + actionName + strLayoutId + strNameId + strActionId +
					pRowStart + pRowStop + pColumnStart + pColumnStop +
					lRowStart + lRowStop + lColumnStart + lColumnStop;
			
			try {
				outputStream.write( strButtonInfo.getBytes() );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 
	private String ReadButtons(String filename) {
		
		String buttons="";
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_BUTTON_CONFIG", filename + " not found");
			return null;
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
			return null;
		}
				
				// Separating fileContent into lines/tokens
		String delims = "[\n]+";
		String[] strTokens = fileContent.split(delims);
		
		// Reading the number of buttons
		int N = Integer.parseInt(strTokens[0]);
		
		// Checking if there are no buttons to load
		if( N == 0 ) {
			return null;
		}
				
		// Creating each button and adding it to the layout
		for(int i=1; i<14*N; i+=14) {
			// Reading name for the current button
			String name = strTokens[i];
			
			if( i == 1 ) {
				buttons += name;
			}
			else {
				buttons += ", " + name;
			}
		}
		
		return buttons;
	}
}
