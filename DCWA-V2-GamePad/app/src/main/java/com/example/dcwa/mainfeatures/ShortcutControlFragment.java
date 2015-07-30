package com.example.dcwa.mainfeatures;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import com.example.dcwa.auxclasses.ButtonDimensions;
import com.example.dcwa.auxclasses.ButtonInfo;
import com.example.dcwa.auxclasses.DisplayOrientation;
import com.example.dcwa.auxclasses.StringAction;
import com.example.dcwa.networking.NetworkingThread;
import com.example.dcwa.networking.PostRequestRunnable;
import com.example.dcwa.settings.SelectionMapFragment;
import com.example.dcwa.settings.SettingsActivity;
import com.example.desktopcontrolwithandroid.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;

public class ShortcutControlFragment extends Fragment {

	private boolean configurationLoadedFlag = false;
	OnGlobalLayoutListener shortcutLayoutListener;
	
	private final int SHORTCUT_TEXT_SIZE_RATIO = 5;
	
	public final int KMP_LAYOUT_ID_PREFIX = 5000;
	public static final int SHORTCUT_BUTTON_ID_PREFIX = 6000;
	public static final int NAME_VIEW_ID_INCREMENT = 1000;
	public static final int ACTION_VIEW_ID_INCREMENT = 2000;
	public static final int ROW_LAYOUT_ID_PREFIX = 1000;
	public static final int CELL_VIEW_ID_PREFIX = 2000;
	
	public static int parentHeight;
	public static int parentWidth;
	public static int parentViewStartPoint;
	public static DisplayOrientation orientation;

	public static final int MAX_CELL_HEIGHT = 100;
	public static final int MAX_CELL_WIDTH = 100;
	
	public static int realCellHeight, realCellWidth;
	public static int cellsOnRow = 12;
	public static int cellsOnColumn = 20;
	public static int xEps, yEps;
	
	public static String currentConfigFile;
	public static String KMPConfigFile;
	public static String VLCConfigFile;
	
	View parentView;
	RelativeLayout parentLayout;
	
	private HashMap<Integer,ButtonInfo> buttonMap;
	
	private NetworkingThread shortcutThread;
	private PostRequestRunnable inputActionRunnable;
	
	private StringAction stringAction;
	
	public ShortcutControlFragment() {}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_shortcuts));
    	
//    	Log.d("SHORTCUT_CONTROL_FRAGMENT","ON_CREATE_VIEW called");
    	
        // Inflate the layout for this fragment
    	setHasOptionsMenu(true);
        parentView = inflater.inflate(R.layout.fragment_shortcut_control, container, false);
        // Getting the parent layout
    	parentLayout = (RelativeLayout)parentView.findViewById(R.id.config_buttons_layout);
    	
        // Starting networking thread
    	shortcutThread = new NetworkingThread();
    	shortcutThread.start();
		inputActionRunnable = new PostRequestRunnable(getActivity(), shortcutThread);
		stringAction = new StringAction(MainActivity.inputUri);
        
//		// DEBUG: Checking thread state
//		boolean threadAlive = keyboardThread.isAlive();
//		Thread.State state = keyboardThread.getState();
//		long id = keyboardThread.getId();
//		Log.d("ON_CREATE_KEYBOARD","THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
		
        // Initializing the buttonMap
     	buttonMap = new HashMap<Integer, ButtonInfo>();
        
		// Reading configuration file names
        currentConfigFile = getString(R.string.current_config_file);
        KMPConfigFile = getString(R.string.kmp_config_file);
        VLCConfigFile = getString(R.string.vlc_config_file);
     	
        shortcutLayoutListener = new OnGlobalLayoutListener() {
			@Override
		    public void onGlobalLayout() {
		    	
		        if( configurationLoadedFlag == true && MainActivity.mainConfigurationChanged == false ) {
		        	return;
		        }
				
		    	// Computing view dimensions
		       	parentHeight = parentLayout.getHeight();
		        parentWidth = parentLayout.getWidth();
		        parentViewStartPoint = parentLayout.getRootView().getHeight() - parentHeight;
		        
		        Log.d("SHORTCUT_CONTROL_FRAGMENT","ON_GLOBAL_LAYOUT_LISTENER called");
		        Log.d("	parentLayout: (height, width, start)", parentHeight + "," + parentWidth + "," + parentViewStartPoint);
		        
		        // Setting configurationLoadedFlag to true
		        configurationLoadedFlag = true;
		        MainActivity.mainConfigurationChanged = false;
		        
		        // Removing all buttons
		        parentLayout.removeAllViews();
		        
		        // Calculating map parameters
		        SettingsActivity.ComputeMapParamteters();
			        
		        // Loading and displaying startup configuration
		        LoadAndDisplayConfiguration();
		    }
		};
		
        return parentView;
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        
//    	Log.d("SHORTCUT_CONTROL_FRAGMENT","ON_RESUME called");
    	
    	// Telling the softInputMode NOT to resize the screen contents
    	getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
    	
        // Removing all buttons
        parentLayout.removeAllViews();
        
        // Calculating map parameters
        SettingsActivity.ComputeMapParamteters();
	        
        // Loading and displaying startup configuration
        LoadAndDisplayConfiguration();
    	
        // Resetting configurationLoadedFlag
        configurationLoadedFlag = false;
        
    	// Adding OnGlobalLayoutListener
    	parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(shortcutLayoutListener);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
