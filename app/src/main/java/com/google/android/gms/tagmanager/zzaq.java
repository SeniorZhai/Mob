package com.google.android.gms.tagmanager;

import android.text.TextUtils;

class zzaq {
    private final long zzMb;
    private final long zzaLL;
    private final long zzaLM;
    private String zzaLN;

    zzaq(long j, long j2, long j3) {
        this.zzaLL = j;
        this.zzMb = j2;
        this.zzaLM = j3;
    }

    void zzev(String str) {
        if (str != null && !TextUtils.isEmpty(str.trim())) {
            this.zzaLN = str;
        }
    }

    long zzyO() {
        return this.zzaLL;
    }

    long zzyP() {
        return this.zzaLM;
    }

    String zzyQ() {
        return this.zzaLN;
    }
}
