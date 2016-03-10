package com.mixpanel.android.mpmetrics;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

class SharedPreferencesLoader {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    interface OnPrefsLoadedListener {
        void onPrefsLoaded(SharedPreferences sharedPreferences);
    }

    private static class LoadSharedPreferences implements Callable<SharedPreferences> {
        private final Context mContext;
        private final OnPrefsLoadedListener mListener;
        private final String mPrefsName;

        public LoadSharedPreferences(Context context, String prefsName, OnPrefsLoadedListener listener) {
            this.mContext = context;
            this.mPrefsName = prefsName;
            this.mListener = listener;
        }

        public SharedPreferences call() {
            SharedPreferences ret = this.mContext.getSharedPreferences(this.mPrefsName, 0);
            if (this.mListener != null) {
                this.mListener.onPrefsLoaded(ret);
            }
            return ret;
        }
    }

    public Future<SharedPreferences> loadPreferences(Context context, String name, OnPrefsLoadedListener listener) {
        FutureTask<SharedPreferences> task = new FutureTask(new LoadSharedPreferences(context, name, listener));
        this.mExecutor.execute(task);
        return task;
    }
}
