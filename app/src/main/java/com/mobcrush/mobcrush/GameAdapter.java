package com.mobcrush.mobcrush;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Game;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import java.util.ArrayList;
import java.util.Arrays;

public class GameAdapter extends Adapter<ViewHolder> implements OnClickListener {
    private static final String CUSTOM_FONT_NAME = "RobotoCondensed-Light.ttf";
    private static final String TAG = "GameAdapter";
    private ArrayList<Game> mDataset = new ArrayList();
    private DisplayImageOptions mDio;
    private OnItemClickListener mOnItemClickListener;
    private Typeface mTypeface;

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        public CardView mCardView;
        public ImageView mImageView;
        public TextView mTextView;

        public ViewHolder(CardView v) {
            super(v);
            this.mCardView = v;
            this.mImageView = (ImageView) v.findViewById(R.id.image);
            this.mTextView = (TextView) v.findViewById(R.id.info_text);
        }
    }

    public GameAdapter(Context context, Game[] games) {
        this.mTypeface = UIUtils.getTypeface(context, CUSTOM_FONT_NAME);
        this.mDio = new Builder().displayer(new RoundedBitmapDisplayer(context.getResources().getDimensionPixelSize(R.dimen.games_corner))).cacheOnDisk(true).cacheInMemory(true).build();
        addGames(games);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void addGames(Game[] games) {
        this.mDataset.addAll(Arrays.asList(games));
        safeNotifyDataSetChanged();
    }

    public Game getItem(int position) {
        if (position < 0 || position > this.mDataset.size() - 1) {
            return null;
        }
        return (Game) this.mDataset.get(position);
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_game, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Game game = (Game) this.mDataset.get(position);
        if (game != null) {
            holder.mTextView.setText(game.name);
            holder.mTextView.setTypeface(this.mTypeface);
            holder.mImageView.setImageBitmap(null);
            ImageLoader.getInstance().displayImage(game.icon, holder.mImageView, this.mDio);
            holder.mCardView.setTag(Integer.valueOf(position));
        }
    }

    public int getItemCount() {
        return this.mDataset.size();
    }

    public void onClick(View v) {
        if (v != null && v.getTag() != null && this.mOnItemClickListener != null) {
            this.mOnItemClickListener.onItemClick(null, v, ((Integer) v.getTag()).intValue(), 0);
        }
    }

    private void safeNotifyDataSetChanged() {
        try {
            Crashlytics.log("GameAdapter.notifyDataSetChanged");
            notifyDataSetChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
        }
    }
}
