package com.google.android.exoplayer.upstream;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.exoplayer.util.Assertions;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;

public final class DefaultUriDataSource implements UriDataSource {
    private static final String SCHEME_ASSET = "asset";
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_FILE = "file";
    private final UriDataSource assetDataSource;
    private final UriDataSource contentDataSource;
    private UriDataSource dataSource;
    private final UriDataSource fileDataSource;
    private final UriDataSource httpDataSource;

    public DefaultUriDataSource(Context context, String userAgent) {
        this(context, null, userAgent, false);
    }

    public DefaultUriDataSource(Context context, TransferListener listener, String userAgent) {
        this(context, listener, userAgent, false);
    }

    public DefaultUriDataSource(Context context, TransferListener listener, String userAgent, boolean allowCrossProtocolRedirects) {
        this(context, listener, new DefaultHttpDataSource(userAgent, null, listener, SettingsJsonConstants.ANALYTICS_MAX_BYTE_SIZE_PER_FILE_DEFAULT, SettingsJsonConstants.ANALYTICS_MAX_BYTE_SIZE_PER_FILE_DEFAULT, allowCrossProtocolRedirects));
    }

    public DefaultUriDataSource(Context context, TransferListener listener, UriDataSource httpDataSource) {
        this.httpDataSource = (UriDataSource) Assertions.checkNotNull(httpDataSource);
        this.fileDataSource = new FileDataSource(listener);
        this.assetDataSource = new AssetDataSource(context, listener);
        this.contentDataSource = new ContentDataSource(context, listener);
    }

    public long open(DataSpec dataSpec) throws IOException {
        Assertions.checkState(this.dataSource == null);
        String scheme = dataSpec.uri.getScheme();
        if (SCHEME_FILE.equals(scheme) || TextUtils.isEmpty(scheme)) {
            if (dataSpec.uri.getPath().startsWith("/android_asset/")) {
                this.dataSource = this.assetDataSource;
            } else {
                this.dataSource = this.fileDataSource;
            }
        } else if (SCHEME_ASSET.equals(scheme)) {
            this.dataSource = this.assetDataSource;
        } else if (SCHEME_CONTENT.equals(scheme)) {
            this.dataSource = this.contentDataSource;
        } else {
            this.dataSource = this.httpDataSource;
        }
        return this.dataSource.open(dataSpec);
    }

    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return this.dataSource.read(buffer, offset, readLength);
    }

    public String getUri() {
        return this.dataSource == null ? null : this.dataSource.getUri();
    }

    public void close() throws IOException {
        if (this.dataSource != null) {
            try {
                this.dataSource.close();
            } finally {
                this.dataSource = null;
            }
        }
    }
}
