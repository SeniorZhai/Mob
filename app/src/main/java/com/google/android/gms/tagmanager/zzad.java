package com.google.android.gms.tagmanager;

import android.util.Base64;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzad extends zzak {
    private static final String ID = com.google.android.gms.internal.zzad.ENCODE.toString();
    private static final String zzaLE = zzae.ARG0.toString();
    private static final String zzaLF = zzae.NO_PADDING.toString();
    private static final String zzaLG = zzae.INPUT_FORMAT.toString();
    private static final String zzaLH = zzae.OUTPUT_FORMAT.toString();

    public zzad() {
        super(ID, zzaLE);
    }

    public zza zzE(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLE);
        if (com_google_android_gms_internal_zzag_zza == null || com_google_android_gms_internal_zzag_zza == zzdf.zzzQ()) {
            return zzdf.zzzQ();
        }
        String zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLG);
        String zzg2 = com_google_android_gms_internal_zzag_zza == null ? MimeTypes.BASE_TYPE_TEXT : zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLH);
        String zzg3 = com_google_android_gms_internal_zzag_zza == null ? "base16" : zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLF);
        int i = (com_google_android_gms_internal_zzag_zza == null || !zzdf.zzk(com_google_android_gms_internal_zzag_zza).booleanValue()) ? 2 : 3;
        try {
            byte[] bytes;
            Object zzi;
            if (MimeTypes.BASE_TYPE_TEXT.equals(zzg2)) {
                bytes = zzg.getBytes();
            } else if ("base16".equals(zzg2)) {
                bytes = zzk.zzee(zzg);
            } else if ("base64".equals(zzg2)) {
                bytes = Base64.decode(zzg, i);
            } else if ("base64url".equals(zzg2)) {
                bytes = Base64.decode(zzg, i | 8);
            } else {
                zzbg.zzaz("Encode: unknown input format: " + zzg2);
                return zzdf.zzzQ();
            }
            if ("base16".equals(zzg3)) {
                zzi = zzk.zzi(bytes);
            } else if ("base64".equals(zzg3)) {
                zzi = Base64.encodeToString(bytes, i);
            } else if ("base64url".equals(zzg3)) {
                zzi = Base64.encodeToString(bytes, i | 8);
            } else {
                zzbg.zzaz("Encode: unknown output format: " + zzg3);
                return zzdf.zzzQ();
            }
            return zzdf.zzI(zzi);
        } catch (IllegalArgumentException e) {
            zzbg.zzaz("Encode: invalid input:");
            return zzdf.zzzQ();
        }
    }

    public boolean zzyh() {
        return true;
    }
}
