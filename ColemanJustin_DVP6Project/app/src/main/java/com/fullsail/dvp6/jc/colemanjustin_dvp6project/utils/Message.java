package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.util.Log;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message implements IMessage {
     private static final String TAG = "MessageUtil";
    private String mID;
    private String mText;
    private Author mUser;
    private Date mCreatedAt;
    private String mImageUrl = "";

    public Message(UserMessage message){
        mID = String.valueOf(message.getMessageId());
        mText = message.getMessage();
        mUser = new Author(message.getSender());
        mCreatedAt = new Date(message.getCreatedAt());
    }

    public Message(Author user){
        mID = "";
        mText = "";
        mUser = user;
        mCreatedAt = new Date();
    }

    public Message(String id, Author user, String text, Date date){
        mID = id;
        mText = text;
        mUser = user;
        mCreatedAt = date;
    }

    public Message(AdminMessage adminMessage){
        mID = String.valueOf(adminMessage.getMessageId());
        mText = adminMessage.getMessage();
        String adminID = adminMessage.getData();
        mCreatedAt = new Date(adminMessage.getCreatedAt());
        getAdminUser(adminID);
    }

    @Override
    public String getId() {
        return mID;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public IUser getUser() {
        return mUser;
    }

    @Override
    public Date getCreatedAt() {
        if (mCreatedAt != null) {
            return mCreatedAt;
        }
        return new Date();
    }

    public void setImageUrl(String url){
        mImageUrl = url;
    }

    public String getImageUrl(){
        return mImageUrl;
    }

    private void getAdminUser(String adminID){
        List<String> userIDs = new ArrayList<>();
        userIDs.add(adminID);
        UserListQuery userListQuery = SendBird.createUserListQuery(userIDs);

        userListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }

                if (list.size() > 0) {
                    mUser = new Author(list.get(0));
                }
            }
        });
    }
}
