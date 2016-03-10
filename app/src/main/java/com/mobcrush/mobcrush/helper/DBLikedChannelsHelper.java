package com.mobcrush.mobcrush.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.datamodel.User;

public class DBLikedChannelsHelper extends SQLiteOpenHelper {
    public static final String CHANNEL = "channel";
    public static final String KEY_ID = "id";
    public static final String LIKED = "liked";
    public static final String TABLE = "channels";
    public static final String USER = "user";

    public DBLikedChannelsHelper(Context context) {
        super(context, context.getString(R.string.app_name), null, 2);
    }

    public static synchronized SQLiteDatabase getWritableDB(Context context) {
        SQLiteDatabase writableDatabase;
        synchronized (DBLikedChannelsHelper.class) {
            writableDatabase = new DBLikedChannelsHelper(context).getWritableDatabase();
        }
        return writableDatabase;
    }

    public static synchronized SQLiteDatabase getReadableDB(Context context) {
        SQLiteDatabase readableDatabase;
        synchronized (DBLikedChannelsHelper.class) {
            readableDatabase = new DBLikedChannelsHelper(context).getReadableDatabase();
        }
        return readableDatabase;
    }

    public static void saveLikedChannel(Context context, User user, String channel) {
        ContentValues cv = new ContentValues();
        cv.put(USER, user._id);
        cv.put(CHANNEL, channel);
        cv.put(LIKED, Integer.valueOf(1));
        SQLiteDatabase db = getWritableDB(context);
        if (db.update(TABLE, cv, "user LIKE '" + user._id + "' AND " + CHANNEL + " LIKE '" + channel + "'", null) == 0) {
            db.insert(TABLE, null, cv);
        }
        db.close();
    }

    public static boolean isChannelLiked(Context context, User user, String channel) {
        SQLiteDatabase db = getReadableDB(context);
        Cursor c = db.query(TABLE, new String[]{KEY_ID, USER, LIKED, CHANNEL}, "user LIKE '" + user._id + "' AND " + CHANNEL + " LIKE '" + channel + "'", null, KEY_ID, null, "id DESC");
        boolean liked = false;
        if (c.moveToFirst()) {
            if (TextUtils.equals(c.getString(1), user._id) && c.getInt(2) == 1) {
                liked = true;
            } else {
                liked = false;
            }
        }
        c.close();
        db.close();
        return liked;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE %s ( %s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s INTEGER);", new Object[]{TABLE, KEY_ID, USER, CHANNEL, LIKED}));
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS channels");
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}
