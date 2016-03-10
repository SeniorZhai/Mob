package com.soundcloud.android.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.volley.DefaultRetryPolicy;
import com.soundcloud.android.crop.ImageViewTouchBase.Recycler;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Iterator;

public class CropImageView extends ImageViewTouchBase {
    Context context;
    ArrayList<HighlightView> highlightViews = new ArrayList();
    private float lastX;
    private float lastY;
    private int motionEdge;
    HighlightView motionHighlightView;

    public /* bridge */ /* synthetic */ void clear() {
        super.clear();
    }

    public /* bridge */ /* synthetic */ Matrix getUnrotatedMatrix() {
        return super.getUnrotatedMatrix();
    }

    public /* bridge */ /* synthetic */ boolean onKeyDown(int x0, KeyEvent x1) {
        return super.onKeyDown(x0, x1);
    }

    public /* bridge */ /* synthetic */ boolean onKeyUp(int x0, KeyEvent x1) {
        return super.onKeyUp(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setImageBitmap(Bitmap x0) {
        super.setImageBitmap(x0);
    }

    public /* bridge */ /* synthetic */ void setImageBitmapResetBase(Bitmap x0, boolean x1) {
        super.setImageBitmapResetBase(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setImageRotateBitmapResetBase(RotateBitmap x0, boolean x1) {
        super.setImageRotateBitmapResetBase(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setRecycler(Recycler x0) {
        super.setRecycler(x0);
    }

    public CropImageView(Context context) {
        super(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.bitmapDisplayed.getBitmap() != null) {
            Iterator i$ = this.highlightViews.iterator();
            while (i$.hasNext()) {
                HighlightView hv = (HighlightView) i$.next();
                hv.matrix.set(getUnrotatedMatrix());
                hv.invalidate();
                if (hv.hasFocus()) {
                    centerBasedOnHighlightView(hv);
                }
            }
        }
    }

    protected void zoomTo(float scale, float centerX, float centerY) {
        super.zoomTo(scale, centerX, centerY);
        Iterator i$ = this.highlightViews.iterator();
        while (i$.hasNext()) {
            HighlightView hv = (HighlightView) i$.next();
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    protected void zoomIn() {
        super.zoomIn();
        Iterator i$ = this.highlightViews.iterator();
        while (i$.hasNext()) {
            HighlightView hv = (HighlightView) i$.next();
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    protected void zoomOut() {
        super.zoomOut();
        Iterator i$ = this.highlightViews.iterator();
        while (i$.hasNext()) {
            HighlightView hv = (HighlightView) i$.next();
            hv.matrix.set(getUnrotatedMatrix());
            hv.invalidate();
        }
    }

    protected void postTranslate(float deltaX, float deltaY) {
        super.postTranslate(deltaX, deltaY);
        Iterator i$ = this.highlightViews.iterator();
        while (i$.hasNext()) {
            HighlightView hv = (HighlightView) i$.next();
            hv.matrix.postTranslate(deltaX, deltaY);
            hv.invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.context.isSaving()) {
            return false;
        }
        switch (event.getAction()) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                Iterator i$ = this.highlightViews.iterator();
                while (i$.hasNext()) {
                    HighlightView hv = (HighlightView) i$.next();
                    int edge = hv.getHit(event.getX(), event.getY());
                    if (edge != 1) {
                        this.motionEdge = edge;
                        this.motionHighlightView = hv;
                        this.lastX = event.getX();
                        this.lastY = event.getY();
                        this.motionHighlightView.setMode(edge == 32 ? ModifyMode.Move : ModifyMode.Grow);
                        break;
                    }
                }
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (this.motionHighlightView != null) {
                    centerBasedOnHighlightView(this.motionHighlightView);
                    this.motionHighlightView.setMode(ModifyMode.None);
                }
                this.motionHighlightView = null;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this.motionHighlightView != null) {
                    this.motionHighlightView.handleMotion(this.motionEdge, event.getX() - this.lastX, event.getY() - this.lastY);
                    this.lastX = event.getX();
                    this.lastY = event.getY();
                    ensureVisible(this.motionHighlightView);
                    break;
                }
                break;
        }
        switch (event.getAction()) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                center(true, true);
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (getScale() == DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                    center(true, true);
                    break;
                }
                break;
        }
        return true;
    }

    private void ensureVisible(HighlightView hv) {
        int panDeltaX;
        int panDeltaY;
        Rect r = hv.drawRect;
        int panDeltaX1 = Math.max(0, getLeft() - r.left);
        int panDeltaX2 = Math.min(0, getRight() - r.right);
        int panDeltaY1 = Math.max(0, getTop() - r.top);
        int panDeltaY2 = Math.min(0, getBottom() - r.bottom);
        if (panDeltaX1 != 0) {
            panDeltaX = panDeltaX1;
        } else {
            panDeltaX = panDeltaX2;
        }
        if (panDeltaY1 != 0) {
            panDeltaY = panDeltaY1;
        } else {
            panDeltaY = panDeltaY2;
        }
        if (panDeltaX != 0 || panDeltaY != 0) {
            panBy((float) panDeltaX, (float) panDeltaY);
        }
    }

    private void centerBasedOnHighlightView(HighlightView hv) {
        Rect drawRect = hv.drawRect;
        float thisWidth = (float) getWidth();
        float thisHeight = (float) getHeight();
        float zoom = Math.max(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, Math.min((thisWidth / ((float) drawRect.width())) * 0.6f, (thisHeight / ((float) drawRect.height())) * 0.6f) * getScale());
        if (((double) (Math.abs(zoom - getScale()) / zoom)) > 0.1d) {
            float[] coordinates = new float[]{hv.cropRect.centerX(), hv.cropRect.centerY()};
            getUnrotatedMatrix().mapPoints(coordinates);
            zoomTo(zoom, coordinates[0], coordinates[1], 300.0f);
        }
        ensureVisible(hv);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator i$ = this.highlightViews.iterator();
        while (i$.hasNext()) {
            ((HighlightView) i$.next()).draw(canvas);
        }
    }

    public void add(HighlightView hv) {
        this.highlightViews.add(hv);
        invalidate();
    }
}
