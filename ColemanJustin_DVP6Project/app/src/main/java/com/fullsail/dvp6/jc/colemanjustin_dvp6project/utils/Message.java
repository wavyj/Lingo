package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.util.Log;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message implements IMessage {
     private static final String TAG = "MessageUtil";
    private String mID;
    private String mText;
    private String mLang;
    private Author mUser;
    private Date mCreatedAt;

    public Message(UserMessage message){
        mID = String.valueOf(message.getMessageId());
        mText = message.getMessage();
        mUser = new Author(message.getSender());
        mCreatedAt = new Date(message.getCreatedAt());
        mLang = message.getCustomType();
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
        //String adminID = adminMessage.getData();
        mCreatedAt = new Date(adminMessage.getCreatedAt());
    }

    public Message(FileMessage fileMessage){
        mID = String.valueOf(fileMessage.getMessageId());
        mText = "";
        mCreatedAt = new Date(fileMessage.getCreatedAt());
        mUser = new Author(fileMessage.getSender());
    }

    @Override
    public String getId() {
        return mID;
    }

    public void setText(String text){
        mText = text;
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

    public String getLang(){
        return mLang;
    }

    public void setTranslated(String translation){
        mText = translation;
    }
}
