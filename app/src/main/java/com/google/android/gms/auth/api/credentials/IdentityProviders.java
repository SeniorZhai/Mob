package com.google.android.gms.auth.api.credentials;

import android.accounts.Account;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.internal.zzu;

public final class IdentityProviders {
    public static final String FACEBOOK = "https://www.facebook.com";
    public static final String GOOGLE = "https://accounts.google.com";
    public static final String LINKEDIN = "https://www.linkedin.com";
    public static final String MICROSOFT = "https://login.live.com";
    public static final String PAYPAL = "https://www.paypal.com";
    public static final String TWITTER = "https://twitter.com";
    public static final String YAHOO = "https://login.yahoo.com";

    private IdentityProviders() {
    }

    public static final String getIdentityProviderForAccount(Account account) {
        zzu.zzb((Object) account, (Object) "account cannot be null");
        if (GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE.equals(account.type)) {
            return GOOGLE;
        }
        return "com.facebook.auth.login".equals(account.type) ? FACEBOOK : null;
    }
}
