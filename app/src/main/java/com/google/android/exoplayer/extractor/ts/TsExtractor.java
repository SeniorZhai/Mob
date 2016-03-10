package com.google.android.exoplayer.extractor.ts;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.util.ParsableBitArray;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.io.IOException;

public final class TsExtractor implements Extractor, SeekMap {
    private static final long MAX_PTS = 8589934591L;
    private static final String TAG = "TsExtractor";
    private static final int TS_PACKET_SIZE = 188;
    private static final int TS_PAT_PID = 0;
    private static final int TS_STREAM_TYPE_AAC = 15;
    private static final int TS_STREAM_TYPE_ATSC_AC3 = 129;
    private static final int TS_STREAM_TYPE_ATSC_E_AC3 = 135;
    private static final int TS_STREAM_TYPE_EIA608 = 256;
    private static final int TS_STREAM_TYPE_H264 = 27;
    private static final int TS_STREAM_TYPE_H265 = 36;
    private static final int TS_STREAM_TYPE_ID3 = 21;
    private static final int TS_STREAM_TYPE_MPA = 3;
    private static final int TS_STREAM_TYPE_MPA_LSF = 4;
    private static final int TS_SYNC_BYTE = 71;
    final SparseBooleanArray allowedPassthroughStreamTypes;
    private final long firstSampleTimestampUs;
    Id3Reader id3Reader;
    private final boolean idrKeyframesOnly;
    private long lastPts;
    private ExtractorOutput output;
    final SparseBooleanArray streamTypes;
    private long timestampOffsetUs;
    private final ParsableByteArray tsPacketBuffer;
    final SparseArray<TsPayloadReader> tsPayloadReaders;
    private final ParsableBitArray tsScratch;

    private static abstract class TsPayloadReader {
        public abstract void consume(ParsableByteArray parsableByteArray, boolean z, ExtractorOutput extractorOutput);

        public abstract void seek();

        private TsPayloadReader() {
        }
    }

    private class PatReader extends TsPayloadReader {
        private final ParsableBitArray patScratch = new ParsableBitArray(new byte[TsExtractor.TS_STREAM_TYPE_MPA_LSF]);

        public PatReader() {
            super();
        }

        public void seek() {
        }

        public void consume(ParsableByteArray data, boolean payloadUnitStartIndicator, ExtractorOutput output) {
            if (payloadUnitStartIndicator) {
                data.skipBytes(data.readUnsignedByte());
            }
            data.readBytes(this.patScratch, (int) TsExtractor.TS_STREAM_TYPE_MPA);
            this.patScratch.skipBits(12);
            int sectionLength = this.patScratch.readBits(12);
            data.skipBytes(5);
            int programCount = (sectionLength - 9) / TsExtractor.TS_STREAM_TYPE_MPA_LSF;
            for (int i = TsExtractor.TS_PAT_PID; i < programCount; i++) {
                data.readBytes(this.patScratch, (int) TsExtractor.TS_STREAM_TYPE_MPA_LSF);
                this.patScratch.skipBits(19);
                TsExtractor.this.tsPayloadReaders.put(this.patScratch.readBits(13), new PmtReader());
            }
        }
    }

    private class PesReader extends TsPayloadReader {
        private static final int HEADER_SIZE = 9;
        private static final int MAX_HEADER_EXTENSION_SIZE = 5;
        private static final int STATE_FINDING_HEADER = 0;
        private static final int STATE_READING_BODY = 3;
        private static final int STATE_READING_HEADER = 1;
        private static final int STATE_READING_HEADER_EXTENSION = 2;
        private boolean bodyStarted;
        private int bytesRead;
        private int extendedHeaderLength;
        private int payloadSize;
        private final ElementaryStreamReader pesPayloadReader;
        private final ParsableBitArray pesScratch = new ParsableBitArray(new byte[HEADER_SIZE]);
        private boolean ptsFlag;
        private int state = STATE_FINDING_HEADER;
        private long timeUs;

