package com.stacksync.android.task;

import java.util.Date;
import java.util.logging.Logger;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.util.Log;

import com.stacksync.android.MainActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.ListResponse;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.cache.CachedFile;
import com.stacksync.android.exceptions.APIException;

public class ListDirectoryTask extends MyAsyncTask<String, Integer, ListResponse> {

	// private ProgressDialog progressBar;

	private static String TAG = "ListDirectoryTask";
	private String fileId;

	public ListDirectoryTask(Context context) {
		super(context);

	}

	@Override
	protected void onPostExecute(ListResponse result) {

		if (result.getSucced()) {
			String metadata = result.getMetadata().toString();
			String lastModified = new Date().toString();
			Long size = 0L;
			Long version = 0L; // FIXME: get the real version
			String name = ""; // FIXME: get the real name
			String localPath = "";

			CachedFile cachedFile = new CachedFile(fileId, name, version, size, lastModified, metadata, localPath);
			getCacheManager().saveFileToCache(cachedFile);
		}

		((MainActivity) getContext()).onReceiveListResponse(result);
	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected ListResponse doInBackground(String... params) {

		fileId = params[0];

		StacksyncClient client = StacksyncApp.getClient(getContext());

		ListResponse result;
		try {
			boolean retry = true;
			result = client.listDirectory(fileId, retry);
		} catch (APIException e) {
			Log.e(TAG, e.getMessage(), e);			
			result = new ListResponse();
			result.setSucced(false);
			result.setStatusCode(e.getStatusCode());
			result.setMessage(e.getMessage());
		} catch (ConnectTimeoutException e){
			Log.e(TAG, e.getMessage(), e);
			result = new ListResponse();
			result.setSucced(false);
			result.setMessage("Could not connect to service");
		} catch (Exception e) {
			Log.e(TAG, "error", e);
			result = new ListResponse();
			result.setSucced(false);
			if (e == null) {
				// for some unknown reason sometimes the exception is null... 
				result.setMessage("Unknown error");
			} else if (e.getMessage() == null) {
				result.setMessage(e.toString());
			} else {
				result.setMessage(e.getMessage());
			}
		}

		return result;

	}

	protected void onProgressUpdate(Integer... progress) {
		// progressBar.setProgress(progress[0]);
	}

	@Override
	public void setProgress(int value) {
		// TODO Auto-generated method stub

	}

}