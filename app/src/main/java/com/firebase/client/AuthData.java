package com.firebase.client;

import java.util.Collections;
import java.util.Map;

public class AuthData {
    private final Map<String, Object> auth;
    private final long expires;
    private final String provider;
    private final Map<String, Object> providerData;
    private final String token;
    private final String uid;

    public AuthData(String token, long expires, String uid, String provider, Map<String, Object> auth, Map<String, Object> providerData) {
        Map unmodifiableMap;
        Map map = null;
        this.token = token;
        this.expires = expires;
        this.uid = uid;
        this.provider = provider;
        if (providerData != null) {
            unmodifiableMap = Collections.unmodifiableMap(providerData);
        } else {
            unmodifiableMap = null;
        }
        this.providerData = unmodifiableMap;
        if (auth != null) {
            map = Collections.unmodifiableMap(auth);
        }
        this.auth = map;
    }

    public String getToken() {
        return this.token;
    }

    public long getExpires() {
        return this.expires;
    }

    public String getUid() {
        return this.uid;
    }

    public String getProvider() {
        return this.provider;
    }

    public Map<String, Object> getProviderData() {
        return this.providerData;
    }

    public Map<String, Object> getAuth() {
        return this.auth;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthData authData = (AuthData) o;
        if (this.provider == null ? authData.provider != null : !this.provider.equals(authData.provider)) {
            return false;
        }
        if (this.providerData == null ? authData.providerData != null : !this.providerData.equals(authData.providerData)) {
            return false;
        }
        if (this.auth == null ? authData.auth != null : !this.auth.equals(authData.auth)) {
            return false;
        }
        if (this.token == null ? authData.token != null : !this.token.equals(authData.token)) {
            return false;
        }
        if (this.expires != authData.expires) {
            return false;
        }
        if (this.uid != null) {
            if (this.uid.equals(authData.uid)) {
                return true;
            }
        } else if (authData.uid == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result;
        int hashCode;
        int i = 0;
        if (this.token != null) {
            result = this.token.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.uid != null) {
            hashCode = this.uid.hashCode();
        } else {
            hashCode = 0;
        }
        i2 = (i2 + hashCode) * 31;
        if (this.provider != null) {
            hashCode = this.provider.hashCode();
        } else {
            hashCode = 0;
        }
        i2 = (i2 + hashCode) * 31;
        if (this.providerData != null) {
            hashCode = this.providerData.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (i2 + hashCode) * 31;
        if (this.auth != null) {
            i = this.auth.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return "AuthData{uid='" + this.uid + '\'' + ", provider='" + this.provider + '\'' + ", token='" + (this.token == null ? null : "***") + '\'' + ", expires='" + this.expires + '\'' + ", auth='" + this.auth + '\'' + ", providerData='" + this.providerData + '\'' + '}';
    }
}
