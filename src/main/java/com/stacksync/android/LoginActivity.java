package com.stacksync.android;

import com.stacksync.android.api.LoginResponse;
import com.stacksync.android.task.LoginTask;
import com.stacksync.android.utils.Constants;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private String apiUrl;
	private String email;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		Button btnLogin = (Button) findViewById(R.id.loginButton);
		
		btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				apiUrl = ((EditText) findViewById(R.id.loginHost)).getText().toString();
				email = ((EditText) findViewById(R.id.loginUser)).getText().toString();
				password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();
				
				if (Utils.validateLoginFields(apiUrl, email, password)) {

                    AsyncTask<String, Integer, LoginResponse> loginTask = new LoginTask(LoginActivity.this);
					loginTask.execute(apiUrl, email, password);

				} else {
					Toast.makeText(LoginActivity.this, "Please, fill all fields properly.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}


	public void onReceiveLoginResponse(LoginResponse loginResponse) {

		if (loginResponse.getSucced()) {

			SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("access_token_key", loginResponse.getAccessTokenKey());
			editor.putString("access_token_secret", loginResponse.getAccessTokenSecret());
            editor.putString("email", email);
            editor.putString("api_url", apiUrl);


			editor.commit();
			
			Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
			LoginActivity.this.startActivity(myIntent);
			finish();
		} else {
			Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
}
