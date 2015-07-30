package com.example.dcwa.settings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.example.dcwa.auxclasses.ButtonDimensions;
import com.example.dcwa.auxclasses.ButtonInfo;
import com.example.dcwa.auxclasses.ButtonParameters;
import com.example.dcwa.mainfeatures.ShortcutControlFragment;
import com.example.desktopcontrolwithandroid.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class SelectionMapFragment extends Fragment {
	
	public boolean mapLoadedFlag = false;
	OnGlobalLayoutListener selectionLayoutListener;
	
	private final int SELECTION_TEXT_SIZE_RATIO = 8;
	
	public static boolean currentConfigFileExists = false;
	public static boolean currentConfigIsSaved;
	
	public boolean reloadConfigurationFlag = false;
	private boolean newConfigurationFlag = false;
	// USE THIS IF YOU WANT THE ACTION PICKER DIALOG TO BE DISPLAYED AFTER USER DRAWS A BUTTON
//	private boolean setDefaultButtonNameFlag;
	
	private int MAP_CELL_COLOR = Color.WHITE;
	
	private RelativeLayout parentLayout;
	private View parentView;
	
	private int startRowIndex, startColumnIndex;
	float startX, stopX;
	float startY, stopY;
	
	int[][] idMatrix;
	boolean[][] availabilityMatrix;
	int[][] colorMatrix;
	
	private String keyCodesFile;
	
	private String keyCodes[];
	private String keyNames[];
	private Vector<String> keyCodesVector;
	
	private HashMap<Integer,ButtonInfo> buttonMap;
	private Vector<String> savedConfigVector;
	private Vector<Integer> buttonIdVector;
	
	public static String savedConfigListFile;
	public static String flagFile;
	public static String lastSavedConfigFile;
	
	public String lastSavedConfigName;
	
	private String toLoadConfigFile;
	
	private int myHeight;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_layout_configuration));
		
		Log.d("SELECTION_MAP_FRAGMENT", "onCreateView() called");
		
		setHasOptionsMenu(true);
		
		parentView = inflater.inflate(R.layout.fragment_selection_map, container, false);
		parentLayout = (RelativeLayout) parentView.findViewById(R.id.selection_map_fragment_layout);
		
        // Initializing the buttonMap
		buttonMap = new HashMap<Integer, ButtonInfo>();
		savedConfigVector = new Vector<String>();
		buttonIdVector = new Vector<Integer>();
		
		// Reading keyCodesFile name
		keyCodesFile = getString(R.string.virtual_keycodes);
		// Reading configuration files list file name
		savedConfigListFile = getString(R.string.config_files_list_file);
        // Reading flag file name
        flagFile = getString(R.string.flag_file);
        // Reading last saved configuration file name
        lastSavedConfigFile = getString(R.string.last_saved_config_file);
		
		// Reading key codes
		String[][] keyInfo = ReadKeyCodes();
		keyCodes = keyInfo[1];
		keyNames = keyInfo[0];
		keyCodesVector = new Vector<String>(Arrays.asList(keyCodes));
        
		// Creating OnGlobalLayoutListener()
		selectionLayoutListener = new OnGlobalLayoutListener() {
			@Override
		    public void onGlobalLayout() {
		    	
				if( mapLoadedFlag == true && SettingsActivity.settingsConfigurationChanged == false ) {
					return;
				}
				
				// Computing view dimensions
				ShortcutControlFragment.parentHeight = parentLayout.getHeight();
				ShortcutControlFragment.parentWidth = parentLayout.getWidth();
				ShortcutControlFragment.parentViewStartPoint = parentLayout.getRootView().getHeight() - ShortcutControlFragment.parentHeight;
				
				Log.d("SELECTION_MAP_FRAGMENT","ON_GLOBAL_LAYOUT_LISTENER called");
				Log.d("	parentLayout: (height, width, start)", ShortcutControlFragment.parentHeight + "," +
						ShortcutControlFragment.parentWidth + "," + ShortcutControlFragment.parentViewStartPoint);
				
				// Computing map parameters
				SettingsActivity.ComputeMapParamteters();
				
				// Removing all buttons
		    	parentLayout.removeAllViews();
		   		
				// Creating map
				CreateMap();
		        // Loading and displaying configuration
		        LoadAndDisplayConfiguration();
				
				// Setting the onTouchListener
				SetOnTouchListener();
				
				// Reading currentConfigIsSaved flag from file
				ReadFlagFile();
				
				// Reading last saved configuration file name
				ReadLastSavedConfigName();
				
				mapLoadedFlag = true;
				SettingsActivity.settingsConfigurationChanged = false;
				
				Log.d("SELECTION_MAP_FRAGMENT", "ON_RESUME");
			}
		};
		
		// INFO: not sure if this is used but keep it here and check if it is useful later
		// inflate layout to get that fucking height
        View view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        TextView txt1 = (TextView) view.findViewById(android.R.id.text1);
        myHeight = txt1.getHeight();
        
		return parentView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mapLoadedFlag = false;
		
		// Adding On OnGlobalLayoutListener
		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(selectionLayoutListener);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Removing OnGlobalLayoutListener
        parentLayout.getViewTreeObserver().removeGlobalOnLayoutListener(selectionLayoutListener);
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	inflater.inflate(R.menu.selection_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent getActivity() in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_save) {
			Log.d("BUTTON_CONFIG_FRAGMENT","SAVE action bar item pressed");
			showSaveDialog();
			return true;
		}
		else if (id == R.id.action_load) {
			Log.d("BUTTON_CONFIG_FRAGMENT","LOAD action bar item pressed");
			
			// Check if current configuration is saved
			if( currentConfigIsSaved == false ) {
				// I am setting this flag to false to tell the DiscardChanges method that it was
				// called from the Load dialog and not from the New dialog
				// P.S. Dear future me, I am very tired right now and you'd better thank me for putting this comment here and
				// sparing you from another 10 minutes of cursing and asking the universal developer question: WTF is this code doing?
				// 20.09.2014, 00:07 - fixing final bugs and preparing for release)
				newConfigurationFlag = false;
				showDiscardChangesQuestionDialog(toLoadConfigFile);
			}
			else {
				showLoadDialog();
			}
			
			return true;
		}
		else if (id == R.id.action_new) {
			Log.d("BUTTON_CONFIG_FRAGMENT","NEW action bar item pressed");
			
			if( currentConfigIsSaved ) {
				ClearMap();
			}
			else {
				newConfigurationFlag = true;
				showDiscardChangesQuestionDialog(null);
			}
			
			return true;
		}
		else if (id == R.id.action_manage) {
			Log.d("BUTTON_CONFIG_FRAGMENT","MANAGE CONFIGURATIONS action bar item pressed");
			showEditFragment();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
//	--- Startup mechanism ---
	
	// Loads and display the startup configuration
	private void LoadAndDisplayConfiguration() {
		
		// Checking current configuration file and loading configuration in the buttonMap
		if( checkCurrentConfigFile() == true ) {
			// Reading current configuration
			ReadButtonConfig(ShortcutControlFragment.currentConfigFile);
			
			// Reading saved configurations names to vector
			ReadConfigFilesList();
		}
		else {
			// Reading configuration from KMPConfigFile (written in ShortcutControlFragment)
			ReadButtonConfig(ShortcutControlFragment.VLCConfigFile);
			
			// Writing VLC configuration to the current configuration file
			WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
			
			// Adding VLC and KMP configuration files to the configuration files vector
			savedConfigVector.add(ShortcutControlFragment.VLCConfigFile);
			savedConfigVector.add(ShortcutControlFragment.KMPConfigFile);
			// Writing configuration files list from vector to file
			WriteConfigFilesList();
			
			// Setting currentConfigIsSaved flag to true
			currentConfigIsSaved = true;
			WriteFlagFile();
			
			// Writing last saved configuration name to file
			lastSavedConfigName = ShortcutControlFragment.VLCConfigFile;
			WriteLastSavedConfigName();
		}
		// Displaying current configuration from buttonMap
		DisplayMapConfig();
	}
	
	
//	--- Configuration list, Flag and Last saved configuration files read/write methods ---
	
	// Reads the saved configurations file names from the file to the vector
	private void ReadConfigFilesList() {
		
		// Clearing vector
		savedConfigVector.removeAllElements();
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(savedConfigListFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_CONFIG_FILES_LIST", savedConfigListFile + " not found");
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
			outputStream = getActivity().openFileOutput(savedConfigListFile, Context.MODE_PRIVATE);
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
	
	// Reads the currentConfigIsSavedFlag from the flag file
	private void ReadFlagFile() {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(flagFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_FLAG_FILE", flagFile + " not found");
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
	
	// Writes the currentConfigIsSavedFlag to the flag file
	private void WriteFlagFile() {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(flagFile, Context.MODE_PRIVATE);
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
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// Method that reads the last saved configuration name from file
	private void ReadLastSavedConfigName() {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(lastSavedConfigFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_FLAG_FILE", lastSavedConfigFile + " not found");
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
		
		lastSavedConfigName = fileContent;
	}
	
	// Method that writes the last saved configuration name from file
	private void WriteLastSavedConfigName() {
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(lastSavedConfigFile, Context.MODE_PRIVATE);
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
	
	
//	--- Configuration files methods (read/write/load) and "key codes" read method ---
	
	// Method that reads the keyCodes from file
    private String[][] ReadKeyCodes() {
    	
		// Reading default configuration from assets file
        InputStream is = null;
		try {
			is = getActivity().getAssets().open(keyCodesFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader in =  new BufferedReader(new InputStreamReader(is));
		
		// Reading the number of lines
		String NoOfLines = null;
		try {
			NoOfLines = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("READ_KEYCODES","Reading no of key codes failed");
			return null;
		}
		int N = Integer.parseInt(NoOfLines);
		
		// Reading key codes and adding them to the array
		String[][] keyCodesList = new String[2][N];
		
		String line="";
		
		for(int i=0; i<N; i++) {
			
			// Reading current line
			try {
				line = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("READ_KEYCODES","Reading key code no " + i + " failed");
			}
			
			// Getting name and code from line
			String delims = "[ \t]+";
			String[] strTokens = line.split(delims);
			keyCodesList[0][i] = strTokens[0];
			keyCodesList[1][i] = strTokens[1];
		}
		
		// Closing file
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return keyCodesList;
    }
	
	// Checks whether the currentConfigFile exists (returns true if it exists)
	private boolean checkCurrentConfigFile() {

		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(ShortcutControlFragment.currentConfigFile);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("CHECK_CURRENT_CONFIG_FILE", ShortcutControlFragment.currentConfigFile + " NOT found");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d("CHECK_CURRENT_CONFIG_FILE", ShortcutControlFragment.currentConfigFile + " exists");
		return true;
	}

	// Reads the button configuration from the filename to the buttonMap
	private void ReadButtonConfig(String filename) {
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d("READ_BUTTON_CONFIG", filename + " not found");
			// If configuration file is not found, an empty button configuration will be displayed
			ClearMap();
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
	
	// Loads the configuration from the specified file and displays it as the current configuration
	private void LoadConfigurationFromFile(String filename) {
		// Clearing map
		ClearMap();
		// Loading the configuration
		ReadButtonConfig(filename);
		// Writing it to the currentConfigFile
		WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
		// Display map configuration
		DisplayMapConfig();
		
		// Updating current configuration file name
		lastSavedConfigName = filename;
		WriteLastSavedConfigName();
	}
	
	
// 	--- Dialogs responding to action bar entries (Save, Load, Manage/Edit) and EditFragment ---
    
	// Displays the Save dialog 
	private void showSaveDialog() {
		
		// Creating dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Save");
        
        // Creating parent layout
		LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        
        // TODO: check for other configurations saved as NewConfiguration<number>
        // and add the first available number
        String configName = "NewConfiguration";
        
        // Adding EditText to dialog
        final EditText editText = new EditText(getActivity());
        editText.setText(configName);
        editText.setSingleLine();
        editText.selectAll();
        editText.setMinimumHeight(myHeight);
//        editText.setHeight(myHeight);
        
        int textAppearece = android.R.attr.textAppearanceListItem;
        
        editText.setTextAppearance(editText.getContext(), textAppearece);
        
        // TODO: solve the edit text height problem
        
//        int minHeight = android.R.attr.minHeight;
//        int editTextStyle = android.R.attr.editTextStyle;
        
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, minHeight);
//        editText.setLayoutParams(layoutParams);
        
        // Adding ListView to dialog
        ListView configList = new ListView(parentLayout.getContext());
        int simpleListItemId = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(parentLayout.getContext(), simpleListItemId, savedConfigVector);
        
        // Assign adapter to ListView
        configList.setAdapter(adapter);
        // Setting click listener to each item
        configList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d("SAVE_NEW_CONFIG","Item click: " +  savedConfigVector.get(arg2) );
                editText.setText(savedConfigVector.get(arg2));
                editText.setSelection(editText.getText().length());
                editText.selectAll();
            }
        });
        
        TextView separator = CreateHorizontalSeparator(layout,0,4,Color.LTGRAY);
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
                String name = editText.getText().toString();
                Log.d("SAVE_NEW_CONFIGURATION","Name: " + name);
                
                // Save to the file with the specified name
                String configfilename = editText.getText().toString();
                
                // Checking if the file name is reserved by the app
                if( SettingsActivity.reservedFileNames.contains(configfilename) ) {
                	// Displaying ReservedFileNameDialog
                	showReservedFileNameDialog(configfilename);
                	return;
                }
                
                // Checking if configuration file name already exists
                if( savedConfigVector.contains(configfilename) ) {
                	// Displaying "Overwrite configuration?" question dialog
                	showOverwriteConfigurationQuestionDialog(configfilename);
                }
                else {
                	// Saving configuration
                	SaveConfiguration(configfilename, false);
                	
                	// Updating current configuration file name
            		lastSavedConfigName = configfilename;
            		WriteLastSavedConfigName();
                }
                
            }
        });
        
        // Creating dialog
        AlertDialog dialog = builder.create();
        
        // Opening soft keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        // Displaying dialog
        dialog.show();
	}
	
	// Contains the save configuration mechanism
	private void SaveConfiguration(String configfilename, boolean overwriteFlag) {
		
		// Checking if the configuration is overridden
		if( overwriteFlag == false ) {
			// Adding new configuration file to vector
	    	savedConfigVector.add(configfilename);
	    	// Writing the vector to file
	    	WriteConfigFilesList();
		}
		
    	//Writing current configuration to the specified file
    	WriteCurrentConfig(configfilename);
    	
    	// Save current configuration. Setting currentConfigIsSaved flag to true
    	currentConfigIsSaved = true;
    	WriteFlagFile();
    	
    	// HERE!!!
    	lastSavedConfigName = configfilename;
    	WriteLastSavedConfigName();
	}
	
	// Displays the ReservedFileNameDialog
	private void showReservedFileNameDialog(final String configfilename) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Error");
		builder.setMessage("The file name " + configfilename + " is reserved by the application for internal usage. Please choose another name.");
		
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // Displaying save dialog
                showSaveDialog();
            }
        });
        
		AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Displays the "Overwrite <configuration name>?" question dialog
	private void showOverwriteConfigurationQuestionDialog(final String configfilename) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Overwrite configuration");
		builder.setMessage("A configuration named " + configfilename + " already exists. Overwrite it?");
		
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // Displaying save dialog
                showSaveDialog();
            }
        });
 
        // Adding PositiveButton to dialog
		builder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	// Overriding configuration
            	SaveConfiguration(configfilename, true);
            	
            	// Updating current configuration file name
        		lastSavedConfigName = configfilename;
        		WriteLastSavedConfigName();
            }
        });
        
		AlertDialog dialog = builder.create();
        dialog.show();
		
	}
	
	// Displays the Load dialog
	private void showLoadDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Load");
		
		// Converting vector to array
		String[] savedConfigArray = savedConfigVector.toArray(new String[savedConfigVector.size()]);
		
		builder.setItems(savedConfigArray, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				toLoadConfigFile = savedConfigVector.get(which);
				
				Log.d("LOAD_CONFIGURATION","LOAD button clicked: " + toLoadConfigFile);
		    	
				LoadConfigurationFromFile(toLoadConfigFile);
				
				// Save current configuration. Setting currentConfigIsSaved flag to true
		    	currentConfigIsSaved = true;
		    	WriteFlagFile();
		    	
		    	// HERE!!!
		    	lastSavedConfigName = toLoadConfigFile;
		    	WriteLastSavedConfigName();
		    }
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	// Displays the "save current configuration?" question dialog
	private void showDiscardChangesQuestionDialog(final String configfilename) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Configuration not saved");
		builder.setMessage("Current configuration is not saved. Discard unsaved changes?");
		
		// Adding NegativeButton to dialog (Cancel)
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Adding PositiveButton to dialog (Discard)
		builder.setPositiveButton("Discard changes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
                // Check if it is called by Load method or by New method
                if( newConfigurationFlag == true ) {
                    // Current configuration discarded. Setting currentConfigIsSaved flag to true
                    currentConfigIsSaved = true;
    	    		WriteFlagFile();
    	    		
    	    		// Checking if the last save configuration exits
    	    		if( ! savedConfigVector.contains(lastSavedConfigName) ) {
    	    			lastSavedConfigName = "No configuration loaded";
    	    			WriteLastSavedConfigName();
    	    		}
    	    		
    	    		// Loading last saved configuration file
    	    		LoadConfigurationFromFile(lastSavedConfigName);
    	    		
                	// Called by New method
                	ClearMap();
            		newConfigurationFlag = false;
                }
                else {
                	// Called by Load method
                	showLoadDialog();
                	
                	// Checking if the last save configuration exits
    	    		if( ! savedConfigVector.contains(lastSavedConfigName) ) {
    	    			lastSavedConfigName = "No configuration loaded";
    	    			WriteLastSavedConfigName();
    	    		}
                	
                    // Current configuration discarded. Setting currentConfigIsSaved flag to true
                    currentConfigIsSaved = true;
    	    		WriteFlagFile();
                	
    	    		// Loading last saved configuration file
    	    		LoadConfigurationFromFile(lastSavedConfigName);
                }
            }
        });
        
		AlertDialog dialog = builder.create();
        dialog.show();
	}
	
	// Method that displays the Manage/Edit fragment
	private void showEditFragment() {
		
		// Creating fragment
		ManageConfigurationsFragment editPreferenceFragment = new ManageConfigurationsFragment();
		
		// Sending current configuration file name to the fragment
		Bundle bundle = new Bundle();
		bundle.putString("CURRENT_CONFIG_FILE", lastSavedConfigName);
		editPreferenceFragment.setArguments(bundle);
		
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(android.R.id.content, editPreferenceFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	
//	--- Dialogs responding to clicks on a "button view" ---
	
	// Creates and displays the Rename/Change Action/Delete dialog
	private void showRenameChangeActionDeleteDialog(final int layoutId) {
		
		final String[] choiceList = {"Rename", "Change Action", "Delete"};
        
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		final ButtonInfo button = buttonMap.get(layoutId);
		
		builder.setItems(choiceList, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	Log.d("PREF", choiceList[which] + " called");
		    	
		    	if( which == 0 ) {
		    		showRenameDialog(layoutId);
		    	}
		    	else if( which == 1 ){
		    		showActionPickerDialog(layoutId);
		    	}
		    	else {
		    		deleteButtonLayout(button, layoutId);
		    	}
				
		    }
		});
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	// Deletes the specified button layout
	private void deleteButtonLayout(ButtonInfo button, int layoutId) {
		
		// Computing button dimensions
		int rowStart = 0, rowStop = 0, columnStart = 0, columnStop = 0;
		
		// Checking orientation
		if(getActivity().getResources().getConfiguration().orientation == 1) {
			rowStart = button.getPRowStart();
			rowStop = button.getPRowStop();
			columnStart = button.getPColumnStart();
			columnStop = button.getPColumnStop();
		}
		else if(getActivity().getResources().getConfiguration().orientation == 2) {
			rowStart = button.getLRowStart();
			rowStop = button.getLRowStop();
			columnStart = button.getLColumnStart();
			columnStop = button.getLColumnStop();
		}
		
		// Changing color of current rectangle, updating colorMatrix and availabilityMatrix
		for(int i=rowStart; i<=rowStop; ++i) {
			for(int j=columnStart; j<=columnStop; ++j) {
				TextView textView = (TextView)parentView.findViewById(idMatrix[i][j]);
				textView.setBackgroundColor(MAP_CELL_COLOR);
				availabilityMatrix[i][j] = true;
				colorMatrix[i][j] = MAP_CELL_COLOR;
			}
		}
		
		// Removing button view from layout
		LinearLayout buttonLayout = (LinearLayout)parentLayout.findViewById(layoutId);
		parentLayout.removeView(buttonLayout);
		
		// Removing button from map
		buttonMap.remove(layoutId);
		// When I use this the dialog dose not disappear
//		buttonIdVector.remove(id);
		
		// Writing current configuration to currentConfigFile
		WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
		
		// Modifications occurred. Setting currentConfigIsSaved flag to false
		currentConfigIsSaved = false;
		WriteFlagFile();
	}
	
	// Creates and displays the rename dialog
	private void showRenameDialog(final int layoutId) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Rename");

		final ButtonInfo button = buttonMap.get(layoutId);
		
		final TextView nameText = (TextView)parentLayout.findViewById(button.getNameId());
		String initialName = nameText.getText().toString();
		
		final EditText editText = new EditText(getActivity());
		editText.setText(initialName);
		editText.selectAll();
		
		builder.setView(editText);
		
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
            	
            	// Obtaining new name
                String newName = editText.getText().toString();
                
                // Updating buttonView 
                nameText.setText(newName);
                
                // Updating buttonMap
                button.setName(newName);
                
                // Writing updated configuration to file
                WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
                
                // Modifications occurred. Setting currentConfigIsSaved flag to false
        		currentConfigIsSaved = false;
        		WriteFlagFile();
            }
        });
        
		// Opening soft keyboard
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        
        dialog.show();
	}
	
	private String newShortcutAction;
	private String newShortcutName;
	private boolean[] checkedItems;
	Vector<String> keysVector;
	Vector<String> namesVector;
	
	// Creates and displays the action picker dialog
	private void showActionPickerDialog(final int layoutId) {
		
		// Creating dialog and reading action
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Pick an action");
		
		final ButtonInfo button = buttonMap.get(layoutId);
		String buttonAction = button.getAction();
		
		// Checking if the name was set by the user
		boolean nameNotSet = false;
		if( button.getActionName().equals(button.getName()) || button.getName().equals("<No Name>") ) {
			nameNotSet = true;
		}
		final boolean nameNotSetFinal = nameNotSet;
		
		// Setting checkedItems and adding them to the vector
		String[] keys = buttonAction.split("[ +]+");
		keysVector = new Vector<String>();
		namesVector = new Vector<String>();
		checkedItems = new boolean[keyCodesVector.size()];

		int index;
		for(String key : keys) {
			index = keyCodesVector.indexOf(key);
			if( index != -1 ) {
				checkedItems[index] = true;
				keysVector.add(key);
				namesVector.add(keyNames[index]);
			}
		}
		
		// Adding MultiChoice item list
		builder.setMultiChoiceItems(keyNames, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// Updating item status
				checkedItems[which] = isChecked;
				
				// Appending/Removing to/from vector
				if( isChecked == true ) {
					keysVector.add(keyCodes[which]);
					namesVector.add(keyNames[which]);
				}
				else {
					keysVector.remove(keyCodes[which]);
					namesVector.remove(keyNames[which]);
				}
		    }
		});
		
        // Adding PositiveButton to dialog
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	// Appending keys to the newShortcutAction string
            	if( keysVector.size() == 0 ) {
            		newShortcutAction = "<No Action>";
            		newShortcutName = "<No Action>";
            	}
            	else {
	        		newShortcutAction = keysVector.firstElement();
	        		newShortcutName = namesVector.firstElement();
	        		Iterator<String> keysIt = keysVector.iterator();
	        		Iterator<String> namesIt = namesVector.iterator();
	        		if( keysIt.hasNext() ) {
	        			keysIt.next();
	        			namesIt.next();
	        		}
	            	while( keysIt.hasNext() ) {
	            		newShortcutAction += " + " + keysIt.next();
	            		newShortcutName += " + " + namesIt.next();
	            	}
            	}
            	
            	// Updating buttonView
            	// Checking if a name is defined. If not, the default action name is used
            	TextView nameView = (TextView)parentLayout.findViewById(button.getNameId());
            	if( nameNotSetFinal == true ) {
            		nameView.setText(newShortcutName);
            		button.setName(newShortcutName);
            	}
		    	TextView actionView = (TextView)parentLayout.findViewById(button.getActionId());
		    	actionView.setText(newShortcutName);
				// Updating buttonMap
		    	buttonMap.get(layoutId).setAction(newShortcutAction);
				buttonMap.get(layoutId).setActionName(newShortcutName);
		    	
				// DON'T KNOW IF THIS IS USEFUL, BUT KEEP IT. IT MAY BE RELATED WITH
				// THE ACTION PICKER DIALOG BEING DISPLAYED AFTER USER DRAWS A BUTTON
