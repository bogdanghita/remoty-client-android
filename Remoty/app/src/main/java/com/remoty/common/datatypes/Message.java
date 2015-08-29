package com.remoty.common.datatypes;

/**
 * Created by Bogdan on 8/8/2015.
 */

public class Message {

	public static class AbstractMessage {

		public long id;
		public long timestamp;
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
}