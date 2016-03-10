package com.google.android.gms.playlog.internal;

import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzrr.zzd;
import java.util.ArrayList;

public class zzb {
    private final ArrayList<zza> zzaGI;
    private int zzaGJ;

    public static class zza {
        public final PlayLoggerContext zzaGK;
        public final LogEvent zzaGL;
        public final zzd zzaGM;

        private zza(PlayLoggerContext playLoggerContext, LogEvent logEvent) {
            this.zzaGK = (PlayLoggerContext) zzu.zzu(playLoggerContext);
            this.zzaGL = (LogEvent) zzu.zzu(logEvent);
            this.zzaGM = null;
        }
    }

    public zzb() {
        this(100);
    }

    public zzb(int i) {
        this.zzaGI = new ArrayList();
        this.zzaGJ = i;
    }

    private void zzxo() {
        while (getSize() > getCapacity()) {
            this.zzaGI.remove(0);
        }
    }

    public void clear() {
        this.zzaGI.clear();
    }

    public int getCapacity() {
        return this.zzaGJ;
    }

    public int getSize() {
        return this.zzaGI.size();
    }

    public boolean isEmpty() {
        return this.zzaGI.isEmpty();
    }

    public void zza(PlayLoggerContext playLoggerContext, LogEvent logEvent) {
        this.zzaGI.add(new zza(playLoggerContext, logEvent));
        zzxo();
    }

    public ArrayList<zza> zzxn() {
        return this.zzaGI;
    }
}
