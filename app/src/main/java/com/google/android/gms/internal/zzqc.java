package com.google.android.gms.internal;

import com.google.android.gms.internal.zzqf.zzc;
import com.google.android.gms.internal.zzqf.zzg;
import com.google.android.gms.tagmanager.zzbg;
import org.json.JSONException;

public final class zzqc {
    public static zzqb zzaPm = new zzqb() {
        public Object zzt(byte[] bArr) throws zzg {
            if (bArr == null) {
                throw new zzg("Cannot parse a null byte[]");
            } else if (bArr.length == 0) {
                throw new zzg("Cannot parse a 0 length byte[]");
            } else {
                try {
                    zzc zzey = zzpz.zzey(new String(bArr));
                    if (zzey != null) {
                        zzbg.zzaB("The container was successfully parsed from the resource");
                    }
                    return zzey;
                } catch (JSONException e) {
                    throw new zzg("The resource data is corrupted. The container cannot be extracted from the binary data");
                } catch (zzg e2) {
                    throw new zzg("The resource data is invalid. The container cannot be extracted from the binary data");
                }
            }
        }
    };
}
