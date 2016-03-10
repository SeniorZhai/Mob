package com.helpshift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.File;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadManager {
    private static final int CORE_POOL_SIZE = 5;
    static final int DOWNLOAD_COMPLETE = 4;
    static final int DOWNLOAD_FAILED = -1;
    static final int DOWNLOAD_PAUSED = 2;
    static final int DOWNLOAD_RESUMED = 3;
    static final int DOWNLOAD_STARTED = 1;
    public static final int GENERIC = 6;
    public static final int IMAGE = 7;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final int MAXIMUM_POOL_SIZE = 5;
    static final int PROGRESS_CHANGED = 5;
    public static final String TAG = "HelpShiftDebug";
    public static final int THUMBNAIL = 8;
    private static DownloadTaskCallBacks downloadTaskCallBacks;
    private final BlockingQueue<Runnable> downloadRunnableQueue = new LinkedBlockingQueue();
    private final BlockingQueue<DownloadTask> downloadTasksQueue = new LinkedBlockingQueue();
    private final ThreadPoolExecutor downloadThreadPool = new ThreadPoolExecutor(PROGRESS_CHANGED, PROGRESS_CHANGED, 1, KEEP_ALIVE_TIME_UNIT, this.downloadRunnableQueue);
    private Handler handler;
    private HSApiData hsApiData;
    private HSStorage hsStorage;

    private static class Holder {
        private static final DownloadManager INSTANCE = new DownloadManager(HelpshiftContext.getApplicationContext());

        private Holder() {
        }
    }

    public DownloadManager(Context context) {
        this.hsApiData = new HSApiData(context);
        this.hsStorage = this.hsApiData.storage;
        this.handler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                DownloadTask task = msg.obj;
                switch (msg.what) {
                    case DownloadManager.DOWNLOAD_FAILED /*-1*/:
                        if (DownloadManager.downloadTaskCallBacks != null) {
                            DownloadManager.downloadTaskCallBacks.onDownloadTaskFailed(msg.arg1, task.getMsgId(), task.getIssueId(), task.getDownloadType());
                        }
                        DownloadManager.this.recycleTask(task);
                        return;
                    case DownloadManager.KEEP_ALIVE_TIME /*1*/:
                        return;
                    case DownloadManager.DOWNLOAD_PAUSED /*2*/:
                        if (DownloadManager.downloadTaskCallBacks != null) {
                            DownloadManager.downloadTaskCallBacks.onDownloadTaskPaused(msg.arg1, task.getMsgId(), task.getIssueId(), task.getDownloadType());
                            return;
                        }
                        return;
                    case DownloadManager.DOWNLOAD_RESUMED /*3*/:
                        if (DownloadManager.downloadTaskCallBacks != null) {
                            DownloadManager.downloadTaskCallBacks.onDownloadTaskResumed(msg.arg1, task.getMsgId(), task.getIssueId(), task.getDownloadType());
                            return;
                        }
                        return;
                    case DownloadManager.DOWNLOAD_COMPLETE /*4*/:
                        DownloadManager.this.removeFromCache(task.getMsgId());
                        if (DownloadManager.downloadTaskCallBacks != null) {
                            DownloadManager.downloadTaskCallBacks.onDownloadTaskComplete(task.getDownloadedFilePath(), msg.arg1, task.getMsgId(), task.getIssueId(), task.getDownloadType());
                        }
                        DownloadManager.this.recycleTask(task);
                        return;
                    case DownloadManager.PROGRESS_CHANGED /*5*/:
                        if (DownloadManager.downloadTaskCallBacks != null) {
                            DownloadManager.downloadTaskCallBacks.onProgressChanged(task.getProgress(), msg.arg1, task.getMsgId(), task.getIssueId(), task.getDownloadType());
                            return;
                        }
                        return;
                    default:
                        super.handleMessage(msg);
                        return;
                }
            }
        };
    }

    public static synchronized DownloadManager getInstance() {
        DownloadManager access$200;
        synchronized (DownloadManager.class) {
            access$200 = Holder.INSTANCE;
        }
        return access$200;
    }

    public void handleState(DownloadTask downloadTask, int state, int position) {
        switch (state) {
            case DOWNLOAD_PAUSED /*2*/:
                this.handler.obtainMessage(state, position, 0, downloadTask).sendToTarget();
                return;
            case DOWNLOAD_RESUMED /*3*/:
                this.handler.obtainMessage(state, position, 0, downloadTask).sendToTarget();
                return;
            case DOWNLOAD_COMPLETE /*4*/:
                this.handler.obtainMessage(state, position, 0, downloadTask).sendToTarget();
                return;
            case PROGRESS_CHANGED /*5*/:
                this.handler.obtainMessage(state, position, 0, downloadTask).sendToTarget();
                return;
            default:
                this.handler.obtainMessage(state, position, 0, downloadTask).sendToTarget();
                return;
        }
    }

    public static DownloadTask startDownload(JSONObject attachment, int position, String msgId, String issueId, int downloadType) {
        getInstance().hsStorage.addToActiveDownloads(msgId, issueId);
        DownloadTask downloadTask = (DownloadTask) getInstance().downloadTasksQueue.poll();
        if (downloadTask == null) {
            downloadTask = new DownloadTask();
        }
        downloadTask.initializeDownloaderTask(getInstance(), attachment, position, msgId, issueId, downloadType);
        try {
            JSONObject cachedFiles = getInstance().getCachedAttachmentFiles();
            if (cachedFiles.has(msgId)) {
                downloadTask.setTempFile(new File(cachedFiles.getString(msgId)));
            } else {
                getInstance().addToCache(msgId, downloadTask.getTempFile().getAbsolutePath());
            }
        } catch (JSONException e) {
            Log.d(TAG, "Exception JSON", e);
        }
        if (downloadTask.getDownloadState() == 0 || downloadTask.getDownloadState() == DOWNLOAD_PAUSED) {
            getInstance().downloadThreadPool.execute(downloadTask.getDownloadRunnable());
        } else {
            getInstance().handleState(downloadTask, downloadTask.getDownloadState(), position);
        }
        return downloadTask;
    }

    public static void removeDownload(DownloadTask downloadTask, URL url, int position) {
        if (downloadTask != null && downloadTask.getDownloadUrl().equals(url)) {
            synchronized (getInstance()) {
                Thread thread = downloadTask.getCurrentThread();
                if (thread != null) {
                    thread.interrupt();
                }
            }
            downloadTask.getTempFile().delete();
            getInstance().removeFromCache(downloadTask.getMsgId());
            getInstance().downloadThreadPool.remove(downloadTask.getDownloadRunnable());
        }
    }

    public static void pauseDownload(DownloadTask downloadTask, URL url, int position) {
        if (downloadTask != null && downloadTask.getDownloadUrl().equals(url)) {
            synchronized (getInstance()) {
                Thread thread = downloadTask.getCurrentThread();
                if (thread != null) {
                    downloadTask.setDownloadState(KEEP_ALIVE_TIME);
                    thread.interrupt();
                }
                getInstance().downloadThreadPool.remove(downloadTask.getDownloadRunnable());
                getInstance().handleState(downloadTask, DOWNLOAD_PAUSED, position);
            }
        }
    }

    public static void resumeDownload(DownloadTask downloadTask, URL url, int position) {
        if (downloadTask != null && downloadTask.getDownloadUrl().equals(url)) {
            synchronized (getInstance()) {
                downloadTask.setDownloadState(DOWNLOAD_PAUSED);
                getInstance().downloadThreadPool.execute(downloadTask.getDownloadRunnable());
                getInstance().handleState(downloadTask, DOWNLOAD_RESUMED, position);
            }
        }
    }

    void recycleTask(DownloadTask downloadTask) {
        downloadTask.recycle();
        this.downloadTasksQueue.offer(downloadTask);
    }

    private JSONObject getCachedAttachmentFiles() {
        return this.hsStorage.getCachedAttachmentFiles();
    }

    private void removeFromCache(String msgId) {
        this.hsStorage.removeFromCachedAttachmentFiles(msgId);
    }

    private void addToCache(String msgId, String path) {
        this.hsStorage.addToCachedAttachmentFiles(msgId, path);
    }

    public static void registerDownloadTaskCallbacks(DownloadTaskCallBacks callBacks) {
        downloadTaskCallBacks = callBacks;
    }

    public static void deregisterDownloadTaskCallBacks() {
        downloadTaskCallBacks = null;
    }

    public static DownloadTaskCallBacks getDownloadTaskCallBacks() {
        return downloadTaskCallBacks;
    }
}
