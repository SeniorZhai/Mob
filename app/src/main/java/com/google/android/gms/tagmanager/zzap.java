package com.google.android.gms.tagmanager;

import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

class zzap extends zzak {
    private static final String ID = zzad.HASH.toString();
    private static final String zzaLE = zzae.ARG0.toString();
    private static final String zzaLG = zzae.INPUT_FORMAT.toString();
    private static final String zzaLK = zzae.ALGORITHM.toString();

    public zzap() {
        super(ID, zzaLE);
    }

    private byte[] zzd(String str, byte[] bArr) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance(str);
        instance.update(bArr);
        return instance.digest();
    }

    public zza zzE(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLE);
        if (com_google_android_gms_internal_zzag_zza == null || com_google_android_gms_internal_zzag_zza == zzdf.zzzQ()) {
            return zzdf.zzzQ();
        }
        byte[] bytes;
        String zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLK);
        String zzg2 = com_google_android_gms_internal_zzag_zza == null ? CommonUtils.MD5_INSTANCE : zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLG);
        String zzg3 = com_google_android_gms_internal_zzag_zza == null ? MimeTypes.BASE_TYPE_TEXT : zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        if (MimeTypes.BASE_TYPE_TEXT.equals(zzg3)) {
            bytes = zzg.getBytes();
        } else if ("base16".equals(zzg3)) {
            bytes = zzk.zzee(zzg);
        } else {
            zzbg.zzaz("Hash: unknown input format: " + zzg3);
            return zzdf.zzzQ();
        }
        try {
            return zzdf.zzI(zzk.zzi(zzd(zzg2, bytes)));
        } catch (NoSuchAlgorithmException e) {
            zzbg.zzaz("Hash: unknown algorithm: " + zzg2);
            return zzdf.zzzQ();
        }
    }

    public boolean zzyh() {
        return true;
    }
}
