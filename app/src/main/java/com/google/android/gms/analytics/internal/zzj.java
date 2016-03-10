package com.google.android.gms.analytics.internal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri.Builder;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.Utility;
import com.google.android.gms.common.internal.zzu;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.ChatFragment;
import io.fabric.sdk.android.BuildConfig;
import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.protocol.HTTP;

class zzj extends zzd implements Closeable {
    private static final String zzKg = String.format("CREATE TABLE IF NOT EXISTS %s ( '%s' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '%s' INTEGER NOT NULL, '%s' TEXT NOT NULL, '%s' TEXT NOT NULL, '%s' INTEGER);", new Object[]{"hits2", "hit_id", "hit_time", "hit_url", "hit_string", "hit_app_id"});
    private static final String zzKh = String.format("SELECT MAX(%s) FROM %s WHERE 1;", new Object[]{"hit_time", "hits2"});
    private final zza zzKi;
    private final zzaj zzKj = new zzaj(zzhP());
    private final zzaj zzKk = new zzaj(zzhP());

    class zza extends SQLiteOpenHelper {
        final /* synthetic */ zzj zzKl;

        zza(zzj com_google_android_gms_analytics_internal_zzj, Context context, String str) {
            this.zzKl = com_google_android_gms_analytics_internal_zzj;
            super(context, str, null, 1);
        }

        private void zza(SQLiteDatabase sQLiteDatabase) {
            int i = 1;
            Set zzb = zzb(sQLiteDatabase, "hits2");
            String[] strArr = new String[]{"hit_id", "hit_string", "hit_time", "hit_url"};
            int length = strArr.length;
            int i2 = 0;
            while (i2 < length) {
                String str = strArr[i2];
                if (zzb.remove(str)) {
                    i2++;
                } else {
                    throw new SQLiteException("Database hits2 is missing required column: " + str);
                }
            }
            if (zzb.remove("hit_app_id")) {
                i = 0;
            }
            if (!zzb.isEmpty()) {
                throw new SQLiteException("Database hits2 has extra columns");
            } else if (i != 0) {
                sQLiteDatabase.execSQL("ALTER TABLE hits2 ADD COLUMN hit_app_id INTEGER");
            }
        }

