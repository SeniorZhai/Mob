package com.google.android.exoplayer.upstream;

import android.text.TextUtils;
import android.util.Log;
import com.google.android.exoplayer.upstream.HttpDataSource.HttpDataSourceException;
import com.google.android.exoplayer.upstream.HttpDataSource.InvalidContentTypeException;
import com.google.android.exoplayer.upstream.HttpDataSource.InvalidResponseCodeException;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.android.exoplayer.util.Predicate;
import com.google.android.exoplayer.util.Util;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

public class DefaultHttpDataSource implements HttpDataSource {
    private static final Pattern CONTENT_RANGE_HEADER = Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$");
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 8000;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 8000;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "HttpDataSource";
    private static final AtomicReference<byte[]> skipBufferReference = new AtomicReference();
    private final boolean allowCrossProtocolRedirects;
    private long bytesRead;
    private long bytesSkipped;
    private long bytesToRead;
    private long bytesToSkip;
    private final int connectTimeoutMillis;
    private HttpURLConnection connection;
    private final Predicate<String> contentTypePredicate;
    private DataSpec dataSpec;
    private InputStream inputStream;
    private final TransferListener listener;
    private boolean opened;
    private final int readTimeoutMillis;
    private final HashMap<String, String> requestProperties;
    private final String userAgent;

    public DefaultHttpDataSource(String userAgent, Predicate<String> contentTypePredicate) {
        this(userAgent, contentTypePredicate, null);
    }

    public DefaultHttpDataSource(String userAgent, Predicate<String> contentTypePredicate, TransferListener listener) {
        this(userAgent, contentTypePredicate, listener, DEFAULT_READ_TIMEOUT_MILLIS, DEFAULT_READ_TIMEOUT_MILLIS);
    }

    public DefaultHttpDataSource(String userAgent, Predicate<String> contentTypePredicate, TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis) {
        this(userAgent, contentTypePredicate, listener, connectTimeoutMillis, readTimeoutMillis, false);
    }

    public DefaultHttpDataSource(String userAgent, Predicate<String> contentTypePredicate, TransferListener listener, int connectTimeoutMillis, int readTimeoutMillis, boolean allowCrossProtocolRedirects) {
        this.userAgent = Assertions.checkNotEmpty(userAgent);
        this.contentTypePredicate = contentTypePredicate;
        this.listener = listener;
        this.requestProperties = new HashMap();
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
    }

    public String getUri() {
        return this.connection == null ? null : this.connection.getURL().toString();
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.connection == null ? null : this.connection.getHeaderFields();
    }

