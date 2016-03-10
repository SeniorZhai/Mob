package com.google.android.gms.internal;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpStatus;

class zzqk implements zzql {
    private HttpURLConnection zzaPT;

    zzqk() {
    }

    private InputStream zzc(HttpURLConnection httpURLConnection) throws IOException {
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpStatus.SC_OK) {
            return httpURLConnection.getInputStream();
        }
        String str = "Bad response: " + responseCode;
        if (responseCode == HttpStatus.SC_NOT_FOUND) {
            throw new FileNotFoundException(str);
        }
        throw new IOException(str);
    }

    private void zzd(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
        }
    }

    public void close() {
        zzd(this.zzaPT);
    }

    public InputStream zzfd(String str) throws IOException {
        this.zzaPT = zzfe(str);
        return zzc(this.zzaPT);
    }

    HttpURLConnection zzfe(String str) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
        httpURLConnection.setReadTimeout(BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
        httpURLConnection.setConnectTimeout(BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
        return httpURLConnection;
    }
}
