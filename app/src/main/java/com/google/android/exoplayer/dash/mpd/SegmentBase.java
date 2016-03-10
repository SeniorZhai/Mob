package com.google.android.exoplayer.dash.mpd;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.util.Util;
import java.util.List;

public abstract class SegmentBase {
    final RangedUri initialization;
    final long presentationTimeOffset;
    final long timescale;

    public static abstract class MultiSegmentBase extends SegmentBase {
        final long duration;
        final long periodDurationMs;
        final List<SegmentTimelineElement> segmentTimeline;
        final int startNumber;

        public abstract int getLastSegmentNum();

        public abstract RangedUri getSegmentUrl(Representation representation, int i);

        public MultiSegmentBase(RangedUri initialization, long timescale, long presentationTimeOffset, long periodDurationMs, int startNumber, long duration, List<SegmentTimelineElement> segmentTimeline) {
            super(initialization, timescale, presentationTimeOffset);
            this.periodDurationMs = periodDurationMs;
            this.startNumber = startNumber;
            this.duration = duration;
            this.segmentTimeline = segmentTimeline;
        }

        public int getSegmentNum(long timeUs) {
            int firstSegmentNum = getFirstSegmentNum();
            int lowIndex = firstSegmentNum;
            int highIndex = getLastSegmentNum();
            if (this.segmentTimeline == null) {
                int segmentNum = this.startNumber + ((int) (timeUs / ((this.duration * C.MICROS_PER_SECOND) / this.timescale)));
                if (segmentNum < lowIndex) {
                    return lowIndex;
                }
                if (highIndex == -1 || segmentNum <= highIndex) {
                    return segmentNum;
                }
                return highIndex;
            }
            while (lowIndex <= highIndex) {
                int midIndex = (lowIndex + highIndex) / 2;
                long midTimeUs = getSegmentTimeUs(midIndex);
                if (midTimeUs < timeUs) {
                    lowIndex = midIndex + 1;
                } else if (midTimeUs <= timeUs) {
                    return midIndex;
                } else {
                    highIndex = midIndex - 1;
                }
            }
            return lowIndex == firstSegmentNum ? lowIndex : highIndex;
        }

        public final long getSegmentDurationUs(int sequenceNumber) {
            if (this.segmentTimeline != null) {
                return (((SegmentTimelineElement) this.segmentTimeline.get(sequenceNumber - this.startNumber)).duration * C.MICROS_PER_SECOND) / this.timescale;
            }
            return sequenceNumber == getLastSegmentNum() ? (this.periodDurationMs * 1000) - getSegmentTimeUs(sequenceNumber) : (this.duration * C.MICROS_PER_SECOND) / this.timescale;
        }

        public final long getSegmentTimeUs(int sequenceNumber) {
            long unscaledSegmentTime;
            if (this.segmentTimeline != null) {
                unscaledSegmentTime = ((SegmentTimelineElement) this.segmentTimeline.get(sequenceNumber - this.startNumber)).startTime - this.presentationTimeOffset;
            } else {
                unscaledSegmentTime = ((long) (sequenceNumber - this.startNumber)) * this.duration;
            }
            return Util.scaleLargeTimestamp(unscaledSegmentTime, C.MICROS_PER_SECOND, this.timescale);
        }

        public int getFirstSegmentNum() {
            return this.startNumber;
        }

        public boolean isExplicit() {
            return this.segmentTimeline != null;
        }
    }

    public static class SegmentList extends MultiSegmentBase {
        final List<RangedUri> mediaSegments;

        public SegmentList(RangedUri initialization, long timescale, long presentationTimeOffset, long periodDurationMs, int startNumber, long duration, List<SegmentTimelineElement> segmentTimeline, List<RangedUri> mediaSegments) {
            super(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, segmentTimeline);
            this.mediaSegments = mediaSegments;
        }

        public RangedUri getSegmentUrl(Representation representation, int sequenceNumber) {
            return (RangedUri) this.mediaSegments.get(sequenceNumber - this.startNumber);
        }

        public int getLastSegmentNum() {
            return (this.startNumber + this.mediaSegments.size()) - 1;
        }

        public boolean isExplicit() {
            return true;
        }
    }

    public static class SegmentTemplate extends MultiSegmentBase {
        private final String baseUrl;
        final UrlTemplate initializationTemplate;
        final UrlTemplate mediaTemplate;

        public SegmentTemplate(RangedUri initialization, long timescale, long presentationTimeOffset, long periodDurationMs, int startNumber, long duration, List<SegmentTimelineElement> segmentTimeline, UrlTemplate initializationTemplate, UrlTemplate mediaTemplate, String baseUrl) {
            super(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, segmentTimeline);
            this.initializationTemplate = initializationTemplate;
            this.mediaTemplate = mediaTemplate;
            this.baseUrl = baseUrl;
        }

        public RangedUri getInitialization(Representation representation) {
            if (this.initializationTemplate == null) {
                return super.getInitialization(representation);
            }
            return new RangedUri(this.baseUrl, this.initializationTemplate.buildUri(representation.format.id, 0, representation.format.bitrate, 0), 0, -1);
        }

        public RangedUri getSegmentUrl(Representation representation, int sequenceNumber) {
            long time;
            if (this.segmentTimeline != null) {
                time = ((SegmentTimelineElement) this.segmentTimeline.get(sequenceNumber - this.startNumber)).startTime;
            } else {
                time = ((long) (sequenceNumber - this.startNumber)) * this.duration;
            }
            return new RangedUri(this.baseUrl, this.mediaTemplate.buildUri(representation.format.id, sequenceNumber, representation.format.bitrate, time), 0, -1);
        }

        public int getLastSegmentNum() {
            if (this.segmentTimeline != null) {
                return (this.segmentTimeline.size() + this.startNumber) - 1;
            }
            if (this.periodDurationMs == -1) {
                return -1;
            }
            return (this.startNumber + ((int) Util.ceilDivide(this.periodDurationMs, (this.duration * 1000) / this.timescale))) - 1;
        }
    }

    public static class SegmentTimelineElement {
        long duration;
        long startTime;

        public SegmentTimelineElement(long startTime, long duration) {
            this.startTime = startTime;
            this.duration = duration;
        }
    }

    public static class SingleSegmentBase extends SegmentBase {
        final long indexLength;
        final long indexStart;
        public final String uri;

        public SingleSegmentBase(RangedUri initialization, long timescale, long presentationTimeOffset, String uri, long indexStart, long indexLength) {
            super(initialization, timescale, presentationTimeOffset);
            this.uri = uri;
            this.indexStart = indexStart;
            this.indexLength = indexLength;
        }

        public SingleSegmentBase(String uri) {
            this(null, 1, 0, uri, 0, -1);
        }

        public RangedUri getIndex() {
            return this.indexLength <= 0 ? null : new RangedUri(this.uri, null, this.indexStart, this.indexLength);
        }
    }

    public SegmentBase(RangedUri initialization, long timescale, long presentationTimeOffset) {
        this.initialization = initialization;
        this.timescale = timescale;
        this.presentationTimeOffset = presentationTimeOffset;
    }

    public RangedUri getInitialization(Representation representation) {
        return this.initialization;
    }

    public long getPresentationTimeOffsetUs() {
        return Util.scaleLargeTimestamp(this.presentationTimeOffset, C.MICROS_PER_SECOND, this.timescale);
    }
}
