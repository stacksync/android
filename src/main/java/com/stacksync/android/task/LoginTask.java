package com.stacksync.android.task;

import com.stacksync.android.LoginActivity;
import com.stacksync.android.MainActivity;
import com.stacksync.android.StacksyncApp;
import com.stacksync.android.api.GenericResponse;
import com.stacksync.android.api.LoginResponse;
import com.stacksync.android.api.StacksyncClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class LoginTask extends AsyncTask<String, Integer, LoginResponse> {

	private static String TAG = LoginTask.class.getName();
	private Context context;
	private ProgressDialog progressBar;

	public LoginTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPostExecute(LoginResponse result) {
		progressBar.dismiss();

		if (context instanceof MainActivity) {
			((MainActivity) context).onReceiveLoginResponse(result);
		} else if (context instanceof LoginActivity) {
			((LoginActivity) context).onReceiveLoginResponse(result);
		} else {
			Toast.makeText(context, "Wow! Who the hell are you?", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPreExecute() {
		progressBar = new ProgressDialog(context);
		progressBar.setCancelable(false);
		progressBar.setMessage("Logging in...");
		progressBar.show();
	}

	@Override
	protected LoginResponse doInBackground(String... params) {

        String apiUrl = params[0];
        String email = params[1];
        String password = params[2];

		StacksyncClient client = StacksyncApp.getClient(context);

		LoginResponse result;

		try {
            client.initOauth(apiUrl);
			client.getRequestToken();
            client.authorize(email, password);
            client.getAccessToken();

            result = new LoginResponse();
            result.setSucced(true);
            result.setAccessTokenKey(client.getConsumer().getToken());
            result.setAccessTokenSecret(client.getConsumer().getTokenSecret());

		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
			result = new LoginResponse();
			result.setSucced(false);
			result.setStatusCode(-1);
			if (e.getMessage() != null)
				result.setMessage(e.getMessage());
			else
				result.setMessage(e.toString());
		}

		return result;
	}

	protected void onProgressUpdate(Integer... progress) {
		progressBar.setProgress(progress[0]);
	}

}