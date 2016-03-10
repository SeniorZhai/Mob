package com.mixpanel.android.util;

import android.content.Context;
import java.io.IOException;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.NameValuePair;

public interface RemoteService {

    public static class ServiceUnavailableException extends Exception {
        private final int mRetryAfter;

        public ServiceUnavailableException(String message, String strRetryAfter) {
            int retry;
            super(message);
            try {
                retry = Integer.parseInt(strRetryAfter);
            } catch (NumberFormatException e) {
                retry = 0;
            }
            this.mRetryAfter = retry;
        }

        public int getRetryAfter() {
            return this.mRetryAfter;
        }
    }

    boolean isOnline(Context context);

    byte[] performRequest(String str, List<NameValuePair> list, SSLSocketFactory sSLSocketFactory) throws ServiceUnavailableException, IOException;
}
