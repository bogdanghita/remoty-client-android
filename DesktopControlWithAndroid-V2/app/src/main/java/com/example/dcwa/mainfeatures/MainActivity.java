package com.example.dcwa.mainfeatures;

import java.net.URI;

import com.example.dcwa.auxclasses.EmptyPackage;
import com.example.dcwa.auxclasses.ISendableAction;
import com.example.dcwa.auxclasses.PointerDisplacement;
import com.example.dcwa.auxclasses.ScrollClicks;
import com.example.dcwa.auxclasses.ToastFactory;
import com.example.dcwa.auxclasses.UriFactory;
import com.example.dcwa.networking.NetworkingThread;
import com.example.dcwa.networking.PostRequestRunnable;
import com.example.dcwa.networking.ServerDetectionActivity;
import com.example.dcwa.networking.ServerDetectionFragment;
import com.example.dcwa.settings.AboutActivity;
import com.example.dcwa.settings.SettingsActivity;
import com.example.desktopcontrolwithandroid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	
	private static Activity activity; 
	
	public static boolean mainConfigurationChanged = false;
	
	private static AlertDialog dialog;
	private static boolean dialogIsDisplayed = false;
	
	public static boolean liveSending;
	
	private static final int SEND_REQ_MSG = 2;
	public static int USER_DEFINED_KEEP_ALIVE_FREQUENCY;
	public static final int CONNECTION_LOST_KEEP_ALIVE_FREQUENCY = 2100;
	public static int IN_USE_KEEP_ALIVE_FREQUENCY;
	
	// Settings value ranges
	public static final double[] POINTER_SENSITIVITY_VALUES = {0.4, 0.6, 0.8, 1, 1.25, 1.5, 2, 2.5, 3, 4, 5};
	public static final int[] SCROLL_SENSITIVITY_VALUES = {80, 60, 40, 35, 30, 25, 20, 15, 10, 8, 6, 3, 1};
	public static final int[] SINGLE_REQUEST_TIMEOUT_VALUES = {20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 1000, 1500, 2000};
	public static final int[] KEEP_ALIVE_REQUEST_FREQUENCY_VALUES = {10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 125, 150, 175, 200};
	
	public static int requestId = 0;
	public final static int maxRequestId = 10000;
	
	private String URL;
	
	public static URI keyboardUri;
	public static URI inputUri;
	private static URI motionUri;
	private static URI scrollUri;
	private static URI emptyPackageUri;
	
	private UriFactory uriFactory;
	public static volatile ToastFactory toast = new ToastFactory();
	
	private boolean backPressedOnce = false;
	private final int TOAST_LENGTH_SHORT_DURATION = 2000;
	
	public static ISendableAction toSendObject;
	private static EmptyPackage emtyPackage;
	public static PointerDisplacement displacement;
	public static ScrollClicks scrollClicks;
	
	private static NetworkingThread netThread;
	private static PostRequestRunnable actionRunnable;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        
        Log.d("MAIN","ON_CREATE called");
