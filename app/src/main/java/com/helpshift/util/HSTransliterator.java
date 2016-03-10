package com.helpshift.util;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import org.apache.http.HttpStatus;

public class HSTransliterator {
    private static final String TAG = "HelpShiftDebug";
    private static boolean initDone = false;
    static HSCharacters1 trans1;
    static HSCharacters10 trans10;
    static HSCharacters11 trans11;
    static HSCharacters12 trans12;
    static HSCharacters13 trans13;
    static HSCharacters14 trans14;
    static HSCharacters15 trans15;
    static HSCharacters16 trans16;
    static HSCharacters2 trans2;
    static HSCharacters3 trans3;
    static HSCharacters4 trans4;
    static HSCharacters5 trans5;
    static HSCharacters6 trans6;
    static HSCharacters7 trans7;
    static HSCharacters8 trans8;
    static HSCharacters9 trans9;

    public static boolean isLoaded() {
        return initDone;
    }

    public static void init() {
        if (!initDone) {
            trans1 = new HSCharacters1();
            trans2 = new HSCharacters2();
            trans3 = new HSCharacters3();
            trans4 = new HSCharacters4();
            trans5 = new HSCharacters5();
            trans6 = new HSCharacters6();
            trans7 = new HSCharacters7();
            trans8 = new HSCharacters8();
            trans9 = new HSCharacters9();
            trans10 = new HSCharacters10();
            trans11 = new HSCharacters11();
            trans12 = new HSCharacters12();
            trans13 = new HSCharacters13();
            trans14 = new HSCharacters14();
            trans15 = new HSCharacters15();
            trans16 = new HSCharacters16();
            initDone = true;
        }
    }

    public static void deinit() {
        trans1 = null;
        trans2 = null;
        trans3 = null;
        trans4 = null;
        trans5 = null;
        trans6 = null;
        trans7 = null;
        trans8 = null;
        trans9 = null;
        trans10 = null;
        trans11 = null;
        trans12 = null;
        trans13 = null;
        trans14 = null;
        trans15 = null;
        trans16 = null;
        initDone = false;
    }

    public static String unidecode(String input) {
        if (input == null || input.length() == 0) {
            return BuildConfig.FLAVOR;
        }
        int i = 0;
        while (i < input.length() && input.charAt(i) <= '\u0080') {
            if (i >= input.length()) {
                return input;
            }
            i++;
        }
        char[] inputArray = input.toCharArray();
        StringBuilder output = new StringBuilder();
        for (char c : inputArray) {
            if (c < '\u0080') {
                output.append(c);
            } else {
                int high = c >> 8;
                int low = c & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                if (high > 0 && high < 17 && trans1.containsKey(high, low)) {
                    output.append(trans1.get(high, low));
                } else if (high >= 17 && high < 37 && trans2.containsKey(high, low)) {
                    output.append(trans2.get(high, low));
                } else if (high >= 37 && high < 51 && trans3.containsKey(high, low)) {
                    output.append(trans3.get(high, low));
                } else if (high >= 51 && high < 85 && trans4.containsKey(high, low)) {
                    output.append(trans4.get(high, low));
                } else if (high >= 85 && high < 98 && trans5.containsKey(high, low)) {
                    output.append(trans5.get(high, low));
                } else if (high >= 98 && high < 111 && trans6.containsKey(high, low)) {
                    output.append(trans6.get(high, low));
                } else if (high >= 111 && high < 121 && trans7.containsKey(high, low)) {
                    output.append(trans7.get(high, low));
                } else if (high >= 121 && high < 131 && trans8.containsKey(high, low)) {
                    output.append(trans8.get(high, low));
                } else if (high >= 131 && high < 141 && trans9.containsKey(high, low)) {
                    output.append(trans9.get(high, low));
                } else if (high >= 141 && high < 151 && trans10.containsKey(high, low)) {
                    output.append(trans10.get(high, low));
                } else if (high >= 151 && high < 163 && trans11.containsKey(high, low)) {
                    output.append(trans11.get(high, low));
                } else if (high >= 163 && high < 182 && trans12.containsKey(high, low)) {
                    output.append(trans12.get(high, low));
                } else if (high >= 182 && high < 192 && trans13.containsKey(high, low)) {
                    output.append(trans13.get(high, low));
                } else if (high >= 192 && high < HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION && trans14.containsKey(high, low)) {
                    output.append(trans14.get(high, low));
                } else if (high >= HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION && high < 215 && trans15.containsKey(high, low)) {
                    output.append(trans15.get(high, low));
                } else if (high < 215 || !trans15.containsKey(high, low)) {
                    output.append(BuildConfig.FLAVOR);
                } else {
                    output.append(trans15.get(high, low));
                }
            }
        }
        return output.toString();
    }
}
