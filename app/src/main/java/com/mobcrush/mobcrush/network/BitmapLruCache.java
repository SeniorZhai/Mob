package com.mobcrush.mobcrush.network;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import com.android.volley.Cache.Entry;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class BitmapLruCache implements ImageCache {
    private DiskBasedCache mDiskCache;

    public BitmapLruCache(File rootDirectory) {
        this.mDiskCache = new DiskBasedCache(rootDirectory);
        new Thread(new Runnable() {
            public void run() {
                BitmapLruCache.this.mDiskCache.initialize();
            }
        }).start();
    }

    public Bitmap getBitmap(String url) {
        Entry entry = this.mDiskCache.get(url);
        if (entry == null) {
            return null;
        }
        byte[] data = entry.data;
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    public void putBitmap(String url, Bitmap bitmap) {
        Entry entry = new Entry();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        entry.data = stream.toByteArray();
        this.mDiskCache.put(url, entry);
    }
}
