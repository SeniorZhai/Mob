package com.facebook.share.internal;

import com.facebook.internal.DialogFeature;
import com.facebook.internal.NativeProtocol;

public enum LikeDialogFeature implements DialogFeature {
    LIKE_DIALOG(NativeProtocol.PROTOCOL_VERSION_20140701);
    
    private int minVersion;

    private LikeDialogFeature(int minVersion) {
        this.minVersion = minVersion;
    }

    public String getAction() {
        return NativeProtocol.ACTION_LIKE_DIALOG;
    }

    public int getMinVersion() {
        return this.minVersion;
    }
}
