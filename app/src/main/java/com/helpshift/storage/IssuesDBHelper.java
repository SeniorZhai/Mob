package com.helpshift.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IssuesDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "__hs__db_issues";
    private static final Integer DATABASE_VERSION = Integer.valueOf(1);

    public IssuesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION.intValue());
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE issues (_id INTEGER PRIMARY KEY AUTOINCREMENT,profile_id TEXT NOT NULL,issue_id TEXT NOT NULL UNIQUE,body TEXT,title TEXT NOT NULL,created_at TEXT NOT NULL,updated_at TEXT NOT NULL,status INTEGER NOT NULL,new_message_count INTEGER,show_agent_name INTEGER);");
        db.execSQL("CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT,issue_id TEXT NOT NULL,message_id TEXT NOT NULL UNIQUE,body TEXT,origin TEXT NOT NULL,type TEXT NOT NULL,created_at TEXT NOT NULL,author TEXT,meta TEXT,screenshot TEXT,message_seen INTEGER,invisible INTEGER,in_progress INTEGER);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    public void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS issues");
        db.execSQL("DROP TABLE IF EXISTS messages");
    }
}
