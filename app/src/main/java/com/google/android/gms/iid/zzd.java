package com.google.android.gms.iid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class zzd {
    SharedPreferences zzaxq;
    Context zzpH;

    public zzd(Context context) {
        this(context, "com.google.android.gms.appid");
    }

    public zzd(Context context, String str) {
        this.zzpH = context;
        this.zzaxq = context.getSharedPreferences(str, 4);
        zzde(str + "-no-backup");
    }

    private void zzde(String str) {
        File file = new File(new ContextCompat().getNoBackupFilesDir(this.zzpH), str);
        if (!file.exists()) {
            try {
                if (file.createNewFile() && !isEmpty()) {
                    Log.i("InstanceID/Store", "App restored, clearing state");
                    InstanceIDListenerService.zza(this.zzpH, this);
                }
            } catch (IOException e) {
                if (Log.isLoggable("InstanceID/Store", 3)) {
                    Log.d("InstanceID/Store", "Error creating file in no backup dir: " + e.getMessage());
                }
            }
        }
    }

    private String zzf(String str, String str2, String str3) {
        return str + "|T|" + str2 + "|" + str3;
    }

    synchronized String get(String key) {
        return this.zzaxq.getString(key, null);
    }

    synchronized String get(String subtype, String key) {
        return this.zzaxq.getString(subtype + "|S|" + key, null);
    }

    boolean isEmpty() {
        return this.zzaxq.getAll().isEmpty();
    }

    public synchronized void zza(String str, String str2, String str3, String str4, String str5) {
        String zzf = zzf(str, str2, str3);
        Editor edit = this.zzaxq.edit();
        edit.putString(zzf, str4);
        edit.putString("appVersion", str5);
        edit.putString("lastToken", Long.toString(System.currentTimeMillis() / 1000));
        edit.commit();
    }

    public synchronized void zzdf(String str) {
        Editor edit = this.zzaxq.edit();
        for (String str2 : this.zzaxq.getAll().keySet()) {
            if (str2.startsWith(str)) {
                edit.remove(str2);
            }
        }
        edit.commit();
    }

    public KeyPair zzdg(String str) {
        return zzdj(str);
    }

    void zzdh(String str) {
        zzdf(str + "|");
    }

    public void zzdi(String str) {
        zzdf(str + "|T|");
    }

    KeyPair zzdj(String str) {
        Object e;
        String str2 = get(str, "|P|");
        String str3 = get(str, "|K|");
        if (str3 == null) {
            return null;
        }
        try {
            byte[] decode = Base64.decode(str2, 8);
            byte[] decode2 = Base64.decode(str3, 8);
            KeyFactory instance = KeyFactory.getInstance("RSA");
            return new KeyPair(instance.generatePublic(new X509EncodedKeySpec(decode)), instance.generatePrivate(new PKCS8EncodedKeySpec(decode2)));
        } catch (InvalidKeySpecException e2) {
            e = e2;
            Log.w("InstanceID/Store", "Invalid key stored " + e);
            InstanceIDListenerService.zza(this.zzpH, this);
            return null;
        } catch (NoSuchAlgorithmException e3) {
            e = e3;
            Log.w("InstanceID/Store", "Invalid key stored " + e);
            InstanceIDListenerService.zza(this.zzpH, this);
            return null;
        }
    }

    synchronized KeyPair zze(String str, long j) {
        KeyPair zzud;
        zzud = zza.zzud();
        this.zzaxq.edit().putString(str + "|P|", InstanceID.zzm(zzud.getPublic().getEncoded())).putString(str + "|K|", InstanceID.zzm(zzud.getPrivate().getEncoded())).putString(str + "|S|" + "cre", Long.toString(j)).commit();
        return zzud;
    }

    public synchronized String zzg(String str, String str2, String str3) {
        return this.zzaxq.getString(zzf(str, str2, str3), null);
    }

    public synchronized void zzh(String str, String str2, String str3) {
        String zzf = zzf(str, str2, str3);
        Editor edit = this.zzaxq.edit();
        edit.remove(zzf);
        edit.commit();
    }

    public synchronized void zzul() {
        this.zzaxq.edit().clear().commit();
    }
}
