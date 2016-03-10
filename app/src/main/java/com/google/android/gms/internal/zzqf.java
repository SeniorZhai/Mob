package com.google.android.gms.internal;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.gms.internal.zzaf.zzh;
import com.google.android.gms.tagmanager.zzbg;
import com.google.android.gms.tagmanager.zzdf;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class zzqf {

    public static class zza {
        private final com.google.android.gms.internal.zzag.zza zzaNw;
        private final Map<String, com.google.android.gms.internal.zzag.zza> zzaPy;

        private zza(Map<String, com.google.android.gms.internal.zzag.zza> map, com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
            this.zzaPy = map;
            this.zzaNw = com_google_android_gms_internal_zzag_zza;
        }

        public static zzb zzAm() {
            return new zzb();
        }

        public String toString() {
            return "Properties: " + zzAn() + " pushAfterEvaluate: " + this.zzaNw;
        }

        public Map<String, com.google.android.gms.internal.zzag.zza> zzAn() {
            return Collections.unmodifiableMap(this.zzaPy);
        }

        public void zza(String str, com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
            this.zzaPy.put(str, com_google_android_gms_internal_zzag_zza);
        }

        public com.google.android.gms.internal.zzag.zza zzzs() {
            return this.zzaNw;
        }
    }

    public static class zzb {
        private com.google.android.gms.internal.zzag.zza zzaNw;
        private final Map<String, com.google.android.gms.internal.zzag.zza> zzaPy;

        private zzb() {
            this.zzaPy = new HashMap();
        }

        public zza zzAo() {
            return new zza(this.zzaPy, this.zzaNw);
        }

        public zzb zzb(String str, com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
            this.zzaPy.put(str, com_google_android_gms_internal_zzag_zza);
            return this;
        }

        public zzb zzq(com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
            this.zzaNw = com_google_android_gms_internal_zzag_zza;
            return this;
        }
    }

    public static class zzc {
        private final String zzTQ;
        private final Map<String, List<zza>> zzaPA;
        private final int zzaPB;
        private final List<zze> zzaPz;

        private zzc(List<zze> list, Map<String, List<zza>> map, String str, int i) {
            this.zzaPz = Collections.unmodifiableList(list);
            this.zzaPA = Collections.unmodifiableMap(map);
            this.zzTQ = str;
            this.zzaPB = i;
        }

        public static zzd zzAp() {
            return new zzd();
        }

        public String getVersion() {
            return this.zzTQ;
        }

        public String toString() {
            return "Rules: " + zzAq() + "  Macros: " + this.zzaPA;
        }

        public List<zze> zzAq() {
            return this.zzaPz;
        }

        public Map<String, List<zza>> zzAr() {
            return this.zzaPA;
        }
    }

    public static class zzd {
        private String zzTQ;
        private final Map<String, List<zza>> zzaPA;
        private int zzaPB;
        private final List<zze> zzaPz;

        private zzd() {
            this.zzaPz = new ArrayList();
            this.zzaPA = new HashMap();
            this.zzTQ = BuildConfig.FLAVOR;
            this.zzaPB = 0;
        }

        public zzc zzAs() {
            return new zzc(this.zzaPz, this.zzaPA, this.zzTQ, this.zzaPB);
        }

        public zzd zzb(zze com_google_android_gms_internal_zzqf_zze) {
            this.zzaPz.add(com_google_android_gms_internal_zzqf_zze);
            return this;
        }

        public zzd zzc(zza com_google_android_gms_internal_zzqf_zza) {
            String zzg = zzdf.zzg((com.google.android.gms.internal.zzag.zza) com_google_android_gms_internal_zzqf_zza.zzAn().get(zzae.INSTANCE_NAME.toString()));
            List list = (List) this.zzaPA.get(zzg);
            if (list == null) {
                list = new ArrayList();
                this.zzaPA.put(zzg, list);
            }
            list.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzd zzeV(String str) {
            this.zzTQ = str;
            return this;
        }

        public zzd zzjb(int i) {
            this.zzaPB = i;
            return this;
        }
    }

    public static class zze {
        private final List<zza> zzaPC;
        private final List<zza> zzaPD;
        private final List<zza> zzaPE;
        private final List<zza> zzaPF;
        private final List<zza> zzaPG;
        private final List<zza> zzaPH;
        private final List<String> zzaPI;
        private final List<String> zzaPJ;
        private final List<String> zzaPK;
        private final List<String> zzaPL;

        private zze(List<zza> list, List<zza> list2, List<zza> list3, List<zza> list4, List<zza> list5, List<zza> list6, List<String> list7, List<String> list8, List<String> list9, List<String> list10) {
            this.zzaPC = Collections.unmodifiableList(list);
            this.zzaPD = Collections.unmodifiableList(list2);
            this.zzaPE = Collections.unmodifiableList(list3);
            this.zzaPF = Collections.unmodifiableList(list4);
            this.zzaPG = Collections.unmodifiableList(list5);
            this.zzaPH = Collections.unmodifiableList(list6);
            this.zzaPI = Collections.unmodifiableList(list7);
            this.zzaPJ = Collections.unmodifiableList(list8);
            this.zzaPK = Collections.unmodifiableList(list9);
            this.zzaPL = Collections.unmodifiableList(list10);
        }

        public static zzf zzAt() {
            return new zzf();
        }

        public String toString() {
            return "Positive predicates: " + zzAu() + "  Negative predicates: " + zzAv() + "  Add tags: " + zzAw() + "  Remove tags: " + zzAx() + "  Add macros: " + zzAy() + "  Remove macros: " + zzAD();
        }

        public List<String> zzAA() {
            return this.zzaPJ;
        }

        public List<String> zzAB() {
            return this.zzaPK;
        }

        public List<String> zzAC() {
            return this.zzaPL;
        }

        public List<zza> zzAD() {
            return this.zzaPH;
        }

        public List<zza> zzAu() {
            return this.zzaPC;
        }

        public List<zza> zzAv() {
            return this.zzaPD;
        }

        public List<zza> zzAw() {
            return this.zzaPE;
        }

        public List<zza> zzAx() {
            return this.zzaPF;
        }

        public List<zza> zzAy() {
            return this.zzaPG;
        }

        public List<String> zzAz() {
            return this.zzaPI;
        }
    }

    public static class zzf {
        private final List<zza> zzaPC;
        private final List<zza> zzaPD;
        private final List<zza> zzaPE;
        private final List<zza> zzaPF;
        private final List<zza> zzaPG;
        private final List<zza> zzaPH;
        private final List<String> zzaPI;
        private final List<String> zzaPJ;
        private final List<String> zzaPK;
        private final List<String> zzaPL;

        private zzf() {
            this.zzaPC = new ArrayList();
            this.zzaPD = new ArrayList();
            this.zzaPE = new ArrayList();
            this.zzaPF = new ArrayList();
            this.zzaPG = new ArrayList();
            this.zzaPH = new ArrayList();
            this.zzaPI = new ArrayList();
            this.zzaPJ = new ArrayList();
            this.zzaPK = new ArrayList();
            this.zzaPL = new ArrayList();
        }

        public zze zzAE() {
            return new zze(this.zzaPC, this.zzaPD, this.zzaPE, this.zzaPF, this.zzaPG, this.zzaPH, this.zzaPI, this.zzaPJ, this.zzaPK, this.zzaPL);
        }

        public zzf zzd(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPC.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzf zze(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPD.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzf zzeW(String str) {
            this.zzaPK.add(str);
            return this;
        }

        public zzf zzeX(String str) {
            this.zzaPL.add(str);
            return this;
        }

        public zzf zzeY(String str) {
            this.zzaPI.add(str);
            return this;
        }

        public zzf zzeZ(String str) {
            this.zzaPJ.add(str);
            return this;
        }

        public zzf zzf(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPE.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzf zzg(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPF.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzf zzh(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPG.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }

        public zzf zzi(zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaPH.add(com_google_android_gms_internal_zzqf_zza);
            return this;
        }
    }

    public static class zzg extends Exception {
        public zzg(String str) {
            super(str);
        }
    }

    private static com.google.android.gms.internal.zzag.zza zza(int i, com.google.android.gms.internal.zzaf.zzf com_google_android_gms_internal_zzaf_zzf, com.google.android.gms.internal.zzag.zza[] com_google_android_gms_internal_zzag_zzaArr, Set<Integer> set) throws zzg {
        int i2 = 0;
        if (set.contains(Integer.valueOf(i))) {
            zzeT("Value cycle detected.  Current value reference: " + i + "." + "  Previous value references: " + set + ".");
        }
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) zza(com_google_android_gms_internal_zzaf_zzf.zzic, i, "values");
        if (com_google_android_gms_internal_zzag_zzaArr[i] != null) {
            return com_google_android_gms_internal_zzag_zzaArr[i];
        }
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza2 = null;
        set.add(Integer.valueOf(i));
        zzh zzp;
        int[] iArr;
        int length;
        int i3;
        int i4;
        switch (com_google_android_gms_internal_zzag_zza.type) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case Player.STATE_ENDED /*5*/:
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                com_google_android_gms_internal_zzag_zza2 = com_google_android_gms_internal_zzag_zza;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                zzp = zzp(com_google_android_gms_internal_zzag_zza);
                com_google_android_gms_internal_zzag_zza2 = zzo(com_google_android_gms_internal_zzag_zza);
                com_google_android_gms_internal_zzag_zza2.zziS = new com.google.android.gms.internal.zzag.zza[zzp.zziD.length];
                iArr = zzp.zziD;
                length = iArr.length;
                i3 = 0;
                while (i2 < length) {
                    i4 = i3 + 1;
                    com_google_android_gms_internal_zzag_zza2.zziS[i3] = zza(iArr[i2], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i2++;
                    i3 = i4;
                }
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                com_google_android_gms_internal_zzag_zza2 = zzo(com_google_android_gms_internal_zzag_zza);
                zzh zzp2 = zzp(com_google_android_gms_internal_zzag_zza);
                if (zzp2.zziE.length != zzp2.zziF.length) {
                    zzeT("Uneven map keys (" + zzp2.zziE.length + ") and map values (" + zzp2.zziF.length + ")");
                }
                com_google_android_gms_internal_zzag_zza2.zziT = new com.google.android.gms.internal.zzag.zza[zzp2.zziE.length];
                com_google_android_gms_internal_zzag_zza2.zziU = new com.google.android.gms.internal.zzag.zza[zzp2.zziE.length];
                int[] iArr2 = zzp2.zziE;
                int length2 = iArr2.length;
                i3 = 0;
                i4 = 0;
                while (i3 < length2) {
                    int i5 = i4 + 1;
                    com_google_android_gms_internal_zzag_zza2.zziT[i4] = zza(iArr2[i3], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i3++;
                    i4 = i5;
                }
                iArr = zzp2.zziF;
                length = iArr.length;
                i3 = 0;
                while (i2 < length) {
                    i4 = i3 + 1;
                    com_google_android_gms_internal_zzag_zza2.zziU[i3] = zza(iArr[i2], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i2++;
                    i3 = i4;
                }
                break;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                com_google_android_gms_internal_zzag_zza2 = zzo(com_google_android_gms_internal_zzag_zza);
                com_google_android_gms_internal_zzag_zza2.zziV = zzdf.zzg(zza(zzp(com_google_android_gms_internal_zzag_zza).zziI, com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, (Set) set));
                break;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                com_google_android_gms_internal_zzag_zza2 = zzo(com_google_android_gms_internal_zzag_zza);
                zzp = zzp(com_google_android_gms_internal_zzag_zza);
                com_google_android_gms_internal_zzag_zza2.zziZ = new com.google.android.gms.internal.zzag.zza[zzp.zziH.length];
                iArr = zzp.zziH;
                length = iArr.length;
                i3 = 0;
                while (i2 < length) {
                    i4 = i3 + 1;
                    com_google_android_gms_internal_zzag_zza2.zziZ[i3] = zza(iArr[i2], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i2++;
                    i3 = i4;
                }
                break;
        }
        if (com_google_android_gms_internal_zzag_zza2 == null) {
            zzeT("Invalid value: " + com_google_android_gms_internal_zzag_zza);
        }
        com_google_android_gms_internal_zzag_zzaArr[i] = com_google_android_gms_internal_zzag_zza2;
        set.remove(Integer.valueOf(i));
        return com_google_android_gms_internal_zzag_zza2;
    }

    private static zza zza(com.google.android.gms.internal.zzaf.zzb com_google_android_gms_internal_zzaf_zzb, com.google.android.gms.internal.zzaf.zzf com_google_android_gms_internal_zzaf_zzf, com.google.android.gms.internal.zzag.zza[] com_google_android_gms_internal_zzag_zzaArr, int i) throws zzg {
        zzb zzAm = zza.zzAm();
        for (int valueOf : com_google_android_gms_internal_zzaf_zzb.zzhN) {
            com.google.android.gms.internal.zzaf.zze com_google_android_gms_internal_zzaf_zze = (com.google.android.gms.internal.zzaf.zze) zza(com_google_android_gms_internal_zzaf_zzf.zzid, Integer.valueOf(valueOf).intValue(), "properties");
            String str = (String) zza(com_google_android_gms_internal_zzaf_zzf.zzib, com_google_android_gms_internal_zzaf_zze.key, "keys");
            com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) zza(com_google_android_gms_internal_zzag_zzaArr, com_google_android_gms_internal_zzaf_zze.value, "values");
            if (zzae.PUSH_AFTER_EVALUATE.toString().equals(str)) {
                zzAm.zzq(com_google_android_gms_internal_zzag_zza);
            } else {
                zzAm.zzb(str, com_google_android_gms_internal_zzag_zza);
            }
        }
        return zzAm.zzAo();
    }

    private static zze zza(com.google.android.gms.internal.zzaf.zzg com_google_android_gms_internal_zzaf_zzg, List<zza> list, List<zza> list2, List<zza> list3, com.google.android.gms.internal.zzaf.zzf com_google_android_gms_internal_zzaf_zzf) {
        zzf zzAt = zze.zzAt();
        for (int valueOf : com_google_android_gms_internal_zzaf_zzg.zzir) {
            zzAt.zzd((zza) list3.get(Integer.valueOf(valueOf).intValue()));
        }
        for (int valueOf2 : com_google_android_gms_internal_zzaf_zzg.zzis) {
            zzAt.zze((zza) list3.get(Integer.valueOf(valueOf2).intValue()));
        }
        for (int valueOf22 : com_google_android_gms_internal_zzaf_zzg.zzit) {
            zzAt.zzf((zza) list.get(Integer.valueOf(valueOf22).intValue()));
        }
        for (int valueOf3 : com_google_android_gms_internal_zzaf_zzg.zziv) {
            zzAt.zzeW(com_google_android_gms_internal_zzaf_zzf.zzic[Integer.valueOf(valueOf3).intValue()].zziR);
        }
        for (int valueOf222 : com_google_android_gms_internal_zzaf_zzg.zziu) {
            zzAt.zzg((zza) list.get(Integer.valueOf(valueOf222).intValue()));
        }
        for (int valueOf32 : com_google_android_gms_internal_zzaf_zzg.zziw) {
            zzAt.zzeX(com_google_android_gms_internal_zzaf_zzf.zzic[Integer.valueOf(valueOf32).intValue()].zziR);
        }
        for (int valueOf2222 : com_google_android_gms_internal_zzaf_zzg.zzix) {
            zzAt.zzh((zza) list2.get(Integer.valueOf(valueOf2222).intValue()));
        }
        for (int valueOf322 : com_google_android_gms_internal_zzaf_zzg.zziz) {
            zzAt.zzeY(com_google_android_gms_internal_zzaf_zzf.zzic[Integer.valueOf(valueOf322).intValue()].zziR);
        }
        for (int valueOf22222 : com_google_android_gms_internal_zzaf_zzg.zziy) {
            zzAt.zzi((zza) list2.get(Integer.valueOf(valueOf22222).intValue()));
        }
        for (int valueOf4 : com_google_android_gms_internal_zzaf_zzg.zziA) {
            zzAt.zzeZ(com_google_android_gms_internal_zzaf_zzf.zzic[Integer.valueOf(valueOf4).intValue()].zziR);
        }
        return zzAt.zzAE();
    }

    private static <T> T zza(T[] tArr, int i, String str) throws zzg {
        if (i < 0 || i >= tArr.length) {
            zzeT("Index out of bounds detected: " + i + " in " + str);
        }
        return tArr[i];
    }

    public static zzc zzb(com.google.android.gms.internal.zzaf.zzf com_google_android_gms_internal_zzaf_zzf) throws zzg {
        int i;
        int i2 = 0;
        com.google.android.gms.internal.zzag.zza[] com_google_android_gms_internal_zzag_zzaArr = new com.google.android.gms.internal.zzag.zza[com_google_android_gms_internal_zzaf_zzf.zzic.length];
        for (i = 0; i < com_google_android_gms_internal_zzaf_zzf.zzic.length; i++) {
            zza(i, com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, new HashSet(0));
        }
        zzd zzAp = zzc.zzAp();
        List arrayList = new ArrayList();
        for (i = 0; i < com_google_android_gms_internal_zzaf_zzf.zzif.length; i++) {
            arrayList.add(zza(com_google_android_gms_internal_zzaf_zzf.zzif[i], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, i));
        }
        List arrayList2 = new ArrayList();
        for (i = 0; i < com_google_android_gms_internal_zzaf_zzf.zzig.length; i++) {
            arrayList2.add(zza(com_google_android_gms_internal_zzaf_zzf.zzig[i], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, i));
        }
        List arrayList3 = new ArrayList();
        for (i = 0; i < com_google_android_gms_internal_zzaf_zzf.zzie.length; i++) {
            zza zza = zza(com_google_android_gms_internal_zzaf_zzf.zzie[i], com_google_android_gms_internal_zzaf_zzf, com_google_android_gms_internal_zzag_zzaArr, i);
            zzAp.zzc(zza);
            arrayList3.add(zza);
        }
        com.google.android.gms.internal.zzaf.zzg[] com_google_android_gms_internal_zzaf_zzgArr = com_google_android_gms_internal_zzaf_zzf.zzih;
        int length = com_google_android_gms_internal_zzaf_zzgArr.length;
        while (i2 < length) {
            zzAp.zzb(zza(com_google_android_gms_internal_zzaf_zzgArr[i2], arrayList, arrayList3, arrayList2, com_google_android_gms_internal_zzaf_zzf));
            i2++;
        }
        zzAp.zzeV(com_google_android_gms_internal_zzaf_zzf.version);
        zzAp.zzjb(com_google_android_gms_internal_zzaf_zzf.zzip);
        return zzAp.zzAs();
    }

    public static void zzc(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
        while (true) {
            int read = inputStream.read(bArr);
            if (read != -1) {
                outputStream.write(bArr, 0, read);
            } else {
                return;
            }
        }
    }

    private static void zzeT(String str) throws zzg {
        zzbg.zzaz(str);
        throw new zzg(str);
    }

    public static com.google.android.gms.internal.zzag.zza zzo(com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza2 = new com.google.android.gms.internal.zzag.zza();
        com_google_android_gms_internal_zzag_zza2.type = com_google_android_gms_internal_zzag_zza.type;
        com_google_android_gms_internal_zzag_zza2.zzja = (int[]) com_google_android_gms_internal_zzag_zza.zzja.clone();
        if (com_google_android_gms_internal_zzag_zza.zzjb) {
            com_google_android_gms_internal_zzag_zza2.zzjb = com_google_android_gms_internal_zzag_zza.zzjb;
        }
        return com_google_android_gms_internal_zzag_zza2;
    }

    private static zzh zzp(com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) throws zzg {
        if (((zzh) com_google_android_gms_internal_zzag_zza.zza(zzh.zziB)) == null) {
            zzeT("Expected a ServingValue and didn't get one. Value is: " + com_google_android_gms_internal_zzag_zza);
        }
        return (zzh) com_google_android_gms_internal_zzag_zza.zza(zzh.zziB);
    }
}
