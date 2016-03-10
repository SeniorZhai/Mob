package com.google.android.gms.common.images;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.widget.ImageView;
import com.google.android.gms.internal.zzkj;
import com.google.android.gms.internal.zzku;
import com.google.android.gms.internal.zzlk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageManager {
    private static final Object zzYN = new Object();
    private static HashSet<Uri> zzYO = new HashSet();
    private static ImageManager zzYP;
    private static ImageManager zzYQ;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService zzYR = Executors.newFixedThreadPool(4);
    private final zzb zzYS;
    private final zzkj zzYT;
    private final Map<zza, ImageReceiver> zzYU;
    private final Map<Uri, ImageReceiver> zzYV;
    private final Map<Uri, Long> zzYW;

    private final class ImageReceiver extends ResultReceiver {
        private final Uri mUri;
        private final ArrayList<zza> zzYX = new ArrayList();
        final /* synthetic */ ImageManager zzYY;

        ImageReceiver(ImageManager imageManager, Uri uri) {
            this.zzYY = imageManager;
            super(new Handler(Looper.getMainLooper()));
            this.mUri = uri;
        }

        public void onReceiveResult(int resultCode, Bundle resultData) {
            this.zzYY.zzYR.execute(new zzc(this.zzYY, this.mUri, (ParcelFileDescriptor) resultData.getParcelable("com.google.android.gms.extra.fileDescriptor")));
        }

        public void zzb(zza com_google_android_gms_common_images_zza) {
            com.google.android.gms.common.internal.zzb.zzbY("ImageReceiver.addImageRequest() must be called in the main thread");
            this.zzYX.add(com_google_android_gms_common_images_zza);
        }

        public void zzc(zza com_google_android_gms_common_images_zza) {
            com.google.android.gms.common.internal.zzb.zzbY("ImageReceiver.removeImageRequest() must be called in the main thread");
            this.zzYX.remove(com_google_android_gms_common_images_zza);
        }

        public void zzno() {
            Intent intent = new Intent("com.google.android.gms.common.images.LOAD_IMAGE");
            intent.putExtra("com.google.android.gms.extras.uri", this.mUri);
            intent.putExtra("com.google.android.gms.extras.resultReceiver", this);
            intent.putExtra("com.google.android.gms.extras.priority", 3);
            this.zzYY.mContext.sendBroadcast(intent);
        }
    }

    public interface OnImageLoadedListener {
        void onImageLoaded(Uri uri, Drawable drawable, boolean z);
    }

    private static final class zza {
        static int zza(ActivityManager activityManager) {
            return activityManager.getLargeMemoryClass();
        }
    }

    private static final class zzb extends zzku<zza, Bitmap> {
        public zzb(Context context) {
            super(zzag(context));
        }

        private static int zzag(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            int memoryClass = (((context.getApplicationInfo().flags & AccessibilityNodeInfoCompat.ACTION_DISMISS) != 0 ? 1 : null) == null || !zzlk.zzoR()) ? activityManager.getMemoryClass() : zza.zza(activityManager);
            return (int) (((float) (memoryClass * AccessibilityNodeInfoCompat.ACTION_DISMISS)) * 0.33f);
        }

        protected /* synthetic */ void entryRemoved(boolean x0, Object x1, Object x2, Object x3) {
            zza(x0, (zza) x1, (Bitmap) x2, (Bitmap) x3);
        }

        protected /* synthetic */ int sizeOf(Object x0, Object x1) {
            return zza((zza) x0, (Bitmap) x1);
        }

        protected int zza(zza com_google_android_gms_common_images_zza_zza, Bitmap bitmap) {
            return bitmap.getHeight() * bitmap.getRowBytes();
        }

        protected void zza(boolean z, zza com_google_android_gms_common_images_zza_zza, Bitmap bitmap, Bitmap bitmap2) {
            super.entryRemoved(z, com_google_android_gms_common_images_zza_zza, bitmap, bitmap2);
        }
    }

    private final class zzc implements Runnable {
        private final Uri mUri;
        final /* synthetic */ ImageManager zzYY;
        private final ParcelFileDescriptor zzYZ;

        public zzc(ImageManager imageManager, Uri uri, ParcelFileDescriptor parcelFileDescriptor) {
            this.zzYY = imageManager;
            this.mUri = uri;
            this.zzYZ = parcelFileDescriptor;
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzbZ("LoadBitmapFromDiskRunnable can't be executed in the main thread");
            boolean z = false;
            Bitmap bitmap = null;
            if (this.zzYZ != null) {
                try {
                    bitmap = BitmapFactory.decodeFileDescriptor(this.zzYZ.getFileDescriptor());
                } catch (Throwable e) {
                    Log.e("ImageManager", "OOM while loading bitmap for uri: " + this.mUri, e);
                    z = true;
                }
                try {
                    this.zzYZ.close();
                } catch (Throwable e2) {
                    Log.e("ImageManager", "closed failed", e2);
                }
            }
            CountDownLatch countDownLatch = new CountDownLatch(1);
            this.zzYY.mHandler.post(new zzf(this.zzYY, this.mUri, bitmap, z, countDownLatch));
            try {
                countDownLatch.await();
            } catch (InterruptedException e3) {
                Log.w("ImageManager", "Latch interrupted while posting " + this.mUri);
            }
        }
    }

    private final class zzd implements Runnable {
        final /* synthetic */ ImageManager zzYY;
        private final zza zzZa;

        public zzd(ImageManager imageManager, zza com_google_android_gms_common_images_zza) {
            this.zzYY = imageManager;
            this.zzZa = com_google_android_gms_common_images_zza;
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzbY("LoadImageRunnable must be executed on the main thread");
            ImageReceiver imageReceiver = (ImageReceiver) this.zzYY.zzYU.get(this.zzZa);
            if (imageReceiver != null) {
                this.zzYY.zzYU.remove(this.zzZa);
                imageReceiver.zzc(this.zzZa);
            }
            zza com_google_android_gms_common_images_zza_zza = this.zzZa.zzZc;
            if (com_google_android_gms_common_images_zza_zza.uri == null) {
                this.zzZa.zza(this.zzYY.mContext, this.zzYY.zzYT, true);
                return;
            }
            Bitmap zza = this.zzYY.zza(com_google_android_gms_common_images_zza_zza);
            if (zza != null) {
                this.zzZa.zza(this.zzYY.mContext, zza, true);
                return;
            }
            Long l = (Long) this.zzYY.zzYW.get(com_google_android_gms_common_images_zza_zza.uri);
            if (l != null) {
                if (SystemClock.elapsedRealtime() - l.longValue() < 3600000) {
                    this.zzZa.zza(this.zzYY.mContext, this.zzYY.zzYT, true);
                    return;
                }
                this.zzYY.zzYW.remove(com_google_android_gms_common_images_zza_zza.uri);
            }
            this.zzZa.zza(this.zzYY.mContext, this.zzYY.zzYT);
            imageReceiver = (ImageReceiver) this.zzYY.zzYV.get(com_google_android_gms_common_images_zza_zza.uri);
            if (imageReceiver == null) {
                imageReceiver = new ImageReceiver(this.zzYY, com_google_android_gms_common_images_zza_zza.uri);
                this.zzYY.zzYV.put(com_google_android_gms_common_images_zza_zza.uri, imageReceiver);
            }
            imageReceiver.zzb(this.zzZa);
            if (!(this.zzZa instanceof com.google.android.gms.common.images.zza.zzc)) {
                this.zzYY.zzYU.put(this.zzZa, imageReceiver);
            }
            synchronized (ImageManager.zzYN) {
                if (!ImageManager.zzYO.contains(com_google_android_gms_common_images_zza_zza.uri)) {
                    ImageManager.zzYO.add(com_google_android_gms_common_images_zza_zza.uri);
                    imageReceiver.zzno();
                }
            }
        }
    }

    private static final class zze implements ComponentCallbacks2 {
        private final zzb zzYS;

        public zze(zzb com_google_android_gms_common_images_ImageManager_zzb) {
            this.zzYS = com_google_android_gms_common_images_ImageManager_zzb;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void onLowMemory() {
            this.zzYS.evictAll();
        }

        public void onTrimMemory(int level) {
            if (level >= 60) {
                this.zzYS.evictAll();
            } else if (level >= 20) {
                this.zzYS.trimToSize(this.zzYS.size() / 2);
            }
        }
    }

    private final class zzf implements Runnable {
        private final Bitmap mBitmap;
        private final Uri mUri;
        final /* synthetic */ ImageManager zzYY;
        private boolean zzZb;
        private final CountDownLatch zzoD;

        public zzf(ImageManager imageManager, Uri uri, Bitmap bitmap, boolean z, CountDownLatch countDownLatch) {
            this.zzYY = imageManager;
            this.mUri = uri;
            this.mBitmap = bitmap;
            this.zzZb = z;
            this.zzoD = countDownLatch;
        }

        private void zza(ImageReceiver imageReceiver, boolean z) {
            ArrayList zza = imageReceiver.zzYX;
            int size = zza.size();
            for (int i = 0; i < size; i++) {
                zza com_google_android_gms_common_images_zza = (zza) zza.get(i);
                if (z) {
                    com_google_android_gms_common_images_zza.zza(this.zzYY.mContext, this.mBitmap, false);
                } else {
                    this.zzYY.zzYW.put(this.mUri, Long.valueOf(SystemClock.elapsedRealtime()));
                    com_google_android_gms_common_images_zza.zza(this.zzYY.mContext, this.zzYY.zzYT, false);
                }
                if (!(com_google_android_gms_common_images_zza instanceof com.google.android.gms.common.images.zza.zzc)) {
                    this.zzYY.zzYU.remove(com_google_android_gms_common_images_zza);
                }
            }
        }

        public void run() {
            com.google.android.gms.common.internal.zzb.zzbY("OnBitmapLoadedRunnable must be executed in the main thread");
            boolean z = this.mBitmap != null;
            if (this.zzYY.zzYS != null) {
                if (this.zzZb) {
                    this.zzYY.zzYS.evictAll();
                    System.gc();
                    this.zzZb = false;
                    this.zzYY.mHandler.post(this);
                    return;
                } else if (z) {
                    this.zzYY.zzYS.put(new zza(this.mUri), this.mBitmap);
                }
            }
            ImageReceiver imageReceiver = (ImageReceiver) this.zzYY.zzYV.remove(this.mUri);
            if (imageReceiver != null) {
                zza(imageReceiver, z);
            }
            this.zzoD.countDown();
            synchronized (ImageManager.zzYN) {
                ImageManager.zzYO.remove(this.mUri);
            }
        }
    }

    private ImageManager(Context context, boolean withMemoryCache) {
        this.mContext = context.getApplicationContext();
        if (withMemoryCache) {
            this.zzYS = new zzb(this.mContext);
            if (zzlk.zzoU()) {
                zznl();
            }
        } else {
            this.zzYS = null;
        }
        this.zzYT = new zzkj();
        this.zzYU = new HashMap();
        this.zzYV = new HashMap();
        this.zzYW = new HashMap();
    }

    public static ImageManager create(Context context) {
        return zzb(context, false);
    }

    private Bitmap zza(zza com_google_android_gms_common_images_zza_zza) {
        return this.zzYS == null ? null : (Bitmap) this.zzYS.get(com_google_android_gms_common_images_zza_zza);
    }

    public static ImageManager zzb(Context context, boolean z) {
        if (z) {
            if (zzYQ == null) {
                zzYQ = new ImageManager(context, true);
            }
            return zzYQ;
        }
        if (zzYP == null) {
            zzYP = new ImageManager(context, false);
        }
        return zzYP;
    }

    private void zznl() {
        this.mContext.registerComponentCallbacks(new zze(this.zzYS));
    }

    public void loadImage(ImageView imageView, int resId) {
        zza(new com.google.android.gms.common.images.zza.zzb(imageView, resId));
    }

    public void loadImage(ImageView imageView, Uri uri) {
        zza(new com.google.android.gms.common.images.zza.zzb(imageView, uri));
    }

    public void loadImage(ImageView imageView, Uri uri, int defaultResId) {
        zza com_google_android_gms_common_images_zza_zzb = new com.google.android.gms.common.images.zza.zzb(imageView, uri);
        com_google_android_gms_common_images_zza_zzb.zzbm(defaultResId);
        zza(com_google_android_gms_common_images_zza_zzb);
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri) {
        zza(new com.google.android.gms.common.images.zza.zzc(listener, uri));
    }

    public void loadImage(OnImageLoadedListener listener, Uri uri, int defaultResId) {
        zza com_google_android_gms_common_images_zza_zzc = new com.google.android.gms.common.images.zza.zzc(listener, uri);
        com_google_android_gms_common_images_zza_zzc.zzbm(defaultResId);
        zza(com_google_android_gms_common_images_zza_zzc);
    }

    public void zza(zza com_google_android_gms_common_images_zza) {
        com.google.android.gms.common.internal.zzb.zzbY("ImageManager.loadImage() must be called in the main thread");
        new zzd(this, com_google_android_gms_common_images_zza).run();
    }
}
