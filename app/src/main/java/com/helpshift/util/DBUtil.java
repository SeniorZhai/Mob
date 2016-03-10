package com.helpshift.util;

import android.os.Environment;
import android.util.Log;
import com.helpshift.HelpshiftContext;
import com.helpshift.storage.IssuesDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DBUtil {
    private static final String TAG = "HelpShiftDebug";
    private static final String sdPath = (".backups/" + HelpshiftContext.getApplicationContext().getPackageName() + "/helpshift/databases/");

    public static void backupDatabase(String dbName) {
        if (doesDatabaseExist(dbName)) {
            try {
                File sd = Environment.getExternalStoragePublicDirectory(sdPath);
                if (!sd.exists()) {
                    sd.mkdirs();
                }
                if (sd.canWrite()) {
                    File srcDBPath = new File(HelpshiftContext.getApplicationContext().getDatabasePath(dbName).getPath());
                    File dstDBPath = new File(sd, dbName);
                    FileChannel src = new FileInputStream(srcDBPath).getChannel();
                    FileChannel dst = new FileOutputStream(dstDBPath).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "backupDatabase", e);
            }
        }
    }

    public static void restoreDatabaseBackup(String dbName) {
        if (!doesDatabaseExist(dbName)) {
            createDatabaseFolder();
            try {
                File sd = Environment.getExternalStoragePublicDirectory(sdPath);
                if (sd.canRead()) {
                    String currentDBPath = HelpshiftContext.getApplicationContext().getDatabasePath(dbName).getPath();
                    File srcDBPath = new File(sd, dbName);
                    File dstDBPath = new File(currentDBPath);
                    FileChannel src = new FileInputStream(srcDBPath).getChannel();
                    FileChannel dst = new FileOutputStream(dstDBPath).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "restoreDatabaseBackup", e);
            }
        }
    }

    private static void createDatabaseFolder() {
        IssuesDataSource.createDB();
    }

    private static boolean doesDatabaseExist(String dbName) {
        return HelpshiftContext.getApplicationContext().getDatabasePath(dbName).exists();
    }
}
