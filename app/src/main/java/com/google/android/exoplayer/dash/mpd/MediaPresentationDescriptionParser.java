package com.google.android.exoplayer.dash.mpd;

import android.text.TextUtils;
import android.util.Base64;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.dash.mpd.SegmentBase.SegmentList;
import com.google.android.exoplayer.dash.mpd.SegmentBase.SegmentTemplate;
import com.google.android.exoplayer.dash.mpd.SegmentBase.SegmentTimelineElement;
import com.google.android.exoplayer.dash.mpd.SegmentBase.SingleSegmentBase;
import com.google.android.exoplayer.extractor.mp4.PsshAtomUtil;
import com.google.android.exoplayer.upstream.UriLoadable.Parser;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.UriUtil;
import com.google.android.exoplayer.util.Util;
import com.helpshift.HSFunnel;
import com.helpshift.constants.MessageColumns;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHeaders;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class MediaPresentationDescriptionParser extends DefaultHandler implements Parser<MediaPresentationDescription> {
    private static final Pattern FRAME_RATE_PATTERN = Pattern.compile("(\\d+)(?:/(\\d+))?");
    private final String contentId;
    private final XmlPullParserFactory xmlParserFactory;

    protected static final class ContentProtectionsBuilder implements Comparator<ContentProtection> {
        private ArrayList<ContentProtection> adaptationSetProtections;
        private ArrayList<ContentProtection> currentRepresentationProtections;
        private ArrayList<ContentProtection> representationProtections;
        private boolean representationProtectionsSet;

        protected ContentProtectionsBuilder() {
        }

        public void addAdaptationSetProtection(ContentProtection contentProtection) {
            if (this.adaptationSetProtections == null) {
                this.adaptationSetProtections = new ArrayList();
            }
            maybeAddContentProtection(this.adaptationSetProtections, contentProtection);
        }

        public void addRepresentationProtection(ContentProtection contentProtection) {
            if (this.currentRepresentationProtections == null) {
                this.currentRepresentationProtections = new ArrayList();
            }
            maybeAddContentProtection(this.currentRepresentationProtections, contentProtection);
        }

        public void endRepresentation() {
            boolean z = true;
            if (!this.representationProtectionsSet) {
                if (this.currentRepresentationProtections != null) {
                    Collections.sort(this.currentRepresentationProtections, this);
                }
                this.representationProtections = this.currentRepresentationProtections;
                this.representationProtectionsSet = true;
            } else if (this.currentRepresentationProtections == null) {
                if (this.representationProtections != null) {
                    z = false;
                }
                Assertions.checkState(z);
            } else {
                Collections.sort(this.currentRepresentationProtections, this);
                Assertions.checkState(this.currentRepresentationProtections.equals(this.representationProtections));
            }
            this.currentRepresentationProtections = null;
        }

        public ArrayList<ContentProtection> build() {
            if (this.adaptationSetProtections == null) {
                return this.representationProtections;
            }
            if (this.representationProtections == null) {
                return this.adaptationSetProtections;
            }
            for (int i = 0; i < this.representationProtections.size(); i++) {
                maybeAddContentProtection(this.adaptationSetProtections, (ContentProtection) this.representationProtections.get(i));
            }
            return this.adaptationSetProtections;
        }

        private void maybeAddContentProtection(List<ContentProtection> contentProtections, ContentProtection contentProtection) {
            if (!contentProtections.contains(contentProtection)) {
                for (int i = 0; i < contentProtections.size(); i++) {
                    Assertions.checkState(!((ContentProtection) contentProtections.get(i)).schemeUriId.equals(contentProtection.schemeUriId));
                }
                contentProtections.add(contentProtection);
            }
        }

        public int compare(ContentProtection first, ContentProtection second) {
            return first.schemeUriId.compareTo(second.schemeUriId);
        }
    }

    public MediaPresentationDescriptionParser() {
        this(null);
    }

    public MediaPresentationDescriptionParser(String contentId) {
        this.contentId = contentId;
        try {
            this.xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Couldn't create XmlPullParserFactory instance", e);
        }
    }

    public MediaPresentationDescription parse(String connectionUrl, InputStream inputStream) throws IOException, ParserException {
        try {
            XmlPullParser xpp = this.xmlParserFactory.newPullParser();
            xpp.setInput(inputStream, null);
            if (xpp.next() == 2 && "MPD".equals(xpp.getName())) {
                return parseMediaPresentationDescription(xpp, connectionUrl);
            }
            throw new ParserException("inputStream does not contain a valid media presentation description");
        } catch (Throwable e) {
            throw new ParserException(e);
        } catch (Throwable e2) {
            throw new ParserException(e2);
        }
    }

    protected MediaPresentationDescription parseMediaPresentationDescription(XmlPullParser xpp, String baseUrl) throws XmlPullParserException, IOException, ParseException {
        long availabilityStartTime = parseDateTime(xpp, "availabilityStartTime", -1);
        long durationMs = parseDuration(xpp, "mediaPresentationDuration", -1);
        long minBufferTimeMs = parseDuration(xpp, "minBufferTime", -1);
        String typeString = xpp.getAttributeValue(null, MessageColumns.TYPE);
        boolean dynamic = typeString != null ? typeString.equals("dynamic") : false;
        long minUpdateTimeMs = dynamic ? parseDuration(xpp, "minimumUpdatePeriod", -1) : -1;
        long timeShiftBufferDepthMs = dynamic ? parseDuration(xpp, "timeShiftBufferDepth", -1) : -1;
        UtcTimingElement utcTiming = null;
        String location = null;
        List<Period> periods = new ArrayList();
        do {
            xpp.next();
            if (isStartTag(xpp, "BaseURL")) {
                baseUrl = parseBaseUrl(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "UTCTiming")) {
                    utcTiming = parseUtcTiming(xpp);
                } else {
                    if (isStartTag(xpp, "Period")) {
                        periods.add(parsePeriod(xpp, baseUrl, durationMs));
                    } else {
                        if (isStartTag(xpp, HttpHeaders.LOCATION)) {
                            location = xpp.nextText();
                        }
                    }
                }
            }
        } while (!isEndTag(xpp, "MPD"));
        return buildMediaPresentationDescription(availabilityStartTime, durationMs, minBufferTimeMs, dynamic, minUpdateTimeMs, timeShiftBufferDepthMs, utcTiming, location, periods);
    }

    protected MediaPresentationDescription buildMediaPresentationDescription(long availabilityStartTime, long durationMs, long minBufferTimeMs, boolean dynamic, long minUpdateTimeMs, long timeShiftBufferDepthMs, UtcTimingElement utcTiming, String location, List<Period> periods) {
        return new MediaPresentationDescription(availabilityStartTime, durationMs, minBufferTimeMs, dynamic, minUpdateTimeMs, timeShiftBufferDepthMs, utcTiming, location, periods);
    }

    protected UtcTimingElement parseUtcTiming(XmlPullParser xpp) {
        return buildUtcTimingElement(xpp.getAttributeValue(null, "schemeIdUri"), xpp.getAttributeValue(null, "value"));
    }

    protected UtcTimingElement buildUtcTimingElement(String schemeIdUri, String value) {
        return new UtcTimingElement(schemeIdUri, value);
    }

    protected Period parsePeriod(XmlPullParser xpp, String baseUrl, long mpdDurationMs) throws XmlPullParserException, IOException {
        String id = xpp.getAttributeValue(null, DBLikedChannelsHelper.KEY_ID);
        long startMs = parseDuration(xpp, "start", 0);
        long durationMs = parseDuration(xpp, "duration", mpdDurationMs);
        SegmentBase segmentBase = null;
        List<AdaptationSet> adaptationSets = new ArrayList();
        do {
            xpp.next();
            if (isStartTag(xpp, "BaseURL")) {
                baseUrl = parseBaseUrl(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "AdaptationSet")) {
                    adaptationSets.add(parseAdaptationSet(xpp, baseUrl, startMs, durationMs, segmentBase));
                } else {
                    if (isStartTag(xpp, "SegmentBase")) {
                        segmentBase = parseSegmentBase(xpp, baseUrl, null);
                    } else {
                        if (isStartTag(xpp, "SegmentList")) {
                            segmentBase = parseSegmentList(xpp, baseUrl, null, durationMs);
                        } else {
                            if (isStartTag(xpp, "SegmentTemplate")) {
                                segmentBase = parseSegmentTemplate(xpp, baseUrl, null, durationMs);
                            }
                        }
                    }
                }
            }
        } while (!isEndTag(xpp, "Period"));
        return buildPeriod(id, startMs, durationMs, adaptationSets);
    }

    protected Period buildPeriod(String id, long startMs, long durationMs, List<AdaptationSet> adaptationSets) {
        return new Period(id, startMs, durationMs, adaptationSets);
    }

    protected AdaptationSet parseAdaptationSet(XmlPullParser xpp, String baseUrl, long periodStartMs, long periodDurationMs, SegmentBase segmentBase) throws XmlPullParserException, IOException {
        int id = parseInt(xpp, DBLikedChannelsHelper.KEY_ID, -1);
        String mimeType = xpp.getAttributeValue(null, "mimeType");
        String language = xpp.getAttributeValue(null, "lang");
        int contentType = parseAdaptationSetType(xpp.getAttributeValue(null, "contentType"));
        if (contentType == -1) {
            contentType = parseAdaptationSetTypeFromMimeType(xpp.getAttributeValue(null, "mimeType"));
        }
        ContentProtectionsBuilder contentProtectionsBuilder = new ContentProtectionsBuilder();
        List<Representation> representations = new ArrayList();
        do {
            xpp.next();
            if (isStartTag(xpp, "BaseURL")) {
                baseUrl = parseBaseUrl(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "ContentProtection")) {
                    contentProtectionsBuilder.addAdaptationSetProtection(parseContentProtection(xpp));
                } else {
                    if (isStartTag(xpp, "ContentComponent")) {
                        language = checkLanguageConsistency(language, xpp.getAttributeValue(null, "lang"));
                        contentType = checkAdaptationSetTypeConsistency(contentType, parseAdaptationSetType(xpp.getAttributeValue(null, "contentType")));
                    } else {
                        if (isStartTag(xpp, "Representation")) {
                            Representation representation = parseRepresentation(xpp, baseUrl, periodStartMs, periodDurationMs, mimeType, language, segmentBase, contentProtectionsBuilder);
                            contentProtectionsBuilder.endRepresentation();
                            contentType = checkAdaptationSetTypeConsistency(contentType, parseAdaptationSetTypeFromMimeType(representation.format.mimeType));
                            representations.add(representation);
                        } else {
                            if (isStartTag(xpp, "SegmentBase")) {
                                segmentBase = parseSegmentBase(xpp, baseUrl, (SingleSegmentBase) segmentBase);
                            } else {
                                if (isStartTag(xpp, "SegmentList")) {
                                    segmentBase = parseSegmentList(xpp, baseUrl, (SegmentList) segmentBase, periodDurationMs);
                                } else {
                                    if (isStartTag(xpp, "SegmentTemplate")) {
                                        segmentBase = parseSegmentTemplate(xpp, baseUrl, (SegmentTemplate) segmentBase, periodDurationMs);
                                    } else if (isStartTag(xpp)) {
                                        parseAdaptationSetChild(xpp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } while (!isEndTag(xpp, "AdaptationSet"));
        return buildAdaptationSet(id, contentType, representations, contentProtectionsBuilder.build());
    }

    protected AdaptationSet buildAdaptationSet(int id, int contentType, List<Representation> representations, List<ContentProtection> contentProtections) {
        return new AdaptationSet(id, contentType, representations, contentProtections);
    }

    protected int parseAdaptationSetType(String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return -1;
        }
        if (MimeTypes.BASE_TYPE_AUDIO.equals(contentType)) {
            return 1;
        }
        if (MimeTypes.BASE_TYPE_VIDEO.equals(contentType)) {
            return 0;
        }
        return MimeTypes.BASE_TYPE_TEXT.equals(contentType) ? 2 : -1;
    }

    protected int parseAdaptationSetTypeFromMimeType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return -1;
        }
        if (MimeTypes.isAudio(mimeType)) {
            return 1;
        }
        if (MimeTypes.isVideo(mimeType)) {
            return 0;
        }
        return (MimeTypes.isText(mimeType) || MimeTypes.isTtml(mimeType)) ? 2 : -1;
    }

    protected ContentProtection parseContentProtection(XmlPullParser xpp) throws XmlPullParserException, IOException {
        String schemeIdUri = xpp.getAttributeValue(null, "schemeIdUri");
        UUID uuid = null;
        byte[] psshAtom = null;
        do {
            xpp.next();
            if (isStartTag(xpp, "cenc:pssh") && xpp.next() == 4) {
                psshAtom = Base64.decode(xpp.getText(), 0);
                uuid = PsshAtomUtil.parseUuid(psshAtom);
                if (uuid == null) {
                    throw new ParserException("Invalid pssh atom in cenc:pssh element");
                }
            }
        } while (!isEndTag(xpp, "ContentProtection"));
        return buildContentProtection(schemeIdUri, uuid, psshAtom);
    }

    protected ContentProtection buildContentProtection(String schemeIdUri, UUID uuid, byte[] data) {
        return new ContentProtection(schemeIdUri, uuid, data);
    }

    protected void parseAdaptationSetChild(XmlPullParser xpp) throws XmlPullParserException, IOException {
    }

    protected Representation parseRepresentation(XmlPullParser xpp, String baseUrl, long periodStartMs, long periodDurationMs, String mimeType, String language, SegmentBase segmentBase, ContentProtectionsBuilder contentProtectionsBuilder) throws XmlPullParserException, IOException {
        SegmentBase segmentBase2;
        String id = xpp.getAttributeValue(null, DBLikedChannelsHelper.KEY_ID);
        int bandwidth = parseInt(xpp, "bandwidth");
        int audioSamplingRate = parseInt(xpp, "audioSamplingRate");
        int width = parseInt(xpp, SettingsJsonConstants.ICON_WIDTH_KEY);
        int height = parseInt(xpp, SettingsJsonConstants.ICON_HEIGHT_KEY);
        float frameRate = -1.0f;
        String frameRateAttribute = xpp.getAttributeValue(null, "frameRate");
        if (frameRateAttribute != null) {
            Matcher frameRateMatcher = FRAME_RATE_PATTERN.matcher(frameRateAttribute);
            if (frameRateMatcher.matches()) {
                int numerator = Integer.parseInt(frameRateMatcher.group(1));
                String denominatorString = frameRateMatcher.group(2);
                frameRate = !TextUtils.isEmpty(denominatorString) ? ((float) numerator) / ((float) Integer.parseInt(denominatorString)) : (float) numerator;
            }
        }
        mimeType = parseString(xpp, "mimeType", mimeType);
        String codecs = parseString(xpp, "codecs", null);
        int numChannels = -1;
        do {
            xpp.next();
            if (isStartTag(xpp, "BaseURL")) {
                baseUrl = parseBaseUrl(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "AudioChannelConfiguration")) {
                    numChannels = Integer.parseInt(xpp.getAttributeValue(null, "value"));
                } else {
                    if (isStartTag(xpp, "SegmentBase")) {
                        segmentBase = parseSegmentBase(xpp, baseUrl, (SingleSegmentBase) segmentBase);
                    } else {
                        if (isStartTag(xpp, "SegmentList")) {
                            segmentBase = parseSegmentList(xpp, baseUrl, (SegmentList) segmentBase, periodDurationMs);
                        } else {
                            if (isStartTag(xpp, "SegmentTemplate")) {
                                segmentBase = parseSegmentTemplate(xpp, baseUrl, (SegmentTemplate) segmentBase, periodDurationMs);
                            } else {
                                if (isStartTag(xpp, "ContentProtection")) {
                                    contentProtectionsBuilder.addRepresentationProtection(parseContentProtection(xpp));
                                }
                            }
                        }
                    }
                }
            }
        } while (!isEndTag(xpp, "Representation"));
        Format format = buildFormat(id, mimeType, width, height, frameRate, numChannels, audioSamplingRate, bandwidth, language, codecs);
        String str = this.contentId;
        if (segmentBase != null) {
            segmentBase2 = segmentBase;
        } else {
            SegmentBase singleSegmentBase = new SingleSegmentBase(baseUrl);
        }
        return buildRepresentation(periodStartMs, periodDurationMs, str, -1, format, segmentBase2);
    }

    protected Format buildFormat(String id, String mimeType, int width, int height, float frameRate, int numChannels, int audioSamplingRate, int bandwidth, String language, String codecs) {
        return new Format(id, mimeType, width, height, frameRate, numChannels, audioSamplingRate, bandwidth, language, codecs);
    }

    protected Representation buildRepresentation(long periodStartMs, long periodDurationMs, String contentId, int revisionId, Format format, SegmentBase segmentBase) {
        return Representation.newInstance(periodStartMs, periodDurationMs, contentId, (long) revisionId, format, segmentBase);
    }

    protected SingleSegmentBase parseSegmentBase(XmlPullParser xpp, String baseUrl, SingleSegmentBase parent) throws XmlPullParserException, IOException {
        long timescale = parseLong(xpp, "timescale", parent != null ? parent.timescale : 1);
        long presentationTimeOffset = parseLong(xpp, "presentationTimeOffset", parent != null ? parent.presentationTimeOffset : 0);
        long indexStart = parent != null ? parent.indexStart : 0;
        long indexLength = parent != null ? parent.indexLength : -1;
        String indexRangeText = xpp.getAttributeValue(null, "indexRange");
        if (indexRangeText != null) {
            String[] indexRange = indexRangeText.split("-");
            indexStart = Long.parseLong(indexRange[0]);
            indexLength = (Long.parseLong(indexRange[1]) - indexStart) + 1;
        }
        RangedUri initialization = parent != null ? parent.initialization : null;
        do {
            xpp.next();
            if (isStartTag(xpp, "Initialization")) {
                initialization = parseInitialization(xpp, baseUrl);
            }
        } while (!isEndTag(xpp, "SegmentBase"));
        return buildSingleSegmentBase(initialization, timescale, presentationTimeOffset, baseUrl, indexStart, indexLength);
    }

    protected SingleSegmentBase buildSingleSegmentBase(RangedUri initialization, long timescale, long presentationTimeOffset, String baseUrl, long indexStart, long indexLength) {
        return new SingleSegmentBase(initialization, timescale, presentationTimeOffset, baseUrl, indexStart, indexLength);
    }

    protected SegmentList parseSegmentList(XmlPullParser xpp, String baseUrl, SegmentList parent, long periodDurationMs) throws XmlPullParserException, IOException {
        long timescale = parseLong(xpp, "timescale", parent != null ? parent.timescale : 1);
        long presentationTimeOffset = parseLong(xpp, "presentationTimeOffset", parent != null ? parent.presentationTimeOffset : 0);
        long duration = parseLong(xpp, "duration", parent != null ? parent.duration : -1);
        int startNumber = parseInt(xpp, "startNumber", parent != null ? parent.startNumber : 1);
        RangedUri initialization = null;
        List<SegmentTimelineElement> timeline = null;
        List<RangedUri> segments = null;
        do {
            xpp.next();
            if (isStartTag(xpp, "Initialization")) {
                initialization = parseInitialization(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "SegmentTimeline")) {
                    timeline = parseSegmentTimeline(xpp);
                } else {
                    if (isStartTag(xpp, "SegmentURL")) {
                        if (segments == null) {
                            segments = new ArrayList();
                        }
                        segments.add(parseSegmentUrl(xpp, baseUrl));
                    }
                }
            }
        } while (!isEndTag(xpp, "SegmentList"));
        if (parent != null) {
            if (initialization == null) {
                initialization = parent.initialization;
            }
            if (timeline == null) {
                timeline = parent.segmentTimeline;
            }
            if (segments == null) {
                segments = parent.mediaSegments;
            }
        }
        return buildSegmentList(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, timeline, segments);
    }

    protected SegmentList buildSegmentList(RangedUri initialization, long timescale, long presentationTimeOffset, long periodDurationMs, int startNumber, long duration, List<SegmentTimelineElement> timeline, List<RangedUri> segments) {
        return new SegmentList(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, timeline, segments);
    }

    protected SegmentTemplate parseSegmentTemplate(XmlPullParser xpp, String baseUrl, SegmentTemplate parent, long periodDurationMs) throws XmlPullParserException, IOException {
        long timescale = parseLong(xpp, "timescale", parent != null ? parent.timescale : 1);
        long presentationTimeOffset = parseLong(xpp, "presentationTimeOffset", parent != null ? parent.presentationTimeOffset : 0);
        long duration = parseLong(xpp, "duration", parent != null ? parent.duration : -1);
        int startNumber = parseInt(xpp, "startNumber", parent != null ? parent.startNumber : 1);
        UrlTemplate mediaTemplate = parseUrlTemplate(xpp, "media", parent != null ? parent.mediaTemplate : null);
        UrlTemplate initializationTemplate = parseUrlTemplate(xpp, "initialization", parent != null ? parent.initializationTemplate : null);
        RangedUri initialization = null;
        List<SegmentTimelineElement> timeline = null;
        do {
            xpp.next();
            if (isStartTag(xpp, "Initialization")) {
                initialization = parseInitialization(xpp, baseUrl);
            } else {
                if (isStartTag(xpp, "SegmentTimeline")) {
                    timeline = parseSegmentTimeline(xpp);
                }
            }
        } while (!isEndTag(xpp, "SegmentTemplate"));
        if (parent != null) {
            if (initialization == null) {
                initialization = parent.initialization;
            }
            if (timeline == null) {
                timeline = parent.segmentTimeline;
            }
        }
        return buildSegmentTemplate(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, timeline, initializationTemplate, mediaTemplate, baseUrl);
    }

    protected SegmentTemplate buildSegmentTemplate(RangedUri initialization, long timescale, long presentationTimeOffset, long periodDurationMs, int startNumber, long duration, List<SegmentTimelineElement> timeline, UrlTemplate initializationTemplate, UrlTemplate mediaTemplate, String baseUrl) {
        return new SegmentTemplate(initialization, timescale, presentationTimeOffset, periodDurationMs, startNumber, duration, timeline, initializationTemplate, mediaTemplate, baseUrl);
    }

    protected List<SegmentTimelineElement> parseSegmentTimeline(XmlPullParser xpp) throws XmlPullParserException, IOException {
        List<SegmentTimelineElement> segmentTimeline = new ArrayList();
        long elapsedTime = 0;
        do {
            xpp.next();
            if (isStartTag(xpp, "S")) {
                elapsedTime = parseLong(xpp, "t", elapsedTime);
                long duration = parseLong(xpp, HSFunnel.LIBRARY_OPENED_DECOMP);
                int count = parseInt(xpp, HSFunnel.REVIEWED_APP, 0) + 1;
                for (int i = 0; i < count; i++) {
                    segmentTimeline.add(buildSegmentTimelineElement(elapsedTime, duration));
                    elapsedTime += duration;
                }
            }
        } while (!isEndTag(xpp, "SegmentTimeline"));
        return segmentTimeline;
    }

    protected SegmentTimelineElement buildSegmentTimelineElement(long elapsedTime, long duration) {
        return new SegmentTimelineElement(elapsedTime, duration);
    }

    protected UrlTemplate parseUrlTemplate(XmlPullParser xpp, String name, UrlTemplate defaultValue) {
        String valueString = xpp.getAttributeValue(null, name);
        if (valueString != null) {
            return UrlTemplate.compile(valueString);
        }
        return defaultValue;
    }

    protected RangedUri parseInitialization(XmlPullParser xpp, String baseUrl) {
        return parseRangedUrl(xpp, baseUrl, "sourceURL", "range");
    }

    protected RangedUri parseSegmentUrl(XmlPullParser xpp, String baseUrl) {
        return parseRangedUrl(xpp, baseUrl, "media", "mediaRange");
    }

    protected RangedUri parseRangedUrl(XmlPullParser xpp, String baseUrl, String urlAttribute, String rangeAttribute) {
        String urlText = xpp.getAttributeValue(null, urlAttribute);
        long rangeStart = 0;
        long rangeLength = -1;
        String rangeText = xpp.getAttributeValue(null, rangeAttribute);
        if (rangeText != null) {
            String[] rangeTextArray = rangeText.split("-");
            rangeStart = Long.parseLong(rangeTextArray[0]);
            if (rangeTextArray.length == 2) {
                rangeLength = (Long.parseLong(rangeTextArray[1]) - rangeStart) + 1;
            }
        }
        return buildRangedUri(baseUrl, urlText, rangeStart, rangeLength);
    }

    protected RangedUri buildRangedUri(String baseUrl, String urlText, long rangeStart, long rangeLength) {
        return new RangedUri(baseUrl, urlText, rangeStart, rangeLength);
    }

    private static String checkLanguageConsistency(String firstLanguage, String secondLanguage) {
        if (firstLanguage == null) {
            return secondLanguage;
        }
        if (secondLanguage == null) {
            return firstLanguage;
        }
        Assertions.checkState(firstLanguage.equals(secondLanguage));
        return firstLanguage;
    }

    private static int checkAdaptationSetTypeConsistency(int firstType, int secondType) {
        if (firstType == -1) {
            return secondType;
        }
        if (secondType == -1) {
            return firstType;
        }
        Assertions.checkState(firstType == secondType);
        return firstType;
    }

    protected static boolean isEndTag(XmlPullParser xpp, String name) throws XmlPullParserException {
        return xpp.getEventType() == 3 && name.equals(xpp.getName());
    }

    protected static boolean isStartTag(XmlPullParser xpp, String name) throws XmlPullParserException {
        return xpp.getEventType() == 2 && name.equals(xpp.getName());
    }

    protected static boolean isStartTag(XmlPullParser xpp) throws XmlPullParserException {
        return xpp.getEventType() == 2;
    }

    protected static long parseDuration(XmlPullParser xpp, String name, long defaultValue) {
        String value = xpp.getAttributeValue(null, name);
        return value == null ? defaultValue : Util.parseXsDuration(value);
    }

    protected static long parseDateTime(XmlPullParser xpp, String name, long defaultValue) throws ParseException {
        String value = xpp.getAttributeValue(null, name);
        return value == null ? defaultValue : Util.parseXsDateTime(value);
    }

    protected static String parseBaseUrl(XmlPullParser xpp, String parentBaseUrl) throws XmlPullParserException, IOException {
        xpp.next();
        return UriUtil.resolve(parentBaseUrl, xpp.getText());
    }

    protected static int parseInt(XmlPullParser xpp, String name) {
        return parseInt(xpp, name, -1);
    }

    protected static int parseInt(XmlPullParser xpp, String name, int defaultValue) {
        String value = xpp.getAttributeValue(null, name);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    protected static long parseLong(XmlPullParser xpp, String name) {
        return parseLong(xpp, name, -1);
    }

    protected static long parseLong(XmlPullParser xpp, String name, long defaultValue) {
        String value = xpp.getAttributeValue(null, name);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    protected static String parseString(XmlPullParser xpp, String name, String defaultValue) {
        String value = xpp.getAttributeValue(null, name);
        return value == null ? defaultValue : value;
    }
}
