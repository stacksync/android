package com.stacksync.android.exceptions;

public class UnexpectedStatusCodeException extends APIException {

	private static final long serialVersionUID = 1699169178702929377L;
	
	public UnexpectedStatusCodeException() {
		super();
	}
	
	public UnexpectedStatusCodeException(String message) {
		super(message);
	}
	
	public UnexpectedStatusCodeException(String message, int statusCode){
		super(message, statusCode);
	}
}
