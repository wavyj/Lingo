package com.fullsail.dvp6.jc.colemanjustin_dvp6project.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

public class ImagesDatabaseSQLHelper extends SQLiteOpenHelper {
    private static final String TAG = "ImagesDatabaseSQLHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_FILE = "images.db";
    private static final String TABLE_NAME = "Images";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_IMAGE = "image";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_URL + " TEXT, " +
            COLUMN_IMAGE + " BLOB)";

    private static ImagesDatabaseSQLHelper INSTANCE = null;

    private SQLiteDatabase mDatabase;

    private ImagesDatabaseSQLHelper(Context context){
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }

    public static ImagesDatabaseSQLHelper getInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = new ImagesDatabaseSQLHelper(context);
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

    public long insertImage(String url , byte[] bmp){
        ContentValues values = new ContentValues();
        values.put(COLUMN_URL, url);
        values.put(COLUMN_IMAGE, bmp);

        return mDatabase.insert(TABLE_NAME, null, values);
    }

    public Cursor getImage(String url){
        String selection = COLUMN_URL + " = ?";
        String[] selectionArgs = {url};

        return mDatabase.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
    }
}
