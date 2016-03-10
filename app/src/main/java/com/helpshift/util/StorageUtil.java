package com.helpshift.util;

import com.helpshift.HSStorage;
import com.helpshift.HelpshiftContext;

public class StorageUtil {
    public static void clearFAQEtag() {
        clearEtag("/faqs/");
    }

    private static void clearEtag(String route) {
        new HSStorage(HelpshiftContext.getApplicationContext()).setEtag(route, null);
    }
}
