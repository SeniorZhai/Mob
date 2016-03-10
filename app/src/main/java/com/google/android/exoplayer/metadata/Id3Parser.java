package com.google.android.exoplayer.metadata;

import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.http.protocol.HTTP;

public class Id3Parser implements MetadataParser<Map<String, Object>> {
    private static final int ID3_TEXT_ENCODING_ISO_8859_1 = 0;
    private static final int ID3_TEXT_ENCODING_UTF_16 = 1;
    private static final int ID3_TEXT_ENCODING_UTF_16BE = 2;
    private static final int ID3_TEXT_ENCODING_UTF_8 = 3;

    public boolean canParse(String mimeType) {
        return mimeType.equals(MimeTypes.APPLICATION_ID3);
    }

    public Map<String, Object> parse(byte[] data, int size) throws UnsupportedEncodingException, ParserException {
        Map<String, Object> metadata = new HashMap();
        ParsableByteArray parsableByteArray = new ParsableByteArray(data, size);
        int id3Size = parseId3Header(parsableByteArray);
        while (id3Size > 0) {
            int frameId0 = parsableByteArray.readUnsignedByte();
            int frameId1 = parsableByteArray.readUnsignedByte();
            int frameId2 = parsableByteArray.readUnsignedByte();
            int frameId3 = parsableByteArray.readUnsignedByte();
            int frameSize = parsableByteArray.readSynchSafeInt();
            if (frameSize <= ID3_TEXT_ENCODING_UTF_16) {
                break;
            }
            parsableByteArray.skipBytes(ID3_TEXT_ENCODING_UTF_16BE);
            int encoding;
            String charset;
            byte[] frame;
            int firstZeroIndex;
            String description;
            String str;
            if (frameId0 == 84 && frameId1 == 88 && frameId2 == 88 && frameId3 == 88) {
                encoding = parsableByteArray.readUnsignedByte();
                charset = getCharsetName(encoding);
                frame = new byte[(frameSize - 1)];
                parsableByteArray.readBytes(frame, ID3_TEXT_ENCODING_ISO_8859_1, frameSize - 1);
                firstZeroIndex = indexOfEOS(frame, ID3_TEXT_ENCODING_ISO_8859_1, encoding);
                description = new String(frame, ID3_TEXT_ENCODING_ISO_8859_1, firstZeroIndex, charset);
                int valueStartIndex = firstZeroIndex + delimiterLength(encoding);
                str = new String(frame, valueStartIndex, indexOfEOS(frame, valueStartIndex, encoding) - valueStartIndex, charset);
                metadata.put(TxxxMetadata.TYPE, new TxxxMetadata(description, str));
            } else if (frameId0 == 80 && frameId1 == 82 && frameId2 == 73 && frameId3 == 86) {
                frame = new byte[frameSize];
                parsableByteArray.readBytes(frame, ID3_TEXT_ENCODING_ISO_8859_1, frameSize);
                firstZeroIndex = indexOf(frame, ID3_TEXT_ENCODING_ISO_8859_1, (byte) 0);
                str = new String(frame, ID3_TEXT_ENCODING_ISO_8859_1, firstZeroIndex, HTTP.ISO_8859_1);
                Object privateData = new byte[((frameSize - firstZeroIndex) - 1)];
                System.arraycopy(frame, firstZeroIndex + ID3_TEXT_ENCODING_UTF_16, privateData, ID3_TEXT_ENCODING_ISO_8859_1, (frameSize - firstZeroIndex) - 1);
                metadata.put(PrivMetadata.TYPE, new PrivMetadata(str, privateData));
            } else if (frameId0 == 71 && frameId1 == 69 && frameId2 == 79 && frameId3 == 66) {
                encoding = parsableByteArray.readUnsignedByte();
                charset = getCharsetName(encoding);
                frame = new byte[(frameSize - 1)];
                parsableByteArray.readBytes(frame, ID3_TEXT_ENCODING_ISO_8859_1, frameSize - 1);
                firstZeroIndex = indexOf(frame, ID3_TEXT_ENCODING_ISO_8859_1, (byte) 0);
                str = new String(frame, ID3_TEXT_ENCODING_ISO_8859_1, firstZeroIndex, HTTP.ISO_8859_1);
                int filenameStartIndex = firstZeroIndex + ID3_TEXT_ENCODING_UTF_16;
                int filenameEndIndex = indexOfEOS(frame, filenameStartIndex, encoding);
                String filename = new String(frame, filenameStartIndex, filenameEndIndex - filenameStartIndex, charset);
                int descriptionStartIndex = filenameEndIndex + delimiterLength(encoding);
                int descriptionEndIndex = indexOfEOS(frame, descriptionStartIndex, encoding);
                description = new String(frame, descriptionStartIndex, descriptionEndIndex - descriptionStartIndex, charset);
                int objectDataSize = ((frameSize - 1) - descriptionEndIndex) - delimiterLength(encoding);
                Object objectData = new byte[objectDataSize];
                System.arraycopy(frame, delimiterLength(encoding) + descriptionEndIndex, objectData, ID3_TEXT_ENCODING_ISO_8859_1, objectDataSize);
                metadata.put(GeobMetadata.TYPE, new GeobMetadata(str, filename, description, objectData));
            } else {
                String type = String.format(Locale.US, "%c%c%c%c", new Object[]{Integer.valueOf(frameId0), Integer.valueOf(frameId1), Integer.valueOf(frameId2), Integer.valueOf(frameId3)});
                frame = new byte[frameSize];
                parsableByteArray.readBytes(frame, ID3_TEXT_ENCODING_ISO_8859_1, frameSize);
                metadata.put(type, frame);
            }
            id3Size -= frameSize + 10;
        }
        return Collections.unmodifiableMap(metadata);
    }

