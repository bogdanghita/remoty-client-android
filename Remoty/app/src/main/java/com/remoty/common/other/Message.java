package com.remoty.common.other;

import java.util.ArrayList;
import java.util.List;

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

	public static class RemoteControlPortsMessage extends AbstractMessage {

		public int userId;

		public int mousePort;
		public int keyPort;
		public int buttonPort;
		public int accelerometerPort;
	}

	// TODO: you need to sync the setter and clear methods; calls are made when buttons are pressed and message is sent
	public static class RemoteControlMessage extends AbstractMessage {

		protected boolean empty = true;

		public void clear() {
			empty = true;
		}
	}

	public static class AccelerometerMessage extends RemoteControlMessage {

		private float x, y, z;

		public void setCoordinates(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
			empty = false;
		}
	}

	public static class ButtonMessage extends RemoteControlMessage {

		private List<ButtonEvent> buttonEventList;

		public ButtonMessage() {
			buttonEventList = new ArrayList<>();
		}

		public void addButtonEvent(ButtonEvent buttonEvent) {
			buttonEventList.add(buttonEvent);
			empty = false;
		}

		@Override
		public void clear() {
			super.clear();

			buttonEventList = new ArrayList<>();
		}
	}

	public static class ButtonEvent {

		public String buttonEvent;
	}
}