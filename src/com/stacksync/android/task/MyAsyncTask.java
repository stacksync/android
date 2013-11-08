package com.stacksync.android.task;

import com.stacksync.android.cache.CacheManager;

import android.content.Context;
import android.os.AsyncTask;

public abstract class MyAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private Context context;
	private CacheManager cacheManager;
	
	public MyAsyncTask(Context context){
		this.context = context;
		this.cacheManager = CacheManager.getInstance(context);
	}
	
	public Context getContext(){
		return context;
	}
	
	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public abstract void setProgress(int value);
}
