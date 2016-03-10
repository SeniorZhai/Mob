package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class zzbb extends zzak {
    private static final String ID = zzad.LANGUAGE.toString();

    public zzbb() {
        super(ID, new String[0]);
    }

    public zza zzE(Map<String, zza> map) {
        Locale locale = Locale.getDefault();
        if (locale == null) {
            return zzdf.zzzQ();
        }
        String language = locale.getLanguage();
        return language == null ? zzdf.zzzQ() : zzdf.zzI(language.toLowerCase());
    }

    public /* bridge */ /* synthetic */ String zzyM() {
        return super.zzyM();
    }

    public /* bridge */ /* synthetic */ Set zzyN() {
        return super.zzyN();
    }

    public boolean zzyh() {
        return false;
    }
}