    private static int indexOf(byte[] data, int fromIndex, byte key) {
        for (int i = fromIndex; i < data.length; i += ID3_TEXT_ENCODING_UTF_16) {
            if (data[i] == key) {
                return i;
            }
        }
        return data.length;
    }

    private static int indexOfEOS(byte[] data, int fromIndex, int encodingByte) {
        int terminationPos = indexOf(data, fromIndex, (byte) 0);
        if (encodingByte == 0 || encodingByte == ID3_TEXT_ENCODING_UTF_8) {
            return terminationPos;
        }
        while (terminationPos < data.length - 1) {
            if (data[terminationPos + ID3_TEXT_ENCODING_UTF_16] == (byte) 0) {
                return terminationPos;
            }
            terminationPos = indexOf(data, terminationPos + ID3_TEXT_ENCODING_UTF_16, (byte) 0);
        }
        return data.length;
    }

    private static int delimiterLength(int encodingByte) {
        return (encodingByte == 0 || encodingByte == ID3_TEXT_ENCODING_UTF_8) ? ID3_TEXT_ENCODING_UTF_16 : ID3_TEXT_ENCODING_UTF_16BE;
    }

    private static int parseId3Header(ParsableByteArray id3Buffer) throws ParserException {
        int id1 = id3Buffer.readUnsignedByte();
        int id2 = id3Buffer.readUnsignedByte();
        int id3 = id3Buffer.readUnsignedByte();
        if (id1 == 73 && id2 == 68 && id3 == 51) {
            id3Buffer.skipBytes(ID3_TEXT_ENCODING_UTF_16BE);
            int flags = id3Buffer.readUnsignedByte();
            int id3Size = id3Buffer.readSynchSafeInt();
            if ((flags & ID3_TEXT_ENCODING_UTF_16BE) != 0) {
                int extendedHeaderSize = id3Buffer.readSynchSafeInt();
                if (extendedHeaderSize > 4) {
                    id3Buffer.skipBytes(extendedHeaderSize - 4);
                }
                id3Size -= extendedHeaderSize;
            }
            if ((flags & 8) != 0) {
                return id3Size - 10;
            }
            return id3Size;
        }
        Object[] objArr = new Object[ID3_TEXT_ENCODING_UTF_8];
        objArr[ID3_TEXT_ENCODING_ISO_8859_1] = Integer.valueOf(id1);
        objArr[ID3_TEXT_ENCODING_UTF_16] = Integer.valueOf(id2);
        objArr[ID3_TEXT_ENCODING_UTF_16BE] = Integer.valueOf(id3);
        throw new ParserException(String.format(Locale.US, "Unexpected ID3 file identifier, expected \"ID3\", actual \"%c%c%c\".", objArr));
    }

    private static String getCharsetName(int encodingByte) {
        switch (encodingByte) {
            case ID3_TEXT_ENCODING_ISO_8859_1 /*0*/:
                return HTTP.ISO_8859_1;
            case ID3_TEXT_ENCODING_UTF_16 /*1*/:
                return HTTP.UTF_16;
            case ID3_TEXT_ENCODING_UTF_16BE /*2*/:
                return "UTF-16BE";
            case ID3_TEXT_ENCODING_UTF_8 /*3*/:
                return HTTP.UTF_8;
            default:
                return HTTP.ISO_8859_1;
        }
    }
}
