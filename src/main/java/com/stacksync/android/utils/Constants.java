package com.stacksync.android.utils;

public class Constants {

	public static boolean DEV_MODE = true;
	
	public static String PREFS_NAME = "stacksync_prefs";
	
	//TODO: set timeouts depending on the operation (GET, PUT, POST...)
	
	// Set the timeout in milliseconds until a connection is established.
	// value zero means the timeout is not used. 
	public static int TIMEOUT_CONNECTION = 10000; 

	// Set the default socket timeout (SO_TIMEOUT) 
	// in milliseconds which is the timeout for waiting for data.
	public static int TIMEOUT_SOCKET = 15000;
	
	//Activity result codes
	public static int RESULT_LOGOUT = 10;
}
