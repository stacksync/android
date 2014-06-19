package com.stacksync.android;

import com.stacksync.android.api.GenericResponse;
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

	private String authUrl;
	private String username;
	private String password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		Button btnLogin = (Button) findViewById(R.id.loginButton);
		
		btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				authUrl = ((EditText) findViewById(R.id.loginHost)).getText().toString();
				username = ((EditText) findViewById(R.id.loginUser)).getText().toString();
				password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();
				
				if (Utils.validateLoginFields(authUrl, username, password)) {


					AsyncTask<String, Integer, GenericResponse> loginTask = new LoginTask(LoginActivity.this);
					loginTask.execute();

				} else {
					Toast.makeText(LoginActivity.this, "Please, fill all fields properly.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}


	public void onReceiveRequestTokenResponse(GenericResponse loginResponse) {

		if (loginResponse.getSucced()) {






			SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("auth_url", authUrl);
			editor.putString("username", username);
			editor.putString("password", password);
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
