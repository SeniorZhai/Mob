package com.google.android.gms.tagmanager;

import android.util.LruCache;
import com.google.android.gms.tagmanager.zzm.zza;

class zzba<K, V> implements zzl<K, V> {
    private LruCache<K, V> zzaMe;

    zzba(int i, final zza<K, V> com_google_android_gms_tagmanager_zzm_zza_K__V) {
        this.zzaMe = new LruCache<K, V>(this, i) {
            final /* synthetic */ zzba zzaMg;

            protected int sizeOf(K key, V value) {
                return com_google_android_gms_tagmanager_zzm_zza_K__V.sizeOf(key, value);
            }
        };
    }

    public V get(K key) {
        return this.zzaMe.get(key);
    }

    public void zzf(K k, V v) {
        this.zzaMe.put(k, v);
    }
}
