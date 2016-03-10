package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class zzce extends zzak {
    private static final String ID = zzad.REGEX_GROUP.toString();
    private static final String zzaMO = zzae.ARG0.toString();
    private static final String zzaMP = zzae.ARG1.toString();
    private static final String zzaMQ = zzae.IGNORE_CASE.toString();
    private static final String zzaMR = zzae.GROUP.toString();

    public zzce() {
        super(ID, zzaMO, zzaMP);
    }

    public zza zzE(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaMO);
        zza com_google_android_gms_internal_zzag_zza2 = (zza) map.get(zzaMP);
        if (com_google_android_gms_internal_zzag_zza == null || com_google_android_gms_internal_zzag_zza == zzdf.zzzQ() || com_google_android_gms_internal_zzag_zza2 == null || com_google_android_gms_internal_zzag_zza2 == zzdf.zzzQ()) {
            return zzdf.zzzQ();
        }
        int i = 64;
        if (zzdf.zzk((zza) map.get(zzaMQ)).booleanValue()) {
            i = 66;
        }
        zza com_google_android_gms_internal_zzag_zza3 = (zza) map.get(zzaMR);
        int intValue;
        if (com_google_android_gms_internal_zzag_zza3 != null) {
            Long zzi = zzdf.zzi(com_google_android_gms_internal_zzag_zza3);
            if (zzi == zzdf.zzzL()) {
                return zzdf.zzzQ();
            }
            intValue = zzi.intValue();
            if (intValue < 0) {
                return zzdf.zzzQ();
            }
        }
        intValue = 1;
        try {
            CharSequence zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
            Object obj = null;
            Matcher matcher = Pattern.compile(zzdf.zzg(com_google_android_gms_internal_zzag_zza2), i).matcher(zzg);
            if (matcher.find() && matcher.groupCount() >= intValue) {
                obj = matcher.group(intValue);
            }
            return obj == null ? zzdf.zzzQ() : zzdf.zzI(obj);
        } catch (PatternSyntaxException e) {
            return zzdf.zzzQ();
        }
    }

    public boolean zzyh() {
        return true;
    }
}
