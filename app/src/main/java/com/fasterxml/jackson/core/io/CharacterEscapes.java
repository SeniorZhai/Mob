package com.fasterxml.jackson.core.io;

import com.fasterxml.jackson.core.SerializableString;
import java.io.Serializable;

public abstract class CharacterEscapes implements Serializable {
    public static final int ESCAPE_CUSTOM = -2;
    public static final int ESCAPE_NONE = 0;
    public static final int ESCAPE_STANDARD = -1;
    private static final long serialVersionUID = 1;

    public abstract int[] getEscapeCodesForAscii();

    public abstract SerializableString getEscapeSequence(int i);

    public static int[] standardAsciiEscapesForJSON() {
        Object obj = CharTypes.get7BitOutputEscapes();
        Object obj2 = new int[obj.length];
        System.arraycopy(obj, ESCAPE_NONE, obj2, ESCAPE_NONE, obj.length);
        return obj2;
    }
}
