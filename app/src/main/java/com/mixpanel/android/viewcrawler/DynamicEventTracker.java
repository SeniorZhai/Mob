package com.mixpanel.android.viewcrawler;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.viewcrawler.ViewVisitor.OnEventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

class DynamicEventTracker implements OnEventListener {
    private static final int DEBOUNCE_TIME_MILLIS = 1000;
    private static String LOGTAG = "MixpanelAPI.DynamicEventTracker";
    private static final int MAX_PROPERTY_LENGTH = 128;
    private final Map<Signature, UnsentEvent> mDebouncedEvents = new HashMap();
    private final Handler mHandler;
    private final MixpanelAPI mMixpanel;
    private final Runnable mTask = new SendDebouncedTask();

    private final class SendDebouncedTask implements Runnable {
        private SendDebouncedTask() {
        }

        public void run() {
            long now = System.currentTimeMillis();
            synchronized (DynamicEventTracker.this.mDebouncedEvents) {
                Iterator<Entry<Signature, UnsentEvent>> iter = DynamicEventTracker.this.mDebouncedEvents.entrySet().iterator();
                while (iter.hasNext()) {
                    UnsentEvent val = (UnsentEvent) ((Entry) iter.next()).getValue();
                    if (now - val.timeSentMillis > 1000) {
                        DynamicEventTracker.this.mMixpanel.track(val.eventName, val.properties);
                        iter.remove();
                    }
                }
                if (!DynamicEventTracker.this.mDebouncedEvents.isEmpty()) {
                    DynamicEventTracker.this.mHandler.postDelayed(this, 500);
                }
            }
        }
    }

    private static class Signature {
        private final int mHashCode;

        public Signature(View view, String eventName) {
            this.mHashCode = view.hashCode() ^ eventName.hashCode();
        }

        public boolean equals(Object o) {
            if ((o instanceof Signature) && this.mHashCode == o.hashCode()) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.mHashCode;
        }
    }

    private static class UnsentEvent {
        public final String eventName;
        public final JSONObject properties;
        public final long timeSentMillis;

        public UnsentEvent(String name, JSONObject props, long timeSent) {
            this.eventName = name;
            this.properties = props;
            this.timeSentMillis = timeSent;
        }
    }

    public DynamicEventTracker(MixpanelAPI mixpanel, Handler homeHandler) {
        this.mMixpanel = mixpanel;
        this.mHandler = homeHandler;
    }

    public void OnEvent(View v, String eventName, boolean debounce) {
        long moment = System.currentTimeMillis();
        JSONObject properties = new JSONObject();
        try {
            properties.put("$text", textPropertyFromView(v));
            properties.put("$from_binding", true);
            properties.put("time", moment / 1000);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Can't format properties from view due to JSON issue", e);
        }
        if (debounce) {
            Signature eventSignature = new Signature(v, eventName);
            UnsentEvent event = new UnsentEvent(eventName, properties, moment);
            synchronized (this.mDebouncedEvents) {
                boolean needsRestart = this.mDebouncedEvents.isEmpty();
                this.mDebouncedEvents.put(eventSignature, event);
                if (needsRestart) {
                    this.mHandler.postDelayed(this.mTask, 1000);
                }
            }
            return;
        }
        this.mMixpanel.track(eventName, properties);
    }

    private static String textPropertyFromView(View v) {
        if (v instanceof TextView) {
            CharSequence retSequence = ((TextView) v).getText();
            if (retSequence != null) {
                return retSequence.toString();
            }
            return null;
        } else if (!(v instanceof ViewGroup)) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            ViewGroup vGroup = (ViewGroup) v;
            int childCount = vGroup.getChildCount();
            boolean textSeen = false;
            for (int i = 0; i < childCount && builder.length() < MAX_PROPERTY_LENGTH; i++) {
                String childText = textPropertyFromView(vGroup.getChildAt(i));
                if (childText != null && childText.length() > 0) {
                    if (textSeen) {
                        builder.append(", ");
                    }
                    builder.append(childText);
                    textSeen = true;
                }
            }
            if (builder.length() > MAX_PROPERTY_LENGTH) {
                return builder.substring(0, MAX_PROPERTY_LENGTH);
            }
            if (textSeen) {
                return builder.toString();
            }
            return null;
        }
    }
}
