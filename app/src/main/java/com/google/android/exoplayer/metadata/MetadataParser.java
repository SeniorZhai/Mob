package com.google.android.exoplayer.metadata;

import java.io.IOException;

public interface MetadataParser<T> {
    boolean canParse(String str);

    T parse(byte[] bArr, int i) throws IOException;
}