        public PesReader(ElementaryStreamReader pesPayloadReader) {
            super();
            this.pesPayloadReader = pesPayloadReader;
        }

        public void seek() {
            this.state = STATE_FINDING_HEADER;
            this.bytesRead = STATE_FINDING_HEADER;
            this.bodyStarted = false;
            this.pesPayloadReader.seek();
        }

        public void consume(ParsableByteArray data, boolean payloadUnitStartIndicator, ExtractorOutput output) {
            if (payloadUnitStartIndicator) {
                switch (this.state) {
                    case STATE_READING_HEADER_EXTENSION /*2*/:
                        Log.w(TsExtractor.TAG, "Unexpected start indicator reading extended header");
                        break;
                    case STATE_READING_BODY /*3*/:
                        if (this.payloadSize != -1) {
                            Log.w(TsExtractor.TAG, "Unexpected start indicator: expected " + this.payloadSize + " more bytes");
                        }
                        if (this.bodyStarted) {
                            this.pesPayloadReader.packetFinished();
                            break;
                        }
                        break;
                }
                setState(STATE_READING_HEADER);
            }
            while (data.bytesLeft() > 0) {
                switch (this.state) {
                    case STATE_FINDING_HEADER /*0*/:
                        data.skipBytes(data.bytesLeft());
                        break;
                    case STATE_READING_HEADER /*1*/:
                        if (!continueRead(data, this.pesScratch.data, HEADER_SIZE)) {
                            break;
                        }
                        setState(parseHeader() ? STATE_READING_HEADER_EXTENSION : STATE_FINDING_HEADER);
                        break;
                    case STATE_READING_HEADER_EXTENSION /*2*/:
                        if (continueRead(data, this.pesScratch.data, Math.min(MAX_HEADER_EXTENSION_SIZE, this.extendedHeaderLength)) && continueRead(data, null, this.extendedHeaderLength)) {
                            parseHeaderExtension();
                            this.bodyStarted = false;
                            setState(STATE_READING_BODY);
                            break;
                        }
                    case STATE_READING_BODY /*3*/:
                        boolean z;
                        int readLength = data.bytesLeft();
                        int padding = this.payloadSize == -1 ? STATE_FINDING_HEADER : readLength - this.payloadSize;
                        if (padding > 0) {
                            readLength -= padding;
                            data.setLimit(data.getPosition() + readLength);
                        }
                        ElementaryStreamReader elementaryStreamReader = this.pesPayloadReader;
                        long j = this.timeUs;
                        if (this.bodyStarted) {
                            z = false;
                        } else {
                            z = true;
                        }
                        elementaryStreamReader.consume(data, j, z);
                        this.bodyStarted = true;
                        if (this.payloadSize == -1) {
                            break;
                        }
                        this.payloadSize -= readLength;
                        if (this.payloadSize != 0) {
                            break;
                        }
                        this.pesPayloadReader.packetFinished();
                        setState(STATE_READING_HEADER);
                        break;
                    default:
                        break;
                }
            }
        }

        private void setState(int state) {
            this.state = state;
            this.bytesRead = STATE_FINDING_HEADER;
        }

        private boolean continueRead(ParsableByteArray source, byte[] target, int targetLength) {
            int bytesToRead = Math.min(source.bytesLeft(), targetLength - this.bytesRead);
            if (bytesToRead <= 0) {
                return true;
            }
            if (target == null) {
                source.skipBytes(bytesToRead);
            } else {
                source.readBytes(target, this.bytesRead, bytesToRead);
            }
            this.bytesRead += bytesToRead;
            if (this.bytesRead != targetLength) {
                return false;
            }
            return true;
        }

