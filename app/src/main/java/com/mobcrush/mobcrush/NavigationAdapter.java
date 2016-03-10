package com.mobcrush.mobcrush;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import io.fabric.sdk.android.BuildConfig;
import java.lang.ref.WeakReference;

public class NavigationAdapter extends ArrayAdapter<UserChannel> {
    private static final int LAYOUT_ID = 2130903170;
    private static final String TYPEFACE = "Roboto-Medium.ttf";
    private WeakReference<FragmentActivity> mActivityRef;
    private DisplayImageOptions mDio;
    private LayoutInflater mLayoutInflater;
    private int mSelectedItem = 0;

    static class ViewHolder {
        ImageView icon;
        TextView title;

        ViewHolder() {
        }
    }

    public NavigationAdapter(FragmentActivity activity) {
        super(activity, R.layout.item_navigation);
        this.mLayoutInflater = LayoutInflater.from(activity);
        this.mActivityRef = new WeakReference(activity);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.cards_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).cacheInMemory(true).build();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(viewHolder);
        }
        UserChannel item = (UserChannel) getItem(position);
        viewHolder = (ViewHolder) convertView.getTag();
        convertView.setBackgroundResource(this.mSelectedItem == position ? R.color.navigation_selected_item_bg : R.drawable.navigation_item_bg);
        if (viewHolder != null) {
            viewHolder.title.setText(item.channel != null ? item.channel.name : BuildConfig.FLAVOR);
            viewHolder.title.setTypeface(UIUtils.getTypeface(getContext(), TYPEFACE));
            viewHolder.title.setTextColor(getContext().getResources().getColor(this.mSelectedItem == position ? R.color.yellow : 17170443));
            if (!(item.channel == null || item.channel._id == null)) {
                viewHolder.icon.setVisibility(0);
                if (item.channel.channelLogo != null) {
                    ImageLoader.getInstance().displayImage(item.channel.channelLogo, viewHolder.icon, this.mDio);
                }
            }
        }
        return convertView;
    }

    public void setSelectedItem(int position) {
        this.mSelectedItem = position;
        notifyDataSetChanged();
    }
}
