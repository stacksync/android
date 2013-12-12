package com.stacksync.android.api;

public class GenericResponse {

	private boolean succed;
	private int statusCode;
	private String message;
	
	public GenericResponse(boolean succed, int statusCode, String message){
		
		this.succed = succed;
		this.statusCode = statusCode;
		this.message = message;
	}

	public boolean getSucced() {
		return succed;
	}

	public void setSucced(boolean succed) {
		this.succed = succed;
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
}