        private boolean zza(SQLiteDatabase sQLiteDatabase, String str) {
            Object e;
            Throwable th;
            Cursor cursor = null;
            Cursor query;
            try {
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                query = sQLiteDatabase2.query("SQLITE_MASTER", new String[]{ProfilesDBHelper.COLUMN_NAME}, "name=?", new String[]{str}, null, null, null);
                try {
                    boolean moveToFirst = query.moveToFirst();
                    if (query == null) {
                        return moveToFirst;
                    }
                    query.close();
                    return moveToFirst;
                } catch (SQLiteException e2) {
                    e = e2;
                    try {
                        this.zzKl.zzc("Error querying for table", str, e);
                        if (query != null) {
                            query.close();
                        }
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        cursor = query;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            } catch (SQLiteException e3) {
                e = e3;
                query = null;
                this.zzKl.zzc("Error querying for table", str, e);
                if (query != null) {
                    query.close();
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }

        private Set<String> zzb(SQLiteDatabase sQLiteDatabase, String str) {
            Set<String> hashSet = new HashSet();
            Cursor rawQuery = sQLiteDatabase.rawQuery("SELECT * FROM " + str + " LIMIT 0", null);
            try {
                String[] columnNames = rawQuery.getColumnNames();
                for (Object add : columnNames) {
                    hashSet.add(add);
                }
                return hashSet;
            } finally {
                rawQuery.close();
            }
        }

        private void zzb(SQLiteDatabase sQLiteDatabase) {
            int i = 0;
            Set zzb = zzb(sQLiteDatabase, "properties");
            String[] strArr = new String[]{"app_uid", "cid", "tid", NativeProtocol.WEB_DIALOG_PARAMS, "adid", "hits_count"};
            int length = strArr.length;
            while (i < length) {
                String str = strArr[i];
                if (zzb.remove(str)) {
                    i++;
                } else {
                    throw new SQLiteException("Database properties is missing required column: " + str);
                }
            }
            if (!zzb.isEmpty()) {
                throw new SQLiteException("Database properties table has extra columns");
            }
        }

        public SQLiteDatabase getWritableDatabase() {
            if (this.zzKl.zzKk.zzv(3600000)) {
                SQLiteDatabase writableDatabase;
                try {
                    writableDatabase = super.getWritableDatabase();
                } catch (SQLiteException e) {
                    this.zzKl.zzKk.start();
                    this.zzKl.zzaX("Opening the database failed, dropping the table and recreating it");
                    this.zzKl.getContext().getDatabasePath(this.zzKl.zziv()).delete();
                    try {
                        writableDatabase = super.getWritableDatabase();
                        this.zzKl.zzKk.clear();
                    } catch (SQLiteException e2) {
                        this.zzKl.zze("Failed to open freshly created database", e2);
                        throw e2;
                    }
                }
                return writableDatabase;
            }
            throw new SQLiteException("Database open failed");
        }

        public void onCreate(SQLiteDatabase database) {
            zzx.zzbe(database.getPath());
        }

        public void onOpen(SQLiteDatabase database) {
            if (VERSION.SDK_INT < 15) {
                Cursor rawQuery = database.rawQuery("PRAGMA journal_mode=memory", null);
                try {
                    rawQuery.moveToFirst();
                } finally {
                    rawQuery.close();
                }
            }
            if (zza(database, "hits2")) {
                zza(database);
            } else {
                database.execSQL(zzj.zzKg);
            }
            if (zza(database, "properties")) {
                zzb(database);
            } else {
                database.execSQL("CREATE TABLE IF NOT EXISTS properties ( app_uid INTEGER NOT NULL, cid TEXT NOT NULL, tid TEXT NOT NULL, params TEXT NOT NULL, adid INTEGER NOT NULL, hits_count INTEGER NOT NULL, PRIMARY KEY (app_uid, cid, tid)) ;");
            }
        }

        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        }
    }

    zzj(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
        this.zzKi = new zza(this, com_google_android_gms_analytics_internal_zzf.getContext(), zziv());
    }

    private static String zzA(Map<String, String> map) {
        zzu.zzu(map);
        Builder builder = new Builder();
        for (Entry entry : map.entrySet()) {
            builder.appendQueryParameter((String) entry.getKey(), (String) entry.getValue());
        }
        String encodedQuery = builder.build().getEncodedQuery();
        return encodedQuery == null ? BuildConfig.FLAVOR : encodedQuery;
    }

    private long zza(String str, String[] strArr, long j) {
        Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery(str, strArr);
            if (cursor.moveToFirst()) {
                j = cursor.getLong(0);
                if (cursor != null) {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
            return j;
        } catch (SQLiteException e) {
            zzd("Database error", str, e);
            throw e;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private long zzb(String str, String[] strArr) {
        Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery(str, strArr);
            if (cursor.moveToFirst()) {
                long j = cursor.getLong(0);
                if (cursor != null) {
                    cursor.close();
                }
                return j;
            }
            throw new SQLiteException("Database returned empty set");
        } catch (SQLiteException e) {
            zzd("Database error", str, e);
            throw e;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String zzd(zzab com_google_android_gms_analytics_internal_zzab) {
        return com_google_android_gms_analytics_internal_zzab.zzjY() ? zzhR().zzjk() : zzhR().zzjl();
    }

    private static String zze(zzab com_google_android_gms_analytics_internal_zzab) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzab);
        Builder builder = new Builder();
        for (Entry entry : com_google_android_gms_analytics_internal_zzab.zzn().entrySet()) {
            String str = (String) entry.getKey();
            if (!("ht".equals(str) || "qt".equals(str) || "AppUID".equals(str))) {
                builder.appendQueryParameter(str, (String) entry.getValue());
            }
        }
        String encodedQuery = builder.build().getEncodedQuery();
        return encodedQuery == null ? BuildConfig.FLAVOR : encodedQuery;
    }

    private void zziu() {
        int zzju = zzhR().zzju();
        long zzil = zzil();
        if (zzil > ((long) (zzju - 1))) {
            List zzo = zzo((zzil - ((long) zzju)) + 1);
            zzd("Store full, deleting hits to make room, count", Integer.valueOf(zzo.size()));
            zzd(zzo);
        }
    }

    private String zziv() {
        return !zzhR().zziW() ? zzhR().zzjw() : zzhR().zziX() ? zzhR().zzjw() : zzhR().zzjx();
    }

    public void beginTransaction() {
        zzia();
        getWritableDatabase().beginTransaction();
    }

    public void close() {
        try {
            this.zzKi.close();
        } catch (SQLiteException e) {
            zze("Sql error closing database", e);
        } catch (IllegalStateException e2) {
            zze("Error closing database", e2);
        }
    }

    public void endTransaction() {
        zzia();
        getWritableDatabase().endTransaction();
    }

    SQLiteDatabase getWritableDatabase() {
        try {
            return this.zzKi.getWritableDatabase();
        } catch (SQLiteException e) {
            zzd("Error opening database", e);
            throw e;
        }
    }

    boolean isEmpty() {
        return zzil() == 0;
    }

    public void setTransactionSuccessful() {
        zzia();
        getWritableDatabase().setTransactionSuccessful();
    }

    public long zza(long j, String str, String str2) {
        zzu.zzcj(str);
        zzu.zzcj(str2);
        zzia();
        zzhO();
        return zza("SELECT hits_count FROM properties WHERE app_uid=? AND cid=? AND tid=?", new String[]{String.valueOf(j), str, str2}, 0);
    }

    public void zza(long j, String str) {
        zzu.zzcj(str);
        zzia();
        zzhO();
        int delete = getWritableDatabase().delete("properties", "app_uid=? AND cid<>?", new String[]{String.valueOf(j), str});
        if (delete > 0) {
            zza("Deleted property records", Integer.valueOf(delete));
        }
    }

    Map<String, String> zzaY(String str) {
        if (TextUtils.isEmpty(str)) {
            return new HashMap(0);
        }
        try {
            if (!str.startsWith("?")) {
                str = "?" + str;
            }
            List<NameValuePair> parse = URLEncodedUtils.parse(new URI(str), HTTP.UTF_8);
            Map<String, String> hashMap = new HashMap(parse.size());
            for (NameValuePair nameValuePair : parse) {
                hashMap.put(nameValuePair.getName(), nameValuePair.getValue());
            }
            return hashMap;
        } catch (URISyntaxException e) {
            zze("Error parsing hit parameters", e);
            return new HashMap(0);
        }
    }

    Map<String, String> zzaZ(String str) {
        if (TextUtils.isEmpty(str)) {
            return new HashMap(0);
        }
        try {
            List<NameValuePair> parse = URLEncodedUtils.parse(new URI("?" + str), HTTP.UTF_8);
            Map<String, String> hashMap = new HashMap(parse.size());
            for (NameValuePair nameValuePair : parse) {
                hashMap.put(nameValuePair.getName(), nameValuePair.getValue());
            }
            return hashMap;
        } catch (URISyntaxException e) {
            zze("Error parsing property parameters", e);
            return new HashMap(0);
        }
    }

    public void zzb(zzh com_google_android_gms_analytics_internal_zzh) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzh);
        zzia();
        zzhO();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String zzA = zzA(com_google_android_gms_analytics_internal_zzh.zzn());
        ContentValues contentValues = new ContentValues();
        contentValues.put("app_uid", Long.valueOf(com_google_android_gms_analytics_internal_zzh.zzii()));
        contentValues.put("cid", com_google_android_gms_analytics_internal_zzh.getClientId());
        contentValues.put("tid", com_google_android_gms_analytics_internal_zzh.zzij());
        contentValues.put("adid", Integer.valueOf(com_google_android_gms_analytics_internal_zzh.zzik() ? 1 : 0));
        contentValues.put("hits_count", Long.valueOf(com_google_android_gms_analytics_internal_zzh.zzil()));
        contentValues.put(NativeProtocol.WEB_DIALOG_PARAMS, zzA);
        try {
            if (writableDatabase.insertWithOnConflict("properties", null, contentValues, 5) == -1) {
                zzaX("Failed to insert/update a property (got -1)");
            }
        } catch (SQLiteException e) {
            zze("Error storing a property", e);
        }
    }

    public void zzc(zzab com_google_android_gms_analytics_internal_zzab) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzab);
        zzhO();
        zzia();
        String zze = zze(com_google_android_gms_analytics_internal_zzab);
        if (zze.length() > Utility.DEFAULT_STREAM_BUFFER_SIZE) {
            zzhQ().zza(com_google_android_gms_analytics_internal_zzab, "Hit length exceeds the maximum allowed size");
            return;
        }
        zziu();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("hit_string", zze);
        contentValues.put("hit_time", Long.valueOf(com_google_android_gms_analytics_internal_zzab.zzjW()));
        contentValues.put("hit_app_id", Integer.valueOf(com_google_android_gms_analytics_internal_zzab.zzjU()));
        contentValues.put("hit_url", zzd(com_google_android_gms_analytics_internal_zzab));
        try {
            long insert = writableDatabase.insert("hits2", null, contentValues);
            if (insert == -1) {
                zzaX("Failed to insert a hit (got -1)");
            } else {
                zzb("Hit saved to database. db-id, hit", Long.valueOf(insert), com_google_android_gms_analytics_internal_zzab);
            }
        } catch (SQLiteException e) {
            zze("Error storing a hit", e);
        }
    }

