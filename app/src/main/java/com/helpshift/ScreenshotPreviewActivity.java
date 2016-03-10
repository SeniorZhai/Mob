package com.helpshift;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import com.helpshift.app.ActionBarActivity;
import com.helpshift.util.AttachmentUtil;
import com.helpshift.view.ScreenshotPreviewView;
import com.helpshift.view.ScreenshotPreviewView.ScreenshotPreviewInterface;

public class ScreenshotPreviewActivity extends ActionBarActivity implements ScreenshotPreviewInterface {
    public static final String SCREENSHOT = "SCREENSHOT";
    public static final int SCREENSHOT_ADD_TEXT = 1;
    public static final String SCREENSHOT_POSITION = "screenshot_position";
    public static final int SCREENSHOT_PREVIEW_REQUEST_CODE = 32700;
    public static final int SCREENSHOT_REMOVE_TEXT = 2;
    public static final String SCREENSHOT_TEXT_TYPE = "screenshot_text_type";
    private Bundle bundle;
    private ScreenshotPreviewView screenshotPreviewView;
    private boolean selectImage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = getIntent().getExtras();
        if (this.bundle != null) {
            if (Boolean.valueOf(this.bundle.getBoolean("showInFullScreen")).booleanValue()) {
                getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            }
            this.screenshotPreviewView = new ScreenshotPreviewView(this);
            setContentView(this.screenshotPreviewView);
            this.screenshotPreviewView.setScreenshotPreviewInterface(this);
            this.screenshotPreviewView.setScreenshotPreview(this.bundle.getString(SCREENSHOT));
            this.screenshotPreviewView.setSendButtonText(this.bundle.getInt(SCREENSHOT_TEXT_TYPE));
            return;
        }
        finish();
    }

    public void sendScreenshotResult(String screenshot) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(SCREENSHOT, screenshot);
        resultIntent.putExtra(SCREENSHOT_POSITION, this.bundle.getInt(SCREENSHOT_POSITION));
        setResult(-1, resultIntent);
        finish();
    }

    public void selectImage() {
        this.selectImage = true;
        startActivityForResult(new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI), SCREENSHOT_PREVIEW_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (-1 == resultCode && requestCode == SCREENSHOT_PREVIEW_REQUEST_CODE && AttachmentUtil.isImageUri(this, data)) {
            String screenshotPath = AttachmentUtil.getPath((Activity) this, data);
            if (!TextUtils.isEmpty(screenshotPath)) {
                this.screenshotPreviewView.setScreenshotPreview(screenshotPath);
            }
        }
    }

    public void onResume() {
        super.onResume();
        this.selectImage = false;
    }

    public void onStart() {
        super.onStart();
        if (!this.selectImage) {
            HSAnalytics.onActivityStarted(this);
        }
        this.selectImage = false;
    }

    public void onStop() {
        super.onStop();
        if (!this.selectImage) {
            HSAnalytics.onActivityStopped(this);
        }
    }
}
