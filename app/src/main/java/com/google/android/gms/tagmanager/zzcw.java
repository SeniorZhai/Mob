package com.google.android.gms.tagmanager;

import com.google.android.gms.tagmanager.zzm.zza;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class zzcw<K, V> implements zzl<K, V> {
    private final Map<K, V> zzaNR = new HashMap();
    private final int zzaNS;
    private final zza<K, V> zzaNT;
    private int zzaNU;

    zzcw(int i, zza<K, V> com_google_android_gms_tagmanager_zzm_zza_K__V) {
        this.zzaNS = i;
        this.zzaNT = com_google_android_gms_tagmanager_zzm_zza_K__V;
    }

    public synchronized V get(K key) {
        return this.zzaNR.get(key);
    }

    public synchronized void zzf(K k, V v) {
        if (k == null || v == null) {
            throw new NullPointerException("key == null || value == null");
        }
        this.zzaNU += this.zzaNT.sizeOf(k, v);
        if (this.zzaNU > this.zzaNS) {
            Iterator it = this.zzaNR.entrySet().iterator();
            while (it.hasNext()) {
                Entry entry = (Entry) it.next();
                this.zzaNU -= this.zzaNT.sizeOf(entry.getKey(), entry.getValue());
                it.remove();
                if (this.zzaNU <= this.zzaNS) {
                    break;
                }
            }
        }
        this.zzaNR.put(k, v);
    }
}
