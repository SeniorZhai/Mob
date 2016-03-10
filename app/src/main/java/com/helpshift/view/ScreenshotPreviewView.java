package com.helpshift.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.HSApiData;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.util.AttachmentUtil;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class ScreenshotPreviewView extends RelativeLayout implements OnClickListener {
    private String currentScreenshot;
    private int currentText = 0;
    private HSApiData data;
    private ImageView screenshotPreview;
    private ScreenshotPreviewInterface screenshotPreviewInterface;
    private Button send;

    public interface ScreenshotPreviewInterface {
        void selectImage();

        void sendScreenshotResult(String str);
    }

    public ScreenshotPreviewView(Context context) {
        super(context);
        this.data = new HSApiData(context);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, layout.hs__screenshot_preview, this);
        this.screenshotPreview = (ImageView) findViewById(id.screenshotPreview);
        Button change = (Button) findViewById(id.change);
        this.send = (Button) findViewById(id.send);
        if (!this.data.storage.isHelpshiftBrandingDisabled()) {
            ImageView hsFooter = (ImageView) findViewById(id.hs__helpshiftActivityFooter);
            hsFooter.setImageDrawable(HSDraw.getBitmapDrawable(context, (String) HSImages.imagesMap.get("newHSLogo")));
            hsFooter.setBackgroundResource(17170444);
        }
        change.setOnClickListener(this);
        this.send.setOnClickListener(this);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == id.change) {
            this.screenshotPreviewInterface.selectImage();
        } else if (id == id.send) {
            switch (this.currentText) {
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    this.screenshotPreviewInterface.sendScreenshotResult(BuildConfig.FLAVOR);
                    return;
                default:
                    this.screenshotPreviewInterface.sendScreenshotResult(this.currentScreenshot);
                    return;
            }
        }
    }

    public void setScreenshotPreviewInterface(ScreenshotPreviewInterface screenshotPreviewInterface) {
        this.screenshotPreviewInterface = screenshotPreviewInterface;
    }

    public void setScreenshotPreview(String screenshot) {
        this.currentScreenshot = screenshot;
        this.screenshotPreview.setImageBitmap(AttachmentUtil.getBitmap(screenshot, -1));
        if (this.currentText == 2) {
            setSendButtonText(1);
        }
    }

    public void setSendButtonText(int textType) {
        this.currentText = textType;
        switch (textType) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                this.send.setText(getContext().getString(string.hs__screenshot_add));
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                this.send.setText(getContext().getString(string.hs__screenshot_remove));
                return;
            default:
                this.send.setText(getContext().getString(string.hs__send_msg_btn));
                return;
        }
    }
}
