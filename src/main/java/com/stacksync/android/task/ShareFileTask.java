package com.stacksync.android.task;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.cache.CachedFile;
import com.stacksync.android.exceptions.FileNotCachedException;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class ShareFileTask extends MyAsyncTask<String, Integer, Boolean> {

	private static String TAG = ShareFileTask.class.getName();
	private String message;
	private File localFile;
	private String mimetype;
	private ProgressDialog progressDialog;

	public ShareFileTask(Context context) {
		super(context);
	}

	@Override
	protected void onPostExecute(Boolean result) {

		this.progressDialog.cancel();

		if (result) {

			if (localFile.exists()) {

				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType(mimetype);
				share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(localFile));

				try {
					getContext().startActivity(Intent.createChooser(share, "Share file"));
				} catch (ActivityNotFoundException e) {
					// TODO: check this before downloading the file
					Log.e(TAG, e.toString(), e);
					Toast.makeText(getContext(), "No app found to open this file",
							Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(getContext(), "File not found", Toast.LENGTH_LONG).show();
			}

		} else {
			Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		this.progressDialog = new ProgressDialog(getContext());
		this.progressDialog.setMessage("Downloading file...");
		this.progressDialog.setIndeterminate(false);
		this.progressDialog.setMax(100);
		this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.progressDialog.setCancelable(false);
		this.progressDialog.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {

		String fileId = params[0];
		Long version = Long.parseLong(params[1]);
		String filename = params[2];
		mimetype = params[3];

		try {
		    localFile = getCacheManager().getFileFromCache(fileId, version);
			return true;
		} catch (FileNotCachedException e) {
		}

		boolean result = false;

		localFile = new File(getCacheManager().getCacheDir(), filename);

		StacksyncClient client = StacksyncApp.getClient(getContext());

		try {
			client.downloadFileSmart(this, fileId, localFile.getAbsolutePath());
			result = true;
			message = "File downloaded successfully";
			String lastModified = new Date().toString();

			CachedFile cachedFile = new CachedFile(fileId, filename, version, 0L, lastModified, "",
					localFile.getAbsolutePath());

			getCacheManager().saveFileToCache(cachedFile);

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
		} catch (OAuthExpectationFailedException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        } catch (OAuthCommunicationException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        } catch (OAuthMessageSignerException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        }

        return result;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {

		if (progress[0] >= 0 && progress[0] <= 100) {
			super.onProgressUpdate(progress);
			this.progressDialog.setProgress(progress[0]);
		}
	}

	@Override
	public void setProgress(int value) {
		publishProgress(value);

	}

}