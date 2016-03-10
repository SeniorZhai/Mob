package com.helpshift;

import com.helpshift.res.values.HSConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public final class Log {
    static final int LIMIT = 100;
    private static final String TAG = "HelpShiftDebug";
    private static LinkedList<HashMap> logs = new LinkedList();

    private static void updateLogs(HashMap log) {
        Integer logLimit = (Integer) HSConfig.configData.get("dbgl");
        if (logs.size() > LIMIT) {
            try {
                logs.removeLast();
            } catch (NoSuchElementException e) {
                android.util.Log.d(TAG, "Exception No Such Element", e);
            }
        }
        if (logLimit.intValue() != 0) {
            logs.addFirst(log);
        }
    }

    public static ArrayList<HashMap> getLogs(int limit) {
        ArrayList<HashMap> logOutput = new ArrayList();
        int size = logs.size();
        int i = 0;
        while (i < size && i < limit) {
            try {
                logOutput.add(logs.removeFirst());
            } catch (NoSuchElementException e) {
                android.util.Log.d(TAG, "Exception No Such Element", e);
            }
            i++;
        }
        logs.clear();
        return logOutput;
    }

    private static void appendLog(Integer level, String tag, String msg) {
        HashMap log = new HashMap();
        log.put(SettingsJsonConstants.PROMPT_MESSAGE_KEY, msg);
        log.put("level", level);
        log.put("tag", tag);
        updateLogs(log);
    }

    private static void appendLog(Integer level, String tag, String msg, Throwable tr) {
        HashMap log = new HashMap();
        log.put(SettingsJsonConstants.PROMPT_MESSAGE_KEY, msg);
        log.put("level", level);
        log.put("tag", tag);
        log.put("exception", getStackTraceString(tr));
        updateLogs(log);
    }

    private static void appendLog(Integer level, String tag, Throwable tr) {
        HashMap log = new HashMap();
        log.put("level", level);
        log.put("tag", tag);
        log.put("exception", getStackTraceString(tr));
        updateLogs(log);
    }

    public static int v(String tag, String msg) {
        appendLog(Integer.valueOf(2), tag, msg);
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(2), tag, msg, tr);
        return android.util.Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        appendLog(Integer.valueOf(3), tag, msg);
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(3), tag, msg, tr);
        return android.util.Log.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        appendLog(Integer.valueOf(4), tag, msg);
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(4), tag, msg, tr);
        return android.util.Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        appendLog(Integer.valueOf(5), tag, msg);
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(5), tag, msg, tr);
        return android.util.Log.e(tag, msg, tr);
    }

    public static boolean isLoggable(String tag, int level) {
        return android.util.Log.isLoggable(tag, level);
    }

    public static int w(String tag, Throwable tr) {
        appendLog(Integer.valueOf(5), tag, tr);
        return android.util.Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        appendLog(Integer.valueOf(6), tag, msg);
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(6), tag, msg, tr);
        return android.util.Log.e(tag, msg, tr);
    }

    public static int wtf(String tag, String msg) {
        appendLog(Integer.valueOf(-1), tag, msg);
        return android.util.Log.wtf(tag, msg);
    }

    public static int wtf(String tag, Throwable tr) {
        appendLog(Integer.valueOf(-1), tag, tr);
        return android.util.Log.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        appendLog(Integer.valueOf(-1), tag, msg, tr);
        return android.util.Log.wtf(tag, msg, tr);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static int println(int priority, String tag, String msg) {
        return android.util.Log.println(priority, tag, msg);
    }
}
