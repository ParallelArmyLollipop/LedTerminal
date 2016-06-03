package com.shiki.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Maik on 2016/3/1.
 */
public class ApkUtils {
    public ApkUtils() {
    }

    public static void install(Activity activity, File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
        activity.startActivityForResult(intent, 0);
    }

    public static void uninstall(Activity activity, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent intent = new Intent("android.intent.action.DELETE", packageURI);
        activity.startActivityForResult(intent, 0);
    }

    public static boolean isAvilible(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List packageInfos = packageManager.getInstalledPackages(0);
        ArrayList packageNames = new ArrayList();
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); ++i) {
                String packName = ((PackageInfo) packageInfos.get(i)).packageName;
                packageNames.add(packName);
            }
        }

        return packageNames.contains(packageName);
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);// 根据包名,获取相关信息
            String versionName = packageInfo.versionName;// 版本名称
            // int versionCode = packageInfo.versionCode;// 版本号
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // 包名未找到异常
            e.printStackTrace();
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);// 根据包名,获取相关信息
            //String versionName = packageInfo.versionName;// 版本名称
            int versionCode = packageInfo.versionCode;// 版本号
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // 包名未找到异常
            e.printStackTrace();
        }
        return -1;
    }

    public static String getChannelFromApk(Context context, String channelPrefix) {
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        String key = "META-INF/" + channelPrefix;
        String ret = "";
        ZipFile zipfile = null;

        try {
            zipfile = new ZipFile(sourceDir);
            Enumeration split = zipfile.entries();

            while (split.hasMoreElements()) {
                ZipEntry channel = (ZipEntry) split.nextElement();
                String entryName = channel.getName();
                if (entryName.startsWith(key)) {
                    ret = entryName;
                    break;
                }
            }
        } catch (IOException var18) {
            var18.printStackTrace();
        } finally {
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException var17) {
                    var17.printStackTrace();
                }
            }

        }

        String[] split1 = ret.split(channelPrefix);
        String channel1 = "";
        if (split1 != null && split1.length >= 2) {
            channel1 = ret.substring(key.length());
        }

        return channel1;
    }
}
