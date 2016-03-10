package com.google.android.exoplayer.audio;

import android.annotation.TargetApi;
import android.media.AudioTimestamp;
import android.media.MediaFormat;
import android.os.ConditionVariable;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.util.Ac3Util;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

@TargetApi(16)
public final class AudioTrack {
    private static final int BUFFER_MULTIPLICATION_FACTOR = 4;
    public static final long CURRENT_POSITION_NOT_SET = Long.MIN_VALUE;
    private static final long MAX_AUDIO_TIMESTAMP_OFFSET_US = 5000000;
    private static final long MAX_BUFFER_DURATION_US = 750000;
    private static final long MAX_LATENCY_US = 5000000;
    private static final int MAX_PLAYHEAD_OFFSET_COUNT = 10;
    private static final long MIN_BUFFER_DURATION_US = 250000;
    private static final int MIN_PLAYHEAD_OFFSET_SAMPLE_INTERVAL_US = 30000;
    private static final int MIN_TIMESTAMP_SAMPLE_INTERVAL_US = 500000;
    public static final int RESULT_BUFFER_CONSUMED = 2;
    public static final int RESULT_POSITION_DISCONTINUITY = 1;
    public static final int SESSION_ID_NOT_SET = 0;
    private static final int START_IN_SYNC = 1;
    private static final int START_NEED_SYNC = 2;
    private static final int START_NOT_SET = 0;
    private static final String TAG = "AudioTrack";
    private static final int UNKNOWN_AC3_BITRATE = 0;
    public static boolean enablePreV21AudioSessionWorkaround = false;
    public static boolean failOnSpuriousAudioTimestamp = false;
    private int ac3Bitrate;
    private boolean audioTimestampSet;
    private android.media.AudioTrack audioTrack;
    private final AudioTrackUtil audioTrackUtil;
    private int bufferSize;
    private int channelConfig;
    private int encoding;
    private int frameSize;
    private Method getLatencyMethod;
    private boolean isAc3;
    private android.media.AudioTrack keepSessionIdAudioTrack;
    private long lastPlayheadSampleTimeUs;
    private long lastTimestampSampleTimeUs;
    private long latencyUs;
    private int minBufferSize;
    private int nextPlayheadOffsetIndex;
    private int playheadOffsetCount;
    private final long[] playheadOffsets;
    private final ConditionVariable releasingConditionVariable = new ConditionVariable(true);
    private long resumeSystemTimeUs;
    private int sampleRate;
    private long smoothedPlayheadOffsetUs;
    private int startMediaTimeState;
    private long startMediaTimeUs;
    private long submittedBytes;
    private byte[] temporaryBuffer;
    private int temporaryBufferOffset;
    private int temporaryBufferSize;
    private float volume;

    private static class AudioTrackUtil {
        protected android.media.AudioTrack audioTrack;
        private boolean isPassthrough;
        private long lastRawPlaybackHeadPosition;
        private long passthroughWorkaroundPauseOffset;
        private long rawPlaybackHeadWrapCount;
        private int sampleRate;

        private AudioTrackUtil() {
        }

        public void reconfigure(android.media.AudioTrack audioTrack, boolean isPassthrough) {
            this.audioTrack = audioTrack;
            this.isPassthrough = isPassthrough;
            this.lastRawPlaybackHeadPosition = 0;
            this.rawPlaybackHeadWrapCount = 0;
            this.passthroughWorkaroundPauseOffset = 0;
            if (audioTrack != null) {
                this.sampleRate = audioTrack.getSampleRate();
            }
        }

        public boolean overrideHasPendingData() {
            return Util.SDK_INT <= 22 && this.isPassthrough && this.audioTrack.getPlayState() == AudioTrack.START_NEED_SYNC && this.audioTrack.getPlaybackHeadPosition() == 0;
        }

