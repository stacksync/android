package com.stacksync.android.exceptions;

public class APIException extends Exception {

	private static final long serialVersionUID = 8982820070391924966L;

	private int statusCode;

	public APIException() {
		super();
		this.statusCode = 0;
	}

	public APIException(String message) {
		super(message);
		this.statusCode = 0;
	}

	public APIException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

}
