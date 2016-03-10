package com.google.android.exoplayer.extractor.ts;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.text.eia608.Eia608Parser;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.ParsableByteArray;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

class SeiReader extends ElementaryStreamReader {
    public SeiReader(TrackOutput output) {
        super(output);
        output.format(MediaFormat.createTextFormat(MimeTypes.APPLICATION_EIA608));
    }

    public void seek() {
    }

    public void consume(ParsableByteArray seiBuffer, long pesTimeUs, boolean startOfPacket) {
        while (seiBuffer.bytesLeft() > 1) {
            int b;
            int payloadType = 0;
            do {
                b = seiBuffer.readUnsignedByte();
                payloadType += b;
            } while (b == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            int payloadSize = 0;
            do {
                b = seiBuffer.readUnsignedByte();
                payloadSize += b;
            } while (b == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            if (Eia608Parser.isSeiMessageEia608(payloadType, payloadSize, seiBuffer)) {
                this.output.sampleData(seiBuffer, payloadSize);
                this.output.sampleMetadata(pesTimeUs, 1, payloadSize, 0, null);
            } else {
                seiBuffer.skipBytes(payloadSize);
            }
        }
    }

    public void packetFinished() {
    }
}
