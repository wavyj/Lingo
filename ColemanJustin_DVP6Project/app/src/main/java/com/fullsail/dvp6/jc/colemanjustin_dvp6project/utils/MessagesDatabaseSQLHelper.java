package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sendbird.android.BaseMessage;

public class MessagesDatabaseSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "MessagesDatabaseSQLHelp";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_FILE = "translateMessages.db";

    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CHANNEL_URL = "channel";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_TYPE = "type";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CHANNEL_URL + " TEXT, " +
            COLUMN_TEXT + " TEXT, " +
            COLUMN_TIME + " INTEGER, " +
            COLUMN_IMAGE + " TEXT, " +
            COLUMN_SENDER + " TEXT, " +
            COLUMN_TYPE + " TEXT)";

    private static MessagesDatabaseSQLHelper INSTANCE = null;

    private SQLiteDatabase mDatabase;

    private MessagesDatabaseSQLHelper(Context context){
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }

    public static MessagesDatabaseSQLHelper getInsance(Context context){
        if (INSTANCE == null){
            INSTANCE = new MessagesDatabaseSQLHelper(context);
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public long insertMessage(Message m, String channel){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHANNEL_URL, channel);
        values.put(COLUMN_TEXT, m.getText());
        values.put(COLUMN_TIME, m.getCreatedAt().getTime());
        values.put(COLUMN_TYPE, "Text");
        values.put(COLUMN_SENDER, m.getUser().getId());

        return mDatabase.insert(TABLE_NAME, null, values);
    }

    public long insertImage(ImageMessage m, String channel){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHANNEL_URL, channel);
        values.put(COLUMN_TEXT, m.getText());
        values.put(COLUMN_TIME, m.getCreatedAt().getTime());
        values.put(COLUMN_TYPE, "Image");
        values.put(COLUMN_IMAGE, m.getImageUrl());
        values.put(COLUMN_SENDER, m.getUser().getId());

        return mDatabase.insert(TABLE_NAME, null, values);
    }

    public int clearAll(){
        return mDatabase.delete(TABLE_NAME, null, null);
    }

    public Cursor query(String currentChannel){
        String selection = COLUMN_CHANNEL_URL + " = ?";
        String[] selectionArgs = {currentChannel};
        String sortOrder = COLUMN_TIME + " DESC";
        String limit = "60";

        return mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder, limit);
    }
}
