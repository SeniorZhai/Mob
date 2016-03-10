package com.mobcrush.mobcrush;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

public class MobcrushActivty extends AppCompatActivity {
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    protected void onResume() {
        super.onResume();
        MainApplication.onActivityResumed(getClass().getSimpleName());
    }

    protected void onPause() {
        MainApplication.onActivityPaused(getClass().getSimpleName());
        super.onPause();
    }
}
