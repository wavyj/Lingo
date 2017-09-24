package com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageMessage;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Message;

public class MessagesDatabaseSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "MessagesDatabaseSQLHelp";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_FILE = "messages.db";

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
            COLUMN_ID + " TEXT PRIMARY KEY, " +
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
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }

    public long insertMessage(Message m, String channel){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHANNEL_URL, channel);
        values.put(COLUMN_ID, m.getId());
        values.put(COLUMN_TEXT, m.getText());
        values.put(COLUMN_TIME, m.getCreatedAt().getTime());
        values.put(COLUMN_TYPE, "Text");
        values.put(COLUMN_SENDER, m.getUser().getId());

        return mDatabase.insertOrThrow(TABLE_NAME, null, values);
    }

    public int updateText(Message m, String channel){
        String selection = COLUMN_CHANNEL_URL + " = ? " +
                "AND " +
                COLUMN_ID + " = ?";
        String [] selectionArgs = {channel, m.getId()};

        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, m.getText());

        return mDatabase.update(TABLE_NAME, values, selection, selectionArgs);
    }

    public int updateImage(ImageMessage m, String channel){
        String selection = COLUMN_CHANNEL_URL + " = ? " +
                "AND " +
                COLUMN_ID + " = ? " +
                "AND " +
                COLUMN_IMAGE + " = ?";
        String [] selectionArgs = {channel, m.getId(), m.getImageUrl()};

        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT, m.getText());

        return mDatabase.update(TABLE_NAME, values, selection, selectionArgs);
    }

    public long insertImage(ImageMessage m, String channel){
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHANNEL_URL, channel);
        values.put(COLUMN_ID, m.getId());
        values.put(COLUMN_TEXT, m.getText());
        values.put(COLUMN_TIME, m.getCreatedAt().getTime());
        values.put(COLUMN_TYPE, "Image");
        values.put(COLUMN_IMAGE, m.getImageUrl());
        values.put(COLUMN_SENDER, m.getUser().getId());

        return mDatabase.insertOrThrow(TABLE_NAME, null, values);
    }

    public boolean checkMessage(Message m, String channel){
        String selection = COLUMN_CHANNEL_URL + " = ? " + "AND " + COLUMN_ID + " = ?";
        String[] selectionArgs = {channel, m.getId()};
        Cursor c = mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        c.moveToFirst();

        if (c.getCount() == 0){
            c.close();
            return false;
        } else {
            c.close();
            return true;
        }
    }

    public Cursor query(String currentChannel){
        String selection = COLUMN_CHANNEL_URL + " = ?";
        String[] selectionArgs = {currentChannel};
        String sortOrder = COLUMN_TIME + " DESC";
        String limit = "60";

        return mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder, limit);
    }
}
