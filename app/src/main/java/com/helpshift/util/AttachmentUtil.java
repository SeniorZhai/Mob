package com.helpshift.util;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.widget.Toast;
import com.helpshift.D.string;
import com.helpshift.HSApiData;
import com.helpshift.HSStorage;
import com.helpshift.Log;
import com.helpshift.constants.MessageColumns;
import com.helpshift.models.Issue;
import com.helpshift.models.Message;
import com.helpshift.storage.IssuesDataSource;
import com.helpshift.viewstructs.HSMsg;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class AttachmentUtil {
    private static final String FILE_TYPE_AUDIO = "audio/";
    private static final String FILE_TYPE_CSV = "text/csv";
    private static final String FILE_TYPE_MS_OFFICE_SUBSCRIPT = "vnd.openxmlformats-officedocument";
    private static final String FILE_TYPE_PDF = "application/pdf";
    private static final String FILE_TYPE_RTF = "text/rtf";
    private static final String FILE_TYPE_TEXT = "text/";
    private static final String FILE_TYPE_VIDEO = "video/";
    public static final String LOCAL_RSC_MSG_ID_PREFIX = "localRscMessage_";
    public static final int SCREENSHOT_ATTACH_REQ_CODE = 0;
    private static final String TAG = "HelpShiftDebug";

    private static void showScreenshotErrorToast(Activity activity) {
        Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(string.hs__screenshot_cloud_attach_error), 1).show();
    }

    private static void showScreenshotNotOfImageTypeErrorToast(Activity activity) {
        Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(string.hs__screenshot_upload_error_msg), 1).show();
    }

    private static String getPath(Activity activity, Uri selectedImageUri) {
        Cursor cursor = activity.managedQuery(selectedImageUri, new String[]{"_data"}, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String getPath(Activity activity, Intent dataIntent) {
        try {
            String screenshot = getPath(activity, dataIntent.getData());
            File screenshotFile = new File(screenshot);
            if (screenshotFile.exists()) {
                if (screenshotFile.length() <= new Long(5242880).longValue()) {
                    return screenshot;
                }
                Toast.makeText(activity.getApplicationContext(), String.format(activity.getResources().getString(string.hs__screenshot_limit_error), new Object[]{Float.valueOf(((float) screenshotLimit.longValue()) / 1048576.0f)}), 1).show();
                return null;
            }
            showScreenshotErrorToast(activity);
            return null;
        } catch (NullPointerException e) {
            showScreenshotErrorToast(activity);
            return null;
        }
    }

    public static Bitmap getBitmap(String path, int scale) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Options options = new Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (scale <= 0) {
            return bitmap;
        }
        return Bitmap.createScaledBitmap(bitmap, scale, (int) (((float) scale) * (((float) options.outHeight) / ((float) options.outWidth))), false);
    }

    public static Bitmap getUnscaledBitmap(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Options options = new Options();
        options.inScaled = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static String copyAttachment(Activity activity, HSApiData hsApiData, String filename, String messageId, int attachId) throws IOException {
        String absolutePath;
        NullPointerException e;
        Throwable th;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            String outputName = messageId + attachId + "-thumbnail";
            File outputFile = new File(activity.getFilesDir(), outputName);
            absolutePath = outputFile.getAbsolutePath();
            if (!outputFile.exists()) {
                hsApiData.storeFile(outputName);
                InputStream input2 = new FileInputStream(new File(filename));
                try {
                    output = activity.openFileOutput(outputName, SCREENSHOT_ATTACH_REQ_CODE);
                    byte[] data = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
                    while (true) {
                        int read = input2.read(data);
                        if (read == -1) {
                            break;
                        }
                        output.write(data, SCREENSHOT_ATTACH_REQ_CODE, read);
                    }
                    input = input2;
                } catch (NullPointerException e2) {
                    e = e2;
                    input = input2;
                    try {
                        Log.d(TAG, "NPE", e);
                        absolutePath = null;
                        if (output != null) {
                            output.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        return absolutePath;
                    } catch (Throwable th2) {
                        th = th2;
                        if (output != null) {
                            output.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    input = input2;
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    throw th;
                }
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        } catch (NullPointerException e3) {
            e = e3;
            Log.d(TAG, "NPE", e);
            absolutePath = null;
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            return absolutePath;
        }
        return absolutePath;
    }

    public static HSMsg addAndGetLocalRscMsg(HSStorage storage, String issueId, String screenshot) {
        return addAndGetLocalRscMsg(storage, issueId, screenshot, false);
    }

    public static HSMsg addAndGetLocalRscMsg(HSStorage storage, String issueId, String screenshot, boolean inProgress) {
        try {
            String messageId = LOCAL_RSC_MSG_ID_PREFIX + UUID.randomUUID().toString();
            String date = resolveTimestamp(IssuesDataSource.getIssue(issueId));
            String body = "Attaching Screenshot...";
            JSONObject message = new JSONObject();
            message.put(DBLikedChannelsHelper.KEY_ID, messageId);
            message.put(MessageColumns.ISSUE_ID, issueId);
            message.put(MessageColumns.TYPE, "rsc");
            message.put(MessageColumns.ORIGIN, "admin");
            message.put(MessageColumns.BODY, body);
            message.put(MessageColumns.INVISIBLE, false);
            message.put(MessageColumns.SCREENSHOT, screenshot);
            message.put("state", SCREENSHOT_ATTACH_REQ_CODE);
            message.put("inProgress", inProgress);
            message.put(MPDbAdapter.KEY_CREATED_AT, date);
            message.put("seen", true);
            message.put(MessageColumns.AUTHOR, new JSONObject());
            message.put(MessageColumns.META, new JSONObject());
            JSONArray messages = new JSONArray();
            messages.put(message);
            IssuesDataSource.storeMessages(IssuesUtil.jsonArrayToMessageList(messages));
            return new HSMsg(messageId, "rsc", "admin", body, date, Boolean.valueOf(false), screenshot, SCREENSHOT_ATTACH_REQ_CODE, Boolean.valueOf(false), BuildConfig.FLAVOR);
        } catch (JSONException e) {
            Log.d(TAG, "addAndGetLocalRscMessage", e);
            return null;
        }
    }

    private static String resolveTimestamp(Issue issue) {
        Date localTs = new Date();
        try {
            List<Message> messageList = issue.getMessageList();
            Date lastMessageTs = HSFormat.issueTsFormat.parse(((Message) messageList.get(messageList.size() - 1)).getCreatedAt());
            if (localTs.before(lastMessageTs)) {
                localTs.setTime(lastMessageTs.getTime() + 3000);
            }
        } catch (ParseException e) {
            Log.d(TAG, "resolveDate", e);
        }
        return HSFormat.issueTsFormat.format(localTs);
    }

    public static boolean isImageUri(Activity activity, Intent dataIntent) {
        if (new HashSet(Arrays.asList(new String[]{"image/jpeg", "image/png", "image/gif", "image/x-png", "image/x-citrix-pjpeg", "image/x-citrix-gif", "image/pjpeg"})).contains(activity.getContentResolver().getType(dataIntent.getData()))) {
            return true;
        }
        showScreenshotNotOfImageTypeErrorToast(activity);
        return false;
    }

    public static String getFileType(Activity activity, String contentType, String fileName) {
        if (contentType.contains(FILE_TYPE_AUDIO)) {
            return activity.getResources().getString(string.hs__file_type_audio);
        }
        if (contentType.contains(FILE_TYPE_VIDEO)) {
            return activity.getResources().getString(string.hs__file_type_video);
        }
        if (contentType.contains(FILE_TYPE_PDF)) {
            return activity.getResources().getString(string.hs__file_type_pdf);
        }
        if (contentType.contains(FILE_TYPE_MS_OFFICE_SUBSCRIPT)) {
            return activity.getResources().getString(string.hs__file_type_ms_office);
        }
        if (contentType.equals(FILE_TYPE_RTF)) {
            return activity.getResources().getString(string.hs__file_type_rtf);
        }
        if (contentType.equals(FILE_TYPE_CSV)) {
            return activity.getResources().getString(string.hs__file_type_csv);
        }
        if (contentType.equals(FILE_TYPE_TEXT)) {
            return activity.getResources().getString(string.hs__file_type_text);
        }
        String[] split = fileName.split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        split = contentType.split("/");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return activity.getResources().getString(string.hs__file_type_unknown);
    }
}
