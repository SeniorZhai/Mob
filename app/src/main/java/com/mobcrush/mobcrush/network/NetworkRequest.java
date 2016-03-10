package com.mobcrush.mobcrush.network;

import android.content.Intent;
import android.text.TextUtils;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.mobcrush.mobcrush.EmailVerificationRequestActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import java.io.UnsupportedEncodingException;
import org.apache.http.protocol.HTTP;

public class NetworkRequest extends StringRequest {
    private final ErrorListener mErrorListener;
    private final Listener<String> mListener;

    public NetworkRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    public static String safeParseNetworkResponseData(NetworkResponse response) {
        if (response == null) {
            return null;
        }
        try {
            return new String(response.data, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return new String(response.data);
        }
    }

    public Response<String> parseNetworkResponse(NetworkResponse response) {
        String responseData = safeParseNetworkResponseData(response);
        if (!(response == null || response.headers == null)) {
            String data = (String) response.headers.get(Network.MC_USER);
            if (!TextUtils.isEmpty(data)) {
                synchronized (PreferenceUtility.mLocker) {
                    PreferenceUtility.setEmailVerified(data);
                }
            }
            if (responseData.contains("{\"status\":\"unverified\"}") && !PreferenceUtility.isEmailVerified()) {
                Intent intent = new Intent(MainApplication.getContext(), EmailVerificationRequestActivity.class);
                intent.setFlags(268435456);
                MainApplication.getContext().startActivity(intent);
            }
        }
        return Response.success(responseData, HttpHeaderParser.parseCacheHeaders(response));
    }

    public Listener<String> getResponseListener() {
        return this.mListener;
    }

    public ErrorListener getErrorListener() {
        return this.mErrorListener;
    }
}
