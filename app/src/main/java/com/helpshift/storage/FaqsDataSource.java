package com.helpshift.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.facebook.internal.ServerProtocol;
import com.helpshift.Faq;
import com.helpshift.Log;
import com.helpshift.constants.FaqsColumns;
import com.helpshift.constants.MessageColumns;
import com.helpshift.constants.SectionsColumns;
import com.helpshift.constants.Tables;
import com.helpshift.util.HSJSONUtils;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FaqsDataSource implements FaqDAO {
    private static final String TAG = "HelpShiftDebug";
    private SQLiteDatabase database;
    private final FaqsDBHelper dbHelper = FaqsDBHelper.getInstance();

    public void write() {
        this.database = this.dbHelper.getWritableDatabase();
    }

    public void read() {
        this.database = this.dbHelper.getReadableDatabase();
    }

    public void close() {
        this.dbHelper.close();
    }

    private void createFaq(Faq faq) {
        ContentValues values = faqToContentValues(faq);
        synchronized (this.dbHelper) {
            write();
            this.database.insert(Tables.FAQS, null, values);
            close();
        }
    }

    private void updateFaq(Faq faq) {
        ContentValues values = faqToContentValues(faq);
        synchronized (this.dbHelper) {
            write();
            this.database.update(Tables.FAQS, values, "question_id = ?", new String[]{faq.getId()});
            close();
        }
    }

    public void addFaq(Faq faq) {
        if (getFaq(faq.getPublishId()) == null) {
            createFaq(faq);
        } else {
            updateFaq(faq);
        }
    }

    public Faq getFaq(String publishId) {
        if (TextUtils.isEmpty(publishId)) {
            return new Faq();
        }
        Faq faq = null;
        synchronized (this.dbHelper) {
            read();
            Cursor cursor = this.database.query(Tables.FAQS, null, "publish_id = ?", new String[]{publishId}, null, null, null);
            if (cursor.moveToFirst()) {
                faq = cursorToFaq(cursor);
            }
            cursor.close();
            close();
        }
        return faq;
    }

    public List<Faq> getFaqsDataForSection(String sectionPublishId) {
        if (TextUtils.isEmpty(sectionPublishId)) {
            return new ArrayList();
        }
        List<Faq> faqs = new ArrayList();
        synchronized (this.dbHelper) {
            read();
            Cursor cursor = this.database.query(Tables.FAQS, null, "section_id = ?", new String[]{sectionPublishId}, null, null, null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    faqs.add(cursorToFaq(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            close();
        }
        return faqs;
    }

    public List<Faq> getFaqsForSection(String sectionPublishId) {
        if (TextUtils.isEmpty(sectionPublishId)) {
            return new ArrayList();
        }
        List<Faq> faqs = new ArrayList();
        synchronized (this.dbHelper) {
            read();
            Cursor cursor = this.database.query(Tables.FAQS, FaqsColumns.UI_COLUMNS, "section_id = ?", new String[]{sectionPublishId}, null, null, null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    faqs.add(cursorToFaqForUI(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            close();
        }
        return faqs;
    }

    public int setIsHelpful(String questionId, Boolean state) {
        int i = 1;
        if (TextUtils.isEmpty(questionId)) {
            return 0;
        }
        int returnVal;
        ContentValues values = new ContentValues();
        String str = FaqsColumns.HELPFUL;
        if (!state.booleanValue()) {
            i = -1;
        }
        values.put(str, Integer.valueOf(i));
        synchronized (this.dbHelper) {
            write();
            returnVal = this.database.update(Tables.FAQS, values, "question_id = ?", new String[]{questionId});
            close();
        }
        return returnVal;
    }

    public static void addFaqsUnsafe(SQLiteDatabase database, String sectionPublishId, JSONArray faqs) {
        int j = 0;
        while (j < faqs.length()) {
            try {
                database.insert(Tables.FAQS, null, faqToContentValues(sectionPublishId, faqs.getJSONObject(j)));
                j++;
            } catch (JSONException e) {
                Log.d(TAG, "JSONException", e);
                return;
            }
        }
    }

    private static Faq cursorToFaq(Cursor cursor) {
        boolean z = true;
        long j = cursor.getLong(0);
        String string = cursor.getString(1);
        String string2 = cursor.getString(2);
        String string3 = cursor.getString(3);
        String string4 = cursor.getString(4);
        String string5 = cursor.getString(5);
        int i = cursor.getInt(6);
        if (cursor.getInt(7) != 1) {
            z = false;
        }
        return new Faq(j, string, string2, string3, string4, string5, i, Boolean.valueOf(z), HSJSONUtils.jsonToStringArrayList(cursor.getString(8)));
    }

    private static Faq cursorToFaqForUI(Cursor cursor) {
        return new Faq(0, cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), BuildConfig.FLAVOR, 0, Boolean.valueOf(false), new ArrayList());
    }

    private static ContentValues faqToContentValues(Faq faq) {
        ContentValues values = new ContentValues();
        values.put(FaqsColumns.QUESTION_ID, faq.getId());
        values.put(SectionsColumns.PUBLISH_ID, faq.getPublishId());
        values.put(SectionsColumns.SECTION_ID, faq.getSectionPublishId());
        values.put(SettingsJsonConstants.PROMPT_TITLE_KEY, faq.getTitle());
        values.put(MessageColumns.BODY, faq.getBody());
        values.put(FaqsColumns.HELPFUL, Integer.valueOf(faq.getIsHelpful()));
        values.put(FaqsColumns.RTL, faq.getIsRtl());
        values.put(FaqsColumns.TAGS, String.valueOf(new JSONArray(faq.getTags())));
        return values;
    }

    private static ContentValues faqToContentValues(String sectionPublishId, JSONObject faq) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(FaqsColumns.QUESTION_ID, faq.getString(DBLikedChannelsHelper.KEY_ID));
        values.put(SectionsColumns.PUBLISH_ID, faq.getString(SectionsColumns.PUBLISH_ID));
        values.put(SectionsColumns.SECTION_ID, sectionPublishId);
        values.put(SettingsJsonConstants.PROMPT_TITLE_KEY, faq.getString(SettingsJsonConstants.PROMPT_TITLE_KEY));
        values.put(MessageColumns.BODY, faq.getString(MessageColumns.BODY));
        values.put(FaqsColumns.HELPFUL, Integer.valueOf(0));
        values.put(FaqsColumns.RTL, Boolean.valueOf(faq.getString("is_rtl").equals(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE)));
        values.put(FaqsColumns.TAGS, faq.has("stags") ? faq.optJSONArray("stags").toString() : new JSONArray().toString());
        return values;
    }
}