//		    	// Checking if the name must be set to the default one
//		    	if( setDefaultButtonNameFlag ) {
//		    		// Updating buttonView
//		    		nameView = (TextView)parentLayout.findViewById(button.getNameId());
//			    	nameView.setText(newShortcutName);
//			    	// Updating buttonMap
//			    	buttonMap.get(layoutId).setName(newShortcutName);
//		    		
//			    	// Resetting flag
//		    		setDefaultButtonNameFlag = false;
//		    	}
		    	
                // Writing updated configuration to file
                WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
                
                // Modifications occurred. Setting currentConfigIsSaved flag to false
        		currentConfigIsSaved = false;
        		WriteFlagFile();

            }
        });
        
		builder.show();
	}

	
// 	--- Map-related methods ---
	
	// Creates the map
	private void CreateMap() {
		
		Log.d("CREATE_MAP","CreateMap() called");
		
		idMatrix = new int[ShortcutControlFragment.cellsOnColumn][ShortcutControlFragment.cellsOnRow];
		availabilityMatrix = new boolean[ShortcutControlFragment.cellsOnColumn][ShortcutControlFragment.cellsOnRow];
		colorMatrix = new int[ShortcutControlFragment.cellsOnColumn][ShortcutControlFragment.cellsOnRow];
		
		int previousId = -1;
		
		for(int i=0; i < ShortcutControlFragment.cellsOnColumn; ++i) {
			
			RelativeLayout layout = new RelativeLayout(parentView.getContext());
//			layout.setBackgroundColor(Color.BLUE);
			layout.setId(ShortcutControlFragment.ROW_LAYOUT_ID_PREFIX + i);
			
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			if( previousId == -1 ) {
				layoutParams.leftMargin = ShortcutControlFragment.xEps;
				layoutParams.topMargin = ShortcutControlFragment.yEps;
	  		}
	  		else {
	  			layoutParams.addRule(RelativeLayout.BELOW, previousId);
	  			layoutParams.addRule(RelativeLayout.ALIGN_LEFT, previousId);
	  		}
			
			layout.setLayoutParams(layoutParams);
			
			PopulateRow(layout, i);
			
			parentLayout.addView(layout);

			previousId = layout.getId();
		}

		// Setting parent layout background color
		parentLayout.setBackgroundColor(MAP_CELL_COLOR);
		
		// Adding grid lines
		AddGridLines();
	}
	
	// Adds text views to each row (relative layout)
	private void PopulateRow(RelativeLayout layout, int rowIndex) {
		Log.d("POPULATE_ROW","PopulateRow() called");
		
		int previousId = -1;
		
		for(int i=0; i < ShortcutControlFragment.cellsOnRow; ++i) {
			
			TextView textView = new TextView(parentView.getContext());
			textView.setHeight(ShortcutControlFragment.realCellHeight);
			textView.setWidth(ShortcutControlFragment.realCellWidth);
			textView.setBackgroundColor(Color.WHITE);
			textView.setId(ShortcutControlFragment.CELL_VIEW_ID_PREFIX + ShortcutControlFragment.cellsOnRow*Integer.valueOf(rowIndex) + i);
//			textView.setText(String.valueOf(textView.getId()));

			// Adding TextView id to the idMatrix
			idMatrix[rowIndex][i] = textView.getId();
			availabilityMatrix[rowIndex][i] = true;
			colorMatrix[rowIndex][i] = MAP_CELL_COLOR;
			
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ShortcutControlFragment.realCellWidth, 
					ShortcutControlFragment.realCellHeight);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			
			if( previousId == -1 ) {
				layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	  		}
	  		else {
	  			layoutParams.addRule(RelativeLayout.RIGHT_OF, previousId);
	  		}
			
			textView.setLayoutParams(layoutParams);
			
			layout.addView(textView);
			
			previousId = textView.getId();
		}
		
	}
	
	// Computes startX, startY, height and width based on row/columnStart/Stop
	public static ButtonDimensions computeButtonParametersInPixels(int rowStart, int rowStop, int columnStart, int columnStop) {
		// Computing start point and size
		int startY = ShortcutControlFragment.realCellHeight * rowStart;
		int startX = ShortcutControlFragment.realCellWidth* columnStart;
		int height = ShortcutControlFragment.realCellHeight * (rowStop - rowStart + 1);
		int width = ShortcutControlFragment.realCellWidth * (columnStop - columnStart + 1);
		
		return new ButtonDimensions(startX, startY, height, width);
	}
	
	// Creates a TextView according to the ButtonInfo object
	private void CreateExistingButtonView(ButtonInfo button) {
		
		// Computing button dimensions
		ButtonDimensions dimensions = null;
		int rowStart = 0, rowStop = 0, columnStart = 0, columnStop = 0;
		
		// Checking orientation
		if(getActivity().getResources().getConfiguration().orientation == 1) {
			rowStart = button.getPRowStart();
			rowStop = button.getPRowStop();
			columnStart = button.getPColumnStart();
			columnStop = button.getPColumnStop();
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(rowStart, rowStop, columnStart, columnStop);
		}
		else if(getActivity().getResources().getConfiguration().orientation == 2) {
			rowStart = button.getLRowStart();
			rowStop = button.getLRowStop();
			columnStart = button.getLColumnStart();
			columnStop = button.getLColumnStop();
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(rowStart, rowStop, columnStart, columnStop);
		}
		
		int startX = dimensions.startX;
		int startY = dimensions.startY;
		int height = dimensions.heigth;
		int width = dimensions.width;
		
		String name = button.getName();
		String action = button.getAction();
		String actionName = button.getActionName();
		final int layoutId = button.getLayoutId();
		final int nameViewId = button.getNameId();
		final int actionViewId = button.getActionId();

		// Appending ids to vector
		buttonIdVector.add(layoutId);
		buttonIdVector.add(nameViewId);
		buttonIdVector.add(actionViewId);
		
		Log.d("CREATE_VIEW","startX: " + startX + ", startY: " + startY + ", width: " + width + ", " + height);
		
		// Changing color of current rectangle, updating colorMatrix and availabilityMatrix
		markRectAsUnavailable(rowStart, rowStop, columnStart, columnStop);
		
		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX + ShortcutControlFragment.xEps;
		params.topMargin = startY + ShortcutControlFragment.yEps;
		
		// Creating linear layout to hold the two text views
		final LinearLayout buttonLayout = CreateButtonLayout(name, action, actionName, height, width,
															layoutId, nameViewId, actionViewId);
		buttonLayout.setLayoutParams(params);
		
		buttonLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("BUTTON_LAYOUT_CLICKED","Button layout clicked. ID: " + layoutId);
				showRenameChangeActionDeleteDialog(layoutId);
			}
		});
		
		parentLayout.addView(buttonLayout);
	}
	
	// Creates a TextView of specified dimensions at the specified position and adds it to the buttonMap
	private void CreateNewButtonView(String name, String action, String actionName,
						int pRowStart, int pRowStop, int pColumnStart, int pColumnStop,
						int lRowStart, int lRowStop, int lColumnStart, int lColumnStop) {
		
		// Computing button dimensions
		ButtonDimensions dimensions = null;
		// Checking orientation
		if(getActivity().getResources().getConfiguration().orientation == 1) {
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(pRowStart, pRowStop, pColumnStart, pColumnStop);
		}
		else if(getActivity().getResources().getConfiguration().orientation == 2) {
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(lRowStart, lRowStop, lColumnStart, lColumnStop);
		}
		
		int startX = dimensions.startX;
		int startY = dimensions.startY;
		int height = dimensions.heigth;
		int width = dimensions.width;
		
		// Creating ids
		int id = ShortcutControlFragment.SHORTCUT_BUTTON_ID_PREFIX;
		while( buttonIdVector.contains(id) ) {
			++ id;
		}
		final int layoutId = id;
		final int nameViewId = id + ShortcutControlFragment.NAME_VIEW_ID_INCREMENT;
		final int actionViewId = id + ShortcutControlFragment.ACTION_VIEW_ID_INCREMENT;
		
		// Appending ids to vector
		buttonIdVector.add(layoutId);
		buttonIdVector.add(nameViewId);
		buttonIdVector.add(actionViewId);
		
		// Creating new ButtonInfo object and adding it to the map
		ButtonInfo newButton = new ButtonInfo(name, action, actionName, layoutId, nameViewId, actionViewId,
				pRowStart, pRowStop, pColumnStart, pColumnStop, lRowStart, lRowStop, lColumnStart, lColumnStop);
		buttonMap.put(layoutId, newButton);
		
		Log.d("CREATE_VIEW","startX: " + startX + ", startY: " + startY + ", width: " + width + ", " + height);
		
		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX + ShortcutControlFragment.xEps;
		params.topMargin = startY + ShortcutControlFragment.yEps;
		
		// Creating linear layout to hold the two text views
		final LinearLayout buttonLayout = CreateButtonLayout(name, action, actionName, height, width, 
													layoutId, nameViewId, actionViewId);
		buttonLayout.setLayoutParams(params);
		
		buttonLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("BUTTON_LAYOUT_CLICKED","Button layout clicked. ID: " + layoutId);
				showRenameChangeActionDeleteDialog(layoutId);
			}
		});
		
		parentLayout.addView(buttonLayout);
		
		// USE THIS IF YOU WANT THE ACTION PICKER DIALOG TO BE DISPLAYED AFTER USER DRAWS A BUTTON
		// Displaying action picker dialog
