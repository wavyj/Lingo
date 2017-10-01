package com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.ImageMessage;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.Message;

public class ConversationsDatabaseSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "ConversationsDatabaseSQ";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_FILE = "conversations.db";
    private static final String TABLE_NAME = "Conversations";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIMESTAMP = "time";
    public static final String COLUMN_SENDERID = "sender";
    public static final String COLUMN_LASTMESSAGE = "lastMessage";
    public static final String COLUMN_TYPE = "type";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + "(" +
            COLUMN_ID + " TEXT PRIMARY KEY, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_SENDERID + " TEXT, " +
            COLUMN_LASTMESSAGE + " TEXT, " +
            COLUMN_TYPE + " TEXT, " +
            COLUMN_TIMESTAMP + " INTEGER)";

    private static ConversationsDatabaseSQLHelper INSTANCE = null;

    private SQLiteDatabase mDatabase;

    private ConversationsDatabaseSQLHelper(Context context){
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }

    public static ConversationsDatabaseSQLHelper getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = new ConversationsDatabaseSQLHelper(context);
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertConversation(Message m, String channel){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, channel);
        values.put(COLUMN_TITLE, m.getUser().getName());
        values.put(COLUMN_TIMESTAMP, m.getCreatedAt().getTime());
        values.put(COLUMN_SENDERID, m.getUser().getId());
        values.put(COLUMN_LASTMESSAGE, m.getText());

        if (m instanceof ImageMessage){
            values.put(COLUMN_TYPE, "Image");
        }else {
            values.put(COLUMN_TYPE, "Text");
        }

        return mDatabase.insert(TABLE_NAME, null, values);
    }

    public Cursor getConversation(String channel){
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {channel};

        return mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }

    public int deleteConversation(String channel){
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {channel};

        return mDatabase.delete(TABLE_NAME, selection, selectionArgs);
    }

    public boolean checkConversation(String channel){
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {channel};
        Cursor c = mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (c != null){
            c.moveToFirst();
            if (c.getCount() > 0){
                return true;
            }
        }
        return false;
    }

}
