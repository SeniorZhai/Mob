package com.mobcrush.mobcrush;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.DataModel;
import com.mobcrush.mobcrush.helper.ShareHelper;
import com.mobcrush.mobcrush.mixpanel.Source;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import io.fabric.sdk.android.BuildConfig;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class BroadcastAdapter extends Adapter<ViewHolder> implements OnClickListener, OnMenuItemClickListener, Listener<Boolean>, ErrorListener {
    private static final String EXTRA_POSITION = "POSITION";
    private static final String TAG = "BroadcastAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private WeakReference<FragmentActivity> mActivityRef;
    private int mCurrentHeight;
    private ArrayList<Broadcast> mDataset;
    private int mDefaultImageHeight;
    private DisplayImageOptions mDioCashed;
    private DisplayImageOptions mDioLive;
    private Handler mHandler = new Handler();
    private View mHeaderView;
    private int mMinimalHeight;
    private Callback mOnEventsCallback;
    private Callback mOnNeedNextDataCallback;
    private ProgressDialog mProgressDialog;
    private boolean mShowOptionMenu;
    private boolean mShowUserNames;
    private Source mSource;
    private boolean mWithFooter;
    private boolean mWithHeader;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private static final String CUSTOM_FONT_NAME = "Klavika-Light.ttf";
        public TextView mBroadcastNameView;
        public CardView mCardView;
        public TextView mGameNameView;
        public ImageView mImageView;
        public TextView mLikesView;
        public View mLiveView;
        public View mOverflowBtn;
        public View mProgressView;
        public View mShareBtn;
        public View mUserGameDelimiter;
        public TextView mUserNameView;
        public TextView mViewersCountView;

        public ViewHolder(CardView v, OnClickListener listener) {
            super(v);
            this.mCardView = v;
            this.mImageView = (ImageView) v.findViewById(R.id.image);
            this.mLiveView = v.findViewById(R.id.live_text);
            Typeface tf = UIUtils.getTypeface(v.getContext(), CUSTOM_FONT_NAME);
            this.mGameNameView = (TextView) v.findViewById(R.id.game_name_text);
            this.mGameNameView.setTypeface(tf);
            this.mUserNameView = (TextView) v.findViewById(R.id.user_name_text);
            this.mUserNameView.setTypeface(tf);
            this.mBroadcastNameView = (TextView) v.findViewById(R.id.broadcast_name_text);
            this.mBroadcastNameView.setTypeface(tf);
            this.mViewersCountView = (TextView) v.findViewById(R.id.viewers_text);
            this.mViewersCountView.setTypeface(tf);
            this.mLikesView = (TextView) v.findViewById(R.id.likes_text);
            this.mLikesView.setTypeface(tf);
            this.mProgressView = v.findViewById(R.id.progressBar);
            this.mUserGameDelimiter = v.findViewById(R.id.user_game_delimiter);
            this.mShareBtn = v.findViewById(R.id.share_btn);
            this.mOverflowBtn = v.findViewById(R.id.overflow_btn);
            v.setOnClickListener(listener);
        }
    }

    static /* synthetic */ int access$012(BroadcastAdapter x0, int x1) {
        int i = x0.mCurrentHeight + x1;
        x0.mCurrentHeight = i;
        return i;
    }

    static /* synthetic */ int access$020(BroadcastAdapter x0, int x1) {
        int i = x0.mCurrentHeight - x1;
        x0.mCurrentHeight = i;
        return i;
    }

    public BroadcastAdapter(FragmentActivity activity, Broadcast[] broadcasts, boolean showUserNames, Source source) {
        setHasStableIds(true);
        this.mShowOptionMenu = true;
        this.mShowUserNames = true;
        this.mActivityRef = new WeakReference(activity);
        this.mDataset = new ArrayList();
        this.mDefaultImageHeight = activity.getResources().getDimensionPixelSize(R.dimen.card_watch_height);
        this.mDioLive = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.cards_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).showImageOnFail((int) R.drawable.ic_error_grey600_36dp).cacheOnDisk(false).cacheInMemory(true).build();
        this.mDioCashed = new Builder().displayer(new RoundedBitmapDisplayer(activity.getResources().getDimensionPixelSize(R.dimen.cards_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).showImageOnFail((int) R.drawable.ic_error_grey600_36dp).cacheOnDisk(true).cacheInMemory(true).build();
        this.mSource = source;
        addBroadcasts(broadcasts, true);
    }

    public long getItemId(int position) {
        if (this.mDataset == null || this.mDataset.size() <= position) {
            return super.getItemId(position);
        }
        return ((Broadcast) this.mDataset.get(position))._id != null ? (long) ((Broadcast) this.mDataset.get(position))._id.hashCode() : (long) position;
    }

    public void disableOptionMenu() {
        this.mShowOptionMenu = false;
    }

    public void setOnEventsListener(Callback callback) {
        this.mOnEventsCallback = callback;
    }

    public void setOnNeedNextDataCallback(Callback callback) {
        this.mOnNeedNextDataCallback = callback;
    }

    public void addBroadcasts(Broadcast[] broadcasts, boolean onlyUnique) {
        int prevSize = this.mDataset.size();
        if (onlyUnique) {
            ArrayList<Broadcast> list = new ArrayList();
            Broadcast[] arr$ = broadcasts;
            int len$ = arr$.length;
            for (int i$ = TYPE_HEADER; i$ < len$; i$ += TYPE_ITEM) {
                Broadcast b = arr$[i$];
                if (isUniqueBroadcast(b)) {
                    list.add(b);
                }
            }
            if (list.size() > 0) {
                this.mDataset.addAll(list);
            }
            if (prevSize == 0) {
                safeNotifyDataSetChanged();
                return;
            }
            try {
                notifyItemRangeInserted(prevSize, list.size());
                return;
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                safeNotifyDataSetChanged();
                return;
            }
        }
        this.mDataset.addAll(Arrays.asList(broadcasts));
        if (prevSize == 0) {
            safeNotifyDataSetChanged();
            return;
        }
        try {
            Crashlytics.log("BroadcastAdapter.addBroadcasts.prevSize " + prevSize + "; lenght: " + broadcasts.length);
            notifyItemRangeInserted(prevSize, broadcasts.length);
        } catch (Throwable e2) {
            e2.printStackTrace();
            Crashlytics.logException(e2);
            safeNotifyDataSetChanged();
        }
    }

    public boolean isUniqueBroadcast(Broadcast broadcast) {
        if (this.mDataset.size() == 0) {
            return true;
        }
        Iterator i$ = this.mDataset.iterator();
        while (i$.hasNext()) {
            Broadcast b = (Broadcast) i$.next();
            if (b != null && broadcast != null && TextUtils.equals(b._id, broadcast._id)) {
                return false;
            }
        }
        return true;
    }

    public void clearBroadcasts(boolean notify) {
        if (this.mDataset != null) {
            this.mDataset.clear();
            if (notify) {
                safeNotifyDataSetChanged();
            }
        }
    }

    public int getItemViewType(int position) {
        if ((!this.mWithHeader || position != 0) && (!this.mWithFooter || position < this.mDataset.size())) {
            return TYPE_ITEM;
        }
        return TYPE_HEADER;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_broadcast, parent, false);
        ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressBar);
        Drawable d = pb.getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, v.getResources().getColor(R.color.gray)));
        pb.setIndeterminateDrawable(d);
        v.setUseCompatPadding(false);
        return new ViewHolder(v, this);
    }

    public void onBindViewHolder(final ViewHolder holder, int position) {
        int i = 4;
        int i2 = TYPE_HEADER;
        LayoutParams lp = holder.mCardView.getLayoutParams();
        if (this.mWithHeader && position == 0) {
            holder.mCardView.setClickable(false);
            lp.height = this.mHeaderView.getMinimumHeight();
            holder.mCardView.setLayoutParams(lp);
            holder.mCardView.setVisibility(4);
        } else if (!this.mWithFooter || position <= this.mDataset.size()) {
            if (this.mWithHeader) {
                position--;
            }
            Broadcast broadcast = (Broadcast) this.mDataset.get(position);
            if (!TextUtils.isEmpty(broadcast._id)) {
                lp.height = -2;
                holder.mCardView.setClickable(true);
                holder.mCardView.setLayoutParams(lp);
                holder.mCardView.setVisibility(TYPE_HEADER);
                holder.mCardView.setTag(Integer.valueOf(position));
                View view = holder.mLiveView;
                if (broadcast.isLive) {
                    i = TYPE_HEADER;
                }
                view.setVisibility(i);
                if (this.mShowUserNames) {
                    holder.mUserNameView.setVisibility(TYPE_HEADER);
                    holder.mUserNameView.setText(broadcast.user != null ? broadcast.user.username : BuildConfig.FLAVOR);
                    holder.mGameNameView.setText(broadcast.game != null ? broadcast.game.name : BuildConfig.FLAVOR);
                    view = holder.mUserGameDelimiter;
                    i = (broadcast.user == null || TextUtils.isEmpty(broadcast.user.username)) ? 8 : TYPE_HEADER;
                    view.setVisibility(i);
                } else {
                    holder.mGameNameView.setText(broadcast.game != null ? broadcast.game.name : BuildConfig.FLAVOR);
                }
                LayoutParams layoutParams = holder.mBroadcastNameView.getLayoutParams();
                if (TextUtils.isEmpty(broadcast.title)) {
                    layoutParams.height = TYPE_HEADER;
                } else {
                    holder.mBroadcastNameView.setText(broadcast.title);
                    layoutParams.height = -2;
                }
                holder.mBroadcastNameView.setLayoutParams(layoutParams);
                holder.mLikesView.setText(String.valueOf(broadcast.likes));
                holder.mLikesView.setCompoundDrawablesWithIntrinsicBounds(holder.mLikesView.getResources().getDrawable(broadcast.currentLiked ? R.drawable.ic_like_feed_active : R.drawable.ic_like_feed), null, null, null);
                holder.mViewersCountView.setText(broadcast.getViewsNumber() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + MainApplication.getRString(R.string.views, new Object[TYPE_HEADER]));
                holder.mCardView.forceLayout();
                layoutParams = holder.mImageView.getLayoutParams();
                if (!(broadcast.width == 0 || broadcast.height == 0)) {
                    layoutParams.height = (int) ((((double) holder.mImageView.getWidth()) * ((double) broadcast.height)) / ((double) broadcast.width));
                }
                if (layoutParams.height == 0) {
                    layoutParams.height = this.mDefaultImageHeight;
                }
                holder.mImageView.setLayoutParams(layoutParams);
                final int viewHeight = holder.mCardView.getMeasuredHeight();
                this.mCurrentHeight += viewHeight;
                if (PreferenceUtility.getConfig() == null || PreferenceUtility.getConfig().snapshotBaseUrl == null) {
                    holder.mImageView.setImageBitmap(null);
                } else {
                    String imageUrl;
                    DisplayImageOptions displayImageOptions;
                    if (!broadcast.hasCustomThumbnail || PreferenceUtility.getConfig().videoThumbnailUrl == null) {
                        imageUrl = PreferenceUtility.getConfig().videoSnapshotUrl.replace(Constants.BROADCAST_ID_HOLDER, broadcast._id);
                    } else {
                        imageUrl = PreferenceUtility.getConfig().videoThumbnailUrl.replace(Constants.BROADCAST_ID_HOLDER, broadcast._id);
                    }
                    holder.mImageView.setImageBitmap(null);
                    ImageLoader instance = ImageLoader.getInstance();
                    ImageView imageView = holder.mImageView;
                    if (broadcast.isLive) {
                        displayImageOptions = this.mDioLive;
                    } else {
                        displayImageOptions = this.mDioCashed;
                    }
                    instance.displayImage(imageUrl, imageView, displayImageOptions, new ImageLoadingListener() {
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.mProgressView.setVisibility(BroadcastAdapter.TYPE_HEADER);
                        }

                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            holder.mImageView.setScaleType(ScaleType.CENTER);
                            holder.mProgressView.setVisibility(8);
                        }

                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            BroadcastAdapter.access$020(BroadcastAdapter.this, viewHeight);
                            holder.mImageView.setScaleType(ScaleType.FIT_CENTER);
                            holder.mProgressView.setVisibility(8);
                            if (loadedImage != null) {
                                LayoutParams layoutParams = holder.mImageView.getLayoutParams();
                                layoutParams.height = (int) ((((double) holder.mImageView.getWidth()) * ((double) loadedImage.getHeight())) / ((double) loadedImage.getWidth()));
                                holder.mImageView.setLayoutParams(layoutParams);
                                if (BroadcastAdapter.this.mOnEventsCallback != null) {
                                    BroadcastAdapter.this.mOnEventsCallback.handleMessage(Message.obtain(null, BroadcastAdapter.TYPE_ITEM));
                                }
                                BroadcastAdapter.access$012(BroadcastAdapter.this, holder.mCardView.getMeasuredHeight());
                            }
                        }

                        public void onLoadingCancelled(String imageUri, View view) {
                            holder.mProgressView.setVisibility(8);
                        }
                    });
                }
                view = holder.mShareBtn;
                i = (this.mShowOptionMenu && PreferenceUtility.getUser().equals(broadcast.user)) ? TYPE_HEADER : 8;
                view.setVisibility(i);
                holder.mShareBtn.setTag(Integer.valueOf(position));
                holder.mShareBtn.setOnClickListener(this);
                View view2 = holder.mOverflowBtn;
                if (!(this.mShowOptionMenu && PreferenceUtility.getUser().equals(broadcast.user))) {
                    i2 = 8;
                }
                view2.setVisibility(i2);
                holder.mOverflowBtn.setTag(Integer.valueOf(position));
                holder.mOverflowBtn.setOnClickListener(this);
                if (position == getItemCount() - 5 && this.mOnNeedNextDataCallback != null) {
                    this.mOnNeedNextDataCallback.handleMessage(Message.obtain(null, 2));
                }
            }
        } else {
            holder.mCardView.setClickable(false);
            lp.height = this.mMinimalHeight - this.mCurrentHeight;
            if (lp.height < 0) {
                lp.height = TYPE_HEADER;
            }
            holder.mCardView.setLayoutParams(lp);
            CardView cardView = holder.mCardView;
            if (lp.height <= 0) {
                i = 8;
            }
            cardView.setVisibility(i);
        }
    }

    public void onClick(View view) {
        FragmentActivity a = (FragmentActivity) this.mActivityRef.get();
        if (a == null) {
            return;
        }
        if (view.getId() == R.id.overflow_btn) {
            PopupMenu popup = new PopupMenu(a, view);
            Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.menu_broadcast_more, menu);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_POSITION, (Integer) view.getTag());
            for (int i = TYPE_HEADER; i < menu.size(); i += TYPE_ITEM) {
                menu.getItem(i).setIntent(intent);
            }
            popup.setOnMenuItemClickListener(this);
            popup.show();
        } else if (view.getId() == R.id.share_btn) {
            if (PreferenceUtility.getUser().isGuest(a)) {
                a.startActivity(LoginActivity.getIntent(a));
            } else {
                ShareHelper.showSharePopupMenu(view, ShareHelper.getShareMenuItemClickListener(a, (Broadcast) this.mDataset.get(((Integer) view.getTag()).intValue())), true, null);
            }
        } else if (a != null && this.mDataset.size() > ((Integer) view.getTag()).intValue()) {
            a.startActivity(GameDetailsActivity.getIntent(a, (Broadcast) this.mDataset.get(((Integer) view.getTag()).intValue()), this.mSource));
        }
    }

    public int getItemCount() {
        int i = TYPE_ITEM;
        int dataItemCount = (this.mWithHeader ? TYPE_ITEM : TYPE_HEADER) + getDataItemCount();
        if (!this.mWithFooter) {
            i = TYPE_HEADER;
        }
        return dataItemCount + i;
    }

    public int getDataItemCount() {
        return this.mDataset == null ? TYPE_HEADER : this.mDataset.size();
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (item == null) {
            return false;
        }
        Intent intent = item.getIntent();
        if (intent == null) {
            return false;
        }
        final int position = intent.getIntExtra(EXTRA_POSITION, -1);
        if (position == -1) {
            return false;
        }
        if (this.mDataset == null || this.mDataset.size() <= position) {
            Log.e(TAG, "Dataset is empty or IndexOutOfBounds");
            return false;
        }
        final Broadcast broadcast = (Broadcast) this.mDataset.get(position);
        if (broadcast == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_edit_broadcast_info:
                final EditText input = new EditText((Context) this.mActivityRef.get());
                input.setText(broadcast.title);
                new AlertDialog.Builder((Context) this.mActivityRef.get()).setTitle(R.string.broadcast_info).setView(input).setPositiveButton(R.string.action_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        broadcast.title = TextUtils.isEmpty(value) ? BuildConfig.FLAVOR : String.valueOf(value);
                        BroadcastAdapter.this.showProgressDialog(R.string.updating__);
                        Network.setBroadcastInfo((FragmentActivity) BroadcastAdapter.this.mActivityRef.get(), broadcast._id, broadcast.title, null, BroadcastAdapter.this, BroadcastAdapter.this);
                        UIUtils.hideVirtualKeyboard((FragmentActivity) BroadcastAdapter.this.mActivityRef.get(), input.getWindowToken());
                    }
                }).setNegativeButton(17039360, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        UIUtils.hideVirtualKeyboard((FragmentActivity) BroadcastAdapter.this.mActivityRef.get(), input.getWindowToken());
                    }
                }).show();
                break;
            case R.id.action_delete_broadcast:
                new AlertDialog.Builder((Context) this.mActivityRef.get()).setTitle(R.string.action_delete_broadcast).setMessage(R.string.delete_broadcast_confirmation).setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        BroadcastAdapter.this.showProgressDialog(R.string.deleting__);
                        BroadcastAdapter.this.mDataset.remove(position);
                        Network.deleteBroadcast((FragmentActivity) BroadcastAdapter.this.mActivityRef.get(), broadcast._id, BroadcastAdapter.this, BroadcastAdapter.this);
                    }
                }).setNegativeButton(17039360, null).show();
                break;
        }
        return true;
    }

    public void onResponse(Boolean response) {
        if (response != null) {
            safeNotifyDataSetChanged();
            if (this.mOnEventsCallback != null) {
                this.mOnEventsCallback.handleMessage(Message.obtain(null, response.booleanValue() ? 3 : 4));
            }
            hideProgressDialog();
        }
    }

    public void onErrorResponse(VolleyError error) {
        if (this.mOnEventsCallback != null) {
            this.mOnEventsCallback.handleMessage(Message.obtain(null, 4));
        }
        hideProgressDialog();
    }

    public void addHeaderView(View header) {
        if (header != null) {
            this.mWithHeader = true;
            this.mHeaderView = header;
            safeNotifyDataSetChanged();
        }
    }

    public void setMinimalHeight(int minimalHeight) {
        this.mMinimalHeight = minimalHeight;
        this.mWithFooter = this.mMinimalHeight > 0;
        safeNotifyDataSetChanged();
    }

    private void showProgressDialog(int resId) {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            try {
                this.mProgressDialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mProgressDialog = new ProgressDialog((Context) this.mActivityRef.get());
        this.mProgressDialog.setMessage(MainApplication.getRString(resId, new Object[TYPE_HEADER]));
        this.mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.cancel();
        }
    }

    public void updateBroadcasts(Broadcast[] broadcasts) {
        if (broadcasts != null && broadcasts.length != 0) {
            if (this.mDataset.size() == 0) {
                addBroadcasts(broadcasts, false);
                return;
            }
            int i = TYPE_HEADER;
            while (i < broadcasts.length) {
                boolean removed = false;
                if (this.mDataset.size() >= i + TYPE_ITEM) {
                    if (broadcasts[i].equals((DataModel) this.mDataset.get(i))) {
                        i += TYPE_ITEM;
                    } else {
                        this.mDataset.remove(i);
                        removed = true;
                    }
                }
                this.mDataset.add(i, broadcasts[i]);
                if (removed) {
                    try {
                        Crashlytics.log("BroadcastAdapter.notifyItemChanged " + i);
                        notifyItemChanged(i);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        safeNotifyDataSetChanged();
                    }
                    i += TYPE_ITEM;
                } else {
                    Crashlytics.log("BroadcastAdapter.notifyItemInserted " + i);
                    notifyItemInserted(i);
                    i += TYPE_ITEM;
                }
            }
        }
    }

    private void safeNotifyDataSetChanged() {
        try {
            Crashlytics.log("BroadcastAdapter.safeNotifyDataSetChanged");
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}
