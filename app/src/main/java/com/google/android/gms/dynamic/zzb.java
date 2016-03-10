package com.google.android.gms.dynamic;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.dynamic.zzc.zza;

public final class zzb extends zza {
    private Fragment zzajt;

    private zzb(Fragment fragment) {
        this.zzajt = fragment;
    }

    public static zzb zza(Fragment fragment) {
        return fragment != null ? new zzb(fragment) : null;
    }

    public Bundle getArguments() {
        return this.zzajt.getArguments();
    }

    public int getId() {
        return this.zzajt.getId();
    }

    public boolean getRetainInstance() {
        return this.zzajt.getRetainInstance();
    }

    public String getTag() {
        return this.zzajt.getTag();
    }

    public int getTargetRequestCode() {
        return this.zzajt.getTargetRequestCode();
    }

    public boolean getUserVisibleHint() {
        return this.zzajt.getUserVisibleHint();
    }

    public zzd getView() {
        return zze.zzw(this.zzajt.getView());
    }

    public boolean isAdded() {
        return this.zzajt.isAdded();
    }

    public boolean isDetached() {
        return this.zzajt.isDetached();
    }

    public boolean isHidden() {
        return this.zzajt.isHidden();
    }

    public boolean isInLayout() {
        return this.zzajt.isInLayout();
    }

    public boolean isRemoving() {
        return this.zzajt.isRemoving();
    }

    public boolean isResumed() {
        return this.zzajt.isResumed();
    }

    public boolean isVisible() {
        return this.zzajt.isVisible();
    }

    public void setHasOptionsMenu(boolean hasMenu) {
        this.zzajt.setHasOptionsMenu(hasMenu);
    }

    public void setMenuVisibility(boolean menuVisible) {
        this.zzajt.setMenuVisibility(menuVisible);
    }

    public void setRetainInstance(boolean retain) {
        this.zzajt.setRetainInstance(retain);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.zzajt.setUserVisibleHint(isVisibleToUser);
    }

    public void startActivity(Intent intent) {
        this.zzajt.startActivity(intent);
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        this.zzajt.startActivityForResult(intent, requestCode);
    }

    public void zzl(zzd com_google_android_gms_dynamic_zzd) {
        this.zzajt.registerForContextMenu((View) zze.zzn(com_google_android_gms_dynamic_zzd));
    }

    public void zzm(zzd com_google_android_gms_dynamic_zzd) {
        this.zzajt.unregisterForContextMenu((View) zze.zzn(com_google_android_gms_dynamic_zzd));
    }

    public zzd zzqk() {
        return zze.zzw(this.zzajt.getActivity());
    }

    public zzc zzql() {
        return zza(this.zzajt.getParentFragment());
    }

    public zzd zzqm() {
        return zze.zzw(this.zzajt.getResources());
    }

    public zzc zzqn() {
        return zza(this.zzajt.getTargetFragment());
    }
}
