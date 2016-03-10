package com.mixpanel.android.mpmetrics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.helpshift.storage.ProfilesDBHelper;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class MPDbAdapter {
    private static final String CREATE_EVENTS_TABLE = ("CREATE TABLE " + Table.EVENTS.getName() + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATA + " STRING NOT NULL, " + KEY_CREATED_AT + " INTEGER NOT NULL);");
    private static final String CREATE_PEOPLE_TABLE = ("CREATE TABLE " + Table.PEOPLE.getName() + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_DATA + " STRING NOT NULL, " + KEY_CREATED_AT + " INTEGER NOT NULL);");
    private static final String DATABASE_NAME = "mixpanel";
    private static final int DATABASE_VERSION = 4;
    public static final int DB_OUT_OF_MEMORY_ERROR = -2;
    public static final int DB_UNDEFINED_CODE = -3;
    public static final int DB_UPDATE_ERROR = -1;
    private static final String EVENTS_TIME_INDEX = ("CREATE INDEX IF NOT EXISTS time_idx ON " + Table.EVENTS.getName() + " (" + KEY_CREATED_AT + ");");
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_DATA = "data";
    private static final String LOGTAG = "MixpanelAPI.Database";
    private static final String PEOPLE_TIME_INDEX = ("CREATE INDEX IF NOT EXISTS time_idx ON " + Table.PEOPLE.getName() + " (" + KEY_CREATED_AT + ");");
    private final MPDatabaseHelper mDb;

    private static class MPDatabaseHelper extends SQLiteOpenHelper {
        private final MPConfig mConfig;
        private final File mDatabaseFile;

        MPDatabaseHelper(Context context, String dbName) {
            super(context, dbName, null, MPDbAdapter.DATABASE_VERSION);
            this.mDatabaseFile = context.getDatabasePath(dbName);
            this.mConfig = MPConfig.getInstance(context);
        }

        public void deleteDatabase() {
            close();
            this.mDatabaseFile.delete();
        }

        public void onCreate(SQLiteDatabase db) {
            if (MPConfig.DEBUG) {
                Log.v(MPDbAdapter.LOGTAG, "Creating a new Mixpanel events DB");
            }
            db.execSQL(MPDbAdapter.CREATE_EVENTS_TABLE);
            db.execSQL(MPDbAdapter.CREATE_PEOPLE_TABLE);
            db.execSQL(MPDbAdapter.EVENTS_TIME_INDEX);
            db.execSQL(MPDbAdapter.PEOPLE_TIME_INDEX);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (MPConfig.DEBUG) {
                Log.v(MPDbAdapter.LOGTAG, "Upgrading app, replacing Mixpanel events DB");
            }
            db.execSQL("DROP TABLE IF EXISTS " + Table.EVENTS.getName());
            db.execSQL("DROP TABLE IF EXISTS " + Table.PEOPLE.getName());
            db.execSQL(MPDbAdapter.CREATE_EVENTS_TABLE);
            db.execSQL(MPDbAdapter.CREATE_PEOPLE_TABLE);
            db.execSQL(MPDbAdapter.EVENTS_TIME_INDEX);
            db.execSQL(MPDbAdapter.PEOPLE_TIME_INDEX);
        }

        public boolean belowMemThreshold() {
            if (!this.mDatabaseFile.exists() || Math.max(this.mDatabaseFile.getUsableSpace(), (long) this.mConfig.getMinimumDatabaseLimit()) >= this.mDatabaseFile.length()) {
                return true;
            }
            return false;
        }
    }

    public enum Table {
        EVENTS("events"),
        PEOPLE("people");
        
        private final String mTableName;

        private Table(String name) {
            this.mTableName = name;
        }

        public String getName() {
            return this.mTableName;
        }
    }

    public MPDbAdapter(Context context) {
        this(context, DATABASE_NAME);
    }

    public MPDbAdapter(Context context, String dbName) {
        this.mDb = new MPDatabaseHelper(context, dbName);
    }

    public int addJSON(JSONObject j, Table table) {
        if (belowMemThreshold()) {
            String tableName = table.getName();
            Cursor c = null;
            int count = DB_UPDATE_ERROR;
            try {
                SQLiteDatabase db = this.mDb.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(KEY_DATA, j.toString());
                cv.put(KEY_CREATED_AT, Long.valueOf(System.currentTimeMillis()));
                db.insert(tableName, null, cv);
                c = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
                c.moveToFirst();
                count = c.getInt(0);
                return count;
            } catch (SQLiteException e) {
                Log.e(LOGTAG, "Could not add Mixpanel data to table " + tableName + ". Re-initializing database.", e);
                if (c != null) {
                    c.close();
                    c = null;
                }
                this.mDb.deleteDatabase();
                return count;
            } finally {
                if (c != null) {
                    c.close();
                }
                this.mDb.close();
            }
        } else {
            Log.e(LOGTAG, "There is not enough space left on the device to store Mixpanel data, so data was discarded");
            return DB_OUT_OF_MEMORY_ERROR;
        }
    }

    public void cleanupEvents(String last_id, Table table) {
        String tableName = table.getName();
        try {
            this.mDb.getWritableDatabase().delete(tableName, "_id <= " + last_id, null);
        } catch (SQLiteException e) {
            Log.e(LOGTAG, "Could not clean sent Mixpanel records from " + tableName + ". Re-initializing database.", e);
            this.mDb.deleteDatabase();
        } finally {
            this.mDb.close();
        }
    }

    public void cleanupEvents(long time, Table table) {
        String tableName = table.getName();
        try {
            this.mDb.getWritableDatabase().delete(tableName, "created_at <= " + time, null);
        } catch (SQLiteException e) {
            Log.e(LOGTAG, "Could not clean timed-out Mixpanel records from " + tableName + ". Re-initializing database.", e);
            this.mDb.deleteDatabase();
        } finally {
            this.mDb.close();
        }
    }

    public void deleteDB() {
        this.mDb.deleteDatabase();
    }

    public String[] generateDataString(Table table) {
        Cursor c = null;
        String data = null;
        String last_id = null;
        String tableName = table.getName();
        try {
            c = this.mDb.getReadableDatabase().rawQuery("SELECT * FROM " + tableName + " ORDER BY " + KEY_CREATED_AT + " ASC LIMIT 50", null);
            JSONArray arr = new JSONArray();
            while (c.moveToNext()) {
                if (c.isLast()) {
                    last_id = c.getString(c.getColumnIndex(ProfilesDBHelper.COLUMN_ID));
                }
                try {
                    arr.put(new JSONObject(c.getString(c.getColumnIndex(KEY_DATA))));
                } catch (JSONException e) {
                }
            }
            if (arr.length() > 0) {
                data = arr.toString();
            }
            this.mDb.close();
            if (c != null) {
                c.close();
            }
        } catch (SQLiteException e2) {
            Log.e(LOGTAG, "Could not pull records for Mixpanel out of database " + tableName + ". Waiting to send.", e2);
            last_id = null;
            data = null;
            this.mDb.close();
            if (c != null) {
                c.close();
            }
        } catch (Throwable th) {
            this.mDb.close();
            if (c != null) {
                c.close();
            }
        }
        if (last_id == null || data == null) {
            return null;
        }
        return new String[]{last_id, data};
    }

    protected boolean belowMemThreshold() {
        return this.mDb.belowMemThreshold();
    }
}
