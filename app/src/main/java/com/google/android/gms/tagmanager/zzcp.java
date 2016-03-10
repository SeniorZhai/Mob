package com.google.android.gms.tagmanager;

import android.content.Context;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.facebook.internal.AnalyticsEvents;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzaf.zzi;
import com.google.android.gms.internal.zzqf;
import com.google.android.gms.internal.zzqf.zze;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.message.TokenParser;

class zzcp {
    private static final zzbw<com.google.android.gms.internal.zzag.zza> zzaNe = new zzbw(zzdf.zzzQ(), true);
    private final DataLayer zzaKz;
    private final com.google.android.gms.internal.zzqf.zzc zzaNf;
    private final zzah zzaNg;
    private final Map<String, zzak> zzaNh;
    private final Map<String, zzak> zzaNi;
    private final Map<String, zzak> zzaNj;
    private final zzl<com.google.android.gms.internal.zzqf.zza, zzbw<com.google.android.gms.internal.zzag.zza>> zzaNk;
    private final zzl<String, zzb> zzaNl;
    private final Set<zze> zzaNm;
    private final Map<String, zzc> zzaNn;
    private volatile String zzaNo;
    private int zzaNp;

    interface zza {
        void zza(zze com_google_android_gms_internal_zzqf_zze, Set<com.google.android.gms.internal.zzqf.zza> set, Set<com.google.android.gms.internal.zzqf.zza> set2, zzck com_google_android_gms_tagmanager_zzck);
    }

    private static class zzb {
        private zzbw<com.google.android.gms.internal.zzag.zza> zzaNv;
        private com.google.android.gms.internal.zzag.zza zzaNw;

        public zzb(zzbw<com.google.android.gms.internal.zzag.zza> com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza, com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza) {
            this.zzaNv = com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza;
            this.zzaNw = com_google_android_gms_internal_zzag_zza;
        }

        public int getSize() {
            return (this.zzaNw == null ? 0 : this.zzaNw.zzBU()) + ((com.google.android.gms.internal.zzag.zza) this.zzaNv.getObject()).zzBU();
        }

        public zzbw<com.google.android.gms.internal.zzag.zza> zzzr() {
            return this.zzaNv;
        }

        public com.google.android.gms.internal.zzag.zza zzzs() {
            return this.zzaNw;
        }
    }

    private static class zzc {
        private final Map<zze, List<String>> zzaNA = new HashMap();
        private com.google.android.gms.internal.zzqf.zza zzaNB;
        private final Set<zze> zzaNm = new HashSet();
        private final Map<zze, List<com.google.android.gms.internal.zzqf.zza>> zzaNx = new HashMap();
        private final Map<zze, List<com.google.android.gms.internal.zzqf.zza>> zzaNy = new HashMap();
        private final Map<zze, List<String>> zzaNz = new HashMap();

        public void zza(zze com_google_android_gms_internal_zzqf_zze) {
            this.zzaNm.add(com_google_android_gms_internal_zzqf_zze);
        }

        public void zza(zze com_google_android_gms_internal_zzqf_zze, com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza) {
            List list = (List) this.zzaNx.get(com_google_android_gms_internal_zzqf_zze);
            if (list == null) {
                list = new ArrayList();
                this.zzaNx.put(com_google_android_gms_internal_zzqf_zze, list);
            }
            list.add(com_google_android_gms_internal_zzqf_zza);
        }

        public void zza(zze com_google_android_gms_internal_zzqf_zze, String str) {
            List list = (List) this.zzaNz.get(com_google_android_gms_internal_zzqf_zze);
            if (list == null) {
                list = new ArrayList();
                this.zzaNz.put(com_google_android_gms_internal_zzqf_zze, list);
            }
            list.add(str);
        }

        public void zzb(com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza) {
            this.zzaNB = com_google_android_gms_internal_zzqf_zza;
        }

        public void zzb(zze com_google_android_gms_internal_zzqf_zze, com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza) {
            List list = (List) this.zzaNy.get(com_google_android_gms_internal_zzqf_zze);
            if (list == null) {
                list = new ArrayList();
                this.zzaNy.put(com_google_android_gms_internal_zzqf_zze, list);
            }
            list.add(com_google_android_gms_internal_zzqf_zza);
        }

