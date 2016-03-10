package com.helpshift.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.HSFunnel;
import com.helpshift.HSQuestionFragment;
import com.helpshift.app.ActionBarActivity;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.events.EventsFilesManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSHTML5WebView extends WebView {
    public static final String TAG = "HelpShiftDebug";
    private ActionBarActivity mActivity;
    private FrameLayout mBrowserFrameLayout;
    private FrameLayout mContentView;
    private Context mContext;
    private View mCustomView;
    private CustomViewCallback mCustomViewCallback;
    private FrameLayout mCustomViewContainer;
    private HSQuestionFragment mFragment;
    private HSWebChromeClient mWebChromeClient;
    private String unhandledUrl;
    private boolean unhandledUrlSchemeNotFound;

    private class HSWebChromeClient extends WebChromeClient {
        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;

        private HSWebChromeClient() {
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            HSHTML5WebView.this.setVisibility(8);
            if (HSHTML5WebView.this.mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            HSHTML5WebView.this.mCustomViewContainer.addView(view);
            HSHTML5WebView.this.mCustomView = view;
            HSHTML5WebView.this.mCustomViewCallback = callback;
            HSHTML5WebView.this.mCustomViewContainer.setVisibility(0);
        }

        public void onHideCustomView() {
            if (HSHTML5WebView.this.mCustomView != null) {
                HSHTML5WebView.this.mCustomView.setVisibility(8);
                HSHTML5WebView.this.mCustomViewContainer.removeView(HSHTML5WebView.this.mCustomView);
                HSHTML5WebView.this.mCustomView = null;
                HSHTML5WebView.this.mCustomViewContainer.setVisibility(8);
                HSHTML5WebView.this.mCustomViewCallback.onCustomViewHidden();
                HSHTML5WebView.this.setVisibility(0);
            }
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return true;
        }

        public Bitmap getDefaultVideoPoster() {
            if (this.mDefaultVideoPoster == null) {
            }
            return this.mDefaultVideoPoster;
        }

        public View getVideoLoadingProgressView() {
            if (this.mVideoProgressView == null) {
                this.mVideoProgressView = LayoutInflater.from(HSHTML5WebView.this.mContext).inflate(layout.hs__video_loading_progress, null);
            }
            return this.mVideoProgressView;
        }

        public void onReceivedTitle(WebView view, String title) {
            ((ActionBarActivity) HSHTML5WebView.this.mContext).setTitle(title);
        }

        public void onProgressChanged(WebView view, int newProgress) {
            ((ActionBarActivity) HSHTML5WebView.this.mContext).getWindow().setFeatureInt(2, newProgress * 100);
        }

        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        private MyWebViewClient() {
        }

        public void onPageFinished(WebView view, String url) {
            HSHTML5WebView.this.mActivity.setSupportProgressBarIndeterminateVisibility(false);
            HSHTML5WebView.this.mFragment.showMenuOptions();
            HSHTML5WebView.this.mFragment.highlightSearchTerms();
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl.equals(HSHTML5WebView.this.unhandledUrl) && errorCode == -10) {
                HSHTML5WebView.this.unhandledUrlSchemeNotFound = true;
                HSHTML5WebView.this.mFragment.hideQuestionFooter();
            }
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!TextUtils.isEmpty(url)) {
                Intent intent = new Intent("android.intent.action.VIEW");
                Uri linkUri = Uri.parse(url.trim());
                intent.setData(linkUri);
                if (intent.resolveActivity(HSHTML5WebView.this.mActivity.getPackageManager()) != null) {
                    try {
                        JSONObject eventObj = new JSONObject();
                        eventObj.put(HSFunnel.CONVERSATION_POSTED, linkUri.getScheme());
                        eventObj.put(HSFunnel.MARKED_UNHELPFUL, url.trim());
                        HSFunnel.pushEvent(HSFunnel.LINK_VIA_FAQ, eventObj);
                    } catch (JSONException e) {
                        Log.d(HSHTML5WebView.TAG, "JSONException : ", e);
                    }
                    HSHTML5WebView.this.mActivity.startActivity(intent);
                    return true;
                }
                HSHTML5WebView.this.unhandledUrl = url;
            }
            return false;
        }

        @TargetApi(11)
        public WebResourceResponse shouldInterceptRequest(WebView view, String inputUrl) {
            URL url = null;
            File storagePath = HSHTML5WebView.this.mActivity.getExternalCacheDir();
            try {
                url = new URL(inputUrl);
            } catch (MalformedURLException e) {
                Log.d(HSHTML5WebView.TAG, "MalformedURLException", e);
            }
            if (url != null) {
                File saveFile = new File(storagePath, inputUrl.replace("/", EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR));
                if (saveFile.exists()) {
                    try {
                        return new WebResourceResponse(BuildConfig.FLAVOR, BuildConfig.FLAVOR, new FileInputStream(saveFile));
                    } catch (FileNotFoundException e2) {
                        Log.d(HSHTML5WebView.TAG, "FileNotFoundException", e2);
                    }
                } else if (HSHTML5WebView.this.isImage(url)) {
                    HSHTML5WebView.this.saveFile(url, saveFile);
                }
            }
            return super.shouldInterceptRequest(view, inputUrl);
        }
    }

    private void init(Context context, HSQuestionFragment fragment) {
        this.mFragment = fragment;
        this.mContext = context;
        this.mActivity = (ActionBarActivity) this.mContext;
        this.mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(this.mActivity).inflate(layout.hs__webview_custom_content, null);
        this.mContentView = (FrameLayout) this.mBrowserFrameLayout.findViewById(id.hs__webview_main_content);
        this.mCustomViewContainer = (FrameLayout) this.mActivity.findViewById(id.hs__customViewContainer);
        this.mWebChromeClient = new HSWebChromeClient();
        setWebChromeClient(this.mWebChromeClient);
        setWebViewClient(new MyWebViewClient());
        WebSettings s = getSettings();
        s.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
        s.setLoadWithOverviewMode(true);
        s.setSavePassword(true);
        s.setSaveFormData(true);
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setJavaScriptEnabled(true);
        s.setAllowFileAccess(true);
        s.setAppCacheEnabled(true);
        s.setDomStorageEnabled(true);
        s.setPluginState(PluginState.OFF);
        s.setAllowFileAccess(true);
        this.mContentView.addView(this);
    }

    public HSHTML5WebView(Context context, HSQuestionFragment fragment) {
        super(context);
        init(context, fragment);
    }

    public FrameLayout getLayout() {
        return this.mBrowserFrameLayout;
    }

    public boolean inCustomView() {
        return this.mCustomView != null;
    }

    public void hideCustomView() {
        this.mWebChromeClient.onHideCustomView();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4 || this.mCustomView != null || !canGoBack()) {
            return super.onKeyDown(keyCode, event);
        }
        if (this.unhandledUrlSchemeNotFound) {
            this.mFragment.getActivity().finish();
        } else {
            goBack();
        }
        return true;
    }

    private boolean isImage(URL url) {
        boolean z = false;
        try {
            z = new HashSet(Arrays.asList(new String[]{"image/jpeg", "image/png", "image/gif", "image/x-png", "image/x-citrix-pjpeg", "image/x-citrix-gif", "image/pjpeg"})).contains(url.openConnection().getContentType());
        } catch (Exception e) {
            Log.d(TAG, "openConnection() Exception :", e);
        }
        return z;
    }

    private void saveFile(URL url, File saveFile) {
        InputStream input;
        OutputStream output;
        try {
            input = url.openStream();
            output = new FileOutputStream(saveFile);
            byte[] buffer = new byte[HttpStatus.SC_INTERNAL_SERVER_ERROR];
            while (true) {
                int bytesRead = input.read(buffer, 0, buffer.length);
                if (bytesRead >= 0) {
                    output.write(buffer, 0, bytesRead);
                } else {
                    output.close();
                    input.close();
                    return;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "saveFile Exception :", e);
        } catch (Throwable th) {
            output.close();
            input.close();
        }
    }
}
