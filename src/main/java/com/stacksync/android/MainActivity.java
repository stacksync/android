package com.stacksync.android;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.stacksync.android.api.ListResponse;
import com.stacksync.android.api.LoginResponse;
import com.stacksync.android.api.StacksyncClient;
import com.stacksync.android.cache.CacheManager;
import com.stacksync.android.exceptions.FileNotCachedException;
import com.stacksync.android.task.CreateFolderTask;
import com.stacksync.android.task.DeleteFileTask;
import com.stacksync.android.task.DownloadFileTask;
import com.stacksync.android.task.ListDirectoryTask;
import com.stacksync.android.task.OpenFileTask;
import com.stacksync.android.task.RenameItemTask;
import com.stacksync.android.task.ShareFileTask;
import com.stacksync.android.task.UploadFileTask;
import com.stacksync.android.utils.Constants;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends SherlockActivity implements SearchView.OnQueryTextListener, OnRefreshListener {

    private static String TAG = MainActivity.class.getName();

    // XML node keys
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_FILEID = "fileid";
    public static final String KEY_FILEINFO = "fileinfo";
    public static final String KEY_ICON = "imgicon";
    public static final String KEY_ISFOLDER = "isfolder";
    public static final String KEY_MIMETYPE = "mimetype";

    // Menu
    private static final int MENU_DOWNLOAD = 1;
    private static final int MENU_DELETE = 2;
    private static final int MENU_SENTTO = 3;
    private static final int MENU_RENAME = 4;
    private static final int MENU_SHARE = 5;

    // Intent result codes
    private static final int UPLOAD_FILE_RESULT_CODE = 1;
    private static final int SETTINGS_RESULT_CODE = 3;
    private static final int EXPORT_FILE_RESULT_CODE = 4;

    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<',
            '>', '|', '\"', ':'};

    private SimpleExpandableListAdapter expListAdapter;
    private FileManager fileManager;
    private ExpandableListView expListView;
    private ArrayList<Pair<String, String>> navigation;
    private String appName;


    // Selected item
    private int selectedGroup;
    private int selectedChild;

    // Flags
    private boolean isInitialized;

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isInitialized = false;

        StacksyncClient client = StacksyncApp.getClient(this);
        if (client.isLoggedIn()) {

            if (!isInitialized)
                initialize();
            listDirectory();

        } else {

            // Restore preferences
            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
            String accessTokenKey = settings.getString("access_token_key", "");
            String accessTokenSecret = settings.getString("access_token_secret", "");
            String apiUrl = settings.getString("api_url", "");

            if (Utils.validateTokenFields(accessTokenKey, accessTokenSecret)) {

                client.initOauth(apiUrl);
                client.getConsumer().setTokenWithSecret(accessTokenKey, accessTokenSecret);
                client.setIsLoggedIn(true);

                if (!isInitialized)
                    initialize();
                listDirectory();
                // login();
                // if login succeds it will call initialize()

            } else {
                removeCredentialsAndLogout();
            }
        }
    }

    private void initialize() {
        setContentView(R.layout.activity_main);

        // add the empty view
        LayoutInflater inflater = getLayoutInflater();
        View emptyView = inflater.inflate(R.layout.empty_view_layout, null);
        addContentView(emptyView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));


        // Now find the PullToRefreshLayout to setup
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.main_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(MainActivity.this)
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set the OnRefreshListener
                .listener(MainActivity.this)
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(mPullToRefreshLayout);


        navigation = new ArrayList<Pair<String, String>>();

        // get application name
        appName = Utils.getApplicationName(this);

        fileManager = new FileManager(this);

        // init cache manager
        initializeCacheManager();

        expListAdapter = new SimpleExpandableListAdapter(this, fileManager.getGroupList(), R.layout.group_row,
                new String[]{"Group Item"}, new int[]{R.id.row_name}, fileManager.getContent(),
                R.layout.file_row, new String[]{KEY_FILENAME, KEY_FILEINFO}, new int[]{R.id.filename,
                R.id.fileinfo}
        ) {
            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                     ViewGroup parent) {
                final View v = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

                HashMap<String, Object> child = (HashMap<String, Object>) getChild(groupPosition, childPosition);

                // Populate your custom view here
                ((TextView) v.findViewById(R.id.filename)).setText((String) child.get(KEY_FILENAME));

                ((TextView) v.findViewById(R.id.fileinfo)).setText((String) child.get(KEY_FILEINFO));

                ((ImageView) v.findViewById(R.id.imgicon)).setImageDrawable((Drawable) child.get(KEY_ICON));

                return v;
            }

        };

        expListView = (ExpandableListView) findViewById(R.id.exp_list_view);
        expListView.setEmptyView(findViewById(R.id.empty_view_layout));
        expListView.setAdapter(expListAdapter);

        expListView.setOnGroupClickListener(new OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        expListView.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) expListAdapter.getChild(groupPosition,
                        childPosition);

                if ((Boolean) item.get(KEY_ISFOLDER)) {
                    folderDown((String) item.get(KEY_FILEID), (String) item.get(KEY_FILENAME));
                    return true;
                } else {
                    String filename = (String) item.get(KEY_FILENAME);
                    String fileid = (String) item.get(KEY_FILEID);
                    String mimetype = (String) item.get(KEY_MIMETYPE);
                    onOpenFileClick(fileid, filename, mimetype);

                    return false;
                }
            }
        });

        expListView.setGroupIndicator(null);

        for (int i = 0; i < expListAdapter.getGroupCount(); i++)
            expListView.expandGroup(i);

        registerForContextMenu(expListView);

        isInitialized = true;
    }

    public void onReceiveLoginResponse(LoginResponse loginResponse) {

        if (loginResponse.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            removeCredentialsAndLogout();
        } else {
            if (!isInitialized)
                initialize();
            showString(loginResponse.getMessage());
        }
    }

    private void removeCredentialsAndLogout() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(myIntent);
        finish();
    }

    private void initializeCacheManager() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        CacheManager cacheManager = CacheManager.getInstance(this);

        File cacheDir = Utils.getCacheDirectory(this);
        cacheManager.setCacheDir(cacheDir);

        cacheManager.setCacheLimit(settings.getLong("cache_limit", 0));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        // Get the info on which item was selected
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        selectedGroup = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        selectedChild = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // Only create a context menu for child items
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

            // Retrieve the item that was clicked on
            HashMap<String, Object> item = (HashMap<String, Object>) expListAdapter.getChild(selectedGroup,
                    selectedChild);

            boolean isFolder = (Boolean) item.get(KEY_ISFOLDER);
            String filename = (String) item.get(KEY_FILENAME);

            menu.setHeaderTitle(filename);
            if (isFolder) {
                menu.add(v.getId(), MENU_DELETE, 1, "Delete");
                menu.add(v.getId(), MENU_RENAME, 2, "Rename");
                menu.add(v.getId(), MENU_SHARE, 3, "Share");

            } else {
                menu.add(v.getId(), MENU_DOWNLOAD, 0, "Export");
                menu.add(v.getId(), MENU_DELETE, 1, "Delete");
                menu.add(v.getId(), MENU_RENAME, 2, "Rename");
                menu.add(v.getId(), MENU_SENTTO, 3, "Sent to...");

            }
        }
    }

    public boolean onContextItemSelected(android.view.MenuItem menuItem) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

        // int groupPos = 0, childPos = 0;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            // groupPos =
            // ExpandableListView.getPackedPositionGroup(info.packedPosition);
            // childPos =
            // ExpandableListView.getPackedPositionChild(info.packedPosition);

            HashMap<String, Object> item = (HashMap<String, Object>) expListAdapter.getChild(selectedGroup,
                    selectedChild);

            String filename = (String) item.get(KEY_FILENAME);
            String fileId = (String) item.get(KEY_FILEID);
            String mimetype = (String) item.get(KEY_MIMETYPE);
            boolean isFolder = (Boolean) item.get(KEY_ISFOLDER);
            // String path = getCurrentPath();

            switch (menuItem.getItemId()) {
                case MENU_DOWNLOAD:
                    // TODO: get the real version
                    onExportFileClick(fileId, "0", filename, mimetype);
                    return true;

                case MENU_DELETE:
                    onDeleteFileClick(fileId, isFolder);
                    return true;

                case MENU_SENTTO:
                    // TODO: get the real version
                    onSentToClick(fileId, "0", filename, mimetype);
                    return true;
                case MENU_RENAME:
                    onRenameItemClick(fileId, filename, isFolder);
                    return true;
                case MENU_SHARE:
                    onShareFolderClick(fileId, filename);
                    return true;
                default:
                    return false;
            }

        } else {
            return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        /*
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setQueryHint(getResources().getString(R.string.search_hint));
        */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(myIntent, SETTINGS_RESULT_CODE);
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            folderUp();
            return true;
        } else if (item.getItemId() == R.id.menu_upload) {
            onUploadFileClick();
            return true;

        } else if (item.getItemId() == R.id.menu_create_folder) {
            onCreateFolderClick();
            return true;
        } else {
            // Toast.makeText(this, "Default", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case UPLOAD_FILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {

                    Uri selectedFile = data.getData();
                    String parentId = getCurrentFileId();
                    doUploadFile(selectedFile, parentId);
                }
                break;

            case EXPORT_FILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {

                    String localPath = data.getData().getPath();
                    // Toast.makeText(this, localPath, Toast.LENGTH_LONG).show();
                    doExportFile(localPath);
                }
                break;
            case SETTINGS_RESULT_CODE:

                if (resultCode == Constants.RESULT_LOGOUT) {
                    logout();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (!navigation.isEmpty()) {
            folderUp();
        } else {
            super.onBackPressed();
        }
    }

    private void folderUp() {
        if (!navigation.isEmpty()) {
            navigation.remove(navigation.size() - 1);
        }
        listDirectory();
    }

    private void folderDown(String fileId, String filename) {
        navigation.add(new Pair<String, String>(fileId, filename));
        listDirectory();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void listDirectory() {

        fileManager.clearContent();

        String fileId = getCurrentFileId();

        AsyncTask<String, Integer, ListResponse> listDirectoryTask = new ListDirectoryTask(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            listDirectoryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileId);
        } else {
            listDirectoryTask.execute(fileId);
        }

        showCachedView(fileId);

        // expects result on OnListDirectoryFinish method
    }

    private void showCachedView(String fileId) {
        CacheManager cacheManager = CacheManager.getInstance(this);
        try {
            JSONObject metadata = cacheManager.getMetadataFromCache(fileId);
            showMetadata(metadata);
        } catch (FileNotCachedException e) {
            showString("Loading... please wait.");
        }
    }

    private void enableTitleAndButtons() {
        // check to activate back icon
        if (!navigation.isEmpty()) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            setTitle(getWorkingFolderName());
        } else {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
            setTitle(appName);
        }
    }

    private void refreshView(String message) {

        enableTitleAndButtons();

        int count = expListAdapter.getGroupCount();

        if (count > 0) {
            for (int position = 1; position <= count; position++) {
                expListView.expandGroup(position - 1);
            }
        } else {
            TextView tview = ((TextView) findViewById(R.id.empty_view_textview));
            tview.setText(message);
        }

        expListAdapter.notifyDataSetChanged();
    }

    public void onReceiveListResponse(ListResponse response) {

        mPullToRefreshLayout.setRefreshComplete();

        if (response.getSucced()) {

            // display only the metadata if we are on the target folder
            if (response.getFileId() == getCurrentFileId()
                    || (response.getFileId() != null && getCurrentFileId() != null && response.getFileId().equals(
                    getCurrentFileId()))) {
                showMetadata(response.getMetadata());
            }
        } else {
            if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                removeCredentialsAndLogout();
            } else {
                showString(response.getMessage());
            }
        }
    }

    private void showMetadata(JSONObject metadata) {
        String message = fileManager.updateContent(metadata);
        refreshView(message);
    }

    private void showString(String message) {
        if (fileManager.hasContent()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            refreshView(message);
        }

    }

    private String getCurrentFileId() {
        String fileId = "0";

        if (!navigation.isEmpty()) {
            Pair<String, String> workingFolder = navigation.get(navigation.size() - 1);
            fileId = workingFolder.first;
        }

        return fileId;
    }

    private String getWorkingFolderName() {
        String workingFolderName;

        if (navigation.isEmpty()) {
            workingFolderName = appName;
        } else {
            Pair<String, String> workingFolder = navigation.get(navigation.size() - 1);
            workingFolderName = workingFolder.second;
        }

        return workingFolderName;
    }

    private void onExportFileClick(String fileId, String version, String filename, String mimetype) {

        // TODO: create an activity to select a folder in the SD card
        Intent fileExploreIntent = new Intent(
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.INTENT_ACTION_SELECT_DIR, null, this,
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.class);

        fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
                Environment.getExternalStorageDirectory().getPath());
        fileExploreIntent.putExtra(
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showUnreadableFilesParameter, false);
        fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showHiddenFilesParameter,
                false);
        fileExploreIntent.putExtra(
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showUnwriteableDirsParameter, false);

        startActivityForResult(fileExploreIntent, EXPORT_FILE_RESULT_CODE);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doExportFile(String localPath) {

        @SuppressWarnings("unchecked")
        HashMap<String, Object> item = (HashMap<String, Object>) expListAdapter.getChild(selectedGroup, selectedChild);

        String filename = (String) item.get(KEY_FILENAME);
        String fileId = (String) item.get(KEY_FILEID);
        String mimetype = (String) item.get(KEY_MIMETYPE);
        String version = "0";

        // File externalStorage = Environment.getExternalStorageDirectory();
        // File downloadDir = new File(externalStorage,
        // FilesConstants.DOWNLOAD_FOLDER);
        File downloadDir = new File(localPath);

        if (!downloadDir.exists()) {
            if (!downloadDir.mkdirs()) {
                Toast.makeText(MainActivity.this, "Cannot export file into this file!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        AsyncTask<String, Integer, Boolean> downloadFile = new DownloadFileTask(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileId, version, filename, mimetype,
                    downloadDir.toString());
        } else {
            downloadFile.execute(fileId, version, filename, mimetype, downloadDir.toString());
        }
    }

    private void onOpenFileClick(String fileId, String filename, String mimetype) {

        // FIXME: get the actual version
        String version = "0";

        AsyncTask<String, Integer, Boolean> openFile = new OpenFileTask(this);
        openFile.execute(fileId, version, filename, mimetype);

    }

    private void onDeleteFileClick(String fileId, boolean isFolder) {

        AsyncTask<String, Integer, Boolean> deleteFile = new DeleteFileTask(this);

        String type;
        if (isFolder)
            type = "folder";
        else
            type = "file";

        deleteFile.execute(fileId, type);
    }

    private void onSentToClick(String fileId, String version, String filename, String mimetype) {

        AsyncTask<String, Integer, Boolean> shareTask = new ShareFileTask(this);
        shareTask.execute(fileId, version, filename, mimetype);
    }

    private void onRenameItemClick(final String itemId, String itemName, final boolean isFolder) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if (isFolder) {
            alert.setTitle("Rename Folder");
            alert.setMessage("Folder name:");
        } else {
            alert.setTitle("Rename File");
            alert.setMessage("File name:");
        }
        final EditText input = new EditText(this);
        input.setLines(1);
        input.setSingleLine();
        input.setText(itemName);
        alert.setView(input);

        alert.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newItemName = input.getText().toString();
                String parentId = getCurrentFileId();

                if (validateFilename(newItemName)) {
                    AsyncTask<String, Integer, Boolean> renameItem = new RenameItemTask(MainActivity.this);
                    renameItem.execute(newItemName, itemId, parentId, Boolean.toString(isFolder));

                } else {
                    Toast.makeText(MainActivity.this, "Invalid folder name", Toast.LENGTH_LONG).show();
                }

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void onUploadFileClick() {

        final String items[] = {"Photos or videos", "Other files"};

        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        ab.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int choice) {
                if (choice == 0) {
                    onUploadMediaClick();
                } else if (choice == 1) {
                    onUploadOtherFilesClick();
                }
            }
        });
        ab.show();
    }

    private void onUploadOtherFilesClick() {

        Intent fileExploreIntent = new Intent(
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.INTENT_ACTION_SELECT_FILE, null, this,
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.class);

        fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.startDirectoryParameter,
                Environment.getExternalStorageDirectory().getPath());
        fileExploreIntent.putExtra(
                ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showUnreadableFilesParameter, false);
        fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showHiddenFilesParameter,
                false);
        fileExploreIntent.putExtra(ua.com.vassiliev.androidfilebrowser.FileBrowserActivity.showConfirmationParameter,
                true);

        startActivityForResult(fileExploreIntent, UPLOAD_FILE_RESULT_CODE);

    }

    private void onUploadMediaClick() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("video/* image/*");

        try {
            startActivityForResult(intent, UPLOAD_FILE_RESULT_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found to process the request", Toast.LENGTH_LONG).show();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void doUploadFile(Uri selectedFile, String parentId) {

        AsyncTask<Object, Integer, Boolean> uploadFile = new UploadFileTask(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            uploadFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, selectedFile, parentId);
        } else {
            uploadFile.execute(selectedFile, parentId);
        }

    }

    private void onCreateFolderClick() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Create a new folder");
        alert.setMessage("Folder name:");

        final EditText input = new EditText(this);
        input.setLines(1);
        input.setSingleLine();
        alert.setView(input);

        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String folderName = input.getText().toString();

                if (validateFilename(folderName)) {
                    String parent = getCurrentFileId();

                    AsyncTask<String, Integer, Boolean> createFolder = new CreateFolderTask(MainActivity.this);
                    createFolder.execute(folderName, parent);

                } else {
                    Toast.makeText(MainActivity.this, "Invalid folder name", Toast.LENGTH_LONG).show();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void onShareFolderClick(String folderId, String folderName) {

        Intent intent = new Intent(getBaseContext(), SharingActivity.class);
        intent.putExtra(SharingActivity.FOLDER_ID, folderId);
        intent.putExtra(SharingActivity.FOLDER_NAME, folderName);
        startActivity(intent);
    }

    private Boolean validateFilename(String filename) {

        for (char c : ILLEGAL_CHARACTERS) {
            if (filename.contains(Character.toString(c))) {
                return false;
            }
        }

        return true;
    }

    private void logout() {

        StacksyncClient client = StacksyncApp.getClient(this);
        client.logout();

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();

        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        MainActivity.this.startActivity(myIntent);
        finish();
    }

    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "Search disabled", Toast.LENGTH_LONG).show();
        return true;
    }

    public boolean onQueryTextChange(String newText) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onRefreshStarted(View view) {
        listDirectory();
    }

}
