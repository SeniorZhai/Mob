package com.mobcrush.mobcrush.player;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import io.fabric.sdk.android.services.network.HttpRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerUtil {
    public static byte[] executePost(String url, byte[] data, Map<String, String> requestProperties) throws IOException {
        HttpURLConnection urlConnection = null;
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
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(data);
                out.close();
            }
            byte[] convertInputStreamToByteArray = convertInputStreamToByteArray(new BufferedInputStream(urlConnection.getInputStream()));
            return convertInputStreamToByteArray;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
        while (true) {
            int count = inputStream.read(data);
            if (count != -1) {
                bos.write(data, 0, count);
            } else {
                bos.flush();
                bos.close();
                inputStream.close();
                return bos.toByteArray();
            }
        }
    }
}
