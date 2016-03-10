package com.google.android.gms.dynamic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.internal.zzf;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class zza<T extends LifecycleDelegate> {
    private T zzajh;
    private Bundle zzaji;
    private LinkedList<zza> zzajj;
    private final zzf<T> zzajk = new zzf<T>(this) {
        final /* synthetic */ zza zzajl;

        {
            this.zzajl = r1;
        }

        public void zza(T t) {
            this.zzajl.zzajh = t;
            Iterator it = this.zzajl.zzajj.iterator();
            while (it.hasNext()) {
                ((zza) it.next()).zzb(this.zzajl.zzajh);
            }
            this.zzajl.zzajj.clear();
            this.zzajl.zzaji = null;
        }
    };

    private interface zza {
        int getState();

        void zzb(LifecycleDelegate lifecycleDelegate);
    }

    private void zza(Bundle bundle, zza com_google_android_gms_dynamic_zza_zza) {
        if (this.zzajh != null) {
            com_google_android_gms_dynamic_zza_zza.zzb(this.zzajh);
            return;
        }
        if (this.zzajj == null) {
            this.zzajj = new LinkedList();
        }
        this.zzajj.add(com_google_android_gms_dynamic_zza_zza);
        if (bundle != null) {
            if (this.zzaji == null) {
                this.zzaji = (Bundle) bundle.clone();
            } else {
                this.zzaji.putAll(bundle);
            }
        }
        zza(this.zzajk);
    }

    public static void zzb(FrameLayout frameLayout) {
        final Context context = frameLayout.getContext();
        final int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        CharSequence zzb = zzf.zzb(context, isGooglePlayServicesAvailable, GooglePlayServicesUtil.zzad(context));
        CharSequence zzh = zzf.zzh(context, isGooglePlayServicesAvailable);
        View linearLayout = new LinearLayout(frameLayout.getContext());
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LayoutParams(-2, -2));
        frameLayout.addView(linearLayout);
        View textView = new TextView(frameLayout.getContext());
        textView.setLayoutParams(new LayoutParams(-2, -2));
        textView.setText(zzb);
        linearLayout.addView(textView);
        if (zzh != null) {
            View button = new Button(context);
            button.setLayoutParams(new LayoutParams(-2, -2));
            button.setText(zzh);
            linearLayout.addView(button);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    context.startActivity(GooglePlayServicesUtil.zzaT(isGooglePlayServicesAvailable));
                }
            });
        }
    }

    private void zzdY(int i) {
        while (!this.zzajj.isEmpty() && ((zza) this.zzajj.getLast()).getState() >= i) {
            this.zzajj.removeLast();
        }
    }

    public void onCreate(final Bundle savedInstanceState) {
        zza(savedInstanceState, new zza(this) {
            final /* synthetic */ zza zzajl;

            public int getState() {
                return 1;
            }

            public void zzb(LifecycleDelegate lifecycleDelegate) {
                this.zzajl.zzajh.onCreate(savedInstanceState);
            }
        });
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout frameLayout = new FrameLayout(inflater.getContext());
        final LayoutInflater layoutInflater = inflater;
        final ViewGroup viewGroup = container;
        final Bundle bundle = savedInstanceState;
        zza(savedInstanceState, new zza(this) {
            final /* synthetic */ zza zzajl;

            public int getState() {
                return 2;
            }

            public void zzb(LifecycleDelegate lifecycleDelegate) {
                frameLayout.removeAllViews();
                frameLayout.addView(this.zzajl.zzajh.onCreateView(layoutInflater, viewGroup, bundle));
            }
        });
        if (this.zzajh == null) {
            zza(frameLayout);
        }
        return frameLayout;
    }

    public void onDestroy() {
        if (this.zzajh != null) {
            this.zzajh.onDestroy();
        } else {
            zzdY(1);
        }
    }

    public void onDestroyView() {
        if (this.zzajh != null) {
            this.zzajh.onDestroyView();
        } else {
            zzdY(2);
        }
    }

    public void onInflate(final Activity activity, final Bundle attrs, final Bundle savedInstanceState) {
        zza(savedInstanceState, new zza(this) {
            final /* synthetic */ zza zzajl;

            public int getState() {
                return 0;
            }

            public void zzb(LifecycleDelegate lifecycleDelegate) {
                this.zzajl.zzajh.onInflate(activity, attrs, savedInstanceState);
            }
        });
    }

    public void onLowMemory() {
        if (this.zzajh != null) {
            this.zzajh.onLowMemory();
        }
    }

    public void onPause() {
        if (this.zzajh != null) {
            this.zzajh.onPause();
        } else {
            zzdY(5);
        }
    }

    public void onResume() {
        zza(null, new zza(this) {
            final /* synthetic */ zza zzajl;

            {
                this.zzajl = r1;
            }

            public int getState() {
                return 5;
            }

            public void zzb(LifecycleDelegate lifecycleDelegate) {
                this.zzajl.zzajh.onResume();
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.zzajh != null) {
            this.zzajh.onSaveInstanceState(outState);
        } else if (this.zzaji != null) {
            outState.putAll(this.zzaji);
        }
    }

    public void onStart() {
        zza(null, new zza(this) {
            final /* synthetic */ zza zzajl;

            {
                this.zzajl = r1;
            }

            public int getState() {
                return 4;
            }

            public void zzb(LifecycleDelegate lifecycleDelegate) {
                this.zzajl.zzajh.onStart();
            }
        });
    }

    public void onStop() {
        if (this.zzajh != null) {
            this.zzajh.onStop();
        } else {
            zzdY(4);
        }
    }

    protected void zza(FrameLayout frameLayout) {
        zzb(frameLayout);
    }

    protected abstract void zza(zzf<T> com_google_android_gms_dynamic_zzf_T);

    public T zzqj() {
        return this.zzajh;
    }
}
