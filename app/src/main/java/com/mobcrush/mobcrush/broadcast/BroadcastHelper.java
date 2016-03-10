package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Response.Listener;
import com.mobcrush.mobcrush.BuildConfig;
import com.mobcrush.mobcrush.Mobcrush;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.datamodel.BroadcastData;
import com.mobcrush.mobcrush.network.Network;

@TargetApi(21)
public class BroadcastHelper {
    private static final int DISPLAY_HEIGHT = 720;
    private static final int DISPLAY_WIDTH = 1280;
    private static final String SEPARATOR = "/";
    private static final String TAG = "BroadcastHelper";
    private boolean isBroadcasting;
    private boolean isCameraSwapped;
    private boolean isMicEnabled = true;
    private boolean isPrivacyEnabled;
    private String mBroadcastId;
    private BroadcastStatusCallback mCallback;
    private MobcrushCameraManager mCameraMgr;
    private String mChatRoomId;
    private Handler mHandler = new Handler();
    private MediaProjection mMediaProjection;
    private Intent mProjectionIntent;
    private MobcrushProjectionManager mProjectionManager;
    private int mResultCode;
    private int mScreenDensity;
    private Service mService;
    private String mStreamKey;

    public interface BroadcastStatusCallback {
        void onBroadcastEnded();

        void onBroadcastStarted();
    }

    public BroadcastHelper(Service service, int screenDensity, String streamKey, Intent projectionIntent, int resultCode, BroadcastStatusCallback callback) {
        this.mStreamKey = streamKey;
        this.mScreenDensity = screenDensity;
        this.mService = service;
        this.mProjectionIntent = projectionIntent;
        this.mResultCode = resultCode;
        this.mCallback = callback;
    }

    public boolean isBroadcasting() {
        return this.isBroadcasting;
    }

    public boolean isCameraEnabled() {
        return this.mCameraMgr != null;
    }

    public boolean isMicEnabled() {
        return this.isMicEnabled;
    }

    public boolean isPrivacyEnabled() {
        return this.isPrivacyEnabled;
    }

    public boolean isCameraSwapped() {
        return this.isCameraSwapped;
    }

    public void startBroadcast(Callback chatCallback, final BroadcastStatusCallback broadcastStatus, String title, String game) {
        if (!this.isBroadcasting) {
            this.isBroadcasting = true;
            Mobcrush.start(DISPLAY_WIDTH, DISPLAY_HEIGHT, this.mService.getResources().getString(R.string.rtmp_url) + BuildConfig.VERSION_NAME + SEPARATOR + this.mStreamKey);
            if (this.isPrivacyEnabled) {
                Mobcrush.setPrivacyFilter(true);
            }
            if (!this.isMicEnabled) {
                Mobcrush.setMuteMic(true);
            }
            if (this.isCameraSwapped) {
                Mobcrush.setSwap(true);
            }
            if (this.mMediaProjection == null) {
                this.mMediaProjection = ((MediaProjectionManager) this.mService.getSystemService("media_projection")).getMediaProjection(this.mResultCode, this.mProjectionIntent);
            }
            this.mProjectionManager = new MobcrushProjectionManager();
            this.mProjectionManager.startScreenCapture(DISPLAY_WIDTH, DISPLAY_HEIGHT, this.mScreenDensity, this.mMediaProjection);
            this.mHandler.post(new Runnable() {
                public void run() {
                    broadcastStatus.onBroadcastStarted();
                    BroadcastHelper.this.mCallback.onBroadcastStarted();
                }
            });
            final BroadcastStatusCallback broadcastStatusCallback = broadcastStatus;
            final String str = title;
            final String str2 = game;
            final Callback callback = chatCallback;
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Network.latestBroadcast(null, BroadcastHelper.this.mStreamKey, new Listener<BroadcastData>() {
                        public void onResponse(BroadcastData response) {
                            Log.d(BroadcastHelper.TAG, "Network.latestBroadcast: " + response.toString());
                            BroadcastHelper.this.mBroadcastId = response.broadcastId;
                            BroadcastHelper.this.mChatRoomId = response.chatRoomId;
                            if (response == null || BroadcastHelper.this.mBroadcastId == null || BroadcastHelper.this.mChatRoomId == null) {
                                Toast.makeText(BroadcastHelper.this.mService.getApplicationContext(), R.string.broadcast_error, 1).show();
                                broadcastStatusCallback.onBroadcastEnded();
                                BroadcastHelper.this.mCallback.onBroadcastEnded();
                                return;
                            }
                            BroadcastHelper.this.updateBroadcast(str, str2);
                            if (callback != null) {
                                callback.handleMessage(Message.obtain());
                            }
                        }
                    }, null);
                }
            }, 3000);
        }
    }

    public void updateBroadcast(String title, String game) {
        if (this.isBroadcasting) {
            Network.setBroadcastInfo(null, this.mBroadcastId, title, game, null, null);
        }
    }

    public void endBroadcast() {
        if (this.isBroadcasting) {
            if (this.isCameraSwapped) {
                Mobcrush.setSwap(false);
            }
            this.mCallback.onBroadcastEnded();
            this.isBroadcasting = false;
            if (this.mProjectionManager != null) {
                this.mProjectionManager.stopScreenCapture();
                this.mProjectionManager = null;
            }
            if (this.mMediaProjection != null) {
                this.mMediaProjection.stop();
                this.mMediaProjection = null;
            }
            Mobcrush.stop();
            Network.endBroadcast(null, this.mBroadcastId, null, null);
        }
    }

    public void startCamera(AutoFitTextureView preview) {
        if (this.mCameraMgr == null) {
            this.mCameraMgr = new MobcrushCameraManager();
        }
        Mobcrush.startCamera();
        this.mCameraMgr.startCameraCapture(this.mService, preview);
    }

    public void stopCamera() {
        if (this.mCameraMgr != null) {
            if (this.isCameraSwapped) {
                setCameraSwapped(false);
            }
            this.mCameraMgr.stopCameraCapture();
            this.mCameraMgr = null;
            Mobcrush.stopCamera();
        }
    }

    public void setCameraSwapped(boolean enable) {
        if (this.isCameraSwapped != enable) {
            this.isCameraSwapped = enable;
            Mobcrush.setSwap(enable);
        }
    }

    public void setMicEnabled(boolean enable) {
        if (this.isMicEnabled != enable) {
            this.isMicEnabled = enable;
            if (isBroadcasting()) {
                Mobcrush.setMuteMic(!enable);
            }
        }
    }

    public void setPrivacyMode(boolean enable) {
        if (this.isPrivacyEnabled != enable) {
            this.isPrivacyEnabled = enable;
            if (isBroadcasting()) {
                Mobcrush.setPrivacyFilter(enable);
            }
        }
    }

    public String getChatRoomId() {
        return this.mChatRoomId;
    }
}
