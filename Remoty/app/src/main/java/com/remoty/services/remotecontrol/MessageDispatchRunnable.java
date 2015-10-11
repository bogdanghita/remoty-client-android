package com.remoty.services.remotecontrol;

import com.remoty.common.servicemanager.EventManager;
import com.remoty.gui.pages.MainActivity;
import com.remoty.common.other.Message;


public class MessageDispatchRunnable implements Runnable {

	// TODO: Think of all the use cases before deciding for a final implementation:
	//			- automatic interval adjustment (don't know where it will be done - inside or outside of the TaskScheduler)
	//			- thread safety on the message
	//			- selective sending of the message - only when needed (on demand)
	//			- etc. I'm sure there are more...

	private Message.RemoteControlMessage message;

	private MessageDispatchService messageDispatcher;

	private String ip;
	private int port;

	public MessageDispatchRunnable(EventManager eventManager, String ip, int port, Message.RemoteControlMessage message) {

		this.ip = ip;
		this.port = port;

		messageDispatcher = new MessageDispatchService(eventManager, MainActivity.ACCELEROMETER_TIMEOUT);

		this.message = message;
	}

	@Override
	public void run() {

		// Initializing message response service service (opening socket for sending messages)
		if (!messageDispatcher.isOpen()) {
			messageDispatcher.init(ip, port);
		}

		// Checking if init() succeeded
		if (!messageDispatcher.isOpen()) {

			// Bad luck ... init() did not succeed.
			// TODO: Do something intelligent here
			return;
		}

		// TODO: In the future maybe we will want to send the object only if it is necessary
		//Sending the message
		messageDispatcher.send(message);

		// Resetting message
		message.clear();
	}

	public void setMessage(Message.RemoteControlMessage message) {

		// TODO: clone the message or something (think about it and think if it is necessary since we use the task scheduler... don't know)
		// Possible alternative would be a lock. Not sure...
		this.message = message;
	}

	public Message.AbstractMessage getMessage() {
		return this.message;
	}

	public void clear() {

		if (messageDispatcher.isOpen()) {
			messageDispatcher.close();
		}
	}
}
