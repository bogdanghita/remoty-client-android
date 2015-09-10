package com.remoty.remotecontrol;

import android.util.Log;

import com.remoty.common.servicemanager.EventManager;
import com.remoty.gui.MainActivity;
import com.remoty.services.threading.LooperThread;
import com.remoty.services.threading.TaskScheduler;

/**
 * Created by Bogdan on 9/10/2015.
 */
public class KeysService {

	EventManager eventManager;

	LooperThread keysThread;
	MessageDispatchRunnable keysRunnable;

	public KeysService(EventManager eventManager/*, List<Button?, ButtonInfo?, ClickListener?>*/) {

		this.eventManager = eventManager;

		keysRunnable = null;
	}

	// TODO: think the port and ip should be passed here or in other method (ex. constructor)
	public void init(String ip, int port) {

		if (keysRunnable != null) {
			keysRunnable.clear();
		}

		// TODO: handle the thread stop properly
		if(keysThread.isAlive()) {
			keysThread.quit();

			try {
				keysThread.join();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				Log.d(MainActivity.KEYS, e.getClass().getName() + " | " + "exception on join()");

				return;
			}
		}

		keysRunnable = new MessageDispatchRunnable(eventManager, ip, port);
		keysThread = new LooperThread();

		// TODO: be sure the thread is started before send keys is called (I think this can be handled from outside)
		keysThread.start();
	}

	// TODO: see if this is needed and if ywe the see if it must be enhanced
	public boolean isReady() {
		return keysRunnable != null && keysThread.isAlive();
	}

	public void sendKeys(Message.KeysMessage keys) {

		keysRunnable.setMessage(keys);

		// Posting this message on the looper thread with it's handler
		keysThread.handler.post(keysRunnable);
	}

	public void clear() {

		if (keysRunnable != null) {
			keysRunnable.clear();
		}

		keysRunnable = null;
	}
}
