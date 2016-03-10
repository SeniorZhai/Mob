package com.firebase.client.utilities;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtilities {

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType = new int[HttpRequestType.values().length];

        static {
            try {
                $SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[HttpRequestType.GET.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[HttpRequestType.DELETE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[HttpRequestType.POST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[HttpRequestType.PUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public enum HttpRequestType {
        GET,
        POST,
        DELETE,
        PUT
    }

    public static URI buildUrl(String server, String path, Map<String, String> params) {
        try {
            URI serverURI = new URI(server);
            URI uri = new URI(serverURI.getScheme(), serverURI.getAuthority(), path, null, null);
            String query = null;
            if (params != null) {
                StringBuilder queryBuilder = new StringBuilder();
                boolean first = true;
                for (Entry<String, String> entry : params.entrySet()) {
                    if (!first) {
                        queryBuilder.append("&");
                    }
                    first = false;
                    queryBuilder.append(URLEncoder.encode((String) entry.getKey(), "utf-8"));
                    queryBuilder.append("=");
                    queryBuilder.append(URLEncoder.encode((String) entry.getValue(), "utf-8"));
                }
                query = queryBuilder.toString();
            }
            if (query != null) {
                return new URI(uri.toASCIIString() + "?" + query);
            }
            return uri;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't build valid auth URI.", e);
        } catch (URISyntaxException e2) {
            throw new RuntimeException("Couldn't build valid auth URI.", e2);
        }
    }

    private static void addMethodParams(HttpEntityEnclosingRequestBase request, Map<String, String> params) {
        if (params != null) {
            List<NameValuePair> postParams = new ArrayList();
            for (Entry<String, String> entry : params.entrySet()) {
                postParams.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
            }
            try {
                request.setEntity(new UrlEncodedFormEntity(postParams, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Didn't find utf-8 encoding", e);
            }
        }
    }

    public static HttpUriRequest requestWithType(String server, String path, HttpRequestType type, Map<String, String> urlParams, Map<String, String> requestParams) {
        switch (AnonymousClass1.$SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[type.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                Map<String, String> urlParams2 = new HashMap(urlParams);
                urlParams2.putAll(requestParams);
                urlParams = urlParams2;
                break;
        }
        if (type == HttpRequestType.DELETE) {
            urlParams.put("_method", "delete");
        }
        URI url = buildUrl(server, path, urlParams);
        switch (AnonymousClass1.$SwitchMap$com$firebase$client$utilities$HttpUtilities$HttpRequestType[type.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return new HttpGet(url);
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return new HttpDelete(url);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                HttpUriRequest post = new HttpPost(url);
                if (requestParams == null) {
                    return post;
                }
                addMethodParams(post, requestParams);
                return post;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                HttpPut put = new HttpPut(url);
                if (requestParams != null) {
                    addMethodParams(put, requestParams);
                }
                return put;
            default:
                throw new IllegalStateException("Shouldn't reach here!");
        }
    }
}
