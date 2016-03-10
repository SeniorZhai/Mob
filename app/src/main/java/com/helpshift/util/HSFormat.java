package com.helpshift.util;

import android.util.Log;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class HSFormat {
    private static final String TAG = "HelpShiftDebug";
    public static final SimpleDateFormat breadCrumbTsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat deviceInfoTsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat inputMsgFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat issueTsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final SimpleDateFormat outputMsgFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    public static final DecimalFormat tsSecFormatter = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));

    static {
        issueTsFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        inputMsgFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String addMilliSeconds(SimpleDateFormat dateFormat, String inputDate, int milliSeconds) {
        try {
            Date date = dateFormat.parse(inputDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            inputDate = inputMsgFormatter.format(new Date(calendar.getTimeInMillis() + ((long) milliSeconds)));
        } catch (ParseException e) {
            Log.d(TAG, "addMilliSeconds : ", e);
        }
        return inputDate;
    }
}
