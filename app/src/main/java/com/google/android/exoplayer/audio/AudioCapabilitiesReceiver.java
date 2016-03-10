package com.google.android.exoplayer.audio;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;

public final class AudioCapabilitiesReceiver {
    private static final AudioCapabilities DEFAULT_AUDIO_CAPABILITIES = new AudioCapabilities(new int[]{2}, 2);
    private final Context context;
    private final Listener listener;
    private final BroadcastReceiver receiver;

    @TargetApi(21)
    private final class HdmiAudioPlugBroadcastReceiver extends BroadcastReceiver {
        private HdmiAudioPlugBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!isInitialStickyBroadcast() && intent.getAction().equals("android.media.action.HDMI_AUDIO_PLUG")) {
                AudioCapabilitiesReceiver.this.listener.onAudioCapabilitiesChanged(new AudioCapabilities(intent.getIntArrayExtra("android.media.extra.ENCODINGS"), intent.getIntExtra("android.media.extra.MAX_CHANNEL_COUNT", 0)));
            }
        }
    }

    public interface Listener {
        void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities);
    }

    public AudioCapabilitiesReceiver(Context context, Listener listener) {
        BroadcastReceiver hdmiAudioPlugBroadcastReceiver;
        this.context = (Context) Assertions.checkNotNull(context);
        this.listener = (Listener) Assertions.checkNotNull(listener);
        if (Util.SDK_INT >= 21) {
            hdmiAudioPlugBroadcastReceiver = new HdmiAudioPlugBroadcastReceiver();
        } else {
            hdmiAudioPlugBroadcastReceiver = null;
        }
        this.receiver = hdmiAudioPlugBroadcastReceiver;
    }

    @TargetApi(21)
    public void register() {
        if (this.receiver != null) {
            Intent initialStickyIntent = this.context.registerReceiver(this.receiver, new IntentFilter("android.media.action.HDMI_AUDIO_PLUG"));
            if (initialStickyIntent != null) {
                this.receiver.onReceive(this.context, initialStickyIntent);
                return;
            }
        }
        this.listener.onAudioCapabilitiesChanged(DEFAULT_AUDIO_CAPABILITIES);
    }

    public void unregister() {
        if (this.receiver != null) {
            this.context.unregisterReceiver(this.receiver);
        }
    }
}
