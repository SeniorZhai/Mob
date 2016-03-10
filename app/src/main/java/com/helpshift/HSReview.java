package com.helpshift;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.helpshift.util.LocaleUtil;

public final class HSReview extends FragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        LocaleUtil.changeLanguage(this);
        new HSReviewFragment().show(getSupportFragmentManager(), "hs__review_dialog");
    }
}
