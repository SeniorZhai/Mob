package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v4.view.ViewCompat;
import android.view.View;
import com.android.volley.DefaultRetryPolicy;

class FloatingActionButtonHoneycombMr1 extends FloatingActionButtonEclairMr1 {
    private boolean mIsHiding;

    FloatingActionButtonHoneycombMr1(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);
    }

    void hide() {
        if (!this.mIsHiding && this.mView.getVisibility() == 0) {
            if (!ViewCompat.isLaidOut(this.mView) || this.mView.isInEditMode()) {
                this.mView.setVisibility(8);
            } else {
                this.mView.animate().scaleX(0.0f).scaleY(0.0f).alpha(0.0f).setDuration(200).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animation) {
                        FloatingActionButtonHoneycombMr1.this.mIsHiding = true;
                        FloatingActionButtonHoneycombMr1.this.mView.setVisibility(0);
                    }

                    public void onAnimationCancel(Animator animation) {
                        FloatingActionButtonHoneycombMr1.this.mIsHiding = false;
                    }

                    public void onAnimationEnd(Animator animation) {
                        FloatingActionButtonHoneycombMr1.this.mIsHiding = false;
                        FloatingActionButtonHoneycombMr1.this.mView.setVisibility(8);
                    }
                });
            }
        }
    }

    void show() {
        if (this.mView.getVisibility() == 0) {
            return;
        }
        if (!ViewCompat.isLaidOut(this.mView) || this.mView.isInEditMode()) {
            this.mView.setVisibility(0);
            this.mView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.mView.setScaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            this.mView.setScaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            return;
        }
        this.mView.setAlpha(0.0f);
        this.mView.setScaleY(0.0f);
        this.mView.setScaleX(0.0f);
        this.mView.animate().scaleX(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).scaleY(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).alpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setDuration(200).setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                FloatingActionButtonHoneycombMr1.this.mView.setVisibility(0);
            }
        });
    }
}
