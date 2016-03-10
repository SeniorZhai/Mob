package com.firebase.tubesock;

import io.fabric.sdk.android.services.network.UrlUtils;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

class MessageBuilderFactory {

    interface Builder {
        boolean appendBytes(byte[] bArr);

        WebSocketMessage toMessage();
    }

    static class BinaryBuilder implements Builder {
        private int pendingByteCount = 0;
        private List<byte[]> pendingBytes = new ArrayList();

        BinaryBuilder() {
        }

        public boolean appendBytes(byte[] bytes) {
            this.pendingBytes.add(bytes);
            this.pendingByteCount += bytes.length;
            return true;
        }

        public WebSocketMessage toMessage() {
            byte[] payload = new byte[this.pendingByteCount];
            int offset = 0;
            for (int i = 0; i < this.pendingBytes.size(); i++) {
                byte[] segment = (byte[]) this.pendingBytes.get(i);
                System.arraycopy(segment, 0, payload, offset, segment.length);
                offset += segment.length;
            }
            return new WebSocketMessage(payload);
        }
    }

    static class TextBuilder implements Builder {
        private static ThreadLocal<CharsetDecoder> localDecoder = new ThreadLocal<CharsetDecoder>() {
            protected CharsetDecoder initialValue() {
                CharsetDecoder decoder = Charset.forName(UrlUtils.UTF8).newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                return decoder;
            }
        };
        private static ThreadLocal<CharsetEncoder> localEncoder = new ThreadLocal<CharsetEncoder>() {
            protected CharsetEncoder initialValue() {
                CharsetEncoder encoder = Charset.forName(UrlUtils.UTF8).newEncoder();
                encoder.onMalformedInput(CodingErrorAction.REPORT);
                encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                return encoder;
            }
        };
        private StringBuilder builder = new StringBuilder();
        private ByteBuffer carryOver;

        TextBuilder() {
        }

        public boolean appendBytes(byte[] bytes) {
            String nextFrame = decodeString(bytes);
            if (nextFrame == null) {
                return false;
            }
            this.builder.append(nextFrame);
            return true;
        }

        public WebSocketMessage toMessage() {
            if (this.carryOver != null) {
                return null;
            }
            return new WebSocketMessage(this.builder.toString());
        }

        private String decodeString(byte[] bytes) {
            try {
                return ((CharsetDecoder) localDecoder.get()).decode(ByteBuffer.wrap(bytes)).toString();
            } catch (CharacterCodingException e) {
                return null;
            }
        }

        private String decodeStringStreaming(byte[] bytes) {
            try {
                ByteBuffer input = getBuffer(bytes);
                int bufSize = (int) (((CharsetDecoder) localDecoder.get()).averageCharsPerByte() * ((float) input.remaining()));
                CharBuffer output = CharBuffer.allocate(bufSize);
                while (true) {
                    CoderResult result = ((CharsetDecoder) localDecoder.get()).decode(input, output, false);
                    if (result.isError()) {
                        return null;
                    }
                    if (result.isUnderflow()) {
                        break;
                    } else if (result.isOverflow()) {
                        bufSize = (bufSize * 2) + 1;
                        CharBuffer o = CharBuffer.allocate(bufSize);
                        output.flip();
                        o.put(output);
                        output = o;
                    }
                }
                if (input.remaining() > 0) {
                    this.carryOver = input;
                }
                ((CharsetEncoder) localEncoder.get()).encode(CharBuffer.wrap(output));
                output.flip();
                return output.toString();
            } catch (CharacterCodingException e) {
                return null;
            }
        }

        private ByteBuffer getBuffer(byte[] bytes) {
            if (this.carryOver == null) {
                return ByteBuffer.wrap(bytes);
            }
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + this.carryOver.remaining());
            buffer.put(this.carryOver);
            this.carryOver = null;
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        }
    }

    MessageBuilderFactory() {
    }

    static Builder builder(byte opcode) {
        if (opcode == (byte) 2) {
            return new BinaryBuilder();
        }
        return new TextBuilder();
    }
}
