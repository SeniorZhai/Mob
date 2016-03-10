package com.facebook.internal;

import android.net.Uri;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.facebook.LoggingBehavior;
import com.facebook.internal.FileLruCache.Limits;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

class UrlRedirectCache {
    private static final String REDIRECT_CONTENT_TAG = (TAG + "_Redirect");
    static final String TAG = UrlRedirectCache.class.getSimpleName();
    private static volatile FileLruCache urlRedirectCache;

    UrlRedirectCache() {
    }

    static synchronized FileLruCache getCache() throws IOException {
        FileLruCache fileLruCache;
        synchronized (UrlRedirectCache.class) {
            if (urlRedirectCache == null) {
                urlRedirectCache = new FileLruCache(TAG, new Limits());
            }
            fileLruCache = urlRedirectCache;
        }
        return fileLruCache;
    }

    static Uri getRedirectedUri(Uri uri) {
        InputStreamReader inputStreamReader;
        Throwable th;
        Uri uri2 = null;
        if (uri != null) {
            String uriString = uri.toString();
            inputStreamReader = null;
            try {
                FileLruCache cache = getCache();
                boolean redirectExists = false;
                InputStreamReader reader = null;
                while (true) {
                    try {
                        InputStream stream = cache.get(uriString, REDIRECT_CONTENT_TAG);
                        if (stream == null) {
                            break;
                        }
                        redirectExists = true;
                        inputStreamReader = new InputStreamReader(stream);
                        char[] buffer = new char[AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS];
                        StringBuilder urlBuilder = new StringBuilder();
                        while (true) {
                            int bufferLength = inputStreamReader.read(buffer, 0, buffer.length);
                            if (bufferLength <= 0) {
                                break;
                            }
                            urlBuilder.append(buffer, 0, bufferLength);
                        }
                        Utility.closeQuietly(inputStreamReader);
                        uriString = urlBuilder.toString();
                        reader = inputStreamReader;
                    } catch (IOException e) {
                        inputStreamReader = reader;
                    } catch (Throwable th2) {
                        th = th2;
                        inputStreamReader = reader;
                    }
                }
                if (redirectExists) {
                    uri2 = Uri.parse(uriString);
                    Utility.closeQuietly(reader);
                } else {
                    Utility.closeQuietly(reader);
                    inputStreamReader = reader;
                }
            } catch (IOException e2) {
            } catch (Throwable th3) {
                th = th3;
            }
        }
        return uri2;
        Utility.closeQuietly(inputStreamReader);
        throw th;
        Utility.closeQuietly(inputStreamReader);
        return uri2;
    }

    static void cacheUriRedirect(Uri fromUri, Uri toUri) {
        if (fromUri != null && toUri != null) {
            OutputStream redirectStream = null;
            try {
                redirectStream = getCache().openPutStream(fromUri.toString(), REDIRECT_CONTENT_TAG);
                redirectStream.write(toUri.toString().getBytes());
            } catch (IOException e) {
            } finally {
                Utility.closeQuietly(redirectStream);
            }
        }
    }

    static void clearCache() {
        try {
            getCache().clearCache();
        } catch (IOException e) {
            Logger.log(LoggingBehavior.CACHE, 5, TAG, "clearCache failed " + e.getMessage());
        }
    }
}