//    	Log.d("SHORTCUT_CONTROL_FRAGMENT","ON_PAUSE called");
    	
    	// Clearing thread message queue
    	shortcutThread.RemoveHandlerCallbacksAndMessages();
    	
        // Removing OnGlobalLayoutListener
        parentLayout.getViewTreeObserver().removeGlobalOnLayoutListener(shortcutLayoutListener);
    }
    
    @Override
    public void onDestroy () {
    	super.onDestroy();
    	
//    	Log.d("SHORTCUT_CONTROL_FRAGMENT","ON_DESTROY called");
    	
    	// Stopping thread
    	shortcutThread.RemoveHandlerCallbacksAndMessages();
    	shortcutThread.Stop();
    	
//    	// TODO: This is blocking the UI until the thread finishes the current request. Think about it
//    	Log.d("SHORTCUT",">>> join() called on shortcutThread");
//    	
//		// Waiting for thread to finish
//		try {
//			shortcutThread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		boolean threadAlive = shortcutThread.isAlive();
//     	Thread.State state = shortcutThread.getState();
//     	long id = shortcutThread.getId();
//     	Log.d("ON_DESTROY_SHORTCUT","shortcutThread: THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
     	
     	// Checking thread state
    	Runnable checkThread = new Runnable() {
			@Override
			public void run() {
				boolean threadAlive = shortcutThread.isAlive();
				Thread.State state = shortcutThread.getState();
				long id = shortcutThread.getId();
				Log.d("ON_DESTROY_SHORTCUT","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
			}
		};
		new Handler().postDelayed(checkThread, PostRequestRunnable.CONNECTION_LOST_REQUEST_TIMEOUT);
    	
//    	// DEBUG: Checking thread state
//    	// Checking thread state with 6 seconds delay so that the thread has time to finish any attempt to send a request
//    	Handler handler = new Handler();
//        handler.postDelayed(new Runnable() { 
//             public void run() {
//            	boolean threadAlive = keyboardThread.isAlive();
//             	Thread.State state = keyboardThread.getState();
//             	long id = keyboardThread.getId();
//             	Log.d("ON_DESTROY_KEYBOARD","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
//             }
//        }, 4000);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	int id = item.getItemId();
        switch (id) {
        case R.id.switch_to_keyboard_action_bar:
        	switchToKeyboardControl();
        	return true;
        case R.id.switch_to_pointer_action_bar:
        	switchToPointerControl();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

// 	--- Switch between fragments methods

    public void switchToKeyboardControl() {
    	// Create the new fragment
    	KeyboardControlFragment newFragment = new KeyboardControlFragment();
    	
    	// Create a new the transaction
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
    	// Set transaction animations
    	transaction.setCustomAnimations(R.anim.animation_enter_from_left, R.anim.animation_exit_to_right);
    	
    	// Replace the content of the fragment_container view with this fragment
    	transaction.replace(R.id.fragment_container, newFragment);
    	
    	transaction.commit();
    }
    
    public void switchToPointerControl() {
    	// Create the new fragment
    	PointerControlFragment newFragment = new PointerControlFragment();
    	
    	// Create a new the transaction
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
    	// Set transaction animations
    	transaction.setCustomAnimations(R.anim.animation_enter_from_right, R.anim.animation_exit_to_left);
    	
    	// Replace the content of the fragment_container view with this fragment
    	transaction.replace(R.id.fragment_container, newFragment);

    	// Commit transaction
    	transaction.commit();
    }
    
    
//	--- Startup related methods ---
    
    // Loads and display the startup configuration
    private void LoadAndDisplayConfiguration() {
    	
        // Checking current configuration file and loading configuration in the buttonMap
		if( checkCurrentConfigFile() == true ) {
	     	// Reading the current configuration
	     	ReadButtonConfig(currentConfigFile);
		}
		else {
			// Creating KMP configuration and populating buttonMap
			CreatePlayerConfig("RETURN", "ENTER", "LCONTROL + VK_Z", "L_CONTROL + Z", "VK_M", "M", "UP", "UP",
					"DOWN", "DOWN","LEFT", "LEFT", "RIGHT", "RIGHT", "SPACE", "SPACE");
			// Writing the KMP configuration to KMPconfigFile and to currentConfigFile
			WriteCurrentConfig(KMPConfigFile);
			
			// Creating VLC configuration and populating buttonMap
			CreatePlayerConfig("VK_F", "F", "VK_S", "S", "VK_M", "M", "LCONTROL + UP", "L_CONTROL + UP", "LCONTROL + DOWN", "L_CONTROL + DOWN",
					"LMENU + LEFT", "L_ALT + LEFT", "LMENU + RIGHT", "L_ALT + RIGHT", "SPACE", "SPACE");
			// Writing the VLC configuration to VLCconfigFile and to currentConfigFile
			WriteCurrentConfig(VLCConfigFile);
			
//			// Reading the VLC configuration
//			ReadButtonConfig(VLCConfigFile);
		}
		// Displaying the configuration loaded in the map
     	DisplayMapConfig();
    }
    
    
//	--- Shortcut configurations related methods ---
    
	// Checks whether the currentConfigFile exists (returns true if it exists)
	public boolean checkCurrentConfigFile() {

		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(currentConfigFile);
			inputStream.close();
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			Log.d("CHECK_CURRENT_CONFIG_FILE", currentConfigFile + " NOT found");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d("CHECK_CURRENT_CONFIG_FILE", currentConfigFile + " exists");
		return true;
	}
    
	// Creates the KMP configuration and populates the buttonMap with it
	private void CreatePlayerConfig(String actionFullScreen, String nameFullScreen, String actionStop, String nameStop,
				String actionMute, String nameMute, String actionVolumeUp, String nameVolumeUp,
				String actionVolumeDown, String nameVolumeDown, String actionStepBack, String nameStepBack,
				String actionStepForward, String nameStepForward, String actionPlayPause, String namePlayPause) {
	
		ButtonInfo button;
		int layoutId = KMP_LAYOUT_ID_PREFIX;
		
		// Full Screen button
		button = CreateFullScreenButton(layoutId, nameFullScreen, actionFullScreen);
		buttonMap.put(layoutId, button);
		// Stop button
		layoutId += 10;
		button = CreateStopButton(layoutId, nameStop, actionStop);
		buttonMap.put(layoutId, button);
		// Mute button
		layoutId += 10;
		button = CreateMuteButton(layoutId, nameMute, actionMute);
		buttonMap.put(layoutId, button);
		// Volume Up button
		layoutId += 10;
		button = CreateVolumeUpButton(layoutId, nameVolumeUp, actionVolumeUp);
		buttonMap.put(layoutId, button);
		// Volume Down button
		layoutId += 10;
		button = CreateVolumeDownButton(layoutId, nameVolumeDown, actionVolumeDown);
		buttonMap.put(layoutId, button);
		// Step Back button
		layoutId += 10;
		button = CreateStepBackButton(layoutId, nameStepBack, actionStepBack);
		buttonMap.put(layoutId, button);
		// Step Forward button
		layoutId += 10;
		button = CreateStepForwardButton(layoutId, nameStepForward, actionStepForward);
		buttonMap.put(layoutId, button);
		// Play/Pause button
		layoutId += 10;
		button = CreatePlayPauseButton(layoutId, namePlayPause, actionPlayPause);
		buttonMap.put(button.getLayoutId(), button);
	}
	
	// Creates Full Screen button 
	private ButtonInfo CreateFullScreenButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;
		
		int pRowStart = 0;
		int pRowStop = 3;
		int pColumnStart = 0;
		int pColumnStop = 7;
		int lRowStart = 4;
		int lRowStop = 11;
		int lColumnStart = 0;
		int lColumnStop = 3;
				
		return new ButtonInfo("Full Screen", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Stop button
	private ButtonInfo CreateStopButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;
		
		int pRowStart = 4;
		int pRowStop = 7;
		int pColumnStart = 0;
		int pColumnStop = 3;
		int lRowStart = 8;
		int lRowStop = 11;
		int lColumnStart = 4;
		int lColumnStop = 7;
				
		return new ButtonInfo("Stop", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Mute button
	private ButtonInfo CreateMuteButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 4;
		int pRowStop = 7;
		int pColumnStart = 4;
		int pColumnStop = 7;
		int lRowStart = 4;
		int lRowStop = 7;
		int lColumnStart = 4;
		int lColumnStop = 7;
				
		return new ButtonInfo("Mute", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Volume Up button
	private ButtonInfo CreateVolumeUpButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 0;
		int pRowStop = 3;
		int pColumnStart = 8;
		int pColumnStop = 11;
		int lRowStart = 0;
		int lRowStop = 3;
		int lColumnStart = 0;
		int lColumnStop = 3;
			
		return new ButtonInfo("Volume Up", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Volume Down button
	private ButtonInfo CreateVolumeDownButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 4;
		int pRowStop = 7;
		int pColumnStart = 8;
		int pColumnStop = 11;
		int lRowStart = 0;
		int lRowStop = 3;
		int lColumnStart = 4;
		int lColumnStop = 7;
			
		return new ButtonInfo("Volume Down", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Step Back button
	private ButtonInfo CreateStepBackButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 10;
		int pRowStop = 13;
		int pColumnStart = 0;
		int pColumnStop = 5;
		int lRowStart = 6;
		int lRowStop = 11;
		int lColumnStart = 10;
		int lColumnStop = 13;
		
		return new ButtonInfo("Step Back", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Step Back button
	private ButtonInfo CreateStepForwardButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 10;
		int pRowStop = 13;
		int pColumnStart = 6;
		int pColumnStop = 11;
		int lRowStart = 0;
		int lRowStop = 5;
		int lColumnStart = 10;
		int lColumnStop = 13;
			
		return new ButtonInfo("Step Forward", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
	}
	// Creates Play/Pause button
	private ButtonInfo CreatePlayPauseButton(int layoutId, String actionName, String action) {
		
		int nameId = layoutId + 1;
		int actionId = layoutId + 2;

		int pRowStart = 14;
		int pRowStop = 19;
		int pColumnStart = 0;
		int pColumnStop = 11;
		int lRowStart = 0;
		int lRowStop = 11;
		int lColumnStart = 14;
		int lColumnStop = 19;
		
		return new ButtonInfo("Play/Pause", action, actionName, layoutId, nameId, actionId,
				pRowStart, pRowStop, pColumnStart, pColumnStop,
				lRowStart, lRowStop, lColumnStart, lColumnStop);
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
//			ClearMap();
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
			
//			buttonIdVector.add(layoutId);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// For each button in the map creates and displays its corresponding view
	private void DisplayMapConfig() {
		
		// Displaying each button
		ButtonInfo button;
		Iterator<Integer> it = buttonMap.keySet().iterator();
		while( it.hasNext() ) {
		
			int key = it.next();
		    button = buttonMap.get(key);
			
		    CreateButton(button);
		}
	}
	
	// Creates a Button according to the ButtonInfo object
	private void CreateButton(ButtonInfo button) {
		
		// Computing button dimensions
		ButtonDimensions dimensions = null;
		
		// Checking orientation
		if(getActivity().getResources().getConfiguration().orientation == 1) {
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(
					button.getPRowStart(), button.getPRowStop(), button.getPColumnStart(), button.getPColumnStop());
		}
		else if(getActivity().getResources().getConfiguration().orientation == 2) {
			dimensions = SelectionMapFragment.computeButtonParametersInPixels(
					button.getLRowStart(), button.getLRowStop(), button.getLColumnStart(), button.getLColumnStop());
		}
		
		int startX = dimensions.startX;
		int startY = dimensions.startY;
		int height = dimensions.heigth;
		int width = dimensions.width;
		
		String name = button.getName();
		final String action = button.getAction();
		final int layoutId = button.getLayoutId();
		
		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX + xEps;
		params.topMargin = startY + yEps;
		
		// Creating linear layout to hold the two text views
		final Button newButton = new Button(parentView.getContext());
		newButton.setText(name);
		newButton.setId(layoutId);
		newButton.setLayoutParams(params);
		int smallestDim = height < width ? height : width;
		newButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallestDim/SHORTCUT_TEXT_SIZE_RATIO);
		
//		// Setting onClickListener
//        newButton.setOnClickListener( new View.OnClickListener() {
//        	public void onClick(View v) {
//
//        		stringAction.SetText(action);
//        		stringAction.SetPackageId(MainActivity.requestId);
//
//        		SendInputAction(stringAction);
//
//        		++ MainActivity.requestId;
//                MainActivity.requestId %= MainActivity.maxRequestId;
//        	}
//        });

		newButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				String prefix="";

				if( event.getAction() == MotionEvent.ACTION_DOWN) {
					prefix = "Press&";
					newButton.setPressed(true);
				}
				else if( event.getAction() == MotionEvent.ACTION_UP) {
					prefix = "Release&";
					newButton.setPressed(false);
				}
				else {
					return true;
				}

				stringAction.SetText(prefix + action);
				stringAction.SetPackageId(MainActivity.requestId);

				SendInputAction(stringAction);

				++ MainActivity.requestId;
				MainActivity.requestId %= MainActivity.maxRequestId;

				// TODO see what the return value means
				return true;
			}
		});

		parentLayout.addView(newButton);
	}
	
	
//	--- Send input action method ---
    
    // Method that sends input action to the server
    public void SendInputAction(StringAction inputAction) {
    	
    	inputActionRunnable.SetObject(inputAction);
    	shortcutThread.handler.post(inputActionRunnable);
    }
	
}
