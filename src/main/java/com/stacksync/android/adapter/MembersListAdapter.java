package com.stacksync.android.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.stacksync.android.R;
import com.stacksync.android.model.Member;

import java.util.List;

public class MembersListAdapter extends BaseAdapter {

    private List<Member> members;
    private final Context context;

    public MembersListAdapter(Context context, List<Member> members){
        this.context = context;
        this.members = members;
    }

    public void setMembers(List<Member> members){
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.member_row, viewGroup, false);
        }

        TextView usernameView = (TextView) convertView.findViewById(R.id.members_labelUsername);
        TextView ownerView = (TextView) convertView.findViewById(R.id.members_labelOwner);
        TextView joinedAtView = (TextView) convertView.findViewById(R.id.members_labelJoinedAt);

        Member member = getItem(position);

        usernameView.setText(String.format("%s (%s)", member.getName(), member.getEmail()));
        String ownerText = "";
        if (member.getIsOwner()){
            ownerText = "(owner)";
        }
        ownerView.setText(ownerText);
        joinedAtView.setText(member.getJoinedAt().toString());

        return convertView;
    }
}
