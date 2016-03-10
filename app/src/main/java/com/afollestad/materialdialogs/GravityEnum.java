package com.afollestad.materialdialogs;

import android.support.v4.view.GravityCompat;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public enum GravityEnum {
    START,
    CENTER,
    END;
    
    private static final boolean HAS_RTL = false;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$afollestad$materialdialogs$GravityEnum = null;

        static {
            $SwitchMap$com$afollestad$materialdialogs$GravityEnum = new int[GravityEnum.values().length];
            try {
                $SwitchMap$com$afollestad$materialdialogs$GravityEnum[GravityEnum.START.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$afollestad$materialdialogs$GravityEnum[GravityEnum.CENTER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$afollestad$materialdialogs$GravityEnum[GravityEnum.END.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public int getGravityInt() {
        switch (AnonymousClass1.$SwitchMap$com$afollestad$materialdialogs$GravityEnum[ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return HAS_RTL ? GravityCompat.START : 3;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return 1;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return HAS_RTL ? GravityCompat.END : 5;
            default:
                throw new IllegalStateException("Invalid gravity constant");
        }
    }

    public int getTextAlignment() {
        switch (AnonymousClass1.$SwitchMap$com$afollestad$materialdialogs$GravityEnum[ordinal()]) {
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return 4;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return 6;
            default:
                return 5;
        }
    }
}
