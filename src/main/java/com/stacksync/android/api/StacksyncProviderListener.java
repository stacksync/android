package com.stacksync.android.api;

import oauth.signpost.OAuthProviderListener;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;

public class StacksyncProviderListener implements OAuthProviderListener {
    @Override
    public void prepareRequest(HttpRequest httpRequest) throws Exception {
        httpRequest.setHeader("StackSync-API", "v2");
    }

    @Override
    public void prepareSubmission(HttpRequest httpRequest) throws Exception {

    }

    @Override
    public boolean onResponseReceived(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        return false;
    }
};