package com.google.android.gms.internal;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import org.apache.http.protocol.HTTP;

public final class zzrf {
    private final byte[] buffer;
    private int zzaVK;
    private int zzaVL;
    private int zzaVM;
    private int zzaVN;
    private int zzaVO;
    private int zzaVP = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
    private int zzaVQ;
    private int zzaVR = 64;
    private int zzaVS = 67108864;

    private zzrf(byte[] bArr, int i, int i2) {
        this.buffer = bArr;
        this.zzaVK = i;
        this.zzaVL = i + i2;
        this.zzaVN = i;
    }

    private void zzBC() {
        this.zzaVL += this.zzaVM;
        int i = this.zzaVL;
        if (i > this.zzaVP) {
            this.zzaVM = i - this.zzaVP;
            this.zzaVL -= this.zzaVM;
            return;
        }
        this.zzaVM = 0;
    }

    public static long zzV(long j) {
        return (j >>> 1) ^ (-(1 & j));
    }

    public static zzrf zza(byte[] bArr, int i, int i2) {
        return new zzrf(bArr, i, i2);
    }

    public static int zzkB(int i) {
        return (i >>> 1) ^ (-(i & 1));
    }

    public static zzrf zzz(byte[] bArr) {
        return zza(bArr, 0, bArr.length);
    }

    public int getPosition() {
        return this.zzaVN - this.zzaVK;
    }

    public byte[] readBytes() throws IOException {
        int zzBy = zzBy();
        if (zzBy > this.zzaVL - this.zzaVN || zzBy <= 0) {
            return zzkF(zzBy);
        }
        Object obj = new byte[zzBy];
        System.arraycopy(this.buffer, this.zzaVN, obj, 0, zzBy);
        this.zzaVN = zzBy + this.zzaVN;
        return obj;
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(zzBB());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(zzBA());
    }

    public String readString() throws IOException {
        int zzBy = zzBy();
        if (zzBy > this.zzaVL - this.zzaVN || zzBy <= 0) {
            return new String(zzkF(zzBy), HTTP.UTF_8);
        }
        String str = new String(this.buffer, this.zzaVN, zzBy, HTTP.UTF_8);
        this.zzaVN = zzBy + this.zzaVN;
        return str;
    }