        private boolean parseHeader() {
            this.pesScratch.setPosition(STATE_FINDING_HEADER);
            int startCodePrefix = this.pesScratch.readBits(24);
            if (startCodePrefix != STATE_READING_HEADER) {
                Log.w(TsExtractor.TAG, "Unexpected start code prefix: " + startCodePrefix);
                this.payloadSize = -1;
                return false;
            }
            this.pesScratch.skipBits(8);
            int packetLength = this.pesScratch.readBits(16);
            this.pesScratch.skipBits(8);
            this.ptsFlag = this.pesScratch.readBit();
            this.pesScratch.skipBits(7);
            this.extendedHeaderLength = this.pesScratch.readBits(8);
            if (packetLength == 0) {
                this.payloadSize = -1;
            } else {
                this.payloadSize = ((packetLength + 6) - 9) - this.extendedHeaderLength;
            }
            return true;
        }

        private void parseHeaderExtension() {
            this.pesScratch.setPosition(STATE_FINDING_HEADER);
            this.timeUs = 0;
            if (this.ptsFlag) {
                this.pesScratch.skipBits(TsExtractor.TS_STREAM_TYPE_MPA_LSF);
                long pts = ((long) this.pesScratch.readBits(STATE_READING_BODY)) << 30;
                this.pesScratch.skipBits(STATE_READING_HEADER);
                pts |= (long) (this.pesScratch.readBits(TsExtractor.TS_STREAM_TYPE_AAC) << TsExtractor.TS_STREAM_TYPE_AAC);
                this.pesScratch.skipBits(STATE_READING_HEADER);
                pts |= (long) this.pesScratch.readBits(TsExtractor.TS_STREAM_TYPE_AAC);
                this.pesScratch.skipBits(STATE_READING_HEADER);
                this.timeUs = TsExtractor.this.ptsToTimeUs(pts);
            }
        }
    }

    private class PmtReader extends TsPayloadReader {
        private final ParsableBitArray pmtScratch = new ParsableBitArray(new byte[5]);

        public PmtReader() {
            super();
        }