        public long getPlaybackHeadPosition() {
            long rawPlaybackHeadPosition = 4294967295L & ((long) this.audioTrack.getPlaybackHeadPosition());
            if (Util.SDK_INT <= 22 && this.isPassthrough) {
                if (this.audioTrack.getPlayState() == AudioTrack.START_IN_SYNC) {
                    this.lastRawPlaybackHeadPosition = rawPlaybackHeadPosition;
                } else if (this.audioTrack.getPlayState() == AudioTrack.START_NEED_SYNC && rawPlaybackHeadPosition == 0) {
                    this.passthroughWorkaroundPauseOffset = this.lastRawPlaybackHeadPosition;
                }
                rawPlaybackHeadPosition += this.passthroughWorkaroundPauseOffset;
            }
            if (this.lastRawPlaybackHeadPosition > rawPlaybackHeadPosition) {
                this.rawPlaybackHeadWrapCount++;
            }
            this.lastRawPlaybackHeadPosition = rawPlaybackHeadPosition;
            return (this.rawPlaybackHeadWrapCount << 32) + rawPlaybackHeadPosition;
        }

        public long getPlaybackHeadPositionUs() {
            return (getPlaybackHeadPosition() * C.MICROS_PER_SECOND) / ((long) this.sampleRate);
        }

        public boolean updateTimestamp() {
            return false;
        }

        public long getTimestampNanoTime() {
            throw new UnsupportedOperationException();
        }

        public long getTimestampFramePosition() {
            throw new UnsupportedOperationException();
        }
    }

    @TargetApi(19)
    private static class AudioTrackUtilV19 extends AudioTrackUtil {
        private final AudioTimestamp audioTimestamp = new AudioTimestamp();
        private long lastRawTimestampFramePosition;
        private long lastTimestampFramePosition;
        private long rawTimestampFramePositionWrapCount;

        public AudioTrackUtilV19() {
            super();
        }

        public void reconfigure(android.media.AudioTrack audioTrack, boolean isPassthrough) {
            super.reconfigure(audioTrack, isPassthrough);
            this.rawTimestampFramePositionWrapCount = 0;
            this.lastRawTimestampFramePosition = 0;
            this.lastTimestampFramePosition = 0;
        }

        public boolean updateTimestamp() {
            boolean updated = this.audioTrack.getTimestamp(this.audioTimestamp);
            if (updated) {
                long rawFramePosition = this.audioTimestamp.framePosition;
                if (this.lastRawTimestampFramePosition > rawFramePosition) {
                    this.rawTimestampFramePositionWrapCount++;
                }
                this.lastRawTimestampFramePosition = rawFramePosition;
                this.lastTimestampFramePosition = (this.rawTimestampFramePositionWrapCount << 32) + rawFramePosition;
            }
            return updated;
        }

        public long getTimestampNanoTime() {
            return this.audioTimestamp.nanoTime;
        }

        public long getTimestampFramePosition() {
            return this.lastTimestampFramePosition;
        }
    }

    public static final class InitializationException extends Exception {
        public final int audioTrackState;

        public InitializationException(int audioTrackState, int sampleRate, int channelConfig, int bufferSize) {
            super("AudioTrack init failed: " + audioTrackState + ", Config(" + sampleRate + ", " + channelConfig + ", " + bufferSize + ")");
            this.audioTrackState = audioTrackState;
        }
    }

    private static final class InvalidAudioTrackTimestampException extends RuntimeException {
        public InvalidAudioTrackTimestampException(String message) {
            super(message);
        }
    }

    public static final class WriteException extends Exception {
        public final int errorCode;

        public WriteException(int errorCode) {
            super("AudioTrack write failed: " + errorCode);
            this.errorCode = errorCode;
        }
    }

    public AudioTrack() {
        if (Util.SDK_INT >= 18) {
            try {
                this.getLatencyMethod = android.media.AudioTrack.class.getMethod("getLatency", (Class[]) null);
            } catch (NoSuchMethodException e) {
            }
        }
        if (Util.SDK_INT >= 19) {
            this.audioTrackUtil = new AudioTrackUtilV19();
        } else {
            this.audioTrackUtil = new AudioTrackUtil();
        }
        this.playheadOffsets = new long[MAX_PLAYHEAD_OFFSET_COUNT];
        this.volume = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        this.startMediaTimeState = START_NOT_SET;
    }

