package com.google.android.gms.analytics;

import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import com.google.android.gms.analytics.ecommerce.Product;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.google.android.gms.analytics.ecommerce.Promotion;
import com.google.android.gms.analytics.internal.zzab;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.analytics.internal.zzc;
import com.google.android.gms.analytics.internal.zze;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.analytics.internal.zzh;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzim;
import com.google.android.gms.internal.zzin;
import com.google.android.gms.internal.zzio;
import com.google.android.gms.internal.zzip;
import com.google.android.gms.internal.zzno;
import com.google.android.gms.internal.zznu;
import com.google.android.gms.internal.zznx;
import com.google.android.gms.internal.zzny;
import com.google.android.gms.internal.zznz;
import com.google.android.gms.internal.zzoa;
import com.google.android.gms.internal.zzob;
import com.google.android.gms.internal.zzoc;
import com.google.android.gms.internal.zzod;
import com.google.android.gms.internal.zzoe;
import com.google.android.gms.internal.zzof;
import com.helpshift.HSFunnel;
import com.helpshift.res.values.HSConsts;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class zzb extends zzc implements zznu {
    private static DecimalFormat zzIk;
    private final zzf zzIa;
    private final Uri zzIl;
    private final boolean zzIm;
    private final boolean zzIn;
    private final String zztd;

    public zzb(zzf com_google_android_gms_analytics_internal_zzf, String str) {
        this(com_google_android_gms_analytics_internal_zzf, str, true, false);
    }

    public zzb(zzf com_google_android_gms_analytics_internal_zzf, String str, boolean z, boolean z2) {
        super(com_google_android_gms_analytics_internal_zzf);
        zzu.zzcj(str);
        this.zzIa = com_google_android_gms_analytics_internal_zzf;
        this.zztd = str;
        this.zzIm = z;
        this.zzIn = z2;
        this.zzIl = zzaK(this.zztd);
    }

    static String zza(double d) {
        if (zzIk == null) {
            zzIk = new DecimalFormat("0.######");
        }
        return zzIk.format(d);
    }

    private static void zza(Map<String, String> map, String str, double d) {
        if (d != 0.0d) {
            map.put(str, zza(d));
        }
    }

    private static void zza(Map<String, String> map, String str, int i, int i2) {
        if (i > 0 && i2 > 0) {
            map.put(str, i + HSFunnel.OPEN_INBOX + i2);
        }
    }

    private static void zza(Map<String, String> map, String str, String str2) {
        if (!TextUtils.isEmpty(str2)) {
            map.put(str, str2);
        }
    }

    private static void zza(Map<String, String> map, String str, boolean z) {
        if (z) {
            map.put(str, HSConsts.STATUS_INPROGRESS);
        }
    }

    static Uri zzaK(String str) {
        zzu.zzcj(str);
        Builder builder = new Builder();
        builder.scheme("uri");
        builder.authority("google-analytics.com");
        builder.path(str);
        return builder.build();
    }

    public static Map<String, String> zzc(zzno com_google_android_gms_internal_zzno) {
        CharSequence zzO;
        Map hashMap = new HashMap();
        zzio com_google_android_gms_internal_zzio = (zzio) com_google_android_gms_internal_zzno.zzd(zzio.class);
        if (com_google_android_gms_internal_zzio != null) {
            for (Entry entry : com_google_android_gms_internal_zzio.zzhv().entrySet()) {
                String zzh = zzh(entry.getValue());
                if (zzh != null) {
                    hashMap.put(entry.getKey(), zzh);
                }
            }
        }
        zzip com_google_android_gms_internal_zzip = (zzip) com_google_android_gms_internal_zzno.zzd(zzip.class);
        if (com_google_android_gms_internal_zzip != null) {
            zza(hashMap, "t", com_google_android_gms_internal_zzip.zzhw());
            zza(hashMap, "cid", com_google_android_gms_internal_zzip.getClientId());
            zza(hashMap, "uid", com_google_android_gms_internal_zzip.getUserId());
            zza(hashMap, "sc", com_google_android_gms_internal_zzip.zzhz());
            zza(hashMap, "sf", com_google_android_gms_internal_zzip.zzhB());
            zza(hashMap, "ni", com_google_android_gms_internal_zzip.zzhA());
            zza(hashMap, "adid", com_google_android_gms_internal_zzip.zzhx());
            zza(hashMap, "ate", com_google_android_gms_internal_zzip.zzhy());
        }
        zzod com_google_android_gms_internal_zzod = (zzod) com_google_android_gms_internal_zzno.zzd(zzod.class);
        if (com_google_android_gms_internal_zzod != null) {
            zza(hashMap, "cd", com_google_android_gms_internal_zzod.zzwB());
            zza(hashMap, "a", (double) com_google_android_gms_internal_zzod.zzbn());
            zza(hashMap, "dr", com_google_android_gms_internal_zzod.zzwE());
        }
        zzob com_google_android_gms_internal_zzob = (zzob) com_google_android_gms_internal_zzno.zzd(zzob.class);
        if (com_google_android_gms_internal_zzob != null) {
            zza(hashMap, "ec", com_google_android_gms_internal_zzob.zzwy());
            zza(hashMap, "ea", com_google_android_gms_internal_zzob.getAction());
            zza(hashMap, "el", com_google_android_gms_internal_zzob.getLabel());
            zza(hashMap, "ev", (double) com_google_android_gms_internal_zzob.getValue());
        }
        zzny com_google_android_gms_internal_zzny = (zzny) com_google_android_gms_internal_zzno.zzd(zzny.class);
        if (com_google_android_gms_internal_zzny != null) {
            zza(hashMap, "cn", com_google_android_gms_internal_zzny.getName());
            zza(hashMap, "cs", com_google_android_gms_internal_zzny.getSource());
            zza(hashMap, "cm", com_google_android_gms_internal_zzny.zzwj());
            zza(hashMap, "ck", com_google_android_gms_internal_zzny.zzwk());
            zza(hashMap, "cc", com_google_android_gms_internal_zzny.getContent());
            zza(hashMap, "ci", com_google_android_gms_internal_zzny.getId());
            zza(hashMap, "anid", com_google_android_gms_internal_zzny.zzwl());
            zza(hashMap, "gclid", com_google_android_gms_internal_zzny.zzwm());
            zza(hashMap, "dclid", com_google_android_gms_internal_zzny.zzwn());
            zza(hashMap, "aclid", com_google_android_gms_internal_zzny.zzwo());
        }
        zzoc com_google_android_gms_internal_zzoc = (zzoc) com_google_android_gms_internal_zzno.zzd(zzoc.class);
        if (com_google_android_gms_internal_zzoc != null) {
            zza(hashMap, "exd", com_google_android_gms_internal_zzoc.getDescription());
            zza(hashMap, "exf", com_google_android_gms_internal_zzoc.zzwz());
        }
        zzoe com_google_android_gms_internal_zzoe = (zzoe) com_google_android_gms_internal_zzno.zzd(zzoe.class);
        if (com_google_android_gms_internal_zzoe != null) {
            zza(hashMap, "sn", com_google_android_gms_internal_zzoe.zzwI());
            zza(hashMap, "sa", com_google_android_gms_internal_zzoe.getAction());
            zza(hashMap, "st", com_google_android_gms_internal_zzoe.getTarget());
        }
        zzof com_google_android_gms_internal_zzof = (zzof) com_google_android_gms_internal_zzno.zzd(zzof.class);
        if (com_google_android_gms_internal_zzof != null) {
            zza(hashMap, "utv", com_google_android_gms_internal_zzof.zzwJ());
            zza(hashMap, "utt", (double) com_google_android_gms_internal_zzof.getTimeInMillis());
            zza(hashMap, "utc", com_google_android_gms_internal_zzof.zzwy());
            zza(hashMap, "utl", com_google_android_gms_internal_zzof.getLabel());
        }
        zzim com_google_android_gms_internal_zzim = (zzim) com_google_android_gms_internal_zzno.zzd(zzim.class);
        if (com_google_android_gms_internal_zzim != null) {
            for (Entry entry2 : com_google_android_gms_internal_zzim.zzht().entrySet()) {
                zzO = zzc.zzO(((Integer) entry2.getKey()).intValue());
                if (!TextUtils.isEmpty(zzO)) {
                    hashMap.put(zzO, entry2.getValue());
                }
            }
        }
        zzin com_google_android_gms_internal_zzin = (zzin) com_google_android_gms_internal_zzno.zzd(zzin.class);
        if (com_google_android_gms_internal_zzin != null) {
            for (Entry entry22 : com_google_android_gms_internal_zzin.zzhu().entrySet()) {
                zzO = zzc.zzQ(((Integer) entry22.getKey()).intValue());
                if (!TextUtils.isEmpty(zzO)) {
                    hashMap.put(zzO, zza(((Double) entry22.getValue()).doubleValue()));
                }
            }
        }
        zzoa com_google_android_gms_internal_zzoa = (zzoa) com_google_android_gms_internal_zzno.zzd(zzoa.class);
        if (com_google_android_gms_internal_zzoa != null) {
            ProductAction zzwu = com_google_android_gms_internal_zzoa.zzwu();
            if (zzwu != null) {
                for (Entry entry3 : zzwu.build().entrySet()) {
                    if (((String) entry3.getKey()).startsWith("&")) {
                        hashMap.put(((String) entry3.getKey()).substring(1), entry3.getValue());
                    } else {
                        hashMap.put(entry3.getKey(), entry3.getValue());
                    }
                }
            }
            int i = 1;
            for (Promotion zzaQ : com_google_android_gms_internal_zzoa.zzwx()) {
                hashMap.putAll(zzaQ.zzaQ(zzc.zzU(i)));
                i++;
            }
            i = 1;
            for (Product zzaQ2 : com_google_android_gms_internal_zzoa.zzwv()) {
                hashMap.putAll(zzaQ2.zzaQ(zzc.zzS(i)));
                i++;
            }
            i = 1;
            for (Entry entry222 : com_google_android_gms_internal_zzoa.zzww().entrySet()) {
                List<Product> list = (List) entry222.getValue();
                String zzX = zzc.zzX(i);
                int i2 = 1;
                for (Product zzaQ22 : list) {
                    hashMap.putAll(zzaQ22.zzaQ(zzX + zzc.zzV(i2)));
                    i2++;
                }
                if (!TextUtils.isEmpty((CharSequence) entry222.getKey())) {
                    hashMap.put(zzX + "nm", entry222.getKey());
                }
                i++;
            }
        }
        zznz com_google_android_gms_internal_zznz = (zznz) com_google_android_gms_internal_zzno.zzd(zznz.class);
        if (com_google_android_gms_internal_zznz != null) {
            zza(hashMap, "ul", com_google_android_gms_internal_zznz.getLanguage());
            zza(hashMap, "sd", (double) com_google_android_gms_internal_zznz.zzwp());
            zza(hashMap, HSFunnel.START_CSAT_RATING, com_google_android_gms_internal_zznz.zzwq(), com_google_android_gms_internal_zznz.zzwr());
            zza(hashMap, "vp", com_google_android_gms_internal_zznz.zzws(), com_google_android_gms_internal_zznz.zzwt());
        }
        zznx com_google_android_gms_internal_zznx = (zznx) com_google_android_gms_internal_zzno.zzd(zznx.class);
        if (com_google_android_gms_internal_zznx != null) {
            zza(hashMap, "an", com_google_android_gms_internal_zznx.zzjL());
            zza(hashMap, "aid", com_google_android_gms_internal_zznx.zzsK());
            zza(hashMap, "aiid", com_google_android_gms_internal_zznx.zzwi());
            zza(hashMap, "av", com_google_android_gms_internal_zznx.zzjN());
        }
        return hashMap;
    }

    private static String zzh(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return TextUtils.isEmpty(str) ? null : str;
        } else if (!(obj instanceof Double)) {
            return obj instanceof Boolean ? obj != Boolean.FALSE ? HSConsts.STATUS_INPROGRESS : null : String.valueOf(obj);
        } else {
            Double d = (Double) obj;
            return d.doubleValue() != 0.0d ? zza(d.doubleValue()) : null;
        }
    }

    private static String zzz(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry entry : map.entrySet()) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append((String) entry.getKey());
            stringBuilder.append("=");
            stringBuilder.append((String) entry.getValue());
        }
        return stringBuilder.toString();
    }

    public void zzb(zzno com_google_android_gms_internal_zzno) {
        zzu.zzu(com_google_android_gms_internal_zzno);
        zzu.zzb(com_google_android_gms_internal_zzno.zzvU(), (Object) "Can't deliver not submitted measurement");
        zzu.zzbZ("deliver should be called on worker thread");
        zzno zzvP = com_google_android_gms_internal_zzno.zzvP();
        zzip com_google_android_gms_internal_zzip = (zzip) zzvP.zze(zzip.class);
        if (TextUtils.isEmpty(com_google_android_gms_internal_zzip.zzhw())) {
            zzhQ().zzg(zzc(zzvP), "Ignoring measurement without type");
        } else if (TextUtils.isEmpty(com_google_android_gms_internal_zzip.getClientId())) {
            zzhQ().zzg(zzc(zzvP), "Ignoring measurement without client id");
        } else if (!this.zzIa.zzie().getAppOptOut()) {
            double zzhB = com_google_android_gms_internal_zzip.zzhB();
            if (zzam.zza(zzhB, com_google_android_gms_internal_zzip.getClientId())) {
                zzb("Sampling enabled. Hit sampled out. sampling rate", Double.valueOf(zzhB));
                return;
            }
            Map zzc = zzc(zzvP);
            zzc.put("v", HSConsts.STATUS_INPROGRESS);
            zzc.put("_v", zze.zzJB);
            zzc.put("tid", this.zztd);
            if (this.zzIa.zzie().isDryRunEnabled()) {
                zzc("Dry run is enabled. GoogleAnalytics would have sent", zzz(zzc));
                return;
            }
            Map hashMap = new HashMap();
            zzam.zzb(hashMap, "uid", com_google_android_gms_internal_zzip.getUserId());
            zznx com_google_android_gms_internal_zznx = (zznx) com_google_android_gms_internal_zzno.zzd(zznx.class);
            if (com_google_android_gms_internal_zznx != null) {
                zzam.zzb(hashMap, "an", com_google_android_gms_internal_zznx.zzjL());
                zzam.zzb(hashMap, "aid", com_google_android_gms_internal_zznx.zzsK());
                zzam.zzb(hashMap, "av", com_google_android_gms_internal_zznx.zzjN());
                zzam.zzb(hashMap, "aiid", com_google_android_gms_internal_zznx.zzwi());
            }
            zzc.put("_s", String.valueOf(zzhl().zza(new zzh(0, com_google_android_gms_internal_zzip.getClientId(), this.zztd, !TextUtils.isEmpty(com_google_android_gms_internal_zzip.zzhx()), 0, hashMap))));
            zzhl().zza(new zzab(zzhQ(), zzc, com_google_android_gms_internal_zzno.zzvS(), true));
        }
    }

    public Uri zzhe() {
        return this.zzIl;
    }
}
