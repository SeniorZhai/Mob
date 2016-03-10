package com.mobcrush.mobcrush.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.BuildConfig;

public class RealPathUtil {
    private static final String TAG = "RealPathUtil";

    public static String getRealPath(Context context, Uri uri) {
        if (VERSION.SDK_INT < 11) {
            return getRealPathFromURI_BelowAPI11(context, uri);
        }
        if (VERSION.SDK_INT < 19) {
            return getRealPathFromURI_API11to18(context, uri);
        }
        return getRealPathFromURI_API19(context, uri);
    }

    @SuppressLint({"NewApi"})
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = BuildConfig.FLAVOR;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String id = DocumentsContract.getDocumentId(uri).split(":")[1];
            String[] column = new String[]{"_data"};
            Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, column, "_id=?", new String[]{id}, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            return filePath;
        }
        Log.e(TAG, "Not a document: " + uri);
        Crashlytics.log("Not a document: " + uri);
        return BuildConfig.FLAVOR;
    }

    @SuppressLint({"NewApi"})
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        Cursor cursor = new CursorLoader(context, contentUri, new String[]{"_data"}, null, null, null).loadInBackground();
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        Cursor cursor = context.getContentResolver().query(contentUri, new String[]{"_data"}, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