//		setDefaultButtonNameFlag = true;
//		showActionPickerDialog(layoutId);
		
		// Modifications occurred. Setting currentConfigIsSaved flag to false
		currentConfigIsSaved = false;
		WriteFlagFile();
	}
	
	// Creates the linear layout of the size of the button that will hold the nameView and actionView
	private LinearLayout CreateButtonLayout(String name, String action, String actionName, int height, int width,
			int layoutId, int nameViewId, int actionViewId) {
		
		final LinearLayout buttonLayout = new LinearLayout(parentView.getContext());
//		buttonLayout.setBackgroundColor(MAP_CELL_COLOR);
		buttonLayout.setId(layoutId);
		buttonLayout.setOrientation(LinearLayout.VERTICAL);
		buttonLayout.setPadding(5, 5, 5, 5);
		
		LinearLayout.LayoutParams secondLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		
		LinearLayout secondLayout = new LinearLayout(parentView.getContext());
		secondLayout.setBackgroundColor(Color.DKGRAY);
		secondLayout.setOrientation(LinearLayout.VERTICAL);
		secondLayout.setPadding(5, 5, 5, 5);
		secondLayout.setLayoutParams(secondLayoutParams);
		
		// Creating parameters for the text views
		LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
		textViewParams.weight = 1;
		
		// Creating name text view
		TextView nameText = CreateNameView(buttonLayout, name, nameViewId);
		nameText.setLayoutParams(textViewParams);
		
		// Creating action text view
		TextView actionText = CreateActionView(buttonLayout, action, actionName, actionViewId);
		actionText.setLayoutParams(textViewParams);
		
		int smallestDim = height < width ? height : width;
		actionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallestDim/SELECTION_TEXT_SIZE_RATIO);
		nameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallestDim/SELECTION_TEXT_SIZE_RATIO);
		
		// Creating separator
		TextView separator = CreateHorizontalSeparator(buttonLayout, 20, 1, Color.LTGRAY);
		
		secondLayout.addView(nameText);
		secondLayout.addView(separator);
		secondLayout.addView(actionText);
		
		buttonLayout.addView(secondLayout);
		
		return buttonLayout;
	}
	
	// Creates the separator between the nameView and the actionView
	public static TextView CreateHorizontalSeparator(LinearLayout buttonLayout, int margin, int size, int color) {
		
		TextView separator = new TextView(buttonLayout.getContext());
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, size));
		separator.setBackgroundColor(color);
		params.setMargins(margin,0,margin,0);
		separator.setLayoutParams(params);
		
		return separator;
	}
	
	// Creates the separator between the nameView and the actionView
	public static TextView CreateVerticalSeparator(LinearLayout buttonLayout, int margin, int size, int color) {
		
		TextView separator = new TextView(buttonLayout.getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(size, LayoutParams.MATCH_PARENT));
		separator.setBackgroundColor(color);
		params.setMargins(0,margin,0,margin);
		separator.setLayoutParams(params);
		
		return separator;
	}
	
	// Creates the text view that holds the name for a buttonLayout
	private TextView CreateNameView(LinearLayout buttonLayout, String name, int nameViewId) {
		
		TextView nameText = new TextView(buttonLayout.getContext());
		nameText.setBackgroundColor(Color.DKGRAY);
		nameText.setTextColor(Color.WHITE);
		nameText.setText(name);
		nameText.setId(nameViewId);
		nameText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		
		return nameText;
	}
	
	// Creates the TextView that holds the action for a buttonLayout
	private TextView CreateActionView(LinearLayout buttonLayout, String action, String actionName, int actionViewId) {
		
		TextView actionText = new TextView(buttonLayout.getContext());
		actionText.setBackgroundColor(Color.DKGRAY);
		actionText.setTextColor(Color.WHITE);
		actionText.setText(actionName);
		actionText.setId(actionViewId);
		actionText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		
		return actionText;
	}
	
	// Creates the grid lines
	private void AddGridLines() {
		
		for(int i=1; i<ShortcutControlFragment.cellsOnColumn; i++) {
		
			// Creating horizontal separator
			RelativeLayout.LayoutParams separatorParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
			separatorParams.topMargin = ShortcutControlFragment.realCellHeight * i + ShortcutControlFragment.yEps;
			
			TextView separator = new TextView(parentLayout.getContext());
			separator.setBackgroundColor(Color.LTGRAY);
			separator.setLayoutParams(separatorParams);
			
			parentLayout.addView(separator);
		}
		
		for(int i=1; i<ShortcutControlFragment.cellsOnRow; i++) {
			
			// Creating horizontal separator
			RelativeLayout.LayoutParams separatorParams = new RelativeLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
			separatorParams.leftMargin = ShortcutControlFragment.realCellWidth * i + ShortcutControlFragment.xEps;
			
			TextView separator = new TextView(parentLayout.getContext());
			separator.setBackgroundColor(Color.LTGRAY);
			separator.setLayoutParams(separatorParams);
			
			parentLayout.addView(separator);
		}
		
	}
	
	// For each button in the map creates and displays its corresponding view
	private void DisplayMapConfig() {
		
		// Clearing id vector
		buttonIdVector.clear();
		
		// Displaying each button
		ButtonInfo button;
		Iterator<Integer> it = buttonMap.keySet().iterator();
		while( it.hasNext() ) {
		
			int key = it.next();
		    button = buttonMap.get(key);
			
		    CreateExistingButtonView(button);
		}
		
	}
	
	// Clears the map
	private void ClearMap() {
		
		// Resetting availability matrix and color matrix
		for(int i=0; i<ShortcutControlFragment.cellsOnColumn; ++i) {
			for(int j=0; j<ShortcutControlFragment.cellsOnRow; ++j) {
				availabilityMatrix[i][j] = true;
				colorMatrix[i][j] = MAP_CELL_COLOR;
			}
		}
		
		// Restoring color according to color matrix
		RestoreColor();
		
		// Removing all button views
		LinearLayout buttonLayout;
		Iterator<Integer> it = buttonMap.keySet().iterator();
		while( it.hasNext() ) {
		
			int key = it.next();
			buttonLayout = (LinearLayout) parentLayout.findViewById(key);
			
			parentLayout.removeView(buttonLayout);
//			parentLayout.removeAllViews();
		}
		
		// Clearing the button map and the button vector
		buttonMap.clear();
		buttonIdVector.clear();
	}
	
	// Restores the color of all views to the color save in the colorMatrix
	private void RestoreColor() {
		TextView textView;
		for(int i=0; i<ShortcutControlFragment.cellsOnColumn; ++i) {
			for(int j=0; j<ShortcutControlFragment.cellsOnRow; ++j) {
				textView = (TextView)parentView.findViewById(idMatrix[i][j]);
				textView.setBackgroundColor(colorMatrix[i][j]);
			}
		}
	}
	

