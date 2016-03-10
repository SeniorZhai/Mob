package com.google.android.gms.internal;

import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.common.internal.zzq;
import com.google.android.gms.signin.internal.zze;
import java.util.Set;

public interface zzps extends Client {
    void connect();

    void zza(IAccountAccessor iAccountAccessor, Set<Scope> set, zze com_google_android_gms_signin_internal_zze);

    void zza(IAccountAccessor iAccountAccessor, boolean z);

    void zza(zzq com_google_android_gms_common_internal_zzq);

    void zzxY();
}
