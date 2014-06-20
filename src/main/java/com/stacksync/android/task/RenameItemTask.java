package com.stacksync.android.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.stacksync.android.MainActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;

import org.json.JSONException;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 * Created by edgar on 20/06/14.
 */
public class RenameItemTask extends AsyncTask<String, Integer, Boolean> {

    private static String TAG = RenameItemTask.class.getName();
    private Context context;
    private String message;
    private ProgressDialog progressBar;

    public RenameItemTask(Context context) {
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
        progressBar.setMessage("Renameing Item...");
        progressBar.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String itemName = params[0];
        String itemId = params[1];
        boolean isFolder = Boolean.parseBoolean(params[2]);
        boolean result = false;

        StacksyncClient client = StacksyncApp.getClient(context);
        try {
            client.rename(itemId, itemName, isFolder);
            if (isFolder) {
                message = "Folder successfully renamed";
            }else{
                message = "File successfully renamed";
            }
        } catch (NoInternetConnectionException e) {
            Log.e(TAG, e.toString(), e);
            message = "Please check your internet connection";
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (OAuthMessageSignerException e) {
            e.printStackTrace();
        } catch (OAuthCommunicationException e) {
            e.printStackTrace();
        } catch (UnexpectedStatusCodeException e) {
            Log.e(TAG, e.toString(), e);
            message = "Error renaming item. " + e.toString();
        } catch (OAuthExpectationFailedException e) {
            e.printStackTrace();
        } catch (NotLoggedInException e) {
            Log.e(TAG, e.toString(), e);
            message = "Not connected to the service";
        } catch (UnauthorizedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
            message = "Error renaming item. " + e.toString();
        }

        return result;
    }
}
