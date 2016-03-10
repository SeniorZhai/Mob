package com.mobcrush.mobcrush;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class InAppBrowser extends AppCompatActivity {
    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, InAppBrowser.class);
        intent.putExtra("android.intent.extra.TEXT", url);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_in_app_browser);
        String url = getIntent().getStringExtra("android.intent.extra.TEXT");
        final ProgressDialog dialog = new ProgressDialog(this);
        WebView webView = (WebView) findViewById(R.id.browser);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (Constants.TWITTER_SUCCESS_URL.equals(url)) {
                    InAppBrowser.this.setResult(-1);
                    InAppBrowser.this.finish();
                } else if (Constants.TWITTER_FAILURE_URL.equals(url)) {
                    InAppBrowser.this.setResult(0);
                    InAppBrowser.this.finish();
                }
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (dialog.isShowing()) {
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                    }
                }
            }
        });
        if (url != null) {
            dialog.setMessage(MainApplication.getRString(R.string.loading__, new Object[0]));
            dialog.show();
            webView.loadUrl(url);
            return;
        }
        finish();
    }
}
