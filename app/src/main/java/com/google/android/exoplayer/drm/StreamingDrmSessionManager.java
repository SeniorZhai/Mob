package com.google.android.exoplayer.drm;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.media.DeniedByServerException;
import android.media.MediaCrypto;
import android.media.MediaDrm;
import android.media.MediaDrm.OnEventListener;
import android.media.NotProvisionedException;
import android.media.UnsupportedSchemeException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.google.android.exoplayer.extractor.mp4.PsshAtomUtil;
import com.google.android.exoplayer.util.Util;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.util.HashMap;
import java.util.UUID;

@TargetApi(18)
public class StreamingDrmSessionManager implements DrmSessionManager {
    private static final int MSG_KEYS = 1;
    private static final int MSG_PROVISION = 0;
    public static final String PLAYREADY_CUSTOM_DATA_KEY = "PRCustomData";
    public static final UUID PLAYREADY_UUID = new UUID(-7348484286925749626L, -6083546864340672619L);
    public static final UUID WIDEVINE_UUID = new UUID(-1301668207276963122L, -6645017420763422227L);
    final MediaDrmCallback callback;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private Exception lastException;
    private MediaCrypto mediaCrypto;
    private final MediaDrm mediaDrm;
    final MediaDrmHandler mediaDrmHandler;
    private String mimeType;
    private int openCount;
    private final HashMap<String, String> optionalKeyRequestParameters;
    private Handler postRequestHandler;
    final PostResponseHandler postResponseHandler;
    private boolean provisioningInProgress;
    private HandlerThread requestHandlerThread;
    private byte[] schemeData;
    private byte[] sessionId;
    private int state;
    final UUID uuid;

    public interface EventListener {
        void onDrmSessionManagerError(Exception exception);
    }

    private class MediaDrmEventListener implements OnEventListener {
        private MediaDrmEventListener() {
        }

        public void onEvent(MediaDrm md, byte[] sessionId, int event, int extra, byte[] data) {
            StreamingDrmSessionManager.this.mediaDrmHandler.sendEmptyMessage(event);
        }
    }

