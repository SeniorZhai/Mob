package com.helpshift.res.values;

import io.fabric.sdk.android.BuildConfig;

public final class HSConsts {
    public static final String ADMIN_ATTACHMENT_GENERIC_TYPE = "admin_attachment_generic";
    public static final String ADMIN_ATTACHMENT_IMAGE_TYPE = "admin_attachment_image";
    public static final String ISSUE_FILING = "issue-filing";
    public static final String MESSAGE_FILING = "message-filing";
    public static final String QUESTION_FLOW = "questionFlow";
    public static final int REASON_ISSUE_FILING = 1;
    public static final int REASON_MESSAGE_FILING = 3;
    public static final int REASON_OPEN_ISSUE = 2;
    public static final String SEARCH_PERFORMED = "search_performed";
    public static final String SEARCH_QUERY = "searchQuery";
    public static final String SHOW_SEARCH_ON_NEW_CONVERSATION = "showSearchOnNewConversation";
    public static final boolean SHOW_SEARCH_ON_NEW_CONVERSATION_DEFAULT = false;
    public static final String SHOW_SEARCH_ON_NEW_CONVERSATION_FLOW = "showSearchOnNewConversationFlow";
    public static final int SHOW_SEARCH_ON_NEW_CONVERSATION_REQUEST_CODE = 32699;
    public static final String SRC_INAPP = "inapp";
    public static final String SRC_INBOX = "inbox";
    public static final String SRC_PUSH = "push";
    public static final String SRC_SUPPORT = "support";
    public static final String STATUS_INPROGRESS = "1";
    public static final String STATUS_NEW = "0";
    public static final String STATUS_REJECTED = "3";
    public static final String STATUS_RESOLVED = "2";
    public static final String[] invalidLogins;

    static {
        String[] strArr = new String[REASON_MESSAGE_FILING];
        strArr[0] = null;
        strArr[REASON_ISSUE_FILING] = BuildConfig.FLAVOR;
        strArr[REASON_OPEN_ISSUE] = "null";
        invalidLogins = strArr;
    }
}
