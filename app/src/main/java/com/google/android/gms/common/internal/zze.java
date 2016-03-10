package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.view.View;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.internal.zzpt;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class zze {
    private final Account zzMY;
    private final String zzOd;
    private final String zzOe;
    private final Set<Scope> zzWv;
    private final int zzWw;
    private final View zzWx;
    private final Set<Scope> zzZS;
    private final Map<Api<?>, zza> zzZT;
    private final zzpt zzZU;
    private Integer zzZV;

    public static final class zza {
        public final Set<Scope> zzWJ;
        public final boolean zzZW;

        public zza(Set<Scope> set, boolean z) {
            zzu.zzu(set);
            this.zzWJ = Collections.unmodifiableSet(set);
            this.zzZW = z;
        }
    }

    public zze(Account account, Collection<Scope> collection, Map<Api<?>, zza> map, int i, View view, String str, String str2, zzpt com_google_android_gms_internal_zzpt) {
        Map map2;
        this.zzMY = account;
        this.zzWv = collection == null ? Collections.EMPTY_SET : Collections.unmodifiableSet(new HashSet(collection));
        if (map == null) {
            map2 = Collections.EMPTY_MAP;
        }
        this.zzZT = map2;
        this.zzWx = view;
        this.zzWw = i;
        this.zzOe = str;
        this.zzOd = str2;
        this.zzZU = com_google_android_gms_internal_zzpt;
        Set hashSet = new HashSet(this.zzWv);
        for (zza com_google_android_gms_common_internal_zze_zza : this.zzZT.values()) {
            hashSet.addAll(com_google_android_gms_common_internal_zze_zza.zzWJ);
        }
        this.zzZS = Collections.unmodifiableSet(hashSet);
    }

    public Account getAccount() {
        return this.zzMY;
    }

    @Deprecated
    public String getAccountName() {
        return this.zzMY != null ? this.zzMY.name : null;
    }

    public void zza(Integer num) {
        this.zzZV = num;
    }

    public Set<Scope> zzb(Api<?> api) {
        zza com_google_android_gms_common_internal_zze_zza = (zza) this.zzZT.get(api);
        if (com_google_android_gms_common_internal_zze_zza == null || com_google_android_gms_common_internal_zze_zza.zzWJ.isEmpty()) {
            return this.zzWv;
        }
        Set<Scope> hashSet = new HashSet(this.zzWv);
        hashSet.addAll(com_google_android_gms_common_internal_zze_zza.zzWJ);
        return hashSet;
    }

    public View zznA() {
        return this.zzWx;
    }

    public zzpt zznB() {
        return this.zzZU;
    }

    public Integer zznC() {
        return this.zzZV;
    }

    @Deprecated
    public String zzns() {
        return zznt().name;
    }

    public Account zznt() {
        return this.zzMY != null ? this.zzMY : new Account("<<default account>>", GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
    }

    public int zznu() {
        return this.zzWw;
    }

    public Set<Scope> zznv() {
        return this.zzWv;
    }

    public Set<Scope> zznw() {
        return this.zzZS;
    }

    public Map<Api<?>, zza> zznx() {
        return this.zzZT;
    }

    public String zzny() {
        return this.zzOe;
    }

    public String zznz() {
        return this.zzOd;
    }
}
