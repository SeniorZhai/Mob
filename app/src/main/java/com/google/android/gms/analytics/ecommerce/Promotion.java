package com.google.android.gms.analytics.ecommerce;

import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zznq;
import com.helpshift.HSFunnel;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Promotion {
    public static final String ACTION_CLICK = "click";
    public static final String ACTION_VIEW = "view";
    Map<String, String> zzJj = new HashMap();

    void put(String name, String value) {
        zzu.zzb((Object) name, (Object) "Name should be non-null");
        this.zzJj.put(name, value);
    }

    public Promotion setCreative(String value) {
        put(HSFunnel.CANCEL_CSAT_RATING, value);
        return this;
    }

    public Promotion setId(String value) {
        put(DBLikedChannelsHelper.KEY_ID, value);
        return this;
    }

    public Promotion setName(String value) {
        put("nm", value);
        return this;
    }

    public Promotion setPosition(String value) {
        put("ps", value);
        return this;
    }

    public String toString() {
        return zznq.zzD(this.zzJj);
    }

    public Map<String, String> zzaQ(String str) {
        Map<String, String> hashMap = new HashMap();
        for (Entry entry : this.zzJj.entrySet()) {
            hashMap.put(str + ((String) entry.getKey()), entry.getValue());
        }
        return hashMap;
    }
}
