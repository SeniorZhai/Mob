package com.andexert.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import com.android.volley.DefaultRetryPolicy;
import org.apache.http.HttpStatus;

public class RippleView extends RelativeLayout {
    private int HEIGHT;
    private int WIDTH;
    private boolean animationRunning = false;
    private Handler canvasHandler;
    private int durationEmpty = -1;
    private int frameRate = 10;
    private GestureDetector gestureDetector;
    private Boolean hasToZoom;
    private Boolean isCentered;
    private OnRippleCompleteListener onCompletionListener;
    private Bitmap originBitmap;
    private Paint paint;
    private float radiusMax = 0.0f;
    private int rippleAlpha = 90;
    private int rippleColor;
    private int rippleDuration = HttpStatus.SC_BAD_REQUEST;
    private int ripplePadding;
    private Integer rippleType;
    private final Runnable runnable = new Runnable() {
        public void run() {
            RippleView.this.invalidate();
        }
    };
    private ScaleAnimation scaleAnimation;
    private int timer = 0;
    private int timerEmpty = 0;
    private float x = -1.0f;
    private float y = -1.0f;
    private int zoomDuration;
    private float zoomScale;

    public interface OnRippleCompleteListener {
        void onComplete(RippleView rippleView);
    }

    public enum RippleType {
        SIMPLE(0),
        DOUBLE(1),
        RECTANGLE(2);
        
        int type;

