package com.example.dcwa.mainfeatures;

import com.example.dcwa.auxclasses.ISendableAction;
import com.example.dcwa.auxclasses.RGBColor;
import com.example.dcwa.auxclasses.StringAction;
import com.example.dcwa.networking.NetworkingThread;
import com.example.dcwa.networking.PostRequestRunnable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class TouchAreaView extends View {
	
	private static final int FADE_ALPHA = 0xFF;
	private static final int MAX_FADE_STEPS = 1000;
	
	// Computing scroll area width as the size of two cells
	private int scrollAreaWidth = 2 * ShortcutControlFragment.realCellWidth;
    private int scrollAreaMarginStart = ShortcutControlFragment.parentWidth - scrollAreaWidth;
    
    private final int POINTER_SIZE = 20;
    private final int SINGLE_TAP_SIZE = 40;
    private final int DOUBLE_TAP_SIZE = 40;
    
    public static int SCROLL_UNIT = 20;
    public static double POINTER_SPEED = 1;
    
    private final int DOUBLE_TAP_MOVE_START = 2;
    
    private boolean doubleTapMoveFlag;
    private boolean doubleTapFlag;
    private int doubleTapMoveCounter = -1;
    
    private boolean scrollFlag = false;
    private float lastScrollPosition;
	
    private Bitmap bitmap;
    private Canvas canvas;
    private final Paint backgroundPaint;
    private final Paint fadePaint;
    private Paint circlePaint;
    private int currentFadeSteps;
	
	private int startX;
	private int startY;
	private StringAction stringAction;
	
	private NetworkingThread touchThread = new NetworkingThread();
	private PostRequestRunnable touchAreaViewRunnable;
	
	public enum ColorPicker { blue, green, grey1, grey2, orange, pink, purple, yellow }
	
	// Single and double tap listener
	private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			
			stringAction.SetText("LeftButtonClick");
			stringAction.SetPackageId(MainActivity.requestId);
			
			SendInputAction(stringAction, touchAreaViewRunnable);
			drawPoint((int)event.getX(), (int)event.getY(), event.getPressure(), SINGLE_TAP_SIZE, new RGBColor(ColorPicker.pink));
			
			++ MainActivity.requestId;
			MainActivity.requestId %= MainActivity.maxRequestId;
			
			return false;
		}
		
		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			
			if( event.getAction() == MotionEvent.ACTION_DOWN ) {
				// Setting start position
				startX = (int) event.getX();
				startY = (int) event.getY();
				
				doubleTapFlag = true;
				
				doubleTapMoveFlag = false;
				doubleTapMoveCounter = 0;
				
				return true;
			}
			if ( event.getAction() == MotionEvent.ACTION_UP ){
				// Checking if action move was triggered
				if( doubleTapMoveFlag == true ) {
					
					stringAction.SetText("LeftButtonUp");
					stringAction.SetPackageId(MainActivity.requestId);
					
					// Sending left click up
					SendInputAction(stringAction, touchAreaViewRunnable);
	                	
					++ MainActivity.requestId;
					MainActivity.requestId %= MainActivity.maxRequestId;
				}
				else {
					stringAction.SetText("LeftButtonDoubleClick");
					stringAction.SetPackageId(MainActivity.requestId);
					
					// Sending double tap
					SendInputAction(stringAction, touchAreaViewRunnable);
					
					++ MainActivity.requestId;
					MainActivity.requestId %= MainActivity.maxRequestId;
				}
				// Resetting move, first move, and double tap flags
				doubleTapMoveFlag = false;
				doubleTapFlag = false;
				doubleTapMoveCounter = -1;
				
				// Drawing visual response
				drawPoint((int)event.getX(), (int)event.getY(), event.getPressure(), DOUBLE_TAP_SIZE, new RGBColor(ColorPicker.green));
						
				return true;
			}
			return false;
		}
	};
	
	private GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
	    
	public TouchAreaView(Context context, NetworkingThread thread) {
		super(context);
        
        // Doing the Thread-related initializations
        touchThread.start();
        touchAreaViewRunnable = new PostRequestRunnable((Activity)context, touchThread);
        stringAction = new StringAction(MainActivity.inputUri);
        
//		// DEBUG: Checking thread state
//		boolean threadAlive = touchThread.isAlive();
//		Thread.State state = touchThread.getState();
//		long id = touchThread.getId();
//		Log.d("C-TOR_TOUCH_AREA_VIEW","THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
		
        // Initializing Canvas and Paint members
		canvas = new Canvas();
		backgroundPaint = new Paint();
		fadePaint = new Paint();
		circlePaint = new Paint();
		
		backgroundPaint.setARGB(255, 255, 255, 255);
		fadePaint.setARGB(FADE_ALPHA, 255, 255, 255);
		fadePaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {       
	    
		// Creating and drawing bitmap
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		canvas.setBitmap(bitmap);
		canvas.drawBitmap(bitmap, 0, 0, null);

		// Resetting currentFadeSteps
		currentFadeSteps = MAX_FADE_STEPS;
		
		// Obtaining view dimensions and computing the scroll area size and start margin
		int width = this.getWidth();
		int height = this.getHeight();
		ComputeScrollAreaMarginStart(height, width);
    }
	
	@Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
    }
	
	// Computes and sets scrollAreaMarginStart
	private void ComputeScrollAreaMarginStart(int height, int width) {
		
		int cellsOnRow;
		int realCellWidth;
		
		if( height > width ) {
			cellsOnRow = 12;
		}
		else {
			cellsOnRow = 20;
		}
	    
		realCellWidth = width/cellsOnRow;
		
		// Computing scroll area width as the size of two cells
		scrollAreaWidth = 2 * realCellWidth;
	    scrollAreaMarginStart = width - scrollAreaWidth;
	}
    
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		
		// Adding SimpleOnGestureListener to detect single tap and double tap gestures
		gestureDetector.onTouchEvent(motionEvent);
		
		// ACTION DOWN
		if( motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

			// Checking if it is a double tap action
			if( doubleTapFlag == true ) {
				return true;
			}
			
			// Setting start position
			startX = (int) motionEvent.getX();
			startY = (int) motionEvent.getY();
			
			if( startX >= scrollAreaMarginStart ) {
				lastScrollPosition = startY;
				scrollFlag = true;
			}
			else {
				scrollFlag = false;
			}
			
			return true;
		}
		
		// ACTION_MOVE
		if( motionEvent.getAction() == MotionEvent.ACTION_MOVE ) {
			
			// Checking if it is a double tap action
    		if( doubleTapMoveCounter <= DOUBLE_TAP_MOVE_START && doubleTapMoveCounter >= 0 ) {
        		// Increasing double tap move counter
        		++ doubleTapMoveCounter;
    			
    			if( doubleTapMoveCounter == DOUBLE_TAP_MOVE_START ) {
    				
    				stringAction.SetText("LeftButtonDown");
                	stringAction.SetPackageId(MainActivity.requestId);
                    
                	// Sending left click down
            		SendInputAction(stringAction, touchAreaViewRunnable);
            		// Setting move flag to true
            		doubleTapMoveFlag = true;
                	
                    ++ MainActivity.requestId;
                    MainActivity.requestId %= MainActivity.maxRequestId;
    			}
    			else {
    				return true;
    			}
    		}
			
			// Obtaining current position
			int currentX = (int) motionEvent.getX();
			int currentY = (int) motionEvent.getY();
			
			// Checking scroll flag
			if( scrollFlag == true && currentX >= scrollAreaMarginStart ) {
				// Computing displacement and checking if it is
				int scrollDisplacement = (int) (lastScrollPosition - currentY);
				if ( Math.abs(scrollDisplacement) > SCROLL_UNIT ) {
					
					int clicks = MainActivity.scrollClicks.GetNumberOfClicks() +  scrollDisplacement/SCROLL_UNIT;
					
					MainActivity.scrollClicks.SetNumberOfClicks(clicks);
                	MainActivity.toSendObject = MainActivity.scrollClicks;
                	
					// Updating last scroll position
					lastScrollPosition = currentY;
                	
                    ++ MainActivity.requestId;
                    MainActivity.requestId %= MainActivity.maxRequestId;
				}
				
				// Current position becomes the new start position
				// (for the cases when motion starts from the scroll area and the exceeds it)
				startX = currentX;
				startY = currentY;
			}
			else {
				// Computing displacement
				int dispX = (int) ((currentX - startX) * POINTER_SPEED);
				int dispY = (int) ((currentY - startY) * POINTER_SPEED);
				
				int X = MainActivity.displacement.GetX() + dispX;
				int Y = MainActivity.displacement.GetY() + dispY;
				
				MainActivity.displacement.SetDisplacement(X, Y);
				MainActivity.toSendObject = MainActivity.displacement;
				
				// Current position becomes the new start position
				startX = currentX;
				startY = currentY;
				
				// Resetting scroll flag
				scrollFlag = false;
				
				// Drawing visual response
				drawPoint(currentX, currentY, motionEvent.getPressure(), POINTER_SIZE, new RGBColor(ColorPicker.grey2));
			}
			return true;
		}

		// ACTION UP
		if( motionEvent.getAction() == MotionEvent.ACTION_UP ) {
			scrollFlag = false;
			return true;
		}
		
		return false;
	}
	
	public void drawPoint(int x, int y, float pressure, float size, RGBColor color) {

//		int alpha = 255;
//		circlePaint.setARGB(alpha, color.r, color.g, color.b);
//		
//		canvas.drawCircle(x, y, size, circlePaint);
//		invalidate();
//		
//		currentFadeSteps = 0;
	}
	
    public void fade() {
        if (currentFadeSteps < MAX_FADE_STEPS) {
            canvas.drawPaint(fadePaint);
            invalidate();
            currentFadeSteps++;
        }
    }
    
    // Method that stops the thread and handler
    public void stopThread() {
    	
    	// Stopping thread and handler
    	touchThread.RemoveHandlerCallbacksAndMessages();
    	touchThread.Stop();
    	
//    	// TODO: This is blocking the UI until the thread finishes the current request. Think about it
//    	Log.d("TOUCH",">>> join() called on touchThread");
//    	
//		// Waiting for thread to finish
//		try {
//			touchThread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//     	boolean threadAlive = touchThread.isAlive();
//    	Thread.State state = touchThread.getState();
//    	long id = touchThread.getId();
//    	Log.d("D-TOR_TOUCH_AREA_VIEW","touchThread: THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
    	
    	// Checking thread state
    	Runnable checkThread = new Runnable() {
			@Override
			public void run() {
				boolean threadAlive = touchThread.isAlive();
				Thread.State state = touchThread.getState();
				long id = touchThread.getId();
				Log.d("ON_DESTROY_TOUCH","THREAD: " + id + " - Alive: " + threadAlive + " - State: " + state);
			}
		};
		new Handler().postDelayed(checkThread, PostRequestRunnable.CONNECTION_LOST_REQUEST_TIMEOUT);
    	
    	// DEBUG: Checking thread state
    	// Checking thread state with 6 seconds delay so that the thread has time to finish any attempt to send a request
//    	Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//             public void run() {
//             	boolean threadAlive = touchThread.isAlive();
//            	Thread.State state = touchThread.getState();
//            	long id = touchThread.getId();
//            	Log.d("D-TOR_TOUCH_AREA_VIEW","THREAD: " + id + " - Alive: " + String.valueOf(threadAlive) + " - State: " + String.valueOf(state));
//             }
//        }, 4000);
    }
    
    public void SendInputAction(ISendableAction inputAction, PostRequestRunnable runnable) {
    	
    	runnable.SetObject(inputAction);
    	touchThread.handler.post(runnable);
    	
    	Log.d("SEND_INPUT_ACTION","Sending input action from TouchAreaView");
    }
}
