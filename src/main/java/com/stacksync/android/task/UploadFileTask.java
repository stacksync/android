package com.stacksync.android.task;

import java.io.IOException;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.stacksync.android.MainActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

public class UploadFileTask extends MyAsyncTask<Object, Integer, Boolean> {

	private static String TAG = UploadFileTask.class.getName();
	private String message;
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	private int NOTIFICATION_ID;

	public UploadFileTask(Context context) {
		super(context);
		Random random = new Random();
		NOTIFICATION_ID = random.nextInt();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mNotifyManager.cancel(NOTIFICATION_ID);

		if (result) {
			Toast.makeText(getContext(), "File uploaded successfully", Toast.LENGTH_LONG).show();
			((MainActivity) getContext()).listDirectory();
		} else {
			Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		// Build the notification using Notification.Builder

		mNotifyManager = (NotificationManager) getContext().getSystemService(
				Context.NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(getContext());

		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, new Intent(),
				PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentTitle("Uploading file").setContentText("Upload in progress")
				.setSmallIcon(android.R.drawable.stat_sys_upload).setOngoing(true)
				.setContentIntent(contentIntent).setProgress(100, 0, true);

		mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	protected Boolean doInBackground(Object... params) {
				
		Uri selectedFile = (Uri) params[0];
		String parentId = (String) params[1];
		boolean result = false;

		StacksyncClient client = StacksyncApp.getClient(getContext());

		try {
			client.uploadFileSmart(this, selectedFile, parentId);
			result = true;
			message = "File uploaded successfully";

		} catch (NoInternetConnectionException e) {
			message = "Please check your internet connection";
			Log.e(TAG, e.toString(), e);
		} catch (IOException e) {
			message = "Error uploading file. " + e.toString();
			Log.e(TAG, e.toString(), e);
		} catch (NotLoggedInException e) {
			// TODO Handle not logged in exception
			message = "Not connected to the service";
			Log.e(TAG, e.toString(), e);
		} catch (UnexpectedStatusCodeException e) {
			message = "Error uploading file. " + e.toString();
			Log.e(TAG, e.toString(), e);
		} catch (UnauthorizedException e) {
			Log.e(TAG, e.toString(), e);
			message = e.toString();
		} catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        }

		return result;
	}

	protected void onProgressUpdate(Integer... progress) {
		if (progress[0] >= 0 && progress[0] < 100) {
			mBuilder.setProgress(100, progress[0], false);
			mBuilder.setContentText(progress[0] + "%");
			mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
		} else if (progress[0] >= 100) {
			mBuilder.setProgress(100, 100, true);
			mBuilder.setContentText("Finishing upload...");
			mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
		}
	}

	@Override
	public void setProgress(int value) {
		publishProgress(value);
	}

}