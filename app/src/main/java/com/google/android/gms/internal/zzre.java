package com.google.android.gms.internal;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzre {
    private final byte[] zzaVH = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY];
    private int zzaVI;
    private int zzaVJ;

    public zzre(byte[] bArr) {
        int i;
        for (i = 0; i < AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY; i++) {
            this.zzaVH[i] = (byte) i;
        }
        i = 0;
        for (int i2 = 0; i2 < AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY; i2++) {
            i = ((i + this.zzaVH[i2]) + bArr[i2 % bArr.length]) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            byte b = this.zzaVH[i2];
            this.zzaVH[i2] = this.zzaVH[i];
            this.zzaVH[i] = b;
        }
        this.zzaVI = 0;
        this.zzaVJ = 0;
    }

    public void zzy(byte[] bArr) {
        int i = this.zzaVI;
        int i2 = this.zzaVJ;
        for (int i3 = 0; i3 < bArr.length; i3++) {
            i = (i + 1) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            i2 = (i2 + this.zzaVH[i]) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            byte b = this.zzaVH[i];
            this.zzaVH[i] = this.zzaVH[i2];
            this.zzaVH[i2] = b;
            bArr[i3] = (byte) (bArr[i3] ^ this.zzaVH[(this.zzaVH[i] + this.zzaVH[i2]) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT]);
        }
        this.zzaVI = i;
        this.zzaVJ = i2;
    }
}
