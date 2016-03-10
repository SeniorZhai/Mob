package com.helpshift.util;

import java.util.regex.Pattern;

public final class HSPattern {
    private static Pattern emailPattern = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
    private static Pattern specialCharPattern = Pattern.compile("\\W+");

    public static boolean checkEmail(String email) {
        return emailPattern.matcher(email.trim()).matches();
    }

    public static boolean checkSpecialCharacters(String issueText) {
        return specialCharPattern.matcher(issueText.trim()).matches();
    }
}
