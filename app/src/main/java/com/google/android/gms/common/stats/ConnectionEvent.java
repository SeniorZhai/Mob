package com.google.android.gms.common.stats;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public final class ConnectionEvent implements SafeParcelable {
    public static final Creator<ConnectionEvent> CREATOR = new zza();
    final int zzCY;
    private final long zzabZ;
    private int zzaca;
    private final String zzacb;
    private final String zzacc;
    private final String zzacd;
    private final String zzace;
    private final String zzacf;
    private final String zzacg;
    private final long zzach;
    private final long zzaci;
    private long zzacj;

    ConnectionEvent(int versionCode, long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this.zzCY = versionCode;
        this.zzabZ = timeMillis;
        this.zzaca = eventType;
        this.zzacb = callingProcess;
        this.zzacc = callingService;
        this.zzacd = targetProcess;
        this.zzace = targetService;
        this.zzacj = -1;
        this.zzacf = stackTrace;
        this.zzacg = connKey;
        this.zzach = elapsedRealtime;
        this.zzaci = heapAlloc;
    }

    public ConnectionEvent(long timeMillis, int eventType, String callingProcess, String callingService, String targetProcess, String targetService, String stackTrace, String connKey, long elapsedRealtime, long heapAlloc) {
        this(1, timeMillis, eventType, callingProcess, callingService, targetProcess, targetService, stackTrace, connKey, elapsedRealtime, heapAlloc);
    }

    public int describeContents() {
        return 0;
    }

    public int getEventType() {
        return this.zzaca;
    }

    public long getTimeMillis() {
        return this.zzabZ;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzoG() {
        return this.zzacb;
    }

    public String zzoH() {
        return this.zzacc;
    }

    public String zzoI() {
        return this.zzacd;
    }

    public String zzoJ() {
        return this.zzace;
    }

    public String zzoK() {
        return this.zzacf;
    }

    public String zzoL() {
        return this.zzacg;
    }

    public long zzoM() {
        return this.zzaci;
    }

    public long zzoN() {
        return this.zzach;
    }
}
