package com.helpshift.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.helpshift.HelpshiftContext;
import com.helpshift.util.StorageUtil;

public class FaqsDBHelper extends SQLiteOpenHelper {
    private static final int CUR_DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "__hs__db_faq";

    private static class LazyHolder {
        private static final FaqsDBHelper INSTANCE = new FaqsDBHelper(HelpshiftContext.getApplicationContext());

        private LazyHolder() {
        }
    }

    public static FaqsDBHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    private FaqsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE faqs (_id INTEGER PRIMARY KEY AUTOINCREMENT,question_id TEXT NOT NULL,publish_id TEXT NOT NULL,section_id TEXT NOT NULL,title TEXT NOT NULL,body TEXT NOT NULL,helpful INTEGER,rtl INTEGER,tags TEXT,FOREIGN KEY(section_id) REFERENCES sections (_id));");
        db.execSQL("CREATE TABLE sections (_id INTEGER PRIMARY KEY AUTOINCREMENT,section_id TEXT NOT NULL,publish_id INTEGER NOT NULL,title TEXT NOT NULL);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StorageUtil.clearFAQEtag();
        dropTables(db);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        StorageUtil.clearFAQEtag();
        dropTables(db);
        onCreate(db);
    }

    public void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS faqs");
        db.execSQL("DROP TABLE IF EXISTS sections");
    }
}