        private RippleType(int type) {
            this.type = type;
        }
    }

    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
            this.rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, getResources().getColor(R.color.rippelColor));
            this.rippleType = Integer.valueOf(typedArray.getInt(R.styleable.RippleView_rv_type, 0));
            this.hasToZoom = Boolean.valueOf(typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false));
            this.isCentered = Boolean.valueOf(typedArray.getBoolean(R.styleable.RippleView_rv_centered, false));
            this.rippleDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, this.rippleDuration);
            this.frameRate = typedArray.getInteger(R.styleable.RippleView_rv_framerate, this.frameRate);
            this.rippleAlpha = typedArray.getInteger(R.styleable.RippleView_rv_alpha, this.rippleAlpha);
            this.ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0);
            this.canvasHandler = new Handler();
            this.zoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, 1.03f);
            this.zoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, HttpStatus.SC_OK);
            typedArray.recycle();
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setStyle(Style.FILL);
            this.paint.setColor(this.rippleColor);
            this.paint.setAlpha(this.rippleAlpha);
            setWillNotDraw(false);
            this.gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
                public void onLongPress(MotionEvent event) {
                    super.onLongPress(event);
                    RippleView.this.animateRipple(event);
                    RippleView.this.sendClickEvent(Boolean.valueOf(true));
                }

                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return true;
                }

                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
            setDrawingCacheEnabled(true);
            setClickable(true);
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!this.animationRunning) {
            return;
        }
        if (this.rippleDuration <= this.timer * this.frameRate) {
            this.animationRunning = false;
            this.timer = 0;
            this.durationEmpty = -1;
            this.timerEmpty = 0;
            canvas.restore();
            invalidate();
            if (this.onCompletionListener != null) {
                this.onCompletionListener.onComplete(this);
                return;
            }
            return;
        }
        this.canvasHandler.postDelayed(this.runnable, (long) this.frameRate);
        if (this.timer == 0) {
            canvas.save();
        }
        canvas.drawCircle(this.x, this.y, this.radiusMax * ((((float) this.timer) * ((float) this.frameRate)) / ((float) this.rippleDuration)), this.paint);
        this.paint.setColor(Color.parseColor("#ffff4444"));
        if (this.rippleType.intValue() == 1 && this.originBitmap != null && (((float) this.timer) * ((float) this.frameRate)) / ((float) this.rippleDuration) > 0.4f) {
            if (this.durationEmpty == -1) {
                this.durationEmpty = this.rippleDuration - (this.timer * this.frameRate);
            }
            this.timerEmpty++;
            Bitmap tmpBitmap = getCircleBitmap((int) (this.radiusMax * ((((float) this.timerEmpty) * ((float) this.frameRate)) / ((float) this.durationEmpty))));
            canvas.drawBitmap(tmpBitmap, 0.0f, 0.0f, this.paint);
            tmpBitmap.recycle();
        }
        this.paint.setColor(this.rippleColor);
        if (this.rippleType.intValue() != 1) {
            this.paint.setAlpha((int) (((float) this.rippleAlpha) - (((float) this.rippleAlpha) * ((((float) this.timer) * ((float) this.frameRate)) / ((float) this.rippleDuration)))));
        } else if ((((float) this.timer) * ((float) this.frameRate)) / ((float) this.rippleDuration) > 0.6f) {
            this.paint.setAlpha((int) (((float) this.rippleAlpha) - (((float) this.rippleAlpha) * ((((float) this.timerEmpty) * ((float) this.frameRate)) / ((float) this.durationEmpty)))));
        } else {
            this.paint.setAlpha(this.rippleAlpha);
        }
        this.timer++;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.WIDTH = w;
        this.HEIGHT = h;
        float f = (float) (w / 2);
        float f2 = (float) (h / 2);
        this.scaleAnimation = new ScaleAnimation(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, this.zoomScale, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, this.zoomScale, f, f2);
        this.scaleAnimation.setDuration((long) this.zoomDuration);
        this.scaleAnimation.setRepeatMode(2);
        this.scaleAnimation.setRepeatCount(1);
    }

    public void animateRipple(MotionEvent event) {
        createAnimation(event.getX(), event.getY());
    }

    public void animateRipple(float x, float y) {
        createAnimation(x, y);
    }

    private void createAnimation(float x, float y) {
        if (isEnabled() && !this.animationRunning) {
            if (this.hasToZoom.booleanValue()) {
                startAnimation(this.scaleAnimation);
            }
            this.radiusMax = (float) Math.max(this.WIDTH, this.HEIGHT);
            if (this.rippleType.intValue() != 2) {
                this.radiusMax /= 2.0f;
            }
            this.radiusMax -= (float) this.ripplePadding;
            if (this.isCentered.booleanValue() || this.rippleType.intValue() == 1) {
                this.x = (float) (getMeasuredWidth() / 2);
                this.y = (float) (getMeasuredHeight() / 2);
            } else {
                this.x = x;
                this.y = y;
            }
            this.animationRunning = true;
            if (this.rippleType.intValue() == 1 && this.originBitmap == null) {
                this.originBitmap = getDrawingCache(true);
            }
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.gestureDetector.onTouchEvent(event)) {
            animateRipple(event);
            sendClickEvent(Boolean.valueOf(false));
        }
        return super.onTouchEvent(event);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }

    private void sendClickEvent(Boolean isLongClick) {
        if (getParent() instanceof AdapterView) {
            AdapterView adapterView = (AdapterView) getParent();
            int position = adapterView.getPositionForView(this);
            long id = adapterView.getItemIdAtPosition(position);
            if (isLongClick.booleanValue()) {
                if (adapterView.getOnItemLongClickListener() != null) {
                    adapterView.getOnItemLongClickListener().onItemLongClick(adapterView, this, position, id);
                }
            } else if (adapterView.getOnItemClickListener() != null) {
                adapterView.getOnItemClickListener().onItemClick(adapterView, this, position, id);
            }
        }
    }

    private Bitmap getCircleBitmap(int radius) {
        Bitmap output = Bitmap.createBitmap(this.originBitmap.getWidth(), this.originBitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect((int) (this.x - ((float) radius)), (int) (this.y - ((float) radius)), (int) (this.x + ((float) radius)), (int) (this.y + ((float) radius)));
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(this.x, this.y, (float) radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(this.originBitmap, rect, rect, paint);
        return output;
    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = getResources().getColor(rippleColor);
    }

    public int getRippleColor() {
        return this.rippleColor;
    }

    public RippleType getRippleType() {
        return RippleType.values()[this.rippleType.intValue()];
    }

    public void setRippleType(RippleType rippleType) {
        this.rippleType = Integer.valueOf(rippleType.ordinal());
    }

    public Boolean isCentered() {
        return this.isCentered;
    }

    public void setCentered(Boolean isCentered) {
        this.isCentered = isCentered;
    }

    public int getRipplePadding() {
        return this.ripplePadding;
    }

    public void setRipplePadding(int ripplePadding) {
        this.ripplePadding = ripplePadding;
    }

    public Boolean isZooming() {
        return this.hasToZoom;
    }

    public void setZooming(Boolean hasToZoom) {
        this.hasToZoom = hasToZoom;
    }

    public float getZoomScale() {
        return this.zoomScale;
    }

    public void setZoomScale(float zoomScale) {
        this.zoomScale = zoomScale;
    }

    public int getZoomDuration() {
        return this.zoomDuration;
    }

    public void setZoomDuration(int zoomDuration) {
        this.zoomDuration = zoomDuration;
    }

    public int getRippleDuration() {
        return this.rippleDuration;
    }

    public void setRippleDuration(int rippleDuration) {
        this.rippleDuration = rippleDuration;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getRippleAlpha() {
        return this.rippleAlpha;
    }

    public void setRippleAlpha(int rippleAlpha) {
        this.rippleAlpha = rippleAlpha;
    }

    public void setOnRippleCompleteListener(OnRippleCompleteListener listener) {
        this.onCompletionListener = listener;
    }
}
