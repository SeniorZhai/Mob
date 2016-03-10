package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import io.fabric.sdk.android.services.network.HttpRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HTTP;

class zzcx implements zzac {
    private final Context zzaNE;
    private final String zzaNV = zza("GoogleTagManager", "4.00", VERSION.RELEASE, zzc(Locale.getDefault()), Build.MODEL, Build.ID);
    private final HttpClient zzaNW;
    private zza zzaNX;

    public interface zza {
        void zza(zzaq com_google_android_gms_tagmanager_zzaq);

        void zzb(zzaq com_google_android_gms_tagmanager_zzaq);

        void zzc(zzaq com_google_android_gms_tagmanager_zzaq);
    }

    zzcx(HttpClient httpClient, Context context, zza com_google_android_gms_tagmanager_zzcx_zza) {
        this.zzaNE = context.getApplicationContext();
        this.zzaNW = httpClient;
        this.zzaNX = com_google_android_gms_tagmanager_zzcx_zza;
    }

    private void zza(HttpEntityEnclosingRequest httpEntityEnclosingRequest) {
        StringBuffer stringBuffer = new StringBuffer();
        for (Object obj : httpEntityEnclosingRequest.getAllHeaders()) {
            stringBuffer.append(obj.toString()).append("\n");
        }
        stringBuffer.append(httpEntityEnclosingRequest.getRequestLine().toString()).append("\n");
        if (httpEntityEnclosingRequest.getEntity() != null) {
            try {
                InputStream content = httpEntityEnclosingRequest.getEntity().getContent();
                if (content != null) {
                    int available = content.available();
                    if (available > 0) {
                        byte[] bArr = new byte[available];
                        content.read(bArr);
                        stringBuffer.append("POST:\n");
                        stringBuffer.append(new String(bArr)).append("\n");
                    }
                }
            } catch (IOException e) {
                zzbg.zzaB("Error Writing hit to log...");
            }
        }
        zzbg.zzaB(stringBuffer.toString());
    }

    static String zzc(Locale locale) {
        if (locale == null || locale.getLanguage() == null || locale.getLanguage().length() == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(locale.getLanguage().toLowerCase());
        if (!(locale.getCountry() == null || locale.getCountry().length() == 0)) {
            stringBuilder.append("-").append(locale.getCountry().toLowerCase());
        }
        return stringBuilder.toString();
    }

    private HttpEntityEnclosingRequest zzd(URL url) {
        HttpEntityEnclosingRequest basicHttpEntityEnclosingRequest;
        URISyntaxException e;
        try {
            basicHttpEntityEnclosingRequest = new BasicHttpEntityEnclosingRequest(HttpRequest.METHOD_GET, url.toURI().toString());
            try {
                basicHttpEntityEnclosingRequest.addHeader(HTTP.USER_AGENT, this.zzaNV);
            } catch (URISyntaxException e2) {
                e = e2;
                zzbg.zzaC("Exception sending hit: " + e.getClass().getSimpleName());
                zzbg.zzaC(e.getMessage());
                return basicHttpEntityEnclosingRequest;
            }
        } catch (URISyntaxException e3) {
            URISyntaxException uRISyntaxException = e3;
            basicHttpEntityEnclosingRequest = null;
            e = uRISyntaxException;
            zzbg.zzaC("Exception sending hit: " + e.getClass().getSimpleName());
            zzbg.zzaC(e.getMessage());
            return basicHttpEntityEnclosingRequest;
        }
        return basicHttpEntityEnclosingRequest;
    }

    String zza(String str, String str2, String str3, String str4, String str5, String str6) {
        return String.format("%s/%s (Linux; U; Android %s; %s; %s Build/%s)", new Object[]{str, str2, str3, str4, str5, str6});
    }

    URL zzd(zzaq com_google_android_gms_tagmanager_zzaq) {
        try {
            return new URL(com_google_android_gms_tagmanager_zzaq.zzyQ());
        } catch (MalformedURLException e) {
            zzbg.zzaz("Error trying to parse the GTM url.");
            return null;
        }
    }

    public void zzr(List<zzaq> list) {
        int min = Math.min(list.size(), 40);
        Object obj = 1;
        int i = 0;
        while (i < min) {
            Object obj2;
            zzaq com_google_android_gms_tagmanager_zzaq = (zzaq) list.get(i);
            URL zzd = zzd(com_google_android_gms_tagmanager_zzaq);
            if (zzd == null) {
                zzbg.zzaC("No destination: discarding hit.");
                this.zzaNX.zzb(com_google_android_gms_tagmanager_zzaq);
                obj2 = obj;
            } else {
                org.apache.http.HttpRequest zzd2 = zzd(zzd);
                if (zzd2 == null) {
                    this.zzaNX.zzb(com_google_android_gms_tagmanager_zzaq);
                    obj2 = obj;
                } else {
                    HttpHost httpHost = new HttpHost(zzd.getHost(), zzd.getPort(), zzd.getProtocol());
                    zzd2.addHeader(HTTP.TARGET_HOST, httpHost.toHostString());
                    zza(zzd2);
                    if (obj != null) {
                        try {
                            zzbl.zzaJ(this.zzaNE);
                            obj = null;
                        } catch (ClientProtocolException e) {
                            zzbg.zzaC("ClientProtocolException sending hit; discarding hit...");
                            this.zzaNX.zzb(com_google_android_gms_tagmanager_zzaq);
                            obj2 = obj;
                        } catch (IOException e2) {
                            zzbg.zzaC("Exception sending hit: " + e2.getClass().getSimpleName());
                            zzbg.zzaC(e2.getMessage());
                            this.zzaNX.zzc(com_google_android_gms_tagmanager_zzaq);
                            obj2 = obj;
                        }
                    }
                    HttpResponse execute = this.zzaNW.execute(httpHost, zzd2);
                    int statusCode = execute.getStatusLine().getStatusCode();
                    HttpEntity entity = execute.getEntity();
                    if (entity != null) {
                        entity.consumeContent();
                    }
                    if (statusCode != HttpStatus.SC_OK) {
                        zzbg.zzaC("Bad response: " + execute.getStatusLine().getStatusCode());
                        this.zzaNX.zzc(com_google_android_gms_tagmanager_zzaq);
                    } else {
                        this.zzaNX.zza(com_google_android_gms_tagmanager_zzaq);
                    }
                    obj2 = obj;
                }
            }
            i++;
            obj = obj2;
        }
    }

    public boolean zzyH() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.zzaNE.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        zzbg.zzaB("...no network connectivity");
        return false;
    }
}
