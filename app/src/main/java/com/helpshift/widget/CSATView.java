package com.helpshift.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.util.HSColor;

public class CSATView extends RelativeLayout implements OnRatingBarChangeListener {
    private CSATDialog csatDialog;
    private CSATListener csatListener = null;
    private RelativeLayout divider;
    private RatingBar ratingBar;

    public interface CSATListener {
        void csatViewDissmissed();

        void sendCSATSurvey(int i, String str);
    }

    public CSATView(Context context) {
        super(context);
        initView(context);
    }

    public CSATView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CSATView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, layout.hs__csat_view, this);
        this.csatDialog = new CSATDialog(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.ratingBar = (RatingBar) findViewById(id.ratingBar);
        this.ratingBar.setOnRatingBarChangeListener(this);
        TextView likeMessage = (TextView) findViewById(id.csat_like_msg);
        TextView optionText = (TextView) findViewById(id.option_text);
        HSColor.setTextViewAlpha((TextView) findViewById(id.csat_dislike_msg), 0.5f);
        HSColor.setTextViewAlpha(likeMessage, 0.5f);
        HSColor.setTextViewAlpha(optionText, 0.5f);
        this.divider = (RelativeLayout) findViewById(id.divider);
    }

    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        if (fromUser) {
            this.csatDialog.show(this);
        }
    }

    protected RatingBar getRatingBar() {
        return this.ratingBar;
    }

    protected void dismiss() {
        setVisibility(8);
        this.csatDialog = null;
        if (this.csatListener != null) {
            this.csatListener.csatViewDissmissed();
        }
    }

    protected void sendCSATSurvey(float rating, String feedback) {
        if (this.csatListener != null) {
            this.csatListener.sendCSATSurvey(Math.round(rating), feedback);
        }
    }

    public void setCSATListener(CSATListener csatListener) {
        this.csatListener = csatListener;
    }

    public void setDividerMargin(int left, int top, int right, int bottom) {
        LayoutParams layoutParams = (LayoutParams) this.divider.getLayoutParams();
        layoutParams.setMargins(left, top, right, bottom);
        this.divider.setLayoutParams(layoutParams);
    }
}
