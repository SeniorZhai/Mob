package com.helpshift.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.helpshift.model.Profile;
import com.helpshift.util.DBUtil;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class ProfilesDataSource {
    private static final String TAG = "HelpShiftDebug";
    private String[] allColumns = new String[]{ProfilesDBHelper.COLUMN_ID, SettingsJsonConstants.APP_IDENTIFIER_KEY, ProfilesDBHelper.COLUMN_PROFILE_ID, ProfilesDBHelper.COLUMN_NAME, ProfilesDBHelper.COLUMN_EMAIL, ProfilesDBHelper.COLUMN_SALT};
    private SQLiteDatabase database;
    private ProfilesDBHelper dbHelper;

    public ProfilesDataSource(Context context) {
        this.dbHelper = new ProfilesDBHelper(context);
    }

    public void write() {
        this.database = this.dbHelper.getWritableDatabase();
    }

    public void read() {
        this.database = this.dbHelper.getReadableDatabase();
    }

    public void close() {
        this.dbHelper.close();
    }

    public synchronized void addProfile(Profile profile) {
        if (getProfile(profile.getIdentifier()) == null) {
            createProfile(profile);
        } else {
            updateProfile(profile);
        }
        DBUtil.backupDatabase(ProfilesDBHelper.DATABASE_NAME);
    }

    public synchronized void createProfile(Profile profile) {
        write();
        this.database.insert(ProfilesDBHelper.TABLE_PROFILES, null, profileToContentValues(profile));
        close();
    }

    public synchronized void updateProfile(Profile profile) {
        write();
        this.database.update(ProfilesDBHelper.TABLE_PROFILES, profileToContentValues(profile), "identifier = '" + profile.getIdentifier() + "'", null);
        close();
    }

    public synchronized Profile getProfile(String identifier) {
        Profile profile;
        read();
        Cursor cursor = this.database.query(ProfilesDBHelper.TABLE_PROFILES, this.allColumns, "identifier = '" + identifier + "'", null, null, null, null);
        profile = null;
        if (cursor.moveToFirst()) {
            profile = cursorToProfile(cursor);
        }
        cursor.close();
        close();
        return profile;
    }

    private Profile cursorToProfile(Cursor cursor) {
        Profile profile = new Profile(cursor.getString(1));
        profile.setId(Long.valueOf(cursor.getLong(0)));
        profile.setProfileId(cursor.getString(2));
        profile.setName(cursor.getString(3));
        profile.setEmail(cursor.getString(4));
        profile.setSalt(cursor.getString(5));
        return profile;
    }

    private ContentValues profileToContentValues(Profile profile) {
        ContentValues values = new ContentValues();
        values.put(SettingsJsonConstants.APP_IDENTIFIER_KEY, profile.getIdentifier());
        values.put(ProfilesDBHelper.COLUMN_PROFILE_ID, profile.getProfileId());
        values.put(ProfilesDBHelper.COLUMN_NAME, profile.getName());
        values.put(ProfilesDBHelper.COLUMN_EMAIL, profile.getEmail());
        values.put(ProfilesDBHelper.COLUMN_SALT, profile.getSalt());
        return values;
    }
}
