package com.fullsail.dvp6.jc.colemanjustin_dvp6project.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.sendbird.android.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

 class QueryAdapter extends BaseAdapter{

    private static final int ITEM_ID = 0x0001;

    private final Context mContext;
    private final ArrayList<User> mUsers;

    private static class ViewHolder{
        CircleImageView imageView;
        TextView textView;

        ViewHolder(View v){
            imageView = (CircleImageView) v.findViewById(R.id.userImage);
            textView = (TextView) v.findViewById(R.id.userName);
        }
    }

    QueryAdapter(Context context, ArrayList<User> users){
        mContext = context;
        mUsers = users;
    }

    @Override
    public int getCount() {
        if (mUsers != null){
            return  mUsers.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mUsers != null && mUsers.size() > position && position > -1){
            return mUsers.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return ITEM_ID + position;
    }

    @Override
    public View getView(int position, View recycleView, ViewGroup parent) {
        ViewHolder holder;

        if (recycleView == null){
            recycleView = LayoutInflater.from(mContext).inflate(R.layout.search_item_layout, parent, false);

            holder = new ViewHolder(recycleView);
            recycleView.setTag(holder);
        }else {
            holder = (ViewHolder) recycleView.getTag();
        }

        User currentUser = (User) getItem(position);

        holder.textView.setText(currentUser.getNickname());
        if (currentUser.getProfileUrl() != null && !currentUser.getProfileUrl().equals("")) {
            Picasso.with(mContext).load(currentUser.getProfileUrl()).into(holder.imageView);
        }else {
            // Set default profile image
            Picasso.with(mContext).load(R.drawable.emptybox_icon).into(holder.imageView);
        }
        return recycleView;
    }
}
