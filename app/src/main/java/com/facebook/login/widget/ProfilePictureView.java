package com.facebook.login.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.facebook.FacebookException;
import com.facebook.LoggingBehavior;
import com.facebook.R;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageRequest.Builder;
import com.facebook.internal.ImageRequest.Callback;
import com.facebook.internal.ImageResponse;
import com.facebook.internal.Logger;
import com.facebook.internal.Utility;

public class ProfilePictureView extends FrameLayout {
    private static final String BITMAP_HEIGHT_KEY = "ProfilePictureView_height";
    private static final String BITMAP_KEY = "ProfilePictureView_bitmap";
    private static final String BITMAP_WIDTH_KEY = "ProfilePictureView_width";
    public static final int CUSTOM = -1;
    private static final boolean IS_CROPPED_DEFAULT_VALUE = true;
    private static final String IS_CROPPED_KEY = "ProfilePictureView_isCropped";
    public static final int LARGE = -4;
    private static final int MIN_SIZE = 1;
    public static final int NORMAL = -3;
    private static final String PENDING_REFRESH_KEY = "ProfilePictureView_refresh";
    private static final String PRESET_SIZE_KEY = "ProfilePictureView_presetSize";
    private static final String PROFILE_ID_KEY = "ProfilePictureView_profileId";
    public static final int SMALL = -2;
    private static final String SUPER_STATE_KEY = "ProfilePictureView_superState";
    public static final String TAG = ProfilePictureView.class.getSimpleName();
    private Bitmap customizedDefaultProfilePicture = null;
    private ImageView image;
    private Bitmap imageContents;
    private boolean isCropped = IS_CROPPED_DEFAULT_VALUE;
    private ImageRequest lastRequest;
    private OnErrorListener onErrorListener;
    private int presetSizeType = CUSTOM;
    private String profileId;
    private int queryHeight = 0;
    private int queryWidth = 0;

    public interface OnErrorListener {
        void onError(FacebookException facebookException);
    }

    public ProfilePictureView(Context context) {
        super(context);
        initialize(context);
    }

