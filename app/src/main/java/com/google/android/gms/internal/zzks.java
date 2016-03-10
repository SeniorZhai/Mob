package com.google.android.gms.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class zzks<K, V> extends zzkw<K, V> implements Map<K, V> {
    zzkv<K, V> zzabl;

    private zzkv<K, V> zzog() {
        if (this.zzabl == null) {
            this.zzabl = new zzkv<K, V>(this) {
                final /* synthetic */ zzks zzabm;

                {
                    this.zzabm = r1;
                }

                protected void colClear() {
                    this.zzabm.clear();
                }

                protected Object colGetEntry(int index, int offset) {
                    return this.zzabm.mArray[(index << 1) + offset];
                }

                protected Map<K, V> colGetMap() {
                    return this.zzabm;
                }

                protected int colGetSize() {
                    return this.zzabm.mSize;
                }

                protected int colIndexOfKey(Object key) {
                    return key == null ? this.zzabm.indexOfNull() : this.zzabm.indexOf(key, key.hashCode());
                }

                protected int colIndexOfValue(Object value) {
                    return this.zzabm.indexOfValue(value);
                }

                protected void colPut(K key, V value) {
                    this.zzabm.put(key, value);
                }

                protected void colRemoveAt(int index) {
                    this.zzabm.removeAt(index);
                }

                protected V colSetValue(int index, V value) {
                    return this.zzabm.setValueAt(index, value);
                }
            };
        }
        return this.zzabl;
    }

    public Set<Entry<K, V>> entrySet() {
        return zzog().getEntrySet();
    }

    public Set<K> keySet() {
        return zzog().getKeySet();
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Entry entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Collection<V> values() {
        return zzog().getValues();
    }
}
