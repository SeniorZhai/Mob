package com.mobcrush.mobcrush.mixpanel;

import com.facebook.internal.AnalyticsEvents;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.logic.BroadcastLogicType;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public enum Source {
    CHANNEL(Constants.SCREEN_CHANNEL),
    DEEPLINK("Deeplink"),
    FEATURED("Featured"),
    GAMES(Constants.SCREEN_GAMES),
    LIKED("Liked"),
    NEW("New"),
    POPULAR("Popular"),
    SEARCH(Constants.SCREEN_SEARCH),
    TOURNAMENTS("Tournaments"),
    UNKNOWN(AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN),
    USER("User");
    
    private final String mName;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType = null;

        static {
            $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType = new int[BroadcastLogicType.values().length];
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Channel.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Game.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.New.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Popular.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.User.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private Source(String propertyName) {
        this.mName = propertyName;
    }

    public String getName() {
        return this.mName;
    }

    public static Source fromBroadcastLogicType(BroadcastLogicType logicType) {
        switch (AnonymousClass1.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[logicType.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return CHANNEL;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return GAMES;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return NEW;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                return POPULAR;
            case Player.STATE_ENDED /*5*/:
                return USER;
            default:
                return UNKNOWN;
        }
    }
}
