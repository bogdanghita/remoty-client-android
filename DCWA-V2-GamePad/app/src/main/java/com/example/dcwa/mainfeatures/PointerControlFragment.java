package com.example.dcwa.mainfeatures;

import com.example.dcwa.auxclasses.StringAction;
import com.example.dcwa.auxclasses.ToastFactory;
import com.example.dcwa.networking.NetworkingThread;
import com.example.dcwa.networking.PostRequestRunnable;
import com.example.desktopcontrolwithandroid.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class PointerControlFragment extends Fragment {

    private static final int FADE_MSG = 1;
    private static final int FADE_DELAY = 1;
	
	private static TouchAreaView touchAreaView;
	
	private NetworkingThread pointerThread;
	private PostRequestRunnable inputActionRunnable;
	
	private StringAction stringAction;
	
	public PointerControlFragment() {}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
    	// Setting action bar title
    	getActivity().setTitle(getString(R.string.title_pointer));
    	
    	// Inflate the layout for this fragment
    	setHasOptionsMenu(true);
        View parentView = inflater.inflate(R.layout.fragment_pointer_control, container, false);
    	
    	// Initializing ToastFactory and UriFactory
//    	toast = new ToastFactory(getActivity().getApplicationContext());
        	
        // Starting networking thread
		pointerThread = new NetworkingThread();
		pointerThread.start();
		inputActionRunnable = new PostRequestRunnable(getActivity(), pointerThread);
		stringAction = new StringAction(MainActivity.inputUri);
		
//		// DEBUG: Checking thread state
//		boolean threadAlive = pointerThread.isAlive();
//    	Thread.State state = pointerThread.getState();
//    	long id = pointerThread.getId();
//    	Log.d("ON_CREATE_POINTER", "THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
		
        // Adding the TouchAreaView
        touchAreaView = new TouchAreaView(this.getActivity(), pointerThread);
        LinearLayout touchAreaLayout = (LinearLayout) parentView.findViewById(R.id.layout_touch_area);
        touchAreaLayout.addView(touchAreaView);
        // This might be useless. Check it please...
        touchAreaView.requestFocus();
        
        // Adding left/right click buttons
        final Button buttonLeft = (Button) parentView.findViewById(R.id.button_left_click);       
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_DOWN) {
					SendInputAction("LeftButtonDown");
					buttonLeft.setPressed(true);
				}
				else if( event.getAction() == MotionEvent.ACTION_UP) {
					SendInputAction("LeftButtonUp");
					buttonLeft.setPressed(false);
				}
				// TODO see what the return value means
				return true;
			}
		});
        
        final Button buttonRight = (Button) parentView.findViewById(R.id.button_right_click);
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction() == MotionEvent.ACTION_DOWN) {
					SendInputAction("RightButtonDown");
					buttonRight.setPressed(true);
				}
				else if( event.getAction() == MotionEvent.ACTION_UP) {
					SendInputAction("RightButtonUp");
					buttonRight.setPressed(false);
				}
				// TODO see what the return value means
				return true;
			}
		});
        
        return parentView;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
	public void onResume() {
        super.onResume();

        // Telling the softInputMode NOT to resize the screen contents
    	getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        
//        Log.d("POINTER","ON_RESUME");
        
        // Starting handlers
//        startFading();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
//    	Log.d("POINTER","ON_PAUSE");
    	
    	// Clearing thread message queue
    	pointerThread.RemoveHandlerCallbacksAndMessages();
    	
    	// Stopping handlers
    	stopFading();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// Stopping thread running in touchAreaView
    	touchAreaView.stopThread();
    	// Stopping thread
    	pointerThread.RemoveHandlerCallbacksAndMessages();
    	pointerThread.Stop();
    	
//    	// TODO: This is blocking the UI until the thread finishes the current request. Think about it
//    	Log.d("POINTER",">>> join() called on pointerThread");
//    	
//		// Waiting for thread to finish
//		try {
//			pointerThread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	
//    	boolean threadAlive = pointerThread.isAlive();
//     	Thread.State state = pointerThread.getState();
//     	long id = pointerThread.getId();
//     	Log.d("ON_DESTROY_POINTER","pointerThread: THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
     	
    	// Checking thread state
    	Runnable checkThread = new Runnable() {
			@Override
			public void run() {
				boolean threadAlive = pointerThread.isAlive();
				Thread.State state = pointerThread.getState();
				long id = pointerThread.getId();
				Log.d("ON_DESTROY_POINTER","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
			}
		};
		new Handler().postDelayed(checkThread, PostRequestRunnable.CONNECTION_LOST_REQUEST_TIMEOUT);
    	
//    	// DEBUG: Checking thread state
//    	// Checking thread state with 6 seconds delay so that the thread has time to finish any attempt to send a request
//    	Handler handler = new Handler(); 
//        handler.postDelayed(new Runnable() { 
//             public void run() {
//            	 boolean threadAlive = pointerThread.isAlive();
//            	 Thread.State state = pointerThread.getState();
//            	 long id = pointerThread.getId();
//            	 Log.d("ON_DESTROY_POINTER","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
//             }
//        }, 4000);
    	
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
        case R.id.switch_to_keyboard_action_bar:
        	switchToKeyboardControl();
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
    	transaction.setCustomAnimations(R.anim.animation_enter_from_left, R.anim.animation_exit_to_right);
    	
    	// Replace the content of the fragment_container view with this fragment
    	transaction.replace(R.id.fragment_container, newFragment);
    	
    	transaction.commit();
    }
    
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
    
    private static Handler fadeHandler = new Handler() {
    	@Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_MSG: {
                    touchAreaView.fade();
                    fadeHandler.sendMessageDelayed(fadeHandler.obtainMessage(FADE_MSG), FADE_DELAY);
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
    private void startFading() {
    	fadeHandler.removeMessages(FADE_MSG);
        fadeHandler.sendMessageDelayed(fadeHandler.obtainMessage(FADE_MSG), FADE_DELAY);
    }
    private void stopFading() {
    	fadeHandler.removeMessages(FADE_MSG);
    }
    
    private void SendInputAction(String inputAction) {
    	
    	stringAction.SetText(inputAction);
    	stringAction.SetPackageId(MainActivity.requestId);
    	
    	inputActionRunnable.SetObject(stringAction);
    	pointerThread.handler.post(inputActionRunnable);
    	
    	++ MainActivity.requestId;
        MainActivity.requestId %= MainActivity.maxRequestId;
    }
}
