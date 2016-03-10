package com.google.android.exoplayer.drm;

import android.annotation.TargetApi;
import android.media.MediaDrm.KeyRequest;
import android.media.MediaDrm.ProvisionRequest;
import java.util.UUID;

@TargetApi(18)
public interface MediaDrmCallback {
    byte[] executeKeyRequest(UUID uuid, KeyRequest keyRequest) throws Exception;

    byte[] executeProvisionRequest(UUID uuid, ProvisionRequest provisionRequest) throws Exception;
}
