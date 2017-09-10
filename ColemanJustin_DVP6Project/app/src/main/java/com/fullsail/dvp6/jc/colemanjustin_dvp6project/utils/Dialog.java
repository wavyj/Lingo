package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.PreviousMessageListQuery;
import com.sendbird.android.SendBirdException;
import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;
import java.util.List;

public class Dialog implements IDialog<Message> {

    private GroupChannel mGroupChannel;

    private String mID;
    private String mPhoto;
    private String mDialogName;
    private int mUnreadCount;
    private ArrayList<Author> mUsers = new ArrayList<>();
    private Message mLastMessage;

    public Dialog(String id, String name, String photo, ArrayList<Author> users, Message
                  lastMessage, int unreadCount, GroupChannel groupChannel){
        mID = id;
        mPhoto = photo;
        mDialogName = name;
        mUsers = users;
        mLastMessage = lastMessage;
        mUnreadCount = unreadCount;
        mGroupChannel = groupChannel;
    }


    @Override
    public String getId() {
        return mID;
    }

    @Override
    public String getDialogPhoto() {
        return mPhoto;
    }

    @Override
    public String getDialogName() {
        return mDialogName;
    }

    @Override
    public ArrayList<Author> getUsers() {
        return mUsers;
    }

    @Override
    public Message getLastMessage() {
        return mLastMessage;
    }

    @Override
    public void setLastMessage(Message message) {
        mLastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        return mUnreadCount;
    }

    public GroupChannel getGroupChannel(){
        return mGroupChannel;
    }
}