        public void seek() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void consume(com.google.android.exoplayer.util.ParsableByteArray r12, boolean r13, com.google.android.exoplayer.extractor.ExtractorOutput r14) {
            /*
            r11 = this;
            if (r13 == 0) goto L_0x0009;
        L_0x0002:
            r4 = r12.readUnsignedByte();
            r12.skipBytes(r4);
        L_0x0009:
            r8 = r11.pmtScratch;
            r9 = 3;
            r12.readBytes(r8, r9);
            r8 = r11.pmtScratch;
            r9 = 12;
            r8.skipBits(r9);
            r8 = r11.pmtScratch;
            r9 = 12;
            r6 = r8.readBits(r9);
            r8 = 7;
            r12.skipBytes(r8);
            r8 = r11.pmtScratch;
            r9 = 2;
            r12.readBytes(r8, r9);
            r8 = r11.pmtScratch;
            r9 = 4;
            r8.skipBits(r9);
            r8 = r11.pmtScratch;
            r9 = 12;
            r5 = r8.readBits(r9);
            r12.skipBytes(r5);
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r8 = r8.id3Reader;
            if (r8 != 0) goto L_0x004e;
        L_0x003f:
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r9 = new com.google.android.exoplayer.extractor.ts.Id3Reader;
            r10 = 21;
            r10 = r14.track(r10);
            r9.<init>(r10);
            r8.id3Reader = r9;
        L_0x004e:
            r8 = r6 + -9;
            r8 = r8 - r5;
            r1 = r8 + -4;
        L_0x0053:
            if (r1 <= 0) goto L_0x011d;
        L_0x0055:
            r8 = r11.pmtScratch;
            r9 = 5;
            r12.readBytes(r8, r9);
            r8 = r11.pmtScratch;
            r9 = 8;
            r7 = r8.readBits(r9);
            r8 = r11.pmtScratch;
            r9 = 3;
            r8.skipBits(r9);
            r8 = r11.pmtScratch;
            r9 = 13;
            r0 = r8.readBits(r9);
            r8 = r11.pmtScratch;
            r9 = 4;
            r8.skipBits(r9);
            r8 = r11.pmtScratch;
            r9 = 12;
            r2 = r8.readBits(r9);
            r12.skipBytes(r2);
            r8 = r2 + 5;
            r1 = r1 - r8;
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r8 = r8.streamTypes;
            r8 = r8.get(r7);
            if (r8 != 0) goto L_0x0053;
        L_0x008f:
            r3 = 0;
            switch(r7) {
                case 3: goto L_0x00ac;
                case 4: goto L_0x00b7;
                case 15: goto L_0x00c2;
                case 21: goto L_0x0117;
                case 27: goto L_0x00e2;
                case 36: goto L_0x00ff;
                case 129: goto L_0x00ce;
                case 135: goto L_0x00ce;
                default: goto L_0x0093;
            };
        L_0x0093:
            if (r3 == 0) goto L_0x0053;
        L_0x0095:
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r8 = r8.streamTypes;
            r9 = 1;
            r8.put(r7, r9);
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r8 = r8.tsPayloadReaders;
            r9 = new com.google.android.exoplayer.extractor.ts.TsExtractor$PesReader;
            r10 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r9.<init>(r3);
            r8.put(r0, r9);
            goto L_0x0053;
        L_0x00ac:
            r3 = new com.google.android.exoplayer.extractor.ts.MpegAudioReader;
            r8 = 3;
            r8 = r14.track(r8);
            r3.<init>(r8);
            goto L_0x0093;
        L_0x00b7:
            r3 = new com.google.android.exoplayer.extractor.ts.MpegAudioReader;
            r8 = 4;
            r8 = r14.track(r8);
            r3.<init>(r8);
            goto L_0x0093;
        L_0x00c2:
            r3 = new com.google.android.exoplayer.extractor.ts.AdtsReader;
            r8 = 15;
            r8 = r14.track(r8);
            r3.<init>(r8);
            goto L_0x0093;
        L_0x00ce:
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r8 = r8.allowedPassthroughStreamTypes;
            r8 = r8.get(r7);
            if (r8 == 0) goto L_0x0053;
        L_0x00d8:
            r3 = new com.google.android.exoplayer.extractor.ts.Ac3Reader;
            r8 = r14.track(r7);
            r3.<init>(r8);
            goto L_0x0093;
        L_0x00e2:
            r3 = new com.google.android.exoplayer.extractor.ts.H264Reader;
            r8 = 27;
            r8 = r14.track(r8);
            r9 = new com.google.android.exoplayer.extractor.ts.SeiReader;
            r10 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
            r10 = r14.track(r10);
            r9.<init>(r10);
            r10 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r10 = r10.idrKeyframesOnly;
            r3.<init>(r8, r9, r10);
            goto L_0x0093;
        L_0x00ff:
            r3 = new com.google.android.exoplayer.extractor.ts.H265Reader;
            r8 = 36;
            r8 = r14.track(r8);
            r9 = new com.google.android.exoplayer.extractor.ts.SeiReader;
            r10 = 256; // 0x100 float:3.59E-43 double:1.265E-321;
            r10 = r14.track(r10);
            r9.<init>(r10);
            r3.<init>(r8, r9);
            goto L_0x0093;
        L_0x0117:
            r8 = com.google.android.exoplayer.extractor.ts.TsExtractor.this;
            r3 = r8.id3Reader;
            goto L_0x0093;
        L_0x011d:
            r14.endTracks();
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer.extractor.ts.TsExtractor.PmtReader.consume(com.google.android.exoplayer.util.ParsableByteArray, boolean, com.google.android.exoplayer.extractor.ExtractorOutput):void");
        }
    }

    public TsExtractor() {
        this(0);
    }

    public TsExtractor(long firstSampleTimestampUs) {
        this(firstSampleTimestampUs, null);
    }

    public TsExtractor(long firstSampleTimestampUs, AudioCapabilities audioCapabilities) {
        this(firstSampleTimestampUs, audioCapabilities, true);
    }

    public TsExtractor(long firstSampleTimestampUs, AudioCapabilities audioCapabilities, boolean idrKeyframesOnly) {
        this.firstSampleTimestampUs = firstSampleTimestampUs;
        this.idrKeyframesOnly = idrKeyframesOnly;
        this.tsScratch = new ParsableBitArray(new byte[TS_STREAM_TYPE_MPA]);
        this.tsPacketBuffer = new ParsableByteArray((int) TS_PACKET_SIZE);
        this.streamTypes = new SparseBooleanArray();
        this.allowedPassthroughStreamTypes = getPassthroughStreamTypes(audioCapabilities);
        this.tsPayloadReaders = new SparseArray();
        this.tsPayloadReaders.put(TS_PAT_PID, new PatReader());
        this.lastPts = Long.MIN_VALUE;
    }

    public void init(ExtractorOutput output) {
        this.output = output;
        output.seekMap(this);
    }

    public void seek() {
        this.timestampOffsetUs = 0;
        this.lastPts = Long.MIN_VALUE;
        for (int i = TS_PAT_PID; i < this.tsPayloadReaders.size(); i++) {
            ((TsPayloadReader) this.tsPayloadReaders.valueAt(i)).seek();
        }
    }

    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        if (!input.readFully(this.tsPacketBuffer.data, TS_PAT_PID, TS_PACKET_SIZE, true)) {
            return -1;
        }
        this.tsPacketBuffer.setPosition(TS_PAT_PID);
        this.tsPacketBuffer.setLimit(TS_PACKET_SIZE);
        if (this.tsPacketBuffer.readUnsignedByte() != TS_SYNC_BYTE) {
            return TS_PAT_PID;
        }
        this.tsPacketBuffer.readBytes(this.tsScratch, (int) TS_STREAM_TYPE_MPA);
        this.tsScratch.skipBits(1);
        boolean payloadUnitStartIndicator = this.tsScratch.readBit();
        this.tsScratch.skipBits(1);
        int pid = this.tsScratch.readBits(13);
        this.tsScratch.skipBits(2);
        boolean adaptationFieldExists = this.tsScratch.readBit();
        boolean payloadExists = this.tsScratch.readBit();
        if (adaptationFieldExists) {
            this.tsPacketBuffer.skipBytes(this.tsPacketBuffer.readUnsignedByte());
        }
        if (!payloadExists) {
            return TS_PAT_PID;
        }
        TsPayloadReader payloadReader = (TsPayloadReader) this.tsPayloadReaders.get(pid);
        if (payloadReader == null) {
            return TS_PAT_PID;
        }
        payloadReader.consume(this.tsPacketBuffer, payloadUnitStartIndicator, this.output);
        return TS_PAT_PID;
    }

    public boolean isSeekable() {
        return false;
    }

    public long getPosition(long timeUs) {
        return 0;
    }

    long ptsToTimeUs(long pts) {
        if (this.lastPts != Long.MIN_VALUE) {
            long closestWrapCount = (this.lastPts + 4294967295L) / MAX_PTS;
            long ptsWrapBelow = pts + (MAX_PTS * (closestWrapCount - 1));
            long ptsWrapAbove = pts + (MAX_PTS * closestWrapCount);
            if (Math.abs(ptsWrapBelow - this.lastPts) < Math.abs(ptsWrapAbove - this.lastPts)) {
                pts = ptsWrapBelow;
            } else {
                pts = ptsWrapAbove;
            }
        }
        long timeUs = (C.MICROS_PER_SECOND * pts) / 90000;
        if (this.lastPts == Long.MIN_VALUE) {
            this.timestampOffsetUs = this.firstSampleTimestampUs - timeUs;
        }
        this.lastPts = pts;
        return this.timestampOffsetUs + timeUs;
    }

    private static SparseBooleanArray getPassthroughStreamTypes(AudioCapabilities audioCapabilities) {
        SparseBooleanArray streamTypes = new SparseBooleanArray();
        if (audioCapabilities != null) {
            if (audioCapabilities.supportsEncoding(5)) {
                streamTypes.put(TS_STREAM_TYPE_ATSC_AC3, true);
            }
            if (audioCapabilities.supportsEncoding(6)) {
                return streamTypes;
            }
        }
        return streamTypes;
    }
}
