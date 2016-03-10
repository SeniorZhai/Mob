package com.helpshift.util;

import android.net.ParseException;
import android.util.Log;
import java.util.Date;

public final class HSTimeUtil {
    private static final String TAG = "HelpShiftDebug";

    public static Float calculateTimeAdjustment(String serverTime) {
        Float timeDelta = new Float(0.0f);
        try {
            timeDelta = Float.valueOf((float) (((double) (new Date((long) Double.valueOf(Double.parseDouble(serverTime) * 1000.0d).doubleValue()).getTime() / 1000)) - Double.parseDouble(HSFormat.tsSecFormatter.format(((double) System.currentTimeMillis()) / 1000.0d))));
        } catch (ParseException e) {
            Log.d(TAG, "Could not parse the server date");
        }
        return timeDelta;
    }

    public static String getAdjustedTimestamp(Float timeDelta) {
        String deviceTs = HSFormat.tsSecFormatter.format(((double) System.currentTimeMillis()) / 1000.0d);
        if (timeDelta.floatValue() == 0.0f) {
            return deviceTs;
        }
        return HSFormat.tsSecFormatter.format(Double.valueOf(((double) System.currentTimeMillis()) / 1000.0d).doubleValue() + ((double) timeDelta.floatValue()));
    }

    public static long getAdjustedTimeInMillis(Float timeDelta) {
        long deviceTs = System.currentTimeMillis();
        if (timeDelta.floatValue() != 0.0f) {
            return (long) (((float) deviceTs) + (timeDelta.floatValue() * 1000.0f));
        }
        return deviceTs;
    }
}
