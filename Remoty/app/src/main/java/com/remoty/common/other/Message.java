package com.remoty.common.other;

/**
 * Created by Bogdan on 8/8/2015.
 */

public class Message {

	public static class AbstractMessage {

		public long id;
		public long timestamp;
		public boolean empty = false;
	}

	public static class LargeMessage extends AbstractMessage {

		public int[] data;
	}

	public static class PortMessage extends AbstractMessage {

		public int port;
	}

	public static class HostInfoMessage extends PortMessage {

		public String hostname;
	}

	public static class AccelerometerMessage extends AbstractMessage {

		public float x, y, z;
	}

	public static class KeysMessage extends AbstractMessage {

		public String buttonAction;
	}

	public static class RemoteControlPortsMessage extends AbstractMessage {

		public int userId;

		public int mousePort;
		public int keyPort;
		public int buttonPort;
		public int accelerometerPort;
	}
}