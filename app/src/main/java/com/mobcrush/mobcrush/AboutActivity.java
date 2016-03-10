package com.mobcrush.mobcrush;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.crashlytics.android.Crashlytics;

public class AboutActivity extends MobcrushActivty {
    public static Intent getIntent(Context context, String url, String title) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.putExtra("android.intent.extra.TEXT", url);
        intent.putExtra("android.intent.extra.TITLE", title);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        try {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(getIntent().getStringExtra("android.intent.extra.TITLE"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        String url = getIntent().getStringExtra("android.intent.extra.TEXT");
        final ProgressDialog dialog = new ProgressDialog(this);
        WebView webView = (WebView) findViewById(R.id.browser);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
