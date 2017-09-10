package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import com.sendbird.android.Member;
import com.sendbird.android.User;
import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    private Member mMember;
    private User mUser;

    private String mID;
    private String mName;
    private String mPhoto;

    public Author(Member member){
        mMember = member;
        mID = mMember.getUserId();
        mName = mMember.getNickname();
        mPhoto = mMember.getProfileUrl();
    }

    public Author(User user){
        mUser = user;
        mID = mUser.getUserId();
        mName = mUser.getNickname();
        mPhoto = mUser.getProfileUrl();
    }

    public Author(String id, String name, String photo){
        mID = id;
        mName = name;
        mPhoto = photo;
    }

    @Override
    public String getId() {
        return mID;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getAvatar() {
        return mPhoto;
    }
}
