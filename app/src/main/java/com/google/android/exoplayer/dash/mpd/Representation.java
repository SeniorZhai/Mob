package com.google.android.exoplayer.dash.mpd;

import android.net.Uri;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatWrapper;
import com.google.android.exoplayer.dash.DashSegmentIndex;
import com.google.android.exoplayer.dash.DashSingleSegmentIndex;
import com.google.android.exoplayer.dash.mpd.SegmentBase.MultiSegmentBase;
import com.google.android.exoplayer.dash.mpd.SegmentBase.SingleSegmentBase;

public abstract class Representation implements FormatWrapper {
    public final String contentId;
    public final Format format;
    private final RangedUri initializationUri;
    public final long periodDurationMs;
    public final long periodStartMs;
    public final long presentationTimeOffsetUs;
    public final long revisionId;

    public static class MultiSegmentRepresentation extends Representation implements DashSegmentIndex {
        private final MultiSegmentBase segmentBase;

        public MultiSegmentRepresentation(long periodStartMs, long periodDurationMs, String contentId, long revisionId, Format format, MultiSegmentBase segmentBase) {
            super(periodStartMs, periodDurationMs, contentId, revisionId, format, segmentBase);
            this.segmentBase = segmentBase;
        }

        public RangedUri getIndexUri() {
            return null;
        }

        public DashSegmentIndex getIndex() {
            return this;
        }

        public RangedUri getSegmentUrl(int segmentIndex) {
            return this.segmentBase.getSegmentUrl(this, segmentIndex);
        }

        public int getSegmentNum(long timeUs) {
            return this.segmentBase.getSegmentNum(timeUs - (this.periodStartMs * 1000));
        }

        public long getTimeUs(int segmentIndex) {
            return this.segmentBase.getSegmentTimeUs(segmentIndex) + (this.periodStartMs * 1000);
        }

        public long getDurationUs(int segmentIndex) {
            return this.segmentBase.getSegmentDurationUs(segmentIndex);
        }

        public int getFirstSegmentNum() {
            return this.segmentBase.getFirstSegmentNum();
        }

        public int getLastSegmentNum() {
            return this.segmentBase.getLastSegmentNum();
        }

        public boolean isExplicit() {
            return this.segmentBase.isExplicit();
        }
    }

    public static class SingleSegmentRepresentation extends Representation {
        public final long contentLength;
        private final RangedUri indexUri;
        private final DashSingleSegmentIndex segmentIndex;
        public final Uri uri;

        public static SingleSegmentRepresentation newInstance(long periodStartMs, long periodDurationMs, String contentId, long revisionId, Format format, String uri, long initializationStart, long initializationEnd, long indexStart, long indexEnd, long contentLength) {
            return new SingleSegmentRepresentation(periodStartMs, periodDurationMs, contentId, revisionId, format, new SingleSegmentBase(new RangedUri(uri, null, initializationStart, 1 + (initializationEnd - initializationStart)), 1, 0, uri, indexStart, (indexEnd - indexStart) + 1), contentLength);
        }

        public SingleSegmentRepresentation(long periodStartMs, long periodDurationMs, String contentId, long revisionId, Format format, SingleSegmentBase segmentBase, long contentLength) {
            DashSingleSegmentIndex dashSingleSegmentIndex;
            super(periodStartMs, periodDurationMs, contentId, revisionId, format, segmentBase);
            this.uri = Uri.parse(segmentBase.uri);
            this.indexUri = segmentBase.getIndex();
            this.contentLength = contentLength;
            if (this.indexUri != null) {
                dashSingleSegmentIndex = null;
            } else {
                DashSingleSegmentIndex dashSingleSegmentIndex2 = new DashSingleSegmentIndex(periodStartMs * 1000, periodDurationMs * 1000, new RangedUri(segmentBase.uri, null, 0, -1));
            }
            this.segmentIndex = dashSingleSegmentIndex;
        }

        public RangedUri getIndexUri() {
            return this.indexUri;
        }

        public DashSegmentIndex getIndex() {
            return this.segmentIndex;
        }
    }

    public abstract DashSegmentIndex getIndex();

    public abstract RangedUri getIndexUri();

    public static Representation newInstance(long periodStartMs, long periodDurationMs, String contentId, long revisionId, Format format, SegmentBase segmentBase) {
        if (segmentBase instanceof SingleSegmentBase) {
            return new SingleSegmentRepresentation(periodStartMs, periodDurationMs, contentId, revisionId, format, (SingleSegmentBase) segmentBase, -1);
        } else if (segmentBase instanceof MultiSegmentBase) {
            return new MultiSegmentRepresentation(periodStartMs, periodDurationMs, contentId, revisionId, format, (MultiSegmentBase) segmentBase);
        } else {
            throw new IllegalArgumentException("segmentBase must be of type SingleSegmentBase or MultiSegmentBase");
        }
    }

    private Representation(long periodStartMs, long periodDurationMs, String contentId, long revisionId, Format format, SegmentBase segmentBase) {
        this.periodStartMs = periodStartMs;
        this.periodDurationMs = periodDurationMs;
        this.contentId = contentId;
        this.revisionId = revisionId;
        this.format = format;
        this.initializationUri = segmentBase.getInitialization(this);
        this.presentationTimeOffsetUs = segmentBase.getPresentationTimeOffsetUs();
    }

    public Format getFormat() {
        return this.format;
    }

    public RangedUri getInitializationUri() {
        return this.initializationUri;
    }

    public String getCacheKey() {
        return this.contentId + "." + this.format.id + "." + this.revisionId;
    }
}
