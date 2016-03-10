package com.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.facebook.appevents.AppEventsLogger;

public abstract class FacebookButtonBase extends Button {
    private String analyticsButtonCreatedEventName;
    private OnClickListener externalOnClickListener;
    private OnClickListener internalOnClickListener;
    private boolean overrideCompoundPadding;
    private int overrideCompoundPaddingLeft;
    private int overrideCompoundPaddingRight;
    private Fragment parentFragment;

    protected abstract int getDefaultRequestCode();

    protected FacebookButtonBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, String analyticsButtonCreatedEventName) {
        super(context, attrs, 0);
        if (defStyleRes == 0) {
            defStyleRes = getDefaultStyleResource();
        }
        if (defStyleRes == 0) {
            defStyleRes = R.style.com_facebook_button;
        }
        configureButton(context, attrs, defStyleAttr, defStyleRes);
        this.analyticsButtonCreatedEventName = analyticsButtonCreatedEventName;
    }

    public void setFragment(Fragment fragment) {
        this.parentFragment = fragment;
    }

    public Fragment getFragment() {
        return this.parentFragment;
    }

    public void setOnClickListener(OnClickListener l) {
        this.externalOnClickListener = l;
    }

    public int getRequestCode() {
        return getDefaultRequestCode();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            logButtonCreated(getContext());
        }
    }

    protected void onDraw(Canvas canvas) {
        boolean centered;
        if ((getGravity() & 1) != 0) {
            centered = true;
        } else {
            centered = false;
        }
        if (centered) {
            int compoundPaddingLeft = getCompoundPaddingLeft();
            int compoundPaddingRight = getCompoundPaddingRight();
            int inset = Math.min((((getWidth() - (compoundPaddingLeft + getCompoundDrawablePadding())) - compoundPaddingRight) - measureTextWidth(getText().toString())) / 2, (compoundPaddingLeft - getPaddingLeft()) / 2);
            this.overrideCompoundPaddingLeft = compoundPaddingLeft - inset;
            this.overrideCompoundPaddingRight = compoundPaddingRight + inset;
            this.overrideCompoundPadding = true;
        }
        super.onDraw(canvas);
        this.overrideCompoundPadding = false;
    }

    public int getCompoundPaddingLeft() {
        return this.overrideCompoundPadding ? this.overrideCompoundPaddingLeft : super.getCompoundPaddingLeft();
    }

    public int getCompoundPaddingRight() {
        return this.overrideCompoundPadding ? this.overrideCompoundPaddingRight : super.getCompoundPaddingRight();
    }

    protected Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && (context instanceof ContextWrapper)) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof Activity) {
            return (Activity) context;
        }
        throw new FacebookException("Unable to get Activity.");
    }

    protected int getDefaultStyleResource() {
        return 0;
    }

    protected int measureTextWidth(String text) {
        return (int) Math.ceil((double) getPaint().measureText(text));
    }

    protected void configureButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        parseBackgroundAttributes(context, attrs, defStyleAttr, defStyleRes);
        parseCompoundDrawableAttributes(context, attrs, defStyleAttr, defStyleRes);
        parseContentAttributes(context, attrs, defStyleAttr, defStyleRes);
        parseTextAttributes(context, attrs, defStyleAttr, defStyleRes);
        setupOnClickListener();
    }

    protected void callExternalOnClickListener(View v) {
        if (this.externalOnClickListener != null) {
            this.externalOnClickListener.onClick(v);
        }
    }

    protected void setInternalOnClickListener(OnClickListener l) {
        this.internalOnClickListener = l;
    }

    private void logButtonCreated(Context context) {
        AppEventsLogger.newLogger(context).logSdkEvent(this.analyticsButtonCreatedEventName, null, null);
    }

    private void parseBackgroundAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (!isInEditMode()) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, new int[]{16842964}, defStyleAttr, defStyleRes);
            try {
                if (a.hasValue(0)) {
                    int backgroundResource = a.getResourceId(0, 0);
                    if (backgroundResource != 0) {
                        setBackgroundResource(backgroundResource);
                    } else {
                        setBackgroundColor(a.getColor(0, 0));
                    }
                } else {
                    setBackgroundColor(a.getColor(0, R.color.com_facebook_blue));
                }
                a.recycle();
            } catch (Throwable th) {
                a.recycle();
            }
        }
    }

    private void parseCompoundDrawableAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, new int[]{16843119, 16843117, 16843120, 16843118, 16843121}, defStyleAttr, defStyleRes);
        try {
            setCompoundDrawablesWithIntrinsicBounds(a.getResourceId(0, 0), a.getResourceId(1, 0), a.getResourceId(2, 0), a.getResourceId(3, 0));
            setCompoundDrawablePadding(a.getDimensionPixelSize(4, 0));
        } finally {
            a.recycle();
        }
    }

    private void parseContentAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, new int[]{16842966, 16842967, 16842968, 16842969}, defStyleAttr, defStyleRes);
        try {
            setPadding(a.getDimensionPixelSize(0, 0), a.getDimensionPixelSize(1, 0), a.getDimensionPixelSize(2, 0), a.getDimensionPixelSize(3, 0));
        } finally {
            a.recycle();
        }
    }

    private void parseTextAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray colorAttrs = context.getTheme().obtainStyledAttributes(attrs, new int[]{16842904}, defStyleAttr, defStyleRes);
        try {
            setTextColor(colorAttrs.getColor(0, -1));
            TypedArray gravityAttrs = context.getTheme().obtainStyledAttributes(attrs, new int[]{16842927}, defStyleAttr, defStyleRes);
            try {
                setGravity(gravityAttrs.getInt(0, 17));
                TypedArray a = context.getTheme().obtainStyledAttributes(attrs, new int[]{16842901, 16842903, 16843087}, defStyleAttr, defStyleRes);
                try {
                    setTextSize(0, (float) a.getDimensionPixelSize(0, 0));
                    setTypeface(Typeface.defaultFromStyle(a.getInt(1, 1)));
                    setText(a.getString(2));
                } finally {
                    a.recycle();
                }
            } finally {
                gravityAttrs.recycle();
            }
        } finally {
            colorAttrs.recycle();
        }
    }

    private void setupOnClickListener() {
        super.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (FacebookButtonBase.this.internalOnClickListener != null) {
                    FacebookButtonBase.this.internalOnClickListener.onClick(v);
                } else if (FacebookButtonBase.this.externalOnClickListener != null) {
                    FacebookButtonBase.this.externalOnClickListener.onClick(v);
                }
            }
        });
    }
}
