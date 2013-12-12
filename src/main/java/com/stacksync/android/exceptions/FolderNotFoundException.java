package com.stacksync.android.exceptions;

public class FolderNotFoundException extends APIException {

	private static final long serialVersionUID = -4659557598238750176L;

	public FolderNotFoundException() {
		super();
	}

	public FolderNotFoundException(String message) {
		super(message);
	}
	
	public FolderNotFoundException(String message, int statusCode){
		super(message, statusCode);
	}

}
