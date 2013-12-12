package com.stacksync.android.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CacheHelper extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "stacksync";

	// Contacts table name
	private static final String TABLE_CACHE_FILES = "cache";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_VERSION = "version";
	private static final String KEY_SIZE = "size";
	private static final String KEY_LAST_MODIFIED = "last_modified";
	private static final String KEY_METADATA = "metadata";
	private static final String KEY_LOCAL_PATH = "local_path";

	public CacheHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CACHE_FILES + "(" + KEY_ID
				+ " VARCHAR(150) PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_VERSION + " INTEGER,"
				+ KEY_SIZE + " INTEGER," + KEY_LAST_MODIFIED + " TEXT," + KEY_METADATA + " TEXT, "
				+ KEY_LOCAL_PATH + " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHE_FILES);

		// Create tables again
		onCreate(db);
	}

	public boolean existsFile(String id) {

		boolean exists = false;
		
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CACHE_FILES, new String[] { KEY_ID }, KEY_ID + "=?",
				new String[] { (id == null) ? "0" : id }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			exists = (cursor.getCount() > 0);
			cursor.close();
		}

		return exists;
	}

	public void updateFile(CachedFile file) {
		SQLiteDatabase db = this.getWritableDatabase();

		String strFilter = "id=" + ((file.getId() == null) ? "0" : file.getId());

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, file.getName());
		values.put(KEY_VERSION, file.getVersion());
		values.put(KEY_SIZE, file.getSize());
		values.put(KEY_LAST_MODIFIED, file.getLastModified().toString());
		values.put(KEY_METADATA, file.getMetadata());
		values.put(KEY_LOCAL_PATH, file.getLocalPath());

		db.update(TABLE_CACHE_FILES, values, strFilter, null);
		db.close();
	}

	public void addFile(CachedFile file) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, (file.getId() == null) ? "0" : file.getId());
		values.put(KEY_NAME, file.getName());
		values.put(KEY_VERSION, file.getVersion());
		values.put(KEY_SIZE, file.getSize());
		values.put(KEY_LAST_MODIFIED, file.getLastModified().toString());
		values.put(KEY_METADATA, file.getMetadata());
		values.put(KEY_LOCAL_PATH, file.getLocalPath());

		db.insert(TABLE_CACHE_FILES, null, values);
		db.close();
	}

	public CachedFile getFile(String id) {

		CachedFile file = null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CACHE_FILES, new String[] { KEY_ID, KEY_NAME, KEY_VERSION,
				KEY_SIZE, KEY_LAST_MODIFIED, KEY_METADATA, KEY_LOCAL_PATH }, KEY_ID + "=?",
				new String[] { (id == null) ? "0" : id }, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();

			if (cursor.getCount() > 0) {
				file = new CachedFile(cursor.getString(0), cursor.getString(1), cursor.getLong(2),
						cursor.getLong(3), cursor.getString(4), cursor.getString(5),
						cursor.getString(6));
			}
			
			cursor.close();
		}

		return file;
	}

	public void deleteFile(long id) {
		SQLiteDatabase db = this.getWritableDatabase();		
		db.delete(TABLE_CACHE_FILES, KEY_ID + " = ?", new String[] { String.valueOf(id) });
		db.close();
	}
	
	public void clearTable(){
		SQLiteDatabase db = this.getWritableDatabase();		
		db.delete(TABLE_CACHE_FILES, "", new String[] {});
		db.close();
	}
}