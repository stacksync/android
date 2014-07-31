package com.stacksync.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.stacksync.android.adapter.MembersListAdapter;
import com.stacksync.android.model.Member;
import com.stacksync.android.task.FolderMembersTask;

import java.util.ArrayList;
import java.util.List;


public class SharingActivity extends SherlockActivity {

    static final String FOLDER_ID = "com.stacksync.android.FOLDER_ID";
    static final String FOLDER_NAME = "com.stacksync.android.FOLDER_NAME";
    private String folderId;
    private MembersListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_folder);

        adapter = new MembersListAdapter(this, new ArrayList<Member>());
        ListView memberView = (ListView) findViewById(R.id.share_listMembers);
        memberView.setAdapter(adapter);

        Intent intent = getIntent();
        folderId = intent.getStringExtra(FOLDER_ID);
        String folderName = intent.getStringExtra(FOLDER_NAME);
        this.setTitle(folderName);

        FolderMembersTask task = new FolderMembersTask(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, folderId);
        } else {
            task.execute(folderId);
        }
    }

    public void onGetFolderMembersResult(List<Member> members){

        adapter.setMembers(members);
        adapter.notifyDataSetChanged();
    }


    public void onCloseClick(View v){
        this.finish();
    }
}