    public boolean isInitialized() {
        return this.audioTrack != null;
    }

    public long getCurrentPositionUs(boolean sourceEnded) {
        if (!hasCurrentPositionUs()) {
            return CURRENT_POSITION_NOT_SET;
        }
        if (this.audioTrack.getPlayState() == 3) {
            maybeSampleSyncParams();
        }
        long systemClockUs = System.nanoTime() / 1000;
        if (this.audioTimestampSet) {
            return framesToDurationUs(this.audioTrackUtil.getTimestampFramePosition() + durationUsToFrames(systemClockUs - (this.audioTrackUtil.getTimestampNanoTime() / 1000))) + this.startMediaTimeUs;
        }
        long currentPositionUs;
        if (this.playheadOffsetCount == 0) {
            currentPositionUs = this.audioTrackUtil.getPlaybackHeadPositionUs() + this.startMediaTimeUs;
        } else {
            currentPositionUs = (this.smoothedPlayheadOffsetUs + systemClockUs) + this.startMediaTimeUs;
        }
        if (sourceEnded) {
            return currentPositionUs;
        }
        return currentPositionUs - this.latencyUs;
    }

    public int initialize() throws InitializationException {
        return initialize(START_NOT_SET);
    }

    public int initialize(int sessionId) throws InitializationException {
        this.releasingConditionVariable.block();
        if (sessionId == 0) {
            this.audioTrack = new android.media.AudioTrack(3, this.sampleRate, this.channelConfig, this.encoding, this.bufferSize, START_IN_SYNC);
        } else {
            this.audioTrack = new android.media.AudioTrack(3, this.sampleRate, this.channelConfig, this.encoding, this.bufferSize, START_IN_SYNC, sessionId);
        }
        checkAudioTrackInitialized();
        sessionId = this.audioTrack.getAudioSessionId();
        if (enablePreV21AudioSessionWorkaround && Util.SDK_INT < 21) {
            if (!(this.keepSessionIdAudioTrack == null || sessionId == this.keepSessionIdAudioTrack.getAudioSessionId())) {
                releaseKeepSessionIdAudioTrack();
            }
            if (this.keepSessionIdAudioTrack == null) {
                this.keepSessionIdAudioTrack = new android.media.AudioTrack(3, 4000, BUFFER_MULTIPLICATION_FACTOR, START_NEED_SYNC, START_NEED_SYNC, START_NOT_SET, sessionId);
            }
        }
        this.audioTrackUtil.reconfigure(this.audioTrack, this.isAc3);
        setVolume(this.volume);
        return sessionId;
    }

    public void reconfigure(MediaFormat format) {
        reconfigure(format, START_NOT_SET);
    }

    public void reconfigure(MediaFormat format, int specifiedBufferSize) {
        int channelConfig;
        int channelCount = format.getInteger("channel-count");
        switch (channelCount) {
            case START_IN_SYNC /*1*/:
                channelConfig = BUFFER_MULTIPLICATION_FACTOR;
                break;
            case START_NEED_SYNC /*2*/:
                channelConfig = 12;
                break;
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                channelConfig = 252;
                break;
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                channelConfig = 1020;
                break;
            default:
                throw new IllegalArgumentException("Unsupported channel count: " + channelCount);
        }
        int sampleRate = format.getInteger("sample-rate");
        int encoding = MimeTypes.getEncodingForMimeType(format.getString("mime"));
        boolean isAc3 = encoding == 5 || encoding == 6;
        if (!isInitialized() || this.sampleRate != sampleRate || this.channelConfig != channelConfig || this.isAc3 || isAc3) {
            reset();
            this.encoding = encoding;
            this.sampleRate = sampleRate;
            this.channelConfig = channelConfig;
            this.isAc3 = isAc3;
            this.ac3Bitrate = START_NOT_SET;
            this.frameSize = channelCount * START_NEED_SYNC;
            this.minBufferSize = android.media.AudioTrack.getMinBufferSize(sampleRate, channelConfig, encoding);
            Assertions.checkState(this.minBufferSize != -2);
            if (specifiedBufferSize != 0) {
                this.bufferSize = specifiedBufferSize;
                return;
            }
            int multipliedBufferSize = this.minBufferSize * BUFFER_MULTIPLICATION_FACTOR;
            int minAppBufferSize = ((int) durationUsToFrames(MIN_BUFFER_DURATION_US)) * this.frameSize;
            int maxAppBufferSize = (int) Math.max((long) this.minBufferSize, durationUsToFrames(MAX_BUFFER_DURATION_US) * ((long) this.frameSize));
            if (multipliedBufferSize >= minAppBufferSize) {
                minAppBufferSize = multipliedBufferSize > maxAppBufferSize ? maxAppBufferSize : multipliedBufferSize;
            }
            this.bufferSize = minAppBufferSize;
        }
    }