    public int zzBA() throws IOException {
        return (((zzBF() & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) | ((zzBF() & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8)) | ((zzBF() & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16)) | ((zzBF() & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 24);
    }

    public long zzBB() throws IOException {
        byte zzBF = zzBF();
        byte zzBF2 = zzBF();
        return ((((((((((long) zzBF2) & 255) << 8) | (((long) zzBF) & 255)) | ((((long) zzBF()) & 255) << 16)) | ((((long) zzBF()) & 255) << 24)) | ((((long) zzBF()) & 255) << 32)) | ((((long) zzBF()) & 255) << 40)) | ((((long) zzBF()) & 255) << 48)) | ((((long) zzBF()) & 255) << 56);
    }

    public int zzBD() {
        if (this.zzaVP == ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
            return -1;
        }
        return this.zzaVP - this.zzaVN;
    }

    public boolean zzBE() {
        return this.zzaVN == this.zzaVL;
    }

    public byte zzBF() throws IOException {
        if (this.zzaVN == this.zzaVL) {
            throw zzrm.zzBN();
        }
        byte[] bArr = this.buffer;
        int i = this.zzaVN;
        this.zzaVN = i + 1;
        return bArr[i];
    }

    public int zzBr() throws IOException {
        if (zzBE()) {
            this.zzaVO = 0;
            return 0;
        }
        this.zzaVO = zzBy();
        if (this.zzaVO != 0) {
            return this.zzaVO;
        }
        throw zzrm.zzBQ();
    }

    public void zzBs() throws IOException {
        int zzBr;
        do {
            zzBr = zzBr();
            if (zzBr == 0) {
                return;
            }
        } while (zzkA(zzBr));
    }

    public long zzBt() throws IOException {
        return zzBz();
    }

    public int zzBu() throws IOException {
        return zzBy();
    }

    public boolean zzBv() throws IOException {
        return zzBy() != 0;
    }

    public int zzBw() throws IOException {
        return zzkB(zzBy());
    }

    public long zzBx() throws IOException {
        return zzV(zzBz());
    }

    public int zzBy() throws IOException {
        byte zzBF = zzBF();
        if (zzBF >= (byte) 0) {
            return zzBF;
        }
        int i = zzBF & TransportMediator.KEYCODE_MEDIA_PAUSE;
        byte zzBF2 = zzBF();
        if (zzBF2 >= (byte) 0) {
            return i | (zzBF2 << 7);
        }
        i |= (zzBF2 & TransportMediator.KEYCODE_MEDIA_PAUSE) << 7;
        zzBF2 = zzBF();
        if (zzBF2 >= (byte) 0) {
            return i | (zzBF2 << 14);
        }
        i |= (zzBF2 & TransportMediator.KEYCODE_MEDIA_PAUSE) << 14;
        zzBF2 = zzBF();
        if (zzBF2 >= (byte) 0) {
            return i | (zzBF2 << 21);
        }
        i |= (zzBF2 & TransportMediator.KEYCODE_MEDIA_PAUSE) << 21;
        zzBF2 = zzBF();
        i |= zzBF2 << 28;
        if (zzBF2 >= (byte) 0) {
            return i;
        }
        for (int i2 = 0; i2 < 5; i2++) {
            if (zzBF() >= (byte) 0) {
                return i;
            }
        }
        throw zzrm.zzBP();
    }

    public long zzBz() throws IOException {
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            byte zzBF = zzBF();
            j |= ((long) (zzBF & TransportMediator.KEYCODE_MEDIA_PAUSE)) << i;
            if ((zzBF & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) == 0) {
                return j;
            }
        }
        throw zzrm.zzBP();
    }

    public void zza(zzrn com_google_android_gms_internal_zzrn) throws IOException {
        int zzBy = zzBy();
        if (this.zzaVQ >= this.zzaVR) {
            throw zzrm.zzBT();
        }
        zzBy = zzkC(zzBy);
        this.zzaVQ++;
        com_google_android_gms_internal_zzrn.zzb(this);
        zzkz(0);
        this.zzaVQ--;
        zzkD(zzBy);
    }

    public void zza(zzrn com_google_android_gms_internal_zzrn, int i) throws IOException {
        if (this.zzaVQ >= this.zzaVR) {
            throw zzrm.zzBT();
        }
        this.zzaVQ++;
        com_google_android_gms_internal_zzrn.zzb(this);
        zzkz(zzrq.zzD(i, 4));
        this.zzaVQ--;
    }

    public boolean zzkA(int i) throws IOException {
        switch (zzrq.zzkU(i)) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                zzBu();
                return true;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                zzBB();
                return true;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                zzkG(zzBy());
                return true;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                zzBs();
                zzkz(zzrq.zzD(zzrq.zzkV(i), 4));
                return true;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                return false;
            case Player.STATE_ENDED /*5*/:
                zzBA();
                return true;
            default:
                throw zzrm.zzBS();
        }
    }

    public int zzkC(int i) throws zzrm {
        if (i < 0) {
            throw zzrm.zzBO();
        }
        int i2 = this.zzaVN + i;
        int i3 = this.zzaVP;
        if (i2 > i3) {
            throw zzrm.zzBN();
        }
        this.zzaVP = i2;
        zzBC();
        return i3;
    }

    public void zzkD(int i) {
        this.zzaVP = i;
        zzBC();
    }

    public void zzkE(int i) {
        if (i > this.zzaVN - this.zzaVK) {
            throw new IllegalArgumentException("Position " + i + " is beyond current " + (this.zzaVN - this.zzaVK));
        } else if (i < 0) {
            throw new IllegalArgumentException("Bad position " + i);
        } else {
            this.zzaVN = this.zzaVK + i;
        }
    }

    public byte[] zzkF(int i) throws IOException {
        if (i < 0) {
            throw zzrm.zzBO();
        } else if (this.zzaVN + i > this.zzaVP) {
            zzkG(this.zzaVP - this.zzaVN);
            throw zzrm.zzBN();
        } else if (i <= this.zzaVL - this.zzaVN) {
            Object obj = new byte[i];
            System.arraycopy(this.buffer, this.zzaVN, obj, 0, i);
            this.zzaVN += i;
            return obj;
        } else {
            throw zzrm.zzBN();
        }
    }

    public void zzkG(int i) throws IOException {
        if (i < 0) {
            throw zzrm.zzBO();
        } else if (this.zzaVN + i > this.zzaVP) {
            zzkG(this.zzaVP - this.zzaVN);
            throw zzrm.zzBN();
        } else if (i <= this.zzaVL - this.zzaVN) {
            this.zzaVN += i;
        } else {
            throw zzrm.zzBN();
        }
    }

    public void zzkz(int i) throws zzrm {
        if (this.zzaVO != i) {
            throw zzrm.zzBR();
        }
    }

    public byte[] zzx(int i, int i2) {
        if (i2 == 0) {
            return zzrq.zzaWo;
        }
        Object obj = new byte[i2];
        System.arraycopy(this.buffer, this.zzaVK + i, obj, 0, i2);
        return obj;
    }
}
