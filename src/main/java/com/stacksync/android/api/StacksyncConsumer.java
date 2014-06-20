package com.stacksync.android.api;


import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.SecureRandom;

import oauth.signpost.basic.HttpURLConnectionRequestAdapter;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import oauth.signpost.commonshttp.HttpRequestAdapter;
import oauth.signpost.http.HttpRequest;


public class StacksyncConsumer extends CommonsHttpOAuthConsumer {

    private static final long serialVersionUID = 1L;
    private SecureRandom random = new SecureRandom();

    public StacksyncConsumer(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret);
    }

    @Override
    protected HttpRequest wrap(Object request) {
        if (request instanceof org.apache.http.HttpRequest) {
            return new HttpRequestAdapter((org.apache.http.client.methods.HttpUriRequest) request);

        }
        else if (request instanceof HttpURLConnection) {
            return new HttpURLConnectionRequestAdapter((HttpURLConnection) request);
        }
        else{
            throw new IllegalArgumentException(
                    "This consumer expects requests of type "
                            + org.apache.http.HttpRequest.class.getCanonicalName());
        }


    }

    @Override
    protected String generateNonce() {
        return new BigInteger(130, random).toString(32);
    }
}