    public void play() {
        if (isInitialized()) {
            this.resumeSystemTimeUs = System.nanoTime() / 1000;
            this.audioTrack.play();
        }
    }

    public void handleDiscontinuity() {
        if (this.startMediaTimeState == START_IN_SYNC) {
            this.startMediaTimeState = START_NEED_SYNC;
        }
    }

    public int handleBuffer(ByteBuffer buffer, int offset, int size, long presentationTimeUs) throws WriteException {
        if (size == 0) {
            return START_NEED_SYNC;
        }
        if (Util.SDK_INT <= 22 && this.isAc3) {
            if (this.audioTrack.getPlayState() == START_NEED_SYNC) {
                return START_NOT_SET;
            }
            if (this.audioTrack.getPlayState() == START_IN_SYNC && this.audioTrackUtil.getPlaybackHeadPosition() != 0) {
                return START_NOT_SET;
            }
        }
        int result = START_NOT_SET;
        if (this.temporaryBufferSize == 0) {
            if (this.isAc3 && this.ac3Bitrate == 0) {
                this.ac3Bitrate = Ac3Util.getBitrate(size, this.sampleRate);
            }
            long bufferStartTime = presentationTimeUs - framesToDurationUs(bytesToFrames((long) size));
            if (this.startMediaTimeState == 0) {
                this.startMediaTimeUs = Math.max(0, bufferStartTime);
                this.startMediaTimeState = START_IN_SYNC;
            } else {
                long expectedBufferStartTime = this.startMediaTimeUs + framesToDurationUs(bytesToFrames(this.submittedBytes));
                if (this.startMediaTimeState == START_IN_SYNC && Math.abs(expectedBufferStartTime - bufferStartTime) > 200000) {
                    Log.e(TAG, "Discontinuity detected [expected " + expectedBufferStartTime + ", got " + bufferStartTime + "]");
                    this.startMediaTimeState = START_NEED_SYNC;
                }
                if (this.startMediaTimeState == START_NEED_SYNC) {
                    this.startMediaTimeUs += bufferStartTime - expectedBufferStartTime;
                    this.startMediaTimeState = START_IN_SYNC;
                    result = START_NOT_SET | START_IN_SYNC;
                }
            }
        }
        if (this.temporaryBufferSize == 0) {
            this.temporaryBufferSize = size;
            buffer.position(offset);
            if (Util.SDK_INT < 21) {
                if (this.temporaryBuffer == null || this.temporaryBuffer.length < size) {
                    this.temporaryBuffer = new byte[size];
                }
                buffer.get(this.temporaryBuffer, START_NOT_SET, size);
                this.temporaryBufferOffset = START_NOT_SET;
            }
        }
        int bytesWritten = START_NOT_SET;
        if (Util.SDK_INT < 21) {
            int bytesToWrite = this.bufferSize - ((int) (this.submittedBytes - (this.audioTrackUtil.getPlaybackHeadPosition() * ((long) this.frameSize))));
            if (bytesToWrite > 0) {
                bytesWritten = this.audioTrack.write(this.temporaryBuffer, this.temporaryBufferOffset, Math.min(this.temporaryBufferSize, bytesToWrite));
                if (bytesWritten >= 0) {
                    this.temporaryBufferOffset += bytesWritten;
                }
            }
        } else {
            bytesWritten = writeNonBlockingV21(this.audioTrack, buffer, this.temporaryBufferSize);
        }
        if (bytesWritten < 0) {
            throw new WriteException(bytesWritten);
        }
        this.temporaryBufferSize -= bytesWritten;
        this.submittedBytes += (long) bytesWritten;
        if (this.temporaryBufferSize == 0) {
            return result | START_NEED_SYNC;
        }
        return result;
    }

