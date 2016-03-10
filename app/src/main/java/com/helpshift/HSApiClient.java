package com.helpshift;

import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.share.internal.ShareConstants;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.gms.actions.SearchIntents;
import com.helpshift.constants.MessageColumns;
import com.helpshift.exceptions.InstallException;
import com.helpshift.storage.ProfilesDBHelper;
import com.helpshift.util.HSTimeUtil;
import com.helpshift.util.LocaleUtil;
import com.mobcrush.mobcrush.Constants;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.network.HttpRequest;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSApiClient {
    static final String TAG = "HelpShiftDebug";
    public static final String apiVersion = "2";
    public static final String libraryVersion = "3.10.0";
    private static int timeStampErrorReplies = 0;
    static final int timeStampMaxRetries = 3;
    final String SC_SENT = "Screenshot sent";
    final String SOL_ACCEPT = "Accepted the solution";
    final String SOL_REJECTED = "Did not accept the solution";
    final String SOL_REVIEW = "Accepted review request";
    final String apiBase = "/api/lib/";
    final String apiKey;
    final String appId;
    final String domain;
    final String scheme = Constants.BASE_SCHEME;
    private HSStorage storage;

    static /* synthetic */ int access$708() {
        int i = timeStampErrorReplies;
        timeStampErrorReplies = i + 1;
        return i;
    }

    protected HSApiClient(String domain, String appId, String apiKey, HSStorage storage) {
        this.domain = domain;
        this.appId = appId;
        this.apiKey = apiKey;
        this.storage = storage;
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[(j * 2) + 1] = hexArray[v & 15];
        }
        return new String(hexChars);
    }

    private String getSignature(String sigString) throws GeneralSecurityException, InstallException {
        String data = sigString;
        String key = this.apiKey;
        if (TextUtils.isEmpty(key)) {
            throw new InstallException("apiKey Missing");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(HTTP.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            return bytesToHex(mac.doFinal(data.getBytes(HTTP.UTF_8)));
        } catch (UnsupportedEncodingException e) {
            throw new GeneralSecurityException(e);
        }
    }

    private String getApiUri(String route) {
        return new String("/api/lib/2" + route);
    }

    private String getApiUrl(String route) throws InstallException {
        if (!TextUtils.isEmpty(this.domain)) {
            return new String(Constants.BASE_SCHEME + this.domain + getApiUri(route));
        }
        throw new InstallException("domain Missing");
    }

    private String constructGetParams(HashMap<String, String> data) {
        List<String> dataList = new ArrayList();
        for (String key : new ArrayList(data.keySet())) {
            dataList.add(new String(key + "=" + Uri.encode((String) data.get(key))));
        }
        return TextUtils.join("&", dataList);
    }

    private String getStringValue(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof ArrayList) {
            return new JSONArray((ArrayList) value).toString();
        }
        return null;
    }

    private List<NameValuePair> constructPostParams(HashMap<String, String> data) {
        List<String> mapKeys = new ArrayList(data.keySet());
        List<NameValuePair> params = new ArrayList(mapKeys.size());
        for (String key : mapKeys) {
            String dataString = getStringValue(data.get(key));
            if (dataString != null) {
                params.add(new BasicNameValuePair(key, dataString));
            }
        }
        return params;
    }

    private HashMap<String, String> addAuth(HashMap<String, String> data, String route, String method) throws InstallException {
        String uriStr = getApiUri(route);
        if (TextUtils.isEmpty(this.appId)) {
            throw new InstallException("appId Missing");
        }
        data.put("platform-id", this.appId);
        data.put("method", method);
        data.put("uri", uriStr);
        data.put(Constants.CHAT_MESSAGE_TIMESTAMP, HSTimeUtil.getAdjustedTimestamp(this.storage.getServerTimeDelta()));
        List<String> mapKeys = new ArrayList(data.keySet());
        List<String> dataList = new ArrayList();
        Collections.sort(mapKeys);
        for (String key : mapKeys) {
            if (!(key == MessageColumns.SCREENSHOT || key == MessageColumns.META)) {
                String dataString = getStringValue(data.get(key));
                if (dataString != null) {
                    dataList.add(new String(key + "=" + dataString));
                }
            }
        }
        try {
            data.put("signature", getSignature(TextUtils.join("&", dataList)));
            data.remove("method");
            data.remove("uri");
        } catch (GeneralSecurityException e) {
            Log.d(TAG, "Could not generate signature: " + e.getLocalizedMessage(), e);
        }
        return data;
    }

    private void sendFailMessage(Handler failure, int status) {
        Message result = failure.obtainMessage();
        HashMap messageResponse = new HashMap();
        messageResponse.put(SettingsJsonConstants.APP_STATUS_KEY, Integer.valueOf(status));
        result.obj = messageResponse;
        failure.sendMessage(result);
    }

    private void makeRequest(String method, String route, HashMap data, Handler success, Handler failure) {
        final HashMap hashMap = data;
        final String str = route;
        final String str2 = method;
        final Handler handler = success;
        final Handler handler2 = failure;
        new Thread(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                r32 = this;
                r5 = new java.util.HashMap;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r2 = r2;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r5.<init>(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r3;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r29 = r2.getApiUrl(r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r8 = 0;
                r0 = r32;
                r2 = r4;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = "GET";
                if (r2 != r3) goto L_0x00e9;
            L_0x001e:
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r4 = r2;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r6 = r3;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r7 = r4;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = r3.addAuth(r4, r6, r7);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r23 = r2.constructGetParams(r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r30 = new java.net.URL;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = new java.lang.StringBuilder;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2.<init>();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r29;
                r2 = r2.append(r0);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = "?";
                r2 = r2.append(r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r23;
                r2 = r2.append(r0);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = r2.toString();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r30;
                r0.<init>(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r8 = r30.openConnection();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r8 = (java.net.HttpURLConnection) r8;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "GET";
                r8.setRequestMethod(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                com.helpshift.HSApiClient.addHeadersToConnection(r8);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = r2.storage;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r3;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r12 = r2.getEtag(r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = android.text.TextUtils.isEmpty(r12);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                if (r2 != 0) goto L_0x0085;
            L_0x0080:
                r2 = "If-None-Match";
                r8.setRequestProperty(r2, r12);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
            L_0x0085:
                if (r8 == 0) goto L_0x03e0;
            L_0x0087:
                r28 = r8.getResponseCode();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r8.getHeaderFields();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r15 = r2.entrySet();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r16 = r15.iterator();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x0097:
                r2 = r16.hasNext();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x016d;
            L_0x009d:
                r14 = r16.next();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r14 = (java.util.Map.Entry) r14;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0097;
            L_0x00a9:
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = "ETag";
                r2 = r2.equals(r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0097;
            L_0x00b7:
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = r2.storage;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r4 = r3;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r14.getValue();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.util.List) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r6 = 0;
                r2 = r2.get(r6);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3.setEtag(r4, r2);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x0097;
            L_0x00d4:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 1;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception JSON";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
            L_0x00e8:
                return;
            L_0x00e9:
                r0 = r32;
                r2 = r4;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = "POST";
                if (r2 != r3) goto L_0x0085;
            L_0x00f1:
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r4 = r2;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r6 = r3;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r7 = r4;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = r3.addAuth(r4, r6, r7);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r21 = r2.constructPostParams(r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r30 = new java.net.URL;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r30;
                r1 = r29;
                r0.<init>(r1);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r8 = r30.openConnection();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r8 = (java.net.HttpURLConnection) r8;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "POST";
                r8.setRequestMethod(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = 1;
                r8.setDoOutput(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "Content-type";
                r3 = "application/x-www-form-urlencoded";
                r8.setRequestProperty(r2, r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                com.helpshift.HSApiClient.addHeadersToConnection(r8);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r20 = r8.getOutputStream();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r31 = new java.io.BufferedWriter;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = new java.io.OutputStreamWriter;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r3 = "UTF-8";
                r0 = r20;
                r2.<init>(r0, r3);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r31;
                r0.<init>(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = com.helpshift.HSApiClient.constructPostParamsQuery(r21);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r31;
                r0.write(r2);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r31.flush();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r31.close();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r20.close();	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x0085;
            L_0x0157:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "install() not called";
                android.util.Log.e(r2, r3, r9);
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;
                r0 = r32;
                r3 = r6;
                r4 = 1;
                r2.sendFailMessage(r3, r4);
                goto L_0x00e8;
            L_0x016d:
                r18 = "";
                r26 = new java.lang.StringBuilder;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r26.<init>();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
                r0 = r28;
                if (r0 < r2) goto L_0x01f6;
            L_0x017a:
                r2 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
                r0 = r28;
                if (r0 >= r2) goto L_0x01f6;
            L_0x0180:
                r24 = new java.io.BufferedInputStream;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r8.getInputStream();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r24;
                r0.<init>(r2);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r16 = r15.iterator();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r25 = r24;
            L_0x0191:
                r2 = r16.hasNext();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x01ce;
            L_0x0197:
                r14 = r16.next();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r14 = (java.util.Map.Entry) r14;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0404;
            L_0x01a3:
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = "Content-Encoding";
                r2 = r2.equals(r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0404;
            L_0x01b1:
                r2 = r14.getValue();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.util.List) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = 0;
                r2 = r2.get(r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = "gzip";
                r2 = r2.equalsIgnoreCase(r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0404;
            L_0x01c6:
                r24 = new java.util.zip.GZIPInputStream;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r24.<init>(r25);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x01cb:
                r25 = r24;
                goto L_0x0191;
            L_0x01ce:
                r17 = new java.io.InputStreamReader;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r17;
                r1 = r25;
                r0.<init>(r1);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r22 = new java.io.BufferedReader;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r22;
                r1 = r17;
                r0.<init>(r1);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x01e0:
                r18 = r22.readLine();	 Catch:{ IOException -> 0x01ee, JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r18 == 0) goto L_0x01f6;
            L_0x01e6:
                r0 = r26;
                r1 = r18;
                r0.append(r1);	 Catch:{ IOException -> 0x01ee, JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x01e0;
            L_0x01ee:
                r13 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "IO Exception ex";
                android.util.Log.d(r2, r3, r13);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x01f6:
                r19 = new java.util.HashMap;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r19.<init>();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = "status";
                r3 = java.lang.Integer.valueOf(r28);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r0.put(r2, r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
                r0 = r28;
                if (r0 < r2) goto L_0x02b1;
            L_0x020d:
                r2 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
                r0 = r28;
                if (r0 >= r2) goto L_0x02b1;
            L_0x0213:
                r2 = 0;
                com.helpshift.HSApiClient.timeStampErrorReplies = r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = "response";
                r3 = new org.json.JSONArray;	 Catch:{ JSONException -> 0x0270, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r4 = r26.toString();	 Catch:{ JSONException -> 0x0270, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3.<init>(r4);	 Catch:{ JSONException -> 0x0270, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r0.put(r2, r3);	 Catch:{ JSONException -> 0x0270, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x0228:
                r0 = r32;
                r2 = r5;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r27 = r2.obtainMessage();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r1 = r27;
                r1.obj = r0;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r5;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r27;
                r2.sendMessage(r0);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x023f:
                r8.disconnect();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x00e8;
            L_0x0244:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 3;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception Unknown Host";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x025a:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception Malformed URL";
                android.util.Log.d(r2, r3, r9);
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;
                r0 = r32;
                r3 = r6;
                r4 = 1;
                r2.sendFailMessage(r3, r4);
                goto L_0x00e8;
            L_0x0270:
                r10 = move-exception;
                r2 = "response";
                r3 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x0283, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r4 = r26.toString();	 Catch:{ JSONException -> 0x0283, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3.<init>(r4);	 Catch:{ JSONException -> 0x0283, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r0.put(r2, r3);	 Catch:{ JSONException -> 0x0283, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x0228;
            L_0x0283:
                r11 = move-exception;
                throw r11;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x0285:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 0;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception cannot connect Host";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x029b:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception Protocol";
                android.util.Log.d(r2, r3, r9);
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;
                r0 = r32;
                r3 = r6;
                r4 = 1;
                r2.sendFailMessage(r3, r4);
                goto L_0x00e8;
            L_0x02b1:
                r2 = 304; // 0x130 float:4.26E-43 double:1.5E-321;
                r0 = r28;
                if (r0 != r2) goto L_0x02f2;
            L_0x02b7:
                com.helpshift.HSApiClient.access$708();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r5;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r27 = r2.obtainMessage();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = 0;
                r0 = r27;
                r0.obj = r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r5;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r27;
                r2.sendMessage(r0);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x023f;
            L_0x02d2:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception Socket timeout";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x02dc:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception Unknown Host";
                android.util.Log.d(r2, r3, r9);
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;
                r0 = r32;
                r3 = r6;
                r4 = 3;
                r2.sendFailMessage(r3, r4);
                goto L_0x00e8;
            L_0x02f2:
                r2 = 422; // 0x1a6 float:5.91E-43 double:2.085E-321;
                r0 = r28;
                if (r0 != r2) goto L_0x03b9;
            L_0x02f8:
                com.helpshift.HSApiClient.access$708();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = com.helpshift.HSApiClient.timeStampErrorReplies;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = 3;
                if (r2 > r3) goto L_0x0386;
            L_0x0302:
                r16 = r15.iterator();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
            L_0x0306:
                r2 = r16.hasNext();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x023f;
            L_0x030c:
                r14 = r16.next();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r14 = (java.util.Map.Entry) r14;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0306;
            L_0x0318:
                r2 = r14.getKey();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = "HS-UEpoch";
                r2 = r2.equals(r3);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                if (r2 == 0) goto L_0x0306;
            L_0x0326:
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3 = r2.storage;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = r14.getValue();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.util.List) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r4 = 0;
                r2 = r2.get(r4);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = (java.lang.String) r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2 = com.helpshift.util.HSTimeUtil.calculateTimeAdjustment(r2);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r3.setServerTimeDelta(r2);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r3 = r4;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r4 = r3;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r6 = r5;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r7 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r2.makeRequest(r3, r4, r5, r6, r7);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x0306;
            L_0x035a:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 0;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception Socket timeout";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x0370:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception IO";
                android.util.Log.d(r2, r3, r9);
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;
                r0 = r32;
                r3 = r6;
                r4 = 1;
                r2.sendFailMessage(r3, r4);
                goto L_0x00e8;
            L_0x0386:
                r2 = 0;
                com.helpshift.HSApiClient.timeStampErrorReplies = r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r27 = r2.obtainMessage();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r1 = r27;
                r1.obj = r0;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r27;
                r2.sendMessage(r0);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x023f;
            L_0x03a3:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 1;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception Client Protocol";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x03b9:
                r2 = 0;
                com.helpshift.HSApiClient.timeStampErrorReplies = r2;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r27 = r2.obtainMessage();	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r19;
                r1 = r27;
                r1.obj = r0;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r2 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r27;
                r2.sendMessage(r0);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x023f;
            L_0x03d6:
                r9 = move-exception;
                r2 = "HelpShiftDebug";
                r3 = "Exception SSL Peer Unverified";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x03e0:
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r0 = r32;
                r3 = r6;	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                r4 = 3;
                r2.sendFailMessage(r3, r4);	 Catch:{ JSONException -> 0x00d4, UnknownHostException -> 0x0244, HttpHostConnectException -> 0x0285, SocketTimeoutException -> 0x02d2, ConnectTimeoutException -> 0x035a, ClientProtocolException -> 0x03a3, SSLPeerUnverifiedException -> 0x03d6, IOException -> 0x03ee, InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b }
                goto L_0x00e8;
            L_0x03ee:
                r9 = move-exception;
                r0 = r32;
                r2 = com.helpshift.HSApiClient.this;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r0 = r32;
                r3 = r6;	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r4 = 1;
                r2.sendFailMessage(r3, r4);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                r2 = "HelpShiftDebug";
                r3 = "Exception IO";
                android.util.Log.d(r2, r3, r9);	 Catch:{ InstallException -> 0x0157, MalformedURLException -> 0x025a, ProtocolException -> 0x029b, UnknownHostException -> 0x02dc, IOException -> 0x0370 }
                goto L_0x00e8;
            L_0x0404:
                r24 = r25;
                goto L_0x01cb;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.helpshift.HSApiClient.1.run():void");
            }
        }).start();
    }

    private String getMimeType(String url) {
        String type = null;
        try {
            InputStream is = new FileInputStream(url);
            type = URLConnection.guessContentTypeFromStream(is);
            if (type == null) {
                type = URLConnection.guessContentTypeFromName(url);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return type;
    }

    private void uploadImage(String method, String route, HashMap plainData, Handler success, Handler failure) {
        final HashMap hashMap = plainData;
        final String str = route;
        final String str2 = method;
        final Handler handler = success;
        final Handler handler2 = failure;
        new Thread(new Runnable() {
            public void run() {
                try {
                    HashMap data = HSApiClient.this.addAuth(hashMap, str, str2);
                    File file = new File((String) data.get(MessageColumns.SCREENSHOT));
                    String screenshotMimeType = HSApiClient.this.getMimeType(file.getPath());
                    HashMap messageResponse;
                    Message result;
                    if (new HashSet(Arrays.asList(new String[]{"image/jpeg", "image/png", "image/gif", "image/x-png", "image/x-citrix-pjpeg", "image/x-citrix-gif", "image/pjpeg"})).contains(screenshotMimeType)) {
                        URL postUrl = null;
                        try {
                            postUrl = new URL(HSApiClient.this.getApiUrl(str));
                        } catch (MalformedURLException e) {
                            Log.d(HSApiClient.TAG, e.getMessage(), e);
                            HSApiClient.this.sendFailMessage(handler2, 2);
                        }
                        String lineEnd = "\r\n";
                        String twoHyphens = "--";
                        String boundary = "*****";
                        if (postUrl != null) {
                            try {
                                HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
                                conn.setDoInput(true);
                                conn.setDoOutput(true);
                                conn.setUseCaches(false);
                                conn.setRequestMethod(HttpRequest.METHOD_POST);
                                conn.setConnectTimeout(DefaultLoadControl.DEFAULT_HIGH_WATERMARK_MS);
                                conn.setReadTimeout(DefaultLoadControl.DEFAULT_HIGH_WATERMARK_MS);
                                conn.setRequestProperty(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
                                conn.setRequestProperty(HTTP.CONTENT_TYPE, "multipart/form-data;boundary=" + boundary);
                                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                                dos.writeBytes(twoHyphens + boundary + lineEnd);
                                for (String key : new ArrayList(data.keySet())) {
                                    if (!key.equals(MessageColumns.SCREENSHOT)) {
                                        String value = (String) data.get(key);
                                        dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; " + lineEnd);
                                        dos.writeBytes("Content-Type: text/plain;charset=UTF-8" + lineEnd);
                                        dos.writeBytes("Content-Length: " + value.length() + lineEnd);
                                        dos.writeBytes(lineEnd);
                                        dos.writeBytes(value + lineEnd);
                                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                                    }
                                }
                                FileInputStream fileInputStream = new FileInputStream(file);
                                dos.writeBytes(twoHyphens + boundary + lineEnd);
                                dos.writeBytes("Content-Disposition: form-data; name=\"screenshot\"; filename=\"" + file.getName() + "\"" + lineEnd);
                                dos.writeBytes("Content-Type: " + screenshotMimeType + lineEnd);
                                dos.writeBytes("Content-Length: " + file.length() + lineEnd);
                                dos.writeBytes(lineEnd);
                                int bufferSize = Math.min(fileInputStream.available(), AccessibilityNodeInfoCompat.ACTION_DISMISS);
                                byte[] buffer = new byte[bufferSize];
                                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                                while (bytesRead > 0) {
                                    dos.write(buffer, 0, bufferSize);
                                    bufferSize = Math.min(fileInputStream.available(), AccessibilityNodeInfoCompat.ACTION_DISMISS);
                                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                                }
                                dos.writeBytes(lineEnd);
                                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                                fileInputStream.close();
                                int status = conn.getResponseCode();
                                String serverResponseMessage = conn.getResponseMessage();
                                String line = BuildConfig.FLAVOR;
                                StringBuilder responseStr = new StringBuilder();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                while (true) {
                                    try {
                                        line = bufferedReader.readLine();
                                        if (line == null) {
                                            break;
                                        }
                                        responseStr.append(line);
                                    } catch (Throwable ex) {
                                        Log.d(HSApiClient.TAG, "IO Exception ex", ex);
                                        HSApiClient.this.sendFailMessage(handler2, 2);
                                    }
                                }
                                String response = responseStr.toString();
                                messageResponse = new HashMap();
                                messageResponse.put(SettingsJsonConstants.APP_STATUS_KEY, Integer.valueOf(status));
                                if (status < 200 || status >= 300) {
                                    result = handler2.obtainMessage();
                                    result.obj = messageResponse;
                                    handler2.sendMessage(result);
                                } else {
                                    try {
                                        messageResponse.put("response", new JSONArray(response));
                                    } catch (JSONException e2) {
                                        messageResponse.put("response", new JSONObject(response));
                                    }
                                    result = handler.obtainMessage();
                                    result.obj = messageResponse;
                                    handler.sendMessage(result);
                                }
                                conn.disconnect();
                                dos.flush();
                                dos.close();
                                return;
                            } catch (JSONException eobj) {
                                throw eobj;
                            } catch (Exception e3) {
                                Log.d(HSApiClient.TAG, e3.getMessage(), e3);
                                HSApiClient.this.sendFailMessage(handler2, 2);
                                return;
                            }
                        }
                        HSApiClient.this.sendFailMessage(handler2, 2);
                        return;
                    }
                    messageResponse = new HashMap();
                    messageResponse.put(SettingsJsonConstants.APP_STATUS_KEY, Integer.valueOf(-1));
                    result = handler.obtainMessage();
                    result.obj = messageResponse;
                    handler2.sendMessage(result);
                } catch (InstallException e4) {
                    Log.e(HSApiClient.TAG, "Error : ", e4);
                    HSApiClient.this.sendFailMessage(handler2, 1);
                }
            }
        }).start();
    }

    protected void fetchFaqs(Handler success, Handler failure) {
        makeRequest(HttpRequest.METHOD_GET, "/faqs/", new HashMap(), success, failure);
    }

    protected void fetchFaq(Handler success, Handler failure, String faqId) {
        makeRequest(HttpRequest.METHOD_GET, new String("/faqs/" + faqId + "/"), new HashMap(), success, failure);
    }

    protected void search(Handler success, Handler failure, String query) {
        HashMap data = new HashMap();
        data.put(SearchIntents.EXTRA_QUERY, query);
        makeRequest(HttpRequest.METHOD_GET, "/search/", data, success, failure);
    }

    protected void registerProfile(Handler success, Handler failure, String username, String email, String identifier, String crittercismId) {
        HashMap data = new HashMap();
        data.put("displayname", username);
        data.put(ProfilesDBHelper.COLUMN_EMAIL, email);
        data.put(SettingsJsonConstants.APP_IDENTIFIER_KEY, identifier);
        if (crittercismId != null) {
            data.put("crittercism-id", crittercismId);
        }
        makeRequest(HttpRequest.METHOD_POST, "/profiles/", data, success, failure);
    }

    protected void updateUAToken(Handler success, Handler failure, String deviceToken, String profileId) {
        HashMap data = new HashMap();
        data.put("token", deviceToken);
        data.put("profile-id", profileId);
        makeRequest(HttpRequest.METHOD_POST, "/update-ua-token/", data, success, failure);
    }

    protected void fetchMyIssues(Handler success, Handler failure, String profileId, String since, String mc, String chatLaunchSource) {
        HashMap data = new HashMap();
        data.put("profile-id", profileId);
        data.put("since", since);
        data.put("mc", mc);
        if (chatLaunchSource != null) {
            data.put("chat-launch-source", chatLaunchSource);
        }
        makeRequest(HttpRequest.METHOD_POST, "/my-issues/", data, success, failure);
    }

    protected void createIssue(Handler success, Handler failure, String profileId, String messageText, String meta) {
        HashMap data = new HashMap();
        data.put("profile-id", profileId);
        data.put("message-text", messageText);
        data.put(MessageColumns.META, meta);
        makeRequest(HttpRequest.METHOD_POST, "/issues/", data, success, failure);
    }

    protected void addMessage(Handler success, Handler failure, String profileId, String issueId, String messageText, String type, String refers, String messageMeta) {
        HashMap data = new HashMap();
        if (type == "ca") {
            messageText = "Accepted the solution";
        } else if (type == "ncr") {
            messageText = "Did not accept the solution";
        } else if (type == "ar") {
            messageText = "Accepted review request";
        }
        data.put("profile-id", profileId);
        data.put("message-text", messageText);
        data.put(MessageColumns.TYPE, type);
        data.put("refers", refers);
        data.put("message-meta", messageMeta);
        makeRequest(HttpRequest.METHOD_POST, new String("/issues/" + issueId + "/messages/"), data, success, failure);
    }

    protected void addScMessage(Handler success, Handler failure, String profileId, String issueId, String messageText, String type, String refers, String imageUri) {
        HashMap data = new HashMap();
        if (type == "sc") {
            messageText = "Screenshot sent";
        }
        data.put("profile-id", profileId);
        data.put("message-text", messageText);
        data.put(MessageColumns.TYPE, type);
        data.put("refers", refers);
        data.put(MessageColumns.SCREENSHOT, imageUri);
        uploadImage(HttpRequest.METHOD_POST, new String("/issues/" + issueId + "/messages/"), data, success, failure);
    }

    protected void fetchMessages(Handler success, Handler failure, String profileId, String issueId, String since, String chatLaunchSource) {
        HashMap data = new HashMap();
        data.put("profile-id", profileId);
        data.put("since", since);
        if (chatLaunchSource != null) {
            data.put("chat-launch-source", chatLaunchSource);
        }
        makeRequest(HttpRequest.METHOD_GET, new String("/issues/" + issueId + "/messages/"), data, success, failure);
    }

    protected void markHelpful(Handler success, Handler failure, String faqId) {
        HashMap data = new HashMap();
        makeRequest(HttpRequest.METHOD_POST, new String("/faqs/" + faqId + "/helpful/"), data, success, failure);
    }

    protected void markUnhelpful(Handler success, Handler failure, String faqId) {
        HashMap data = new HashMap();
        makeRequest(HttpRequest.METHOD_POST, new String("/faqs/" + faqId + "/unhelpful/"), data, success, failure);
    }

    protected void getConfig(Handler success, Handler failure) {
        makeRequest(HttpRequest.METHOD_GET, "/config/", new HashMap(), success, failure);
    }

    protected void reportActionEvents(Handler success, Handler failure, HashMap data) {
        makeRequest(HttpRequest.METHOD_POST, "/events/", data, success, failure);
    }

    protected void getQuestion(String publishId, Handler success, Handler failure) {
        HashMap data = new HashMap();
        makeRequest(HttpRequest.METHOD_GET, "/faqs/" + publishId + "/", data, success, failure);
    }

    protected void updateMessageSeenState(JSONArray messageIds, String source, String readAt, Handler success, Handler failure) {
        HashMap data = new HashMap();
        data.put("message-ids", messageIds.toString());
        data.put(ShareConstants.FEED_SOURCE_PARAM, source);
        data.put("read-at", readAt);
        makeRequest(HttpRequest.METHOD_POST, "/events/messages/seen/", data, success, failure);
    }

    protected void sendCustomerSatisfactionRating(Integer rating, String feedback, String issueId, Handler success, Handler failure) {
        HashMap data = new HashMap();
        data.put("rating", BuildConfig.FLAVOR + rating);
        if (!TextUtils.isEmpty(feedback)) {
            data.put("feedback", feedback);
        }
        makeRequest(HttpRequest.METHOD_POST, "/issues/" + issueId + "/customer-survey/", data, success, failure);
    }

    private static void addHeadersToConnection(HttpURLConnection connection) {
        String userAgent = "Helpshift-Android/3.10.0/" + VERSION.RELEASE;
        String acceptLangHead = String.format("%s;q=1.0", new Object[]{LocaleUtil.getAcceptLanguageHeader()});
        connection.setConnectTimeout(BaseImageDownloader.DEFAULT_HTTP_CONNECT_TIMEOUT);
        connection.setRequestProperty(CoreProtocolPNames.USER_AGENT, userAgent);
        connection.setRequestProperty(HttpHeaders.ACCEPT_LANGUAGE, acceptLangHead);
        connection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, HttpRequest.ENCODING_GZIP);
        connection.setRequestProperty("X-HS-V", "Helpshift-Android/3.10.0");
    }

    private static String constructPostParamsQuery(List<NameValuePair> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            try {
                result.append(URLEncoder.encode(pair.getName(), HTTP.UTF_8));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "Exception Unsupported Encoding", e);
            }
        }
        return result.toString();
    }
}
