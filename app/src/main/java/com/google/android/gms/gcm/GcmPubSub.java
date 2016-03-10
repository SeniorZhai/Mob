package com.google.android.gms.gcm;

import android.content.Context;
import android.os.Bundle;
import com.google.android.gms.iid.InstanceID;
import java.io.IOException;
import java.util.regex.Pattern;

public class GcmPubSub {
    private static GcmPubSub zzavH;
    private static final Pattern zzavJ = Pattern.compile("/topics/[a-zA-Z0-9-_.~%]{1,900}");
    private InstanceID zzavI;

    private GcmPubSub(Context context) {
        this.zzavI = InstanceID.getInstance(context);
    }

    public static synchronized GcmPubSub getInstance(Context context) {
        GcmPubSub gcmPubSub;
        synchronized (GcmPubSub.class) {
            if (zzavH == null) {
                zzavH = new GcmPubSub(context);
            }
            gcmPubSub = zzavH;
        }
        return gcmPubSub;
    }

    public void subscribe(String registrationToken, String topic, Bundle extras) throws IOException {
        if (registrationToken == null || registrationToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid appInstanceToken: " + registrationToken);
        } else if (topic == null || !zzavJ.matcher(topic).matches()) {
            throw new IllegalArgumentException("Invalid topic name: " + topic);
        } else {
            if (extras == null) {
                extras = new Bundle();
            }
            extras.putString("gcm.topic", topic);
            this.zzavI.getToken(registrationToken, topic, extras);
        }
    }

    public void unsubscribe(String registrationToken, String topic) throws IOException {
        Bundle bundle = new Bundle();
        bundle.putString("gcm.topic", topic);
        this.zzavI.zzb(registrationToken, topic, bundle);
    }
}
