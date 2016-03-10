package com.google.android.gms.tagmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.google.android.exoplayer.upstream.MulticastDataSource;
import com.google.android.gms.internal.zzlb;
import com.google.android.gms.internal.zzld;
import com.helpshift.storage.ProfilesDBHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class zzw implements zzc {
    private static final String zzaLp = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' STRING NOT NULL, '%s' BLOB NOT NULL, '%s' INTEGER NOT NULL);", new Object[]{"datalayer", "ID", "key", "value", "expires"});
    private final Context mContext;
    private final Executor zzaLq;
    private zza zzaLr;
    private int zzaLs;
    private zzlb zzpw;

    class zza extends SQLiteOpenHelper {
        final /* synthetic */ zzw zzaLv;

        zza(zzw com_google_android_gms_tagmanager_zzw, Context context, String str) {
            this.zzaLv = com_google_android_gms_tagmanager_zzw;
            super(context, str, null, 1);
        }

        private boolean zza(String str, SQLiteDatabase sQLiteDatabase) {
            Cursor cursor;
            Throwable th;
            Cursor cursor2 = null;
            try {
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Cursor query = sQLiteDatabase2.query("SQLITE_MASTER", new String[]{ProfilesDBHelper.COLUMN_NAME}, "name=?", new String[]{str}, null, null, null);
                try {
                    boolean moveToFirst = query.moveToFirst();
                    if (query == null) {
                        return moveToFirst;
                    }
                    query.close();
                    return moveToFirst;
                } catch (SQLiteException e) {
                    cursor = query;
                    try {
                        zzbg.zzaC("Error querying for table " + str);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return false;
                    } catch (Throwable th2) {
                        cursor2 = cursor;
                        th = th2;
                        if (cursor2 != null) {
                            cursor2.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    cursor2 = query;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th;
                }
            } catch (SQLiteException e2) {
                cursor = null;
                zzbg.zzaC("Error querying for table " + str);
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            } catch (Throwable th4) {
                th = th4;
                if (cursor2 != null) {
                    cursor2.close();
                }
                throw th;
            }
        }

        private void zzc(SQLiteDatabase sQLiteDatabase) {
            Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT * FROM datalayer WHERE 0", null);
            Set hashSet = new HashSet();
            try {
                String[] columnNames = rawQuery.getColumnNames();
                for (Object add : columnNames) {
                    hashSet.add(add);
                }
                if (!hashSet.remove("key") || !hashSet.remove("value") || !hashSet.remove("ID") || !hashSet.remove("expires")) {
                    throw new SQLiteException("Database column missing");
                } else if (!hashSet.isEmpty()) {
                    throw new SQLiteException("Database has extra columns");
                }
            } finally {
                rawQuery.close();
            }
        }

        public SQLiteDatabase getWritableDatabase() {
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = super.getWritableDatabase();
            } catch (SQLiteException e) {
                this.zzaLv.mContext.getDatabasePath("google_tagmanager.db").delete();
            }
            return sQLiteDatabase == null ? super.getWritableDatabase() : sQLiteDatabase;
        }

        public void onCreate(SQLiteDatabase db) {
            zzal.zzbe(db.getPath());
        }

        public void onOpen(SQLiteDatabase db) {
            if (VERSION.SDK_INT < 15) {
                Cursor rawQuery = db.rawQuery("PRAGMA journal_mode=memory", null);
                try {
                    rawQuery.moveToFirst();
                } finally {
                    rawQuery.close();
                }
            }
            if (zza("datalayer", db)) {
                zzc(db);
            } else {
                db.execSQL(zzw.zzaLp);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private static class zzb {
        final byte[] zzaLy;
        final String zztw;

        zzb(String str, byte[] bArr) {
            this.zztw = str;
            this.zzaLy = bArr;
        }

        public String toString() {
            return "KeyAndSerialized: key = " + this.zztw + " serialized hash = " + Arrays.hashCode(this.zzaLy);
        }
    }

    public zzw(Context context) {
        this(context, zzld.zzoQ(), "google_tagmanager.db", MulticastDataSource.DEFAULT_MAX_PACKET_SIZE, Executors.newSingleThreadExecutor());
    }

    zzw(Context context, zzlb com_google_android_gms_internal_zzlb, String str, int i, Executor executor) {
        this.mContext = context;
        this.zzpw = com_google_android_gms_internal_zzlb;
        this.zzaLs = i;
        this.zzaLq = executor;
        this.zzaLr = new zza(this, this.mContext, str);
    }

    private byte[] zzA(Object obj) {
        ObjectOutputStream objectOutputStream;
        Throwable th;
        byte[] bArr = null;
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            try {
                objectOutputStream.writeObject(obj);
                bArr = byteArrayOutputStream.toByteArray();
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e) {
                    }
                }
                byteArrayOutputStream.close();
            } catch (IOException e2) {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e3) {
                    }
                }
                byteArrayOutputStream.close();
                return bArr;
            } catch (Throwable th2) {
                th = th2;
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException e4) {
                        throw th;
                    }
                }
                byteArrayOutputStream.close();
                throw th;
            }
        } catch (IOException e5) {
            objectOutputStream = bArr;
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
            return bArr;
        } catch (Throwable th3) {
            Throwable th4 = th3;
            objectOutputStream = bArr;
            th = th4;
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
            throw th;
        }
        return bArr;
    }

    private void zzS(long j) {
        SQLiteDatabase zzer = zzer("Error opening database for deleteOlderThan.");
        if (zzer != null) {
            try {
                zzbg.zzaB("Deleted " + zzer.delete("datalayer", "expires <= ?", new String[]{Long.toString(j)}) + " expired items");
            } catch (SQLiteException e) {
                zzbg.zzaC("Error deleting old entries.");
            }
        }
    }

    private synchronized void zzb(List<zzb> list, long j) {
        try {
            long currentTimeMillis = this.zzpw.currentTimeMillis();
            zzS(currentTimeMillis);
            zziS(list.size());
            zzc(list, currentTimeMillis + j);
            zzyF();
        } catch (Throwable th) {
            zzyF();
        }
    }

    private void zzc(List<zzb> list, long j) {
        SQLiteDatabase zzer = zzer("Error opening database for writeEntryToDatabase.");
        if (zzer != null) {
            for (zzb com_google_android_gms_tagmanager_zzw_zzb : list) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("expires", Long.valueOf(j));
                contentValues.put("key", com_google_android_gms_tagmanager_zzw_zzb.zztw);
                contentValues.put("value", com_google_android_gms_tagmanager_zzw_zzb.zzaLy);
                zzer.insert("datalayer", null, contentValues);
            }
        }
    }

    private void zze(String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            SQLiteDatabase zzer = zzer("Error opening database for deleteEntries.");
            if (zzer != null) {
                try {
                    zzer.delete("datalayer", String.format("%s in (%s)", new Object[]{"ID", TextUtils.join(",", Collections.nCopies(strArr.length, "?"))}), strArr);
                } catch (SQLiteException e) {
                    zzbg.zzaC("Error deleting entries " + Arrays.toString(strArr));
                }
            }
        }
    }

    private void zzeq(String str) {
        SQLiteDatabase zzer = zzer("Error opening database for clearKeysWithPrefix.");
        if (zzer != null) {
            try {
                zzbg.zzaB("Cleared " + zzer.delete("datalayer", "key = ? OR key LIKE ?", new String[]{str, str + ".%"}) + " items");
            } catch (SQLiteException e) {
                zzbg.zzaC("Error deleting entries with key prefix: " + str + " (" + e + ").");
            } finally {
                zzyF();
            }
        }
    }

    private SQLiteDatabase zzer(String str) {
        try {
            return this.zzaLr.getWritableDatabase();
        } catch (SQLiteException e) {
            zzbg.zzaC(str);
            return null;
        }
    }

    private void zziS(int i) {
        int zzyE = (zzyE() - this.zzaLs) + i;
        if (zzyE > 0) {
            List zziT = zziT(zzyE);
            zzbg.zzaA("DataLayer store full, deleting " + zziT.size() + " entries to make room.");
            zze((String[]) zziT.toArray(new String[0]));
        }
    }

    private List<String> zziT(int i) {
        SQLiteException e;
        Throwable th;
        List<String> arrayList = new ArrayList();
        if (i <= 0) {
            zzbg.zzaC("Invalid maxEntries specified. Skipping.");
            return arrayList;
        }
        SQLiteDatabase zzer = zzer("Error opening database for peekEntryIds.");
        if (zzer == null) {
            return arrayList;
        }
        Cursor query;
        try {
            query = zzer.query("datalayer", new String[]{"ID"}, null, null, null, null, String.format("%s ASC", new Object[]{"ID"}), Integer.toString(i));
            try {
                if (query.moveToFirst()) {
                    do {
                        arrayList.add(String.valueOf(query.getLong(0)));
                    } while (query.moveToNext());
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLiteException e2) {
                e = e2;
                try {
                    zzbg.zzaC("Error in peekEntries fetching entryIds: " + e.getMessage());
                    if (query != null) {
                        query.close();
                    }
                    return arrayList;
                } catch (Throwable th2) {
                    th = th2;
                    if (query != null) {
                        query.close();
                    }
                    throw th;
                }
            }
        } catch (SQLiteException e3) {
            e = e3;
            query = null;
            zzbg.zzaC("Error in peekEntries fetching entryIds: " + e.getMessage());
            if (query != null) {
                query.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            query = null;
            if (query != null) {
                query.close();
            }
            throw th;
        }
        return arrayList;
    }

    private List<zza> zzp(List<zzb> list) {
        List<zza> arrayList = new ArrayList();
        for (zzb com_google_android_gms_tagmanager_zzw_zzb : list) {
            arrayList.add(new zza(com_google_android_gms_tagmanager_zzw_zzb.zztw, zzq(com_google_android_gms_tagmanager_zzw_zzb.zzaLy)));
        }
        return arrayList;
    }

    private Object zzq(byte[] bArr) {
        Object readObject;
        Throwable th;
        ObjectInputStream objectInputStream = null;
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        ObjectInputStream objectInputStream2;
        try {
            objectInputStream2 = new ObjectInputStream(byteArrayInputStream);
            try {
                readObject = objectInputStream2.readObject();
                if (objectInputStream2 != null) {
                    try {
                        objectInputStream2.close();
                    } catch (IOException e) {
                    }
                }
                byteArrayInputStream.close();
            } catch (IOException e2) {
                if (objectInputStream2 != null) {
                    try {
                        objectInputStream2.close();
                    } catch (IOException e3) {
                    }
                }
                byteArrayInputStream.close();
                return readObject;
            } catch (ClassNotFoundException e4) {
                if (objectInputStream2 != null) {
                    try {
                        objectInputStream2.close();
                    } catch (IOException e5) {
                    }
                }
                byteArrayInputStream.close();
                return readObject;
            } catch (Throwable th2) {
                th = th2;
                if (objectInputStream2 != null) {
                    try {
                        objectInputStream2.close();
                    } catch (IOException e6) {
                        throw th;
                    }
                }
                byteArrayInputStream.close();
                throw th;
            }
        } catch (IOException e7) {
            objectInputStream2 = objectInputStream;
            if (objectInputStream2 != null) {
                objectInputStream2.close();
            }
            byteArrayInputStream.close();
            return readObject;
        } catch (ClassNotFoundException e8) {
            objectInputStream2 = objectInputStream;
            if (objectInputStream2 != null) {
                objectInputStream2.close();
            }
            byteArrayInputStream.close();
            return readObject;
        } catch (Throwable th3) {
            Throwable th4 = th3;
            objectInputStream2 = objectInputStream;
            th = th4;
            if (objectInputStream2 != null) {
                objectInputStream2.close();
            }
            byteArrayInputStream.close();
            throw th;
        }
        return readObject;
    }

    private List<zzb> zzq(List<zza> list) {
        List<zzb> arrayList = new ArrayList();
        for (zza com_google_android_gms_tagmanager_DataLayer_zza : list) {
            arrayList.add(new zzb(com_google_android_gms_tagmanager_DataLayer_zza.zztw, zzA(com_google_android_gms_tagmanager_DataLayer_zza.zzGK)));
        }
        return arrayList;
    }

    private List<zza> zzyC() {
        try {
            zzS(this.zzpw.currentTimeMillis());
            List<zza> zzp = zzp(zzyD());
            return zzp;
        } finally {
            zzyF();
        }
    }

    private List<zzb> zzyD() {
        SQLiteDatabase zzer = zzer("Error opening database for loadSerialized.");
        List<zzb> arrayList = new ArrayList();
        if (zzer == null) {
            return arrayList;
        }
        Cursor query = zzer.query("datalayer", new String[]{"key", "value"}, null, null, null, null, "ID", null);
        while (query.moveToNext()) {
            try {
                arrayList.add(new zzb(query.getString(0), query.getBlob(1)));
            } finally {
                query.close();
            }
        }
        return arrayList;
    }

    private int zzyE() {
        Cursor cursor = null;
        int i = 0;
        SQLiteDatabase zzer = zzer("Error opening database for getNumStoredEntries.");
        if (zzer != null) {
            try {
                cursor = zzer.rawQuery("SELECT COUNT(*) from datalayer", null);
                if (cursor.moveToFirst()) {
                    i = (int) cursor.getLong(0);
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                zzbg.zzaC("Error getting numStoredEntries");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return i;
    }

    private void zzyF() {
        try {
            this.zzaLr.close();
        } catch (SQLiteException e) {
        }
    }

    public void zza(final com.google.android.gms.tagmanager.DataLayer.zzc.zza com_google_android_gms_tagmanager_DataLayer_zzc_zza) {
        this.zzaLq.execute(new Runnable(this) {
            final /* synthetic */ zzw zzaLv;

            public void run() {
                com_google_android_gms_tagmanager_DataLayer_zzc_zza.zzo(this.zzaLv.zzyC());
            }
        });
    }

    public void zza(List<zza> list, final long j) {
        final List zzq = zzq((List) list);
        this.zzaLq.execute(new Runnable(this) {
            final /* synthetic */ zzw zzaLv;

            public void run() {
                this.zzaLv.zzb(zzq, j);
            }
        });
    }

    public void zzep(final String str) {
        this.zzaLq.execute(new Runnable(this) {
            final /* synthetic */ zzw zzaLv;

            public void run() {
                this.zzaLv.zzeq(str);
            }
        });
    }
}
