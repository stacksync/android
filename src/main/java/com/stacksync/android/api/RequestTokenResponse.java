package com.stacksync.android.api;

/**
 * Created by edgar on 17/06/14.
 */
public class RequestTokenResponse extends GenericResponse {

    private String requestToken;
    private String requestTokenSecret;


    public RequestTokenResponse(boolean succed, int statusCode, String message) {
        super(succed, statusCode, message);
    }

    public RequestTokenResponse() {
        super(false, 0, "");
        this.requestToken = "";
        this.requestTokenSecret = "";
    }


    public RequestTokenResponse(boolean succed, int statusCode, String message, String requestToken,
                                String requestTokenSecret) {

        super(succed, statusCode, message);
        this.requestToken = requestToken;
        this.requestTokenSecret = requestTokenSecret;

    }

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getRequestTokenSecret() {
        return requestTokenSecret;
    }

    public void setRequestTokenSecret(String requestTokenSecret) {
        this.requestTokenSecret = requestTokenSecret;
    }
}
