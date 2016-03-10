package com.mixpanel.android.mpmetrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;

public class InstallReferrerReceiver extends BroadcastReceiver {
    private static final String LOGTAG = "MixpanelAPI.InstRfrRcvr";
    private final Pattern UTM_CAMPAIGN_PATTERN = Pattern.compile("(^|&)utm_campaign=([^&#=]*)([#&]|$)");
    private final Pattern UTM_CONTENT_PATTERN = Pattern.compile("(^|&)utm_content=([^&#=]*)([#&]|$)");
    private final Pattern UTM_MEDIUM_PATTERN = Pattern.compile("(^|&)utm_medium=([^&#=]*)([#&]|$)");
    private final Pattern UTM_SOURCE_PATTERN = Pattern.compile("(^|&)utm_source=([^&#=]*)([#&]|$)");
    private final Pattern UTM_TERM_PATTERN = Pattern.compile("(^|&)utm_term=([^&#=]*)([#&]|$)");

    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String referrer = extras.getString("referrer");
            if (referrer != null) {
                Map<String, String> newPrefs = new HashMap();
                newPrefs.put("referrer", referrer);
                String source = find(this.UTM_SOURCE_PATTERN.matcher(referrer));
                if (source != null) {
                    newPrefs.put("utm_source", source);
                }
                String medium = find(this.UTM_MEDIUM_PATTERN.matcher(referrer));
                if (medium != null) {
                    newPrefs.put("utm_medium", medium);
                }
                String campaign = find(this.UTM_CAMPAIGN_PATTERN.matcher(referrer));
                if (campaign != null) {
                    newPrefs.put("utm_campaign", campaign);
                }
                String content = find(this.UTM_CONTENT_PATTERN.matcher(referrer));
                if (content != null) {
                    newPrefs.put("utm_content", content);
                }
                String term = find(this.UTM_TERM_PATTERN.matcher(referrer));
                if (term != null) {
                    newPrefs.put("utm_term", term);
                }
                PersistentIdentity.writeReferrerPrefs(context, "com.mixpanel.android.mpmetrics.ReferralInfo", newPrefs);
            }
        }
    }

    private String find(Matcher matcher) {
        if (matcher.find()) {
            String encoded = matcher.group(2);
            if (encoded != null) {
                try {
                    return URLDecoder.decode(encoded, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    Log.e(LOGTAG, "Could not decode a parameter into UTF-8");
                }
            }
        }
        return null;
    }
}
