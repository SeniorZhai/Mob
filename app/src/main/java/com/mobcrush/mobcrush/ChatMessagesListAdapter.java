package com.mobcrush.mobcrush;

import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.logic.RoleType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ChatMessagesListAdapter extends ArrayAdapter<ChatMessage> implements OnClickListener {
    private static final int LAYOUT_ID = 2130903169;
    private WeakReference<FragmentActivity> mActivityRef;
    private DisplayImageOptions mDio;
    private Callback mGetDataCallback;
    private ChatMessage mLastMessageWhenDataWasAsked;
    private LayoutInflater mLayoutInflater;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$RoleType = new int[RoleType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.broadcaster.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.moderator.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.admin.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView message;
        TextView username;

        ViewHolder() {
        }
    }

    public ChatMessagesListAdapter(FragmentActivity activity) {
        super(activity, R.layout.item_message);
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mActivityRef = new WeakReference(activity);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri((int) R.drawable.default_profile_pic).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build();
    }

    public void setGetDataCallback(Callback callback) {
        this.mLastMessageWhenDataWasAsked = null;
        this.mGetDataCallback = callback;
    }

    public String[] getItemsAsStrings() {
        ArrayList<String> items = new ArrayList();
        for (int i = 0; i < getCount(); i++) {
            items.add(((ChatMessage) getItem(i)).toString());
        }
        return (String[]) items.toArray(new String[items.size()]);
    }

    public void add(String message) {
        add(new Gson().fromJson(message, ChatMessage.class));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = this.mLayoutInflater.inflate(R.layout.item_message, parent, false);
            holder = new ViewHolder();
            holder.username = (TextView) convertView.findViewById(R.id.user_name_text);
            holder.username.setOnClickListener(this);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.icon = (ImageView) convertView.findViewById(R.id.message_icon);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        if (holder != null) {
            ChatMessage message = (ChatMessage) getItem(position);
            if (!(position != 1 || this.mLastMessageWhenDataWasAsked == message || this.mGetDataCallback == null)) {
                this.mLastMessageWhenDataWasAsked = message;
                this.mGetDataCallback.handleMessage(Message.obtain());
            }
            int colorId = R.color.user;
            if (message.role != null) {
                switch (AnonymousClass1.$SwitchMap$com$mobcrush$mobcrush$logic$RoleType[message.role.ordinal()]) {
                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        colorId = R.color.broadcaster;
                        break;
                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                        colorId = R.color.moderator;
                        break;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        colorId = R.color.admin;
                        break;
                    default:
                        colorId = R.color.user;
                        break;
                }
            }
            holder.username.setTextColor(MainApplication.getContext().getResources().getColor(colorId));
            holder.username.setText(TextUtils.isEmpty(message.username) ? MainApplication.getRString(R.string.guest, new Object[0]) : message.username);
            holder.username.setTag(message.userId);
            holder.message.setText(message.message);
            holder.icon.setImageResource(R.drawable.default_profile_pic);
            if (!TextUtils.isEmpty(message.profileLogoSmall)) {
                ImageLoader.getInstance().displayImage(message.profileLogoSmall, holder.icon, this.mDio);
            }
        }
        return convertView;
    }

    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null) {
            User user = new User();
            user._id = (String) tag;
            user.username = (String) ((TextView) view).getText();
            FragmentActivity a = (FragmentActivity) this.mActivityRef.get();
            if (a != null) {
                a.startActivity(ProfileActivity.getIntent(a, user));
            }
        }
    }
}
