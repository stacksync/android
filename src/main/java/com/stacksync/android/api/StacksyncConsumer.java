package com.stacksync.android.api;


import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.SecureRandom;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.OAuthProviderListener;
import oauth.signpost.basic.HttpURLConnectionRequestAdapter;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;


public class StacksyncConsumer extends AbstractOAuthConsumer {

    private static final long serialVersionUID = 1L;
    private SecureRandom random = new SecureRandom();

    public StacksyncConsumer(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret);
    }

    @Override
    protected HttpRequest wrap(Object request) {
        if (!(request instanceof HttpURLConnection)) {
            throw new IllegalArgumentException(
                    "The default consumer expects requests of type java.net.HttpURLConnection");
        }
        return new HttpURLConnectionRequestAdapter((HttpURLConnection) request);
    }

    @Override
    protected String generateNonce() {
        return new BigInteger(130, random).toString(32);
    }
}

