package com.google.android.gms.analytics.ecommerce;

import com.google.android.gms.analytics.zzc;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zznq;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Product {
    Map<String, String> zzJj = new HashMap();

    void put(String name, String value) {
        zzu.zzb((Object) name, (Object) "Name should be non-null");
        this.zzJj.put(name, value);
    }

    public Product setBrand(String value) {
        put(TtmlNode.TAG_BR, value);
        return this;
    }

    public Product setCategory(String value) {
        put("ca", value);
        return this;
    }

    public Product setCouponCode(String value) {
        put("cc", value);
        return this;
    }

    public Product setCustomDimension(int index, String value) {
        put(zzc.zzY(index), value);
        return this;
    }

    public Product setCustomMetric(int index, int value) {
        put(zzc.zzZ(index), Integer.toString(value));
        return this;
    }

    public Product setId(String value) {
        put(DBLikedChannelsHelper.KEY_ID, value);
        return this;
    }

    public Product setName(String value) {
        put("nm", value);
        return this;
    }

    public Product setPosition(int value) {
        put("ps", Integer.toString(value));
        return this;
    }

    public Product setPrice(double value) {
        put("pr", Double.toString(value));
        return this;
    }

    public Product setQuantity(int value) {
        put("qt", Integer.toString(value));
        return this;
    }

    public Product setVariant(String value) {
        put("va", value);
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
