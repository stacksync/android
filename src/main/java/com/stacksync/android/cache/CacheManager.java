package com.stacksync.android.cache;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.stacksync.android.exceptions.FileNotCachedException;

import android.content.Context;
import android.util.Log;

public class CacheManager {

    private static final String TAG = "CACHEMANAGER";
    private static CacheManager instance = null;
    private File cacheDir;
    private long cacheLimit;
    private CacheHelper db;

    protected CacheManager(Context context) {
        db = new CacheHelper(context);
    }

    public static CacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new CacheManager(context);
        }
        return instance;
    }

    public JSONObject getMetadataFromCache(String fileId, Long version) throws FileNotCachedException {

        CachedFile file = db.getFile(fileId);

        if (file.getVersion() != version) {
            throw new FileNotCachedException();
        }
        
        JSONObject metadata;
        try {
            metadata = new JSONObject(file.getMetadata());
        } catch (JSONException e) {
            // TODO Should send another exception
            Log.e(TAG, "Could not patse JSON response from cached file", e);
            throw new FileNotCachedException();
        }

        return metadata;
    }

    public JSONObject getMetadataFromCache(String fileId) throws FileNotCachedException {

        CachedFile file = db.getFile(fileId);

        if (file == null) {
            throw new FileNotCachedException();
        }

        JSONObject metadata;
        try {
            metadata = new JSONObject(file.getMetadata());
        } catch (JSONException e) {
            // TODO Should send another exception
            Log.e(TAG, "Could not patse JSON response from cached file", e);
            throw new FileNotCachedException();
        }

        return metadata;
    }

    public File getFileFromCache(String fileId, Long version) throws FileNotCachedException {

        CachedFile cachedFile = db.getFile(fileId);
        
        if (cachedFile == null) {
            throw new FileNotCachedException();
        }
        
        if (cachedFile.getVersion() != version) {
            throw new FileNotCachedException();
        }
        
        File file = new File(cachedFile.getLocalPath());

        if (!file.exists()){
            throw new FileNotCachedException();
        }
        
        return file;
    }
    
    
    public void deleteFile(Long fileId) {
        db.deleteFile(fileId);
    }

    public void clearCache() {
    	db.clearTable();
    	
        if (cacheDir.isDirectory()) {
            String[] children = cacheDir.list();
            for (int i = 0; i < children.length; i++) {
                new File(cacheDir, children[i]).delete();
            }
        }
    }

    public long getCacheSize() {
        return getFolderSize(cacheDir);
    }

    public void setCacheLimit(long limit) {
        this.cacheLimit = limit;
    }

    public long getCacheLimit() {
        return cacheLimit;
    }

    private long getFolderSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                System.out.println(file.getName() + " " + file.length());
                size += file.length();
            } else
                size += getFolderSize(file);
        }
        return size;
    }

    public void saveFileToCache(CachedFile fileToSave) {

        if (db.existsFile(fileToSave.getId())) {
            db.updateFile(fileToSave);
        } else {
            db.addFile(fileToSave);
        }

        /**
         * Remove some files to keep the cache usage below the limit. The file
         * just added to the cache won't be deleted whatsoever.
         */

        File file = new File(fileToSave.getLocalPath());
        if (file.exists()) {
            long bytesRemoved = 0;
            long cacheSize = getCacheSize();
            if (cacheSize > cacheLimit) {
                for (File f : cacheDir.listFiles()) {
                    if (!f.getAbsolutePath().equals(file.getAbsolutePath())) {
                        bytesRemoved += f.length();
                        f.delete();

                        if (cacheSize - bytesRemoved < cacheLimit) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setCacheDir(File cacheDir) {
        this.cacheDir = cacheDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

}
