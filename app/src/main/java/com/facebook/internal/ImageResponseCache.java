package com.facebook.internal;

import android.content.Context;
import android.net.Uri;
import com.facebook.LoggingBehavior;
import com.facebook.internal.FileLruCache.Limits;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.http.HttpStatus;

class ImageResponseCache {
    static final String TAG = ImageResponseCache.class.getSimpleName();
    private static volatile FileLruCache imageCache;

    private static class BufferedHttpInputStream extends BufferedInputStream {
        HttpURLConnection connection;

        BufferedHttpInputStream(InputStream stream, HttpURLConnection connection) {
            super(stream, Utility.DEFAULT_STREAM_BUFFER_SIZE);
            this.connection = connection;
        }

        public void close() throws IOException {
            super.close();
            Utility.disconnectQuietly(this.connection);
        }
    }

    ImageResponseCache() {
    }

    static synchronized FileLruCache getCache(Context context) throws IOException {
        FileLruCache fileLruCache;
        synchronized (ImageResponseCache.class) {
            if (imageCache == null) {
                imageCache = new FileLruCache(TAG, new Limits());
            }
            fileLruCache = imageCache;
        }
        return fileLruCache;
    }

    static InputStream getCachedImageStream(Uri uri, Context context) {
        InputStream imageStream = null;
        if (uri != null && isCDNURL(uri)) {
            try {
                imageStream = getCache(context).get(uri.toString());
            } catch (IOException e) {
                Logger.log(LoggingBehavior.CACHE, 5, TAG, e.toString());
            }
        }
        return imageStream;
    }

    static InputStream interceptAndCacheImageStream(Context context, HttpURLConnection connection) throws IOException {
        InputStream stream = null;
        if (connection.getResponseCode() == HttpStatus.SC_OK) {
            Uri uri = Uri.parse(connection.getURL().toString());
            stream = connection.getInputStream();
            try {
                if (isCDNURL(uri)) {
                    stream = getCache(context).interceptAndPut(uri.toString(), new BufferedHttpInputStream(stream, connection));
                }
            } catch (IOException e) {
            }
        }
        return stream;
    }

    private static boolean isCDNURL(Uri uri) {
        if (uri != null) {
            String uriHost = uri.getHost();
            if (uriHost.endsWith("fbcdn.net")) {
                return true;
            }
            if (uriHost.startsWith("fbcdn") && uriHost.endsWith("akamaihd.net")) {
                return true;
            }
        }
        return false;
    }

    static void clearCache(Context context) {
        try {
            getCache(context).clearCache();
        } catch (IOException e) {
            Logger.log(LoggingBehavior.CACHE, 5, TAG, "clearCache failed " + e.getMessage());
        }
    }
}
