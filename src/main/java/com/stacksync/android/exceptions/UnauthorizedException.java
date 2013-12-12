package com.stacksync.android.exceptions;

public class UnauthorizedException extends APIException {

	private static final long serialVersionUID = 2711449646420481009L;

	public UnauthorizedException() {
		super();
	}
	
	public UnauthorizedException(String message) {
		super(message);
	}
	
	public UnauthorizedException(String message, int statusCode){
		super(message, statusCode);
	}
}
