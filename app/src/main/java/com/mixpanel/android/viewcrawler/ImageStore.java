package com.mixpanel.android.viewcrawler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.util.HttpService;
import com.mixpanel.android.util.RemoteService;
import com.mixpanel.android.util.RemoteService.ServiceUnavailableException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ImageStore {
    private static final String DEFAULT_DIRECTORY_NAME = "MixpanelAPI.Images";
    private static final String FILE_PREFIX = "MP_IMG_";
    private static final String LOGTAG = "MixpanelAPI.ImageStore";
    private static final int MAX_BITMAP_SIZE = 10000000;
    private final MPConfig mConfig;
    private final MessageDigest mDigest;
    private final File mDirectory;
    private final RemoteService mPoster;

    public static class CantGetImageException extends Exception {
        public CantGetImageException(String message) {
            super(message);
        }

        public CantGetImageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public ImageStore(Context context) {
        this(context, DEFAULT_DIRECTORY_NAME, new HttpService());
    }

    public ImageStore(Context context, String directoryName, RemoteService poster) {
        MessageDigest useDigest;
        this.mDirectory = context.getDir(directoryName, 0);
        this.mPoster = poster;
        this.mConfig = MPConfig.getInstance(context);
        try {
            useDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            Log.w(LOGTAG, "Images won't be stored because this platform doesn't supply a SHA1 hash function");
            useDigest = null;
        }
        this.mDigest = useDigest;
    }

    public Bitmap getImage(String url) throws CantGetImageException {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        Bitmap bitmap;
        File file = storedFile(url);
        byte[] bytes = null;
        if (file == null || !file.exists()) {
            try {
                bytes = this.mPoster.performRequest(url, null, this.mConfig.getSSLSocketFactory());
            } catch (IOException e3) {
                throw new CantGetImageException("Can't download bitmap", e3);
            } catch (ServiceUnavailableException e4) {
                throw new CantGetImageException("Couldn't download image due to service availability", e4);
            }
        }
        if (bytes != null) {
            if (file != null && bytes.length < MAX_BITMAP_SIZE) {
                OutputStream outputStream = null;
                try {
                    OutputStream out = new FileOutputStream(file);
                    try {
                        out.write(bytes);
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e32) {
                                Log.w(LOGTAG, "Problem closing output file", e32);
                            }
                        }
                    } catch (FileNotFoundException e5) {
                        e2 = e5;
                        outputStream = out;
                        try {
                            throw new CantGetImageException("It appears that ImageStore is misconfigured, or disk storage is unavailable- can't write to bitmap directory", e2);
                        } catch (Throwable th2) {
                            th = th2;
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (IOException e322) {
                                    Log.w(LOGTAG, "Problem closing output file", e322);
                                }
                            }
                            throw th;
                        }
                    } catch (IOException e6) {
                        e322 = e6;
                        outputStream = out;
                        throw new CantGetImageException("Can't store bitmap", e322);
                    } catch (Throwable th3) {
                        th = th3;
                        outputStream = out;
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e7) {
                    e2 = e7;
                    throw new CantGetImageException("It appears that ImageStore is misconfigured, or disk storage is unavailable- can't write to bitmap directory", e2);
                } catch (IOException e8) {
                    e322 = e8;
                    throw new CantGetImageException("Can't store bitmap", e322);
                }
            }
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                throw new CantGetImageException("Downloaded data could not be interpreted as a bitmap");
            }
        }
        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bitmap == null) {
            boolean ignored = file.delete();
            throw new CantGetImageException("Bitmap on disk can't be opened or was corrupt");
        }
        return bitmap;
    }

    public void clearStorage() {
        for (File file : this.mDirectory.listFiles()) {
            if (file.getName().startsWith(FILE_PREFIX)) {
                file.delete();
            }
        }
    }

    public void deleteStorage(String url) {
        File file = storedFile(url);
        if (file != null) {
            file.delete();
        }
    }

    private File storedFile(String url) {
        if (this.mDigest == null) {
            return null;
        }
        return new File(this.mDirectory, FILE_PREFIX + Base64.encodeToString(this.mDigest.digest(url.getBytes()), 10));
    }
}