//	--- Touch selection mechanism ---
	
	// Method that sets the OnTouchListener to the parent view
	private void SetOnTouchListener() {
		
		parentView.setOnTouchListener( new View.OnTouchListener() {
            @Override
			public boolean onTouch(View v, MotionEvent ev) {

            	float currentX = ev.getX();
        		float currentY = ev.getY();
        		// No need to subtract anything because listener is no set on the getActivity() but on parentView in the fragment
//        		float currentY = ev.getY() - parentViewStartPoint;
        		
        		Log.d("POSITION", currentX + "," + currentY);
        		
        		// Resetting the color of all views their previous color
        		RestoreColor();
        		
        		// Analyzing touch state
        		if( ev.getAction() == MotionEvent.ACTION_DOWN ) {
        			Log.d("ON_TOUCH","ACTION_DOWN");
        			
        			onTouchActionDown(currentX, currentY);
        		}
        		else if( ev.getAction() == MotionEvent.ACTION_MOVE) {
        			Log.d("ON_TOUCH","ACTION_MOVE");
        			
        			onTouchActionMove(currentX, currentY);
        		}
        		else if( ev.getAction() == MotionEvent.ACTION_UP ) {
        			Log.d("ON_TOUCH","ACTION_UP");
        			
        			onTouchActionUp(currentX, currentY);
        		}
        		
        		return true;
            }
		});
	}
	
	// Touch: action down mechanism (First selection point)
	private void onTouchActionDown(float currentX, float currentY) {
		
		// Initializing start coordinates and indexes
		startX = currentX;
		startY = currentY;
		startRowIndex = getRowIndex(currentY);
		startColumnIndex = getColumnIndex(currentX);
		
		TextView textView;
		if( availabilityMatrix[startRowIndex][startColumnIndex] == true ) {
			textView = (TextView)parentView.findViewById(idMatrix[startRowIndex][startColumnIndex]);
			textView.setBackgroundColor(Color.DKGRAY);
		}
		
	}
	
	// Touch: action move mechanism (computing rectangle)
	private void onTouchActionMove(float currentX, float currentY) {
		
		// Obtaining indexes of current view
		int currentRowIndex = getRowIndex(currentY);
		int currentColumnIndex = getColumnIndex(currentX);
		
		// Setting start/end indexes in the correct order
		int rowStart = startRowIndex;
		int rowStop = currentRowIndex;
		if( startRowIndex > currentRowIndex ) {
			rowStart = currentRowIndex;
			rowStop = startRowIndex;
		}
		int columnStart = startColumnIndex;
		int columnStop = currentColumnIndex;
		if( startColumnIndex > currentColumnIndex ) {
			columnStart = currentColumnIndex;
			columnStop = startColumnIndex;
		}
		
		// Changing color of current rectangle
		TextView textView;
		for(int i=rowStart; i<=rowStop; ++i) {
			for(int j=columnStart; j<=columnStop; ++j) {
				textView = (TextView)parentView.findViewById(idMatrix[i][j]);
				textView.setBackgroundColor(Color.GRAY);
			}
		}
		
	}

	// Touch: action up mechanism (saving selection and creating button)
	private void onTouchActionUp(float currentX, float currentY) {
	
		// The button is selected		
		boolean validFlag = true;
		
		// Obtaining indexes of current view
		int currentRowIndex = getRowIndex(currentY);
		int currentColumnIndex = getColumnIndex(currentX);
		
		// Setting start/end indexes in the correct order
		int rowStart = startRowIndex;
		int rowStop = currentRowIndex;
		if( startRowIndex > currentRowIndex ) {
			rowStart = currentRowIndex;
			rowStop = startRowIndex;
		}
		int columnStart = startColumnIndex;
		int columnStop = currentColumnIndex;
		if( startColumnIndex > currentColumnIndex ) {
			columnStart = currentColumnIndex;
			columnStop = startColumnIndex;
		}

		// Checking validity
		for(int i=rowStart; i<=rowStop; ++i) {
			for(int j=columnStart; j<=columnStop; ++j) {
				if( availabilityMatrix[i][j] == false ) {
					validFlag = false;
					break;
				}
			}
		}

		// If selection is invalid cancel it
		if(validFlag == false) {
			return;
		}
		
		int pRowStart = 0, pRowStop = 0, pColumnStart = 0, pColumnStop = 0, lRowStart = 0, lRowStop = 0, lColumnStart = 0, lColumnStop = 0;
		ButtonParameters parameters;
		
		// Checking orientation
		if(getActivity().getResources().getConfiguration().orientation == 1) {
			pRowStart = rowStart;
			pRowStop = rowStop;
			pColumnStart = columnStart;
			pColumnStop = columnStop;
			parameters = ComputeLandscapeParams(pRowStart, pRowStop, pColumnStart, pColumnStop);
			
			lRowStart = parameters.rowStart;
			lRowStop = parameters.rowStop;
			lColumnStart = parameters.columnStart;
			lColumnStop = parameters.columnStop;
		}
		else if(getActivity().getResources().getConfiguration().orientation == 2) {
			lRowStart = rowStart;
			lRowStop = rowStop;
			lColumnStart = columnStart;
			lColumnStop = columnStop;
			parameters = ComputePortraitParams(lRowStart, lRowStop, lColumnStart, lColumnStop);
			
			pRowStart = parameters.rowStart;
			pRowStop = parameters.rowStop;
			pColumnStart = parameters.columnStart;
			pColumnStop = parameters.columnStop;
		}
		
		// Changing color of current rectangle, updating colorMatrix and availabilityMatrix
		markRectAsUnavailable(rowStart, rowStop, columnStart, columnStop);
		
		// Creating a text view as an image of the future button
		// and adding the new button to the buttonMap
		CreateNewButtonView("<No Name>", "<No Action>", "<No Action>", pRowStart, pRowStop, pColumnStart, pColumnStop,
										lRowStart, lRowStop, lColumnStart, lColumnStop);
		
		// Writing current configuration to currentConfigFile
		WriteCurrentConfig(ShortcutControlFragment.currentConfigFile);
	}
	
	// Computes the landscape orientation button parameters
	private ButtonParameters ComputeLandscapeParams(int pRowStart, int pRowStop, int pColumnStart, int pColumnStop) {
		int lRowStart, lRowStop, lColumnStart, lColumnStop;
		
		lRowStart = ShortcutControlFragment.cellsOnRow - pColumnStart - (pColumnStop - pColumnStart + 1);
		lRowStop = lRowStart + (pColumnStop - pColumnStart + 1) - 1;
		lColumnStart = pRowStart;
		lColumnStop = pRowStop;
		
		return new ButtonParameters(lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	
	// Computes the landscape orientation button parameters
	private ButtonParameters ComputePortraitParams(int lRowStart, int lRowStop, int lColumnStart, int lColumnStop) {
		int pRowStart, pRowStop, pColumnStart, pColumnStop;
		
		pRowStart = lColumnStart;
		pRowStop = lColumnStop;
		pColumnStart = ShortcutControlFragment.cellsOnColumn - lRowStart - (lRowStop - lRowStart + 1);
		pColumnStop = pColumnStart + (lRowStop - lRowStart + 1) - 1;
		
		return new ButtonParameters(pRowStart, pRowStop, pColumnStart, pColumnStop);
	}
	
	// Marks the cells contained in the give rectangle as unavailable
	private void markRectAsUnavailable(int rowStart, int rowStop, int columnStart, int columnStop) {
//		TextView textView;
		for(int i=rowStart; i<=rowStop; ++i) {
			for(int j=columnStart; j<=columnStop; ++j) {
//				textView = (TextView)parentView.findViewById(idMatrix[i][j]);
//				textView.setBackgroundColor(Color.RED);
				availabilityMatrix[i][j] = false;
//				colorMatrix[i][j] = Color.RED;
			}
		}
	}
	
	// Computes the row index
	private int getRowIndex(float Y) {
		Y -= ShortcutControlFragment.yEps;
		
		int idx = (int) Y/ShortcutControlFragment.realCellHeight;
		if( idx >= ShortcutControlFragment.cellsOnColumn ) {
			return ShortcutControlFragment.cellsOnColumn-1;
		}
		else {
			return idx;
		}
	}
	
	// Computes the column index
	private int getColumnIndex(float X) {
		X -= ShortcutControlFragment.xEps;
		
		int idx = (int) X/ShortcutControlFragment.realCellWidth;
		if( idx >= ShortcutControlFragment.cellsOnRow ) {
			return ShortcutControlFragment.cellsOnRow-1;
		}
		else {
			return idx;
		}
	}
	
//	--- TEST methods ---
	
	// Test method
	private void ComputeSizesTest() {
		
		final TextView text1 = new TextView(parentView.getContext());
		text1.setHeight(500);
		text1.setWidth(300);
		text1.setBackgroundColor(Color.LTGRAY);
		text1.setText("Text");
		
		final TextView text2 = new TextView(parentView.getContext());
		text2.setBackgroundColor(Color.BLACK);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		text2.setLayoutParams(params);
		
		parentLayout.addView(text2);
		parentLayout.addView(text1);
		
		text1.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		       	int a = text1.getRootView().getHeight();
		       	int d = text1.getRootView().getWidth();
		        int b = text1.getHeight();
		        int c = text1.getWidth();

		        Log.d("	text1.getRootView(): (height, width)", a + "," + d);
		        Log.d("	text1: (height, width)", b + "," + c);
		     }
		});
		
		text2.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		       	int a = text2.getRootView().getHeight();
		       	int d = text2.getRootView().getWidth();
		        int b = text2.getHeight();
		        int c = text2.getWidth();

		        Log.d("	text2.getRootView(): (height, width)", a + "," + d);
		        Log.d("	text2: (height, width)", b + "," + c);
		     }
		});
		
		int a = text2.getRootView().getHeight();
       	int d = text2.getRootView().getWidth();
        int b = text2.getHeight();
        int c = text2.getWidth();

        Log.d("	OUTSIDE: text2.getRootView(): (height, width)", a + "," + d);
        Log.d("	OUTSIDE: text2: (height, width)", b + "," + c);
        
        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		    @Override
		    public void onGlobalLayout() {
		       	int a = parentLayout.getRootView().getHeight();
		       	int d = parentLayout.getRootView().getWidth();
		        int b = parentLayout.getHeight();
		        int c = parentLayout.getWidth();

		        Log.d("	parentLayout.getRootView(): (height, width)", a + "," + d);
		        Log.d("	parentLayout: (height, width)", b + "," + c);
		     }
		});
		
	}
	
}
