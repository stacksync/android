package com.stacksync.android.task;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stacksync.android.SharingActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.exceptions.NoInternetConnectionException;
import com.stacksync.android.exceptions.NotLoggedInException;
import com.stacksync.android.exceptions.UnauthorizedException;
import com.stacksync.android.exceptions.UnexpectedStatusCodeException;
import com.stacksync.android.model.Member;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class FolderMembersTask extends MyAsyncTask<String, Integer, Boolean> {

    private static String TAG = FolderMembersTask.class.getName();
    private String message;
    private JSONArray members;

    public FolderMembersTask(Context context) {
        super(context);
    }

    @Override
    public void setProgress(int value) {

    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result){

            try {
                ObjectMapper mapper = new ObjectMapper();
                Member[] array = mapper.readValue(members.toString(), Member[].class);
                ArrayList<Member> membersList =  new ArrayList<Member>(Arrays.asList(array));
                ((SharingActivity) getContext()).onGetFolderMembersResult(membersList);
            } catch (IOException e) {
                Log.e(TAG, "Error mapping result to object. " + e.toString(), e);
                Toast.makeText(getContext(), "Error mapping result to object. " + e.toString(), Toast.LENGTH_LONG);
            }

        }else{
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String folderId = params[0];
        boolean result = false;
        StacksyncClient client = StacksyncApp.getClient(getContext());

        try {
            members = client.getFolderMembers(folderId);
            result = true;

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
        } catch (JSONException e) {
            Log.e(TAG, e.toString(), e);
            message = e.toString();
        }

        return result;
    }

}
