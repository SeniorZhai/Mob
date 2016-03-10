package com.google.android.gms.internal;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.facebook.internal.NativeProtocol;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

public final class zzrg {
    private final ByteBuffer zzaVT;

    public static class zza extends IOException {
        zza(int i, int i2) {
            super("CodedOutputStream was writing to a flat byte array and ran out of space (pos " + i + " limit " + i2 + ").");
        }
    }

    private zzrg(ByteBuffer byteBuffer) {
        this.zzaVT = byteBuffer;
    }

    private zzrg(byte[] bArr, int i, int i2) {
        this(ByteBuffer.wrap(bArr, i, i2));
    }

    public static int zzA(int i, int i2) {
        return zzkM(i) + zzkJ(i2);
    }

    public static zzrg zzA(byte[] bArr) {
        return zzb(bArr, 0, bArr.length);
    }

    public static int zzB(int i, int i2) {
        return zzkM(i) + zzkK(i2);
    }

    public static int zzC(byte[] bArr) {
        return zzkO(bArr.length) + bArr.length;
    }

    public static int zzY(long j) {
        return zzab(j);
    }

    public static int zzZ(long j) {
        return zzab(zzad(j));
    }

    private static int zza(CharSequence charSequence) {
        int length = charSequence.length();
        int i = 0;
        while (i < length && charSequence.charAt(i) < '\u0080') {
            i++;
        }
        int i2 = i;
        i = length;
        while (i2 < length) {
            char charAt = charSequence.charAt(i2);
            if (charAt >= '\u0800') {
                i += zza(charSequence, i2);
                break;
            }
            i2++;
            i = ((127 - charAt) >>> 31) + i;
        }
        if (i >= length) {
            return i;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) i) + 4294967296L));
    }

    private static int zza(CharSequence charSequence, int i) {
        int length = charSequence.length();
        int i2 = 0;
        int i3 = i;
        while (i3 < length) {
            char charAt = charSequence.charAt(i3);
            if (charAt < '\u0800') {
                i2 += (127 - charAt) >>> 31;
            } else {
                i2 += 2;
                if ('\ud800' <= charAt && charAt <= '\udfff') {
                    if (Character.codePointAt(charSequence, i3) < NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST) {
                        throw new IllegalArgumentException("Unpaired surrogate at index " + i3);
                    }
                    i3++;
                }
            }
            i3++;
        }
        return i2;
    }

    private static int zza(CharSequence charSequence, byte[] bArr, int i, int i2) {
        int length = charSequence.length();
        int i3 = 0;
        int i4 = i + i2;
        while (i3 < length && i3 + i < i4) {
            char charAt = charSequence.charAt(i3);
            if (charAt >= '\u0080') {
                break;
            }
            bArr[i + i3] = (byte) charAt;
            i3++;
        }
        if (i3 == length) {
            return i + length;
        }
        int i5 = i + i3;
        while (i3 < length) {
            int i6;
            char charAt2 = charSequence.charAt(i3);
            if (charAt2 < '\u0080' && i5 < i4) {
                i6 = i5 + 1;
                bArr[i5] = (byte) charAt2;
            } else if (charAt2 < '\u0800' && i5 <= i4 - 2) {
                r6 = i5 + 1;
                bArr[i5] = (byte) ((charAt2 >>> 6) | 960);
                i6 = r6 + 1;
                bArr[r6] = (byte) ((charAt2 & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            } else if ((charAt2 < '\ud800' || '\udfff' < charAt2) && i5 <= i4 - 3) {
                i6 = i5 + 1;
                bArr[i5] = (byte) ((charAt2 >>> 12) | 480);
                i5 = i6 + 1;
                bArr[i6] = (byte) (((charAt2 >>> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                i6 = i5 + 1;
                bArr[i5] = (byte) ((charAt2 & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            } else if (i5 <= i4 - 4) {
                if (i3 + 1 != charSequence.length()) {
                    i3++;
                    charAt = charSequence.charAt(i3);
                    if (Character.isSurrogatePair(charAt2, charAt)) {
                        int toCodePoint = Character.toCodePoint(charAt2, charAt);
                        i6 = i5 + 1;
                        bArr[i5] = (byte) ((toCodePoint >>> 18) | 240);
                        i5 = i6 + 1;
                        bArr[i6] = (byte) (((toCodePoint >>> 12) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                        r6 = i5 + 1;
                        bArr[i5] = (byte) (((toCodePoint >>> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                        i6 = r6 + 1;
                        bArr[r6] = (byte) ((toCodePoint & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i3 - 1));
            } else {
                throw new ArrayIndexOutOfBoundsException("Failed writing " + charAt2 + " at index " + i5);
            }
            i3++;
            i5 = i6;
        }
        return i5;
    }

    private static void zza(CharSequence charSequence, ByteBuffer byteBuffer) {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else if (byteBuffer.hasArray()) {
            try {
                byteBuffer.position(zza(charSequence, byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining()) - byteBuffer.arrayOffset());
            } catch (Throwable e) {
                BufferOverflowException bufferOverflowException = new BufferOverflowException();
                bufferOverflowException.initCause(e);
                throw bufferOverflowException;
            }
        } else {
            zzb(charSequence, byteBuffer);
        }
    }

    public static int zzab(long j) {
        return (-128 & j) == 0 ? 1 : (-16384 & j) == 0 ? 2 : (-2097152 & j) == 0 ? 3 : (-268435456 & j) == 0 ? 4 : (-34359738368L & j) == 0 ? 5 : (-4398046511104L & j) == 0 ? 6 : (-562949953421312L & j) == 0 ? 7 : (-72057594037927936L & j) == 0 ? 8 : (Long.MIN_VALUE & j) == 0 ? 9 : 10;
    }

    public static long zzad(long j) {
        return (j << 1) ^ (j >> 63);
    }

    public static int zzas(boolean z) {
        return 1;
    }

    public static int zzb(int i, double d) {
        return zzkM(i) + zzi(d);
    }

    public static int zzb(int i, zzrn com_google_android_gms_internal_zzrn) {
        return (zzkM(i) * 2) + zzd(com_google_android_gms_internal_zzrn);
    }

    public static int zzb(int i, byte[] bArr) {
        return zzkM(i) + zzC(bArr);
    }

    public static zzrg zzb(byte[] bArr, int i, int i2) {
        return new zzrg(bArr, i, i2);
    }

    private static void zzb(CharSequence charSequence, ByteBuffer byteBuffer) {
        int length = charSequence.length();
        int i = 0;
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt < '\u0080') {
                byteBuffer.put((byte) charAt);
            } else if (charAt < '\u0800') {
                byteBuffer.put((byte) ((charAt >>> 6) | 960));
                byteBuffer.put((byte) ((charAt & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
            } else if (charAt < '\ud800' || '\udfff' < charAt) {
                byteBuffer.put((byte) ((charAt >>> 12) | 480));
                byteBuffer.put((byte) (((charAt >>> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
                byteBuffer.put((byte) ((charAt & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
            } else {
                if (i + 1 != charSequence.length()) {
                    i++;
                    char charAt2 = charSequence.charAt(i);
                    if (Character.isSurrogatePair(charAt, charAt2)) {
                        int toCodePoint = Character.toCodePoint(charAt, charAt2);
                        byteBuffer.put((byte) ((toCodePoint >>> 18) | 240));
                        byteBuffer.put((byte) (((toCodePoint >>> 12) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
                        byteBuffer.put((byte) (((toCodePoint >>> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
                        byteBuffer.put((byte) ((toCodePoint & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS));
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i - 1));
            }
            i++;
        }
    }

    public static int zzc(int i, float f) {
        return zzkM(i) + zzj(f);
    }

    public static int zzc(int i, zzrn com_google_android_gms_internal_zzrn) {
        return zzkM(i) + zze(com_google_android_gms_internal_zzrn);
    }

    public static int zzc(int i, boolean z) {
        return zzkM(i) + zzas(z);
    }

    public static int zzd(int i, long j) {
        return zzkM(i) + zzY(j);
    }

    public static int zzd(zzrn com_google_android_gms_internal_zzrn) {
        return com_google_android_gms_internal_zzrn.zzBV();
    }

    public static int zze(int i, long j) {
        return zzkM(i) + zzZ(j);
    }

    public static int zze(zzrn com_google_android_gms_internal_zzrn) {
        int zzBV = com_google_android_gms_internal_zzrn.zzBV();
        return zzBV + zzkO(zzBV);
    }

    public static int zzfj(String str) {
        int zza = zza(str);
        return zza + zzkO(zza);
    }

    public static int zzi(double d) {
        return 8;
    }

    public static int zzj(float f) {
        return 4;
    }

    public static int zzk(int i, String str) {
        return zzkM(i) + zzfj(str);
    }

    public static int zzkJ(int i) {
        return i >= 0 ? zzkO(i) : 10;
    }

    public static int zzkK(int i) {
        return zzkO(zzkQ(i));
    }

    public static int zzkM(int i) {
        return zzkO(zzrq.zzD(i, 0));
    }

    public static int zzkO(int i) {
        return (i & -128) == 0 ? 1 : (i & -16384) == 0 ? 2 : (-2097152 & i) == 0 ? 3 : (-268435456 & i) == 0 ? 4 : 5;
    }

    public static int zzkQ(int i) {
        return (i << 1) ^ (i >> 31);
    }

    public void zzB(byte[] bArr) throws IOException {
        zzkN(bArr.length);
        zzD(bArr);
    }

    public int zzBG() {
        return this.zzaVT.remaining();
    }

    public void zzBH() {
        if (zzBG() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public void zzC(int i, int i2) throws IOException {
        zzkN(zzrq.zzD(i, i2));
    }

    public void zzD(byte[] bArr) throws IOException {
        zzc(bArr, 0, bArr.length);
    }

    public void zzW(long j) throws IOException {
        zzaa(j);
    }

    public void zzX(long j) throws IOException {
        zzaa(zzad(j));
    }

    public void zza(int i, double d) throws IOException {
        zzC(i, 1);
        zzh(d);
    }

    public void zza(int i, zzrn com_google_android_gms_internal_zzrn) throws IOException {
        zzC(i, 2);
        zzc(com_google_android_gms_internal_zzrn);
    }

    public void zza(int i, byte[] bArr) throws IOException {
        zzC(i, 2);
        zzB(bArr);
    }

    public void zzaa(long j) throws IOException {
        while ((-128 & j) != 0) {
            zzkL((((int) j) & TransportMediator.KEYCODE_MEDIA_PAUSE) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            j >>>= 7;
        }
        zzkL((int) j);
    }

    public void zzac(long j) throws IOException {
        zzkL(((int) j) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 8)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 16)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 24)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 32)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 40)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 48)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL(((int) (j >> 56)) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public void zzar(boolean z) throws IOException {
        zzkL(z ? 1 : 0);
    }

    public void zzb(byte b) throws IOException {
        if (this.zzaVT.hasRemaining()) {
            this.zzaVT.put(b);
            return;
        }
        throw new zza(this.zzaVT.position(), this.zzaVT.limit());
    }

    public void zzb(int i, float f) throws IOException {
        zzC(i, 5);
        zzi(f);
    }

    public void zzb(int i, long j) throws IOException {
        zzC(i, 0);
        zzW(j);
    }

    public void zzb(int i, String str) throws IOException {
        zzC(i, 2);
        zzfi(str);
    }

    public void zzb(int i, boolean z) throws IOException {
        zzC(i, 0);
        zzar(z);
    }

    public void zzb(zzrn com_google_android_gms_internal_zzrn) throws IOException {
        com_google_android_gms_internal_zzrn.zza(this);
    }

    public void zzc(int i, long j) throws IOException {
        zzC(i, 0);
        zzX(j);
    }

    public void zzc(zzrn com_google_android_gms_internal_zzrn) throws IOException {
        zzkN(com_google_android_gms_internal_zzrn.zzBU());
        com_google_android_gms_internal_zzrn.zza(this);
    }

    public void zzc(byte[] bArr, int i, int i2) throws IOException {
        if (this.zzaVT.remaining() >= i2) {
            this.zzaVT.put(bArr, i, i2);
            return;
        }
        throw new zza(this.zzaVT.position(), this.zzaVT.limit());
    }

    public void zzfi(String str) throws IOException {
        try {
            int zzkO = zzkO(str.length());
            if (zzkO == zzkO(str.length() * 3)) {
                int position = this.zzaVT.position();
                this.zzaVT.position(position + zzkO);
                zza((CharSequence) str, this.zzaVT);
                int position2 = this.zzaVT.position();
                this.zzaVT.position(position);
                zzkN((position2 - position) - zzkO);
                this.zzaVT.position(position2);
                return;
            }
            zzkN(zza(str));
            zza((CharSequence) str, this.zzaVT);
        } catch (BufferOverflowException e) {
            throw new zza(this.zzaVT.position(), this.zzaVT.limit());
        }
    }

    public void zzh(double d) throws IOException {
        zzac(Double.doubleToLongBits(d));
    }

    public void zzi(float f) throws IOException {
        zzkP(Float.floatToIntBits(f));
    }

    public void zzkH(int i) throws IOException {
        if (i >= 0) {
            zzkN(i);
        } else {
            zzaa((long) i);
        }
    }

    public void zzkI(int i) throws IOException {
        zzkN(zzkQ(i));
    }

    public void zzkL(int i) throws IOException {
        zzb((byte) i);
    }

    public void zzkN(int i) throws IOException {
        while ((i & -128) != 0) {
            zzkL((i & TransportMediator.KEYCODE_MEDIA_PAUSE) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            i >>>= 7;
        }
        zzkL(i);
    }

    public void zzkP(int i) throws IOException {
        zzkL(i & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL((i >> 8) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL((i >> 16) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        zzkL((i >> 24) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public void zzy(int i, int i2) throws IOException {
        zzC(i, 0);
        zzkH(i2);
    }

    public void zzz(int i, int i2) throws IOException {
        zzC(i, 0);
        zzkI(i2);
    }
}
