package com.google.android.gms.dynamic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import com.google.android.gms.dynamic.zzc.zza;

public final class zzh extends zza {
    private Fragment zzZX;

    private zzh(Fragment fragment) {
        this.zzZX = fragment;
    }

    public static zzh zza(Fragment fragment) {
        return fragment != null ? new zzh(fragment) : null;
    }

    public Bundle getArguments() {
        return this.zzZX.getArguments();
    }

    public int getId() {
        return this.zzZX.getId();
    }

    public boolean getRetainInstance() {
        return this.zzZX.getRetainInstance();
    }

    public String getTag() {
        return this.zzZX.getTag();
    }

    public int getTargetRequestCode() {
        return this.zzZX.getTargetRequestCode();
    }

    public boolean getUserVisibleHint() {
        return this.zzZX.getUserVisibleHint();
    }

    public zzd getView() {
        return zze.zzw(this.zzZX.getView());
    }

    public boolean isAdded() {
        return this.zzZX.isAdded();
    }

    public boolean isDetached() {
        return this.zzZX.isDetached();
    }

    public boolean isHidden() {
        return this.zzZX.isHidden();
    }

    public boolean isInLayout() {
        return this.zzZX.isInLayout();
    }

    public boolean isRemoving() {
        return this.zzZX.isRemoving();
    }

    public boolean isResumed() {
        return this.zzZX.isResumed();
    }

    public boolean isVisible() {
        return this.zzZX.isVisible();
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        this.zzZX.setHasOptionsMenu(hasMenu);
    }

    public void setMenuVisibility(boolean menuVisible) {
        this.zzZX.setMenuVisibility(menuVisible);
    }

    public void setRetainInstance(boolean retain) {
        this.zzZX.setRetainInstance(retain);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.zzZX.setUserVisibleHint(isVisibleToUser);
    }

    public void startActivity(Intent intent) {
        this.zzZX.startActivity(intent);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.zzZX.startActivityForResult(intent, requestCode);
    }

    public void zzl(zzd com_google_android_gms_dynamic_zzd) {
        this.zzZX.registerForContextMenu((View) zze.zzn(com_google_android_gms_dynamic_zzd));
    }

    public void zzm(zzd com_google_android_gms_dynamic_zzd) {
        this.zzZX.unregisterForContextMenu((View) zze.zzn(com_google_android_gms_dynamic_zzd));
    }

    public zzd zzqk() {
        return zze.zzw(this.zzZX.getActivity());
    }

    public zzc zzql() {
        return zza(this.zzZX.getParentFragment());
    }

    public zzd zzqm() {
        return zze.zzw(this.zzZX.getResources());
    }

    public zzc zzqn() {
        return zza(this.zzZX.getTargetFragment());
    }
}
