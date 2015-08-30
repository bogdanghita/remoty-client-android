package com.remoty.common;

/**
 * Created by Bogdan on 8/30/2015.
 */
public class MessageDispatchRunnable implements Runnable {

	// TODO: Think of all the use cases before deciding for a final implementation:
	//			- automatic interval adjustment (don't know where it will be done - inside or outside of the TaskScheduler)
	//			- thread safety on the message
	//			- selective sending of the message - only when needed (on demand)
	//			- etc. I'm sure there are more...

	private Message.AbstractMessage message;

	private MessageDispatchService messageDispatcher;

	private String ip;
	private int port;

	public MessageDispatchRunnable(String ip, int port) {

		this.ip = ip;
		this.port= port;

		// TODO: think about this timeout and then move it from here
		int MESSAGE_DISPATCH_TIMEOUT = 100;

		messageDispatcher = new MessageDispatchService(MESSAGE_DISPATCH_TIMEOUT);

		message = new Message.AbstractMessage();
	}

	@Override
	public void run() {

		// Initializing message response service service (opening socket for sending messages)
		if (!messageDispatcher.isOpen()) {
			messageDispatcher.init(ip, port);
		}

		// TODO: In the future maybe we will want to send the object only if it is necessary
		//Sending the message
		messageDispatcher.send(message);
	}

	public void setMessage(Message.AbstractMessage message) {

		// TODO: clone the message or something (think about it and think if it is necessary since we use the task scheduler... don't know)
		// Possible alternative would be a lock. Not sure...
		this.message = message;
	}

	public void clear() {

		if (messageDispatcher.isOpen()) {
			messageDispatcher.close();
		}
	}
}
