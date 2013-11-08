package com.stacksync.android.exceptions;

public class UnexpectedResponseException extends APIException {

	private static final long serialVersionUID = 9196293860013222753L;

	public UnexpectedResponseException() {
		super();
	}
	
	public UnexpectedResponseException(String message) {
		super(message);
	}
	
	public UnexpectedResponseException(String message, int statusCode){
		super(message, statusCode);
	}
	
}
