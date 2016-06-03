package com.shiki.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Maik on 2016/3/1.
 */
public final class StorageUtils {
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "cache";

    private StorageUtils() {
    }

    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true, "cache");
    }

    public static File getCacheDirectory(Context context, boolean isCache, String customDir) {
        return getCacheDirectory(context, true, isCache, customDir);
    }

    public static File getCacheDirectory(Context context, boolean preferExternal, boolean cache, String customDir) {
        File appCacheDir = null;

        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException var7) {
            externalStorageState = "";
        } catch (IncompatibleClassChangeError var8) {
            externalStorageState = "";
        }

        if (preferExternal && "mounted".equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context, cache, customDir);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/" + customDir + "/";
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    public static File getIndividualCacheDirectory(Context context) {
        return getIndividualCacheDirectory(context, "cache");
    }

    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists() && !individualCacheDir.mkdir()) {
            individualCacheDir = appCacheDir;
        }

        return individualCacheDir;
    }

    public static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if ("mounted".equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }

        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }

        return appCacheDir;
    }

    public static File getOwnCacheDirectory(Context context, String cacheDir, boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal && "mounted".equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }

        if (appCacheDir == null || !appCacheDir.exists() && !appCacheDir.mkdirs()) {
            appCacheDir = context.getCacheDir();
        }

        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context, boolean cache, String customDir) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), customDir);
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }

            try {
                (new File(appCacheDir, ".nomedia")).createNewFile();
            } catch (IOException var6) {
                ;
            }
        }

        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        return perm == 0;
    }
}
