package com.example.dcwa.mainfeatures;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.dcwa.auxclasses.StringAction;
import com.example.dcwa.networking.NetworkingThread;
import com.example.dcwa.networking.PostRequestRunnable;
import com.example.desktopcontrolwithandroid.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class KeyboardControlFragment extends Fragment {

	private NetworkingThread keyboardThread;
	private PostRequestRunnable inputActionRunnable;
	
	private StringAction stringAction;
	private StringAction inputAction;
	
	private EditText editText;
	private Button send_button;
	private CheckBox liveCheckBox;
	
	// Creating InputMethodManager for later use
	private InputMethodManager imm;
    
	// Defining the text watcher
	private final TextWatcher editTextWatcher = new TextWatcher() {
        
		String enter = "<Enter>";
		String backspace = "<Backspace>";
		String space = "<Space>";
		String previousText = "";
		
		boolean textUpdated = true;
		
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        	previousText = s.toString();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        public void afterTextChanged(Editable s)
        {
        	if( MainActivity.liveSending == false ) {
        		return;
        	}
        	
        	if( textUpdated == true ) {
        		
        		textUpdated = false;
        		return;
        	}
        	
        	String text = s.toString();

        	if( previousText.length() == 0 && text.length() == 0 ) {
        		return;
        	}
        	
        	String pressedChar = null;
        	int length = text.length();
        	if( length > previousText.length() ) {
        		
        		int inputLength = length - previousText.length();
        		
        		pressedChar = text.substring(editText.getSelectionEnd()-inputLength, editText.getSelectionEnd());
        		
        		if( pressedChar.equals("\n") ) {
            		pressedChar = enter;
            	}
        		else if( pressedChar.equals(" ") ) {
        			pressedChar = space;
        		}
        	}
        	else {
        		pressedChar = backspace;
        	}
        	
    		textUpdated = true;
    		
        	editText.setText(pressedChar);
        	editText.setSelection(pressedChar.length());
        	
        	if( pressedChar.equals(enter) ) {
        		SendInputAction("RETURN");
        	}
        	else if( pressedChar.equals(backspace) ) {
        		SendInputAction("BACK");
        	}
        	else if( pressedChar.equals(space) ) {
        		SendInputAction("SPACE");
        	}
        	else {
        		SendText(pressedChar);
        	}
        }
	};
	
	// Defining the check box click listener
    private final OnClickListener checkBoxListener =  new OnClickListener() {
		  @Override
		  public void onClick(View v) {
			  
			  if( ((CheckBox)v).isChecked() ) {
				  
				  // Setting live sending flag and writing it to file
				  MainActivity.liveSending = true;
				  WriteLiveSendingState();
				  
				  // Disabling send button and edit text
				  send_button.setEnabled(false);
				  editText.setHint("");
				  editText.setText("");
				  editText.setCursorVisible(false);
				  
				  // Requesting focus on the edit text and displaying soft keyboard
				  editText.requestFocus();
				  imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			  }
			  else {
				  
				  // Setting live sending flag and writing it to file
				  MainActivity.liveSending = false;
				  WriteLiveSendingState();
				  
				  // Enabling send button and edit text
				  send_button.setEnabled(true);
				  editText.setHint("Type your text here");
				  editText.setText("");
				  editText.setCursorVisible(true);
				  
				  editText.setVisibility(View.VISIBLE);
				  
				  // Requesting focus on the edit text and displaying soft keyboard
				  editText.requestFocus();
				  imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			  }
		  }
    };
	
    public KeyboardControlFragment() {}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_keyboard));
    	
        // Inflate the layout for this fragment
    	setHasOptionsMenu(true);
        final View parentView = inflater.inflate(R.layout.fragment_keyboard_control_1, container, false);
		
		// Configuring edit text, send button, check box and soft keyboard
		editText = (EditText) parentView.findViewById(R.id.keyboard_edit_text);		
		send_button = (Button) parentView.findViewById(R.id.button_send);
        liveCheckBox = (CheckBox) parentView.findViewById(R.id.checkbox_live_sending);
		imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
        // Starting networking thread
		keyboardThread = new NetworkingThread();
		keyboardThread.start();
		inputActionRunnable = new PostRequestRunnable(getActivity(), keyboardThread);
		stringAction = new StringAction(MainActivity.keyboardUri);
		inputAction = new StringAction(MainActivity.inputUri);
		
        // Setting send button on click listener
        send_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = editText.getText().toString();
				
				Log.d("SEND_TEXT", "TEXT: " + text);
				
				SendText(text);
				editText.setText("");
			}
		});
        
        // Setting check box and edit text on click listeners
        liveCheckBox.setOnClickListener(checkBoxListener);
        editText.addTextChangedListener(editTextWatcher);
        
        return parentView;
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	// Telling the softInputMode to resize the screen contents
    	getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    	
        // Requesting focus on the edit text and displaying soft keyboard
        editText.setEnabled(true);
    	editText.requestFocus();
    	imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    	
    	// Reading live sending state
    	ReadLiveSendingState();
    	
    	// Checking live sending flag state and displaying the corresponding items
    	if( MainActivity.liveSending == true ) {
    		liveCheckBox.setChecked(true);
    		send_button.setEnabled(false);
			editText.setText("");
			editText.setCursorVisible(false);
    	}
    	else {
    		liveCheckBox.setChecked(false);
    		send_button.setEnabled(true);
    		editText.setHint("Type your text here");
			editText.setText("");
			editText.setCursorVisible(true);
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	// Hiding the keyboard
    	imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    	
    	// Clearing thread message queue
    	keyboardThread.RemoveHandlerCallbacksAndMessages();
    }
    
    @Override
    public void onDestroy () {
    	super.onDestroy();
    	
    	// Stopping thread
    	keyboardThread.RemoveHandlerCallbacksAndMessages();
    	keyboardThread.Stop();
    	
//    	// TODO: This is blocking the UI until the thread finishes the current request. Think about it
//    	Log.d("KEYBOARD",">>> join() called on keyboardThread");
//    	
//		// Waiting for thread to finish
//		try {
//			keyboardThread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//		boolean threadAlive = keyboardThread.isAlive();
//     	Thread.State state = keyboardThread.getState();
//     	long id = keyboardThread.getId();
//     	Log.d("ON_DESTROY_KEYBOARD","keyboardThread: THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
		
     // Checking thread state
    	Runnable checkThread = new Runnable() {
			@Override
			public void run() {
				boolean threadAlive = keyboardThread.isAlive();
				Thread.State state = keyboardThread.getState();
				long id = keyboardThread.getId();
				Log.d("ON_DESTROY_KEYBOARD","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
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
        case R.id.switch_to_shortcut_action_bar:
        	switchToShortcutControl();
        	return true;
        case R.id.switch_to_pointer_action_bar:
        	switchToPointerControl();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void switchToShortcutControl() {
    	// Create the new fragment
    	ShortcutControlFragment newFragment = new ShortcutControlFragment();
    	
    	// Create a new the transaction
    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
    	// Set transaction animations
    	transaction.setCustomAnimations(R.anim.animation_enter_from_right, R.anim.animation_exit_to_left);
    	
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

	
    private void ReadLiveSendingState() {
    	
    	String filename = getString(R.string.live_sending_state_file);
		
		// Opening file for reading
		FileInputStream inputStream;
		try {
			inputStream = getActivity().openFileInput(filename);
		} catch (FileNotFoundException e) {
			Log.d("READ_LIVE_SENDING", filename + " NOT found");

			// The file dose not exist. Setting default values
			MainActivity.liveSending = false;
			
			// Writing default values to file
			WriteLiveSendingState();
			
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
		
		MainActivity.liveSending = Boolean.valueOf(strTokens[0]);
    }
    
    private void WriteLiveSendingState() {
    	
    	String filename = getString(R.string.live_sending_state_file);
		
		FileOutputStream outputStream = null;
		try {
			outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String strLiveSending = String.valueOf(MainActivity.liveSending);
		
		try {
			outputStream.write(strLiveSending.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void SendText(String text) {
    	
    	stringAction.SetText(text);
    	stringAction.SetPackageId(MainActivity.requestId);
    	
    	inputActionRunnable.SetObject(stringAction);
    	keyboardThread.handler.post(inputActionRunnable);
    	
    	++ MainActivity.requestId;
        MainActivity.requestId %= MainActivity.maxRequestId;
    }
    
    private void SendInputAction(String action) {
    	
    	inputAction.SetText(action);
    	inputAction.SetPackageId(MainActivity.requestId);
    	
    	inputActionRunnable.SetObject(inputAction);
    	keyboardThread.handler.post(inputActionRunnable);
    	
    	++ MainActivity.requestId;
        MainActivity.requestId %= MainActivity.maxRequestId;
    }
}

