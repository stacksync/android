package com.stacksync.android;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

public class Utils {

	private static String TAG = "Utils";

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	public static String getApplicationName(Context context) {
		// FIXME: this function must return the actual application name,
		// NOT the package name

		String name = "StackSync";

		return name;
	}

	public static String getReadableFileSize(long size) {
		if (size <= 0)
			return "0 Bytes";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " "
				+ units[digitGroups];
	}

	public static File getCacheDirectory(Context context) {

		File cacheDir;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			cacheDir = context.getExternalFilesDir(null);
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.e(TAG, "Media is read-only. Using internal storage.");

			// FIXME: files saved in this directory cannot be accessed by other
			// apps
			cacheDir = context.getFilesDir();
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Log.e(TAG, "Media is not writeable nor read-only. Using internal storage.");
			cacheDir = context.getFilesDir();
		}

		return cacheDir;
	}

	public static String buildUrl(String url, String path, List<Pair<String, String>> params) {
		Uri uri = Uri.parse(url);
		Uri.Builder uriBuilder = uri.buildUpon();
		uriBuilder.path(uri.getPath() + path);

		for (Pair<String, String> param : params) {

			uriBuilder.appendQueryParameter(param.first, param.second);
		}

		return uriBuilder.build().toString();
	}

	public static Date convertStringToDate(String sDate) {

		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(sDate);
		} catch (ParseException e) {
			date = new Date(Long.MIN_VALUE);
		}

		return date;
	}

	public static boolean validateTokenFields(String accessTokenKey, String accessTokenSecret) {

		if (accessTokenKey == null || accessTokenSecret == null) {
			return false;
		} else if (accessTokenKey == "") {
			return false;
		} else if (accessTokenSecret == "") {
			return false;
		}

		return true;
	}

    public static boolean validateLoginFields(String apiUrl, String email, String password) {

        if (apiUrl == null || email == null || password == null) {
            return false;
        } else if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
            return false;
        } else if (email == "") {
            return false;
        } else if (password == "") {
            return false;
        }

        return true;
    }
}
