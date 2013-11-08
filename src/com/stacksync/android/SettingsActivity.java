package com.stacksync.android;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;

import com.stacksync.android.cache.CacheManager;
import com.stacksync.android.utils.Constants;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	
	
	private Preference mButtonServerUrl;
	private Preference mButtonUsername;
	private Preference mButtonLogout;
	private Preference mButtonAppVersion;
	private Preference mButtonCacheSize;
	private Preference mButtonCacheClear;
	private ListPreference mButtonCacheLimit;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}

		mButtonServerUrl = (Preference) findPreference("server_url");
		mButtonUsername = (Preference) findPreference("username");
		mButtonLogout = (Preference) findPreference("logout");

		mButtonAppVersion = (Preference) findPreference("appversion");
		mButtonCacheSize = (Preference) findPreference("cache_size");
		mButtonCacheClear = (Preference) findPreference("cache_clear");
		mButtonCacheLimit = (ListPreference) findPreference("cache_limit");
		mButtonCacheLimit.setOnPreferenceChangeListener(this);

		setServerUrl();
		setUsername();
		setAppVersion();
		setCacheSize();
		setCacheLimit(CacheManager.getInstance(this).getCacheLimit());
	}

	private void setServerUrl() {

		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		String serverUrl = settings.getString("auth_url", "Not set");

		mButtonServerUrl.setSummary(serverUrl);
	}

	private void setUsername() {

		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		String username = settings.getString("username", "Not set");

		mButtonUsername.setSummary(username);
	}

	private void setAppVersion() {

		String version;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "Unknown";
		}

		mButtonAppVersion.setSummary(version);
	}

	private void setCacheSize() {
		CacheManager cacheManager = CacheManager.getInstance(this);

		if (cacheManager.getCacheLimit() == 0) {
			mButtonCacheSize.setSummary("Disabled");
		} else {
			String cacheSize = Utils.getReadableFileSize(cacheManager.getCacheSize());
			mButtonCacheSize.setSummary("Currently using " + cacheSize);
		}

	}

	private void setCacheLimit(long limit) {
		CacheManager cacheManager = CacheManager.getInstance(this);
		cacheManager.setCacheLimit(limit);
		String textLimit = Utils.getReadableFileSize(limit);

		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("cache_limit", limit);
		editor.commit();

		mButtonCacheLimit.setDefaultValue(limit);

		if (limit == 0) {
			mButtonCacheLimit.setSummary("Cache is disabled");
		} else {
			mButtonCacheLimit.setSummary("Limit set to " + textLimit);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

		if (preference == mButtonCacheClear) {
			CacheManager cacheManager = CacheManager.getInstance(this);
			cacheManager.clearCache();
			setCacheSize();
			Toast.makeText(this, "Cache cleared", Toast.LENGTH_LONG).show();
			return true;
		}
		else if (preference == mButtonLogout) {
			CacheManager cacheManager = CacheManager.getInstance(this);
			cacheManager.clearCache();
			setResult(Constants.RESULT_LOGOUT);
			finish();
			return true;
		}

		return false;
	}
	


	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mButtonCacheLimit) {
			setCacheLimit(Long.parseLong((String) newValue));
			return true;
		}

		return false;
	}
}