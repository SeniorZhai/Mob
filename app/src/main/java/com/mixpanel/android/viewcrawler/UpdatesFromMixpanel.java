package com.mixpanel.android.viewcrawler;

import com.mixpanel.android.mpmetrics.Tweaks;
import org.json.JSONArray;

public interface UpdatesFromMixpanel {
    Tweaks getTweaks();

    void setEventBindings(JSONArray jSONArray);

    void setVariants(JSONArray jSONArray);

    void startUpdates();
}
