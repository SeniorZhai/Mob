package com.helpshift.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.helpshift.HelpshiftContext;
import com.helpshift.constants.IssueColumns;
import com.helpshift.constants.MessageColumns;
import com.helpshift.constants.Tables;
import com.helpshift.models.Issue;
import com.helpshift.models.IssueBuilder;
import com.helpshift.models.Message;
import com.helpshift.models.MessageBuilder;
import com.helpshift.util.DatabaseUtils;
import com.helpshift.util.ListUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;

public class IssuesDataSource {
    private static SQLiteDatabase database;
    private static final IssuesDBHelper helper = new IssuesDBHelper(HelpshiftContext.getApplicationContext());

    private static void write() {
        database = helper.getWritableDatabase();
    }

    private static void read() {
        database = helper.getReadableDatabase();
    }

    private static void close() {
        database.close();
    }

    public static void createDB() {
        synchronized (helper) {
            read();
            close();
        }
    }

    public static int storeIssue(Issue issue) {
        List<Issue> issueList = new ArrayList();
        issueList.add(issue);
        return storeIssues(issueList);
    }

    public static int storeIssues(List<Issue> issueList) {
        int newIssuesCount = 0;
        if (ListUtils.isEmpty(issueList)) {
            return 0;
        }
        synchronized (helper) {
            write();
            database.beginTransaction();
            String whereClause = "issue_id=?";
            for (Issue issue : issueList) {
                int newMessagesCount = insertMessagesUnsafe(issue.getMessageList());
                String[] whereArgs = new String[]{issue.getIssueId()};
                if (DatabaseUtils.exists(database, Tables.ISSUES, whereClause, whereArgs)) {
                    if (issue.getNewMessagesCount() != -1) {
                        newMessagesCount += getCurrentNewMessagesCount(whereClause, whereArgs);
                    }
                    database.update(Tables.ISSUES, issueToContentValue(issue, newMessagesCount), whereClause, whereArgs);
                } else {
                    database.insert(Tables.ISSUES, null, issueToContentValue(issue, 0));
                    newIssuesCount++;
                }
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            close();
        }
        return newIssuesCount;
    }

    private static int getCurrentNewMessagesCount(String whereClause, String[] whereArgs) {
        int currentNewMessagesCount = 0;
        Cursor cursor = database.query(Tables.ISSUES, new String[]{IssueColumns.NEW_MESSAGE_COUNT}, whereClause, whereArgs, null, null, null);
        if (cursor.moveToFirst()) {
            currentNewMessagesCount = cursor.getInt(cursor.getColumnIndex(IssueColumns.NEW_MESSAGE_COUNT));
        }
        cursor.close();
        return currentNewMessagesCount;
    }

    private static ContentValues issueToContentValue(Issue issue, int newMessagesCount) {
        ContentValues values = new ContentValues();
        values.put(ProfilesDBHelper.COLUMN_PROFILE_ID, issue.getProfileId());
        values.put(MessageColumns.ISSUE_ID, issue.getIssueId());
        values.put(MessageColumns.BODY, issue.getBody());
        values.put(SettingsJsonConstants.PROMPT_TITLE_KEY, issue.getTitle());
        values.put(MPDbAdapter.KEY_CREATED_AT, issue.getCreatedAt());
        values.put(IssueColumns.UPDATED_AT, issue.getUpdatedAt());
        values.put(SettingsJsonConstants.APP_STATUS_KEY, Integer.valueOf(issue.getStatus()));
        values.put(IssueColumns.NEW_MESSAGE_COUNT, Integer.valueOf(newMessagesCount));
        values.put(IssueColumns.SHOW_AGENT_NAME, Integer.valueOf(issue.isShowAgentName() ? 1 : 0));
        return values;
    }

    public static Issue getIssue(String issueId) {
        Issue issue = null;
        synchronized (helper) {
            read();
            Cursor cursor = database.query(Tables.ISSUES, null, "issue_id=?", new String[]{issueId}, null, null, null);
            if (cursor.moveToFirst()) {
                issue = cursorToIssue(cursor);
            }
            cursor.close();
            close();
        }
        if (issue != null) {
            issue.getMessageList();
        }
        return issue;
    }

    public static List<Issue> getIssues(String profileId) {
        List<Issue> issueList = new ArrayList();
        if (!TextUtils.isEmpty(profileId)) {
            synchronized (helper) {
                read();
                Cursor cursor = database.query(Tables.ISSUES, null, "profile_id=?", new String[]{profileId}, null, null, null);
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        issueList.add(cursorToIssue(cursor));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
                close();
            }
        }
        return issueList;
    }

    private static Issue cursorToIssue(Cursor cursor) {
        boolean z = true;
        String profileId = cursor.getString(cursor.getColumnIndex(ProfilesDBHelper.COLUMN_PROFILE_ID));
        String issueId = cursor.getString(cursor.getColumnIndex(MessageColumns.ISSUE_ID));
        String body = cursor.getString(cursor.getColumnIndex(MessageColumns.BODY));
        String title = cursor.getString(cursor.getColumnIndex(SettingsJsonConstants.PROMPT_TITLE_KEY));
        String createdAt = cursor.getString(cursor.getColumnIndex(MPDbAdapter.KEY_CREATED_AT));
        String updatedAt = cursor.getString(cursor.getColumnIndex(IssueColumns.UPDATED_AT));
        int status = cursor.getInt(cursor.getColumnIndex(SettingsJsonConstants.APP_STATUS_KEY));
        int newMessageCount = cursor.getInt(cursor.getColumnIndex(IssueColumns.NEW_MESSAGE_COUNT));
        if (cursor.getInt(cursor.getColumnIndex(IssueColumns.SHOW_AGENT_NAME)) != 1) {
            z = false;
        }
        return new IssueBuilder(profileId, issueId, body, title, createdAt, updatedAt, status, z).setNewMessagesCount(newMessageCount).build();
    }

    public static int storeMessages(List<Message> messageList) {
        if (ListUtils.isEmpty(messageList)) {
            return 0;
        }
        int newMessagesCount;
        synchronized (helper) {
            write();
            database.beginTransaction();
            newMessagesCount = insertMessagesUnsafe(messageList);
            database.setTransactionSuccessful();
            database.endTransaction();
            close();
        }
        return newMessagesCount;
    }

    public static int storeMessage(Message message) {
        int newMessageCount;
        synchronized (helper) {
            write();
            newMessageCount = 0 + insertMessageUnsafe(message);
            close();
        }
        return newMessageCount;
    }

    private static int insertMessagesUnsafe(List<Message> messageList) {
        int newMessagesCount = 0;
        for (Message message : messageList) {
            newMessagesCount += insertMessageUnsafe(message);
        }
        return newMessagesCount;
    }

    private static int insertMessageUnsafe(Message message) {
        String whereClause = "message_id=?";
        String[] whereArgs = new String[]{message.getMessageId()};
        if (DatabaseUtils.exists(database, Tables.MESSAGES, whereClause, whereArgs)) {
            database.update(Tables.MESSAGES, messageToContentValues(message), whereClause, whereArgs);
        } else {
            database.insert(Tables.MESSAGES, null, messageToContentValues(message));
            if (message.getOrigin().equals("admin")) {
                return 1;
            }
        }
        return 0;
    }

    private static ContentValues messageToContentValues(Message message) {
        int i;
        int i2 = 1;
        ContentValues values = new ContentValues();
        values.put(MessageColumns.ISSUE_ID, message.getIssueId());
        values.put(MessageColumns.MESSAGE_ID, message.getMessageId());
        values.put(MessageColumns.BODY, message.getBody());
        values.put(MessageColumns.ORIGIN, message.getOrigin());
        values.put(MessageColumns.TYPE, message.getType());
        values.put(MPDbAdapter.KEY_CREATED_AT, message.getCreatedAt());
        values.put(MessageColumns.AUTHOR, message.getAuthor());
        values.put(MessageColumns.META, message.getMeta());
        values.put(MessageColumns.SCREENSHOT, message.getScreenshot());
        String str = MessageColumns.MESSAGE_SEEN;
        if (message.isMessageSeen()) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        str = MessageColumns.INVISIBLE;
        if (message.isInvisible()) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        String str2 = MessageColumns.IN_PROGRESS;
        if (!message.isInProgress()) {
            i2 = 0;
        }
        values.put(str2, Integer.valueOf(i2));
        return values;
    }

    public static List<Message> getMessages(String issueId) {
        if (TextUtils.isEmpty(issueId)) {
            return new ArrayList();
        }
        return getMessages("issue_id=?", new String[]{issueId});
    }

    public static List<Message> getUnseenMessages(String issueId) {
        if (TextUtils.isEmpty(issueId)) {
            return new ArrayList();
        }
        return getMessages("issue_id=? AND message_seen=? AND origin=?", new String[]{issueId, Integer.toString(0), "admin"});
    }

    private static List<Message> getMessages(String whereClause, String[] whereArgs) {
        List<Message> messageList = new ArrayList();
        synchronized (helper) {
            read();
            Cursor cursor = database.query(Tables.MESSAGES, null, whereClause, whereArgs, null, null, null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    messageList.add(cursorToMessage(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            close();
        }
        return messageList;
    }

    public static Message getMessage(String messageId) {
        Message message = null;
        synchronized (helper) {
            read();
            Cursor cursor = database.query(Tables.MESSAGES, null, "message_id=?", new String[]{messageId}, null, null, null);
            if (cursor.moveToFirst()) {
                message = cursorToMessage(cursor);
            }
            cursor.close();
            close();
        }
        return message;
    }

    public static void deleteMessage(String messageId) {
        synchronized (helper) {
            write();
            database.delete(Tables.MESSAGES, "message_id=?", new String[]{messageId});
            close();
        }
    }

    private static Message cursorToMessage(Cursor cursor) {
        String issueId = cursor.getString(cursor.getColumnIndex(MessageColumns.ISSUE_ID));
        String messageId = cursor.getString(cursor.getColumnIndex(MessageColumns.MESSAGE_ID));
        String body = cursor.getString(cursor.getColumnIndex(MessageColumns.BODY));
        String origin = cursor.getString(cursor.getColumnIndex(MessageColumns.ORIGIN));
        String type = cursor.getString(cursor.getColumnIndex(MessageColumns.TYPE));
        String createdAt = cursor.getString(cursor.getColumnIndex(MPDbAdapter.KEY_CREATED_AT));
        String author = cursor.getString(cursor.getColumnIndex(MessageColumns.AUTHOR));
        String meta = cursor.getString(cursor.getColumnIndex(MessageColumns.META));
        return new MessageBuilder(issueId, messageId, body, origin, type, createdAt, author, meta).setScreenshot(cursor.getString(cursor.getColumnIndex(MessageColumns.SCREENSHOT))).setMessageSeen(cursor.getInt(cursor.getColumnIndex(MessageColumns.MESSAGE_SEEN)) == 1).setInvisible(cursor.getInt(cursor.getColumnIndex(MessageColumns.INVISIBLE)) == 1).setInProgress(cursor.getInt(cursor.getColumnIndex(MessageColumns.IN_PROGRESS)) == 1).build();
    }
}
