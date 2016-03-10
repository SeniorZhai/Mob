package com.mobcrush.mobcrush.datamodel;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import java.util.Map;
import org.json.JSONObject;

public class ChatRateLimit extends DataModel {
    public static final String ENABLED = "enabled";
    public static final String RATE = "rate";
    public static final String RATE_LIMIT = "rateLimit";
    public static final String SECONDS_PER = "secondsPer";
    public boolean enabled;
    public int rate;
    public int secondsPer;

    public static ChatRateLimit from(DataSnapshot snapshot) {
        ChatRateLimit limit = new ChatRateLimit();
        if (snapshot != null) {
            try {
                Map map = (Map) snapshot.getValue(Map.class);
                if (map.containsKey(RATE_LIMIT)) {
                    JSONObject o = new JSONObject(String.valueOf(map.get(RATE_LIMIT)));
                    limit.secondsPer = o.optInt(SECONDS_PER);
                    limit.rate = o.optInt(RATE);
                    limit.enabled = o.optBoolean(ENABLED, false);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
        return limit;
    }
}
