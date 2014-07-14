package com.stacksync.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.CancelledTaskException;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

import org.json.JSONException;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;


public class ShareFolderTask  extends MyAsyncTask<String, Integer, Boolean> {

    private static String TAG = ShareFolderTask.class.getName();
    private String message;
    private ProgressDialog progressDialog;

    public ShareFolderTask(Context context) {
        super(context);
    }

    @Override
    protected void onPostExecute(Boolean result) {

        this.progressDialog.cancel();

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onPreExecute() {
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage("Sharing folder...");
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setMax(100);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {

        Boolean result = false;
        String folderId = params[0];
        String email = params[1];

        StacksyncClient client = StacksyncApp.getClient(getContext());

        try {
            client.shareFolder(folderId, email);
            result = true;
            message = "Folder shared successfully";

        } catch (NoInternetConnectionException e) {
            message = "Please check your internet connection";
            Log.e(TAG, e.toString(), e);
        } catch (IOException e) {
            message = "Error sharing folder. " + e.toString();
            Log.e(TAG, e.toString(), e);
        } catch (NotLoggedInException e) {
            message = "Not connected to the service";
            Log.e(TAG, e.toString(), e);
        } catch (UnexpectedStatusCodeException e) {
            message = "Error sharing folder. " + e.toString();
            Log.e(TAG, e.toString(), e);
        } catch (UnauthorizedException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        } catch (CancelledTaskException e) {
            Log.w(TAG, e.toString(), e);
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

    @Override
    protected void onProgressUpdate(Integer... progress) {

        if (progress[0] >= 0 && progress[0] <= 100) {
            super.onProgressUpdate(progress);
            this.progressDialog.setProgress(progress[0]);
        }
    }

    @Override
    public void setProgress(int value) {

        if (this.progressDialog.isIndeterminate()) {
            this.progressDialog.setIndeterminate(false);
        }

        publishProgress(value);
    }

}