package com.google.android.exoplayer.util;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Callback;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.upstream.UriDataSource;
import com.google.android.exoplayer.upstream.UriLoadable;
import com.google.android.exoplayer.upstream.UriLoadable.Parser;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.concurrent.CancellationException;

public class ManifestFetcher<T> implements Callback {
    private UriLoadable<T> currentLoadable;
    private int enabledCount;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private IOException loadException;
    private int loadExceptionCount;
    private long loadExceptionTimestamp;
    private Loader loader;
    private volatile T manifest;
    private volatile long manifestLoadTimestamp;
    volatile String manifestUri;
    private final Parser<T> parser;
    private final UriDataSource uriDataSource;

    public interface RedirectingManifest {
        String getNextManifestUri();
    }

    public interface EventListener {
        void onManifestError(IOException iOException);

        void onManifestRefreshStarted();

        void onManifestRefreshed();
    }

    public interface ManifestCallback<T> {
        void onSingleManifest(T t);

        void onSingleManifestError(IOException iOException);
    }

    private class SingleFetchHelper implements Callback {
        private final Looper callbackLooper;
        private final UriLoadable<T> singleUseLoadable;
        private final Loader singleUseLoader = new Loader("manifestLoader:single");
        private final ManifestCallback<T> wrappedCallback;

        public SingleFetchHelper(UriLoadable<T> singleUseLoadable, Looper callbackLooper, ManifestCallback<T> wrappedCallback) {
            this.singleUseLoadable = singleUseLoadable;
            this.callbackLooper = callbackLooper;
            this.wrappedCallback = wrappedCallback;
        }

        public void startLoading() {
            this.singleUseLoader.startLoading(this.callbackLooper, this.singleUseLoadable, this);
        }

        public void onLoadCompleted(Loadable loadable) {
            try {
                T result = this.singleUseLoadable.getResult();
                ManifestFetcher.this.onSingleFetchCompleted(result);
                this.wrappedCallback.onSingleManifest(result);
            } finally {
                releaseLoader();
            }
        }

        public void onLoadCanceled(Loadable loadable) {
            try {
                this.wrappedCallback.onSingleManifestError(new IOException("Load cancelled", new CancellationException()));
            } finally {
                releaseLoader();
            }
        }

        public void onLoadError(Loadable loadable, IOException exception) {
            try {
                this.wrappedCallback.onSingleManifestError(exception);
            } finally {
                releaseLoader();
            }
        }

        private void releaseLoader() {
            this.singleUseLoader.release();
        }
    }

    public ManifestFetcher(String manifestUri, UriDataSource uriDataSource, Parser<T> parser) {
        this(manifestUri, uriDataSource, parser, null, null);
    }

    public ManifestFetcher(String manifestUri, UriDataSource uriDataSource, Parser<T> parser, Handler eventHandler, EventListener eventListener) {
        this.parser = parser;
        this.manifestUri = manifestUri;
        this.uriDataSource = uriDataSource;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
    }

    public void updateManifestUri(String manifestUri) {
        this.manifestUri = manifestUri;
    }

    public void singleLoad(Looper callbackLooper, ManifestCallback<T> callback) {
        new SingleFetchHelper(new UriLoadable(this.manifestUri, this.uriDataSource, this.parser), callbackLooper, callback).startLoading();
    }

    public T getManifest() {
        return this.manifest;
    }

    public long getManifestLoadTimestamp() {
        return this.manifestLoadTimestamp;
    }

    public IOException getError() {
        if (this.loadExceptionCount <= 1) {
            return null;
        }
        return this.loadException;
    }

    public void enable() {
        int i = this.enabledCount;
        this.enabledCount = i + 1;
        if (i == 0) {
            this.loadExceptionCount = 0;
            this.loadException = null;
        }
    }

    public void disable() {
        int i = this.enabledCount - 1;
        this.enabledCount = i;
        if (i == 0 && this.loader != null) {
            this.loader.release();
            this.loader = null;
        }
    }

    public void requestRefresh() {
        if (this.loadException == null || SystemClock.elapsedRealtime() >= this.loadExceptionTimestamp + getRetryDelayMillis((long) this.loadExceptionCount)) {
            if (this.loader == null) {
                this.loader = new Loader("manifestLoader");
            }
            if (!this.loader.isLoading()) {
                this.currentLoadable = new UriLoadable(this.manifestUri, this.uriDataSource, this.parser);
                this.loader.startLoading(this.currentLoadable, this);
                notifyManifestRefreshStarted();
            }
        }
    }

    public void onLoadCompleted(Loadable loadable) {
        if (this.currentLoadable == loadable) {
            this.manifest = this.currentLoadable.getResult();
            this.manifestLoadTimestamp = SystemClock.elapsedRealtime();
            this.loadExceptionCount = 0;
            this.loadException = null;
            if (this.manifest instanceof RedirectingManifest) {
                String nextLocation = this.manifest.getNextManifestUri();
                if (!TextUtils.isEmpty(nextLocation)) {
                    this.manifestUri = nextLocation;
                }
            }
            notifyManifestRefreshed();
        }
    }

    public void onLoadCanceled(Loadable loadable) {
    }

    public void onLoadError(Loadable loadable, IOException exception) {
        if (this.currentLoadable == loadable) {
            this.loadExceptionCount++;
            this.loadExceptionTimestamp = SystemClock.elapsedRealtime();
            this.loadException = new IOException(exception);
            notifyManifestError(this.loadException);
        }
    }

    void onSingleFetchCompleted(T result) {
        this.manifest = result;
        this.manifestLoadTimestamp = SystemClock.elapsedRealtime();
    }

    private long getRetryDelayMillis(long errorCount) {
        return Math.min((errorCount - 1) * 1000, Constants.NOTIFICATION_BANNER_TIMEOUT);
    }

    private void notifyManifestRefreshStarted() {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ManifestFetcher.this.eventListener.onManifestRefreshStarted();
                }
            });
        }
    }

    private void notifyManifestRefreshed() {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ManifestFetcher.this.eventListener.onManifestRefreshed();
                }
            });
        }
    }

    private void notifyManifestError(final IOException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ManifestFetcher.this.eventListener.onManifestError(e);
                }
            });
        }
    }
}
