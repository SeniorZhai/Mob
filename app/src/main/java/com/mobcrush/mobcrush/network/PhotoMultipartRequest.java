package com.mobcrush.mobcrush.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import io.fabric.sdk.android.services.network.HttpRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.HTTP;

public class PhotoMultipartRequest extends StringRequest {
    private static final String FILE_PART_NAME = "file";
    protected Map<String, String> headers;
    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private final File mImageFile;
    private final Listener<String> mListener;

    public PhotoMultipartRequest(String url, File imageFile, Listener<String> listener, ErrorListener errorListener) {
        super(1, url, listener, errorListener);
        this.mListener = listener;
        this.mImageFile = imageFile;
        buildMultipartEntity();
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap();
        }
        headers.put(HttpHeaders.ACCEPT, HttpRequest.CONTENT_TYPE_JSON);
        return headers;
    }

    private void buildMultipartEntity() {
        this.mBuilder.addBinaryBody(FILE_PART_NAME, this.mImageFile, ContentType.create("image/jpeg"), this.mImageFile.getName());
        this.mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        this.mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName(HTTP.UTF_8));
    }

    public String getBodyContentType() {
        return this.mBuilder.build().getContentType().getValue();
    }

    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            this.mBuilder.build().writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.", new Object[0]);
        }
        return bos.toByteArray();
    }

    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success(NetworkRequest.safeParseNetworkResponseData(response), HttpHeaderParser.parseCacheHeaders(response));
    }
}
