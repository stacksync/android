package com.stacksync.android.exceptions;

public class NotLoggedInException extends APIException{
	
	private static final long serialVersionUID = 8013279147601070724L;

	public NotLoggedInException() {
		super("User is not logged in");
	}
	
	public NotLoggedInException(String message) {
		super(message);
	}
	
	public NotLoggedInException(String message, int statusCode){
		super(message, statusCode);
	}
}
