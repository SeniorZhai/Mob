package com.mobcrush.mobcrush.common;

import android.content.Context;
import android.view.OrientationEventListener;

public class OrientationManager extends OrientationEventListener {
    private static final String TAG = OrientationManager.class.getName();
    private static OrientationManager instance;
    private Context context;
    private OrientationChangeListener orientationChangeListener;
    private int previousAngle;
    private int previousOrientation;

    public interface OrientationChangeListener {
        void onOrientationChanged(int i);
    }

    private OrientationManager(Context context) {
        super(context);
        this.context = context;
    }

    public static OrientationManager getInstance(Context context) {
        if (instance == null) {
            instance = new OrientationManager(context);
            instance.enable();
        }
        return instance;
    }

    public int getOrientation() {
        return this.previousOrientation;
    }

    public void setOrientation(int orientation) {
        this.previousOrientation = orientation;
    }

    public void onOrientationChanged(int orientation) {
        if (orientation != -1) {
            if (this.previousOrientation == 0) {
                this.previousOrientation = this.context.getResources().getConfiguration().orientation;
                if (this.orientationChangeListener != null) {
                    this.orientationChangeListener.onOrientationChanged(this.previousOrientation);
                }
            }
            if (this.previousOrientation == 2 && ((this.previousAngle > 45 && orientation <= 45) || (this.previousAngle < 315 && this.previousAngle > 270 && orientation >= 315))) {
                if (this.orientationChangeListener != null) {
                    this.orientationChangeListener.onOrientationChanged(1);
                }
                this.previousOrientation = 1;
            }
            if (this.previousOrientation == 1 && ((this.previousAngle < 90 && orientation >= 90 && orientation < 270) || (this.previousAngle > 315 && orientation <= 315 && orientation > 180))) {
                if (this.orientationChangeListener != null) {
                    this.orientationChangeListener.onOrientationChanged(2);
                }
                this.previousOrientation = 2;
            }
            this.previousAngle = orientation;
        }
    }

    public void setOrientationChangedListener(OrientationChangeListener l) {
        this.orientationChangeListener = l;
    }
}
