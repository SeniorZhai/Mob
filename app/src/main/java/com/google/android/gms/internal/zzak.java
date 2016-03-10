package com.google.android.gms.internal;

import android.content.Context;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.android.volley.DefaultRetryPolicy;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import org.apache.http.protocol.HTTP;

public abstract class zzak implements zzaj {
    protected MotionEvent zzmR;
    protected DisplayMetrics zzmS;
    protected zzap zzmT;
    private zzaq zzmU;

    protected zzak(Context context, zzap com_google_android_gms_internal_zzap, zzaq com_google_android_gms_internal_zzaq) {
        this.zzmT = com_google_android_gms_internal_zzap;
        this.zzmU = com_google_android_gms_internal_zzaq;
        try {
            this.zzmS = context.getResources().getDisplayMetrics();
        } catch (UnsupportedOperationException e) {
            this.zzmS = new DisplayMetrics();
            this.zzmS.density = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        }
    }

    private void zzS() {
        this.zzmU.reset();
    }

    private byte[] zzT() throws IOException {
        return this.zzmU.zzac();
    }

    private String zza(Context context, String str, boolean z) {
        try {
            byte[] zzT;
            synchronized (this) {
                zzS();
                if (z) {
                    zzd(context);
                } else {
                    zzc(context);
                }
                zzT = zzT();
            }
            return zzT.length == 0 ? Integer.toString(5) : zza(zzT, str);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(7);
        } catch (UnsupportedEncodingException e2) {
            return Integer.toString(7);
        } catch (IOException e3) {
            return Integer.toString(3);
        }
    }

    public String zza(Context context, String str) {
        return zza(context, str, true);
    }

    String zza(byte[] bArr, String str) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        byte[] bArr2;
        if (bArr.length > 239) {
            zzS();
            zza(20, 1);
            bArr = zzT();
        }
        if (bArr.length < 239) {
            bArr2 = new byte[(239 - bArr.length)];
            new SecureRandom().nextBytes(bArr2);
            bArr2 = ByteBuffer.allocate(240).put((byte) bArr.length).put(bArr).put(bArr2).array();
        } else {
            bArr2 = ByteBuffer.allocate(240).put((byte) bArr.length).put(bArr).array();
        }
        MessageDigest instance = MessageDigest.getInstance(CommonUtils.MD5_INSTANCE);
        instance.update(bArr2);
        bArr2 = ByteBuffer.allocate(AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY).put(instance.digest()).put(bArr2).array();
        byte[] bArr3 = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY];
        new zzai().zzb(bArr2, bArr3);
        if (str != null && str.length() > 0) {
            zza(str, bArr3);
        }
        return this.zzmT.zza(bArr3, true);
    }

    public void zza(int i, int i2, int i3) {
        if (this.zzmR != null) {
            this.zzmR.recycle();
        }
        this.zzmR = MotionEvent.obtain(0, (long) i3, 1, ((float) i) * this.zzmS.density, ((float) i2) * this.zzmS.density, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0);
    }

    protected void zza(int i, long j) throws IOException {
        this.zzmU.zzb(i, j);
    }

    protected void zza(int i, String str) throws IOException {
        this.zzmU.zzb(i, str);
    }

    public void zza(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1) {
            if (this.zzmR != null) {
                this.zzmR.recycle();
            }
            this.zzmR = MotionEvent.obtain(motionEvent);
        }
    }

    void zza(String str, byte[] bArr) throws UnsupportedEncodingException {
        if (str.length() > 32) {
            str = str.substring(0, 32);
        }
        new zzre(str.getBytes(HTTP.UTF_8)).zzy(bArr);
    }

    public String zzb(Context context) {
        return zza(context, null, false);
    }

    protected abstract void zzc(Context context);

    protected abstract void zzd(Context context);

    protected String zzk(String str) {
        if (str == null || !str.matches("^[a-fA-F0-9]{8}-([a-fA-F0-9]{4}-){3}[a-fA-F0-9]{12}$")) {
            return str;
        }
        UUID fromString = UUID.fromString(str);
        byte[] bArr = new byte[16];
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        wrap.putLong(fromString.getMostSignificantBits());
        wrap.putLong(fromString.getLeastSignificantBits());
        return this.zzmT.zza(bArr, true);
    }
}
