package com.google.android.gms.internal;

import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.util.LogPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class zznn implements zznu {
    private static final Uri zzaDR;
    private final LogPrinter zzaDS = new LogPrinter(4, "GA/LogCatTransport");

    static {
        Builder builder = new Builder();
        builder.scheme("uri");
        builder.authority("local");
        zzaDR = builder.build();
    }

    public void zzb(zzno com_google_android_gms_internal_zzno) {
        List<zznq> arrayList = new ArrayList(com_google_android_gms_internal_zzno.zzvQ());
        Collections.sort(arrayList, new Comparator<zznq>(this) {
            final /* synthetic */ zznn zzaDT;

            {
                this.zzaDT = r1;
            }

            public /* synthetic */ int compare(Object x0, Object x1) {
                return zza((zznq) x0, (zznq) x1);
            }

            public int zza(zznq com_google_android_gms_internal_zznq, zznq com_google_android_gms_internal_zznq2) {
                return com_google_android_gms_internal_zznq.getClass().getCanonicalName().compareTo(com_google_android_gms_internal_zznq2.getClass().getCanonicalName());
            }
        });
        StringBuilder stringBuilder = new StringBuilder();
        for (zznq obj : arrayList) {
            Object obj2 = obj.toString();
            if (!TextUtils.isEmpty(obj2)) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(obj2);
            }
        }
        this.zzaDS.println(stringBuilder.toString());
    }

    public Uri zzhe() {
        return zzaDR;
    }
}
