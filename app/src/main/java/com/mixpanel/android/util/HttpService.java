package com.mixpanel.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.facebook.internal.Utility;
import com.google.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.google.android.exoplayer.upstream.MulticastDataSource;
import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.util.RemoteService.ServiceUnavailableException;
import io.fabric.sdk.android.services.network.HttpRequest;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;

public class HttpService implements RemoteService {
    private static final String LOGTAG = "MixpanelAPI.Message";

    public boolean isOnline(Context context) {
        boolean isOnline;
        try {
            NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            isOnline = netInfo != null && netInfo.isConnectedOrConnecting();
            if (MPConfig.DEBUG) {
                Log.v(LOGTAG, "ConnectivityManager says we " + (isOnline ? "are" : "are not") + " online");
            }
        } catch (SecurityException e) {
            isOnline = true;
            if (MPConfig.DEBUG) {
                Log.v(LOGTAG, "Don't have permission to check connectivity, will assume we are online");
            }
        }
        return isOnline;
    }

    public byte[] performRequest(String endpointUrl, List<NameValuePair> params, SSLSocketFactory socketFactory) throws ServiceUnavailableException, IOException {
        Throwable th;
        EOFException e;
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, "Attempting request to " + endpointUrl);
        }
        byte[] response = null;
        int retries = 0;
        boolean succeeded = false;
        while (retries < 3 && !succeeded) {
            InputStream in = null;
            OutputStream out = null;
            BufferedOutputStream bufferedOutputStream = null;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(endpointUrl).openConnection();
                if (socketFactory != null && (connection instanceof HttpsURLConnection)) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
                }
                connection.setConnectTimeout(MulticastDataSource.DEFAULT_MAX_PACKET_SIZE);
                connection.setReadTimeout(AdaptiveEvaluator.DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS);
                if (params != null) {
                    connection.setDoOutput(true);
                    UrlEncodedFormEntity form = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                    connection.setRequestMethod(HttpRequest.METHOD_POST);
                    connection.setFixedLengthStreamingMode((int) form.getContentLength());
                    out = connection.getOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream(out);
                    try {
                        form.writeTo(bout);
                        bout.close();
                        bufferedOutputStream = null;
                        out.close();
                        out = null;
                    } catch (EOFException e2) {
                        bufferedOutputStream = bout;
                        try {
                            if (MPConfig.DEBUG) {
                                Log.d(LOGTAG, "Failure to connect, likely caused by a known issue with Android lib. Retrying.");
                            }
                            retries++;
                            if (bufferedOutputStream != null) {
                                try {
                                    bufferedOutputStream.close();
                                } catch (IOException e3) {
                                }
                            }
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e4) {
                                }
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e5) {
                                }
                            }
                            if (connection != null) {
                                connection.disconnect();
                            }
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } catch (IOException e6) {
                        e = e6;
                        bufferedOutputStream = bout;
                    } catch (Throwable th3) {
                        th = th3;
                        bufferedOutputStream = bout;
                    }
                }
                in = connection.getInputStream();
                response = slurp(in);
                in.close();
                in = null;
                succeeded = true;
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e7) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e8) {
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e9) {
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (EOFException e10) {
                if (MPConfig.DEBUG) {
                    Log.d(LOGTAG, "Failure to connect, likely caused by a known issue with Android lib. Retrying.");
                }
                retries++;
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e11) {
                e = e11;
            }
        }
        if (MPConfig.DEBUG && retries >= 3) {
            Log.v(LOGTAG, "Could not connect to Mixpanel service after three retries.");
        }
        return response;
        if (connection != null) {
            connection.disconnect();
        }
        throw th;
        if (bufferedOutputStream != null) {
            try {
                bufferedOutputStream.close();
            } catch (IOException e12) {
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e13) {
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e14) {
            }
        }
        if (connection != null) {
            connection.disconnect();
        }
        throw th;
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
        throw th;
        if (HttpStatus.SC_SERVICE_UNAVAILABLE == connection.getResponseCode()) {
            throw new ServiceUnavailableException("Service Unavailable", connection.getHeaderField(HttpHeaders.RETRY_AFTER));
        }
        throw e;
        if (in != null) {
            in.close();
        }
        if (connection != null) {
            connection.disconnect();
        }
        throw th;
    }

    private static byte[] slurp(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[Utility.DEFAULT_STREAM_BUFFER_SIZE];
        while (true) {
            int nRead = inputStream.read(data, 0, data.length);
            if (nRead != -1) {
                buffer.write(data, 0, nRead);
            } else {
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }
}
