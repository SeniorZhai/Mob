package com.google.android.gms.iid;

import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;

public class MessengerCompat implements Parcelable {
    public static final Creator<MessengerCompat> CREATOR = new Creator<MessengerCompat>() {
        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return zzeb(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return zzgo(x0);
        }

        public MessengerCompat zzeb(Parcel parcel) {
            IBinder readStrongBinder = parcel.readStrongBinder();
            return readStrongBinder != null ? new MessengerCompat(readStrongBinder) : null;
        }

        public MessengerCompat[] zzgo(int i) {
            return new MessengerCompat[i];
        }
    };
    Messenger zzaxa;
    zzb zzaxb;

    private final class zza extends com.google.android.gms.iid.zzb.zza {
        Handler handler;
        final /* synthetic */ MessengerCompat zzaxc;

        zza(MessengerCompat messengerCompat, Handler handler) {
            this.zzaxc = messengerCompat;
            this.handler = handler;
        }

        public void send(Message msg) throws RemoteException {
            msg.arg2 = Binder.getCallingUid();
            this.handler.dispatchMessage(msg);
        }
    }

    public MessengerCompat(Handler handler) {
        if (VERSION.SDK_INT >= 21) {
            this.zzaxa = new Messenger(handler);
        } else {
            this.zzaxb = new zza(this, handler);
        }
    }

    public MessengerCompat(IBinder target) {
        if (VERSION.SDK_INT >= 21) {
            this.zzaxa = new Messenger(target);
        } else {
            this.zzaxb = com.google.android.gms.iid.zzb.zza.zzbR(target);
        }
    }

    public static int zzc(Message message) {
        return VERSION.SDK_INT >= 21 ? zzd(message) : message.arg2;
    }

    private static int zzd(Message message) {
        return message.sendingUid;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object otherObj) {
        boolean z = false;
        if (otherObj != null) {
            try {
                z = getBinder().equals(((MessengerCompat) otherObj).getBinder());
            } catch (ClassCastException e) {
            }
        }
        return z;
    }

    public IBinder getBinder() {
        return this.zzaxa != null ? this.zzaxa.getBinder() : this.zzaxb.asBinder();
    }

    public int hashCode() {
        return getBinder().hashCode();
    }

    public void send(Message message) throws RemoteException {
        if (this.zzaxa != null) {
            this.zzaxa.send(message);
        } else {
            this.zzaxb.send(message);
        }
    }

    public void writeToParcel(Parcel out, int flags) {
        if (this.zzaxa != null) {
            out.writeStrongBinder(this.zzaxa.getBinder());
        } else {
            out.writeStrongBinder(this.zzaxb.asBinder());
        }
    }
}
