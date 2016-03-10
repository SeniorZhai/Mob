package com.google.android.gms.internal;

import com.google.android.gms.tagmanager.zzbg;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

class zzqj implements zzql {
    private HttpClient zzaE;

    zzqj() {
    }

    private InputStream zza(HttpClient httpClient, HttpResponse httpResponse) throws IOException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            zzbg.zzaB("Success response");
            return httpResponse.getEntity().getContent();
        }
        String str = "Bad response: " + statusCode;
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new FileNotFoundException(str);
        }
        throw new IOException(str);
    }

    private void zza(HttpClient httpClient) {
        if (httpClient != null && httpClient.getConnectionManager() != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public void close() {
        zza(this.zzaE);
    }

    HttpClient zzAF() {
        HttpParams basicHttpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(basicHttpParams, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
        HttpConnectionParams.setSoTimeout(basicHttpParams, BaseImageDownloader.DEFAULT_HTTP_READ_TIMEOUT);
        return new DefaultHttpClient(basicHttpParams);
    }

    public InputStream zzfd(String str) throws IOException {
        this.zzaE = zzAF();
        return zza(this.zzaE, this.zzaE.execute(new HttpGet(str)));
    }
}
