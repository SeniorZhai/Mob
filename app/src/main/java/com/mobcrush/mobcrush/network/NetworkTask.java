package com.mobcrush.mobcrush.network;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.LoginActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.broadcast.BroadcastService;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.logic.NetworkLogic;
import io.fabric.sdk.android.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.apache.http.HttpStatus;

public class NetworkTask implements ErrorListener {
    private static final String TAG = "Network.NetworkTask";
    private static final ArrayList<NetworkTask> TASK_LIST = new ArrayList();
    private static AlertDialog mDialog;
    private WeakReference<FragmentActivity> mActivityRef;
    private String mAddress;
    private ErrorListener mErrorListener;
    private Listener<String> mListener;
    private int mMaxRetryCount;
    private int mMethod;
    private boolean mNeedAuthorization;
    private Map<String, String> mParams;
    private Listener<?> mResultListener;
    private Object mTag;
    private int mTryNo = 1;

    public NetworkTask(FragmentActivity activity, int method, boolean needAuthorization, String address, Map<String, String> params, int maxRetryCount, Listener<String> listener, Listener<?> resultListener, ErrorListener errorListener) {
        this.mActivityRef = new WeakReference(activity);
        this.mMethod = method;
        this.mNeedAuthorization = needAuthorization;
        this.mAddress = address;
        this.mMaxRetryCount = maxRetryCount;
        this.mListener = listener;
        this.mResultListener = resultListener;
        this.mErrorListener = errorListener;
        this.mParams = params;
    }

    public Object getTag() {
        return this.mTag;
    }

    public NetworkTask setTag(Object tag) {
        this.mTag = tag;
        return this;
    }

    public void onErrorResponse(final VolleyError error) {
        Log.e(TAG, BuildConfig.FLAVOR + error + "; Message:" + NetworkRequest.safeParseNetworkResponseData(error.networkResponse));
        if (isNeedAuthorization() && error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case HttpStatus.SC_UNAUTHORIZED /*401*/:
                    if (Network.isLoggedIn()) {
                        Network.refreshToken((FragmentActivity) this.mActivityRef.get(), new Listener<Boolean>() {
                            public void onResponse(Boolean response) {
                                if (response == null || !response.booleanValue()) {
                                    Network.logout(false);
                                    NetworkTask.this.getActivity().stopService(new Intent(NetworkTask.this.getActivity(), BroadcastService.class));
                                    NetworkLogic.onLogout();
                                    if (NetworkTask.this.mResultListener != null) {
                                        NetworkTask.this.mResultListener.onResponse(null);
                                        return;
                                    }
                                    return;
                                }
                                Network.executeAuthRequest(NetworkTask.this);
                            }
                        }, new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null && error.networkResponse.statusCode == HttpStatus.SC_FORBIDDEN) {
                                    NetworkLogic.onAuthRequired();
                                    FragmentActivity fragmentActivity = NetworkTask.this.mActivityRef != null ? (FragmentActivity) NetworkTask.this.mActivityRef.get() : null;
                                    if (fragmentActivity != null && !LoginActivity.mIsAlreadyShowing) {
                                        LoginActivity.mIsAlreadyShowing = true;
                                        fragmentActivity.startActivity(LoginActivity.getIntent(fragmentActivity));
                                    }
                                }
                            }
                        });
                        return;
                    } else if (getErrorListener() != null) {
                        getErrorListener().onErrorResponse(error);
                        return;
                    } else if (getListener() != null) {
                        getListener().onResponse(null);
                        return;
                    } else {
                        return;
                    }
            }
        }
        if (error.networkResponse == null || error.networkResponse.statusCode != HttpStatus.SC_FORBIDDEN) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                if (!Utils.isInternetAvailable(activity) || this.mTryNo >= this.mMaxRetryCount) {
                    if (mDialog != null && mDialog.isShowing()) {
                        try {
                            mDialog.dismiss();
                        } catch (Exception e) {
                        }
                    }
                    synchronized (TASK_LIST) {
                        TASK_LIST.add(this);
                    }
                    final boolean internetAvailable = Utils.isInternetAvailable(MainApplication.getContext());
                    if (!activity.isFinishing()) {
                        int i;
                        Builder builder = new Builder(activity);
                        if (internetAvailable) {
                            i = R.string.error_network_undeterminated;
                        } else {
                            i = R.string.error_network_no_internet;
                        }
                        builder = builder.setMessage(i);
                        if (internetAvailable) {
                            i = 17039370;
                        } else {
                            i = R.string.retry;
                        }
                        Builder builder2 = builder.setPositiveButton(i, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (internetAvailable) {
                                    NetworkTask.this.cancelTask(NetworkTask.this, error);
                                    if (error != null) {
                                        Crashlytics.log(error.getMessage());
                                    }
                                } else {
                                    NetworkTask.this.restartNetworkTasks();
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        if (!internetAvailable) {
                            builder2.setNegativeButton(R.string.settings, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        NetworkTask.this.getActivity().startActivity(new Intent("android.settings.SETTINGS"));
                                        NetworkTask.this.restartNetworkTasks();
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            });
                            builder2.setCancelable(false);
                        }
                        mDialog = builder2.create();
                        try {
                            mDialog.show();
                            return;
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            Crashlytics.logException(e2);
                            return;
                        }
                    }
                    return;
                }
                Log.i(TAG, "retry " + getAddress() + ". #" + this.mTryNo);
                this.mTryNo++;
                Network.executeAuthRequest(this);
            }
            if (this.mErrorListener != null) {
                this.mErrorListener.onErrorResponse(error);
                return;
            } else if (this.mResultListener != null) {
                this.mResultListener.onResponse(null);
                return;
            } else {
                return;
            }
        }
        NetworkLogic.onAuthRequired();
        if (this.mErrorListener != null) {
            this.mErrorListener.onErrorResponse(error);
        } else if (this.mResultListener != null) {
            this.mResultListener.onResponse(null);
        }
    }

    private void cancelTasks(VolleyError error) {
        if (!TASK_LIST.isEmpty()) {
            Iterator i$ = TASK_LIST.iterator();
            while (i$.hasNext()) {
                cancelTask((NetworkTask) i$.next(), error);
            }
        }
        TASK_LIST.clear();
        Network.cancelAll();
    }

    private void cancelTask(NetworkTask task, VolleyError error) {
        if (task == null) {
            return;
        }
        if (task.getErrorListener() != null) {
            task.getErrorListener().onErrorResponse(error);
        } else if (task.getListener() != null) {
            task.getListener().onResponse(null);
        }
    }

    private void restartNetworkTasks() {
        synchronized (TASK_LIST) {
            if (!TASK_LIST.isEmpty()) {
                Iterator<NetworkTask> it = TASK_LIST.iterator();
                while (it.hasNext()) {
                    Network.executeAuthRequest((NetworkTask) it.next());
                    it.remove();
                }
            }
        }
    }

    public FragmentActivity getActivity() {
        return (FragmentActivity) this.mActivityRef.get();
    }

    public String getAddress() {
        return this.mAddress;
    }

    public int getMethod() {
        return this.mMethod;
    }

    public Map<String, String> getParams() {
        return this.mParams;
    }

    public Listener<String> getListener() {
        return this.mListener;
    }

    public Listener<?> getResultListener() {
        return this.mResultListener;
    }

    public ErrorListener getErrorListener() {
        return this.mErrorListener;
    }

    public boolean isNeedAuthorization() {
        return this.mNeedAuthorization;
    }

    public int getMaxRetryCount() {
        return this.mMaxRetryCount;
    }
}
