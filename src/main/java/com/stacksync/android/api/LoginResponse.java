package com.stacksync.android.api;

public class LoginResponse extends GenericResponse {

	private String accessTokenKey;
	private String accessTokenSecret;

	public LoginResponse() {
		super(false, 0, "");

		this.accessTokenKey = "";
		this.accessTokenSecret = "";
	}

	public LoginResponse(boolean succeed, int statusCode, String message) {
		super(succeed, statusCode, message);
		
		this.accessTokenKey = "";
		this.accessTokenSecret = "";
	}

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getAccessTokenKey() {
        return accessTokenKey;
    }

    public void setAccessTokenKey(String accessTokenKey) {
        this.accessTokenKey = accessTokenKey;
    }
}
