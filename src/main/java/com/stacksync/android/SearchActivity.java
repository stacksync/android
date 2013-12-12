package com.stacksync.android;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

@TargetApi(11)
public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
	ProgressDialog progressDialog = null;
	String query = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
			// actionBar.setDisplayShowTitleEnabled(false);
		}
		
		// Get the intent, verify the action and get the query
		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
			// doMySearch(query);
			
			Log.w(TAG, "Search not implemented");

			/*
			 * progressDialog = ProgressDialog.show(this, "", "Loading...");
			 * 
			 * new Thread() { public void run() {
			 * 
			 * try { sleep(5000);
			 * 
			 * } catch (Exception e) { Log.e(TAG, e.toString()); } // dismiss
			 * the progressdialog progressDialog.dismiss(); } }.start();
			 */

			RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.results_layout);

			TextView resultText = new TextView(this);
			resultText.setText("Not implemented");
			resultText.setId((int) System.currentTimeMillis());

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL, resultText.getId());
			lp.addRule(RelativeLayout.CENTER_VERTICAL, resultText.getId());

			relativeLayout.addView(resultText, lp);
		} else {
			// no query -> end activity
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			super.onBackPressed();
			return true;
		} else {
			return false;
		}

	}
}
