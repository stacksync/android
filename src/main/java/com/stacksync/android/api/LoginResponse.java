package com.stacksync.android.api;

public class LoginResponse extends GenericResponse {

	private String storageUrl;
	private String authToken;

	public LoginResponse() {
		super(false, 0, "");

		this.storageUrl = "";
		this.authToken = "";
	}

	public LoginResponse(boolean succed, int statusCode, String message) {
		super(succed, statusCode, message);
		
		this.storageUrl = "";
		this.authToken = "";
	}
	
	public LoginResponse(boolean succed, int statusCode, String message, String storageUrl,
			String authToken) {
		super(succed, statusCode, message);
		
		this.storageUrl = storageUrl;
		this.authToken = authToken;
	}

	public String getStorageURL() {
		return storageUrl;
	}
	
	public void setStorageURL(String storageUrl) {
		this.storageUrl = storageUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

}