        public void zzb(zze com_google_android_gms_internal_zzqf_zze, String str) {
            List list = (List) this.zzaNA.get(com_google_android_gms_internal_zzqf_zze);
            if (list == null) {
                list = new ArrayList();
                this.zzaNA.put(com_google_android_gms_internal_zzqf_zze, list);
            }
            list.add(str);
        }

        public Set<zze> zzzt() {
            return this.zzaNm;
        }

        public Map<zze, List<com.google.android.gms.internal.zzqf.zza>> zzzu() {
            return this.zzaNx;
        }

        public Map<zze, List<String>> zzzv() {
            return this.zzaNz;
        }

        public Map<zze, List<String>> zzzw() {
            return this.zzaNA;
        }

        public Map<zze, List<com.google.android.gms.internal.zzqf.zza>> zzzx() {
            return this.zzaNy;
        }

        public com.google.android.gms.internal.zzqf.zza zzzy() {
            return this.zzaNB;
        }
    }

    public zzcp(Context context, com.google.android.gms.internal.zzqf.zzc com_google_android_gms_internal_zzqf_zzc, DataLayer dataLayer, com.google.android.gms.tagmanager.zzt.zza com_google_android_gms_tagmanager_zzt_zza, com.google.android.gms.tagmanager.zzt.zza com_google_android_gms_tagmanager_zzt_zza2, zzah com_google_android_gms_tagmanager_zzah) {
        if (com_google_android_gms_internal_zzqf_zzc == null) {
            throw new NullPointerException("resource cannot be null");
        }
        this.zzaNf = com_google_android_gms_internal_zzqf_zzc;
        this.zzaNm = new HashSet(com_google_android_gms_internal_zzqf_zzc.zzAq());
        this.zzaKz = dataLayer;
        this.zzaNg = com_google_android_gms_tagmanager_zzah;
        this.zzaNk = new zzm().zza(AccessibilityNodeInfoCompat.ACTION_DISMISS, new com.google.android.gms.tagmanager.zzm.zza<com.google.android.gms.internal.zzqf.zza, zzbw<com.google.android.gms.internal.zzag.zza>>(this) {
            final /* synthetic */ zzcp zzaNq;

            {
                this.zzaNq = r1;
            }

            public /* synthetic */ int sizeOf(Object x0, Object x1) {
                return zza((com.google.android.gms.internal.zzqf.zza) x0, (zzbw) x1);
            }

            public int zza(com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza, zzbw<com.google.android.gms.internal.zzag.zza> com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza) {
                return ((com.google.android.gms.internal.zzag.zza) com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza.getObject()).zzBU();
            }
        });
        this.zzaNl = new zzm().zza(AccessibilityNodeInfoCompat.ACTION_DISMISS, new com.google.android.gms.tagmanager.zzm.zza<String, zzb>(this) {
            final /* synthetic */ zzcp zzaNq;

            {
                this.zzaNq = r1;
            }

            public /* synthetic */ int sizeOf(Object x0, Object x1) {
                return zza((String) x0, (zzb) x1);
            }

            public int zza(String str, zzb com_google_android_gms_tagmanager_zzcp_zzb) {
                return str.length() + com_google_android_gms_tagmanager_zzcp_zzb.getSize();
            }
        });
        this.zzaNh = new HashMap();
        zzb(new zzj(context));
        zzb(new zzt(com_google_android_gms_tagmanager_zzt_zza2));
        zzb(new zzx(dataLayer));
        zzb(new zzdg(context, dataLayer));
        zzb(new zzdb(context, dataLayer));
        this.zzaNi = new HashMap();
        zzc(new zzr());
        zzc(new zzae());
        zzc(new zzaf());
        zzc(new zzam());
        zzc(new zzan());
        zzc(new zzbc());
        zzc(new zzbd());
        zzc(new zzcf());
        zzc(new zzcy());
        this.zzaNj = new HashMap();
        zza(new zzb(context));
        zza(new zzc(context));
        zza(new zze(context));
        zza(new zzf(context));
        zza(new zzg(context));
        zza(new zzh(context));
        zza(new zzi(context));
        zza(new zzn());
        zza(new zzq(this.zzaNf.getVersion()));
        zza(new zzt(com_google_android_gms_tagmanager_zzt_zza));
        zza(new zzv(dataLayer));
        zza(new zzaa(context));
        zza(new zzab());
        zza(new zzad());
        zza(new zzai(this));
        zza(new zzao());
        zza(new zzap());
        zza(new zzaw(context));
        zza(new zzay());
        zza(new zzbb());
        zza(new zzbi());
        zza(new zzbk(context));
        zza(new zzbx());
        zza(new zzbz());
        zza(new zzcc());
        zza(new zzce());
        zza(new zzcg(context));
        zza(new zzcq());
        zza(new zzcr());
        zza(new zzda());
        zza(new zzdh());
        this.zzaNn = new HashMap();
        for (zze com_google_android_gms_internal_zzqf_zze : this.zzaNm) {
            if (com_google_android_gms_tagmanager_zzah.zzyL()) {
                zza(com_google_android_gms_internal_zzqf_zze.zzAy(), com_google_android_gms_internal_zzqf_zze.zzAz(), "add macro");
                zza(com_google_android_gms_internal_zzqf_zze.zzAD(), com_google_android_gms_internal_zzqf_zze.zzAA(), "remove macro");
                zza(com_google_android_gms_internal_zzqf_zze.zzAw(), com_google_android_gms_internal_zzqf_zze.zzAB(), "add tag");
                zza(com_google_android_gms_internal_zzqf_zze.zzAx(), com_google_android_gms_internal_zzqf_zze.zzAC(), "remove tag");
            }
            int i = 0;
            while (i < com_google_android_gms_internal_zzqf_zze.zzAy().size()) {
                com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza = (com.google.android.gms.internal.zzqf.zza) com_google_android_gms_internal_zzqf_zze.zzAy().get(i);
                String str = AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
                if (com_google_android_gms_tagmanager_zzah.zzyL() && i < com_google_android_gms_internal_zzqf_zze.zzAz().size()) {
                    str = (String) com_google_android_gms_internal_zzqf_zze.zzAz().get(i);
                }
                zzc zzh = zzh(this.zzaNn, zza(com_google_android_gms_internal_zzqf_zza));
                zzh.zza(com_google_android_gms_internal_zzqf_zze);
                zzh.zza(com_google_android_gms_internal_zzqf_zze, com_google_android_gms_internal_zzqf_zza);
                zzh.zza(com_google_android_gms_internal_zzqf_zze, str);
                i++;
            }
            i = 0;
            while (i < com_google_android_gms_internal_zzqf_zze.zzAD().size()) {
                com_google_android_gms_internal_zzqf_zza = (com.google.android.gms.internal.zzqf.zza) com_google_android_gms_internal_zzqf_zze.zzAD().get(i);
                str = AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
                if (com_google_android_gms_tagmanager_zzah.zzyL() && i < com_google_android_gms_internal_zzqf_zze.zzAA().size()) {
                    str = (String) com_google_android_gms_internal_zzqf_zze.zzAA().get(i);
                }
                zzh = zzh(this.zzaNn, zza(com_google_android_gms_internal_zzqf_zza));
                zzh.zza(com_google_android_gms_internal_zzqf_zze);
                zzh.zzb(com_google_android_gms_internal_zzqf_zze, com_google_android_gms_internal_zzqf_zza);
                zzh.zzb(com_google_android_gms_internal_zzqf_zze, str);
                i++;
            }
        }
        for (Entry entry : this.zzaNf.zzAr().entrySet()) {
            for (com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza2 : (List) entry.getValue()) {
                if (!zzdf.zzk((com.google.android.gms.internal.zzag.zza) com_google_android_gms_internal_zzqf_zza2.zzAn().get(zzae.NOT_DEFAULT_MACRO.toString())).booleanValue()) {
                    zzh(this.zzaNn, (String) entry.getKey()).zzb(com_google_android_gms_internal_zzqf_zza2);
                }
            }
        }
    }

