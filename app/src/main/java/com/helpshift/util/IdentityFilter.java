package com.helpshift.util;

import android.text.TextUtils;
import com.helpshift.HSApiData;
import com.helpshift.HSStorage;
import com.helpshift.res.values.HSConfig;

public final class IdentityFilter {
    private static final String TAG = "HelpShiftDebug";

    public static boolean sendNameEmail(HSStorage hsStorage) {
        if (((Boolean) HSConfig.configData.get("pfe")).booleanValue()) {
            return true;
        }
        return false;
    }

    public static boolean showNameEmailForm(HSApiData data) {
        if (!data.storage.getEnableFullPrivacy().booleanValue()) {
            String name = data.getUsername();
            String email = data.getEmail();
            Boolean hideNameAndEmail = data.storage.getHideNameAndEmail();
            if (((Boolean) HSConfig.configData.get("rne")).booleanValue()) {
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                    return true;
                }
                return !hideNameAndEmail.booleanValue();
            } else if (!((Boolean) HSConfig.configData.get("pfe")).booleanValue()) {
                return false;
            } else {
                if (!hideNameAndEmail.booleanValue()) {
                    return true;
                }
                if ((data.storage.getRequireEmail().booleanValue() && TextUtils.isEmpty(email)) || TextUtils.isEmpty(name)) {
                    return true;
                }
                return false;
            }
        } else if (((Boolean) HSConfig.configData.get("pfe")).booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean requireEmailFromUI(HSStorage hsStorage) {
        if (hsStorage.getEnableFullPrivacy().booleanValue() || (!((Boolean) HSConfig.configData.get("rne")).booleanValue() && (!((Boolean) HSConfig.configData.get("pfe")).booleanValue() || !hsStorage.getRequireEmail().booleanValue()))) {
            return false;
        }
        return true;
    }
}
