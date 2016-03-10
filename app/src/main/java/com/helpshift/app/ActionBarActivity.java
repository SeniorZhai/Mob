package com.helpshift.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MenuInflater;

public abstract class ActionBarActivity extends FragmentActivity {
    private ActionBarHelper actionBarHelper = ActionBarHelper.createInstance(this);

    public ActionBarHelper getActionBarHelper() {
        return this.actionBarHelper;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.actionBarHelper.onCreate(savedInstanceState);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.actionBarHelper.onPostCreate(savedInstanceState);
    }

    public MenuInflater getMenuInflater() {
        return this.actionBarHelper.getMenuInflater(super.getMenuInflater());
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();
        int action = event.getAction();
        if (keycode == 82 && action == 1) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void onBackPressed() {
        if ((this.actionBarHelper instanceof ActionBarHelperBase) && ((ActionBarHelperBase) this.actionBarHelper).isViewExpanded()) {
            this.actionBarHelper.collapseActionView(null);
        } else {
            super.onBackPressed();
        }
    }

    public void supportRequestWindowFeature(int featureId) {
        this.actionBarHelper.supportRequestWindowFeature(featureId);
    }

    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        this.actionBarHelper.setSupportProgressBarIndeterminateVisibility(visible);
    }
}
