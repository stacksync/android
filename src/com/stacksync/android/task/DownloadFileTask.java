package com.stacksync.android.task;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.stacksync.android.MainActivity;
import com.stacksync.android.R;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileTask extends MyAsyncTask<String, Integer, Boolean> {

	private static String TAG = DownloadFileTask.class.getName();
	private String message;
	private String filename;
	private String mimetype;
	private File localFile;
	private NotificationManager mNotifyManager;
	private NotificationCompat.Builder mBuilder;
	private int NOTIFICATION_ID;

	public DownloadFileTask(Context context) {
		super(context);
		Random random = new Random();
		NOTIFICATION_ID = random.nextInt();
	}

	@Override
	protected void onPostExecute(Boolean result) {

		mNotifyManager.cancel(NOTIFICATION_ID);

		if (result) {

			if (localFile.exists()) {

				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(localFile), mimetype);

				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
						.setSmallIcon(R.drawable.logo).setContentTitle(filename)
						.setContentText("Download completed. Click to open.").setAutoCancel(true);

				TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(intent);

				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
						PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
				// mId allows you to update the notification later on.
				Random random = new Random();
				mNotifyManager.notify(random.nextInt(), mBuilder.build());

			} else {
				Toast.makeText(getContext(), "File not found", Toast.LENGTH_LONG).show();
			}

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

		mBuilder.setContentTitle("Downloading file").setContentText("Download in progress")
				.setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true)
				.setContentIntent(contentIntent).setProgress(100, 0, true);

		mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	@Override
	protected Boolean doInBackground(String... params) {

		String fileId = params[0];
		// Long version = Long.parseLong(params[1]);
		filename = params[2];
		mimetype = params[3];
		String downloadDir = params[4];

		boolean result = false;

		StacksyncClient client = StacksyncApp.getClient(getContext());

		localFile = new File(downloadDir, filename);

		try {

			client.downloadFileSmart(this, fileId, localFile.getAbsolutePath());
			result = true;
			message = "File downloaded successfully";

		} catch (NoInternetConnectionException e) {
			message = "Please check your internet connection";
			Log.e(TAG, e.toString(), e);
		} catch (IOException e) {
			message = "Error downloading file. " + e.toString();
			Log.e(TAG, e.toString(), e);
		} catch (NotLoggedInException e) {
			// TODO Handle not logged in exception
			message = "Not connected to the service";
			Log.e(TAG, e.toString(), e);
		} catch (UnexpectedStatusCodeException e) {
			message = "Error downloading file. " + e.toString();
			Log.e(TAG, e.toString(), e);
		} catch (UnauthorizedException e) {
			Log.e(TAG, e.toString(), e);
			message = e.toString();
		}

		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {

		if (progress[0] >= 0 && progress[0] <= 100) {
			mBuilder.setProgress(100, progress[0], false);
			mBuilder.setContentText(progress[0] + "%");
			mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
		}
	}

	@Override
	public void setProgress(int value) {
		publishProgress(value);

	}

}