    public ProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
        parseAttributes(attrs);
    }

    public ProfilePictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
        parseAttributes(attrs);
    }

    public final int getPresetSize() {
        return this.presetSizeType;
    }

    public final void setPresetSize(int sizeType) {
        switch (sizeType) {
            case LARGE /*-4*/:
            case NORMAL /*-3*/:
            case SMALL /*-2*/:
            case CUSTOM /*-1*/:
                this.presetSizeType = sizeType;
                requestLayout();
                return;
            default:
                throw new IllegalArgumentException("Must use a predefined preset size");
        }
    }

    public final boolean isCropped() {
        return this.isCropped;
    }

    public final void setCropped(boolean showCroppedVersion) {
        this.isCropped = showCroppedVersion;
        refreshImage(false);
    }

    public final String getProfileId() {
        return this.profileId;
    }

    public final void setProfileId(String profileId) {
        boolean force = false;
        if (Utility.isNullOrEmpty(this.profileId) || !this.profileId.equalsIgnoreCase(profileId)) {
            setBlankProfilePicture();
            force = IS_CROPPED_DEFAULT_VALUE;
        }
        this.profileId = profileId;
        refreshImage(force);
    }

    public final OnErrorListener getOnErrorListener() {
        return this.onErrorListener;
    }

    public final void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public final void setDefaultProfilePicture(Bitmap inputBitmap) {
        this.customizedDefaultProfilePicture = inputBitmap;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams params = getLayoutParams();
        boolean customMeasure = false;
        int newHeight = MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(heightMeasureSpec) != 1073741824 && params.height == SMALL) {
            newHeight = getPresetSizeInPixels(IS_CROPPED_DEFAULT_VALUE);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, 1073741824);
            customMeasure = IS_CROPPED_DEFAULT_VALUE;
        }
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824 && params.width == SMALL) {
            newWidth = getPresetSizeInPixels(IS_CROPPED_DEFAULT_VALUE);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(newWidth, 1073741824);
            customMeasure = IS_CROPPED_DEFAULT_VALUE;
        }
        if (customMeasure) {
            setMeasuredDimension(newWidth, newHeight);
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        refreshImage(false);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle instanceState = new Bundle();
        instanceState.putParcelable(SUPER_STATE_KEY, superState);
        instanceState.putString(PROFILE_ID_KEY, this.profileId);
        instanceState.putInt(PRESET_SIZE_KEY, this.presetSizeType);
        instanceState.putBoolean(IS_CROPPED_KEY, this.isCropped);
        instanceState.putParcelable(BITMAP_KEY, this.imageContents);
        instanceState.putInt(BITMAP_WIDTH_KEY, this.queryWidth);
        instanceState.putInt(BITMAP_HEIGHT_KEY, this.queryHeight);
        instanceState.putBoolean(PENDING_REFRESH_KEY, this.lastRequest != null ? IS_CROPPED_DEFAULT_VALUE : false);
        return instanceState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state.getClass() != Bundle.class) {
            super.onRestoreInstanceState(state);
            return;
        }
        Bundle instanceState = (Bundle) state;
        super.onRestoreInstanceState(instanceState.getParcelable(SUPER_STATE_KEY));
        this.profileId = instanceState.getString(PROFILE_ID_KEY);
        this.presetSizeType = instanceState.getInt(PRESET_SIZE_KEY);
        this.isCropped = instanceState.getBoolean(IS_CROPPED_KEY);
        this.queryWidth = instanceState.getInt(BITMAP_WIDTH_KEY);
        this.queryHeight = instanceState.getInt(BITMAP_HEIGHT_KEY);
        setImageBitmap((Bitmap) instanceState.getParcelable(BITMAP_KEY));
        if (instanceState.getBoolean(PENDING_REFRESH_KEY)) {
            refreshImage(IS_CROPPED_DEFAULT_VALUE);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.lastRequest = null;
    }

    private void initialize(Context context) {
        removeAllViews();
        this.image = new ImageView(context);
        this.image.setLayoutParams(new FrameLayout.LayoutParams(CUSTOM, CUSTOM));
        this.image.setScaleType(ScaleType.CENTER_INSIDE);
        addView(this.image);
    }

    private void parseAttributes(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.com_facebook_profile_picture_view);
        setPresetSize(a.getInt(R.styleable.com_facebook_profile_picture_view_com_facebook_preset_size, CUSTOM));
        this.isCropped = a.getBoolean(R.styleable.com_facebook_profile_picture_view_com_facebook_is_cropped, IS_CROPPED_DEFAULT_VALUE);
        a.recycle();
    }

    private void refreshImage(boolean force) {
        boolean changed = updateImageQueryParameters();
        if (this.profileId == null || this.profileId.length() == 0 || (this.queryWidth == 0 && this.queryHeight == 0)) {
            setBlankProfilePicture();
        } else if (changed || force) {
            sendImageRequest(IS_CROPPED_DEFAULT_VALUE);
        }
    }

    private void setBlankProfilePicture() {
        if (this.lastRequest != null) {
            ImageDownloader.cancelRequest(this.lastRequest);
        }
        if (this.customizedDefaultProfilePicture == null) {
            setImageBitmap(BitmapFactory.decodeResource(getResources(), isCropped() ? R.drawable.com_facebook_profile_picture_blank_square : R.drawable.com_facebook_profile_picture_blank_portrait));
            return;
        }
        updateImageQueryParameters();
        setImageBitmap(Bitmap.createScaledBitmap(this.customizedDefaultProfilePicture, this.queryWidth, this.queryHeight, false));
    }

    private void setImageBitmap(Bitmap imageBitmap) {
        if (this.image != null && imageBitmap != null) {
            this.imageContents = imageBitmap;
            this.image.setImageBitmap(imageBitmap);
        }
    }

    private void sendImageRequest(boolean allowCachedResponse) {
        ImageRequest request = new Builder(getContext(), ImageRequest.getProfilePictureUri(this.profileId, this.queryWidth, this.queryHeight)).setAllowCachedRedirects(allowCachedResponse).setCallerTag(this).setCallback(new Callback() {
            public void onCompleted(ImageResponse response) {
                ProfilePictureView.this.processResponse(response);
            }
        }).build();
        if (this.lastRequest != null) {
            ImageDownloader.cancelRequest(this.lastRequest);
        }
        this.lastRequest = request;
        ImageDownloader.downloadAsync(request);
    }

    private void processResponse(ImageResponse response) {
        if (response.getRequest() == this.lastRequest) {
            this.lastRequest = null;
            Bitmap responseImage = response.getBitmap();
            Throwable error = response.getError();
            if (error != null) {
                OnErrorListener listener = this.onErrorListener;
                if (listener != null) {
                    listener.onError(new FacebookException("Error in downloading profile picture for profileId: " + getProfileId(), error));
                } else {
                    Logger.log(LoggingBehavior.REQUESTS, 6, TAG, error.toString());
                }
            } else if (responseImage != null) {
                setImageBitmap(responseImage);
                if (response.isCachedRedirect()) {
                    sendImageRequest(false);
                }
            }
        }
    }

    private boolean updateImageQueryParameters() {
        boolean changed = IS_CROPPED_DEFAULT_VALUE;
        int newHeightPx = getHeight();
        int newWidthPx = getWidth();
        if (newWidthPx < MIN_SIZE || newHeightPx < MIN_SIZE) {
            return false;
        }
        int presetSize = getPresetSizeInPixels(false);
        if (presetSize != 0) {
            newWidthPx = presetSize;
            newHeightPx = presetSize;
        }
        if (newWidthPx > newHeightPx) {
            newWidthPx = isCropped() ? newHeightPx : 0;
        } else if (isCropped()) {
            newHeightPx = newWidthPx;
        } else {
            newHeightPx = 0;
        }
        if (newWidthPx == this.queryWidth && newHeightPx == this.queryHeight) {
            changed = false;
        }
        this.queryWidth = newWidthPx;
        this.queryHeight = newHeightPx;
        return changed;
    }

    private int getPresetSizeInPixels(boolean forcePreset) {
        int dimensionId;
        switch (this.presetSizeType) {
            case LARGE /*-4*/:
                dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_large;
                break;
            case NORMAL /*-3*/:
                dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_normal;
                break;
            case SMALL /*-2*/:
                dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_small;
                break;
            case CUSTOM /*-1*/:
                if (forcePreset) {
                    dimensionId = R.dimen.com_facebook_profilepictureview_preset_size_normal;
                    break;
                }
                return 0;
            default:
                return 0;
        }
        return getResources().getDimensionPixelSize(dimensionId);
    }
}
