package com.remoty.services.remotecontrol;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.remoty.R;
import com.remoty.common.servicemanager.EventManager;
import com.remoty.gui.pages.MainActivity;
import com.remoty.common.other.KeysButtonInfo;
import com.remoty.common.other.Message;
import com.remoty.services.threading.TaskScheduler;

import java.util.List;

/**
 * Created by Bogdan on 9/10/2015.
 */
public class ButtonService {

	EventManager eventManager;

	TaskScheduler timer;
	MessageDispatchRunnable buttonRunnable;

	// TODO: Decide what type the layout should be
	public ButtonService(EventManager eventManager) {

		this.eventManager = eventManager;

		timer = new TaskScheduler();

		buttonRunnable = null;
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

		// Adding padding.
		final int padding = 5;

		int startX = (int) (buttonInfo.startXPercent * layoutWidth) + padding;
		int startY = (int) (buttonInfo.startYPercent * layoutHeight) + padding;
		int width = (int) (buttonInfo.widthPercent * layoutWidth) - 2 * padding;
		int height = (int) (buttonInfo.heightPercent * layoutHeight) - 2 * padding;

		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX;
		params.topMargin = startY;

		// TODO: change the MainActivity.Instance thing
		// Creating linear layout to hold the two text views
		final Button button = new Button(MainActivity.Instance);

		// This removes the default toUpper a button makes to it's text.
		button.setTransformationMethod(null);

		// Prepare the text.
		button.setText(buttonInfo.name);
		button.setTextColor(Color.parseColor("#ffffff"));
		button.setTextSize(18);

		// Button position, size and colours.
		button.setLayoutParams(params);
		button.setBackgroundResource(R.drawable.button_border);

		return button;
	}

	private void addClickListener(final Button button, final String action) {

		button.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				String prefix = "";

				// TODO: URGENT !!!
				// TODO: Refactor the format of the button action
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					prefix = "Press";
					button.setPressed(true);
				}
				else if (event.getAction() == MotionEvent.ACTION_UP) {
					prefix = "Release";
					button.setPressed(false);
				}
				else {
					return true;
				}

				Message.ButtonEvent message = new Message.ButtonEvent();
				message.eventType = prefix;
				message.buttonAction = action;

				addButtonEvent(message);

				return true;
			}
		});
	}

	// TODO: think the port and ip should be passed here or in other method (ex. constructor)
	public void init(String ip, int port) {

		if (buttonRunnable != null) {
			buttonRunnable.clear();
		}

		Message.ButtonMessage emptyMessage = new Message.ButtonMessage();
		buttonRunnable = new MessageDispatchRunnable(eventManager, ip, port, emptyMessage);
	}

	public void start() {

		if (!timer.isRunning()) {

			// TODO: Change this interval
			timer.start(buttonRunnable, MainActivity.ACCELEROMETER_INTERVAL);
		}
	}

	public void stop() {

		if (timer.isRunning()) {
			timer.stop();
		}
	}

	public void clear() {

		stop();

		if (buttonRunnable != null) {
			buttonRunnable.clear();
		}

		buttonRunnable = null;
	}

	public boolean isReady() {
		return buttonRunnable != null;
	}

	public boolean isRunning() {
		return timer.isRunning();
	}

	private void addButtonEvent(Message.ButtonEvent buttonEvent) {

		// NOTE: button runnable is set to null if connection is lost
		// This is an option of making the app not crash if a button is pressed when connection is lost
		// TODO: Think of a better solution for this problem
		if (buttonRunnable == null) {
			return;
		}

		Message.ButtonMessage message = (Message.ButtonMessage) buttonRunnable.getMessage();
		message.addButtonEvent(buttonEvent);
	}
}
