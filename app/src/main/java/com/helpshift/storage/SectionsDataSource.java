package com.helpshift.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.helpshift.Log;
import com.helpshift.Section;
import com.helpshift.constants.SectionsColumns;
import com.helpshift.constants.Tables;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SectionsDataSource implements SectionDAO {
    private static final String TAG = "HelpShiftDebug";
    private SQLiteDatabase database;
    private final FaqsDBHelper dbHelper = FaqsDBHelper.getInstance();
    private FaqDAO faqDAO = new FaqsDataSource();

    public void write() {
        this.database = this.dbHelper.getWritableDatabase();
    }

    public void read() {
        this.database = this.dbHelper.getReadableDatabase();
    }

    public void close() {
        this.dbHelper.close();
    }

    public void storeSections(JSONArray sections) {
        synchronized (this.dbHelper) {
            write();
            try {
                this.database.beginTransaction();
                for (int i = 0; i < sections.length(); i++) {
                    JSONObject section = sections.getJSONObject(i);
                    this.database.insert(Tables.SECTIONS, null, sectionToContentValues(section));
                    JSONArray faqs = section.optJSONArray(Tables.FAQS);
                    if (faqs != null) {
                        FaqsDataSource.addFaqsUnsafe(this.database, section.getString(SectionsColumns.PUBLISH_ID), faqs);
                    }
                }
                this.database.setTransactionSuccessful();
                this.database.endTransaction();
            } catch (JSONException e) {
                Log.d(TAG, "JSONException", e);
                this.database.endTransaction();
            } catch (Throwable th) {
                this.database.endTransaction();
            }
            close();
        }
    }

    public Section getSection(String publishId) {
        if (publishId == null || publishId.equals(BuildConfig.FLAVOR)) {
            return new Section();
        }
        Section section = null;
        synchronized (this.dbHelper) {
            read();
            Cursor cursor = this.database.query(Tables.SECTIONS, null, "publish_id = ?", new String[]{publishId}, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                section = cursorToSection(cursor);
            }
            cursor.close();
            close();
        }
        return section;
    }

    public List<Section> getAllSections() {
        List<Section> sections = new ArrayList();
        synchronized (this.dbHelper) {
            read();
            Cursor cursor = this.database.query(Tables.SECTIONS, null, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                sections.add(cursorToSection(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            close();
        }
        return sections;
    }

    public void clearSectionsData() {
        synchronized (this.dbHelper) {
            write();
            this.dbHelper.dropTables(this.database);
            this.dbHelper.onCreate(this.database);
            close();
        }
    }

    private static Section cursorToSection(Cursor cursor) {
        return new Section(cursor.getLong(0), cursor.getString(1), cursor.getString(3), cursor.getString(2));
    }

    private static ContentValues sectionToContentValues(JSONObject section) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(SettingsJsonConstants.PROMPT_TITLE_KEY, section.getString(SettingsJsonConstants.PROMPT_TITLE_KEY));
        values.put(SectionsColumns.PUBLISH_ID, section.getString(SectionsColumns.PUBLISH_ID));
        values.put(SectionsColumns.SECTION_ID, section.getString(DBLikedChannelsHelper.KEY_ID));
        return values;
    }
}
