package com.mobcrush.mobcrush.logic;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.LoginActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.network.Network;

public class NetworkLogic {
    public static synchronized void onSignedUp() {
        synchronized (NetworkLogic.class) {
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(new Intent(Constants.EVENT_SIGNUP));
        }
    }

    public static synchronized void onLogin() {
        synchronized (NetworkLogic.class) {
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(new Intent(Constants.EVENT_LOGIN));
        }
    }

    public static synchronized void onLogout() {
        synchronized (NetworkLogic.class) {
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(new Intent(Constants.EVENT_LOGOUT));
        }
    }

    public static synchronized void onAuthRequired() {
        synchronized (NetworkLogic.class) {
            LocalBroadcastManager.getInstance(MainApplication.getContext()).sendBroadcast(new Intent(Constants.EVENT_AUTH_REQUIRED));
        }
    }

    public static synchronized boolean isSessionExpired() {
        boolean z;
        synchronized (NetworkLogic.class) {
            z = PreferenceUtility.getExpirationDate() < System.currentTimeMillis();
        }
        return z;
    }

    public static synchronized void renewTokenIfRequired(final FragmentActivity activity) {
        synchronized (NetworkLogic.class) {
            if (Network.isLoggedIn() && isSessionExpired()) {
                Network.refreshToken(activity, new Listener<Boolean>() {
                    public void onResponse(Boolean response) {
                        if (response == null || !response.booleanValue()) {
                            activity.startActivity(LoginActivity.getIntent(activity));
                        }
                    }
                }, new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        activity.startActivity(LoginActivity.getIntent(activity));
                    }
                });
            }
        }
    }
}
