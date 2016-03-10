package com.google.android.exoplayer.upstream;

import android.text.TextUtils;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Predicate;
import com.google.android.exoplayer.util.Util;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HttpDataSource extends UriDataSource {
    public static final Predicate<String> REJECT_PAYWALL_TYPES = new Predicate<String>() {
        public boolean evaluate(String contentType) {
            contentType = Util.toLowerInvariant(contentType);
            return (TextUtils.isEmpty(contentType) || ((contentType.contains(MimeTypes.BASE_TYPE_TEXT) && !contentType.contains(MimeTypes.TEXT_VTT)) || contentType.contains("html") || contentType.contains("xml"))) ? false : true;
        }
    };

    public static class HttpDataSourceException extends IOException {
        public final DataSpec dataSpec;

        public HttpDataSourceException(DataSpec dataSpec) {
            this.dataSpec = dataSpec;
        }

        public HttpDataSourceException(String message, DataSpec dataSpec) {
            super(message);
            this.dataSpec = dataSpec;
        }

        public HttpDataSourceException(IOException cause, DataSpec dataSpec) {
            super(cause);
            this.dataSpec = dataSpec;
        }

        public HttpDataSourceException(String message, IOException cause, DataSpec dataSpec) {
            super(message, cause);
            this.dataSpec = dataSpec;
        }
    }

    public static final class InvalidContentTypeException extends HttpDataSourceException {
        public final String contentType;

        public InvalidContentTypeException(String contentType, DataSpec dataSpec) {
            super("Invalid content type: " + contentType, dataSpec);
            this.contentType = contentType;
        }
    }

    public static final class InvalidResponseCodeException extends HttpDataSourceException {
        public final Map<String, List<String>> headerFields;
        public final int responseCode;

        public InvalidResponseCodeException(int responseCode, Map<String, List<String>> headerFields, DataSpec dataSpec) {
            super("Response code: " + responseCode, dataSpec);
            this.responseCode = responseCode;
            this.headerFields = headerFields;
        }
    }

    void clearAllRequestProperties();

    void clearRequestProperty(String str);

    void close() throws HttpDataSourceException;

    Map<String, List<String>> getResponseHeaders();

    long open(DataSpec dataSpec) throws HttpDataSourceException;

    int read(byte[] bArr, int i, int i2) throws HttpDataSourceException;

    void setRequestProperty(String str, String str2);
}
