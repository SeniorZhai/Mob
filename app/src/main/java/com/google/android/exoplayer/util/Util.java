package com.google.android.exoplayer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import com.google.android.exoplayer.ExoPlayerLibraryInfo;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import io.fabric.sdk.android.services.network.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {
    public static final String DEVICE = Build.DEVICE;
    public static final String MANUFACTURER = Build.MANUFACTURER;
    private static final long MAX_BYTES_TO_DRAIN = 2048;
    public static final int SDK_INT = VERSION.SDK_INT;
    private static final Pattern XS_DATE_TIME_PATTERN = Pattern.compile("(\\d\\d\\d\\d)\\-(\\d\\d)\\-(\\d\\d)[Tt](\\d\\d):(\\d\\d):(\\d\\d)(\\.(\\d+))?([Zz]|((\\+|\\-)(\\d\\d):(\\d\\d)))?");
    private static final Pattern XS_DURATION_PATTERN = Pattern.compile("^(-)?P(([0-9]*)Y)?(([0-9]*)M)?(([0-9]*)D)?(T(([0-9]*)H)?(([0-9]*)M)?(([0-9.]*)S)?)?$");

    private Util() {
    }

    @SuppressLint({"InlinedApi"})
    public static boolean isAndroidTv(Context context) {
        return context.getPackageManager().hasSystemFeature("android.software.leanback");
    }

    public static boolean isUrlLocalFile(URL url) {
        return url.getProtocol().equals("file");
    }

    public static boolean areEqual(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    public static boolean contains(Object[] items, Object item) {
        for (Object areEqual : items) {
            if (areEqual(areEqual, item)) {
                return true;
            }
        }
        return false;
    }

    public static ExecutorService newSingleThreadExecutor(final String threadName) {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, threadName);
            }
        });
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(final String threadName) {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, threadName);
            }
        });
    }

    public static void closeQuietly(DataSource dataSource) {
        try {
            dataSource.close();
        } catch (IOException e) {
        }
    }

    public static void closeQuietly(OutputStream outputStream) {
        try {
            outputStream.close();
        } catch (IOException e) {
        }
    }

    public static String toLowerInvariant(String text) {
        return text == null ? null : text.toLowerCase(Locale.US);
    }

    public static int ceilDivide(int numerator, int denominator) {
        return ((numerator + denominator) - 1) / denominator;
    }

    public static long ceilDivide(long numerator, long denominator) {
        return ((numerator + denominator) - 1) / denominator;
    }

    public static int binarySearchFloor(long[] a, long key, boolean inclusive, boolean stayInBounds) {
        int index = Arrays.binarySearch(a, key);
        if (index < 0) {
            index = -(index + 2);
        } else if (!inclusive) {
            index--;
        }
        return stayInBounds ? Math.max(0, index) : index;
    }

    public static int binarySearchCeil(long[] a, long key, boolean inclusive, boolean stayInBounds) {
        int index = Arrays.binarySearch(a, key);
        if (index < 0) {
            index ^= -1;
        } else if (!inclusive) {
            index++;
        }
        return stayInBounds ? Math.min(a.length - 1, index) : index;
    }

    public static <T> int binarySearchFloor(List<? extends Comparable<? super T>> list, T key, boolean inclusive, boolean stayInBounds) {
        int index = Collections.binarySearch(list, key);
        if (index < 0) {
            index = -(index + 2);
        } else if (!inclusive) {
            index--;
        }
        return stayInBounds ? Math.max(0, index) : index;
    }

    public static <T> int binarySearchCeil(List<? extends Comparable<? super T>> list, T key, boolean inclusive, boolean stayInBounds) {
        int index = Collections.binarySearch(list, key);
        if (index < 0) {
            index ^= -1;
        } else if (!inclusive) {
            index++;
        }
        return stayInBounds ? Math.min(list.size() - 1, index) : index;
    }

    public static long parseXsDuration(String value) {
        Matcher matcher = XS_DURATION_PATTERN.matcher(value);
        if (!matcher.matches()) {
            return (long) ((Double.parseDouble(value) * 3600.0d) * 1000.0d);
        }
        boolean negated = !TextUtils.isEmpty(matcher.group(1));
        String years = matcher.group(3);
        double durationSeconds = years != null ? Double.parseDouble(years) * 3.1556908E7d : 0.0d;
        String months = matcher.group(5);
        durationSeconds += months != null ? Double.parseDouble(months) * 2629739.0d : 0.0d;
        String days = matcher.group(7);
        durationSeconds += days != null ? Double.parseDouble(days) * 86400.0d : 0.0d;
        String hours = matcher.group(10);
        durationSeconds += hours != null ? Double.parseDouble(hours) * 3600.0d : 0.0d;
        String minutes = matcher.group(12);
        durationSeconds += minutes != null ? Double.parseDouble(minutes) * 60.0d : 0.0d;
        String seconds = matcher.group(14);
        long durationMillis = (long) (1000.0d * (durationSeconds + (seconds != null ? Double.parseDouble(seconds) : 0.0d)));
        if (negated) {
            return -durationMillis;
        }
        return durationMillis;
    }

    public static long parseXsDateTime(String value) throws ParseException {
        Matcher matcher = XS_DATE_TIME_PATTERN.matcher(value);
        if (matcher.matches()) {
            int timezoneShift;
            if (matcher.group(9) == null) {
                timezoneShift = 0;
            } else if (matcher.group(9).equalsIgnoreCase("Z")) {
                timezoneShift = 0;
            } else {
                timezoneShift = (Integer.parseInt(matcher.group(12)) * 60) + Integer.parseInt(matcher.group(13));
                if (matcher.group(11).equals("-")) {
                    timezoneShift *= -1;
                }
            }
            Calendar dateTime = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            dateTime.clear();
            dateTime.set(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1, Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)), Integer.parseInt(matcher.group(6)));
            if (!TextUtils.isEmpty(matcher.group(8))) {
                dateTime.set(14, new BigDecimal("0." + matcher.group(8)).movePointRight(3).intValue());
            }
            long time = dateTime.getTimeInMillis();
            if (timezoneShift != 0) {
                return time - ((long) (60000 * timezoneShift));
            }
            return time;
        }
        throw new ParseException("Invalid date/time format: " + value, 0);
    }

    public static long scaleLargeTimestamp(long timestamp, long multiplier, long divisor) {
        if (divisor >= multiplier && divisor % multiplier == 0) {
            return timestamp / (divisor / multiplier);
        }
        if (divisor < multiplier && multiplier % divisor == 0) {
            return timestamp * (multiplier / divisor);
        }
        return (long) (((double) timestamp) * (((double) multiplier) / ((double) divisor)));
    }

    public static long[] scaleLargeTimestamps(List<Long> timestamps, long multiplier, long divisor) {
        long[] scaledTimestamps = new long[timestamps.size()];
        int i;
        if (divisor >= multiplier && divisor % multiplier == 0) {
            long divisionFactor = divisor / multiplier;
            for (i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = ((Long) timestamps.get(i)).longValue() / divisionFactor;
            }
        } else if (divisor >= multiplier || multiplier % divisor != 0) {
            double multiplicationFactor = ((double) multiplier) / ((double) divisor);
            for (i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = (long) (((double) ((Long) timestamps.get(i)).longValue()) * multiplicationFactor);
            }
        } else {
            long multiplicationFactor2 = multiplier / divisor;
            for (i = 0; i < scaledTimestamps.length; i++) {
                scaledTimestamps[i] = ((Long) timestamps.get(i)).longValue() * multiplicationFactor2;
            }
        }
        return scaledTimestamps;
    }

    public static void scaleLargeTimestampsInPlace(long[] timestamps, long multiplier, long divisor) {
        int i;
        if (divisor >= multiplier && divisor % multiplier == 0) {
            long divisionFactor = divisor / multiplier;
            for (i = 0; i < timestamps.length; i++) {
                timestamps[i] = timestamps[i] / divisionFactor;
            }
        } else if (divisor >= multiplier || multiplier % divisor != 0) {
            double multiplicationFactor = ((double) multiplier) / ((double) divisor);
            for (i = 0; i < timestamps.length; i++) {
                timestamps[i] = (long) (((double) timestamps[i]) * multiplicationFactor);
            }
        } else {
            long multiplicationFactor2 = multiplier / divisor;
            for (i = 0; i < timestamps.length; i++) {
                timestamps[i] = timestamps[i] * multiplicationFactor2;
            }
        }
    }

    public static int[] toArray(List<Integer> list) {
        if (list == null) {
            return null;
        }
        int length = list.size();
        int[] intArray = new int[length];
        for (int i = 0; i < length; i++) {
            intArray[i] = ((Integer) list.get(i)).intValue();
        }
        return intArray;
    }

    public static void maybeTerminateInputStream(HttpURLConnection connection, long bytesRemaining) {
        if (SDK_INT == 19 || SDK_INT == 20) {
            try {
                InputStream inputStream = connection.getInputStream();
                if (bytesRemaining == -1) {
                    if (inputStream.read() == -1) {
                        return;
                    }
                } else if (bytesRemaining <= MAX_BYTES_TO_DRAIN) {
                    return;
                }
                String className = inputStream.getClass().getName();
                if (className.equals("com.android.okhttp.internal.http.HttpTransport$ChunkedInputStream") || className.equals("com.android.okhttp.internal.http.HttpTransport$FixedLengthInputStream")) {
                    Method unexpectedEndOfInput = inputStream.getClass().getSuperclass().getDeclaredMethod("unexpectedEndOfInput", new Class[0]);
                    unexpectedEndOfInput.setAccessible(true);
                    unexpectedEndOfInput.invoke(inputStream, new Object[0]);
                }
            } catch (IOException e) {
            } catch (Exception e2) {
            }
        }
    }

    public static DataSpec getRemainderDataSpec(DataSpec dataSpec, int bytesLoaded) {
        long remainingLength = -1;
        if (bytesLoaded == 0) {
            return dataSpec;
        }
        if (dataSpec.length != -1) {
            remainingLength = dataSpec.length - ((long) bytesLoaded);
        }
        return new DataSpec(dataSpec.uri, dataSpec.position + ((long) bytesLoaded), remainingLength, dataSpec.key, dataSpec.flags);
    }

    public static int getIntegerCodeForString(String string) {
        int length = string.length();
        Assertions.checkArgument(length <= 4);
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | string.charAt(i);
        }
        return result;
    }

    public static String getHexStringFromBytes(byte[] data, int beginIndex, int endIndex) {
        StringBuffer dataStringBuffer = new StringBuffer(endIndex - beginIndex);
        for (int i = beginIndex; i < endIndex; i++) {
            dataStringBuffer.append(String.format(Locale.US, "%02X", new Object[]{Byte.valueOf(data[i])}));
        }
        return dataStringBuffer.toString();
    }

    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + VERSION.RELEASE + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
    }

    public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties) throws IOException {
        OutputStream out;
        HttpURLConnection urlConnection = null;
        InputStream inputStream;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod(HttpRequest.METHOD_POST);
            urlConnection.setDoOutput(data != null);
            urlConnection.setDoInput(true);
            if (requestProperties != null) {
                for (Entry<String, String> requestProperty : requestProperties.entrySet()) {
                    urlConnection.setRequestProperty((String) requestProperty.getKey(), (String) requestProperty.getValue());
                }
            }
            if (data != null) {
                out = urlConnection.getOutputStream();
                out.write(data);
                out.close();
            }
            inputStream = urlConnection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] scratch = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            while (true) {
                int bytesRead = inputStream.read(scratch);
                if (bytesRead == -1) {
                    break;
                }
                byteArrayOutputStream.write(scratch, 0, bytesRead);
            }
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            inputStream.close();
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return toByteArray;
        } catch (Throwable th) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
