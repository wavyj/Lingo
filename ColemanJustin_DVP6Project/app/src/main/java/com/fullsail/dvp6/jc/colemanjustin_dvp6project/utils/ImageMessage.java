package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import com.sendbird.android.FileMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class ImageMessage extends Message implements MessageContentType.Image{
    private static final String TAG = "ImageMessage";

    private String mID;
    private String mText;
    private Author mUser;
    private Date mCreatedAt;
    private String mImageUrl = "";

    public ImageMessage(FileMessage fileMessage) {
        super(fileMessage);
        mID = String.valueOf(fileMessage.getMessageId());
        mText = "Image";
        mImageUrl = fileMessage.getUrl();
        mCreatedAt = new Date(fileMessage.getCreatedAt());
        mUser = new Author(fileMessage.getSender());
    }

    public ImageMessage(String id, String text, Author a, Date date, String imageUrl){
        super(id, a, text, date);
        mID = id;
        mText = text;
        mUser = a;
        mCreatedAt = date;
        mImageUrl = imageUrl;
    }

    public void setImageUrl(String url){
        mImageUrl = url;
    }

    public String getImageUrl(){
        return mImageUrl;
    }
}
