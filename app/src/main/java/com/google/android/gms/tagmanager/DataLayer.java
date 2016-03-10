package com.google.android.gms.tagmanager;

import android.support.v7.app.AppCompatDelegate;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;

public class DataLayer {
    public static final String EVENT_KEY = "event";
    public static final Object OBJECT_NOT_PRESENT = new Object();
    static final String[] zzaLf = "gtm.lifetime".toString().split("\\.");
    private static final Pattern zzaLg = Pattern.compile("(\\d+)\\s*([smhd]?)");
    private final ConcurrentHashMap<zzb, Integer> zzaLh;
    private final Map<String, Object> zzaLi;
    private final ReentrantLock zzaLj;
    private final LinkedList<Map<String, Object>> zzaLk;
    private final zzc zzaLl;
    private final CountDownLatch zzaLm;

    interface zzc {

        public interface zza {
            void zzo(List<zza> list);
        }

        void zza(zza com_google_android_gms_tagmanager_DataLayer_zzc_zza);

        void zza(List<zza> list, long j);

        void zzep(String str);
    }

    static final class zza {
        public final Object zzGK;
        public final String zztw;

        zza(String str, Object obj) {
            this.zztw = str;
            this.zzGK = obj;
        }

        public boolean equals(Object o) {
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_tagmanager_DataLayer_zza = (zza) o;
            return this.zztw.equals(com_google_android_gms_tagmanager_DataLayer_zza.zztw) && this.zzGK.equals(com_google_android_gms_tagmanager_DataLayer_zza.zzGK);
        }

        public int hashCode() {
            return Arrays.hashCode(new Integer[]{Integer.valueOf(this.zztw.hashCode()), Integer.valueOf(this.zzGK.hashCode())});
        }

        public String toString() {
            return "Key: " + this.zztw + " value: " + this.zzGK.toString();
        }
    }

    interface zzb {
        void zzF(Map<String, Object> map);
    }

    DataLayer() {
        this(new zzc() {
            public void zza(zza com_google_android_gms_tagmanager_DataLayer_zzc_zza) {
                com_google_android_gms_tagmanager_DataLayer_zzc_zza.zzo(new ArrayList());
            }

            public void zza(List<zza> list, long j) {
            }

            public void zzep(String str) {
            }
        });
    }

    DataLayer(zzc persistentStore) {
        this.zzaLl = persistentStore;
        this.zzaLh = new ConcurrentHashMap();
        this.zzaLi = new HashMap();
        this.zzaLj = new ReentrantLock();
        this.zzaLk = new LinkedList();
        this.zzaLm = new CountDownLatch(1);
        zzyy();
    }

