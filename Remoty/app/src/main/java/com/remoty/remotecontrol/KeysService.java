package com.remoty.remotecontrol;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.remoty.R;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.gui.MainActivity;
import com.remoty.services.threading.TaskScheduler;

import java.util.List;

/**
 * Created by Bogdan on 9/10/2015.
 */
public class KeysService {

	EventManager eventManager;

	TaskScheduler timer;
	MessageDispatchRunnable keysRunnable;

	// TODO: Decide what type the layout should be
	public KeysService(EventManager eventManager) {

		this.eventManager = eventManager;

		timer = new TaskScheduler();

		keysRunnable = null;
	}

	public void populateLayout(List<KeysButtonInfo> buttonInfoList, RelativeLayout layout,
							   int layoutWidth, int layoutHeight) {

		for (KeysButtonInfo buttonInfo : buttonInfoList) {

			// Creating button
			Button button = getButton(buttonInfo, layoutWidth, layoutHeight);

			// Adding click listener
			addClickListener(button, buttonInfo.action);

			// Adding button to layout
			layout.addView(button);
		}
	}

	private Button getButton(KeysButtonInfo buttonInfo, int layoutWidth, int layoutHeight) {

		int startX = (int) (buttonInfo.startXPercent * layoutWidth);
		int startY = (int) (buttonInfo.startYPercent * layoutHeight);
		int width = (int) (buttonInfo.widthPercent * layoutWidth);
		int height = (int) (buttonInfo.heightPercent * layoutHeight);

		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX;
		params.topMargin = startY;

		// TODO: change the MainActivity.Instance thing
		// Creating linear layout to hold the two text views
		final Button button = new Button(MainActivity.Instance);
		button.setText(buttonInfo.name);
		button.setLayoutParams(params);
        button.setBackgroundColor(Color.parseColor("#b6b6b6"));

		return button;
	}

	private void addClickListener(final Button button, final String buttonAction) {

		button.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				String prefix = "";

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					prefix = "Press&";
					button.setPressed(true);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					prefix = "Release&";
					button.setPressed(false);
				}
				else {
					return true;
				}

				Message.KeysMessage message = new Message.KeysMessage();
				String action = prefix + buttonAction;

				message.buttonAction = action;

				setMessage(message);

				return true;
			}
		});
	}

	// TODO: think the port and ip should be passed here or in other method (ex. constructor)
	public void init(String ip, int port) {

		if (keysRunnable != null) {
			keysRunnable.clear();
		}

		Message.KeysMessage emptyMessage = new Message.KeysMessage();
		keysRunnable = new MessageDispatchRunnable(eventManager, ip, port, emptyMessage);
	}

	public void start() {

		if (!timer.isRunning()) {

			// TODO: Change this interval
			timer.start(keysRunnable, MainActivity.ACCELEROMETER_INTERVAL);
		}
	}

	public void stop() {

		if (timer.isRunning()) {
			timer.stop();
		}
	}

	public void clear() {

		stop();

		if (keysRunnable != null) {
			keysRunnable.clear();
		}

		keysRunnable = null;
	}

	public boolean isReady() {
		return keysRunnable != null;
	}

	public boolean isRunning() {
		return timer.isRunning();
	}

	// This is not needed to be synchronised because the calls are made on the same thread
	private void setMessage(Message.KeysMessage keys) {

		// NOTE: keys runnable is set to null if connection is lost
		// This is an option of making the app not crash if a button is pressed when connection is lost
		// TODO: Think of a better solution for this problem
		if(keysRunnable == null) {
			return;
		}

		keysRunnable.setMessage(keys);
	}
}
