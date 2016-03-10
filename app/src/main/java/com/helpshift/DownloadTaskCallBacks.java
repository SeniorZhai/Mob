package com.helpshift;

interface DownloadTaskCallBacks {
    void onDownloadTaskComplete(String str, int i, String str2, String str3, int i2);

    void onDownloadTaskFailed(int i, String str, String str2, int i2);

    void onDownloadTaskPaused(int i, String str, String str2, int i2);

    void onDownloadTaskResumed(int i, String str, String str2, int i2);

    void onProgressChanged(double d, int i, String str, String str2, int i2);
}