    @SuppressLint({"HandlerLeak"})
    private class MediaDrmHandler extends Handler {
        public MediaDrmHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (StreamingDrmSessionManager.this.openCount == 0) {
                return;
            }
            if (StreamingDrmSessionManager.this.state == 3 || StreamingDrmSessionManager.this.state == 4) {
                switch (msg.what) {
                    case StreamingDrmSessionManager.MSG_KEYS /*1*/:
                        StreamingDrmSessionManager.this.state = 3;
                        StreamingDrmSessionManager.this.postProvisionRequest();
                        return;
                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                        StreamingDrmSessionManager.this.postKeyRequest();
                        return;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        StreamingDrmSessionManager.this.state = 3;
                        StreamingDrmSessionManager.this.onError(new KeysExpiredException());
                        return;
                    default:
                        return;
                }
            }
        }
    }

    @SuppressLint({"HandlerLeak"})
    private class PostRequestHandler extends Handler {
        public PostRequestHandler(Looper backgroundLooper) {
            super(backgroundLooper);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r6) {
            /*
            r5 = this;
            r2 = r6.what;	 Catch:{ Exception -> 0x000b }
            switch(r2) {
                case 0: goto L_0x001b;
                case 1: goto L_0x002c;
                default: goto L_0x0005;
            };	 Catch:{ Exception -> 0x000b }
        L_0x0005:
            r2 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x000b }
            r2.<init>();	 Catch:{ Exception -> 0x000b }
            throw r2;	 Catch:{ Exception -> 0x000b }
        L_0x000b:
            r0 = move-exception;
            r1 = r0;
        L_0x000d:
            r2 = com.google.android.exoplayer.drm.StreamingDrmSessionManager.this;
            r2 = r2.postResponseHandler;
            r3 = r6.what;
            r2 = r2.obtainMessage(r3, r1);
            r2.sendToTarget();
            return;
        L_0x001b:
            r2 = com.google.android.exoplayer.drm.StreamingDrmSessionManager.this;	 Catch:{ Exception -> 0x000b }
            r3 = r2.callback;	 Catch:{ Exception -> 0x000b }
            r2 = com.google.android.exoplayer.drm.StreamingDrmSessionManager.this;	 Catch:{ Exception -> 0x000b }
            r4 = r2.uuid;	 Catch:{ Exception -> 0x000b }
            r2 = r6.obj;	 Catch:{ Exception -> 0x000b }
            r2 = (android.media.MediaDrm.ProvisionRequest) r2;	 Catch:{ Exception -> 0x000b }
            r1 = r3.executeProvisionRequest(r4, r2);	 Catch:{ Exception -> 0x000b }
            goto L_0x000d;
        L_0x002c:
            r2 = com.google.android.exoplayer.drm.StreamingDrmSessionManager.this;	 Catch:{ Exception -> 0x000b }
            r3 = r2.callback;	 Catch:{ Exception -> 0x000b }
            r2 = com.google.android.exoplayer.drm.StreamingDrmSessionManager.this;	 Catch:{ Exception -> 0x000b }
            r4 = r2.uuid;	 Catch:{ Exception -> 0x000b }
            r2 = r6.obj;	 Catch:{ Exception -> 0x000b }
            r2 = (android.media.MediaDrm.KeyRequest) r2;	 Catch:{ Exception -> 0x000b }
            r1 = r3.executeKeyRequest(r4, r2);	 Catch:{ Exception -> 0x000b }
            goto L_0x000d;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer.drm.StreamingDrmSessionManager.PostRequestHandler.handleMessage(android.os.Message):void");
        }
    }

    @SuppressLint({"HandlerLeak"})
    private class PostResponseHandler extends Handler {
        public PostResponseHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StreamingDrmSessionManager.MSG_PROVISION /*0*/:
                    StreamingDrmSessionManager.this.onProvisionResponse(msg.obj);
                    return;
                case StreamingDrmSessionManager.MSG_KEYS /*1*/:
                    StreamingDrmSessionManager.this.onKeyResponse(msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public static StreamingDrmSessionManager newWidevineInstance(Looper playbackLooper, MediaDrmCallback callback, HashMap<String, String> optionalKeyRequestParameters, Handler eventHandler, EventListener eventListener) throws UnsupportedDrmException {
        return new StreamingDrmSessionManager(WIDEVINE_UUID, playbackLooper, callback, optionalKeyRequestParameters, eventHandler, eventListener);
    }

    public static StreamingDrmSessionManager newPlayReadyInstance(Looper playbackLooper, MediaDrmCallback callback, String customData, Handler eventHandler, EventListener eventListener) throws UnsupportedDrmException {
        HashMap<String, String> optionalKeyRequestParameters;
        if (TextUtils.isEmpty(customData)) {
            optionalKeyRequestParameters = null;
        } else {
            optionalKeyRequestParameters = new HashMap();
            optionalKeyRequestParameters.put(PLAYREADY_CUSTOM_DATA_KEY, customData);
        }
        return new StreamingDrmSessionManager(PLAYREADY_UUID, playbackLooper, callback, optionalKeyRequestParameters, eventHandler, eventListener);
    }

    public StreamingDrmSessionManager(UUID uuid, Looper playbackLooper, MediaDrmCallback callback, HashMap<String, String> optionalKeyRequestParameters, Handler eventHandler, EventListener eventListener) throws UnsupportedDrmException {
        this.uuid = uuid;
        this.callback = callback;
        this.optionalKeyRequestParameters = optionalKeyRequestParameters;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        try {
            this.mediaDrm = new MediaDrm(uuid);
            this.mediaDrm.setOnEventListener(new MediaDrmEventListener());
            this.mediaDrmHandler = new MediaDrmHandler(playbackLooper);
            this.postResponseHandler = new PostResponseHandler(playbackLooper);
            this.state = MSG_KEYS;
        } catch (UnsupportedSchemeException e) {
            throw new UnsupportedDrmException(MSG_KEYS, e);
        } catch (Exception e2) {
            throw new UnsupportedDrmException(2, e2);
        }
    }

    public final int getState() {
        return this.state;
    }

    public final MediaCrypto getMediaCrypto() {
        if (this.state == 3 || this.state == 4) {
            return this.mediaCrypto;
        }
        throw new IllegalStateException();
    }

    public boolean requiresSecureDecoderComponent(String mimeType) {
        if (this.state == 3 || this.state == 4) {
            return this.mediaCrypto.requiresSecureDecoderComponent(mimeType);
        }
        throw new IllegalStateException();
    }

    public final Exception getError() {
        return this.state == 0 ? this.lastException : null;
    }

    public final String getPropertyString(String key) {
        return this.mediaDrm.getPropertyString(key);
    }

    public final void setPropertyString(String key, String value) {
        this.mediaDrm.setPropertyString(key, value);
    }

    public final byte[] getPropertyByteArray(String key) {
        return this.mediaDrm.getPropertyByteArray(key);
    }

    public final void setPropertyByteArray(String key, byte[] value) {
        this.mediaDrm.setPropertyByteArray(key, value);
    }

    public final void open(DrmInitData drmInitData) {
        int i = this.openCount + MSG_KEYS;
        this.openCount = i;
        if (i == MSG_KEYS) {
            if (this.postRequestHandler == null) {
                this.requestHandlerThread = new HandlerThread("DrmRequestHandler");
                this.requestHandlerThread.start();
                this.postRequestHandler = new PostRequestHandler(this.requestHandlerThread.getLooper());
            }
            if (this.schemeData == null) {
                this.mimeType = drmInitData.mimeType;
                this.schemeData = drmInitData.get(this.uuid);
                if (this.schemeData == null) {
                    onError(new IllegalStateException("Media does not support uuid: " + this.uuid));
                    return;
                } else if (Util.SDK_INT < 21) {
                    byte[] psshData = PsshAtomUtil.parseSchemeSpecificData(this.schemeData, WIDEVINE_UUID);
                    if (psshData != null) {
                        this.schemeData = psshData;
                    }
                }
            }
            this.state = 2;
            openInternal(true);
        }
    }

    public final void close() {
        int i = this.openCount - 1;
        this.openCount = i;
        if (i == 0) {
            this.state = MSG_KEYS;
            this.provisioningInProgress = false;
            this.mediaDrmHandler.removeCallbacksAndMessages(null);
            this.postResponseHandler.removeCallbacksAndMessages(null);
            this.postRequestHandler.removeCallbacksAndMessages(null);
            this.postRequestHandler = null;
            this.requestHandlerThread.quit();
            this.requestHandlerThread = null;
            this.schemeData = null;
            this.mediaCrypto = null;
            this.lastException = null;
            if (this.sessionId != null) {
                this.mediaDrm.closeSession(this.sessionId);
                this.sessionId = null;
            }
        }
    }

    private void openInternal(boolean allowProvisioning) {
        try {
            this.sessionId = this.mediaDrm.openSession();
            this.mediaCrypto = new MediaCrypto(this.uuid, this.sessionId);
            this.state = 3;
            postKeyRequest();
        } catch (NotProvisionedException e) {
            if (allowProvisioning) {
                postProvisionRequest();
            } else {
                onError(e);
            }
        } catch (Exception e2) {
            onError(e2);
        }
    }

    private void postProvisionRequest() {
        if (!this.provisioningInProgress) {
            this.provisioningInProgress = true;
            this.postRequestHandler.obtainMessage(MSG_PROVISION, this.mediaDrm.getProvisionRequest()).sendToTarget();
        }
    }

    private void onProvisionResponse(Object response) {
        this.provisioningInProgress = false;
        if (this.state != 2 && this.state != 3 && this.state != 4) {
            return;
        }
        if (response instanceof Exception) {
            onError((Exception) response);
            return;
        }
        try {
            this.mediaDrm.provideProvisionResponse((byte[]) response);
            if (this.state == 2) {
                openInternal(false);
            } else {
                postKeyRequest();
            }
        } catch (DeniedByServerException e) {
            onError(e);
        }
    }

    private void postKeyRequest() {
        try {
            this.postRequestHandler.obtainMessage(MSG_KEYS, this.mediaDrm.getKeyRequest(this.sessionId, this.schemeData, this.mimeType, MSG_KEYS, this.optionalKeyRequestParameters)).sendToTarget();
        } catch (NotProvisionedException e) {
            onKeysError(e);
        }
    }

    private void onKeyResponse(Object response) {
        if (this.state != 3 && this.state != 4) {
            return;
        }
        if (response instanceof Exception) {
            onKeysError((Exception) response);
            return;
        }
        try {
            this.mediaDrm.provideKeyResponse(this.sessionId, (byte[]) response);
            this.state = 4;
        } catch (Exception e) {
            onKeysError(e);
        }
    }

    private void onKeysError(Exception e) {
        if (e instanceof NotProvisionedException) {
            postProvisionRequest();
        } else {
            onError(e);
        }
    }

    private void onError(final Exception e) {
        this.lastException = e;
        if (!(this.eventHandler == null || this.eventListener == null)) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    StreamingDrmSessionManager.this.eventListener.onDrmSessionManagerError(e);
                }
            });
        }
        if (this.state != 4) {
            this.state = MSG_PROVISION;
        }
    }
}
