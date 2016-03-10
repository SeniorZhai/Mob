package com.google.android.exoplayer.extractor.webm;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Pair;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.drm.DrmInitData.Universal;
import com.google.android.exoplayer.extractor.ChunkIndex;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.LongArray;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.NalUnitUtil;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class WebmExtractor implements Extractor {
    private static final int BLOCK_STATE_DATA = 2;
    private static final int BLOCK_STATE_HEADER = 1;
    private static final int BLOCK_STATE_START = 0;
    private static final String CODEC_ID_AAC = "A_AAC";
    private static final String CODEC_ID_AC3 = "A_AC3";
    private static final String CODEC_ID_H264 = "V_MPEG4/ISO/AVC";
    private static final String CODEC_ID_MP3 = "A_MPEG/L3";
    private static final String CODEC_ID_OPUS = "A_OPUS";
    private static final String CODEC_ID_VORBIS = "A_VORBIS";
    private static final String CODEC_ID_VP8 = "V_VP8";
    private static final String CODEC_ID_VP9 = "V_VP9";
    private static final int CUES_STATE_BUILDING = 1;
    private static final int CUES_STATE_BUILT = 2;
    private static final int CUES_STATE_NOT_BUILT = 0;
    private static final String DOC_TYPE_MATROSKA = "matroska";
    private static final String DOC_TYPE_WEBM = "webm";
    private static final int ENCRYPTION_IV_SIZE = 8;
    private static final int ID_AUDIO = 225;
    private static final int ID_BLOCK = 161;
    private static final int ID_BLOCK_GROUP = 160;
    private static final int ID_CHANNELS = 159;
    private static final int ID_CLUSTER = 524531317;
    private static final int ID_CODEC_DELAY = 22186;
    private static final int ID_CODEC_ID = 134;
    private static final int ID_CODEC_PRIVATE = 25506;
    private static final int ID_CONTENT_COMPRESSION = 20532;
    private static final int ID_CONTENT_COMPRESSION_ALGORITHM = 16980;
    private static final int ID_CONTENT_COMPRESSION_SETTINGS = 16981;
    private static final int ID_CONTENT_ENCODING = 25152;
    private static final int ID_CONTENT_ENCODINGS = 28032;
    private static final int ID_CONTENT_ENCODING_ORDER = 20529;
    private static final int ID_CONTENT_ENCODING_SCOPE = 20530;
    private static final int ID_CONTENT_ENCRYPTION = 20533;
    private static final int ID_CONTENT_ENCRYPTION_AES_SETTINGS = 18407;
    private static final int ID_CONTENT_ENCRYPTION_AES_SETTINGS_CIPHER_MODE = 18408;
    private static final int ID_CONTENT_ENCRYPTION_ALGORITHM = 18401;
    private static final int ID_CONTENT_ENCRYPTION_KEY_ID = 18402;
    private static final int ID_CUES = 475249515;
    private static final int ID_CUE_CLUSTER_POSITION = 241;
    private static final int ID_CUE_POINT = 187;
    private static final int ID_CUE_TIME = 179;
    private static final int ID_CUE_TRACK_POSITIONS = 183;
    private static final int ID_DEFAULT_DURATION = 2352003;
    private static final int ID_DOC_TYPE = 17026;
    private static final int ID_DOC_TYPE_READ_VERSION = 17029;
    private static final int ID_DURATION = 17545;
    private static final int ID_EBML = 440786851;
    private static final int ID_EBML_READ_VERSION = 17143;
    private static final int ID_INFO = 357149030;
    private static final int ID_PIXEL_HEIGHT = 186;
    private static final int ID_PIXEL_WIDTH = 176;
    private static final int ID_REFERENCE_BLOCK = 251;
    private static final int ID_SAMPLING_FREQUENCY = 181;
    private static final int ID_SEEK = 19899;
    private static final int ID_SEEK_HEAD = 290298740;
    private static final int ID_SEEK_ID = 21419;
    private static final int ID_SEEK_POSITION = 21420;
    private static final int ID_SEEK_PRE_ROLL = 22203;
    private static final int ID_SEGMENT = 408125543;
    private static final int ID_SIMPLE_BLOCK = 163;
    private static final int ID_TIMECODE_SCALE = 2807729;
    private static final int ID_TIME_CODE = 231;
    private static final int ID_TRACKS = 374648427;
    private static final int ID_TRACK_ENTRY = 174;
    private static final int ID_TRACK_NUMBER = 215;
    private static final int ID_TRACK_TYPE = 131;
    private static final int ID_VIDEO = 224;
    private static final int LACING_EBML = 3;
    private static final int LACING_FIXED_SIZE = 2;
    private static final int LACING_NONE = 0;
    private static final int LACING_XIPH = 1;
    private static final int MP3_MAX_INPUT_SIZE = 4096;
    private static final int OPUS_MAX_INPUT_SIZE = 5760;
    private static final int TRACK_TYPE_AUDIO = 2;
    private static final int TRACK_TYPE_VIDEO = 1;
    private static final int UNKNOWN = -1;
    private static final int VORBIS_MAX_INPUT_SIZE = 8192;
    private TrackFormat audioTrackFormat;
    private byte[] blockEncryptionKeyId;
    private int blockFlags;
    private int blockLacingSampleCount;
    private int blockLacingSampleIndex;
    private int[] blockLacingSampleSizes;
    private int blockState;
    private long blockTimeUs;
    private int blockTrackNumber;
    private int blockTrackNumberLength;
    private long clusterTimecodeUs;
    private LongArray cueClusterPositions;
    private LongArray cueTimesUs;
    private long cuesContentPosition;
    private int cuesState;
    private long durationUs;
    private ExtractorOutput extractorOutput;
    private final ParsableByteArray nalLength;
    private final ParsableByteArray nalStartCode;
    private final EbmlReader reader;
    private int sampleBytesRead;
    private int sampleBytesWritten;
    private int sampleCurrentNalBytesRemaining;
    private boolean sampleEncodingHandled;
    private boolean sampleRead;
    private boolean sampleSeenReferenceBlock;
    private final ParsableByteArray sampleStrippedBytes;
    private final ParsableByteArray scratch;
    private int seekEntryId;
    private final ParsableByteArray seekEntryIdBytes;
    private long seekEntryPosition;
    private boolean seekForCues;
    private long seekPositionAfterBuildingCues;
    private boolean seenClusterPositionForCurrentCuePoint;
    private long segmentContentPosition;
    private long segmentContentSize;
    private boolean sentDrmInitData;
    private long timecodeScale;
    private TrackFormat trackFormat;
    private final VarintReader varintReader;
    private TrackFormat videoTrackFormat;
    private final ParsableByteArray vorbisNumPageSamples;

    private final class InnerEbmlReaderOutput implements EbmlReaderOutput {
        private InnerEbmlReaderOutput() {
        }

        public int getElementType(int id) {
            return WebmExtractor.this.getElementType(id);
        }

        public void startMasterElement(int id, long contentPosition, long contentSize) throws ParserException {
            WebmExtractor.this.startMasterElement(id, contentPosition, contentSize);
        }

        public void endMasterElement(int id) throws ParserException {
            WebmExtractor.this.endMasterElement(id);
        }

        public void integerElement(int id, long value) throws ParserException {
            WebmExtractor.this.integerElement(id, value);
        }

        public void floatElement(int id, double value) {
            WebmExtractor.this.floatElement(id, value);
        }

        public void stringElement(int id, String value) throws ParserException {
            WebmExtractor.this.stringElement(id, value);
        }

        public void binaryElement(int id, int contentsSize, ExtractorInput input) throws IOException, InterruptedException {
            WebmExtractor.this.binaryElement(id, contentsSize, input);
        }
    }

    private static final class TrackFormat {
        public int channelCount;
        public long codecDelayNs;
        public String codecId;
        public byte[] codecPrivate;
        public int defaultSampleDurationNs;
        public byte[] encryptionKeyId;
        public boolean hasContentEncryption;
        public int nalUnitLengthFieldLength;
        public int number;
        public int pixelHeight;
        public int pixelWidth;
        public int sampleRate;
        public byte[] sampleStrippedBytes;
        public long seekPreRollNs;
        public TrackOutput trackOutput;
        public int type;

        private TrackFormat() {
            this.number = WebmExtractor.UNKNOWN;
            this.type = WebmExtractor.UNKNOWN;
            this.defaultSampleDurationNs = WebmExtractor.UNKNOWN;
            this.pixelWidth = WebmExtractor.UNKNOWN;
            this.pixelHeight = WebmExtractor.UNKNOWN;
            this.nalUnitLengthFieldLength = WebmExtractor.UNKNOWN;
            this.channelCount = WebmExtractor.UNKNOWN;
            this.sampleRate = WebmExtractor.UNKNOWN;
            this.codecDelayNs = -1;
            this.seekPreRollNs = -1;
        }

        public MediaFormat getMediaFormat(long durationUs) throws ParserException {
            String mimeType;
            List<byte[]> initializationData = null;
            int maxInputSize = WebmExtractor.UNKNOWN;
            String str = this.codecId;
            int i = WebmExtractor.UNKNOWN;
            switch (str.hashCode()) {
                case -1730367663:
                    if (str.equals(WebmExtractor.CODEC_ID_VORBIS)) {
                        i = WebmExtractor.LACING_EBML;
                        break;
                    }
                    break;
                case -1482641357:
                    if (str.equals(WebmExtractor.CODEC_ID_MP3)) {
                        i = 6;
                        break;
                    }
                    break;
                case -538363109:
                    if (str.equals(WebmExtractor.CODEC_ID_H264)) {
                        i = WebmExtractor.TRACK_TYPE_AUDIO;
                        break;
                    }
                    break;
                case 62923557:
                    if (str.equals(WebmExtractor.CODEC_ID_AAC)) {
                        i = 5;
                        break;
                    }
                    break;
                case 62923603:
                    if (str.equals(WebmExtractor.CODEC_ID_AC3)) {
                        i = 7;
                        break;
                    }
                    break;
                case 82338133:
                    if (str.equals(WebmExtractor.CODEC_ID_VP8)) {
                        i = WebmExtractor.LACING_NONE;
                        break;
                    }
                    break;
                case 82338134:
                    if (str.equals(WebmExtractor.CODEC_ID_VP9)) {
                        i = WebmExtractor.TRACK_TYPE_VIDEO;
                        break;
                    }
                    break;
                case 1951062397:
                    if (str.equals(WebmExtractor.CODEC_ID_OPUS)) {
                        i = 4;
                        break;
                    }
                    break;
            }
            switch (i) {
                case WebmExtractor.LACING_NONE /*0*/:
                    mimeType = MimeTypes.VIDEO_VP8;
                    break;
                case WebmExtractor.TRACK_TYPE_VIDEO /*1*/:
                    mimeType = MimeTypes.VIDEO_VP9;
                    break;
                case WebmExtractor.TRACK_TYPE_AUDIO /*2*/:
                    mimeType = MimeTypes.VIDEO_H264;
                    Pair<List<byte[]>, Integer> h264Data = parseH264CodecPrivate(new ParsableByteArray(this.codecPrivate));
                    initializationData = h264Data.first;
                    this.nalUnitLengthFieldLength = ((Integer) h264Data.second).intValue();
                    break;
                case WebmExtractor.LACING_EBML /*3*/:
                    mimeType = MimeTypes.AUDIO_VORBIS;
                    maxInputSize = WebmExtractor.VORBIS_MAX_INPUT_SIZE;
                    initializationData = parseVorbisCodecPrivate(this.codecPrivate);
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    mimeType = MimeTypes.AUDIO_OPUS;
                    maxInputSize = WebmExtractor.OPUS_MAX_INPUT_SIZE;
                    initializationData = new ArrayList(WebmExtractor.LACING_EBML);
                    initializationData.add(this.codecPrivate);
                    initializationData.add(ByteBuffer.allocate(64).putLong(this.codecDelayNs).array());
                    initializationData.add(ByteBuffer.allocate(64).putLong(this.seekPreRollNs).array());
                    break;
                case Player.STATE_ENDED /*5*/:
                    mimeType = MimeTypes.AUDIO_AAC;
                    initializationData = Collections.singletonList(this.codecPrivate);
                    break;
                case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    maxInputSize = WebmExtractor.MP3_MAX_INPUT_SIZE;
                    mimeType = MimeTypes.AUDIO_MPEG;
                    break;
                case DayPickerView.DAYS_PER_WEEK /*7*/:
                    mimeType = MimeTypes.AUDIO_AC3;
                    break;
                default:
                    throw new ParserException("Unrecognized codec identifier.");
            }
            if (MimeTypes.isAudio(mimeType)) {
                return MediaFormat.createAudioFormat(mimeType, maxInputSize, durationUs, this.channelCount, this.sampleRate, initializationData);
            } else if (MimeTypes.isVideo(mimeType)) {
                return MediaFormat.createVideoFormat(mimeType, maxInputSize, durationUs, this.pixelWidth, this.pixelHeight, initializationData);
            } else {
                throw new ParserException("Unexpected MIME type.");
            }
        }

        private static Pair<List<byte[]>, Integer> parseH264CodecPrivate(ParsableByteArray buffer) throws ParserException {
            try {
                buffer.setPosition(4);
                int nalUnitLengthFieldLength = (buffer.readUnsignedByte() & WebmExtractor.LACING_EBML) + WebmExtractor.TRACK_TYPE_VIDEO;
                Assertions.checkState(nalUnitLengthFieldLength != WebmExtractor.LACING_EBML);
                List<byte[]> initializationData = new ArrayList();
                int numSequenceParameterSets = buffer.readUnsignedByte() & 31;
                for (int i = WebmExtractor.LACING_NONE; i < numSequenceParameterSets; i += WebmExtractor.TRACK_TYPE_VIDEO) {
                    initializationData.add(NalUnitUtil.parseChildNalUnit(buffer));
                }
                int numPictureParameterSets = buffer.readUnsignedByte();
                for (int j = WebmExtractor.LACING_NONE; j < numPictureParameterSets; j += WebmExtractor.TRACK_TYPE_VIDEO) {
                    initializationData.add(NalUnitUtil.parseChildNalUnit(buffer));
                }
                return Pair.create(initializationData, Integer.valueOf(nalUnitLengthFieldLength));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ParserException("Error parsing vorbis codec private");
            }
        }

        private static List<byte[]> parseVorbisCodecPrivate(byte[] codecPrivate) throws ParserException {
            try {
                if (codecPrivate[WebmExtractor.LACING_NONE] != (byte) 2) {
                    throw new ParserException("Error parsing vorbis codec private");
                }
                int vorbisInfoLength = WebmExtractor.LACING_NONE;
                int offset = WebmExtractor.TRACK_TYPE_VIDEO;
                while (codecPrivate[offset] == (byte) -1) {
                    vorbisInfoLength += SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                    offset += WebmExtractor.TRACK_TYPE_VIDEO;
                }
                int offset2 = offset + WebmExtractor.TRACK_TYPE_VIDEO;
                vorbisInfoLength += codecPrivate[offset];
                int vorbisSkipLength = WebmExtractor.LACING_NONE;
                offset = offset2;
                while (codecPrivate[offset] == (byte) -1) {
                    vorbisSkipLength += SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                    offset += WebmExtractor.TRACK_TYPE_VIDEO;
                }
                offset2 = offset + WebmExtractor.TRACK_TYPE_VIDEO;
                vorbisSkipLength += codecPrivate[offset];
                if (codecPrivate[offset2] != (byte) 1) {
                    throw new ParserException("Error parsing vorbis codec private");
                }
                byte[] vorbisInfo = new byte[vorbisInfoLength];
                System.arraycopy(codecPrivate, offset2, vorbisInfo, WebmExtractor.LACING_NONE, vorbisInfoLength);
                offset2 += vorbisInfoLength;
                if (codecPrivate[offset2] != (byte) 3) {
                    throw new ParserException("Error parsing vorbis codec private");
                }
                offset2 += vorbisSkipLength;
                if (codecPrivate[offset2] != (byte) 5) {
                    throw new ParserException("Error parsing vorbis codec private");
                }
                byte[] vorbisBooks = new byte[(codecPrivate.length - offset2)];
                System.arraycopy(codecPrivate, offset2, vorbisBooks, WebmExtractor.LACING_NONE, codecPrivate.length - offset2);
                List<byte[]> initializationData = new ArrayList(WebmExtractor.TRACK_TYPE_AUDIO);
                initializationData.add(vorbisInfo);
                initializationData.add(vorbisBooks);
                return initializationData;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new ParserException("Error parsing vorbis codec private");
            }
        }
    }

    public WebmExtractor() {
        this(new DefaultEbmlReader());
    }

    WebmExtractor(EbmlReader reader) {
        this.segmentContentPosition = -1;
        this.segmentContentSize = -1;
        this.timecodeScale = C.MICROS_PER_SECOND;
        this.durationUs = -1;
        this.cuesContentPosition = -1;
        this.seekPositionAfterBuildingCues = -1;
        this.cuesState = LACING_NONE;
        this.clusterTimecodeUs = -1;
        this.reader = reader;
        this.reader.init(new InnerEbmlReaderOutput());
        this.varintReader = new VarintReader();
        this.scratch = new ParsableByteArray(4);
        this.vorbisNumPageSamples = new ParsableByteArray(ByteBuffer.allocate(4).putInt(UNKNOWN).array());
        this.seekEntryIdBytes = new ParsableByteArray(4);
        this.nalStartCode = new ParsableByteArray(NalUnitUtil.NAL_START_CODE);
        this.nalLength = new ParsableByteArray(4);
        this.sampleStrippedBytes = new ParsableByteArray();
    }

    public void init(ExtractorOutput output) {
        this.extractorOutput = output;
    }

    public void seek() {
        this.clusterTimecodeUs = -1;
        this.blockState = LACING_NONE;
        this.reader.reset();
        this.varintReader.reset();
        resetSample();
    }

    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        this.sampleRead = false;
        boolean continueReading = true;
        while (continueReading && !this.sampleRead) {
            continueReading = this.reader.read(input);
            if (continueReading && maybeSeekForCues(seekPosition, input.getPosition())) {
                return TRACK_TYPE_VIDEO;
            }
        }
        if (continueReading) {
            return LACING_NONE;
        }
        return UNKNOWN;
    }

    int getElementType(int id) {
        switch (id) {
            case ID_TRACK_TYPE /*131*/:
            case ID_CHANNELS /*159*/:
            case ID_PIXEL_WIDTH /*176*/:
            case ID_CUE_TIME /*179*/:
            case ID_PIXEL_HEIGHT /*186*/:
            case ID_TRACK_NUMBER /*215*/:
            case ID_TIME_CODE /*231*/:
            case ID_CUE_CLUSTER_POSITION /*241*/:
            case ID_REFERENCE_BLOCK /*251*/:
            case ID_CONTENT_COMPRESSION_ALGORITHM /*16980*/:
            case ID_DOC_TYPE_READ_VERSION /*17029*/:
            case ID_EBML_READ_VERSION /*17143*/:
            case ID_CONTENT_ENCRYPTION_ALGORITHM /*18401*/:
            case ID_CONTENT_ENCRYPTION_AES_SETTINGS_CIPHER_MODE /*18408*/:
            case ID_CONTENT_ENCODING_ORDER /*20529*/:
            case ID_CONTENT_ENCODING_SCOPE /*20530*/:
            case ID_SEEK_POSITION /*21420*/:
            case ID_CODEC_DELAY /*22186*/:
            case ID_SEEK_PRE_ROLL /*22203*/:
            case ID_DEFAULT_DURATION /*2352003*/:
            case ID_TIMECODE_SCALE /*2807729*/:
                return TRACK_TYPE_AUDIO;
            case ID_CODEC_ID /*134*/:
            case ID_DOC_TYPE /*17026*/:
                return LACING_EBML;
            case ID_BLOCK_GROUP /*160*/:
            case ID_TRACK_ENTRY /*174*/:
            case ID_CUE_TRACK_POSITIONS /*183*/:
            case ID_CUE_POINT /*187*/:
            case ID_VIDEO /*224*/:
            case ID_AUDIO /*225*/:
            case ID_CONTENT_ENCRYPTION_AES_SETTINGS /*18407*/:
            case ID_SEEK /*19899*/:
            case ID_CONTENT_COMPRESSION /*20532*/:
            case ID_CONTENT_ENCRYPTION /*20533*/:
            case ID_CONTENT_ENCODING /*25152*/:
            case ID_CONTENT_ENCODINGS /*28032*/:
            case ID_SEEK_HEAD /*290298740*/:
            case ID_INFO /*357149030*/:
            case ID_TRACKS /*374648427*/:
            case ID_SEGMENT /*408125543*/:
            case ID_EBML /*440786851*/:
            case ID_CUES /*475249515*/:
            case ID_CLUSTER /*524531317*/:
                return TRACK_TYPE_VIDEO;
            case ID_BLOCK /*161*/:
            case ID_SIMPLE_BLOCK /*163*/:
            case ID_CONTENT_COMPRESSION_SETTINGS /*16981*/:
            case ID_CONTENT_ENCRYPTION_KEY_ID /*18402*/:
            case ID_SEEK_ID /*21419*/:
            case ID_CODEC_PRIVATE /*25506*/:
                return 4;
            case ID_SAMPLING_FREQUENCY /*181*/:
            case ID_DURATION /*17545*/:
                return 5;
            default:
                return LACING_NONE;
        }
    }

    void startMasterElement(int id, long contentPosition, long contentSize) throws ParserException {
        switch (id) {
            case ID_BLOCK_GROUP /*160*/:
                this.sampleSeenReferenceBlock = false;
                return;
            case ID_TRACK_ENTRY /*174*/:
                this.trackFormat = new TrackFormat();
                return;
            case ID_CUE_POINT /*187*/:
                this.seenClusterPositionForCurrentCuePoint = false;
                return;
            case ID_SEEK /*19899*/:
                this.seekEntryId = UNKNOWN;
                this.seekEntryPosition = -1;
                return;
            case ID_CONTENT_ENCRYPTION /*20533*/:
                this.trackFormat.hasContentEncryption = true;
                return;
            case ID_SEGMENT /*408125543*/:
                if (this.segmentContentPosition == -1 || this.segmentContentPosition == contentPosition) {
                    this.segmentContentPosition = contentPosition;
                    this.segmentContentSize = contentSize;
                    return;
                }
                throw new ParserException("Multiple Segment elements not supported");
            case ID_CUES /*475249515*/:
                this.cueTimesUs = new LongArray();
                this.cueClusterPositions = new LongArray();
                return;
            case ID_CLUSTER /*524531317*/:
                if (this.cuesState == 0 && this.cuesContentPosition != -1) {
                    this.seekForCues = true;
                    return;
                }
                return;
            default:
                return;
        }
    }

    void endMasterElement(int id) throws ParserException {
        switch (id) {
            case ID_BLOCK_GROUP /*160*/:
                if (this.blockState == TRACK_TYPE_AUDIO) {
                    if (!this.sampleSeenReferenceBlock) {
                        this.blockFlags |= TRACK_TYPE_VIDEO;
                    }
                    TrackOutput trackOutput = (this.audioTrackFormat == null || this.blockTrackNumber != this.audioTrackFormat.number) ? this.videoTrackFormat.trackOutput : this.audioTrackFormat.trackOutput;
                    outputSampleMetadata(trackOutput, this.blockTimeUs);
                    this.blockState = LACING_NONE;
                    return;
                }
                return;
            case ID_TRACK_ENTRY /*174*/:
                if (this.trackFormat.number == UNKNOWN || this.trackFormat.type == UNKNOWN) {
                    throw new ParserException("Mandatory element TrackNumber or TrackType not found");
                } else if ((this.trackFormat.type != TRACK_TYPE_AUDIO || this.audioTrackFormat == null) && (this.trackFormat.type != TRACK_TYPE_VIDEO || this.videoTrackFormat == null)) {
                    if (this.trackFormat.type == TRACK_TYPE_AUDIO && isCodecSupported(this.trackFormat.codecId)) {
                        this.audioTrackFormat = this.trackFormat;
                        this.audioTrackFormat.trackOutput = this.extractorOutput.track(this.audioTrackFormat.number);
                        this.audioTrackFormat.trackOutput.format(this.audioTrackFormat.getMediaFormat(this.durationUs));
                    } else if (this.trackFormat.type == TRACK_TYPE_VIDEO && isCodecSupported(this.trackFormat.codecId)) {
                        this.videoTrackFormat = this.trackFormat;
                        this.videoTrackFormat.trackOutput = this.extractorOutput.track(this.videoTrackFormat.number);
                        this.videoTrackFormat.trackOutput.format(this.videoTrackFormat.getMediaFormat(this.durationUs));
                    }
                    this.trackFormat = null;
                    return;
                } else {
                    this.trackFormat = null;
                    return;
                }
            case ID_SEEK /*19899*/:
                if (this.seekEntryId == UNKNOWN || this.seekEntryPosition == -1) {
                    throw new ParserException("Mandatory element SeekID or SeekPosition not found");
                } else if (this.seekEntryId == ID_CUES) {
                    this.cuesContentPosition = this.seekEntryPosition;
                    return;
                } else {
                    return;
                }
            case ID_CONTENT_ENCODING /*25152*/:
                if (!this.trackFormat.hasContentEncryption) {
                    return;
                }
                if (this.trackFormat.encryptionKeyId == null) {
                    throw new ParserException("Encrypted Track found but ContentEncKeyID was not found");
                } else if (!this.sentDrmInitData) {
                    this.extractorOutput.drmInitData(new Universal(MimeTypes.VIDEO_WEBM, this.trackFormat.encryptionKeyId));
                    this.sentDrmInitData = true;
                    return;
                } else {
                    return;
                }
            case ID_CONTENT_ENCODINGS /*28032*/:
                if (this.trackFormat.hasContentEncryption && this.trackFormat.sampleStrippedBytes != null) {
                    throw new ParserException("Combining encryption and compression is not supported");
                }
                return;
            case ID_TRACKS /*374648427*/:
                if (this.videoTrackFormat == null && this.audioTrackFormat == null) {
                    throw new ParserException("No valid tracks were found");
                }
                this.extractorOutput.endTracks();
                return;
            case ID_CUES /*475249515*/:
                if (this.cuesState != TRACK_TYPE_AUDIO) {
                    this.extractorOutput.seekMap(buildCues());
                    this.cuesState = TRACK_TYPE_AUDIO;
                    return;
                }
                return;
            default:
                return;
        }
    }

    void integerElement(int id, long value) throws ParserException {
        switch (id) {
            case ID_TRACK_TYPE /*131*/:
                this.trackFormat.type = (int) value;
                return;
            case ID_CHANNELS /*159*/:
                this.trackFormat.channelCount = (int) value;
                return;
            case ID_PIXEL_WIDTH /*176*/:
                this.trackFormat.pixelWidth = (int) value;
                return;
            case ID_CUE_TIME /*179*/:
                this.cueTimesUs.add(scaleTimecodeToUs(value));
                return;
            case ID_PIXEL_HEIGHT /*186*/:
                this.trackFormat.pixelHeight = (int) value;
                return;
            case ID_TRACK_NUMBER /*215*/:
                this.trackFormat.number = (int) value;
                return;
            case ID_TIME_CODE /*231*/:
                this.clusterTimecodeUs = scaleTimecodeToUs(value);
                return;
            case ID_CUE_CLUSTER_POSITION /*241*/:
                if (!this.seenClusterPositionForCurrentCuePoint) {
                    this.cueClusterPositions.add(value);
                    this.seenClusterPositionForCurrentCuePoint = true;
                    return;
                }
                return;
            case ID_REFERENCE_BLOCK /*251*/:
                this.sampleSeenReferenceBlock = true;
                return;
            case ID_CONTENT_COMPRESSION_ALGORITHM /*16980*/:
                if (value != 3) {
                    throw new ParserException("ContentCompAlgo " + value + " not supported");
                }
                return;
            case ID_DOC_TYPE_READ_VERSION /*17029*/:
                if (value < 1 || value > 2) {
                    throw new ParserException("DocTypeReadVersion " + value + " not supported");
                }
                return;
            case ID_EBML_READ_VERSION /*17143*/:
                if (value != 1) {
                    throw new ParserException("EBMLReadVersion " + value + " not supported");
                }
                return;
            case ID_CONTENT_ENCRYPTION_ALGORITHM /*18401*/:
                if (value != 5) {
                    throw new ParserException("ContentEncAlgo " + value + " not supported");
                }
                return;
            case ID_CONTENT_ENCRYPTION_AES_SETTINGS_CIPHER_MODE /*18408*/:
                if (value != 1) {
                    throw new ParserException("AESSettingsCipherMode " + value + " not supported");
                }
                return;
            case ID_CONTENT_ENCODING_ORDER /*20529*/:
                if (value != 0) {
                    throw new ParserException("ContentEncodingOrder " + value + " not supported");
                }
                return;
            case ID_CONTENT_ENCODING_SCOPE /*20530*/:
                if (value != 1) {
                    throw new ParserException("ContentEncodingScope " + value + " not supported");
                }
                return;
            case ID_SEEK_POSITION /*21420*/:
                this.seekEntryPosition = this.segmentContentPosition + value;
                return;
            case ID_CODEC_DELAY /*22186*/:
                this.trackFormat.codecDelayNs = value;
                return;
            case ID_SEEK_PRE_ROLL /*22203*/:
                this.trackFormat.seekPreRollNs = value;
                return;
            case ID_DEFAULT_DURATION /*2352003*/:
                this.trackFormat.defaultSampleDurationNs = (int) value;
                return;
            case ID_TIMECODE_SCALE /*2807729*/:
                this.timecodeScale = value;
                return;
            default:
                return;
        }
    }

    void floatElement(int id, double value) {
        switch (id) {
            case ID_SAMPLING_FREQUENCY /*181*/:
                this.trackFormat.sampleRate = (int) value;
                return;
            case ID_DURATION /*17545*/:
                this.durationUs = scaleTimecodeToUs((long) value);
                return;
            default:
                return;
        }
    }

    void stringElement(int id, String value) throws ParserException {
        switch (id) {
            case ID_CODEC_ID /*134*/:
                this.trackFormat.codecId = value;
                return;
            case ID_DOC_TYPE /*17026*/:
                if (!DOC_TYPE_WEBM.equals(value) && !DOC_TYPE_MATROSKA.equals(value)) {
                    throw new ParserException("DocType " + value + " not supported");
                }
                return;
            default:
                return;
        }
    }

    void binaryElement(int id, int contentSize, ExtractorInput input) throws IOException, InterruptedException {
        switch (id) {
            case ID_BLOCK /*161*/:
            case ID_SIMPLE_BLOCK /*163*/:
                if (this.blockState == 0) {
                    this.blockTrackNumber = (int) this.varintReader.readUnsignedVarint(input, false, true);
                    this.blockTrackNumberLength = this.varintReader.getLastLength();
                    this.blockState = TRACK_TYPE_VIDEO;
                    this.scratch.reset();
                }
                if ((this.audioTrackFormat == null || this.videoTrackFormat == null || this.audioTrackFormat.number == this.blockTrackNumber || this.videoTrackFormat.number == this.blockTrackNumber) && ((this.audioTrackFormat == null || this.videoTrackFormat != null || this.audioTrackFormat.number == this.blockTrackNumber) && (this.audioTrackFormat != null || this.videoTrackFormat == null || this.videoTrackFormat.number == this.blockTrackNumber))) {
                    TrackFormat sampleTrackFormat;
                    if (this.audioTrackFormat == null || this.blockTrackNumber != this.audioTrackFormat.number) {
                        sampleTrackFormat = this.videoTrackFormat;
                    } else {
                        sampleTrackFormat = this.audioTrackFormat;
                    }
                    TrackOutput trackOutput = sampleTrackFormat.trackOutput;
                    if (this.blockState == TRACK_TYPE_VIDEO) {
                        int i;
                        readScratch(input, LACING_EBML);
                        int lacing = (this.scratch.data[TRACK_TYPE_AUDIO] & 6) >> TRACK_TYPE_VIDEO;
                        if (lacing == 0) {
                            this.blockLacingSampleCount = TRACK_TYPE_VIDEO;
                            this.blockLacingSampleSizes = ensureArrayCapacity(this.blockLacingSampleSizes, TRACK_TYPE_VIDEO);
                            this.blockLacingSampleSizes[LACING_NONE] = (contentSize - this.blockTrackNumberLength) - 3;
                        } else if (id != ID_SIMPLE_BLOCK) {
                            throw new ParserException("Lacing only supported in SimpleBlocks.");
                        } else {
                            readScratch(input, 4);
                            this.blockLacingSampleCount = (this.scratch.data[LACING_EBML] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) + TRACK_TYPE_VIDEO;
                            this.blockLacingSampleSizes = ensureArrayCapacity(this.blockLacingSampleSizes, this.blockLacingSampleCount);
                            if (lacing == TRACK_TYPE_AUDIO) {
                                Arrays.fill(this.blockLacingSampleSizes, LACING_NONE, this.blockLacingSampleCount, ((contentSize - this.blockTrackNumberLength) - 4) / this.blockLacingSampleCount);
                            } else if (lacing == TRACK_TYPE_VIDEO) {
                                totalSamplesSize = LACING_NONE;
                                headerSize = 4;
                                for (sampleIndex = LACING_NONE; sampleIndex < this.blockLacingSampleCount + UNKNOWN; sampleIndex += TRACK_TYPE_VIDEO) {
                                    this.blockLacingSampleSizes[sampleIndex] = LACING_NONE;
                                    int byteValue;
                                    do {
                                        headerSize += TRACK_TYPE_VIDEO;
                                        readScratch(input, headerSize);
                                        byteValue = this.scratch.data[headerSize + UNKNOWN] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                                        r26 = this.blockLacingSampleSizes;
                                        r26[sampleIndex] = r26[sampleIndex] + byteValue;
                                    } while (byteValue == 255);
                                    totalSamplesSize += this.blockLacingSampleSizes[sampleIndex];
                                }
                                this.blockLacingSampleSizes[this.blockLacingSampleCount + UNKNOWN] = ((contentSize - this.blockTrackNumberLength) - headerSize) - totalSamplesSize;
                            } else if (lacing == LACING_EBML) {
                                totalSamplesSize = LACING_NONE;
                                headerSize = 4;
                                sampleIndex = LACING_NONE;
                                while (sampleIndex < this.blockLacingSampleCount + UNKNOWN) {
                                    this.blockLacingSampleSizes[sampleIndex] = LACING_NONE;
                                    headerSize += TRACK_TYPE_VIDEO;
                                    readScratch(input, headerSize);
                                    if (this.scratch.data[headerSize + UNKNOWN] == (byte) 0) {
                                        throw new ParserException("No valid varint length mask found");
                                    }
                                    long readValue = 0;
                                    int i2 = LACING_NONE;
                                    while (i2 < ENCRYPTION_IV_SIZE) {
                                        int lengthMask = TRACK_TYPE_VIDEO << (7 - i2);
                                        if ((this.scratch.data[headerSize + UNKNOWN] & lengthMask) != 0) {
                                            int readPosition = headerSize + UNKNOWN;
                                            headerSize += i2;
                                            readScratch(input, headerSize);
                                            readValue = (long) ((this.scratch.data[readPosition] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) & (lengthMask ^ UNKNOWN));
                                            for (int readPosition2 = readPosition + TRACK_TYPE_VIDEO; readPosition2 < headerSize; readPosition2 += TRACK_TYPE_VIDEO) {
                                                readValue = (readValue << ENCRYPTION_IV_SIZE) | ((long) (this.scratch.data[readPosition2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
                                            }
                                            if (sampleIndex > 0) {
                                                readValue -= (1 << ((i2 * 7) + 6)) - 1;
                                            }
                                            if (readValue >= -2147483648L || readValue > 2147483647L) {
                                                throw new ParserException("EBML lacing sample size out of range.");
                                            }
                                            int intReadValue = (int) readValue;
                                            r26 = this.blockLacingSampleSizes;
                                            if (sampleIndex != 0) {
                                                intReadValue += this.blockLacingSampleSizes[sampleIndex + UNKNOWN];
                                            }
                                            r26[sampleIndex] = intReadValue;
                                            totalSamplesSize += this.blockLacingSampleSizes[sampleIndex];
                                            sampleIndex += TRACK_TYPE_VIDEO;
                                        } else {
                                            i2 += TRACK_TYPE_VIDEO;
                                        }
                                    }
                                    if (readValue >= -2147483648L) {
                                        break;
                                    }
                                    throw new ParserException("EBML lacing sample size out of range.");
                                }
                                this.blockLacingSampleSizes[this.blockLacingSampleCount + UNKNOWN] = ((contentSize - this.blockTrackNumberLength) - headerSize) - totalSamplesSize;
                            } else {
                                throw new IllegalStateException("Unexpected lacing value: " + lacing);
                            }
                        }
                        this.blockTimeUs = this.clusterTimecodeUs + scaleTimecodeToUs((long) ((this.scratch.data[LACING_NONE] << ENCRYPTION_IV_SIZE) | (this.scratch.data[TRACK_TYPE_VIDEO] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)));
                        boolean isInvisible = (this.scratch.data[TRACK_TYPE_AUDIO] & ENCRYPTION_IV_SIZE) == ENCRYPTION_IV_SIZE;
                        boolean isKeyframe = id == ID_SIMPLE_BLOCK && (this.scratch.data[TRACK_TYPE_AUDIO] & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) == 128;
                        int i3 = isKeyframe ? TRACK_TYPE_VIDEO : LACING_NONE;
                        if (isInvisible) {
                            i = C.SAMPLE_FLAG_DECODE_ONLY;
                        } else {
                            i = LACING_NONE;
                        }
                        this.blockFlags = i | i3;
                        this.blockEncryptionKeyId = sampleTrackFormat.encryptionKeyId;
                        this.blockState = TRACK_TYPE_AUDIO;
                        this.blockLacingSampleIndex = LACING_NONE;
                    }
                    if (id == ID_SIMPLE_BLOCK) {
                        while (this.blockLacingSampleIndex < this.blockLacingSampleCount) {
                            writeSampleData(input, trackOutput, sampleTrackFormat, this.blockLacingSampleSizes[this.blockLacingSampleIndex]);
                            outputSampleMetadata(trackOutput, this.blockTimeUs + ((long) ((this.blockLacingSampleIndex * sampleTrackFormat.defaultSampleDurationNs) / Constants.UPDATE_COFIG_INTERVAL)));
                            this.blockLacingSampleIndex += TRACK_TYPE_VIDEO;
                        }
                        this.blockState = LACING_NONE;
                        return;
                    }
                    writeSampleData(input, trackOutput, sampleTrackFormat, this.blockLacingSampleSizes[LACING_NONE]);
                    return;
                }
                input.skipFully(contentSize - this.blockTrackNumberLength);
                this.blockState = LACING_NONE;
                return;
            case ID_CONTENT_COMPRESSION_SETTINGS /*16981*/:
                this.trackFormat.sampleStrippedBytes = new byte[contentSize];
                input.readFully(this.trackFormat.sampleStrippedBytes, LACING_NONE, contentSize);
                return;
            case ID_CONTENT_ENCRYPTION_KEY_ID /*18402*/:
                this.trackFormat.encryptionKeyId = new byte[contentSize];
                input.readFully(this.trackFormat.encryptionKeyId, LACING_NONE, contentSize);
                return;
            case ID_SEEK_ID /*21419*/:
                Arrays.fill(this.seekEntryIdBytes.data, (byte) 0);
                input.readFully(this.seekEntryIdBytes.data, 4 - contentSize, contentSize);
                this.seekEntryIdBytes.setPosition(LACING_NONE);
                this.seekEntryId = (int) this.seekEntryIdBytes.readUnsignedInt();
                return;
            case ID_CODEC_PRIVATE /*25506*/:
                this.trackFormat.codecPrivate = new byte[contentSize];
                input.readFully(this.trackFormat.codecPrivate, LACING_NONE, contentSize);
                return;
            default:
                throw new ParserException("Unexpected id: " + id);
        }
    }

    private void outputSampleMetadata(TrackOutput trackOutput, long timeUs) {
        trackOutput.sampleMetadata(timeUs, this.blockFlags, this.sampleBytesWritten, LACING_NONE, this.blockEncryptionKeyId);
        this.sampleRead = true;
        resetSample();
    }

    private void resetSample() {
        this.sampleBytesRead = LACING_NONE;
        this.sampleBytesWritten = LACING_NONE;
        this.sampleCurrentNalBytesRemaining = LACING_NONE;
        this.sampleEncodingHandled = false;
        this.sampleStrippedBytes.reset();
    }

    private void readScratch(ExtractorInput input, int requiredLength) throws IOException, InterruptedException {
        if (this.scratch.limit() < requiredLength) {
            if (this.scratch.capacity() < requiredLength) {
                this.scratch.reset(Arrays.copyOf(this.scratch.data, Math.max(this.scratch.data.length * TRACK_TYPE_AUDIO, requiredLength)), this.scratch.limit());
            }
            input.readFully(this.scratch.data, this.scratch.limit(), requiredLength - this.scratch.limit());
            this.scratch.setLimit(requiredLength);
        }
    }

    private void writeSampleData(ExtractorInput input, TrackOutput output, TrackFormat format, int size) throws IOException, InterruptedException {
        if (!this.sampleEncodingHandled) {
            if (format.hasContentEncryption) {
                this.blockFlags &= -3;
                input.readFully(this.scratch.data, LACING_NONE, TRACK_TYPE_VIDEO);
                this.sampleBytesRead += TRACK_TYPE_VIDEO;
                if ((this.scratch.data[LACING_NONE] & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                    throw new ParserException("Extension bit is set in signal byte");
                } else if ((this.scratch.data[LACING_NONE] & TRACK_TYPE_VIDEO) == TRACK_TYPE_VIDEO) {
                    this.scratch.data[LACING_NONE] = (byte) 8;
                    this.scratch.setPosition(LACING_NONE);
                    output.sampleData(this.scratch, (int) TRACK_TYPE_VIDEO);
                    this.sampleBytesWritten += TRACK_TYPE_VIDEO;
                    this.blockFlags |= TRACK_TYPE_AUDIO;
                }
            } else if (format.sampleStrippedBytes != null) {
                this.sampleStrippedBytes.reset(format.sampleStrippedBytes, format.sampleStrippedBytes.length);
            }
            this.sampleEncodingHandled = true;
        }
        size += this.sampleStrippedBytes.limit();
        if (CODEC_ID_H264.equals(format.codecId)) {
            byte[] nalLengthData = this.nalLength.data;
            nalLengthData[LACING_NONE] = (byte) 0;
            nalLengthData[TRACK_TYPE_VIDEO] = (byte) 0;
            nalLengthData[TRACK_TYPE_AUDIO] = (byte) 0;
            int nalUnitLengthFieldLength = format.nalUnitLengthFieldLength;
            int nalUnitLengthFieldLengthDiff = 4 - format.nalUnitLengthFieldLength;
            while (this.sampleBytesRead < size) {
                if (this.sampleCurrentNalBytesRemaining == 0) {
                    readToTarget(input, nalLengthData, nalUnitLengthFieldLengthDiff, nalUnitLengthFieldLength);
                    this.nalLength.setPosition(LACING_NONE);
                    this.sampleCurrentNalBytesRemaining = this.nalLength.readUnsignedIntToInt();
                    this.nalStartCode.setPosition(LACING_NONE);
                    output.sampleData(this.nalStartCode, 4);
                    this.sampleBytesWritten += 4;
                } else {
                    this.sampleCurrentNalBytesRemaining -= readToOutput(input, output, this.sampleCurrentNalBytesRemaining);
                }
            }
        } else {
            while (this.sampleBytesRead < size) {
                readToOutput(input, output, size - this.sampleBytesRead);
            }
        }
        if (CODEC_ID_VORBIS.equals(format.codecId)) {
            this.vorbisNumPageSamples.setPosition(LACING_NONE);
            output.sampleData(this.vorbisNumPageSamples, 4);
            this.sampleBytesWritten += 4;
        }
    }

    private void readToTarget(ExtractorInput input, byte[] target, int offset, int length) throws IOException, InterruptedException {
        int pendingStrippedBytes = Math.min(length, this.sampleStrippedBytes.bytesLeft());
        input.readFully(target, offset + pendingStrippedBytes, length - pendingStrippedBytes);
        if (pendingStrippedBytes > 0) {
            this.sampleStrippedBytes.readBytes(target, offset, pendingStrippedBytes);
        }
        this.sampleBytesRead += length;
    }

    private int readToOutput(ExtractorInput input, TrackOutput output, int length) throws IOException, InterruptedException {
        int bytesRead;
        int strippedBytesLeft = this.sampleStrippedBytes.bytesLeft();
        if (strippedBytesLeft > 0) {
            bytesRead = Math.min(length, strippedBytesLeft);
            output.sampleData(this.sampleStrippedBytes, bytesRead);
        } else {
            bytesRead = output.sampleData(input, length);
        }
        this.sampleBytesRead += bytesRead;
        this.sampleBytesWritten += bytesRead;
        return bytesRead;
    }

    private ChunkIndex buildCues() throws ParserException {
        if (this.segmentContentPosition == -1) {
            throw new ParserException("Segment start/end offsets unknown");
        } else if (this.durationUs == -1) {
            throw new ParserException("Duration unknown");
        } else if (this.cueTimesUs == null || this.cueClusterPositions == null || this.cueTimesUs.size() == 0 || this.cueTimesUs.size() != this.cueClusterPositions.size()) {
            throw new ParserException("Invalid/missing cue points");
        } else {
            int i;
            int cuePointsSize = this.cueTimesUs.size();
            int[] sizes = new int[cuePointsSize];
            long[] offsets = new long[cuePointsSize];
            long[] durationsUs = new long[cuePointsSize];
            long[] timesUs = new long[cuePointsSize];
            for (i = LACING_NONE; i < cuePointsSize; i += TRACK_TYPE_VIDEO) {
                timesUs[i] = this.cueTimesUs.get(i);
                offsets[i] = this.segmentContentPosition + this.cueClusterPositions.get(i);
            }
            for (i = LACING_NONE; i < cuePointsSize + UNKNOWN; i += TRACK_TYPE_VIDEO) {
                sizes[i] = (int) (offsets[i + TRACK_TYPE_VIDEO] - offsets[i]);
                durationsUs[i] = timesUs[i + TRACK_TYPE_VIDEO] - timesUs[i];
            }
            sizes[cuePointsSize + UNKNOWN] = (int) ((this.segmentContentPosition + this.segmentContentSize) - offsets[cuePointsSize + UNKNOWN]);
            durationsUs[cuePointsSize + UNKNOWN] = this.durationUs - timesUs[cuePointsSize + UNKNOWN];
            this.cueTimesUs = null;
            this.cueClusterPositions = null;
            return new ChunkIndex(sizes, offsets, durationsUs, timesUs);
        }
    }

    private boolean maybeSeekForCues(PositionHolder seekPosition, long currentPosition) {
        if (this.seekForCues) {
            this.seekPositionAfterBuildingCues = currentPosition;
            seekPosition.position = this.cuesContentPosition;
            this.cuesState = TRACK_TYPE_VIDEO;
            this.seekForCues = false;
            return true;
        } else if (this.cuesState != TRACK_TYPE_AUDIO || this.seekPositionAfterBuildingCues == -1) {
            return false;
        } else {
            seekPosition.position = this.seekPositionAfterBuildingCues;
            this.seekPositionAfterBuildingCues = -1;
            return true;
        }
    }

    private long scaleTimecodeToUs(long unscaledTimecode) {
        return TimeUnit.NANOSECONDS.toMicros(this.timecodeScale * unscaledTimecode);
    }

    private static boolean isCodecSupported(String codecId) {
        return CODEC_ID_VP8.equals(codecId) || CODEC_ID_VP9.equals(codecId) || CODEC_ID_H264.equals(codecId) || CODEC_ID_OPUS.equals(codecId) || CODEC_ID_VORBIS.equals(codecId) || CODEC_ID_AAC.equals(codecId) || CODEC_ID_MP3.equals(codecId) || CODEC_ID_AC3.equals(codecId);
    }

    private static int[] ensureArrayCapacity(int[] array, int length) {
        if (array == null) {
            return new int[length];
        }
        return array.length < length ? new int[Math.max(array.length * TRACK_TYPE_AUDIO, length)] : array;
    }
}