    private zzbw<com.google.android.gms.internal.zzag.zza> zza(com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza, Set<String> set, zzdi com_google_android_gms_tagmanager_zzdi) {
        if (!com_google_android_gms_internal_zzag_zza.zzjb) {
            return new zzbw(com_google_android_gms_internal_zzag_zza, true);
        }
        com.google.android.gms.internal.zzag.zza zzo;
        int i;
        zzbw zza;
        switch (com_google_android_gms_internal_zzag_zza.type) {
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                zzo = zzqf.zzo(com_google_android_gms_internal_zzag_zza);
                zzo.zziS = new com.google.android.gms.internal.zzag.zza[com_google_android_gms_internal_zzag_zza.zziS.length];
                for (i = 0; i < com_google_android_gms_internal_zzag_zza.zziS.length; i++) {
                    zza = zza(com_google_android_gms_internal_zzag_zza.zziS[i], (Set) set, com_google_android_gms_tagmanager_zzdi.zziU(i));
                    if (zza == zzaNe) {
                        return zzaNe;
                    }
                    zzo.zziS[i] = (com.google.android.gms.internal.zzag.zza) zza.getObject();
                }
                return new zzbw(zzo, false);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                zzo = zzqf.zzo(com_google_android_gms_internal_zzag_zza);
                if (com_google_android_gms_internal_zzag_zza.zziT.length != com_google_android_gms_internal_zzag_zza.zziU.length) {
                    zzbg.zzaz("Invalid serving value: " + com_google_android_gms_internal_zzag_zza.toString());
                    return zzaNe;
                }
                zzo.zziT = new com.google.android.gms.internal.zzag.zza[com_google_android_gms_internal_zzag_zza.zziT.length];
                zzo.zziU = new com.google.android.gms.internal.zzag.zza[com_google_android_gms_internal_zzag_zza.zziT.length];
                for (i = 0; i < com_google_android_gms_internal_zzag_zza.zziT.length; i++) {
                    zza = zza(com_google_android_gms_internal_zzag_zza.zziT[i], (Set) set, com_google_android_gms_tagmanager_zzdi.zziV(i));
                    zzbw zza2 = zza(com_google_android_gms_internal_zzag_zza.zziU[i], (Set) set, com_google_android_gms_tagmanager_zzdi.zziW(i));
                    if (zza == zzaNe || zza2 == zzaNe) {
                        return zzaNe;
                    }
                    zzo.zziT[i] = (com.google.android.gms.internal.zzag.zza) zza.getObject();
                    zzo.zziU[i] = (com.google.android.gms.internal.zzag.zza) zza2.getObject();
                }
                return new zzbw(zzo, false);
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                if (set.contains(com_google_android_gms_internal_zzag_zza.zziV)) {
                    zzbg.zzaz("Macro cycle detected.  Current macro reference: " + com_google_android_gms_internal_zzag_zza.zziV + "." + "  Previous macro references: " + set.toString() + ".");
                    return zzaNe;
                }
                set.add(com_google_android_gms_internal_zzag_zza.zziV);
                zzbw<com.google.android.gms.internal.zzag.zza> zza3 = zzdj.zza(zza(com_google_android_gms_internal_zzag_zza.zziV, (Set) set, com_google_android_gms_tagmanager_zzdi.zzyZ()), com_google_android_gms_internal_zzag_zza.zzja);
                set.remove(com_google_android_gms_internal_zzag_zza.zziV);
                return zza3;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                zzo = zzqf.zzo(com_google_android_gms_internal_zzag_zza);
                zzo.zziZ = new com.google.android.gms.internal.zzag.zza[com_google_android_gms_internal_zzag_zza.zziZ.length];
                for (i = 0; i < com_google_android_gms_internal_zzag_zza.zziZ.length; i++) {
                    zza = zza(com_google_android_gms_internal_zzag_zza.zziZ[i], (Set) set, com_google_android_gms_tagmanager_zzdi.zziX(i));
                    if (zza == zzaNe) {
                        return zzaNe;
                    }
                    zzo.zziZ[i] = (com.google.android.gms.internal.zzag.zza) zza.getObject();
                }
                return new zzbw(zzo, false);
            default:
                zzbg.zzaz("Unknown type: " + com_google_android_gms_internal_zzag_zza.type);
                return zzaNe;
        }
    }