    @TargetApi(21)
    private static int writeNonBlockingV21(android.media.AudioTrack audioTrack, ByteBuffer buffer, int size) {
        return audioTrack.write(buffer, size, START_IN_SYNC);
    }

    public boolean hasPendingData() {
        return isInitialized() && (bytesToFrames(this.submittedBytes) > this.audioTrackUtil.getPlaybackHeadPosition() || this.audioTrackUtil.overrideHasPendingData());
    }

    public boolean hasEnoughDataToBeginPlayback() {
        return this.submittedBytes > ((long) ((this.minBufferSize * 3) / START_NEED_SYNC));
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (!isInitialized()) {
            return;
        }
        if (Util.SDK_INT >= 21) {
            setVolumeV21(this.audioTrack, volume);
        } else {
            setVolumeV3(this.audioTrack, volume);
        }
    }

    @TargetApi(21)
    private static void setVolumeV21(android.media.AudioTrack audioTrack, float volume) {
        audioTrack.setVolume(volume);
    }

    private static void setVolumeV3(android.media.AudioTrack audioTrack, float volume) {
        audioTrack.setStereoVolume(volume, volume);
    }

    public void pause() {
        if (isInitialized()) {
            resetSyncParams();
            this.audioTrack.pause();
        }
    }

    public void reset() {
        if (isInitialized()) {
            this.submittedBytes = 0;
            this.temporaryBufferSize = START_NOT_SET;
            this.startMediaTimeState = START_NOT_SET;
            this.latencyUs = 0;
            resetSyncParams();
            if (this.audioTrack.getPlayState() == 3) {
                this.audioTrack.pause();
            }
            final android.media.AudioTrack toRelease = this.audioTrack;
            this.audioTrack = null;
            this.audioTrackUtil.reconfigure(null, false);
            this.releasingConditionVariable.close();
            new Thread() {
                public void run() {
                    try {
                        toRelease.release();
                    } finally {
                        AudioTrack.this.releasingConditionVariable.open();
                    }
                }
            }.start();
        }
    }

    public void release() {
        reset();
        releaseKeepSessionIdAudioTrack();
    }

    private void releaseKeepSessionIdAudioTrack() {
        if (this.keepSessionIdAudioTrack != null) {
            final android.media.AudioTrack toRelease = this.keepSessionIdAudioTrack;
            this.keepSessionIdAudioTrack = null;
            new Thread() {
                public void run() {
                    toRelease.release();
                }
            }.start();
        }
    }

    private boolean hasCurrentPositionUs() {
        return isInitialized() && this.startMediaTimeState != 0;
    }

