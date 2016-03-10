package com.google.android.gms.common.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.widget.ImageView;
import com.google.android.gms.common.images.ImageManager.OnImageLoadedListener;
import com.google.android.gms.common.internal.zzt;
import com.google.android.gms.internal.zzkg;
import com.google.android.gms.internal.zzkh;
import com.google.android.gms.internal.zzki;
import com.google.android.gms.internal.zzkj;
import java.lang.ref.WeakReference;

public abstract class zza {
    final zza zzZc;
    protected int zzZd = 0;
    protected int zzZe = 0;
    protected boolean zzZf = false;
    protected OnImageLoadedListener zzZg;
    private boolean zzZh = true;
    private boolean zzZi = false;
    private boolean zzZj = true;
    protected int zzZk;

    static final class zza {
        public final Uri uri;

        public zza(Uri uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            if (obj instanceof zza) {
                return this == obj ? true : zzt.equal(((zza) obj).uri, this.uri);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return zzt.hashCode(this.uri);
        }
    }

    public static final class zzb extends zza {
        private WeakReference<ImageView> zzZl;

        public zzb(ImageView imageView, int i) {
            super(null, i);
            com.google.android.gms.common.internal.zzb.zzq(imageView);
            this.zzZl = new WeakReference(imageView);
        }

        public zzb(ImageView imageView, Uri uri) {
            super(uri, 0);
            com.google.android.gms.common.internal.zzb.zzq(imageView);
            this.zzZl = new WeakReference(imageView);
        }

        private void zza(ImageView imageView, Drawable drawable, boolean z, boolean z2, boolean z3) {
            Object obj = (z2 || z3) ? null : 1;
            if (obj != null && (imageView instanceof zzki)) {
                int zznr = ((zzki) imageView).zznr();
                if (this.zzZe != 0 && zznr == this.zzZe) {
                    return;
                }
            }
            boolean zzb = zzb(z, z2);
            Drawable newDrawable = (!this.zzZf || drawable == null) ? drawable : drawable.getConstantState().newDrawable();
            if (zzb) {
                newDrawable = zza(imageView.getDrawable(), newDrawable);
            }
            imageView.setImageDrawable(newDrawable);
            if (imageView instanceof zzki) {
                zzki com_google_android_gms_internal_zzki = (zzki) imageView;
                com_google_android_gms_internal_zzki.zzi(z3 ? this.zzZc.uri : null);
                com_google_android_gms_internal_zzki.zzbo(obj != null ? this.zzZe : 0);
            }
            if (zzb) {
                ((zzkg) newDrawable).startTransition(Callback.DEFAULT_SWIPE_ANIMATION_DURATION);
            }
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zzb)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            ImageView imageView = (ImageView) this.zzZl.get();
            ImageView imageView2 = (ImageView) ((zzb) obj).zzZl.get();
            boolean z = (imageView2 == null || imageView == null || !zzt.equal(imageView2, imageView)) ? false : true;
            return z;
        }

        public int hashCode() {
            return 0;
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            ImageView imageView = (ImageView) this.zzZl.get();
            if (imageView != null) {
                zza(imageView, drawable, z, z2, z3);
            }
        }
    }

    public static final class zzc extends zza {
        private WeakReference<OnImageLoadedListener> zzZm;

        public zzc(OnImageLoadedListener onImageLoadedListener, Uri uri) {
            super(uri, 0);
            com.google.android.gms.common.internal.zzb.zzq(onImageLoadedListener);
            this.zzZm = new WeakReference(onImageLoadedListener);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zzc)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            zzc com_google_android_gms_common_images_zza_zzc = (zzc) obj;
            OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzZm.get();
            OnImageLoadedListener onImageLoadedListener2 = (OnImageLoadedListener) com_google_android_gms_common_images_zza_zzc.zzZm.get();
            boolean z = onImageLoadedListener2 != null && onImageLoadedListener != null && zzt.equal(onImageLoadedListener2, onImageLoadedListener) && zzt.equal(com_google_android_gms_common_images_zza_zzc.zzZc, this.zzZc);
            return z;
        }

        public int hashCode() {
            return zzt.hashCode(this.zzZc);
        }

        protected void zza(Drawable drawable, boolean z, boolean z2, boolean z3) {
            if (!z2) {
                OnImageLoadedListener onImageLoadedListener = (OnImageLoadedListener) this.zzZm.get();
                if (onImageLoadedListener != null) {
                    onImageLoadedListener.onImageLoaded(this.zzZc.uri, drawable, z3);
                }
            }
        }
    }

    public zza(Uri uri, int i) {
        this.zzZc = new zza(uri);
        this.zzZe = i;
    }

    private Drawable zza(Context context, zzkj com_google_android_gms_internal_zzkj, int i) {
        Resources resources = context.getResources();
        if (this.zzZk <= 0) {
            return resources.getDrawable(i);
        }
        com.google.android.gms.internal.zzkj.zza com_google_android_gms_internal_zzkj_zza = new com.google.android.gms.internal.zzkj.zza(i, this.zzZk);
        Drawable drawable = (Drawable) com_google_android_gms_internal_zzkj.get(com_google_android_gms_internal_zzkj_zza);
        if (drawable != null) {
            return drawable;
        }
        drawable = resources.getDrawable(i);
        if ((this.zzZk & 1) != 0) {
            drawable = zza(resources, drawable);
        }
        com_google_android_gms_internal_zzkj.put(com_google_android_gms_internal_zzkj_zza, drawable);
        return drawable;
    }

    protected Drawable zza(Resources resources, Drawable drawable) {
        return zzkh.zza(resources, drawable);
    }

    protected zzkg zza(Drawable drawable, Drawable drawable2) {
        if (drawable == null) {
            drawable = null;
        } else if (drawable instanceof zzkg) {
            drawable = ((zzkg) drawable).zznp();
        }
        return new zzkg(drawable, drawable2);
    }

    void zza(Context context, Bitmap bitmap, boolean z) {
        com.google.android.gms.common.internal.zzb.zzq(bitmap);
        if ((this.zzZk & 1) != 0) {
            bitmap = zzkh.zza(bitmap);
        }
        Drawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
        if (this.zzZg != null) {
            this.zzZg.onImageLoaded(this.zzZc.uri, bitmapDrawable, true);
        }
        zza(bitmapDrawable, z, false, true);
    }

    void zza(Context context, zzkj com_google_android_gms_internal_zzkj) {
        if (this.zzZj) {
            Drawable drawable = null;
            if (this.zzZd != 0) {
                drawable = zza(context, com_google_android_gms_internal_zzkj, this.zzZd);
            }
            zza(drawable, false, true, false);
        }
    }

    void zza(Context context, zzkj com_google_android_gms_internal_zzkj, boolean z) {
        Drawable drawable = null;
        if (this.zzZe != 0) {
            drawable = zza(context, com_google_android_gms_internal_zzkj, this.zzZe);
        }
        if (this.zzZg != null) {
            this.zzZg.onImageLoaded(this.zzZc.uri, drawable, false);
        }
        zza(drawable, z, false, false);
    }

    protected abstract void zza(Drawable drawable, boolean z, boolean z2, boolean z3);

    protected boolean zzb(boolean z, boolean z2) {
        return this.zzZh && !z2 && (!z || this.zzZi);
    }

    public void zzbm(int i) {
        this.zzZe = i;
    }
}
