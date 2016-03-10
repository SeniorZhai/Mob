package com.google.android.exoplayer.dash.mpd;

import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;
import java.util.Arrays;
import java.util.UUID;

public class ContentProtection {
    public final byte[] data;
    public final String schemeUriId;
    public final UUID uuid;

    public ContentProtection(String schemeUriId, UUID uuid, byte[] data) {
        this.schemeUriId = (String) Assertions.checkNotNull(schemeUriId);
        this.uuid = uuid;
        this.data = data;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ContentProtection)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        ContentProtection other = (ContentProtection) obj;
        if (this.schemeUriId.equals(other.schemeUriId) && Util.areEqual(this.uuid, other.uuid) && Arrays.equals(this.data, other.data)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int hashCode = this.schemeUriId.hashCode() + 37;
        if (this.uuid != null) {
            hashCode = (hashCode * 37) + this.uuid.hashCode();
        }
        if (this.data != null) {
            return (hashCode * 37) + Arrays.hashCode(this.data);
        }
        return hashCode;
    }
}
