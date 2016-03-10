package com.mixpanel.android.viewcrawler;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import com.mixpanel.android.mpmetrics.MPConfig;

class FlipGesture implements SensorEventListener {
    private static final float ACCELEROMETER_SMOOTHING = 0.7f;
    private static final int FLIP_STATE_DOWN = 1;
    private static final int FLIP_STATE_NONE = 0;
    private static final int FLIP_STATE_UP = -1;
    private static final String LOGTAG = "MixpanelAPI.FlipGesture";
    private static final float MAXIMUM_GRAVITY_FOR_FLIP = 11.8f;
    private static final long MINIMUM_CANCEL_DURATION = 1000000000;
    private static final float MINIMUM_GRAVITY_FOR_FLIP = 7.8f;
    private static final long MINIMUM_UP_DOWN_DURATION = 250000000;
    private static final int TRIGGER_STATE_BEGIN = 1;
    private static final int TRIGGER_STATE_NONE = 0;
    private int mFlipState = FLIP_STATE_NONE;
    private long mLastFlipTime = -1;
    private final OnFlipGestureListener mListener;
    private final float[] mSmoothed = new float[3];
    private int mTriggerState = FLIP_STATE_UP;

    public interface OnFlipGestureListener {
        void onFlipGesture();
    }

    public FlipGesture(OnFlipGestureListener listener) {
        this.mListener = listener;
    }

    public void onSensorChanged(SensorEvent event) {
        float[] smoothed = smoothXYZ(event.values);
        int oldFlipState = this.mFlipState;
        float totalGravitySquared = ((smoothed[FLIP_STATE_NONE] * smoothed[FLIP_STATE_NONE]) + (smoothed[TRIGGER_STATE_BEGIN] * smoothed[TRIGGER_STATE_BEGIN])) + (smoothed[2] * smoothed[2]);
        this.mFlipState = FLIP_STATE_NONE;
        if (smoothed[2] > MINIMUM_GRAVITY_FOR_FLIP && smoothed[2] < MAXIMUM_GRAVITY_FOR_FLIP) {
            this.mFlipState = FLIP_STATE_UP;
        }
        if (smoothed[2] < -7.8f && smoothed[2] > -11.8f) {
            this.mFlipState = TRIGGER_STATE_BEGIN;
        }
        if (totalGravitySquared < 60.840004f || totalGravitySquared > 139.24f) {
            this.mFlipState = FLIP_STATE_NONE;
        }
        if (oldFlipState != this.mFlipState) {
            this.mLastFlipTime = event.timestamp;
        }
        long flipDurationNanos = event.timestamp - this.mLastFlipTime;
        switch (this.mFlipState) {
            case FLIP_STATE_UP /*-1*/:
                if (flipDurationNanos > MINIMUM_UP_DOWN_DURATION && this.mTriggerState == TRIGGER_STATE_BEGIN) {
                    if (MPConfig.DEBUG) {
                        Log.v(LOGTAG, "Flip gesture completed");
                    }
                    this.mTriggerState = FLIP_STATE_NONE;
                    this.mListener.onFlipGesture();
                    return;
                }
                return;
            case FLIP_STATE_NONE /*0*/:
                if (flipDurationNanos > MINIMUM_CANCEL_DURATION && this.mTriggerState != 0) {
                    if (MPConfig.DEBUG) {
                        Log.v(LOGTAG, "Flip gesture abandoned");
                    }
                    this.mTriggerState = FLIP_STATE_NONE;
                    return;
                }
                return;
            case TRIGGER_STATE_BEGIN /*1*/:
                if (flipDurationNanos > MINIMUM_UP_DOWN_DURATION && this.mTriggerState == 0) {
                    if (MPConfig.DEBUG) {
                        Log.v(LOGTAG, "Flip gesture begun");
                    }
                    this.mTriggerState = TRIGGER_STATE_BEGIN;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private float[] smoothXYZ(float[] samples) {
        for (int i = FLIP_STATE_NONE; i < 3; i += TRIGGER_STATE_BEGIN) {
            float oldVal = this.mSmoothed[i];
            this.mSmoothed[i] = (ACCELEROMETER_SMOOTHING * (samples[i] - oldVal)) + oldVal;
        }
        return this.mSmoothed;
    }
}
