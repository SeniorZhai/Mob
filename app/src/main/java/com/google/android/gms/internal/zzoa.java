package com.google.android.gms.internal;

import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.google.android.gms.analytics.ecommerce.Promotion;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class zzoa extends zznq<zzoa> {
    private ProductAction zzIC;
    private final Map<String, List<Product>> zzID = new HashMap();
    private final List<Promotion> zzIE = new ArrayList();
    private final List<Product> zzIF = new ArrayList();

    public String toString() {
        Map hashMap = new HashMap();
        if (!this.zzIF.isEmpty()) {
            hashMap.put("products", this.zzIF);
        }
        if (!this.zzIE.isEmpty()) {
            hashMap.put("promotions", this.zzIE);
        }
        if (!this.zzID.isEmpty()) {
            hashMap.put("impressions", this.zzID);
        }
        hashMap.put("productAction", this.zzIC);
        return zznq.zzy(hashMap);
    }

    public void zza(Product product, String str) {
        if (product != null) {
            Object obj;
            if (str == null) {
                obj = BuildConfig.FLAVOR;
            }
            if (!this.zzID.containsKey(obj)) {
                this.zzID.put(obj, new ArrayList());
            }
            ((List) this.zzID.get(obj)).add(product);
        }
    }

    public void zza(zzoa com_google_android_gms_internal_zzoa) {
        com_google_android_gms_internal_zzoa.zzIF.addAll(this.zzIF);
        com_google_android_gms_internal_zzoa.zzIE.addAll(this.zzIE);
        for (Entry entry : this.zzID.entrySet()) {
            String str = (String) entry.getKey();
            for (Product zza : (List) entry.getValue()) {
                com_google_android_gms_internal_zzoa.zza(zza, str);
            }
        }
        if (this.zzIC != null) {
            com_google_android_gms_internal_zzoa.zzIC = this.zzIC;
        }
    }

    public ProductAction zzwu() {
        return this.zzIC;
    }

    public List<Product> zzwv() {
        return Collections.unmodifiableList(this.zzIF);
    }

    public Map<String, List<Product>> zzww() {
        return this.zzID;
    }

    public List<Promotion> zzwx() {
        return Collections.unmodifiableList(this.zzIE);
    }
}
