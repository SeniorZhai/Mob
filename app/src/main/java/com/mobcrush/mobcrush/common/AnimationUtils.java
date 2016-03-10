package com.mobcrush.mobcrush.common;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
    public static void changeColor(final View view, int colorFrom, int colorTo, boolean checkForColorToStart) {
        if (view != null) {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(colorFrom), Integer.valueOf(colorTo)});
            colorAnimation.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animator) {
                    view.setBackgroundColor(((Integer) animator.getAnimatedValue()).intValue());
                }
            });
            colorAnimation.setDuration(300);
            if (!checkForColorToStart || (view.getBackground() != null && ((ColorDrawable) view.getBackground()).getColor() != colorTo)) {
                colorAnimation.start();
            }
        }
    }

    public static void expand(View v, int posFrom) {
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, (float) posFrom, 0.0f);
        animation.setDuration(300);
        v.startAnimation(animation);
    }

    public static void shift(View v, int posFrom, int posTo, final Runnable onAnimationEnd) {
        boolean z;
        boolean z2 = true;
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, (float) posFrom, (float) posTo);
        animation.setDuration(300);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (onAnimationEnd != null) {
                    try {
                        onAnimationEnd.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        if (onAnimationEnd != null) {
            z = true;
        } else {
            z = false;
        }
        animation.setFillBefore(z);
        if (onAnimationEnd == null) {
            z2 = false;
        }
        animation.setFillEnabled(z2);
        v.startAnimation(animation);
    }

    public static void collapse(View v, int posTo, final Runnable onAnimationEnd) {
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) posTo);
        animation.setDuration(300);
        animation.setFillBefore(true);
        animation.setFillEnabled(true);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (onAnimationEnd != null) {
                    try {
                        onAnimationEnd.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(animation);
    }

    private static ValueAnimator slideAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{start, end});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}
