package com.helpshift.util;

import android.database.sqlite.SQLiteDatabase;

public class DatabaseUtils {
    public static boolean exists(SQLiteDatabase database, String table, String query, String[] selectionArgs) {
        return android.database.DatabaseUtils.longForQuery(database, new StringBuilder().append("SELECT COUNT(*) FROM ").append(table).append(" WHERE ").append(query).append(" LIMIT 1").toString(), selectionArgs) > 0;
    }
}
