package com.remoty.remotecontrol;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

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
	public KeysService(EventManager eventManager, List<KeysButtonInfo> buttonInfoList, RelativeLayout layout) {

		this.eventManager = eventManager;

		populateLayout(buttonInfoList, layout);

		timer = new TaskScheduler();

		keysRunnable = null;
	}

	private void populateLayout(List<KeysButtonInfo> buttonInfoList, RelativeLayout layout) {

		for (KeysButtonInfo buttonInfo : buttonInfoList) {

			// TODO: give the actual layout dimensions here
			// Creating button
			Button button = getButton(buttonInfo, layout, 0, 0);

			// Adding click listener
			addClickListener(button, buttonInfo.action);

			// Adding button to layout
			layout.addView(button);
		}
	}

	private Button getButton(KeysButtonInfo buttonInfo, RelativeLayout layout, int layoutWidth, int layoutHeight) {

		// TODO: Claudiu
		// TODO: compute this with the percent in buttonInfo and with the height and width of
		// the layout (think when to get them, I think from outside ...)
		int startX = 30;
		int startY = 30;
		int height = 300;
		int width = 500;

		// Creating view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.leftMargin = startX;
		params.topMargin = startY;

		// TODO: change the MainActivity.Instance thing
		// Creating linear layout to hold the two text views
		final Button button = new Button(MainActivity.Instance);
		button.setText(buttonInfo.name);
		button.setLayoutParams(params);

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

		keysRunnable.setMessage(keys);
	}
}