    private zzbw<com.google.android.gms.internal.zzag.zza> zza(String str, Set<String> set, zzbj com_google_android_gms_tagmanager_zzbj) {
        this.zzaNp++;
        zzb com_google_android_gms_tagmanager_zzcp_zzb = (zzb) this.zzaNl.get(str);
        if (com_google_android_gms_tagmanager_zzcp_zzb == null || this.zzaNg.zzyL()) {
            zzc com_google_android_gms_tagmanager_zzcp_zzc = (zzc) this.zzaNn.get(str);
            if (com_google_android_gms_tagmanager_zzcp_zzc == null) {
                zzbg.zzaz(zzzq() + "Invalid macro: " + str);
                this.zzaNp--;
                return zzaNe;
            }
            com.google.android.gms.internal.zzqf.zza zzzy;
            zzbw zza = zza(str, com_google_android_gms_tagmanager_zzcp_zzc.zzzt(), com_google_android_gms_tagmanager_zzcp_zzc.zzzu(), com_google_android_gms_tagmanager_zzcp_zzc.zzzv(), com_google_android_gms_tagmanager_zzcp_zzc.zzzx(), com_google_android_gms_tagmanager_zzcp_zzc.zzzw(), set, com_google_android_gms_tagmanager_zzbj.zzyB());
            if (((Set) zza.getObject()).isEmpty()) {
                zzzy = com_google_android_gms_tagmanager_zzcp_zzc.zzzy();
            } else {
                if (((Set) zza.getObject()).size() > 1) {
                    zzbg.zzaC(zzzq() + "Multiple macros active for macroName " + str);
                }
                zzzy = (com.google.android.gms.internal.zzqf.zza) ((Set) zza.getObject()).iterator().next();
            }
            if (zzzy == null) {
                this.zzaNp--;
                return zzaNe;
            }
            zzbw zza2 = zza(this.zzaNj, zzzy, (Set) set, com_google_android_gms_tagmanager_zzbj.zzyR());
            boolean z = zza.zzza() && zza2.zzza();
            zzbw<com.google.android.gms.internal.zzag.zza> com_google_android_gms_tagmanager_zzbw = zza2 == zzaNe ? zzaNe : new zzbw(zza2.getObject(), z);
            com.google.android.gms.internal.zzag.zza zzzs = zzzy.zzzs();
            if (com_google_android_gms_tagmanager_zzbw.zzza()) {
                this.zzaNl.zzf(str, new zzb(com_google_android_gms_tagmanager_zzbw, zzzs));
            }
            zza(zzzs, (Set) set);
            this.zzaNp--;
            return com_google_android_gms_tagmanager_zzbw;
        }
        zza(com_google_android_gms_tagmanager_zzcp_zzb.zzzs(), (Set) set);
        this.zzaNp--;
        return com_google_android_gms_tagmanager_zzcp_zzb.zzzr();
    }

