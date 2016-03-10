package com.helpshift;

import android.content.Context;
import android.text.TextUtils;
import com.helpshift.Helpshift.ENABLE_CONTACT_US;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashMap;

public final class ContactUsFilter {
    private static HSApiData data;
    private static ENABLE_CONTACT_US enableContactUs = ENABLE_CONTACT_US.ALWAYS;
    private static HSStorage storage;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$helpshift$ContactUsFilter$LOCATION = new int[LOCATION.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$helpshift$Helpshift$ENABLE_CONTACT_US = new int[ENABLE_CONTACT_US.values().length];

        static {
            try {
                $SwitchMap$com$helpshift$Helpshift$ENABLE_CONTACT_US[ENABLE_CONTACT_US.ALWAYS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$helpshift$Helpshift$ENABLE_CONTACT_US[ENABLE_CONTACT_US.NEVER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$helpshift$Helpshift$ENABLE_CONTACT_US[ENABLE_CONTACT_US.AFTER_VIEWING_FAQS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$helpshift$ContactUsFilter$LOCATION[LOCATION.SEARCH_RESULT_ACTIVITY_HEADER.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$helpshift$ContactUsFilter$LOCATION[LOCATION.SEARCH_FOOTER.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$helpshift$ContactUsFilter$LOCATION[LOCATION.QUESTION_FOOTER.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$helpshift$ContactUsFilter$LOCATION[LOCATION.QUESTION_ACTION_BAR.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$helpshift$ContactUsFilter$LOCATION[LOCATION.ACTION_BAR.ordinal()] = 5;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    protected enum LOCATION {
        ACTION_BAR,
        SEARCH_FOOTER,
        QUESTION_FOOTER,
        QUESTION_ACTION_BAR,
        SEARCH_RESULT_ACTIVITY_HEADER
    }

    private ContactUsFilter() {
    }

    public static void init(Context context) {
        if (data == null) {
            data = new HSApiData(context);
            storage = data.storage;
        }
    }

    protected static void setConfig(HashMap configMap) {
        if (configMap == null) {
            configMap = new HashMap();
        }
        Object enableContactUsObj = configMap.get("enableContactUs");
        if (enableContactUsObj instanceof ENABLE_CONTACT_US) {
            enableContactUs = (ENABLE_CONTACT_US) configMap.get("enableContactUs");
        } else if (!(enableContactUsObj instanceof Boolean)) {
        } else {
            if (((Boolean) enableContactUsObj).booleanValue()) {
                enableContactUs = ENABLE_CONTACT_US.ALWAYS;
            } else {
                enableContactUs = ENABLE_CONTACT_US.NEVER;
            }
        }
    }

    protected static boolean showContactUs(LOCATION location) {
        switch (AnonymousClass1.$SwitchMap$com$helpshift$ContactUsFilter$LOCATION[location.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return false;
            default:
                switch (AnonymousClass1.$SwitchMap$com$helpshift$Helpshift$ENABLE_CONTACT_US[enableContactUs.ordinal()]) {
                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        return true;
                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                        return false;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        switch (AnonymousClass1.$SwitchMap$com$helpshift$ContactUsFilter$LOCATION[location.ordinal()]) {
                            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                                return true;
                            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                                return true;
                            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                                return true;
                            case Player.STATE_ENDED /*5*/:
                                String activeConversation = storage.getActiveConversation(data.getProfileId());
                                String archivedConversation = storage.getArchivedConversation(data.getProfileId());
                                if (TextUtils.isEmpty(activeConversation) && TextUtils.isEmpty(archivedConversation)) {
                                    return false;
                                }
                                return true;
                            default:
                                return true;
                        }
                    default:
                        return true;
                }
        }
    }
}
