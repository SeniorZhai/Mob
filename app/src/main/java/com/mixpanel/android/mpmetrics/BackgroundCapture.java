package com.mixpanel.android.mpmetrics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.support.v4.view.ViewCompat;
import com.mixpanel.android.util.ActivityImageUtils;
import com.mixpanel.android.util.StackBlurManager;

class BackgroundCapture {
    private static final int GRAY_72PERCENT_OPAQUE = Color.argb(186, 28, 28, 28);
    private static final String LOGTAG = "MixpanelAPI.BackgroundCapture";

    private static class BackgroundCaptureTask extends AsyncTask<Void, Void, Void> {
        private int mCalculatedHighlightColor = ViewCompat.MEASURED_STATE_MASK;
        private final OnBackgroundCapturedListener mListener;
        private final Activity mParentActivity;
        private Bitmap mSourceImage;

        public BackgroundCaptureTask(Activity parentActivity, OnBackgroundCapturedListener listener) {
            this.mParentActivity = parentActivity;
            this.mListener = listener;
        }

        protected void onPreExecute() {
            this.mSourceImage = ActivityImageUtils.getScaledScreenshot(this.mParentActivity, 2, 2, true);
            this.mCalculatedHighlightColor = ActivityImageUtils.getHighlightColorFromBitmap(this.mSourceImage);
        }

        protected Void doInBackground(Void... params) {
            if (this.mSourceImage != null) {
                try {
                    StackBlurManager.process(this.mSourceImage, 20);
                    new Canvas(this.mSourceImage).drawColor(BackgroundCapture.GRAY_72PERCENT_OPAQUE, Mode.SRC_ATOP);
                } catch (ArrayIndexOutOfBoundsException e) {
                    this.mSourceImage = null;
                } catch (OutOfMemoryError e2) {
                    this.mSourceImage = null;
                }
            }
            return null;
        }

        protected void onPostExecute(Void _ignored) {
            this.mListener.onBackgroundCaptured(this.mSourceImage, this.mCalculatedHighlightColor);
        }
    }

    public interface OnBackgroundCapturedListener {
        void onBackgroundCaptured(Bitmap bitmap, int i);
    }

    BackgroundCapture() {
    }

    public static void captureBackground(final Activity parentActivity, final OnBackgroundCapturedListener listener) {
        parentActivity.runOnUiThread(new Runnable() {
            public void run() {
                new BackgroundCaptureTask(parentActivity, listener).execute(new Void[BackgroundCapture.GRAY_72PERCENT_OPAQUE]);
            }
        });
    }
}