    private zzbw<com.google.android.gms.internal.zzag.zza> zza(Map<String, zzak> map, com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza, Set<String> set, zzch com_google_android_gms_tagmanager_zzch) {
        boolean z = true;
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) com_google_android_gms_internal_zzqf_zza.zzAn().get(zzae.FUNCTION.toString());
        if (com_google_android_gms_internal_zzag_zza == null) {
            zzbg.zzaz("No function id in properties");
            return zzaNe;
        }
        String str = com_google_android_gms_internal_zzag_zza.zziW;
        zzak com_google_android_gms_tagmanager_zzak = (zzak) map.get(str);
        if (com_google_android_gms_tagmanager_zzak == null) {
            zzbg.zzaz(str + " has no backing implementation.");
            return zzaNe;
        }
        zzbw<com.google.android.gms.internal.zzag.zza> com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza = (zzbw) this.zzaNk.get(com_google_android_gms_internal_zzqf_zza);
        if (com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza != null && !this.zzaNg.zzyL()) {
            return com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza;
        }
        Map hashMap = new HashMap();
        boolean z2 = true;
        for (Entry entry : com_google_android_gms_internal_zzqf_zza.zzAn().entrySet()) {
            zzbw zza = zza((com.google.android.gms.internal.zzag.zza) entry.getValue(), (Set) set, com_google_android_gms_tagmanager_zzch.zzez((String) entry.getKey()).zze((com.google.android.gms.internal.zzag.zza) entry.getValue()));
            if (zza == zzaNe) {
                return zzaNe;
            }
            boolean z3;
            if (zza.zzza()) {
                com_google_android_gms_internal_zzqf_zza.zza((String) entry.getKey(), (com.google.android.gms.internal.zzag.zza) zza.getObject());
                z3 = z2;
            } else {
                z3 = false;
            }
            hashMap.put(entry.getKey(), zza.getObject());
            z2 = z3;
        }
        if (com_google_android_gms_tagmanager_zzak.zzg(hashMap.keySet())) {
            if (!(z2 && com_google_android_gms_tagmanager_zzak.zzyh())) {
                z = false;
            }
            com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza = new zzbw(com_google_android_gms_tagmanager_zzak.zzE(hashMap), z);
            if (z) {
                this.zzaNk.zzf(com_google_android_gms_internal_zzqf_zza, com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza);
            }
            com_google_android_gms_tagmanager_zzch.zzd((com.google.android.gms.internal.zzag.zza) com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza.getObject());
            return com_google_android_gms_tagmanager_zzbw_com_google_android_gms_internal_zzag_zza;
        }
        zzbg.zzaz("Incorrect keys for function " + str + " required " + com_google_android_gms_tagmanager_zzak.zzyN() + " had " + hashMap.keySet());
        return zzaNe;
    }

    private zzbw<Set<com.google.android.gms.internal.zzqf.zza>> zza(Set<zze> set, Set<String> set2, zza com_google_android_gms_tagmanager_zzcp_zza, zzco com_google_android_gms_tagmanager_zzco) {
        Set hashSet = new HashSet();
        Collection hashSet2 = new HashSet();
        boolean z = true;
        for (zze com_google_android_gms_internal_zzqf_zze : set) {
            zzck zzyY = com_google_android_gms_tagmanager_zzco.zzyY();
            zzbw zza = zza(com_google_android_gms_internal_zzqf_zze, (Set) set2, zzyY);
            if (((Boolean) zza.getObject()).booleanValue()) {
                com_google_android_gms_tagmanager_zzcp_zza.zza(com_google_android_gms_internal_zzqf_zze, hashSet, hashSet2, zzyY);
            }
            boolean z2 = z && zza.zzza();
            z = z2;
        }
        hashSet.removeAll(hashSet2);
        com_google_android_gms_tagmanager_zzco.zzh(hashSet);
        return new zzbw(hashSet, z);
    }

    private static String zza(com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza) {
        return zzdf.zzg((com.google.android.gms.internal.zzag.zza) com_google_android_gms_internal_zzqf_zza.zzAn().get(zzae.INSTANCE_NAME.toString()));
    }

    private void zza(com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza, Set<String> set) {
        if (com_google_android_gms_internal_zzag_zza != null) {
            zzbw zza = zza(com_google_android_gms_internal_zzag_zza, (Set) set, new zzbu());
            if (zza != zzaNe) {
                Object zzl = zzdf.zzl((com.google.android.gms.internal.zzag.zza) zza.getObject());
                if (zzl instanceof Map) {
                    this.zzaKz.push((Map) zzl);
                } else if (zzl instanceof List) {
                    for (Object zzl2 : (List) zzl2) {
                        if (zzl2 instanceof Map) {
                            this.zzaKz.push((Map) zzl2);
                        } else {
                            zzbg.zzaC("pushAfterEvaluate: value not a Map");
                        }
                    }
                } else {
                    zzbg.zzaC("pushAfterEvaluate: value not a Map or List");
                }
            }
        }
    }

    private static void zza(List<com.google.android.gms.internal.zzqf.zza> list, List<String> list2, String str) {
        if (list.size() != list2.size()) {
            zzbg.zzaA("Invalid resource: imbalance of rule names of functions for " + str + " operation. Using default rule name instead");
        }
    }

    private static void zza(Map<String, zzak> map, zzak com_google_android_gms_tagmanager_zzak) {
        if (map.containsKey(com_google_android_gms_tagmanager_zzak.zzyM())) {
            throw new IllegalArgumentException("Duplicate function type name: " + com_google_android_gms_tagmanager_zzak.zzyM());
        }
        map.put(com_google_android_gms_tagmanager_zzak.zzyM(), com_google_android_gms_tagmanager_zzak);
    }

    private static zzc zzh(Map<String, zzc> map, String str) {
        zzc com_google_android_gms_tagmanager_zzcp_zzc = (zzc) map.get(str);
        if (com_google_android_gms_tagmanager_zzcp_zzc != null) {
            return com_google_android_gms_tagmanager_zzcp_zzc;
        }
        com_google_android_gms_tagmanager_zzcp_zzc = new zzc();
        map.put(str, com_google_android_gms_tagmanager_zzcp_zzc);
        return com_google_android_gms_tagmanager_zzcp_zzc;
    }

    private String zzzq() {
        if (this.zzaNp <= 1) {
            return BuildConfig.FLAVOR;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Integer.toString(this.zzaNp));
        for (int i = 2; i < this.zzaNp; i++) {
            stringBuilder.append(TokenParser.SP);
        }
        stringBuilder.append(": ");
        return stringBuilder.toString();
    }

    zzbw<Boolean> zza(com.google.android.gms.internal.zzqf.zza com_google_android_gms_internal_zzqf_zza, Set<String> set, zzch com_google_android_gms_tagmanager_zzch) {
        zzbw zza = zza(this.zzaNi, com_google_android_gms_internal_zzqf_zza, (Set) set, com_google_android_gms_tagmanager_zzch);
        Boolean zzk = zzdf.zzk((com.google.android.gms.internal.zzag.zza) zza.getObject());
        com_google_android_gms_tagmanager_zzch.zzd(zzdf.zzI(zzk));
        return new zzbw(zzk, zza.zzza());
    }

    zzbw<Boolean> zza(zze com_google_android_gms_internal_zzqf_zze, Set<String> set, zzck com_google_android_gms_tagmanager_zzck) {
        boolean z = true;
        for (com.google.android.gms.internal.zzqf.zza zza : com_google_android_gms_internal_zzqf_zze.zzAv()) {
            zzbw zza2 = zza(zza, (Set) set, com_google_android_gms_tagmanager_zzck.zzyS());
            if (((Boolean) zza2.getObject()).booleanValue()) {
                com_google_android_gms_tagmanager_zzck.zzf(zzdf.zzI(Boolean.valueOf(false)));
                return new zzbw(Boolean.valueOf(false), zza2.zzza());
            }
            boolean z2 = z && zza2.zzza();
            z = z2;
        }
        for (com.google.android.gms.internal.zzqf.zza zza3 : com_google_android_gms_internal_zzqf_zze.zzAu()) {
            zza2 = zza(zza3, (Set) set, com_google_android_gms_tagmanager_zzck.zzyT());
            if (((Boolean) zza2.getObject()).booleanValue()) {
                z = z && zza2.zzza();
            } else {
                com_google_android_gms_tagmanager_zzck.zzf(zzdf.zzI(Boolean.valueOf(false)));
                return new zzbw(Boolean.valueOf(false), zza2.zzza());
            }
        }
        com_google_android_gms_tagmanager_zzck.zzf(zzdf.zzI(Boolean.valueOf(true)));
        return new zzbw(Boolean.valueOf(true), z);
    }

    zzbw<Set<com.google.android.gms.internal.zzqf.zza>> zza(String str, Set<zze> set, Map<zze, List<com.google.android.gms.internal.zzqf.zza>> map, Map<zze, List<String>> map2, Map<zze, List<com.google.android.gms.internal.zzqf.zza>> map3, Map<zze, List<String>> map4, Set<String> set2, zzco com_google_android_gms_tagmanager_zzco) {
        final Map<zze, List<com.google.android.gms.internal.zzqf.zza>> map5 = map;
        final Map<zze, List<String>> map6 = map2;
        final Map<zze, List<com.google.android.gms.internal.zzqf.zza>> map7 = map3;
        final Map<zze, List<String>> map8 = map4;
        return zza((Set) set, (Set) set2, new zza(this) {
            final /* synthetic */ zzcp zzaNq;

            public void zza(zze com_google_android_gms_internal_zzqf_zze, Set<com.google.android.gms.internal.zzqf.zza> set, Set<com.google.android.gms.internal.zzqf.zza> set2, zzck com_google_android_gms_tagmanager_zzck) {
                List list = (List) map5.get(com_google_android_gms_internal_zzqf_zze);
                List list2 = (List) map6.get(com_google_android_gms_internal_zzqf_zze);
                if (list != null) {
                    set.addAll(list);
                    com_google_android_gms_tagmanager_zzck.zzyU().zzc(list, list2);
                }
                list = (List) map7.get(com_google_android_gms_internal_zzqf_zze);
                list2 = (List) map8.get(com_google_android_gms_internal_zzqf_zze);
                if (list != null) {
                    set2.addAll(list);
                    com_google_android_gms_tagmanager_zzck.zzyV().zzc(list, list2);
                }
            }
        }, com_google_android_gms_tagmanager_zzco);
    }

    zzbw<Set<com.google.android.gms.internal.zzqf.zza>> zza(Set<zze> set, zzco com_google_android_gms_tagmanager_zzco) {
        return zza((Set) set, new HashSet(), new zza(this) {
            final /* synthetic */ zzcp zzaNq;

            {
                this.zzaNq = r1;
            }

            public void zza(zze com_google_android_gms_internal_zzqf_zze, Set<com.google.android.gms.internal.zzqf.zza> set, Set<com.google.android.gms.internal.zzqf.zza> set2, zzck com_google_android_gms_tagmanager_zzck) {
                set.addAll(com_google_android_gms_internal_zzqf_zze.zzAw());
                set2.addAll(com_google_android_gms_internal_zzqf_zze.zzAx());
                com_google_android_gms_tagmanager_zzck.zzyW().zzc(com_google_android_gms_internal_zzqf_zze.zzAw(), com_google_android_gms_internal_zzqf_zze.zzAB());
                com_google_android_gms_tagmanager_zzck.zzyX().zzc(com_google_android_gms_internal_zzqf_zze.zzAx(), com_google_android_gms_internal_zzqf_zze.zzAC());
            }
        }, com_google_android_gms_tagmanager_zzco);
    }

    void zza(zzak com_google_android_gms_tagmanager_zzak) {
        zza(this.zzaNj, com_google_android_gms_tagmanager_zzak);
    }

    void zzb(zzak com_google_android_gms_tagmanager_zzak) {
        zza(this.zzaNh, com_google_android_gms_tagmanager_zzak);
    }

    void zzc(zzak com_google_android_gms_tagmanager_zzak) {
        zza(this.zzaNi, com_google_android_gms_tagmanager_zzak);
    }

    public zzbw<com.google.android.gms.internal.zzag.zza> zzeD(String str) {
        this.zzaNp = 0;
        zzag zzet = this.zzaNg.zzet(str);
        zzbw<com.google.android.gms.internal.zzag.zza> zza = zza(str, new HashSet(), zzet.zzyI());
        zzet.zzyK();
        return zza;
    }

    synchronized void zzeE(String str) {
        this.zzaNo = str;
    }

    public synchronized void zzeh(String str) {
        zzeE(str);
        zzag zzeu = this.zzaNg.zzeu(str);
        zzu zzyJ = zzeu.zzyJ();
        for (com.google.android.gms.internal.zzqf.zza zza : (Set) zza(this.zzaNm, zzyJ.zzyB()).getObject()) {
            zza(this.zzaNh, zza, new HashSet(), zzyJ.zzyA());
        }
        zzeu.zzyK();
        zzeE(null);
    }

    public synchronized void zzs(List<zzi> list) {
        for (zzi com_google_android_gms_internal_zzaf_zzi : list) {
            if (com_google_android_gms_internal_zzaf_zzi.name == null || !com_google_android_gms_internal_zzaf_zzi.name.startsWith("gaExperiment:")) {
                zzbg.zzaB("Ignored supplemental: " + com_google_android_gms_internal_zzaf_zzi);
            } else {
                zzaj.zza(this.zzaKz, com_google_android_gms_internal_zzaf_zzi);
            }
        }
    }

    synchronized String zzzp() {
        return this.zzaNo;
    }
}
