package com.google.android.exoplayer.drm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DrmInitData {
    public final String mimeType;

    public static final class Mapped extends DrmInitData {
        private final Map<UUID, byte[]> schemeData = new HashMap();

        public Mapped(String mimeType) {
            super(mimeType);
        }

        public byte[] get(UUID schemeUuid) {
            return (byte[]) this.schemeData.get(schemeUuid);
        }

        public void put(UUID schemeUuid, byte[] data) {
            this.schemeData.put(schemeUuid, data);
        }
    }

    public static final class Universal extends DrmInitData {
        private byte[] data;

        public Universal(String mimeType, byte[] data) {
            super(mimeType);
            this.data = data;
        }

        public byte[] get(UUID schemeUuid) {
            return this.data;
        }
    }

    public abstract byte[] get(UUID uuid);

    public DrmInitData(String mimeType) {
        this.mimeType = mimeType;
    }
}
