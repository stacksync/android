package com.stacksync.android.exceptions;

public class NoInternetConnectionException extends APIException {

	private static final long serialVersionUID = 4417608885597558121L;

	public NoInternetConnectionException() {
		super("Could not connect to Internet");
	}
	
	public NoInternetConnectionException(String message) {
		super(message);
	}
	
	public NoInternetConnectionException(String message, int statusCode){
		super(message, statusCode);
	}

}
