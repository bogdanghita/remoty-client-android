package com.remoty.common.other;


public class Constants {

	public static final String SERVICES = "SRV-";
	public final static String APP = "APP-";
	public final static String LIFECYCLE = "LIFEC-";
	public static final String DETECTION = "DET-";
	public static final String BROADCAST = "BROAD-";
	public static final String RESPONSE = "RESP-";
	public static final String SERVERS_STATE_UPDATE_SERVICE = "SRVUP-";
	public final static String KEYS = "KEYS-";
	public final static String MENU = "MENU-";
	public final static String SIGN_IN = "SIGN_IN-";
	public final static String CONFIG = "CONF-";

	public final static int ASYNC_TASK_GET_TIMEOUT = 600;
	public final static int DETECTION_RESPONSE_TIMEOUT = 500;
	public final static int PING_RESPONSE_TIMEOUT = 500;
	public final static int INIT_REMOTE_CONTROL_TIMEOUT = 2000;

	// TODO: rename and see if it is relevant since at this moment only send operations are performed
	public final static int ACCELEROMETER_TIMEOUT = 100;
	public final static int CONNECT_TIMEOUT = 500;

	public final static long DETECTION_INTERVAL = 2000;
	public final static long ACCELEROMETER_INTERVAL = 20;

	public final static int LOCAL_DETECTION_RESPONSE_PORT = 10000;
	public final static int REMOTE_DETECTION_PORT = 9001;

	public final static int MSG_SCHEDULE = 1000;

	public final static int RC_SIGN_IN = 9001;

}