    public static List<Object> listOf(Object... objects) {
        List<Object> arrayList = new ArrayList();
        for (Object add : objects) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public static Map<String, Object> mapOf(Object... objects) {
        if (objects.length % 2 != 0) {
            throw new IllegalArgumentException("expected even number of key-value pairs");
        }
        Map<String, Object> hashMap = new HashMap();
        int i = 0;
        while (i < objects.length) {
            if (objects[i] instanceof String) {
                hashMap.put((String) objects[i], objects[i + 1]);
                i += 2;
            } else {
                throw new IllegalArgumentException("key is not a string: " + objects[i]);
            }
        }
        return hashMap;
    }

    private void zzH(Map<String, Object> map) {
        this.zzaLj.lock();
        try {
            this.zzaLk.offer(map);
            if (this.zzaLj.getHoldCount() == 1) {
                zzyz();
            }
            zzI(map);
        } finally {
            this.zzaLj.unlock();
        }
    }

    private void zzI(Map<String, Object> map) {
        Long zzJ = zzJ(map);
        if (zzJ != null) {
            List zzL = zzL(map);
            zzL.remove("gtm.lifetime");
            this.zzaLl.zza(zzL, zzJ.longValue());
        }
    }

    private Long zzJ(Map<String, Object> map) {
        Object zzK = zzK(map);
        return zzK == null ? null : zzeo(zzK.toString());
    }

    private Object zzK(Map<String, Object> map) {
        String[] strArr = zzaLf;
        int length = strArr.length;
        int i = 0;
        Object obj = map;
        while (i < length) {
            Object obj2 = strArr[i];
            if (!(obj instanceof Map)) {
                return null;
            }
            i++;
            obj = ((Map) obj).get(obj2);
        }
        return obj;
    }

    private List<zza> zzL(Map<String, Object> map) {
        Object arrayList = new ArrayList();
        zza(map, BuildConfig.FLAVOR, arrayList);
        return arrayList;
    }

    private void zzM(Map<String, Object> map) {
        synchronized (this.zzaLi) {
            for (String str : map.keySet()) {
                zzc(zzj(str, map.get(str)), this.zzaLi);
            }
        }
        zzN(map);
    }

    private void zzN(Map<String, Object> map) {
        for (zzb zzF : this.zzaLh.keySet()) {
            zzF.zzF(map);
        }
    }

    private void zza(Map<String, Object> map, String str, Collection<zza> collection) {
        for (Entry entry : map.entrySet()) {
            String str2 = str + (str.length() == 0 ? BuildConfig.FLAVOR : ".") + ((String) entry.getKey());
            if (entry.getValue() instanceof Map) {
                zza((Map) entry.getValue(), str2, collection);
            } else if (!str2.equals("gtm.lifetime")) {
                collection.add(new zza(str2, entry.getValue()));
            }
        }
    }

    static Long zzeo(String str) {
        Matcher matcher = zzaLg.matcher(str);
        if (matcher.matches()) {
            long parseLong;
            try {
                parseLong = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                zzbg.zzaC("illegal number in _lifetime value: " + str);
                parseLong = 0;
            }
            if (parseLong <= 0) {
                zzbg.zzaA("non-positive _lifetime: " + str);
                return null;
            }
            String group = matcher.group(2);
            if (group.length() == 0) {
                return Long.valueOf(parseLong);
            }
            switch (group.charAt(0)) {
                case HttpStatus.SC_CONTINUE /*100*/:
                    return Long.valueOf((((parseLong * 1000) * 60) * 60) * 24);
                case R.styleable.Theme_radioButtonStyle /*104*/:
                    return Long.valueOf(((parseLong * 1000) * 60) * 60);
                case AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY /*109*/:
                    return Long.valueOf((parseLong * 1000) * 60);
                case 's':
                    return Long.valueOf(parseLong * 1000);
                default:
                    zzbg.zzaC("unknown units in _lifetime: " + str);
                    return null;
            }
        }
        zzbg.zzaA("unknown _lifetime: " + str);
        return null;
    }

    private void zzyy() {
        this.zzaLl.zza(new zza(this) {
            final /* synthetic */ DataLayer zzaLn;

            {
                this.zzaLn = r1;
            }

            public void zzo(List<zza> list) {
                for (zza com_google_android_gms_tagmanager_DataLayer_zza : list) {
                    this.zzaLn.zzH(this.zzaLn.zzj(com_google_android_gms_tagmanager_DataLayer_zza.zztw, com_google_android_gms_tagmanager_DataLayer_zza.zzGK));
                }
                this.zzaLn.zzaLm.countDown();
            }
        });
    }

    private void zzyz() {
        int i = 0;
        while (true) {
            Map map = (Map) this.zzaLk.poll();
            if (map != null) {
                zzM(map);
                int i2 = i + 1;
                if (i2 > HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    this.zzaLk.clear();
                    throw new RuntimeException("Seems like an infinite loop of pushing to the data layer");
                }
                i = i2;
            } else {
                return;
            }
        }
    }

    public Object get(String key) {
        synchronized (this.zzaLi) {
            Map map = this.zzaLi;
            String[] split = key.split("\\.");
            int length = split.length;
            Object obj = map;
            int i = 0;
            while (i < length) {
                Object obj2 = split[i];
                if (obj instanceof Map) {
                    obj2 = ((Map) obj).get(obj2);
                    if (obj2 == null) {
                        return null;
                    }
                    i++;
                    obj = obj2;
                } else {
                    return null;
                }
            }
            return obj;
        }
    }

    public void push(String key, Object value) {
        push(zzj(key, value));
    }

    public void push(Map<String, Object> update) {
        try {
            this.zzaLm.await();
        } catch (InterruptedException e) {
            zzbg.zzaC("DataLayer.push: unexpected InterruptedException");
        }
        zzH(update);
    }

    public void pushEvent(String eventName, Map<String, Object> update) {
        Map hashMap = new HashMap(update);
        hashMap.put(EVENT_KEY, eventName);
        push(hashMap);
    }

    public String toString() {
        String stringBuilder;
        synchronized (this.zzaLi) {
            StringBuilder stringBuilder2 = new StringBuilder();
            for (Entry entry : this.zzaLi.entrySet()) {
                stringBuilder2.append(String.format("{\n\tKey: %s\n\tValue: %s\n}\n", new Object[]{entry.getKey(), entry.getValue()}));
            }
            stringBuilder = stringBuilder2.toString();
        }
        return stringBuilder;
    }

    void zza(zzb com_google_android_gms_tagmanager_DataLayer_zzb) {
        this.zzaLh.put(com_google_android_gms_tagmanager_DataLayer_zzb, Integer.valueOf(0));
    }

    void zzb(List<Object> list, List<Object> list2) {
        while (list2.size() < list.size()) {
            list2.add(null);
        }
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof List) {
                if (!(list2.get(i) instanceof List)) {
                    list2.set(i, new ArrayList());
                }
                zzb((List) obj, (List) list2.get(i));
            } else if (obj instanceof Map) {
                if (!(list2.get(i) instanceof Map)) {
                    list2.set(i, new HashMap());
                }
                zzc((Map) obj, (Map) list2.get(i));
            } else if (obj != OBJECT_NOT_PRESENT) {
                list2.set(i, obj);
            }
        }
    }

    void zzc(Map<String, Object> map, Map<String, Object> map2) {
        for (String str : map.keySet()) {
            Object obj = map.get(str);
            if (obj instanceof List) {
                if (!(map2.get(str) instanceof List)) {
                    map2.put(str, new ArrayList());
                }
                zzb((List) obj, (List) map2.get(str));
            } else if (obj instanceof Map) {
                if (!(map2.get(str) instanceof Map)) {
                    map2.put(str, new HashMap());
                }
                zzc((Map) obj, (Map) map2.get(str));
            } else {
                map2.put(str, obj);
            }
        }
    }

    void zzen(String str) {
        push(str, null);
        this.zzaLl.zzep(str);
    }

    Map<String, Object> zzj(String str, Object obj) {
        Map hashMap = new HashMap();
        String[] split = str.toString().split("\\.");
        int i = 0;
        Map map = hashMap;
        while (i < split.length - 1) {
            HashMap hashMap2 = new HashMap();
            map.put(split[i], hashMap2);
            i++;
            Object obj2 = hashMap2;
        }
        map.put(split[split.length - 1], obj);
        return hashMap;
    }
}