    public void zzd(List<Long> list) {
        zzu.zzu(list);
        zzhO();
        zzia();
        if (!list.isEmpty()) {
            int i;
            StringBuilder stringBuilder = new StringBuilder("hit_id");
            stringBuilder.append(" in (");
            for (i = 0; i < list.size(); i++) {
                Long l = (Long) list.get(i);
                if (l == null || l.longValue() == 0) {
                    throw new SQLiteException("Invalid hit id");
                }
                if (i > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(l);
            }
            stringBuilder.append(")");
            String stringBuilder2 = stringBuilder.toString();
            try {
                SQLiteDatabase writableDatabase = getWritableDatabase();
                zza("Deleting dispatched hits. count", Integer.valueOf(list.size()));
                i = writableDatabase.delete("hits2", stringBuilder2, null);
                if (i != list.size()) {
                    zzb("Deleted fewer hits then expected", Integer.valueOf(list.size()), Integer.valueOf(i), stringBuilder2);
                }
            } catch (SQLiteException e) {
                zze("Error deleting hits", e);
                throw e;
            }
        }
    }

    protected void zzhn() {
    }

    public long zzil() {
        zzhO();
        zzia();
        return zzb("SELECT COUNT(*) FROM hits2", null);
    }

    public void zziq() {
        zzhO();
        zzia();
        getWritableDatabase().delete("hits2", null, null);
    }

    public void zzir() {
        zzhO();
        zzia();
        getWritableDatabase().delete("properties", null, null);
    }

    public int zzis() {
        zzhO();
        zzia();
        if (!this.zzKj.zzv(ChatFragment.MID_MUTE_TIME)) {
            return 0;
        }
        this.zzKj.start();
        zzaT("Deleting stale hits (if any)");
        int delete = getWritableDatabase().delete("hits2", "hit_time < ?", new String[]{Long.toString(zzhP().currentTimeMillis() - 2592000000L)});
        zza("Deleted stale hits, count", Integer.valueOf(delete));
        return delete;
    }

    public long zzit() {
        zzhO();
        zzia();
        return zza(zzKh, null, 0);
    }

    public List<Long> zzo(long j) {
        Cursor query;
        Object e;
        Throwable th;
        Cursor cursor = null;
        zzhO();
        zzia();
        if (j <= 0) {
            return Collections.emptyList();
        }
        SQLiteDatabase writableDatabase = getWritableDatabase();
        List<Long> arrayList = new ArrayList();
        try {
            query = writableDatabase.query("hits2", new String[]{"hit_id"}, null, null, null, null, String.format("%s ASC", new Object[]{"hit_id"}), Long.toString(j));
            try {
                if (query.moveToFirst()) {
                    do {
                        arrayList.add(Long.valueOf(query.getLong(0)));
                    } while (query.moveToNext());
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLiteException e2) {
                e = e2;
                try {
                    zzd("Error selecting hit ids", e);
                    if (query != null) {
                        query.close();
                    }
                    return arrayList;
                } catch (Throwable th2) {
                    th = th2;
                    cursor = query;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        } catch (SQLiteException e3) {
            e = e3;
            query = null;
            zzd("Error selecting hit ids", e);
            if (query != null) {
                query.close();
            }
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return arrayList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<com.google.android.gms.analytics.internal.zzab> zzp(long r14) {
        /*
        r13 = this;
        r0 = 1;
        r1 = 0;
        r9 = 0;
        r2 = 0;
        r2 = (r14 > r2 ? 1 : (r14 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x008f;
    L_0x0009:
        com.google.android.gms.common.internal.zzu.zzV(r0);
        r13.zzhO();
        r13.zzia();
        r0 = r13.getWritableDatabase();
        r1 = "hits2";
        r2 = 5;
        r2 = new java.lang.String[r2];	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 0;
        r4 = "hit_id";
        r2[r3] = r4;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 1;
        r4 = "hit_time";
        r2[r3] = r4;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 2;
        r4 = "hit_string";
        r2[r3] = r4;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 3;
        r4 = "hit_url";
        r2[r3] = r4;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 4;
        r4 = "hit_app_id";
        r2[r3] = r4;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r3 = 0;
        r4 = 0;
        r5 = 0;
        r6 = 0;
        r7 = "%s ASC";
        r8 = 1;
        r8 = new java.lang.Object[r8];	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r10 = 0;
        r11 = "hit_id";
        r8[r10] = r11;	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r7 = java.lang.String.format(r7, r8);	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r8 = java.lang.Long.toString(r14);	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x0092, all -> 0x00a2 }
        r10 = new java.util.ArrayList;	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r10.<init>();	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r0 = r9.moveToFirst();	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        if (r0 == 0) goto L_0x0089;
    L_0x0059:
        r0 = 0;
        r6 = r9.getLong(r0);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r0 = 1;
        r3 = r9.getLong(r0);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r0 = 2;
        r0 = r9.getString(r0);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r1 = 3;
        r1 = r9.getString(r1);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r2 = 4;
        r8 = r9.getInt(r2);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r2 = r13.zzaY(r0);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r5 = com.google.android.gms.analytics.internal.zzam.zzbn(r1);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r0 = new com.google.android.gms.analytics.internal.zzab;	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r1 = r13;
        r0.<init>(r1, r2, r3, r5, r6, r8);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r10.add(r0);	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        r0 = r9.moveToNext();	 Catch:{ SQLiteException -> 0x00a4, all -> 0x00a2 }
        if (r0 != 0) goto L_0x0059;
    L_0x0089:
        if (r9 == 0) goto L_0x008e;
    L_0x008b:
        r9.close();
    L_0x008e:
        return r10;
    L_0x008f:
        r0 = r1;
        goto L_0x0009;
    L_0x0092:
        r0 = move-exception;
        r1 = r9;
    L_0x0094:
        r2 = "Error loading hits from the database";
        r13.zze(r2, r0);	 Catch:{ all -> 0x009a }
        throw r0;	 Catch:{ all -> 0x009a }
    L_0x009a:
        r0 = move-exception;
        r9 = r1;
    L_0x009c:
        if (r9 == 0) goto L_0x00a1;
    L_0x009e:
        r9.close();
    L_0x00a1:
        throw r0;
    L_0x00a2:
        r0 = move-exception;
        goto L_0x009c;
    L_0x00a4:
        r0 = move-exception;
        r1 = r9;
        goto L_0x0094;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.internal.zzj.zzp(long):java.util.List<com.google.android.gms.analytics.internal.zzab>");
    }

    public void zzq(long j) {
        zzhO();
        zzia();
        List arrayList = new ArrayList(1);
        arrayList.add(Long.valueOf(j));
        zza("Deleting hit, id", Long.valueOf(j));
        zzd(arrayList);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<com.google.android.gms.analytics.internal.zzh> zzr(long r14) {
        /*
        r13 = this;
        r13.zzia();
        r13.zzhO();
        r0 = r13.getWritableDatabase();
        r9 = 0;
        r1 = 5;
        r2 = new java.lang.String[r1];	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 0;
        r3 = "cid";
        r2[r1] = r3;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 1;
        r3 = "tid";
        r2[r1] = r3;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 2;
        r3 = "adid";
        r2[r1] = r3;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 3;
        r3 = "hits_count";
        r2[r1] = r3;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 4;
        r3 = "params";
        r2[r1] = r3;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = r13.zzhR();	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r10 = r1.zzjv();	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r8 = java.lang.String.valueOf(r10);	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r3 = "app_uid=?";
        r1 = 1;
        r4 = new java.lang.String[r1];	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = 0;
        r5 = java.lang.String.valueOf(r14);	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r4[r1] = r5;	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r1 = "properties";
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r9 = r0.query(r1, r2, r3, r4, r5, r6, r7, r8);	 Catch:{ SQLiteException -> 0x00bb, all -> 0x00b9 }
        r11 = new java.util.ArrayList;	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r11.<init>();	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r0 = r9.moveToFirst();	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 == 0) goto L_0x008c;
    L_0x0054:
        r0 = 0;
        r3 = r9.getString(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r0 = 1;
        r4 = r9.getString(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r0 = 2;
        r0 = r9.getInt(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 == 0) goto L_0x009d;
    L_0x0065:
        r5 = 1;
    L_0x0066:
        r0 = 3;
        r0 = r9.getInt(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r6 = (long) r0;	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r0 = 4;
        r0 = r9.getString(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r8 = r13.zzaZ(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r0 = android.text.TextUtils.isEmpty(r3);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 != 0) goto L_0x0081;
    L_0x007b:
        r0 = android.text.TextUtils.isEmpty(r4);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 == 0) goto L_0x009f;
    L_0x0081:
        r0 = "Read property with empty client id or tracker id";
        r13.zzc(r0, r3, r4);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
    L_0x0086:
        r0 = r9.moveToNext();	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 != 0) goto L_0x0054;
    L_0x008c:
        r0 = r11.size();	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        if (r0 < r10) goto L_0x0097;
    L_0x0092:
        r0 = "Sending hits to too many properties. Campaign report might be incorrect";
        r13.zzaW(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
    L_0x0097:
        if (r9 == 0) goto L_0x009c;
    L_0x0099:
        r9.close();
    L_0x009c:
        return r11;
    L_0x009d:
        r5 = 0;
        goto L_0x0066;
    L_0x009f:
        r0 = new com.google.android.gms.analytics.internal.zzh;	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r1 = r14;
        r0.<init>(r1, r3, r4, r5, r6, r8);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        r11.add(r0);	 Catch:{ SQLiteException -> 0x00a9, all -> 0x00b9 }
        goto L_0x0086;
    L_0x00a9:
        r0 = move-exception;
        r1 = r9;
    L_0x00ab:
        r2 = "Error loading hits from the database";
        r13.zze(r2, r0);	 Catch:{ all -> 0x00b1 }
        throw r0;	 Catch:{ all -> 0x00b1 }
    L_0x00b1:
        r0 = move-exception;
        r9 = r1;
    L_0x00b3:
        if (r9 == 0) goto L_0x00b8;
    L_0x00b5:
        r9.close();
    L_0x00b8:
        throw r0;
    L_0x00b9:
        r0 = move-exception;
        goto L_0x00b3;
    L_0x00bb:
        r0 = move-exception;
        r1 = r9;
        goto L_0x00ab;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.internal.zzj.zzr(long):java.util.List<com.google.android.gms.analytics.internal.zzh>");
    }
}
