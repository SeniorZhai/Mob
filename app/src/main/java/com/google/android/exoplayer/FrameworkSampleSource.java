package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.drm.DrmInitData.Mapped;
import com.google.android.exoplayer.extractor.mp4.PsshAtomUtil;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.Util;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@TargetApi(16)
@Deprecated
public final class FrameworkSampleSource implements SampleSource, SampleSourceReader {
    private static final int ALLOWED_FLAGS_MASK = 3;
    private static final int TRACK_STATE_DISABLED = 0;
    private static final int TRACK_STATE_ENABLED = 1;
    private static final int TRACK_STATE_FORMAT_SENT = 2;
    private final Context context;
    private MediaExtractor extractor;
    private final FileDescriptor fileDescriptor;
    private final long fileDescriptorLength;
    private final long fileDescriptorOffset;
    private final Map<String, String> headers;
    private boolean[] pendingDiscontinuities;
    private boolean prepared;
    private int remainingReleaseCount;
    private long seekPositionUs;
    private TrackInfo[] trackInfos;
    private int[] trackStates;
    private final Uri uri;

    public FrameworkSampleSource(Context context, Uri uri, Map<String, String> headers) {
        Assertions.checkState(Util.SDK_INT >= 16);
        this.context = (Context) Assertions.checkNotNull(context);
        this.uri = (Uri) Assertions.checkNotNull(uri);
        this.headers = headers;
        this.fileDescriptor = null;
        this.fileDescriptorOffset = 0;
        this.fileDescriptorLength = 0;
    }

    public FrameworkSampleSource(FileDescriptor fileDescriptor, long fileDescriptorOffset, long fileDescriptorLength) {
        Assertions.checkState(Util.SDK_INT >= 16);
        this.fileDescriptor = (FileDescriptor) Assertions.checkNotNull(fileDescriptor);
        this.fileDescriptorOffset = fileDescriptorOffset;
        this.fileDescriptorLength = fileDescriptorLength;
        this.context = null;
        this.uri = null;
        this.headers = null;
    }

    public SampleSourceReader register() {
        this.remainingReleaseCount += TRACK_STATE_ENABLED;
        return this;
    }

    public boolean prepare(long positionUs) throws IOException {
        if (!this.prepared) {
            this.extractor = new MediaExtractor();
            if (this.context != null) {
                this.extractor.setDataSource(this.context, this.uri, this.headers);
            } else {
                this.extractor.setDataSource(this.fileDescriptor, this.fileDescriptorOffset, this.fileDescriptorLength);
            }
            this.trackStates = new int[this.extractor.getTrackCount()];
            this.pendingDiscontinuities = new boolean[this.trackStates.length];
            this.trackInfos = new TrackInfo[this.trackStates.length];
            for (int i = TRACK_STATE_DISABLED; i < this.trackStates.length; i += TRACK_STATE_ENABLED) {
                MediaFormat format = this.extractor.getTrackFormat(i);
                this.trackInfos[i] = new TrackInfo(format.getString("mime"), format.containsKey("durationUs") ? format.getLong("durationUs") : -1);
            }
            this.prepared = true;
        }
        return true;
    }

    public int getTrackCount() {
        Assertions.checkState(this.prepared);
        return this.trackStates.length;
    }

    public TrackInfo getTrackInfo(int track) {
        Assertions.checkState(this.prepared);
        return this.trackInfos[track];
    }

