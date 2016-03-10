package com.helpshift;

import android.os.Build.VERSION;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class DownloadRunnable implements Runnable {
    static final int HTTP_STATE_COMPLETED = 3;
    static final int HTTP_STATE_FAILED = -1;
    static final int HTTP_STATE_PAUSED = 1;
    static final int HTTP_STATE_RESUMED = 2;
    static final int HTTP_STATE_STARTED = 0;
    static final int PROGRESS_CHANGED = 4;
    static final String TAG = "HelpShiftDebug";
    byte[] byteBuffer;
    final DownloadRunnableMethods downloadTask;
    private long downloadedBytes;
    private long totalBytes;
    String userAgent = ("Helpshift-Android/3.10.0/" + VERSION.RELEASE);

    interface DownloadRunnableMethods {
        int getDownloadState();

        URL getDownloadUrl();

        int getFileSize();

        File getFinalFile();

        File getTempFile();

        void handleDownloadState(int i);

        void setDownloadState(int i);

        void setDownloadThread(Thread thread);

        void setDownloadedFilePath(String str);

        void setProgress(double d);
    }

    public void run() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003f in list [B:11:0x0037]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:280)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:167)
*/
        /*
        r18 = this;
        r0 = r18;
        r13 = r0.downloadTask;
        r14 = java.lang.Thread.currentThread();
        r13.setDownloadThread(r14);
        r13 = 10;
        android.os.Process.setThreadPriority(r13);
        r13 = java.lang.Thread.interrupted();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        if (r13 == 0) goto L_0x0040;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
    L_0x0016:
        r13 = new java.lang.InterruptedException;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13.<init>();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        throw r13;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
    L_0x001c:
        r4 = move-exception;
        r0 = r18;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r14 = -1;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13.setDownloadState(r14);	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13 = "HelpShiftDebug";	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r14 = "Exception Interrupted";	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        android.util.Log.d(r13, r14, r4);	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r0 = r18;
        r13 = r0.downloadTask;
        r13 = r13.getDownloadState();
        r14 = -1;
        if (r13 != r14) goto L_0x003f;
    L_0x0037:
        r0 = r18;
        r13 = r0.downloadTask;
        r14 = -1;
        r13.handleDownloadState(r14);
    L_0x003f:
        return;
    L_0x0040:
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r13.getDownloadUrl();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r9 = r13.openConnection();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r9 = (java.net.HttpURLConnection) r9;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = "User-Agent";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r0.userAgent;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r9.setRequestProperty(r13, r14);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r5 = r13.getTempFile();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r5.length();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0.downloadedBytes = r14;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = "Range";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = new java.lang.StringBuilder;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14.<init>();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r15 = "bytes=";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r14.append(r15);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r0.downloadedBytes;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r16 = r0;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r16;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r14.append(r0);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r15 = "-";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r14.append(r15);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r14.toString();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r9.setRequestProperty(r13, r14);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r14.getDownloadState();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13.handleDownloadState(r14);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r13.getDownloadState();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        if (r13 == 0) goto L_0x00b1;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
    L_0x00a6:
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r13.getDownloadState();	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = 2;
        if (r13 != r14) goto L_0x00d4;
    L_0x00b1:
        r2 = 0;
        r7 = 0;
        r13 = java.lang.Thread.interrupted();	 Catch:{ IOException -> 0x00bf }
        if (r13 == 0) goto L_0x00e9;	 Catch:{ IOException -> 0x00bf }
    L_0x00b9:
        r13 = new java.lang.InterruptedException;	 Catch:{ IOException -> 0x00bf }
        r13.<init>();	 Catch:{ IOException -> 0x00bf }
        throw r13;	 Catch:{ IOException -> 0x00bf }
    L_0x00bf:
        r4 = move-exception;
    L_0x00c0:
        r0 = r18;	 Catch:{ all -> 0x00f9 }
        r13 = r0.downloadTask;	 Catch:{ all -> 0x00f9 }
        r14 = -1;	 Catch:{ all -> 0x00f9 }
        r13.setDownloadState(r14);	 Catch:{ all -> 0x00f9 }
        r13 = "HelpShiftDebug";	 Catch:{ all -> 0x00f9 }
        r14 = "Exception IO";	 Catch:{ all -> 0x00f9 }
        android.util.Log.d(r13, r14, r4);	 Catch:{ all -> 0x00f9 }
        if (r2 == 0) goto L_0x00d4;
    L_0x00d1:
        r2.close();	 Catch:{ IOException -> 0x0241, InterruptedException -> 0x001c }
    L_0x00d4:
        r0 = r18;
        r13 = r0.downloadTask;
        r13 = r13.getDownloadState();
        r14 = -1;
        if (r13 != r14) goto L_0x003f;
    L_0x00df:
        r0 = r18;
        r13 = r0.downloadTask;
        r14 = -1;
        r13.handleDownloadState(r14);
        goto L_0x003f;
    L_0x00e9:
        r2 = r9.getInputStream();	 Catch:{ IOException -> 0x00bf }
        r13 = java.lang.Thread.interrupted();	 Catch:{ IOException -> 0x00bf }
        if (r13 == 0) goto L_0x0125;	 Catch:{ IOException -> 0x00bf }
    L_0x00f3:
        r13 = new java.lang.InterruptedException;	 Catch:{ IOException -> 0x00bf }
        r13.<init>();	 Catch:{ IOException -> 0x00bf }
        throw r13;	 Catch:{ IOException -> 0x00bf }
    L_0x00f9:
        r13 = move-exception;
    L_0x00fa:
        if (r2 == 0) goto L_0x00ff;
    L_0x00fc:
        r2.close();	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
    L_0x00ff:
        throw r13;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
    L_0x0100:
        r4 = move-exception;
        r0 = r18;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r14 = -1;	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13.setDownloadState(r14);	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r13 = "HelpShiftDebug";	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r14 = "Exception IO";	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        android.util.Log.d(r13, r14, r4);	 Catch:{ IOException -> 0x0253, InterruptedException -> 0x001c, all -> 0x022c }
        r0 = r18;
        r13 = r0.downloadTask;
        r13 = r13.getDownloadState();
        r14 = -1;
        if (r13 != r14) goto L_0x003f;
    L_0x011b:
        r0 = r18;
        r13 = r0.downloadTask;
        r14 = -1;
        r13.handleDownloadState(r14);
        goto L_0x003f;
    L_0x0125:
        r3 = r9.getContentLength();	 Catch:{ IOException -> 0x00bf }
        r0 = r18;	 Catch:{ IOException -> 0x00bf }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x00bf }
        r13 = r13.getFileSize();	 Catch:{ IOException -> 0x00bf }
        r14 = (long) r13;	 Catch:{ IOException -> 0x00bf }
        r0 = r18;	 Catch:{ IOException -> 0x00bf }
        r0.totalBytes = r14;	 Catch:{ IOException -> 0x00bf }
        r8 = new java.io.FileOutputStream;	 Catch:{ IOException -> 0x00bf }
        r13 = 1;	 Catch:{ IOException -> 0x00bf }
        r8.<init>(r5, r13);	 Catch:{ IOException -> 0x00bf }
        r13 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r13 = new byte[r13];	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0.byteBuffer = r13;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x0144:
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.byteBuffer;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r15 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r12 = r2.read(r13, r14, r15);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = -1;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        if (r12 == r13) goto L_0x01a6;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x0152:
        if (r12 >= 0) goto L_0x015e;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x0154:
        r13 = new java.io.EOFException;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.<init>();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        throw r13;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x015a:
        r4 = move-exception;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r7 = r8;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        goto L_0x00c0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x015e:
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.byteBuffer;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r8.write(r13, r14, r12);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = r5.length();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0.downloadedBytes = r14;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = r0.downloadedBytes;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = (double) r14;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r0.totalBytes;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r16 = r0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r16;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = (double) r0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r16 = r0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = r14 / r16;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r16 = 4666723172467343360; // 0x40c3880000000000 float:0.0 double:10000.0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r10 = r14 * r16;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.setProgress(r10);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 4;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.handleDownloadState(r14);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = java.lang.Thread.interrupted();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        if (r13 == 0) goto L_0x0144;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x019c:
        r13 = new java.lang.InterruptedException;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.<init>();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        throw r13;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01a2:
        r13 = move-exception;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r7 = r8;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        goto L_0x00fa;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01a6:
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r13.getDownloadState();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        if (r13 == 0) goto L_0x01bb;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01b0:
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r13.getDownloadState();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 2;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        if (r13 != r14) goto L_0x0213;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01bb:
        if (r8 == 0) goto L_0x01c0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01bd:
        r8.close();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x01c0:
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r13.getTempFile();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = r14.getFinalFile();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0.copy(r13, r14);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r13.getTempFile();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.delete();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r13.getFinalFile();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r6 = r13.getAbsolutePath();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.setDownloadedFilePath(r6);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = 0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0.byteBuffer = r13;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 0;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.setDownloadThread(r14);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        java.lang.Thread.interrupted();	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 3;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.setDownloadState(r14);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r0 = r18;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13 = r0.downloadTask;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r14 = 3;	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
        r13.handleDownloadState(r14);	 Catch:{ IOException -> 0x015a, all -> 0x01a2 }
    L_0x0213:
        if (r2 == 0) goto L_0x00d4;
    L_0x0215:
        r2.close();	 Catch:{ IOException -> 0x021a, InterruptedException -> 0x001c }
        goto L_0x00d4;
    L_0x021a:
        r4 = move-exception;
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = -1;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13.setDownloadState(r14);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = "HelpShiftDebug";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = "Exception IO";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        android.util.Log.d(r13, r14, r4);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        goto L_0x00d4;
    L_0x022c:
        r13 = move-exception;
        r0 = r18;
        r14 = r0.downloadTask;
        r14 = r14.getDownloadState();
        r15 = -1;
        if (r14 != r15) goto L_0x0240;
    L_0x0238:
        r0 = r18;
        r14 = r0.downloadTask;
        r15 = -1;
        r14.handleDownloadState(r15);
    L_0x0240:
        throw r13;
    L_0x0241:
        r4 = move-exception;
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = -1;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13.setDownloadState(r14);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r13 = "HelpShiftDebug";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = "Exception IO";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        android.util.Log.d(r13, r14, r4);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        goto L_0x00d4;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
    L_0x0253:
        r4 = move-exception;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r0 = r18;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = r0.downloadTask;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r15 = -1;	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14.setDownloadState(r15);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r14 = "HelpShiftDebug";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        r15 = "Exception IO";	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        android.util.Log.d(r14, r15, r4);	 Catch:{ InterruptedException -> 0x001c, IOException -> 0x0100 }
        goto L_0x00ff;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.helpshift.DownloadRunnable.run():void");
    }

    public DownloadRunnable(DownloadRunnableMethods downloadTask) {
        this.downloadTask = downloadTask;
    }

    private void copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);
            byte[] buf = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
            while (true) {
                int len = in.read(buf);
                if (len > 0) {
                    out.write(buf, HTTP_STATE_STARTED, len);
                } else {
                    in.close();
                    out.close();
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Exception File Not Found", e);
        } catch (IOException e2) {
            Log.d(TAG, "Exception IO", e2);
        }
    }
}
