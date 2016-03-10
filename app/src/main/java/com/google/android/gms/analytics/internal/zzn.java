package com.google.android.gms.analytics.internal;

import android.content.Context;
import com.google.android.gms.common.internal.zzu;
import com.helpshift.res.values.HSConsts;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class zzn extends zzd {
    private volatile String zzJd;
    private Future<String> zzKG;

    protected zzn(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    private boolean zzg(Context context, String str) {
        boolean z = false;
        zzu.zzcj(str);
        zzu.zzbZ("ClientId should be saved from worker thread");
        FileOutputStream fileOutputStream = null;
        try {
            zza("Storing clientId", str);
            fileOutputStream = context.openFileOutput("gaClientId", 0);
            fileOutputStream.write(str.getBytes());
            z = true;
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    zze("Failed to close clientId writing stream", e);
                }
            }
        } catch (FileNotFoundException e2) {
            zze("Error creating clientId file", e2);
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    zze("Failed to close clientId writing stream", e3);
                }
            }
        } catch (IOException e32) {
            zze("Error writing to clientId file", e32);
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e322) {
                    zze("Failed to close clientId writing stream", e322);
                }
            }
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3222) {
                    zze("Failed to close clientId writing stream", e3222);
                }
            }
        }
        return z;
    }

    private String zziS() {
        String zziT = zziT();
        try {
            return !zzg(zzhS().getContext(), zziT) ? HSConsts.STATUS_NEW : zziT;
        } catch (Exception e) {
            zze("Error saving clientId file", e);
            return HSConsts.STATUS_NEW;
        }
    }

    protected String zzX(Context context) {
        FileInputStream openFileInput;
        FileInputStream fileInputStream;
        Object e;
        Throwable th;
        zzu.zzbZ("ClientId should be loaded from worker thread");
        try {
            openFileInput = context.openFileInput("gaClientId");
            try {
                byte[] bArr = new byte[36];
                int read = openFileInput.read(bArr, 0, bArr.length);
                if (openFileInput.available() > 0) {
                    zzaW("clientId file seems corrupted, deleting it.");
                    openFileInput.close();
                    context.deleteFile("gaClientId");
                    if (openFileInput == null) {
                        return null;
                    }
                    try {
                        openFileInput.close();
                        return null;
                    } catch (IOException e2) {
                        zze("Failed to close client id reading stream", e2);
                        return null;
                    }
                } else if (read < 14) {
                    zzaW("clientId file is empty, deleting it.");
                    openFileInput.close();
                    context.deleteFile("gaClientId");
                    if (openFileInput == null) {
                        return null;
                    }
                    try {
                        openFileInput.close();
                        return null;
                    } catch (IOException e22) {
                        zze("Failed to close client id reading stream", e22);
                        return null;
                    }
                } else {
                    openFileInput.close();
                    String str = new String(bArr, 0, read);
                    zza("Read client id from disk", str);
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e3) {
                            zze("Failed to close client id reading stream", e3);
                        }
                    }
                    return str;
                }
            } catch (FileNotFoundException e4) {
                fileInputStream = openFileInput;
                if (fileInputStream != null) {
                    return null;
                }
                try {
                    fileInputStream.close();
                    return null;
                } catch (IOException e222) {
                    zze("Failed to close client id reading stream", e222);
                    return null;
                }
            } catch (IOException e5) {
                e = e5;
                try {
                    zze("Error reading client id file, deleting it", e);
                    context.deleteFile("gaClientId");
                    if (openFileInput != null) {
                        return null;
                    }
                    try {
                        openFileInput.close();
                        return null;
                    } catch (IOException e2222) {
                        zze("Failed to close client id reading stream", e2222);
                        return null;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e22222) {
                            zze("Failed to close client id reading stream", e22222);
                        }
                    }
                    throw th;
                }
            }
        } catch (FileNotFoundException e6) {
            fileInputStream = null;
            if (fileInputStream != null) {
                return null;
            }
            fileInputStream.close();
            return null;
        } catch (IOException e7) {
            e = e7;
            openFileInput = null;
            zze("Error reading client id file, deleting it", e);
            context.deleteFile("gaClientId");
            if (openFileInput != null) {
                return null;
            }
            openFileInput.close();
            return null;
        } catch (Throwable th3) {
            openFileInput = null;
            th = th3;
            if (openFileInput != null) {
                openFileInput.close();
            }
            throw th;
        }
    }

    protected void zzhn() {
    }

    public String zziP() {
        String str;
        zzia();
        synchronized (this) {
            if (this.zzJd == null) {
                this.zzKG = zzhS().zzb(new Callable<String>(this) {
                    final /* synthetic */ zzn zzKH;

                    {
                        this.zzKH = r1;
                    }

                    public /* synthetic */ Object call() throws Exception {
                        return zziU();
                    }

                    public String zziU() throws Exception {
                        return this.zzKH.zziR();
                    }
                });
            }
            if (this.zzKG != null) {
                try {
                    this.zzJd = (String) this.zzKG.get();
                } catch (InterruptedException e) {
                    zzd("ClientId loading or generation was interrupted", e);
                    this.zzJd = HSConsts.STATUS_NEW;
                } catch (ExecutionException e2) {
                    zze("Failed to load or generate client id", e2);
                    this.zzJd = HSConsts.STATUS_NEW;
                }
                if (this.zzJd == null) {
                    this.zzJd = HSConsts.STATUS_NEW;
                }
                zza("Loaded clientId", this.zzJd);
                this.zzKG = null;
            }
            str = this.zzJd;
        }
        return str;
    }

    String zziQ() {
        synchronized (this) {
            this.zzJd = null;
            this.zzKG = zzhS().zzb(new Callable<String>(this) {
                final /* synthetic */ zzn zzKH;

                {
                    this.zzKH = r1;
                }

                public /* synthetic */ Object call() throws Exception {
                    return zziU();
                }

                public String zziU() throws Exception {
                    return this.zzKH.zziS();
                }
            });
        }
        return zziP();
    }

    String zziR() {
        String zzX = zzX(zzhS().getContext());
        return zzX == null ? zziS() : zzX;
    }

    protected String zziT() {
        return UUID.randomUUID().toString().toLowerCase();
    }
}
