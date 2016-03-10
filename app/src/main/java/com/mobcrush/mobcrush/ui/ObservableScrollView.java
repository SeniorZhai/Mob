package com.mobcrush.mobcrush.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Iterator;

public class ObservableScrollView extends ScrollView {
    private ArrayList<Callbacks> mCallbacks = new ArrayList();
    private boolean mCanScrollDown;
    private boolean mCanScrollUp;
    private OnTouchListener mOnToushListenerEx;
    private float mPrevY = -1.0f;

    public interface Callbacks {
        void onScrollChanged(int i, int i2);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Iterator i$ = this.mCallbacks.iterator();
        while (i$.hasNext()) {
            ((Callbacks) i$.next()).onScrollChanged(l - oldl, t - oldt);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                Log.i("VerticalScrollview", "onInterceptTouchEvent: DOWN super false");
                this.mPrevY = ev.getY();
                super.onTouchEvent(ev);
                return false;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                Log.i("VerticalScrollview", "onInterceptTouchEvent: UP super false");
                this.mPrevY = -1.0f;
                return false;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this.mPrevY == -1.0f) {
                    this.mPrevY = ev.getY();
                    return super.onTouchEvent(ev);
                } else if (this.mPrevY <= ev.getY()) {
                    this.mPrevY = ev.getY();
                    return this.mCanScrollDown;
                } else if (this.mPrevY < ev.getY()) {
                    return super.onTouchEvent(ev);
                } else {
                    this.mPrevY = ev.getY();
                    return this.mCanScrollUp;
                }
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                Log.i("VerticalScrollview", "onInterceptTouchEvent: CANCEL super false");
                super.onTouchEvent(ev);
                return false;
            default:
                Log.i("VerticalScrollview", "onInterceptTouchEvent: " + action);
                return false;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mOnToushListenerEx != null) {
            this.mOnToushListenerEx.onTouch(null, ev);
        }
        return super.onTouchEvent(ev);
    }

    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void addCallbacks(Callbacks listener) {
        if (!this.mCallbacks.contains(listener)) {
            this.mCallbacks.add(listener);
        }
    }

    public void setScrollStates(boolean canScrollUp, boolean canScrollDown) {
        this.mCanScrollUp = canScrollUp;
        this.mCanScrollDown = canScrollDown;
    }

    public void setOnTouchListenerEx(OnTouchListener listener) {
        this.mOnToushListenerEx = listener;
    }
}
