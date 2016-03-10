package com.helpshift.external;

public interface Encoder {
    Object encode(Object obj) throws EncoderException;
}
