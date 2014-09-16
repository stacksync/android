package com.stacksync.android;

import com.stacksync.android.api.LoginResponse;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.task.LoginTask;
import com.stacksync.android.utils.Constants;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends Activity implements AdapterView.OnItemSelectedListener {

	private String authUrl;
	private String username;
	private String password;
    private int previousSpinnerPosition = 99;

    private static String TISSAT_URL = "https://www.k.nefeles.es:5000/v3";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		Button btnLogin = (Button) findViewById(R.id.loginButton);
		
		btnLogin.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

                Spinner spinner = (Spinner) findViewById(R.id.hostSpinner);
                int position = spinner.getSelectedItemPosition();
                if (position == 0){
                    authUrl = TISSAT_URL;
                }
                else {
                    authUrl = ((EditText) findViewById(R.id.loginHost)).getText().toString();
                }

				username = ((EditText) findViewById(R.id.loginUser)).getText().toString();
				password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();
				
				if (Utils.validateLoginFields(authUrl, username, password)) {

					StacksyncClient client = StacksyncApp.getClient(LoginActivity.this);
					client.setAuthUrl(authUrl);
					client.setUsername(username);
					client.setPassword(password);
					
					AsyncTask<String, Integer, LoginResponse> loginTask = new LoginTask(
							LoginActivity.this);
					loginTask.execute();

				} else {
					Toast.makeText(LoginActivity.this, "Please, fill all fields properly.",
							Toast.LENGTH_LONG).show();
				}
			}
		});

        Spinner spinner = (Spinner) findViewById(R.id.hostSpinner);
        spinner.setOnItemSelectedListener(this);
	}


	public void onReceiveLoginResponse(LoginResponse loginResponse) {

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (position == previousSpinnerPosition)
            return;

        previousSpinnerPosition = position;

        if (position == 0){
            //Tissat
            EditText hostText = (EditText) findViewById(R.id.loginHost);
            hostText.setText("Tissat's server");
            hostText.setEnabled(false);

        }
        else
        {
            //Custom
            EditText hostText = (EditText) findViewById(R.id.loginHost);
            hostText.setText("");
            hostText.setHint("Set your URL");
            hostText.setEnabled(true);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