    public void enable(int track, long positionUs) {
        boolean z;
        boolean z2 = true;
        Assertions.checkState(this.prepared);
        if (this.trackStates[track] == 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        this.trackStates[track] = TRACK_STATE_ENABLED;
        this.extractor.selectTrack(track);
        if (positionUs == 0) {
            z2 = false;
        }
        seekToUsInternal(positionUs, z2);
    }

    public boolean continueBuffering(long positionUs) {
        return true;
    }

    public int readData(int track, long positionUs, MediaFormatHolder formatHolder, SampleHolder sampleHolder, boolean onlyReadDiscontinuity) {
        Assertions.checkState(this.prepared);
        Assertions.checkState(this.trackStates[track] != 0);
        if (this.pendingDiscontinuities[track]) {
            this.pendingDiscontinuities[track] = false;
            return -5;
        } else if (onlyReadDiscontinuity) {
            return -2;
        } else {
            if (this.trackStates[track] != TRACK_STATE_FORMAT_SENT) {
                formatHolder.format = MediaFormat.createFromFrameworkMediaFormatV16(this.extractor.getTrackFormat(track));
                formatHolder.drmInitData = Util.SDK_INT >= 18 ? getDrmInitDataV18() : null;
                this.trackStates[track] = TRACK_STATE_FORMAT_SENT;
                return -4;
            }
            int extractorTrackIndex = this.extractor.getSampleTrackIndex();
            if (extractorTrackIndex == track) {
                if (sampleHolder.data != null) {
                    int offset = sampleHolder.data.position();
                    sampleHolder.size = this.extractor.readSampleData(sampleHolder.data, offset);
                    sampleHolder.data.position(sampleHolder.size + offset);
                } else {
                    sampleHolder.size = TRACK_STATE_DISABLED;
                }
                sampleHolder.timeUs = this.extractor.getSampleTime();
                sampleHolder.flags = this.extractor.getSampleFlags() & ALLOWED_FLAGS_MASK;
                if (sampleHolder.isEncrypted()) {
                    sampleHolder.cryptoInfo.setFromExtractorV16(this.extractor);
                }
                this.seekPositionUs = -1;
                this.extractor.advance();
                return -3;
            }
            return extractorTrackIndex < 0 ? -1 : -2;
        }
    }

    public void disable(int track) {
        boolean z;
        Assertions.checkState(this.prepared);
        if (this.trackStates[track] != 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        this.extractor.unselectTrack(track);
        this.pendingDiscontinuities[track] = false;
        this.trackStates[track] = TRACK_STATE_DISABLED;
    }

    public void seekToUs(long positionUs) {
        Assertions.checkState(this.prepared);
        seekToUsInternal(positionUs, false);
    }

    public long getBufferedPositionUs() {
        Assertions.checkState(this.prepared);
        long bufferedDurationUs = this.extractor.getCachedDuration();
        if (bufferedDurationUs == -1) {
            return -1;
        }
        long sampleTime = this.extractor.getSampleTime();
        return sampleTime == -1 ? -3 : sampleTime + bufferedDurationUs;
    }

    public void release() {
        Assertions.checkState(this.remainingReleaseCount > 0);
        int i = this.remainingReleaseCount - 1;
        this.remainingReleaseCount = i;
        if (i == 0 && this.extractor != null) {
            this.extractor.release();
            this.extractor = null;
        }
    }

    @TargetApi(18)
    private DrmInitData getDrmInitDataV18() {
        Map<UUID, byte[]> psshInfo = this.extractor.getPsshInfo();
        if (psshInfo == null || psshInfo.isEmpty()) {
            return null;
        }
        DrmInitData drmInitData = new Mapped(MimeTypes.VIDEO_MP4);
        for (UUID uuid : psshInfo.keySet()) {
            drmInitData.put(uuid, PsshAtomUtil.buildPsshAtom(uuid, (byte[]) psshInfo.get(uuid)));
        }
        return drmInitData;
    }

    private void seekToUsInternal(long positionUs, boolean force) {
        if (force || this.seekPositionUs != positionUs) {
            this.seekPositionUs = positionUs;
            this.extractor.seekTo(positionUs, TRACK_STATE_DISABLED);
            for (int i = TRACK_STATE_DISABLED; i < this.trackStates.length; i += TRACK_STATE_ENABLED) {
                if (this.trackStates[i] != 0) {
                    this.pendingDiscontinuities[i] = true;
                }
            }
        }
    }
}
