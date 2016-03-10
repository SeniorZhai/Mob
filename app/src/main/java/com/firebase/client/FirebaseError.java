package com.firebase.client;

import io.fabric.sdk.android.BuildConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FirebaseError {
    public static final int AUTHENTICATION_PROVIDER_DISABLED = -12;
    public static final int DATA_STALE = -1;
    public static final int DENIED_BY_USER = -19;
    public static final int DISCONNECTED = -4;
    public static final int EMAIL_TAKEN = -18;
    public static final int EXPIRED_TOKEN = -6;
    public static final int INVALID_AUTH_ARGUMENTS = -21;
    public static final int INVALID_CONFIGURATION = -13;
    public static final int INVALID_CREDENTIALS = -20;
    public static final int INVALID_EMAIL = -15;
    public static final int INVALID_PASSWORD = -16;
    public static final int INVALID_PROVIDER = -14;
    public static final int INVALID_TOKEN = -7;
    public static final int LIMITS_EXCEEDED = -23;
    public static final int MAX_RETRIES = -8;
    public static final int NETWORK_ERROR = -24;
    public static final int OPERATION_FAILED = -2;
    public static final int OVERRIDDEN_BY_SET = -9;
    public static final int PERMISSION_DENIED = -3;
    public static final int PREEMPTED = -5;
    public static final int PROVIDER_ERROR = -22;
    public static final int UNAVAILABLE = -10;
    public static final int UNKNOWN_ERROR = -999;
    public static final int USER_CODE_EXCEPTION = -11;
    public static final int USER_DOES_NOT_EXIST = -17;
    public static final int WRITE_CANCELED = -25;
    private static final Map<String, Integer> errorCodes = new HashMap();
    private static final Map<Integer, String> errorReasons = new HashMap();
    private final int code;
    private final String details;
    private final String message;

    static {
        errorReasons.put(Integer.valueOf(DATA_STALE), "The transaction needs to be run again with current data");
        errorReasons.put(Integer.valueOf(OPERATION_FAILED), "The server indicated that this operation failed");
        errorReasons.put(Integer.valueOf(PERMISSION_DENIED), "This client does not have permission to perform this operation");
        errorReasons.put(Integer.valueOf(DISCONNECTED), "The operation had to be aborted due to a network disconnect");
        errorReasons.put(Integer.valueOf(PREEMPTED), "The active or pending auth credentials were superseded by another call to auth");
        errorReasons.put(Integer.valueOf(EXPIRED_TOKEN), "The supplied auth token has expired");
        errorReasons.put(Integer.valueOf(INVALID_TOKEN), "The supplied auth token was invalid");
        errorReasons.put(Integer.valueOf(MAX_RETRIES), "The transaction had too many retries");
        errorReasons.put(Integer.valueOf(OVERRIDDEN_BY_SET), "The transaction was overridden by a subsequent set");
        errorReasons.put(Integer.valueOf(UNAVAILABLE), "The service is unavailable");
        errorReasons.put(Integer.valueOf(USER_CODE_EXCEPTION), "User code called from the Firebase runloop threw an exception:\n");
        errorReasons.put(Integer.valueOf(AUTHENTICATION_PROVIDER_DISABLED), "The specified authentication type is not enabled for this Firebase.");
        errorReasons.put(Integer.valueOf(INVALID_CONFIGURATION), "The specified authentication type is not properly configured for this Firebase.");
        errorReasons.put(Integer.valueOf(INVALID_PROVIDER), "Invalid provider specified, please check application code.");
        errorReasons.put(Integer.valueOf(INVALID_EMAIL), "The specified email address is incorrect.");
        errorReasons.put(Integer.valueOf(INVALID_PASSWORD), "The specified password is incorrect.");
        errorReasons.put(Integer.valueOf(USER_DOES_NOT_EXIST), "The specified user does not exist.");
        errorReasons.put(Integer.valueOf(EMAIL_TAKEN), "The specified email address is already in use.");
        errorReasons.put(Integer.valueOf(DENIED_BY_USER), "User denied authentication request.");
        errorReasons.put(Integer.valueOf(INVALID_CREDENTIALS), "Invalid authentication credentials provided.");
        errorReasons.put(Integer.valueOf(INVALID_AUTH_ARGUMENTS), "Invalid authentication arguments provided.");
        errorReasons.put(Integer.valueOf(PROVIDER_ERROR), "A third-party provider error occurred. See data for details.");
        errorReasons.put(Integer.valueOf(LIMITS_EXCEEDED), "Limited exceeded. Please contact support@firebase.com.");
        errorReasons.put(Integer.valueOf(NETWORK_ERROR), "The operation could not be performed due to a network error");
        errorReasons.put(Integer.valueOf(WRITE_CANCELED), "The write was canceled by the user.");
        errorReasons.put(Integer.valueOf(UNKNOWN_ERROR), "An unknown error ocurred");
        errorCodes.put("datastale", Integer.valueOf(DATA_STALE));
        errorCodes.put("failure", Integer.valueOf(OPERATION_FAILED));
        errorCodes.put("permission_denied", Integer.valueOf(PERMISSION_DENIED));
        errorCodes.put("disconnected", Integer.valueOf(DISCONNECTED));
        errorCodes.put("preempted", Integer.valueOf(PREEMPTED));
        errorCodes.put("expired_token", Integer.valueOf(EXPIRED_TOKEN));
        errorCodes.put("invalid_token", Integer.valueOf(INVALID_TOKEN));
        errorCodes.put("maxretries", Integer.valueOf(MAX_RETRIES));
        errorCodes.put("overriddenbyset", Integer.valueOf(OVERRIDDEN_BY_SET));
        errorCodes.put("unavailable", Integer.valueOf(UNAVAILABLE));
        errorCodes.put("authentication_disabled", Integer.valueOf(AUTHENTICATION_PROVIDER_DISABLED));
        errorCodes.put("invalid_configuration", Integer.valueOf(INVALID_CONFIGURATION));
        errorCodes.put("invalid_provider", Integer.valueOf(INVALID_PROVIDER));
        errorCodes.put("invalid_email", Integer.valueOf(INVALID_EMAIL));
        errorCodes.put("invalid_password", Integer.valueOf(INVALID_PASSWORD));
        errorCodes.put("invalid_user", Integer.valueOf(USER_DOES_NOT_EXIST));
        errorCodes.put("email_taken", Integer.valueOf(EMAIL_TAKEN));
        errorCodes.put("user_denied", Integer.valueOf(DENIED_BY_USER));
        errorCodes.put("invalid_credentials", Integer.valueOf(INVALID_CREDENTIALS));
        errorCodes.put("invalid_arguments", Integer.valueOf(INVALID_AUTH_ARGUMENTS));
        errorCodes.put("provider_error", Integer.valueOf(PROVIDER_ERROR));
        errorCodes.put("limits_exceeded", Integer.valueOf(LIMITS_EXCEEDED));
        errorCodes.put("network_error", Integer.valueOf(NETWORK_ERROR));
        errorCodes.put("write_canceled", Integer.valueOf(WRITE_CANCELED));
    }

    public static FirebaseError fromStatus(String status) {
        return fromStatus(status, null);
    }

    public static FirebaseError fromStatus(String status, String reason) {
        return fromStatus(status, reason, null);
    }

    public static FirebaseError fromCode(int code) {
        if (errorReasons.containsKey(Integer.valueOf(code))) {
            return new FirebaseError(code, (String) errorReasons.get(Integer.valueOf(code)), null);
        }
        throw new IllegalArgumentException("Invalid Firebase error code: " + code);
    }

    public static FirebaseError fromStatus(String status, String reason, String details) {
        String message;
        Integer code = (Integer) errorCodes.get(status.toLowerCase());
        if (code == null) {
            code = Integer.valueOf(UNKNOWN_ERROR);
        }
        if (reason == null) {
            message = (String) errorReasons.get(code);
        } else {
            message = reason;
        }
        return new FirebaseError(code.intValue(), message, details);
    }

    public static FirebaseError fromException(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return new FirebaseError(USER_CODE_EXCEPTION, ((String) errorReasons.get(Integer.valueOf(USER_CODE_EXCEPTION))) + stringWriter.toString());
    }

    public FirebaseError(int code, String message) {
        this(code, message, null);
    }

    public FirebaseError(int code, String message, String details) {
        this.code = code;
        this.message = message;
        if (details == null) {
            details = BuildConfig.FLAVOR;
        }
        this.details = details;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getDetails() {
        return this.details;
    }

    public String toString() {
        return "FirebaseError: " + this.message;
    }

    public FirebaseException toException() {
        return new FirebaseException("Firebase error: " + this.message);
    }
}
