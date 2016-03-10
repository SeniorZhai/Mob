package com.helpshift.customadapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.HSMessagesFragment;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.AttachmentUtil;
import com.helpshift.util.Styles;
import com.helpshift.viewstructs.HSMsg;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.ResponseParser;
import java.io.File;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public final class MessagesAdapter extends ArrayAdapter {
    private static final int TYPE_AR_MOBILE = 12;
    private static final int TYPE_CA_MOBILE = 6;
    private static final int TYPE_CB = 5;
    private static final int TYPE_CR_MOBILE = 7;
    private static final int TYPE_GENERIC_ATTACHMENT_ADMIN = 16;
    private static final int TYPE_IMAGE_ATTACHMENT_ADMIN = 15;
    private static final int TYPE_LOCAL_RSC = 14;
    private static final int TYPE_RAR = 11;
    private static final int TYPE_RSC = 13;
    private static final int TYPE_SC_MOBILE = 8;
    private static final int TYPE_TXT_ADMIN = 1;
    private static final int TYPE_TXT_MOBILE = 2;
    private Context c;
    private boolean enableBtn;
    private HSMessagesFragment f;
    private final LayoutInflater inflater = ((LayoutInflater) this.c.getSystemService("layout_inflater"));
    private List<HSMsg> items;

    private static class ARViewHolder {
        public TextView text1;

        private ARViewHolder() {
        }
    }

    private static class AdminAttachmentGenericViewHolder {
        public ImageButton downloadButton;
        public TextView fileName;
        public TextView fileSize;
        public TextView fileType;
        public ImageButton launchButton;
        public ProgressBar progress;
        public ProgressBar secondaryProgress;

        private AdminAttachmentGenericViewHolder() {
        }
    }

    private static class AdminAttachmentImageViewHolder {
        public ImageButton downloadBtn;
        public ImageView image;
        public ProgressBar progress;

        private AdminAttachmentImageViewHolder() {
        }
    }

    private static class CBViewHolder {
        public LinearLayout btnContainer;
        public ImageButton button1;
        public ImageButton button2;
        public ProgressBar progress;
        public TextView text1;

        private CBViewHolder() {
        }
    }

    private static class CSViewHolder {
        public TextView message;
        public TextView text1;
        public TextView text2;

        private CSViewHolder() {
        }
    }

    private static class LocalRSCViewHolder {
        public ImageButton changeBtn;
        public ImageButton doneBtn;
        public ImageView image;
        public LinearLayout imagePreview;
        public ProgressBar progress;
        public View separatorLine;

        private LocalRSCViewHolder() {
        }
    }

    private static class RARViewHolder {
        public TextView message;
        public ProgressBar progress;
        public ImageButton reviewBtn;
        public View separatorLine;

        private RARViewHolder() {
        }
    }

    private static class RSCViewHolder {
        public LinearLayout adminMessage;
        public ImageButton attachBtn;
        public View buttonSeparator;
        public ImageButton changeBtn;
        public ImageButton doneBtn;
        public ImageView image;
        public LinearLayout imagePreview;
        public ProgressBar progress;
        public View separatorLine;
        public TextView text1;

        private RSCViewHolder() {
        }
    }

    private static class SCViewHolder {
        public ImageView image;
        public ProgressBar progress;
        public TextView text1;

        private SCViewHolder() {
        }
    }

    private static class TxtAdminHolder {
        public TextView text1;
        public TextView text2;

        private TxtAdminHolder() {
        }
    }

    private static class TxtUserHolder {
        public ImageView errorImage;
        public TextView text1;
        public TextView text2;

        private TxtUserHolder() {
        }
    }

    public MessagesAdapter(Fragment f, int textViewResourceId, List<HSMsg> objects) {
        super(f.getActivity(), textViewResourceId, objects);
        this.f = (HSMessagesFragment) f;
        this.c = f.getActivity();
        this.items = objects;
    }

    public void enableButtons(boolean enable) {
        this.enableBtn = enable;
    }

    public boolean isEnabled(int position) {
        return false;
    }

    public int getViewTypeCount() {
        return 20;
    }

    public int getItemViewType(int position) {
        HSMsg item = (HSMsg) this.items.get(position);
        if ((item.type.equals("txt") && (item.state == -1 || item.state == TYPE_TXT_ADMIN)) || ((item.type.equals("txt") && item.state <= -2) || (item.type.equals("txt") && item.origin.equals("mobile")))) {
            return TYPE_TXT_MOBILE;
        }
        if (item.origin.equals("admin") && (item.type.equals("txt") || item.type.equals("rfr"))) {
            return TYPE_TXT_ADMIN;
        }
        if (item.type.equals("cb") && item.origin.equals("admin")) {
            return TYPE_CB;
        }
        if (item.type.equals("rsc") && item.origin.equals("admin")) {
            if (item.id.startsWith(AttachmentUtil.LOCAL_RSC_MSG_ID_PREFIX)) {
                return TYPE_LOCAL_RSC;
            }
            return TYPE_RSC;
        } else if (item.type.equals("ca") && item.origin.equals("mobile")) {
            return TYPE_CA_MOBILE;
        } else {
            if (item.type.equals("ncr") && item.origin.equals("mobile")) {
                return TYPE_CR_MOBILE;
            }
            if (item.type.equals("sc") && item.origin.equals("mobile")) {
                return TYPE_SC_MOBILE;
            }
            if (item.type.equals("rar") && item.origin.equals("admin")) {
                return TYPE_RAR;
            }
            if (item.type.equals("ar") && item.origin.equals("mobile")) {
                return TYPE_AR_MOBILE;
            }
            if (item.type.equals(HSConsts.ADMIN_ATTACHMENT_IMAGE_TYPE)) {
                return TYPE_IMAGE_ATTACHMENT_ADMIN;
            }
            if (item.type.equals(HSConsts.ADMIN_ATTACHMENT_GENERIC_TYPE)) {
                return TYPE_GENERIC_ATTACHMENT_ADMIN;
            }
            return item.type.equals(HSConsts.ADMIN_ATTACHMENT_IMAGE_TYPE) ? TYPE_IMAGE_ATTACHMENT_ADMIN : 0;
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        HSMsg item = (HSMsg) this.items.get(position);
        if (item == null) {
            return null;
        }
        switch (getItemViewType(position)) {
            case TYPE_TXT_ADMIN /*1*/:
                return setConvertView(convertView, item, new TxtAdminHolder());
            case TYPE_TXT_MOBILE /*2*/:
                return setConvertView(convertView, item, new TxtUserHolder());
            case TYPE_CB /*5*/:
                return setConvertView(convertView, item, position, new CBViewHolder());
            case TYPE_CA_MOBILE /*6*/:
                return setConvertView(convertView, item, true, new CSViewHolder());
            case TYPE_CR_MOBILE /*7*/:
                return setConvertView(convertView, item, false, new CSViewHolder());
            case TYPE_SC_MOBILE /*8*/:
                return setConvertView(convertView, item, new SCViewHolder());
            case TYPE_RAR /*11*/:
                return setConvertView(convertView, item, position, new RARViewHolder());
            case TYPE_AR_MOBILE /*12*/:
                return setConvertView(convertView, item, new ARViewHolder());
            case TYPE_RSC /*13*/:
                return setConvertView(convertView, item, position, new RSCViewHolder());
            case TYPE_LOCAL_RSC /*14*/:
                return setConvertView(convertView, item, position, new LocalRSCViewHolder());
            case TYPE_IMAGE_ATTACHMENT_ADMIN /*15*/:
                return setConvertView(convertView, item, position, new AdminAttachmentImageViewHolder());
            case TYPE_GENERIC_ATTACHMENT_ADMIN /*16*/:
                return setConvertView(convertView, item, position, new AdminAttachmentGenericViewHolder());
            default:
                return null;
        }
    }

    private View setConvertView(View convertView, HSMsg item, TxtAdminHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_txt_admin, null);
            Styles.setAdminChatBubbleColor(this.c, convertView.findViewById(id.admin_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.text2 = (TextView) convertView.findViewById(16908309);
            convertView.setTag(holder);
        } else {
            holder = (TxtAdminHolder) convertView.getTag();
        }
        holder.text1.setText(getText(item.body));
        holder.text2.setText(item.date);
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, TxtUserHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_txt_user, null);
            Styles.setUserChatBubbleColor(this.c, convertView.findViewById(id.user_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.text2 = (TextView) convertView.findViewById(16908309);
            holder.errorImage = (ImageView) convertView.findViewById(16908294);
            convertView.setTag(holder);
        } else {
            holder = (TxtUserHolder) convertView.getTag();
        }
        if (item.type.equals("txt") && (item.state == -1 || item.state == TYPE_TXT_ADMIN)) {
            holder.text1.setText(getText(item.body));
            holder.text2.setText(string.hs__sending_msg);
            holder.errorImage.setVisibility(TYPE_SC_MOBILE);
        } else if (!item.type.equals("txt") || item.state > -2) {
            holder.text1.setText(getText(item.body));
            holder.text2.setText(item.date);
            holder.errorImage.setVisibility(TYPE_SC_MOBILE);
        } else {
            holder.text1.setText(getText(item.body));
            holder.text1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MessagesAdapter.this.f.retryMessage(item.id);
                }
            });
            holder.text2.setText(string.hs__sending_fail_msg);
            holder.errorImage.setVisibility(TYPE_SC_MOBILE);
        }
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, final int position, CBViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_confirmation_box, null);
            Styles.setAdminChatBubbleColor(this.c, convertView.findViewById(id.admin_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.btnContainer = (LinearLayout) convertView.findViewById(16908312);
            holder.button1 = (ImageButton) convertView.findViewById(16908313);
            holder.button2 = (ImageButton) convertView.findViewById(16908314);
            Styles.setAcceptButtonIconColor(this.c, holder.button1.getDrawable());
            Styles.setRejectButtonIconColor(this.c, holder.button2.getDrawable());
            convertView.setTag(holder);
        } else {
            holder = (CBViewHolder) convertView.getTag();
        }
        holder.text1.setText(getText(item.body));
        if (item.inProgress.booleanValue()) {
            holder.progress.setVisibility(0);
            holder.btnContainer.setVisibility(TYPE_SC_MOBILE);
        } else if (item.invisible.booleanValue()) {
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.btnContainer.setVisibility(TYPE_SC_MOBILE);
        } else {
            holder.btnContainer.setVisibility(0);
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.button1.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.replyConfirmation(item.id, Boolean.valueOf(true), position);
                    }
                }
            });
            holder.button2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.replyConfirmation(item.id, Boolean.valueOf(false), position);
                    }
                }
            });
            holder.button1.setEnabled(this.enableBtn);
            holder.button2.setEnabled(this.enableBtn);
        }
        return convertView;
    }

    private View setConvertView(View convertView, HSMsg item, boolean accepted, CSViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_confirmation_status, null);
            Styles.setAdminChatBubbleColor(this.c, convertView.findViewById(id.admin_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.text2 = (TextView) convertView.findViewById(16908309);
            convertView.setTag(holder);
        } else {
            holder = (CSViewHolder) convertView.getTag();
        }
        if (accepted) {
            holder.text1.setText(string.hs__ca_msg);
        } else {
            holder.text1.setText(string.hs__cr_msg);
        }
        holder.text2.setText(item.date);
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, final int position, RSCViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_request_screenshot, null);
            Styles.setAdminChatBubbleColor(this.c, convertView.findViewById(id.admin_message).getBackground());
            Styles.setUserChatBubbleColor(this.c, convertView.findViewById(id.user_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.attachBtn = (ImageButton) convertView.findViewById(16908313);
            Styles.setAttachScreenshotButtonIconColor(this.c, holder.attachBtn.getDrawable());
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.imagePreview = (LinearLayout) convertView.findViewById(16908291);
            holder.image = (ImageView) convertView.findViewById(16908304);
            holder.changeBtn = (ImageButton) convertView.findViewById(16908314);
            holder.doneBtn = (ImageButton) convertView.findViewById(16908315);
            Styles.setAcceptButtonIconColor(this.c, holder.doneBtn.getDrawable());
            Styles.setRejectButtonIconColor(this.c, holder.changeBtn.getDrawable());
            holder.separatorLine = convertView.findViewById(16908331);
            holder.adminMessage = (LinearLayout) convertView.findViewById(id.admin_message);
            holder.buttonSeparator = convertView.findViewById(id.button_separator);
            convertView.setTag(holder);
        } else {
            holder = (RSCViewHolder) convertView.getTag();
        }
        holder.text1.setText(getText(item.body));
        if (item.inProgress.booleanValue()) {
            holder.adminMessage.setVisibility(0);
            holder.attachBtn.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
            holder.imagePreview.setVisibility(0);
            holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
            holder.progress.setVisibility(0);
            holder.buttonSeparator.setVisibility(TYPE_SC_MOBILE);
            holder.doneBtn.setVisibility(TYPE_SC_MOBILE);
            holder.changeBtn.setVisibility(TYPE_SC_MOBILE);
        } else if (item.screenshot != null && !TextUtils.isEmpty(item.screenshot)) {
            holder.adminMessage.setVisibility(0);
            holder.attachBtn.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
            holder.imagePreview.setVisibility(0);
            holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.buttonSeparator.setVisibility(0);
            holder.doneBtn.setVisibility(0);
            holder.changeBtn.setVisibility(0);
            holder.changeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.removeScreenshot(position);
                    }
                }
            });
            holder.doneBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.attachImage(position);
                    }
                }
            });
        } else if (item.invisible.booleanValue()) {
            holder.adminMessage.setVisibility(0);
            holder.attachBtn.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
            holder.imagePreview.setVisibility(TYPE_SC_MOBILE);
            holder.image.setImageBitmap(null);
            holder.progress.setVisibility(TYPE_SC_MOBILE);
        } else {
            holder.adminMessage.setVisibility(0);
            holder.attachBtn.setVisibility(0);
            holder.separatorLine.setVisibility(0);
            holder.attachBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.selectImagePopup(position);
                    }
                }
            });
            holder.imagePreview.setVisibility(TYPE_SC_MOBILE);
            holder.image.setImageBitmap(null);
            holder.progress.setVisibility(TYPE_SC_MOBILE);
        }
        holder.changeBtn.setEnabled(this.enableBtn);
        holder.doneBtn.setEnabled(this.enableBtn);
        holder.attachBtn.setEnabled(this.enableBtn);
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, final int position, LocalRSCViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__local_msg_request_screenshot, null);
            Styles.setUserChatBubbleColor(this.c, convertView.findViewById(id.user_message).getBackground());
            holder.imagePreview = (LinearLayout) convertView.findViewById(16908299);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.image = (ImageView) convertView.findViewById(16908304);
            holder.changeBtn = (ImageButton) convertView.findViewById(16908314);
            holder.doneBtn = (ImageButton) convertView.findViewById(16908315);
            holder.separatorLine = convertView.findViewById(16908331);
            Styles.setAcceptButtonIconColor(this.c, holder.doneBtn.getDrawable());
            Styles.setRejectButtonIconColor(this.c, holder.changeBtn.getDrawable());
            convertView.setTag(holder);
        } else {
            holder = (LocalRSCViewHolder) convertView.getTag();
        }
        holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
        if (item.inProgress.booleanValue()) {
            holder.imagePreview.setVisibility(0);
            holder.progress.setVisibility(0);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
            holder.changeBtn.setVisibility(TYPE_SC_MOBILE);
            holder.doneBtn.setVisibility(TYPE_SC_MOBILE);
        } else if (!TextUtils.isEmpty(item.screenshot)) {
            holder.imagePreview.setVisibility(0);
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(0);
            holder.changeBtn.setVisibility(0);
            holder.doneBtn.setVisibility(0);
            holder.changeBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.removeScreenshot(position);
                    }
                }
            });
            holder.doneBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.attachImage(position);
                    }
                }
            });
        } else if (item.invisible.booleanValue()) {
            holder.imagePreview.setVisibility(TYPE_SC_MOBILE);
        }
        holder.changeBtn.setEnabled(this.enableBtn);
        holder.doneBtn.setEnabled(this.enableBtn);
        return convertView;
    }

    private View setConvertView(View convertView, HSMsg item, SCViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_screenshot_status, null);
            Styles.setUserChatBubbleColor(this.c, convertView.findViewById(id.user_message).getBackground());
            holder.text1 = (TextView) convertView.findViewById(16908308);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.image = (ImageView) convertView.findViewById(16908304);
            convertView.setTag(holder);
        } else {
            holder = (SCViewHolder) convertView.getTag();
        }
        holder.text1.setText(string.hs__screenshot_sent_msg);
        if (TextUtils.isEmpty(item.screenshot)) {
            holder.progress.setVisibility(0);
            holder.image.setVisibility(TYPE_SC_MOBILE);
            holder.image.setImageBitmap(null);
        } else {
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.image.setVisibility(0);
            holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, -1));
        }
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, final int position, RARViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_review_request, null);
            Styles.setAdminChatBubbleColor(this.c, convertView.findViewById(id.admin_message).getBackground());
            holder.message = (TextView) convertView.findViewById(16908308);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.reviewBtn = (ImageButton) convertView.findViewById(16908313);
            Styles.setReviewButtonIconColor(this.c, holder.reviewBtn.getDrawable());
            holder.separatorLine = convertView.findViewById(16908331);
            convertView.setTag(holder);
        } else {
            holder = (RARViewHolder) convertView.getTag();
        }
        holder.message.setText(string.hs__review_request_message);
        if (item.inProgress.booleanValue()) {
            holder.progress.setVisibility(0);
            holder.reviewBtn.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
        } else if (item.invisible.booleanValue()) {
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.reviewBtn.setVisibility(TYPE_SC_MOBILE);
            holder.separatorLine.setVisibility(TYPE_SC_MOBILE);
        } else {
            holder.progress.setVisibility(TYPE_SC_MOBILE);
            holder.reviewBtn.setVisibility(0);
            holder.separatorLine.setVisibility(0);
            holder.reviewBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.clickable.booleanValue()) {
                        MessagesAdapter.this.f.replyReview(item.id, position);
                    }
                }
            });
        }
        return convertView;
    }

    private View setConvertView(View convertView, HSMsg item, ARViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_review_accepted, null);
            holder.text1 = (TextView) convertView.findViewById(16908308);
            convertView.setTag(holder);
        } else {
            holder = (ARViewHolder) convertView.getTag();
        }
        holder.text1.setText(string.hs__review_accepted_message);
        return convertView;
    }

    private View setConvertView(View convertView, HSMsg item, int position, AdminAttachmentGenericViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_attachment_generic, null);
            holder.fileName = (TextView) convertView.findViewById(16908308);
            holder.fileType = (TextView) convertView.findViewById(16908309);
            holder.fileSize = (TextView) convertView.findViewById(16908304);
            holder.downloadButton = (ImageButton) convertView.findViewById(16908313);
            Styles.setDownloadAttachmentButtonIconColor(this.c, holder.downloadButton.getDrawable());
            holder.secondaryProgress = (ProgressBar) convertView.findViewById(16908303);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.launchButton = (ImageButton) convertView.findViewById(16908314);
            Styles.setLaunchAttachmentButtonIconColor(this.c, holder.launchButton.getDrawable());
            convertView.setTag(holder);
        } else {
            holder = (AdminAttachmentGenericViewHolder) convertView.getTag();
        }
        try {
            String fileSize;
            final JSONObject attachmentObj = new JSONObject(item.body);
            String fileName = attachmentObj.getString("file-name");
            String fileType = AttachmentUtil.getFileType(this.f.getActivity(), attachmentObj.getString("content-type"), fileName);
            int size = attachmentObj.getInt("size");
            if (size < AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) {
                fileSize = size + " B";
            } else if (size < AccessibilityNodeInfoCompat.ACTION_DISMISS) {
                fileSize = (size / AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) + " KB";
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                Object[] objArr = new Object[TYPE_TXT_ADMIN];
                objArr[0] = Float.valueOf(((float) size) / 1048576.0f);
                fileSize = stringBuilder.append(String.format("%.1f", objArr)).append(" MB").toString();
            }
            holder.fileName.setText(fileName);
            holder.fileType.setText(fileType);
            holder.fileSize.setText(fileSize);
            switch (item.state) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    holder.downloadButton.setVisibility(0);
                    holder.secondaryProgress.setVisibility(TYPE_SC_MOBILE);
                    holder.progress.setVisibility(TYPE_SC_MOBILE);
                    holder.launchButton.setVisibility(TYPE_SC_MOBILE);
                    break;
                case TYPE_TXT_ADMIN /*1*/:
                    holder.downloadButton.setVisibility(TYPE_SC_MOBILE);
                    holder.secondaryProgress.setVisibility(0);
                    holder.progress.setVisibility(TYPE_SC_MOBILE);
                    holder.launchButton.setVisibility(TYPE_SC_MOBILE);
                    break;
                case TYPE_TXT_MOBILE /*2*/:
                    holder.downloadButton.setVisibility(TYPE_SC_MOBILE);
                    holder.secondaryProgress.setVisibility(TYPE_SC_MOBILE);
                    holder.progress.setVisibility(0);
                    holder.launchButton.setVisibility(TYPE_SC_MOBILE);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    holder.downloadButton.setVisibility(TYPE_SC_MOBILE);
                    holder.secondaryProgress.setVisibility(TYPE_SC_MOBILE);
                    holder.progress.setVisibility(TYPE_SC_MOBILE);
                    holder.launchButton.setVisibility(0);
                    break;
            }
            final int i = position;
            holder.downloadButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MessagesAdapter.this.f.downloadAdminAttachment(attachmentObj, i, MessagesAdapter.TYPE_CA_MOBILE);
                }
            });
            final HSMsg hSMsg = item;
            holder.launchButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MessagesAdapter.this.f.launchAttachment(hSMsg);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private View setConvertView(View convertView, final HSMsg item, final int position, AdminAttachmentImageViewHolder holder) {
        if (convertView == null) {
            convertView = this.inflater.inflate(layout.hs__msg_attachment_image, null);
            holder.image = (ImageView) convertView.findViewById(16908304);
            holder.progress = (ProgressBar) convertView.findViewById(16908301);
            holder.downloadBtn = (ImageButton) convertView.findViewById(16908313);
            Styles.setDownloadAttachmentButtonIconColor(this.c, holder.downloadBtn.getDrawable());
            convertView.setTag(holder);
        } else {
            holder = (AdminAttachmentImageViewHolder) convertView.getTag();
        }
        try {
            final JSONObject attachmentObject = new JSONObject(item.body);
            File imageFile = new File(item.screenshot);
            switch (item.state) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    holder.image.setVisibility(TYPE_SC_MOBILE);
                    holder.downloadBtn.setVisibility(TYPE_SC_MOBILE);
                    holder.progress.setVisibility(0);
                    this.f.downloadAdminAttachment(attachmentObject, position, TYPE_SC_MOBILE);
                    break;
                case TYPE_TXT_ADMIN /*1*/:
                    holder.progress.setVisibility(TYPE_SC_MOBILE);
                    if (imageFile.exists()) {
                        holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
                        holder.image.setVisibility(0);
                    }
                    holder.downloadBtn.setVisibility(0);
                    break;
                case TYPE_TXT_MOBILE /*2*/:
                    holder.downloadBtn.setVisibility(TYPE_SC_MOBILE);
                    if (imageFile.exists()) {
                        holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
                        holder.image.setVisibility(0);
                    }
                    holder.progress.setVisibility(0);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    holder.downloadBtn.setVisibility(TYPE_SC_MOBILE);
                    holder.progress.setVisibility(TYPE_SC_MOBILE);
                    if (imageFile.exists()) {
                        holder.image.setImageBitmap(AttachmentUtil.getBitmap(item.screenshot, Callback.DEFAULT_SWIPE_ANIMATION_DURATION));
                        holder.image.setVisibility(0);
                        break;
                    }
                    break;
            }
            holder.downloadBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MessagesAdapter.this.f.downloadAdminAttachment(attachmentObject, position, MessagesAdapter.TYPE_CR_MOBILE);
                }
            });
            holder.image.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (item.state == 3) {
                        MessagesAdapter.this.f.launchAttachment(item);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private String getText(String input) {
        return Html.fromHtml(input.replace("\n", "<br/>")).toString();
    }
}
