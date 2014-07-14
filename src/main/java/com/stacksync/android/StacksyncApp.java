package com.stacksync.android;

import android.content.Context;

import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.utils.Constants;

public class StacksyncApp {

	private static StacksyncClient client;

	public static StacksyncClient getClient(Context context) {

		if (client == null) {
			Context appContext = context.getApplicationContext();
			client = new StacksyncClient(appContext, Constants.DEV_MODE);
		}

		return client;
	}

}