package com.stacksync.android;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;

import com.stacksync.android.task.DeleteFileTask;

public class FileManager {

	private static final String TAG = "FILEMANAGER";
	private Activity activity;
	private ArrayList<ArrayList<HashMap<String, Object>>> content;
	private ArrayList<HashMap<String, String>> groupList;

	public FileManager(Activity a) {
		activity = a;
		content = new ArrayList<ArrayList<HashMap<String, Object>>>();
		groupList = new ArrayList<HashMap<String, String>>();
	}

	public ArrayList<ArrayList<HashMap<String, Object>>> getContent() {
		return content;
	}

	public ArrayList<HashMap<String, String>> getGroupList() {
		return groupList;
	}

	public boolean hasContent() {
		return !content.isEmpty();
	}

	public void clearContent() {
		content.clear();
		updateGroupList();
	}

	public String updateContent(JSONObject data) {

		String message = "No text";

		// synchronized (content) {

		clearContent();

		String name, file_id, path, status, mimetype;
		Long size;
		Boolean isFolder;
		String clientModified;
		long currentTimeMillis = System.currentTimeMillis();

		ArrayList<HashMap<String, Object>> secFolders = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> secFiles = new ArrayList<HashMap<String, Object>>();

		try {
			JSONObject metadata = (JSONObject) data;

			if (metadata.has("contents")) {

				JSONArray contents = metadata.getJSONArray("contents");

				for (int i = 0; i < contents.length(); i++) {

					JSONObject jArrayObject = contents.getJSONObject(i);

					file_id = jArrayObject.getString("file_id");
					name = jArrayObject.getString("filename");
					clientModified = jArrayObject.getString("client_modified");
					Date dateClientModified = Utils.convertStringToDate(clientModified);

					CharSequence formatedClientModified = "modified "
							+ DateUtils.getRelativeTimeSpanString(dateClientModified.getTime(),
									currentTimeMillis, 0, DateUtils.FORMAT_ABBREV_ALL);

					if (jArrayObject.has("size")) {
						size = jArrayObject.getLong("size");
					} else {
						size = 0L;
					}

					isFolder = jArrayObject.getBoolean("is_folder");
					path = jArrayObject.getString("path");
					status = jArrayObject.getString("status");

					if (jArrayObject.has("mimetype")) {
						mimetype = jArrayObject.getString("mimetype");
					} else {
						mimetype = "binary/octet-stream";
					}

					// TODO: Check whether deleted files have to be
					// shown or
					// not
					if (status.compareToIgnoreCase("DELETED") != 0) {
						// || FilesConstants.SHOW_DELETED) {

						HashMap<String, Object> child = new HashMap<String, Object>();
						child.put(MainActivity.KEY_FILEID, file_id);
						child.put(MainActivity.KEY_FILENAME, name);
						child.put(MainActivity.KEY_PATH, path);
						child.put(MainActivity.KEY_MIMETYPE, mimetype);

						if (isFolder) {
							child.put(MainActivity.KEY_FILEINFO, formatedClientModified);
							child.put(MainActivity.KEY_ICON,
									activity.getResources().getDrawable(R.drawable.folder));
							child.put(MainActivity.KEY_ISFOLDER, true);
							secFolders.add(child);
						} else {
							child.put(MainActivity.KEY_FILEINFO, Utils.getReadableFileSize(size)
									+ ", " + formatedClientModified);
							
							int drawableId;
							String extension = FilenameUtils.getExtension(name);
							try {
							    Class res = R.drawable.class;
							    Field field = res.getField("file_extension_" + extension);
							    drawableId = field.getInt(field);
								//drawableId = activity.getResources().getIdentifier("file_extension_" + extension + ".png", "drawable", "com.stacksync.android");
							}
							catch (Exception e) {
							    drawableId = R.drawable.file_unknown;
							}
							
							child.put(MainActivity.KEY_ICON,
									activity.getResources().getDrawable(drawableId));
							child.put(MainActivity.KEY_ISFOLDER, false);
							secFiles.add(child);
						}
					}
				}
			} else {
				Log.e(TAG, "Response had no field \"contents\".");
				message = "No contents";
			}

		} catch (JSONException e) {
			Log.e(TAG, e.toString());
			message = e.toString();
		}

		if (!secFolders.isEmpty()) {
			content.add(secFolders);
		}

		if (!secFiles.isEmpty()) {
			content.add(secFiles);
		}

		if (content.isEmpty()) {
			message = "Empty folder";
		}

		updateGroupList();

		return message;
	}

	private void updateGroupList() {

		groupList.clear();
		int size = content.size();

		if (size == 1) {
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("Group Item", "FILES"); // the key and it's value.
			groupList.add(m);
		} else if (size == 2) {
			HashMap<String, String> m = new HashMap<String, String>();
			m.put("Group Item", "FOLDERS"); // the key and it's value.
			groupList.add(m);

			m = new HashMap<String, String>();
			m.put("Group Item", "FILES"); // the key and it's value.
			groupList.add(m);
		}
	}

	public Boolean deleteFile(String path, String filename) {

		boolean result = false;

		AsyncTask<String, Integer, Boolean> deleteFile = new DeleteFileTask(activity);
		deleteFile.execute(path, filename);

		try {
			result = deleteFile.get(20, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			Log.e(TAG, e.toString());
		} catch (ExecutionException e) {
			Log.e(TAG, e.toString());
		} catch (TimeoutException e) {
			Log.e(TAG, e.toString());
		}

		return result;
	}
}
