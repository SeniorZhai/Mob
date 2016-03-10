package com.mobcrush.mobcrush;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.media.MediaDrm.KeyRequest;
import android.media.MediaDrm.ProvisionRequest;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.android.volley.DefaultRetryPolicy;
import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.MediaDrmCallback;
import com.google.android.exoplayer.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.text.Cue;
import com.mobcrush.mobcrush.common.OrientationManager;
import com.mobcrush.mobcrush.common.OrientationManager.OrientationChangeListener;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.DashRendererBuilder;
import com.mobcrush.mobcrush.player.EventLogger;
import com.mobcrush.mobcrush.player.ExtractorRendererBuilder;
import com.mobcrush.mobcrush.player.HlsRendererBuilder;
import com.mobcrush.mobcrush.player.Player;
import com.mobcrush.mobcrush.player.Player.CaptionListener;
import com.mobcrush.mobcrush.player.Player.Id3MetadataListener;
import com.mobcrush.mobcrush.player.Player.Listener;
import com.mobcrush.mobcrush.player.Player.RendererBuilder;
import com.mobcrush.mobcrush.player.PlayerUtil;
import com.mobcrush.mobcrush.player.VideoControllerView;
import com.mobcrush.mobcrush.player.VideoControllerView.MediaPlayerControl;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class PlayerFragment extends ChatFragment implements Callback, Listener, Id3MetadataListener, MediaPlayerControl, OrientationChangeListener, AudioCapabilitiesReceiver.Listener, CaptionListener {
    private static final String TAG = "ExoPlayer";
    private AudioCapabilities mAudioCapabilities;
    private AudioCapabilitiesReceiver mAudioCapabilitiesReceiver;
    private boolean mAudioCapabilitiesReceiverRegistered;
    protected BroadcastReceiver mBroadcastReceiver;
    protected EventLogger mEventLogger;
    protected boolean mFirstPreparation = true;
    protected boolean mFullScreenMode = false;
    protected boolean mIsBusy;
    protected boolean mOrientationIsLocked;
    protected OrientationManager mOrientationManager;
    protected float mPixelWidthAspectRatio;
    protected Player mPlayer;
    protected int mPlayerDuration;
    protected boolean mPlayerNeedsPrepare = true;
    protected boolean mPlayerNeedsToRestorePlaying = true;
    protected int mPlayerPosition;
    private String mPrevExceptionMessage;
    protected int mRequiredOrientation;
    protected boolean mShouldReleasePlayer = true;
    protected SurfaceView mSurfaceView;
    protected VideoControllerView mVideoController;
    protected int mVideoHeight;
    protected RelativeLayout mVideoLayout;
    protected Uri mVideoUri;
    protected int mVideoWidth;

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mOrientationManager = OrientationManager.getInstance(getActivity());
        this.mOrientationManager.setOrientationChangedListener(this);
        this.mAudioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getActivity().getApplicationContext(), this);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPlayerPosition = -1;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction(Constants.ACTION_PAUSE_PLAYER);
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                        Log.d(PlayerFragment.TAG, "android.intent.action.SCREEN_OFF");
                        PlayerFragment.this.mPlayerNeedsToRestorePlaying = PlayerFragment.this.isPlaying();
                        PlayerFragment.this.pause();
                    } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                        Log.d(PlayerFragment.TAG, "android.intent.action.USER_PRESENT");
                        if (PlayerFragment.this.mPlayerNeedsToRestorePlaying) {
                            PlayerFragment.this.mPlayerNeedsToRestorePlaying = false;
                        }
                    } else if ("android.intent.action.PHONE_STATE".equals(intent.getAction())) {
                        Log.d(PlayerFragment.TAG, intent.getStringExtra("state"));
                        if (TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra("state"))) {
                            PlayerFragment.this.mPlayerNeedsToRestorePlaying = PlayerFragment.this.isPlaying();
                            PlayerFragment.this.pause();
                        } else if ((TelephonyManager.EXTRA_STATE_IDLE.equals(intent.getStringExtra("state")) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getStringExtra("state"))) && PlayerFragment.this.mPlayerNeedsToRestorePlaying) {
                            PlayerFragment.this.mPlayerNeedsToRestorePlaying = false;
                        }
                    } else if (Constants.ACTION_PAUSE_PLAYER.equals(intent.getAction())) {
                        PlayerFragment.this.pausePlayer();
                    }
                }
            };
            getActivity().registerReceiver(this.mBroadcastReceiver, intentFilter);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mPlayer == null) {
            try {
                if (VERSION.SDK_INT >= 21) {
                    Crashlytics.log("Register receiver " + this.mAudioCapabilitiesReceiver);
                    this.mAudioCapabilitiesReceiver.register();
                    this.mAudioCapabilitiesReceiverRegistered = true;
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mShouldReleasePlayer) {
            pausePlayer();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mBroadcastReceiver != null && isAdded()) {
            getActivity().unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
    }

    public void pausePlayer() {
        releasePlayer();
        try {
            if (VERSION.SDK_INT >= 21 && this.mAudioCapabilitiesReceiverRegistered) {
                this.mAudioCapabilitiesReceiverRegistered = false;
                Crashlytics.log("Unregister receiver " + this.mAudioCapabilitiesReceiver);
                this.mAudioCapabilitiesReceiver.unregister();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }
    }

    private void preparePlayer() {
        preparePlayer(this.mSurfaceView, this.mVideoUri);
    }

    protected void preparePlayer(SurfaceView surfaceView, Uri videoUri) {
        if (this.mPlayer != null) {
            Log.i(TAG, "preparePlayer for " + videoUri + "; player is already prepared for " + this.mVideoUri);
            return;
        }
        Log.d(TAG, "preparePlayer");
        try {
            this.mSurfaceView = surfaceView;
            this.mSurfaceView.getHolder().addCallback(this);
            this.mVideoUri = videoUri;
            if (this.mVideoUri != null && this.mSurfaceView != null) {
                if (this.mPlayer == null) {
                    this.mPlayer = new Player(getRendererBuilder());
                    this.mPlayer.addListener(this);
                    this.mPlayer.setCaptionListener(this);
                    this.mPlayer.setMetadataListener(this);
                    this.mEventLogger = new EventLogger();
                    this.mEventLogger.startSession();
                    this.mPlayer.addListener(this.mEventLogger);
                    this.mPlayer.setInfoListener(this.mEventLogger);
                    this.mPlayer.setInternalErrorListener(this.mEventLogger);
                }
                if (this.mFirstPreparation) {
                    this.mPlayer.prepare();
                    this.mFirstPreparation = false;
                }
                this.mPlayer.setSurface(this.mSurfaceView.getHolder().getSurface());
                if (getView() != null && this.mVideoLayout == null) {
                    this.mVideoLayout = (RelativeLayout) getView().findViewById(R.id.video_layout);
                }
                if (this.mVideoController == null && getActivity() != null) {
                    this.mVideoController = new VideoControllerView(getActivity(), false);
                }
                if (this.mVideoController != null) {
                    this.mVideoController.setMediaPlayer(this);
                    this.mVideoController.setEnabled(true);
                    this.mVideoController.setAnchorView(this.mVideoLayout);
                    this.mVideoController.show();
                    this.mVideoController.setVisibility(0);
                }
                maybeStartPlayback();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void maybeStartPlayback() {
        if (!this.mPlayerNeedsToRestorePlaying) {
            return;
        }
        if (this.mPlayer.getSurface().isValid() || this.mPlayer.getSelectedTrackIndex(0) == -1) {
            this.mPlayer.setPlayWhenReady(this.mPlayerNeedsToRestorePlaying);
            lockScreenOn();
            this.mPlayerNeedsToRestorePlaying = false;
        }
    }

    private void lockScreenOn() {
        try {
            getActivity().getWindow().addFlags(AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unlockScreenOn() {
        try {
            getActivity().getWindow().clearFlags(AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void releasePlayer() {
        Log.d(TAG, "releasePlayer");
        if (this.mPlayer != null) {
            this.mPlayerPosition = (int) this.mPlayer.getCurrentPosition();
            this.mPlayer.release();
            this.mPlayer = null;
            this.mPlayerNeedsPrepare = true;
            this.mPlayerNeedsToRestorePlaying = true;
            this.mFirstPreparation = true;
            this.mEventLogger.endSession();
            this.mEventLogger = null;
        }
        if (isAdded()) {
            unlockScreenOn();
        }
    }

    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        this.mAudioCapabilities = audioCapabilities;
        releasePlayer();
        preparePlayer();
    }

    protected RendererBuilder getRendererBuilder() {
        if (this.mVideoUri != null) {
            if (this.mVideoUri.toString().endsWith("mp4")) {
                return new ExtractorRendererBuilder(getActivity(), Network.getUserAgent(), this.mVideoUri, new Mp4Extractor());
            }
            if (this.mVideoUri.toString().endsWith("m3u8")) {
                return new HlsRendererBuilder(getActivity(), Network.getUserAgent(), this.mVideoUri.toString(), this.mAudioCapabilities);
            }
            if (this.mVideoUri.toString().endsWith("mpd")) {
                return new DashRendererBuilder(getActivity(), Network.getUserAgent(), this.mVideoUri.toString(), new MediaDrmCallback() {
                    @TargetApi(18)
                    public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest provisionRequest) throws Exception {
                        return PlayerUtil.executePost(provisionRequest.getDefaultUrl(), null, null);
                    }

                    @TargetApi(18)
                    public byte[] executeKeyRequest(UUID uuid, KeyRequest keyRequest) throws Exception {
                        return PlayerUtil.executePost(keyRequest.getDefaultUrl(), keyRequest.getData(), null);
                    }
                }, this.mAudioCapabilities);
            }
        }
        Crashlytics.logException(new IllegalArgumentException("Unsupported video uri: " + this.mVideoUri));
        return null;
    }

    public void onCues(List<Cue> list) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (this.mPlayer != null) {
            this.mPlayer.setSurface(holder.getSurface());
            maybeStartPlayback();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (this.mPlayer != null) {
            this.mPlayer.blockingClearSurface();
        }
    }

    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == 4) {
            if (this.mPlayerPosition != -1) {
                this.mIsBusy = true;
                seekTo(this.mPlayerPosition);
                this.mPlayerPosition = -1;
            } else {
                this.mIsBusy = false;
            }
        } else if (playbackState == 5) {
            Log.i(TAG, "state ended");
            this.mIsBusy = false;
            unlockScreenOn();
        } else if (playbackState == 2 || playbackState == 3) {
            this.mIsBusy = true;
        } else if (playbackState == 1) {
            this.mIsBusy = false;
        }
        if (this.mVideoController != null) {
            VideoControllerView videoControllerView = this.mVideoController;
            if (this.mIsBusy) {
                playbackState = 2;
            }
            videoControllerView.updateControls(playbackState);
        }
    }

    public void onError(Exception e) {
        this.mPlayerPosition = getCurrentPosition();
        this.mPlayerNeedsPrepare = true;
        Log.e(TAG, "Playback failed. Player started again", e);
        if (e.getMessage().equals(this.mPrevExceptionMessage)) {
            this.mIsBusy = false;
            onStateChanged(false, 5);
        } else {
            preparePlayer();
        }
        this.mPrevExceptionMessage = e.getMessage();
    }

    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        Log.d(TAG, String.format("onVideoSizeChanged(%d, %d, %f", new Object[]{Integer.valueOf(width), Integer.valueOf(height), Float.valueOf(pixelWidthAspectRatio)}));
        this.mVideoWidth = width;
        this.mVideoHeight = height;
        this.mPixelWidthAspectRatio = pixelWidthAspectRatio;
        correctVideoAspect(0);
        if (this.mVideoHeight > this.mVideoWidth) {
            this.mOrientationIsLocked = true;
            this.mRequiredOrientation = 1;
            getActivity().setRequestedOrientation(this.mRequiredOrientation);
        }
    }

    public void correctVideoAspect(int defaultHeight) {
        if (isAdded() && this.mVideoLayout != null) {
            Point size = UIUtils.getScreenSize(getActivity().getWindowManager());
            if (this.mVideoWidth == 0) {
                this.mVideoWidth = Math.max(size.x, size.y);
            }
            if (this.mPixelWidthAspectRatio == 0.0f) {
                this.mPixelWidthAspectRatio = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            }
            LayoutParams layoutParams;
            if (UIUtils.isLandscape(getActivity())) {
                layoutParams = this.mVideoLayout.getLayoutParams();
                layoutParams.height = -1;
                this.mVideoLayout.setLayoutParams(layoutParams);
                layoutParams = this.mSurfaceView.getLayoutParams();
                layoutParams.width = (int) (((((float) this.mVideoWidth) * this.mPixelWidthAspectRatio) * ((float) Math.min(size.x, size.y))) / ((float) this.mVideoHeight));
                this.mSurfaceView.setLayoutParams(layoutParams);
            } else {
                layoutParams = this.mSurfaceView.getLayoutParams();
                int videoHeight = defaultHeight != 0 ? defaultHeight : getResources().getDimensionPixelSize(R.dimen.game_video_height);
                if (defaultHeight != 0 && videoHeight > layoutParams.height) {
                    layoutParams.width = (int) (((((float) this.mVideoWidth) * this.mPixelWidthAspectRatio) * ((float) videoHeight)) / ((float) this.mVideoHeight));
                } else if (this.mVideoHeight == 0 || this.mVideoWidth == 0) {
                    layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.game_video_height);
                } else {
                    layoutParams.width = size.x;
                    layoutParams.height = (int) ((((double) this.mVideoHeight) * ((double) size.x)) / (((double) this.mVideoWidth) * ((double) this.mPixelWidthAspectRatio)));
                }
                int height = layoutParams.height;
                this.mSurfaceView.setLayoutParams(layoutParams);
                layoutParams = this.mVideoLayout.getLayoutParams();
                layoutParams.height = height;
                this.mVideoLayout.setLayoutParams(layoutParams);
            }
            if (this.mVideoController != null) {
                this.mVideoController.updateFullScreen();
            }
        }
    }

    public void onId3Metadata(Map<String, Object> metadata) {
        for (int i = 0; i < metadata.size(); i++) {
            if (metadata.containsKey(TxxxMetadata.TYPE)) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) metadata.get(TxxxMetadata.TYPE);
                Log.i(TAG, String.format("ID3 TimedMetadata: description=%s, value=%s", new Object[]{txxxMetadata.description, txxxMetadata.value}));
            }
        }
    }

    public void start() {
        if (this.mPlayer == null) {
            return;
        }
        if (this.mPlayerNeedsPrepare) {
            this.mPlayerNeedsPrepare = false;
            this.mPlayer.setPlayWhenReady(true);
            this.mPlayer.prepare();
            return;
        }
        this.mPlayer.getPlayerControl().start();
    }

    public void pause() {
        if (this.mPlayer != null) {
            this.mPlayer.getPlayerControl().pause();
            this.mPlayerPosition = (int) this.mPlayer.getCurrentPosition();
        }
    }

    public int getDuration() {
        if (this.mPlayerDuration != 0) {
            return this.mPlayerDuration;
        }
        if (this.mPlayer != null) {
            this.mPlayerDuration = this.mPlayer.getPlayerControl().getDuration();
        }
        return this.mPlayerDuration;
    }

    public int getCurrentPosition() {
        int pos = 0;
        if (this.mPlayer != null) {
            pos = this.mPlayer.getPlayerControl().getCurrentPosition();
            if (pos != 0) {
                return pos;
            }
        }
        return Math.max(pos, this.mPlayerPosition);
    }

    public boolean isBusy() {
        return this.mIsBusy;
    }

    public void seekTo(int pos) {
        if (this.mPlayer != null) {
            this.mIsBusy = true;
            this.mPlayer.getPlayerControl().seekTo(pos);
        }
    }

    public boolean isPlaying() {
        if (this.mPlayer != null) {
            return this.mPlayer.getPlayerControl().isPlaying();
        }
        return false;
    }

    public int getBufferPercentage() {
        if (this.mPlayer != null) {
            return this.mPlayer.getPlayerControl().getBufferPercentage();
        }
        return 0;
    }

    public boolean canPause() {
        if (this.mPlayer != null) {
            return this.mPlayer.getPlayerControl().canPause();
        }
        return false;
    }

    public boolean canSeekBackward() {
        if (this.mPlayer != null) {
            return this.mPlayer.getPlayerControl().canSeekBackward();
        }
        return false;
    }

    public boolean canSeekForward() {
        if (this.mPlayer != null) {
            return this.mPlayer.getPlayerControl().canSeekForward();
        }
        return false;
    }

    public boolean isFullScreen() {
        FragmentActivity a = getActivity();
        if (a == null) {
            return false;
        }
        int orientation = a.getRequestedOrientation();
        if (orientation == 6 || orientation == 0 || ((UIUtils.isLandscape(a) && orientation == -1) || this.mFullScreenMode)) {
            return true;
        }
        return false;
    }

    public void toggleFullScreen() {
        boolean z = true;
        int i = -1;
        FragmentActivity a = getActivity();
        if (a != null) {
            Log.d(TAG, "toggleFullScreen");
            boolean fullScreen = isFullScreen();
            if (this.mVideoHeight < this.mVideoWidth) {
                if (this.mRequiredOrientation == -1) {
                    i = fullScreen ? 1 : 2;
                }
                this.mRequiredOrientation = i;
                a.setRequestedOrientation(fullScreen ? 7 : 6);
            }
            if (fullScreen) {
                z = false;
            }
            this.mFullScreenMode = z;
            if (this.mVideoController != null) {
                this.mVideoController.updateControls();
            }
        }
    }

    public void onOrientationChanged(int newOrientation) {
        FragmentActivity a = getActivity();
        if (a != null) {
            boolean fullScreen = isFullScreen();
            if (this.mRequiredOrientation == -1) {
                if (!Utils.isOrientationLocked(getActivity()) && !this.mOrientationIsLocked) {
                    a.setRequestedOrientation(newOrientation != 2 ? 7 : 6);
                }
            } else if (!this.mOrientationIsLocked) {
                if ((newOrientation == 2 && fullScreen) || (newOrientation == 1 && !fullScreen)) {
                    this.mRequiredOrientation = -1;
                }
            }
        }
    }
}
