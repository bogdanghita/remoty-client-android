package com.remoty.common;

/**
 * Created by Bogdan on 8/30/2015.
 */
public class AccelerometerRunnable implements Runnable {

	// TODO: Think of all the use cases before deciding for a final implementation:
	//			- automatic interval adjustment (don't know where it will be done - inside or outside of the TaskScheduler)
	//			- thread safety on the message
	//			- selective sending of the message - only when needed (on demand)
	//			- etc. I'm sure there are more...

	private Message.AbstractMessage message;

	// TODO: think if this is needed or we should use directly the TcpSocket
	MessageDispatchService messageDispatcher;

	public AccelerometerRunnable() {

		messageDispatcher = new MessageDispatchService();
	}

	@Override
	public void run() {

		// TODO: In the future maybe we will want to send the object only if it is necessary

		// TODO: send the message

		messageDispatcher.send(message);
	}

	public void setMessage(Message.AbstractMessage message) {

		// TODO: clone the message or something (think about it and think if it is necessary since we use the task scheduler... don't know)
		// Possible alternative would be a lock. Not sure...

	}

	public void clear() {

	}
}
