package com.stacksync.android.task;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.stacksync.android.MainActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class CreateFolderTask extends AsyncTask<String, Integer, Boolean> {

	private static String TAG = CreateFolderTask.class.getName();
	private Context context;
	private String message;
	private ProgressDialog progressBar;

	public CreateFolderTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progressBar.dismiss();
		
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		
		if(result){
			((MainActivity)context).listDirectory(); 
		}
	}

	@Override
	protected void onPreExecute() {
		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(true);
		progressBar.setMessage("Creating folder...");
		progressBar.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {

		String folderName = params[0];
		String parent = params[1];
		boolean result = false;

		StacksyncClient client = StacksyncApp.getClient(context);

		try {
			client.createFolder(folderName, parent);
			result = true;
			message = "Folder successfully created";
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString(), e);
			message = e.toString();
		} catch (NoInternetConnectionException e) {
			Log.e(TAG, e.toString(), e);
			message = "Please check your internet connection";
		} catch (NotLoggedInException e) {
			Log.e(TAG, e.toString(), e);
			message = "Not connected to the service";
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
			message = "Error creating folder. " + e.toString();
		} catch (UnexpectedStatusCodeException e) {
			Log.e(TAG, e.toString(), e);
			message = "Error creating folder. " + e.toString();
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
        } catch (JSONException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        }

        return result;
	}
}