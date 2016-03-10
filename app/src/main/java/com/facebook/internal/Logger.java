package com.facebook.internal;

import android.util.Log;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import java.util.HashMap;
import java.util.Map.Entry;

public class Logger {
    public static final String LOG_TAG_BASE = "FacebookSDK.";
    private static final HashMap<String, String> stringsToReplace = new HashMap();
    private final LoggingBehavior behavior;
    private StringBuilder contents;
    private int priority = 3;
    private final String tag;

    public static synchronized void registerStringToReplace(String original, String replace) {
        synchronized (Logger.class) {
            stringsToReplace.put(original, replace);
        }
    }

    public static synchronized void registerAccessToken(String accessToken) {
        synchronized (Logger.class) {
            if (!FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.INCLUDE_ACCESS_TOKENS)) {
                registerStringToReplace(accessToken, "ACCESS_TOKEN_REMOVED");
            }
        }
    }

    public static void log(LoggingBehavior behavior, String tag, String string) {
        log(behavior, 3, tag, string);
    }

    public static void log(LoggingBehavior behavior, String tag, String format, Object... args) {
        if (FacebookSdk.isLoggingBehaviorEnabled(behavior)) {
            log(behavior, 3, tag, String.format(format, args));
        }
    }

    public static void log(LoggingBehavior behavior, int priority, String tag, String string) {
        if (FacebookSdk.isLoggingBehaviorEnabled(behavior)) {
            string = replaceStrings(string);
            if (!tag.startsWith(LOG_TAG_BASE)) {
                tag = LOG_TAG_BASE + tag;
            }
            Log.println(priority, tag, string);
            if (behavior == LoggingBehavior.DEVELOPER_ERRORS) {
                new Exception().printStackTrace();
            }
        }
    }

    private static synchronized String replaceStrings(String string) {
        synchronized (Logger.class) {
            for (Entry<String, String> entry : stringsToReplace.entrySet()) {
                string = string.replace((CharSequence) entry.getKey(), (CharSequence) entry.getValue());
            }
        }
        return string;
    }

    public Logger(LoggingBehavior behavior, String tag) {
        Validate.notNullOrEmpty(tag, "tag");
        this.behavior = behavior;
        this.tag = LOG_TAG_BASE + tag;
        this.contents = new StringBuilder();
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int value) {
        Validate.oneOf(Integer.valueOf(value), "value", Integer.valueOf(7), Integer.valueOf(3), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(5));
        this.priority = value;
    }

    public String getContents() {
        return replaceStrings(this.contents.toString());
    }

    public void log() {
        logString(this.contents.toString());
        this.contents = new StringBuilder();
    }

    public void logString(String string) {
        log(this.behavior, this.priority, this.tag, string);
    }

    public void append(StringBuilder stringBuilder) {
        if (shouldLog()) {
            this.contents.append(stringBuilder);
        }
    }

    public void append(String string) {
        if (shouldLog()) {
            this.contents.append(string);
        }
    }

    public void append(String format, Object... args) {
        if (shouldLog()) {
            this.contents.append(String.format(format, args));
        }
    }

    public void appendKeyValue(String key, Object value) {
        append("  %s:\t%s\n", key, value);
    }

    private boolean shouldLog() {
        return FacebookSdk.isLoggingBehaviorEnabled(this.behavior);
    }
}