    private void maybeSampleSyncParams() {
        long playbackPositionUs = this.audioTrackUtil.getPlaybackHeadPositionUs();
        if (playbackPositionUs != 0) {
            long systemClockUs = System.nanoTime() / 1000;
            if (systemClockUs - this.lastPlayheadSampleTimeUs >= Constants.BROADCASTS_LIVE_TIME) {
                this.playheadOffsets[this.nextPlayheadOffsetIndex] = playbackPositionUs - systemClockUs;
                this.nextPlayheadOffsetIndex = (this.nextPlayheadOffsetIndex + START_IN_SYNC) % MAX_PLAYHEAD_OFFSET_COUNT;
                if (this.playheadOffsetCount < MAX_PLAYHEAD_OFFSET_COUNT) {
                    this.playheadOffsetCount += START_IN_SYNC;
                }
                this.lastPlayheadSampleTimeUs = systemClockUs;
                this.smoothedPlayheadOffsetUs = 0;
                for (int i = START_NOT_SET; i < this.playheadOffsetCount; i += START_IN_SYNC) {
                    this.smoothedPlayheadOffsetUs += this.playheadOffsets[i] / ((long) this.playheadOffsetCount);
                }
            }
            if (!this.isAc3 && systemClockUs - this.lastTimestampSampleTimeUs >= 500000) {
                this.audioTimestampSet = this.audioTrackUtil.updateTimestamp();
                if (this.audioTimestampSet) {
                    long audioTimestampUs = this.audioTrackUtil.getTimestampNanoTime() / 1000;
                    long audioTimestampFramePosition = this.audioTrackUtil.getTimestampFramePosition();
                    if (audioTimestampUs < this.resumeSystemTimeUs) {
                        this.audioTimestampSet = false;
                    } else if (Math.abs(audioTimestampUs - systemClockUs) > MAX_LATENCY_US) {
                        message = "Spurious audio timestamp (system clock mismatch): " + audioTimestampFramePosition + ", " + audioTimestampUs + ", " + systemClockUs + ", " + playbackPositionUs;
                        if (failOnSpuriousAudioTimestamp) {
                            throw new InvalidAudioTrackTimestampException(message);
                        }
                        Log.w(TAG, message);
                        this.audioTimestampSet = false;
                    } else if (Math.abs(framesToDurationUs(audioTimestampFramePosition) - playbackPositionUs) > MAX_LATENCY_US) {
                        message = "Spurious audio timestamp (frame position mismatch): " + audioTimestampFramePosition + ", " + audioTimestampUs + ", " + systemClockUs + ", " + playbackPositionUs;
                        if (failOnSpuriousAudioTimestamp) {
                            throw new InvalidAudioTrackTimestampException(message);
                        }
                        Log.w(TAG, message);
                        this.audioTimestampSet = false;
                    }
                }
                if (this.getLatencyMethod != null) {
                    try {
                        Method method = this.getLatencyMethod;
                        this.latencyUs = (((long) ((Integer) method.invoke(this.audioTrack, (Object[]) null)).intValue()) * 1000) - framesToDurationUs(bytesToFrames((long) this.bufferSize));
                        this.latencyUs = Math.max(this.latencyUs, 0);
                        if (this.latencyUs > MAX_LATENCY_US) {
                            Log.w(TAG, "Ignoring impossibly large audio latency: " + this.latencyUs);
                            this.latencyUs = 0;
                        }
                    } catch (Exception e) {
                        this.getLatencyMethod = null;
                    }
                }
                this.lastTimestampSampleTimeUs = systemClockUs;
            }
        }
    }

    private void checkAudioTrackInitialized() throws InitializationException {
        int state = this.audioTrack.getState();
        if (state != START_IN_SYNC) {
            try {
                this.audioTrack.release();
            } catch (Exception e) {
            } finally {
                this.audioTrack = null;
            }
            throw new InitializationException(state, this.sampleRate, this.channelConfig, this.bufferSize);
        }
    }

    private long bytesToFrames(long byteCount) {
        if (this.isAc3) {
            return this.ac3Bitrate == 0 ? 0 : ((8 * byteCount) * ((long) this.sampleRate)) / ((long) (this.ac3Bitrate * Constants.UPDATE_COFIG_INTERVAL));
        } else {
            return byteCount / ((long) this.frameSize);
        }
    }

    private long framesToDurationUs(long frameCount) {
        return (C.MICROS_PER_SECOND * frameCount) / ((long) this.sampleRate);
    }

    private long durationUsToFrames(long durationUs) {
        return (((long) this.sampleRate) * durationUs) / C.MICROS_PER_SECOND;
    }

    private void resetSyncParams() {
        this.smoothedPlayheadOffsetUs = 0;
        this.playheadOffsetCount = START_NOT_SET;
        this.nextPlayheadOffsetIndex = START_NOT_SET;
        this.lastPlayheadSampleTimeUs = 0;
        this.audioTimestampSet = false;
        this.lastTimestampSampleTimeUs = 0;
    }
}
