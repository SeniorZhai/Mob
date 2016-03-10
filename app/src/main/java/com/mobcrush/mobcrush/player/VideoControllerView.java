package com.mobcrush.mobcrush.player;

import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer.DefaultLoadControl;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.LoginActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.helper.ShareHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class VideoControllerView extends FrameLayout {
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SHOW_TIME_FOR_LIVE = 3;
    private static final String TAG = "VideoControllerView";
    private static final int sDefaultTimeout = 0;
    private boolean mAdded;
    private ViewGroup mAnchor;
    private ImageButton mChatButton;
    private OnClickListener mChatListener;
    private Context mContext;
    private TextView mCurrentTime;
    private boolean mDragging;
    private TextView mEndTime;
    private ImageButton mFfwdButton;
    private OnClickListener mFfwdListener;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    private boolean mFromXml;
    private ImageButton mFullscreenButton;
    private boolean mFullscreenDisabled;
    private OnClickListener mFullscreenListener;
    private Handler mHandler;
    private boolean mListenersSet;
    private boolean mLiveMode;
    private long mLiveStartTime;
    private OnItemClickListener mMenuItemClickListener;
    private ImageButton mNextButton;
    private OnClickListener mNextListener;
    private Callback mOnShareMenuClosed;
    private ImageButton mPauseButton;
    private OnClickListener mPauseListener;
    private MediaPlayerControl mPlayerControl;
    private ImageButton mPrevButton;
    private OnClickListener mPrevListener;
    private ProgressBar mProgress;
    private View mProgressLayout;
    private ImageButton mRewButton;
    private OnClickListener mRewListener;
    private View mRoot;
    private OnSeekBarChangeListener mSeekListener;
    private ImageButton mShareButton;
    private OnClickListener mShareListener;
    private boolean mShowing;
    private boolean mUseFastForward;
    private int mViewersCount;

    public interface MediaPlayerControl {
        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        int getBufferPercentage();

        int getCurrentPosition();

        int getDuration();

        boolean isBusy();

        boolean isChatAvailable();

        boolean isChatShowing();

        boolean isFullScreen();

        boolean isPlaying();

        void pause();

        void seekTo(int i);

        void start();

        void toggleChat();

        void toggleFullScreen();
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        MessageHandler(VideoControllerView view) {
            this.mView = new WeakReference(view);
        }

        public void handleMessage(Message msg) {
            VideoControllerView view = (VideoControllerView) this.mView.get();
            if (view != null && view.mPlayerControl != null) {
                int pos;
                switch (msg.what) {
                    case VideoControllerView.FADE_OUT /*1*/:
                        return;
                    case VideoControllerView.SHOW_PROGRESS /*2*/:
                        pos = view.setProgress();
                        removeMessages(VideoControllerView.SHOW_PROGRESS);
                        if (!view.mDragging && view.mShowing && view.mPlayerControl.isPlaying()) {
                            sendMessageDelayed(obtainMessage(VideoControllerView.SHOW_PROGRESS), (long) (1000 - (pos % Constants.UPDATE_COFIG_INTERVAL)));
                            return;
                        }
                        return;
                    case VideoControllerView.SHOW_TIME_FOR_LIVE /*3*/:
                        pos = view.updateCurrentTime();
                        removeMessages(VideoControllerView.SHOW_TIME_FOR_LIVE);
                        if (view.mShowing && view.mPlayerControl.isPlaying()) {
                            sendMessageDelayed(obtainMessage(VideoControllerView.SHOW_TIME_FOR_LIVE), (long) (1000 - (pos % Constants.UPDATE_COFIG_INTERVAL)));
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public VideoControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFullscreenListener = new OnClickListener() {
            public void onClick(View v) {
                VideoControllerView.this.doToggleFullscreen();
            }
        };
        this.mChatListener = new OnClickListener() {
            public void onClick(View v) {
                VideoControllerView.this.doToggleChat();
            }
        };
        this.mShareListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mContext == null || !PreferenceUtility.getUser().isGuest(VideoControllerView.this.mContext)) {
                    ShareHelper.showSharePopupMenu(v, VideoControllerView.this.mMenuItemClickListener, false, VideoControllerView.this.mOnShareMenuClosed);
                } else {
                    VideoControllerView.this.mContext.startActivity(LoginActivity.getIntent(VideoControllerView.this.mContext));
                }
            }
        };
        this.mPauseListener = new OnClickListener() {
            public void onClick(View v) {
                VideoControllerView.this.doPauseResume();
            }
        };
        this.mHandler = new MessageHandler(this);
        this.mRewListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mPlayerControl != null) {
                    VideoControllerView.this.mPlayerControl.seekTo(VideoControllerView.this.mPlayerControl.getCurrentPosition() - 5000);
                    VideoControllerView.this.setProgress();
                    VideoControllerView.this.show(0);
                }
            }
        };
        this.mFfwdListener = new OnClickListener() {
            public void onClick(View v) {
                if (VideoControllerView.this.mPlayerControl != null) {
                    VideoControllerView.this.mPlayerControl.seekTo(VideoControllerView.this.mPlayerControl.getCurrentPosition() + DefaultLoadControl.DEFAULT_LOW_WATERMARK_MS);
                    VideoControllerView.this.setProgress();
                    VideoControllerView.this.show(0);
                }
            }
        };
        this.mSeekListener = new OnSeekBarChangeListener() {
            private long mNewPosition;
            private int mProgress;

            public void onStartTrackingTouch(SeekBar bar) {
                VideoControllerView.this.show(3600000);
                VideoControllerView.this.mDragging = true;
                VideoControllerView.this.mHandler.removeMessages(VideoControllerView.SHOW_PROGRESS);
            }

            public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
                if (VideoControllerView.this.mPlayerControl != null && fromuser) {
                    this.mProgress = progress;
                    this.mNewPosition = (((long) VideoControllerView.this.mPlayerControl.getDuration()) * ((long) progress)) / 1000;
                    if (VideoControllerView.this.mCurrentTime != null) {
                        VideoControllerView.this.mCurrentTime.setText(VideoControllerView.this.stringForTime(VideoControllerView.this.mLiveMode ? (int) (VideoControllerView.this.mLiveStartTime - System.currentTimeMillis()) : (int) this.mNewPosition));
                    }
                }
            }

            public void onStopTrackingTouch(SeekBar bar) {
                VideoControllerView.this.mDragging = false;
                Log.d(VideoControllerView.TAG, "onProgressChanged.SeekTo " + this.mNewPosition);
                if (this.mNewPosition == ((long) VideoControllerView.this.mPlayerControl.getDuration())) {
                    this.mNewPosition--;
                    Log.i(VideoControllerView.TAG, "Modified onProgressChanged.SeekTo " + this.mNewPosition);
                }
                VideoControllerView.this.mPlayerControl.seekTo((int) this.mNewPosition);
                VideoControllerView.this.updateControls(VideoControllerView.SHOW_PROGRESS);
                VideoControllerView.this.setProgress();
                VideoControllerView.this.updatePausePlay();
                VideoControllerView.this.show(0);
                if (!VideoControllerView.this.mLiveMode) {
                    VideoControllerView.this.mHandler.sendEmptyMessage(VideoControllerView.SHOW_PROGRESS);
                }
            }
        };
        this.mRoot = null;
        this.mContext = context;
        this.mUseFastForward = true;
        this.mFromXml = true;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context, boolean useFastForward) {
        super(context);
        this.mFullscreenListener = /* anonymous class already generated */;
        this.mChatListener = /* anonymous class already generated */;
        this.mShareListener = /* anonymous class already generated */;
        this.mPauseListener = /* anonymous class already generated */;
        this.mHandler = new MessageHandler(this);
        this.mRewListener = /* anonymous class already generated */;
        this.mFfwdListener = /* anonymous class already generated */;
        this.mSeekListener = /* anonymous class already generated */;
        this.mContext = context;
        this.mUseFastForward = useFastForward;
        Log.i(TAG, TAG);
    }

    public VideoControllerView(Context context) {
        this(context, true);
        Log.i(TAG, TAG);
    }

    public void onFinishInflate() {
        if (this.mRoot != null) {
            initControllerView(this.mRoot);
        }
        super.onFinishInflate();
    }

    public void setMediaPlayer(MediaPlayerControl playerControl) {
        this.mPlayerControl = playerControl;
        updatePausePlay();
        updateFullScreen();
    }

    public void setTouchListener(OnTouchListener l) {
        setOnTouchListener(l);
        setTouchListener((ViewGroup) this.mRoot, l);
    }

    public void setOnShareMenuItemClickListener(OnItemClickListener onItemClickListener) {
        this.mMenuItemClickListener = onItemClickListener;
    }

    public void setCallbackOnShareMenuClosed(Callback onShareMenuClosed) {
        this.mOnShareMenuClosed = onShareMenuClosed;
    }

    private void setTouchListener(ViewGroup vg, OnTouchListener l) {
        for (int i = 0; i < vg.getChildCount(); i += FADE_OUT) {
            View v = vg.getChildAt(i);
            v.setOnTouchListener(l);
            if (v instanceof ViewGroup) {
                setTouchListener((ViewGroup) v, l);
            }
        }
    }

    public void setAnchorView(ViewGroup view) {
        this.mAnchor = view;
        LayoutParams frameParams = new LayoutParams(-1, -1);
        removeAllViews();
        addView(makeControllerView(), frameParams);
    }

    protected View makeControllerView() {
        this.mRoot = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.media_controller, null);
        initControllerView(this.mRoot);
        return this.mRoot;
    }

    private void initControllerView(View v) {
        int i = 0;
        this.mProgressLayout = v.findViewById(R.id.progress_layout);
        this.mProgressLayout.setVisibility(8);
        ProgressBar pb = (ProgressBar) this.mProgressLayout.findViewById(R.id.progressBar);
        Drawable d = pb.getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, getResources().getColor(R.color.yellow)));
        pb.setIndeterminateDrawable(d);
        this.mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (this.mPauseButton != null) {
            this.mPauseButton.requestFocus();
            this.mPauseButton.setOnClickListener(this.mPauseListener);
        }
        this.mShareButton = (ImageButton) v.findViewById(R.id.share);
        if (this.mShareButton != null) {
            this.mShareButton.setOnClickListener(this.mShareListener);
        }
        this.mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
        if (this.mFullscreenButton != null) {
            this.mFullscreenButton.requestFocus();
            this.mFullscreenButton.setOnClickListener(this.mFullscreenListener);
        }
        this.mChatButton = (ImageButton) v.findViewById(R.id.chat);
        if (this.mChatButton != null) {
            this.mChatButton.setOnClickListener(this.mChatListener);
        }
        this.mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setOnClickListener(this.mFfwdListener);
            if (!this.mFromXml) {
                this.mFfwdButton.setVisibility(this.mUseFastForward ? 0 : 8);
            }
        }
        this.mRewButton = (ImageButton) v.findViewById(R.id.rew);
        if (this.mRewButton != null) {
            this.mRewButton.setOnClickListener(this.mRewListener);
            if (!this.mFromXml) {
                ImageButton imageButton = this.mRewButton;
                if (!this.mUseFastForward) {
                    i = 8;
                }
                imageButton.setVisibility(i);
            }
        }
        this.mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (!(this.mNextButton == null || this.mFromXml || this.mListenersSet)) {
            this.mNextButton.setVisibility(8);
        }
        this.mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (!(this.mPrevButton == null || this.mFromXml || this.mListenersSet)) {
            this.mPrevButton.setVisibility(8);
        }
        this.mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (this.mProgress != null) {
            if (this.mProgress instanceof SeekBar) {
                this.mProgress.setOnSeekBarChangeListener(this.mSeekListener);
            }
            this.mProgress.setMax(Constants.UPDATE_COFIG_INTERVAL);
        }
        this.mEndTime = (TextView) v.findViewById(R.id.time);
        this.mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        this.mFormatBuilder = new StringBuilder();
        this.mFormatter = new Formatter(this.mFormatBuilder, Locale.getDefault());
        installPrevNextListeners();
    }

    public void show() {
        show(0);
    }

    private void disableUnsupportedButtons() {
        if (this.mPlayerControl != null) {
            try {
                if (!(this.mPauseButton == null || this.mPlayerControl.canPause())) {
                    this.mPauseButton.setEnabled(false);
                }
                if (!(this.mRewButton == null || this.mPlayerControl.canSeekBackward())) {
                    this.mRewButton.setEnabled(false);
                }
                if (this.mFfwdButton != null && !this.mPlayerControl.canSeekForward()) {
                    this.mFfwdButton.setEnabled(false);
                }
            } catch (IncompatibleClassChangeError e) {
            }
        }
    }

    public synchronized void show(int timeout) {
        if (!(this.mAdded || this.mAnchor == null)) {
            setProgress();
            if (this.mPauseButton != null) {
                this.mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();
            RelativeLayout.LayoutParams tlp = new RelativeLayout.LayoutParams(-1, -1);
            tlp.addRule(12);
            this.mAnchor.addView(this, tlp);
            this.mAdded = true;
            this.mShowing = true;
        }
        updateControls();
        Message msg = this.mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            this.mHandler.removeMessages(FADE_OUT);
            this.mHandler.sendMessageDelayed(msg, (long) timeout);
        }
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 0) {
            this.mShowing = true;
            this.mHandler.removeMessages(FADE_OUT);
        } else {
            this.mShowing = false;
            this.mHandler.removeMessages(SHOW_PROGRESS);
        }
        updateControls();
    }

    public boolean isShowing() {
        return getVisibility() == 0;
    }

    public void hide() {
        Log.e("MediaController", "hide", new Exception());
        if (this.mAnchor == null) {
            Log.d("MediaController", "nothing to hide");
            return;
        }
        try {
            this.mAnchor.removeView(this);
            this.mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException e) {
            Log.w("MediaController", "already removed");
        }
        Log.d("MediaController", "hided");
        this.mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / Constants.UPDATE_COFIG_INTERVAL;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / SettingsJsonConstants.SETTINGS_CACHE_DURATION_DEFAULT;
        this.mFormatBuilder.setLength(0);
        if (hours > 0) {
            Object[] objArr = new Object[SHOW_TIME_FOR_LIVE];
            objArr[0] = Integer.valueOf(hours);
            objArr[FADE_OUT] = Integer.valueOf(minutes);
            objArr[SHOW_PROGRESS] = Integer.valueOf(seconds);
            return this.mFormatter.format("%d:%02d:%02d", objArr).toString();
        }
        objArr = new Object[SHOW_PROGRESS];
        objArr[0] = Integer.valueOf(minutes);
        objArr[FADE_OUT] = Integer.valueOf(seconds);
        return this.mFormatter.format("%02d:%02d", objArr).toString();
    }

    private int setProgress() {
        if (this.mPlayerControl == null || this.mDragging) {
            return 0;
        }
        int position = this.mPlayerControl.getCurrentPosition();
        if (this.mPlayerControl.isBusy()) {
            return position;
        }
        int duration = this.mPlayerControl.getDuration();
        Log.d(TAG, "setProgress: " + position);
        if (this.mProgress != null) {
            if (duration > 0) {
                this.mProgress.setProgress((int) ((1000 * ((long) position)) / ((long) duration)));
            }
            this.mProgress.setSecondaryProgress(this.mPlayerControl.getBufferPercentage() * 10);
        }
        if (this.mEndTime != null) {
            this.mEndTime.setText(stringForTime(duration));
        }
        if (this.mCurrentTime == null) {
            return position;
        }
        this.mCurrentTime.setText(stringForTime(position));
        return position;
    }

    public int updateCurrentTime() {
        int currentTimeMillis;
        String str;
        int position = this.mPlayerControl.getCurrentPosition();
        TextView textView = this.mCurrentTime;
        StringBuilder stringBuilder = new StringBuilder();
        if (this.mLiveMode) {
            currentTimeMillis = (int) (System.currentTimeMillis() - this.mLiveStartTime);
        } else {
            currentTimeMillis = position;
        }
        stringBuilder = stringBuilder.append(stringForTime(currentTimeMillis));
        if (this.mViewersCount < SHOW_PROGRESS) {
            str = BuildConfig.FLAVOR;
        } else {
            Object[] objArr = new Object[FADE_OUT];
            objArr[0] = Integer.valueOf(this.mViewersCount);
            str = MainApplication.getRString(R.string._N_Views, objArr);
        }
        textView.setText(stringBuilder.append(str).toString());
        return this.mLiveMode ? 0 : position;
    }

    public boolean onTouchEvent(MotionEvent event) {
        show(0);
        return true;
    }

    public boolean onTrackballEvent(MotionEvent ev) {
        show(0);
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mPlayerControl == null) {
            return true;
        }
        boolean uniqueDown;
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0 && event.getAction() == 0) {
            uniqueDown = true;
        } else {
            uniqueDown = false;
        }
        if (keyCode == 79 || keyCode == 85 || keyCode == 62) {
            if (!uniqueDown) {
                return true;
            }
            doPauseResume();
            show(0);
            if (this.mPauseButton == null) {
                return true;
            }
            this.mPauseButton.requestFocus();
            return true;
        } else if (keyCode == TransportMediator.KEYCODE_MEDIA_PLAY) {
            if (!uniqueDown || this.mPlayerControl.isPlaying()) {
                return true;
            }
            this.mPlayerControl.start();
            updatePausePlay();
            show(0);
            return true;
        } else if (keyCode == 86 || keyCode == TransportMediator.KEYCODE_MEDIA_PAUSE) {
            if (!uniqueDown || !this.mPlayerControl.isPlaying()) {
                return true;
            }
            this.mPlayerControl.pause();
            updatePausePlay();
            show(0);
            return true;
        } else if (keyCode == 25 || keyCode == 24 || keyCode == 164) {
            return super.dispatchKeyEvent(event);
        } else {
            if (keyCode == 4 || keyCode == 82) {
                return uniqueDown ? true : true;
            } else {
                show(0);
                return super.dispatchKeyEvent(event);
            }
        }
    }

    public void updatePausePlay() {
        if (this.mRoot != null && this.mPauseButton != null && this.mPlayerControl != null) {
            if (this.mPlayerControl.isPlaying()) {
                this.mPauseButton.setImageResource(R.drawable.ic_playback_pause);
            } else {
                this.mPauseButton.setImageResource(R.drawable.ic_playback_play);
            }
        }
    }

    public void disableFullScreen(boolean disabled) {
        this.mFullscreenDisabled = disabled;
        this.mFullscreenButton.setVisibility(disabled ? 8 : 0);
    }

    public void updateFullScreen() {
        if (this.mRoot != null && this.mFullscreenButton != null && this.mPlayerControl != null) {
            if (this.mPlayerControl.isFullScreen()) {
                this.mFullscreenButton.setImageResource(R.drawable.ic_playback_fullscreen_close);
                this.mChatButton.setVisibility(0);
                return;
            }
            this.mFullscreenButton.setImageResource(R.drawable.ic_playback_fullscreen_open);
            this.mChatButton.setVisibility(8);
        }
    }

    private void doPauseResume() {
        if (this.mPlayerControl != null) {
            if (this.mPlayerControl.isPlaying()) {
                this.mPlayerControl.pause();
            } else {
                this.mPlayerControl.start();
            }
            updatePausePlay();
        }
    }

    private void doToggleFullscreen() {
        if (this.mPlayerControl != null) {
            this.mPlayerControl.toggleFullScreen();
        }
    }

    public void updateChat() {
        int i = 8;
        if (this.mRoot != null && this.mChatButton != null && this.mPlayerControl != null) {
            if (this.mPlayerControl.isChatShowing()) {
                this.mChatButton.setImageResource(R.drawable.ic_chat_button_active);
            } else {
                this.mChatButton.setImageResource(R.drawable.ic_chat_button_inactive);
                this.mChatButton.setVisibility(8);
            }
            ImageButton imageButton = this.mChatButton;
            if (this.mPlayerControl.isChatAvailable()) {
                i = 0;
            }
            imageButton.setVisibility(i);
        }
    }

    private void doToggleChat() {
        if (this.mPlayerControl != null) {
            this.mPlayerControl.toggleChat();
        }
    }

    public void setEnabled(boolean enabled) {
        boolean z = true;
        if (this.mPauseButton != null) {
            this.mPauseButton.setEnabled(enabled);
        }
        if (this.mFfwdButton != null) {
            this.mFfwdButton.setEnabled(enabled);
        }
        if (this.mRewButton != null) {
            this.mRewButton.setEnabled(enabled);
        }
        if (this.mNextButton != null) {
            ImageButton imageButton = this.mNextButton;
            boolean z2 = enabled && this.mNextListener != null;
            imageButton.setEnabled(z2);
        }
        if (this.mPrevButton != null) {
            ImageButton imageButton2 = this.mPrevButton;
            if (!enabled || this.mPrevListener == null) {
                z = false;
            }
            imageButton2.setEnabled(z);
        }
        if (this.mProgress != null) {
            this.mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private void installPrevNextListeners() {
        boolean z = true;
        if (this.mNextButton != null) {
            this.mNextButton.setOnClickListener(this.mNextListener);
            this.mNextButton.setEnabled(this.mNextListener != null);
        }
        if (this.mPrevButton != null) {
            this.mPrevButton.setOnClickListener(this.mPrevListener);
            ImageButton imageButton = this.mPrevButton;
            if (this.mPrevListener == null) {
                z = false;
            }
            imageButton.setEnabled(z);
        }
    }

    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
        this.mNextListener = next;
        this.mPrevListener = prev;
        this.mListenersSet = true;
        if (this.mRoot != null) {
            installPrevNextListeners();
            if (!(this.mNextButton == null || this.mFromXml)) {
                this.mNextButton.setVisibility(0);
            }
            if (this.mPrevButton != null && !this.mFromXml) {
                this.mPrevButton.setVisibility(0);
            }
        }
    }

    public void updateControls() {
        invalidate();
        updatePausePlay();
        updateFullScreen();
        if (this.mShowing) {
            if (this.mLiveMode) {
                this.mHandler.sendEmptyMessage(SHOW_TIME_FOR_LIVE);
            } else {
                this.mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }
        updateFullScreen();
        updateChat();
    }

    public void updateControls(int playbackState) {
        if (playbackState == SHOW_TIME_FOR_LIVE || playbackState == SHOW_PROGRESS) {
            this.mPauseButton.setVisibility(8);
            this.mProgressLayout.setVisibility(0);
            return;
        }
        this.mProgressLayout.setVisibility(8);
        if (!this.mLiveMode) {
            this.mPauseButton.setVisibility(0);
        }
    }

    public void clearLiveMode() {
        this.mLiveMode = false;
        findViewById(R.id.live_text).setVisibility(8);
        this.mFullscreenButton.setVisibility(8);
        this.mPauseButton.setVisibility(8);
        this.mProgress.setVisibility(8);
        this.mCurrentTime.setVisibility(8);
        this.mEndTime.setVisibility(8);
    }

    public void setLiveMode(String time) {
        this.mLiveMode = true;
        this.mHandler.removeMessages(SHOW_PROGRESS);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            this.mLiveStartTime = formatter.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            Crashlytics.logException(new Exception("Can't parse next broadcast.StartTime: " + time, e));
            this.mLiveStartTime = System.currentTimeMillis();
        }
        this.mHandler.sendEmptyMessage(SHOW_TIME_FOR_LIVE);
        findViewById(R.id.live_text).setVisibility(0);
        this.mCurrentTime.setVisibility(0);
        this.mPrevButton.setVisibility(4);
        this.mNextButton.setVisibility(4);
        this.mPauseButton.setVisibility(4);
        this.mFfwdButton.setVisibility(4);
        this.mRewButton.setVisibility(4);
        this.mEndTime.setVisibility(4);
        this.mProgress.setVisibility(4);
    }

    public void setViewersCount(int viewersCount) {
        this.mViewersCount = viewersCount;
    }
}