    public void setRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (this.requestProperties) {
            this.requestProperties.put(name, value);
        }
    }

    public void clearRequestProperty(String name) {
        Assertions.checkNotNull(name);
        synchronized (this.requestProperties) {
            this.requestProperties.remove(name);
        }
    }

    public void clearAllRequestProperties() {
        synchronized (this.requestProperties) {
            this.requestProperties.clear();
        }
    }

    public long open(DataSpec dataSpec) throws HttpDataSourceException {
        long j = 0;
        this.dataSpec = dataSpec;
        this.bytesRead = 0;
        this.bytesSkipped = 0;
        try {
            this.connection = makeConnection(dataSpec);
            try {
                int responseCode = this.connection.getResponseCode();
                if (responseCode < HttpStatus.SC_OK || responseCode > 299) {
                    Map<String, List<String>> headers = this.connection.getHeaderFields();
                    closeConnection();
                    throw new InvalidResponseCodeException(responseCode, headers, dataSpec);
                }
                String contentType = this.connection.getContentType();
                if (this.contentTypePredicate == null || this.contentTypePredicate.evaluate(contentType)) {
                    if (responseCode == HttpStatus.SC_OK && dataSpec.position != 0) {
                        j = dataSpec.position;
                    }
                    this.bytesToSkip = j;
                    if ((dataSpec.flags & 1) == 0) {
                        long contentLength = getContentLength(this.connection);
                        j = dataSpec.length != -1 ? dataSpec.length : contentLength != -1 ? contentLength - this.bytesToSkip : -1;
                        this.bytesToRead = j;
                    } else {
                        this.bytesToRead = dataSpec.length;
                    }
                    try {
                        this.inputStream = this.connection.getInputStream();
                        this.opened = true;
                        if (this.listener != null) {
                            this.listener.onTransferStart();
                        }
                        return this.bytesToRead;
                    } catch (IOException e) {
                        closeConnection();
                        throw new HttpDataSourceException(e, dataSpec);
                    }
                }
                closeConnection();
                throw new InvalidContentTypeException(contentType, dataSpec);
            } catch (IOException e2) {
                closeConnection();
                throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e2, dataSpec);
            }
        } catch (IOException e22) {
            throw new HttpDataSourceException("Unable to connect to " + dataSpec.uri.toString(), e22, dataSpec);
        }
    }

    public int read(byte[] buffer, int offset, int readLength) throws HttpDataSourceException {
        try {
            skipInternal();
            return readInternal(buffer, offset, readLength);
        } catch (IOException e) {
            throw new HttpDataSourceException(e, this.dataSpec);
        }
    }

    public void close() throws HttpDataSourceException {
        try {
            if (this.inputStream != null) {
                Util.maybeTerminateInputStream(this.connection, bytesRemaining());
                this.inputStream.close();
                this.inputStream = null;
            }
            if (this.opened) {
                this.opened = false;
                if (this.listener != null) {
                    this.listener.onTransferEnd();
                }
                closeConnection();
            }
        } catch (IOException e) {
            throw new HttpDataSourceException(e, this.dataSpec);
        } catch (Throwable th) {
            if (this.opened) {
                this.opened = false;
                if (this.listener != null) {
                    this.listener.onTransferEnd();
                }
                closeConnection();
            }
        }
    }

    protected final HttpURLConnection getConnection() {
        return this.connection;
    }

    protected final long bytesSkipped() {
        return this.bytesSkipped;
    }

    protected final long bytesRead() {
        return this.bytesRead;
    }

    protected final long bytesRemaining() {
        return this.bytesToRead == -1 ? this.bytesToRead : this.bytesToRead - this.bytesRead;
    }

    private HttpURLConnection makeConnection(DataSpec dataSpec) throws IOException {
        boolean allowGzip;
        URL url = new URL(dataSpec.uri.toString());
        long position = dataSpec.position;
        long length = dataSpec.length;
        if ((dataSpec.flags & 1) != 0) {
            allowGzip = true;
        } else {
            allowGzip = false;
        }
        HttpURLConnection connection;
        if (this.allowCrossProtocolRedirects) {
            int i = 0;
            while (true) {
                int redirectCount = i + 1;
                if (i <= MAX_REDIRECTS) {
                    connection = configureConnection(url, position, length, allowGzip);
                    connection.setInstanceFollowRedirects(false);
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpStatus.SC_MULTIPLE_CHOICES && responseCode != HttpStatus.SC_MOVED_PERMANENTLY && responseCode != HttpStatus.SC_MOVED_TEMPORARILY && responseCode != HttpStatus.SC_SEE_OTHER && responseCode != HttpStatus.SC_TEMPORARY_REDIRECT && responseCode != 308) {
                        return connection;
                    }
                    String location = connection.getHeaderField(HttpHeaders.LOCATION);
                    connection.disconnect();
                    url = handleRedirect(url, location);
                    i = redirectCount;
                } else {
                    throw new NoRouteToHostException("Too many redirects: " + redirectCount);
                }
            }
        }
        connection = configureConnection(url, position, length, allowGzip);
        connection.connect();
        return connection;
    }

    private HttpURLConnection configureConnection(URL url, long position, long length, boolean allowGzip) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(this.connectTimeoutMillis);
        connection.setReadTimeout(this.readTimeoutMillis);
        connection.setDoOutput(false);
        synchronized (this.requestProperties) {
            for (Entry<String, String> property : this.requestProperties.entrySet()) {
                connection.setRequestProperty((String) property.getKey(), (String) property.getValue());
            }
        }
        if (!(position == 0 && length == -1)) {
            String rangeRequest = "bytes=" + position + "-";
            if (length != -1) {
                rangeRequest = rangeRequest + ((position + length) - 1);
            }
            connection.setRequestProperty(HttpHeaders.RANGE, rangeRequest);
        }
        connection.setRequestProperty(HTTP.USER_AGENT, this.userAgent);
        if (!allowGzip) {
            connection.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, HTTP.IDENTITY_CODING);
        }
        return connection;
    }

    private static URL handleRedirect(URL originalUrl, String location) throws IOException {
        if (location == null) {
            throw new ProtocolException("Null location redirect");
        }
        URL url = new URL(originalUrl, location);
        String protocol = url.getProtocol();
        if ("https".equals(protocol) || HttpHost.DEFAULT_SCHEME_NAME.equals(protocol)) {
            return url;
        }
        throw new ProtocolException("Unsupported protocol redirect: " + protocol);
    }

    private static long getContentLength(HttpURLConnection connection) {
        long contentLength = -1;
        String contentLengthHeader = connection.getHeaderField(HTTP.CONTENT_LEN);
        if (!TextUtils.isEmpty(contentLengthHeader)) {
            try {
                contentLength = Long.parseLong(contentLengthHeader);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Unexpected Content-Length [" + contentLengthHeader + "]");
            }
        }
        String contentRangeHeader = connection.getHeaderField(HttpHeaders.CONTENT_RANGE);
        if (TextUtils.isEmpty(contentRangeHeader)) {
            return contentLength;
        }
        Matcher matcher = CONTENT_RANGE_HEADER.matcher(contentRangeHeader);
        if (!matcher.find()) {
            return contentLength;
        }
        try {
            long contentLengthFromRange = (Long.parseLong(matcher.group(2)) - Long.parseLong(matcher.group(1))) + 1;
            if (contentLength < 0) {
                return contentLengthFromRange;
            }
            if (contentLength == contentLengthFromRange) {
                return contentLength;
            }
            Log.w(TAG, "Inconsistent headers [" + contentLengthHeader + "] [" + contentRangeHeader + "]");
            return Math.max(contentLength, contentLengthFromRange);
        } catch (NumberFormatException e2) {
            Log.e(TAG, "Unexpected Content-Range [" + contentRangeHeader + "]");
            return contentLength;
        }
    }

    private void skipInternal() throws IOException {
        if (this.bytesSkipped != this.bytesToSkip) {
            byte[] skipBuffer = (byte[]) skipBufferReference.getAndSet(null);
            if (skipBuffer == null) {
                skipBuffer = new byte[MpegAudioHeader.MAX_FRAME_SIZE_BYTES];
            }
            while (this.bytesSkipped != this.bytesToSkip) {
                int read = this.inputStream.read(skipBuffer, 0, (int) Math.min(this.bytesToSkip - this.bytesSkipped, (long) skipBuffer.length));
                if (Thread.interrupted()) {
                    throw new InterruptedIOException();
                } else if (read == -1) {
                    throw new EOFException();
                } else {
                    this.bytesSkipped += (long) read;
                    if (this.listener != null) {
                        this.listener.onBytesTransferred(read);
                    }
                }
            }
            skipBufferReference.set(skipBuffer);
        }
    }

    private int readInternal(byte[] buffer, int offset, int readLength) throws IOException {
        if (this.bytesToRead != -1) {
            readLength = (int) Math.min((long) readLength, this.bytesToRead - this.bytesRead);
        }
        if (readLength == 0) {
            return -1;
        }
        int read = this.inputStream.read(buffer, offset, readLength);
        if (read != -1) {
            this.bytesRead += (long) read;
            if (this.listener == null) {
                return read;
            }
            this.listener.onBytesTransferred(read);
            return read;
        } else if (this.bytesToRead == -1 || this.bytesToRead == this.bytesRead) {
            return -1;
        } else {
            throw new EOFException();
        }
    }

    private void closeConnection() {
        if (this.connection != null) {
            this.connection.disconnect();
            this.connection = null;
        }
    }
}