//		Log.d("MAIN", MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX] + " - " + 
//				MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX]);
        
        // Check if the activity is restored from a previous state
        // If true, there is no need to create and add a new fragment
        // (this avoids the case of overlapping fragments)
        if (savedInstanceState != null) {
        
	        // Starting thread
	        netThread = new NetworkingThread();
	        netThread.start();
	        // Seems that without this one it works ... very strange, but this is the only way it works
//  		ActionRunnable = new PostRequestRunnable<String>(motionUri, netThread, this, toast);
  		
//	  		// DEBUG: Checking thread state
//	  		boolean threadAlive = netThread.isAlive();
//	  		Thread.State state = netThread.getState();
//	  		long id = netThread.getId();
//	  		Log.d("ON_CREATE_MAIN_RESTORED", "THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
        	
            return;
        }
        
        // Create a new Fragment to be placed in the activity layout
        ShortcutControlFragment fragment = new ShortcutControlFragment();
        
        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments
        fragment.setArguments(getIntent().getExtras());
        
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        
    	// Initializing server URL
        URL = ServerDetectionFragment.serverURL;
        
		// Initializing UriFactory and uris
		uriFactory = new UriFactory(URL);
		keyboardUri = uriFactory.Create("PostText");
		inputUri = uriFactory.Create("PostInputAction");
        motionUri = uriFactory.Create("PostPointerDisplacement");
        scrollUri = uriFactory.Create("PostScroll");
        emptyPackageUri = uriFactory.Create("PostEmptyPackage");
        
        // Starting thread
        netThread = new NetworkingThread();
        netThread.start();
		actionRunnable = new PostRequestRunnable(this, netThread);

        // Initializing objects
		emtyPackage = new EmptyPackage(emptyPackageUri);
		displacement = new PointerDisplacement(motionUri);
		scrollClicks = new ScrollClicks(scrollUri);
		toSendObject = emtyPackage;
		
		// Reading settings values from file
		SettingsActivity.ReadSettingsValues(this);
    }

	@Override
	public void onResume() {
		super.onResume();
        
		Log.d("MAIN","ON_RESUME called");
//		Log.d("MAIN", MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX] + " - " + 
//				MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX]);
		
		// Setting activity
		activity = this;
		
		// Initializing settings values
		// Setting pointer and scroll sensitivity
		TouchAreaView.POINTER_SPEED = POINTER_SENSITIVITY_VALUES[ServerDetectionActivity.CURRENT_POINTER_SENSITIVITY_IDX];
		TouchAreaView.SCROLL_UNIT = SCROLL_SENSITIVITY_VALUES[ServerDetectionActivity.CURRENT_SCROLL_SENSITIVITY_IDX];
		
		// Setting runnable request timeout and keep alive frequency
		PostRequestRunnable.USER_DEFINED_REQUEST_TIMEOUT = SINGLE_REQUEST_TIMEOUT_VALUES[ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX];
		USER_DEFINED_KEEP_ALIVE_FREQUENCY = KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX];
		
		// Setting the in use keep alive frequency value
		IN_USE_KEEP_ALIVE_FREQUENCY = USER_DEFINED_KEEP_ALIVE_FREQUENCY;
		PostRequestRunnable.IN_USE_REQUEST_TIMEOUT = PostRequestRunnable.USER_DEFINED_REQUEST_TIMEOUT;
		
		if( dialogIsDisplayed == false ) {
			// Starting handler
			startSendingRequests();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
    	
		Log.d("MAIN","ON_PAUSE called");
//		Log.d("MAIN", MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX] + " - " + 
//				MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX]);
		
		// Stopping handler
		stopSendingRequests();
		
		// Dismissing dialog
		if( dialog != null ) {
			dialog.dismiss();
		}
		
		// Canceling toast
		toast.Cancel();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.d("MAIN","ON_DESTROY called");
//		Log.d("MAIN", MainActivity.SINGLE_REQUEST_TIMEOUT_VALUES[ServerDetectionActivity.CURRENT_SINGLE_REQUEST_TIMEOUT_IDX] + " - " + 
//				MainActivity.KEEP_ALIVE_REQUEST_FREQUENCY_VALUES[ServerDetectionActivity.CURRENT_KEEP_ALIVE_REQUEST_FREQUENCY_IDX]);
		
		// Stopping thread
		netThread.RemoveHandlerCallbacksAndMessages();
		netThread.Stop();
		
//		// TODO: This is blocking the UI until the thread finishes the current request. Think about it
//		Log.d("MAIN",">>> join() called on netThread");
//		
//		// Waiting for thread to finish
//		try {
//			netThread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// Checking thread state
    	Runnable checkThread = new Runnable() {
			@Override
			public void run() {
				boolean threadAlive = netThread.isAlive();
				Thread.State state = netThread.getState();
				long id = netThread.getId();
				Log.d("ON_DESTROY_MAIN","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
			}
		};
		new Handler().postDelayed(checkThread, PostRequestRunnable.CONNECTION_LOST_REQUEST_TIMEOUT);
		
		// Canceling toast
		toast.Cancel();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);

    	mainConfigurationChanged = true;
	    
//    	// Checks the orientation of the screen
//    	if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//    		Toast.makeText(this, "Landscape", Toast.LENGTH_SHORT).show();
//    	} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//    		Toast.makeText(this, "Portrait", Toast.LENGTH_SHORT).show();
//    	}
    }
    
    // Method that closes the app
    private void Exit() {
    	super.onBackPressed();
    }
    
    // Method that opens the 'Settings' activity
    private static void openSettings() {
    	Intent intent = new Intent(activity, SettingsActivity.class);
    	activity.startActivity(intent);
    }
    
    // Method that opens the 'About' activity
    private void openAbout() {
    	Intent intent = new Intent(this, AboutActivity.class);
    	startActivity(intent);
    }

    private static Handler keepConnectionAliveHandler = new Handler() {
    	@Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_REQ_MSG: {
 	
                	// Displaying toast if connection is unreliable
                	if( actionRunnable.unreliableConnection == true ) {
                		String message = activity.getString(R.string.connection_failed_message);
//            			MainActivity.toast.Create(message, Toast.LENGTH_LONG, activity);
//            			MainActivity.toast.Show();
            			
            			// Version 2
            			stopSendingRequests();
            			showConnectionLostDialog(message);
            			
            			return;
                	}
                	
                	// Sending object
                	SendObject();
                	
                    // Sending next message with the specified delay
                    keepConnectionAliveHandler.sendMessageDelayed(keepConnectionAliveHandler.obtainMessage(SEND_REQ_MSG), IN_USE_KEEP_ALIVE_FREQUENCY);
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
	// Displays the ReservedFileNameDialog
	private static void showConnectionLostDialog(String message) {
		
		dialogIsDisplayed = true;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Connection failed");
		builder.setMessage(message);
		
		// Adding NegativeButton to dialog
		builder.setNegativeButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();     
                openSettings();
            }
        });
		
		// Adding PositiveButton to dialog
		builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				startSendingRequests();
			}
		});
        
		dialog = builder.create();
		
		dialog.setOnDismissListener(new OnDismissListener() {
	        @Override
	        public void onDismiss(final DialogInterface arg0) {
	            dialogIsDisplayed = false;
	        }
	    });
		
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
        dialog.show();
	}
    
    private static void startSendingRequests() {
    	// Resetting connection check parameters
    	actionRunnable.lostPacketCounter = 0;
    	actionRunnable.unreliableConnection = false;
    	actionRunnable.timeOfLastSuccessfullySentPacket = System.currentTimeMillis();
    	actionRunnable.elapsedTimeSinceLastSuccessfullySentPacket = 0;
    	
    	keepConnectionAliveHandler.sendMessageDelayed(keepConnectionAliveHandler.obtainMessage(SEND_REQ_MSG), 0);
    }
    private static void stopSendingRequests() {
    	keepConnectionAliveHandler.removeCallbacksAndMessages(null);
    	netThread.RemoveHandlerCallbacksAndMessages();
    }
    
    private static void SendObject() {
    	
    	// Setting package id
    	toSendObject.SetPackageId(requestId);
    	
    	// Setting object to runnable
    	actionRunnable.SetObject(toSendObject);
    	
    	// Sending request
    	netThread.handler.post(actionRunnable);
    	
    	// Resetting displacement and scrollClicks
    	if( toSendObject.getClass() == PointerDisplacement.class ) {
    		displacement.SetDisplacement(0, 0);
    	}
    	else if( toSendObject.getClass() == ScrollClicks.class ) {
    		scrollClicks.SetNumberOfClicks(0);
    	}
    	
    	// Resetting toSendObject
    	toSendObject = emtyPackage;
    	
    	// Increasing id
    	++ requestId;
        requestId %= maxRequestId;
    }
    
}
