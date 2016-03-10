package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import com.facebook.internal.ServerProtocol;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

class zzj extends zzdd {
    private static final String ID = zzad.ARBITRARY_PIXEL.toString();
    private static final String URL = zzae.URL.toString();
    private static final String zzaKr = zzae.ADDITIONAL_PARAMS.toString();
    private static final String zzaKs = zzae.UNREPEATABLE.toString();
    static final String zzaKt = ("gtm_" + ID + "_unrepeatable");
    private static final Set<String> zzaKu = new HashSet();
    private final Context mContext;
    private final zza zzaKv;

    public interface zza {
        zzar zzyi();
    }

    class AnonymousClass1 implements zza {
        final /* synthetic */ Context zzqV;

        AnonymousClass1(Context context) {
            this.zzqV = context;
        }

        public zzar zzyi() {
            return zzz.zzaF(this.zzqV);
        }
    }

    public zzj(Context context) {
        this(context, new AnonymousClass1(context));
    }

    zzj(Context context, zza com_google_android_gms_tagmanager_zzj_zza) {
        super(ID, URL);
        this.zzaKv = com_google_android_gms_tagmanager_zzj_zza;
        this.mContext = context;
    }

    private synchronized boolean zzeb(String str) {
        boolean z = true;
        synchronized (this) {
            if (!zzed(str)) {
                if (zzec(str)) {
                    zzaKu.add(str);
                } else {
                    z = false;
                }
            }
        }
        return z;
    }

    public void zzG(Map<String, com.google.android.gms.internal.zzag.zza> map) {
        String zzg = map.get(zzaKs) != null ? zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(zzaKs)) : null;
        if (zzg == null || !zzeb(zzg)) {
            Builder buildUpon = Uri.parse(zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(URL))).buildUpon();
            com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) map.get(zzaKr);
            if (com_google_android_gms_internal_zzag_zza != null) {
                Object zzl = zzdf.zzl(com_google_android_gms_internal_zzag_zza);
                if (zzl instanceof List) {
                    for (Object zzl2 : (List) zzl2) {
                        if (zzl2 instanceof Map) {
                            for (Entry entry : ((Map) zzl2).entrySet()) {
                                buildUpon.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
                            }
                        } else {
                            zzbg.zzaz("ArbitraryPixel: additional params contains non-map: not sending partial hit: " + buildUpon.build().toString());
                            return;
                        }
                    }
                }
                zzbg.zzaz("ArbitraryPixel: additional params not a list: not sending partial hit: " + buildUpon.build().toString());
                return;
            }
            String uri = buildUpon.build().toString();
            this.zzaKv.zzyi().zzes(uri);
            zzbg.zzaB("ArbitraryPixel: url = " + uri);
            if (zzg != null) {
                synchronized (zzj.class) {
                    zzaKu.add(zzg);
                    zzcv.zza(this.mContext, zzaKt, zzg, ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
                }
            }
        }
    }

    boolean zzec(String str) {
        return this.mContext.getSharedPreferences(zzaKt, 0).contains(str);
    }

    boolean zzed(String str) {
        return zzaKu.contains(str);
    }
}
