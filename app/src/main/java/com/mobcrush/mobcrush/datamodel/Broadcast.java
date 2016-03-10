package com.mobcrush.mobcrush.datamodel;

import android.support.annotation.NonNull;
import com.mobcrush.mobcrush.Constants;

public class Broadcast extends DataModel {
    public String _id;
    private double aspectRatio;
    public Channel channel;
    public ChatRoom chatRoom;
    public boolean currentLiked;
    public int currentViewers;
    public String endDate;
    public Game game;
    public boolean hasCustomThumbnail;
    public int height;
    public String ingestIndex;
    public boolean isLive;
    public int likes;
    public String regionName;
    public String startDate;
    public String title;
    public int totalViews;
    public transient int urlsCopied;
    public User user;
    public String viewKey;
    public int width;

    public int getViewsNumber() {
        return this.totalViews;
    }

    public double getAspectRatio() {
        if (this.aspectRatio == 0.0d && this.height != 0) {
            this.aspectRatio = (double) (this.width / this.height);
        }
        return this.aspectRatio;
    }

    public String getURL(@NonNull Config config) {
        String url;
        if (this.isLive) {
            url = config.liveUrl;
            if (url == null) {
                return url;
            }
            if (this.viewKey != null) {
                url = url.replace(Constants.HLS_KEY_HOLDER, this.viewKey);
            }
            if (this.regionName != null) {
                url = url.replace(Constants.REGION_NAME_HOLDER, this.regionName);
            }
            if (this.ingestIndex != null) {
                url = url.replace(Constants.INGEST_INDEX_HOLDER, this.ingestIndex);
            }
            return url.replace(Constants.BROADCAST_ID_HOLDER, this._id);
        }
        url = config.recordedVideoUrl;
        if (url != null) {
            return url.replace(Constants.BROADCAST_ID_HOLDER, this._id);
        }
        return url;
    }
}
