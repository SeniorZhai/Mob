package com.helpshift.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProfilesDBHelper extends SQLiteOpenHelper {
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IDENTIFIER = "identifier";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PROFILE_ID = "profile_id";
    public static final String COLUMN_SALT = "salt";
    private static final String DATABASE_CREATE = "CREATE TABLE profiles(_id INTEGER PRIMARY KEY AUTOINCREMENT, identifier TEXT NOT NULL UNIQUE, profile_id TEXT UNIQUE, name TEXT, email TEXT, salt TEXT);";
    public static final String DATABASE_NAME = "__hs__db_profiles";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_PROFILES = "profiles";

    public ProfilesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS profiles");
        onCreate(db);
    }
}
