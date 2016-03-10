package com.google.android.gms.common.internal;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import com.google.android.gms.dynamic.zze;
import com.google.android.gms.dynamic.zzg;
import com.google.android.gms.dynamic.zzg.zza;

public final class zzx extends zzg<zzr> {
    private static final zzx zzabe = new zzx();

    private zzx() {
        super("com.google.android.gms.common.ui.SignInButtonCreatorImpl");
    }

    public static View zzb(Context context, int i, int i2) throws zza {
        return zzabe.zzc(context, i, i2);
    }

    private View zzc(Context context, int i, int i2) throws zza {
        try {
            return (View) zze.zzn(((zzr) zzak(context)).zza(zze.zzw(context), i, i2));
        } catch (Throwable e) {
            throw new zza("Could not get button with size " + i + " and color " + i2, e);
        }
    }

    public zzr zzaJ(IBinder iBinder) {
        return zzr.zza.zzaI(iBinder);
    }

    public /* synthetic */ Object zzd(IBinder iBinder) {
        return zzaJ(iBinder);
    }
}
