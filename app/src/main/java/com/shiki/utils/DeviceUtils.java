package com.shiki.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.os.Build.VERSION;
import android.provider.MediaStore.Images.Media;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Maik on 2016/3/1.
 */
public class DeviceUtils {
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_CLASS_2_G = 2;
    public static final int NETWORK_CLASS_3_G = 3;
    public static final int NETWORK_CLASS_4_G = 4;

    public DeviceUtils() {
    }

    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getLocalIPAddress() {
        try {
            Enumeration ex = NetworkInterface.getNetworkInterfaces();

            while (ex.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) ex.nextElement();
                Enumeration enumIpAddr = intf.getInetAddresses();

                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }

            return "0.0.0.0";
        } catch (SocketException var4) {
            return "0.0.0.0";
        }
    }

    public static String getExternalStorageDirectory() {
        Map map = System.getenv();
        String[] values = new String[map.values().size()];
        map.values().toArray(values);
        String path = values[values.length - 1];
        return path.startsWith("/mnt/") && !Environment.getExternalStorageDirectory().getAbsolutePath().equals(path) ? path : null;
    }

    public static long getAvailaleSize() {
        if (!existSDCard()) {
            return 0L;
        } else {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = (long) stat.getBlockSize();
            long availableBlocks = (long) stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }
    }

    public static long getAllSize() {
        if (!existSDCard()) {
            return 0L;
        } else {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = (long) stat.getBlockSize();
            long availableBlocks = (long) stat.getBlockCount();
            return availableBlocks * blockSize;
        }
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager e = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = e.getActiveNetworkInfo();
            return ni != null ? ni.isConnectedOrConnecting() : false;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List serviceList = activityManager.getRunningServices(2147483647);
        if (serviceList.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < serviceList.size(); ++i) {
                if (((RunningServiceInfo) serviceList.get(i)).service.getClassName().equals(className)) {
                    isRunning = true;
                    break;
                }
            }

            return isRunning;
        }
    }

    public static boolean isProessRunning(Context context, String proessName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List lists = am.getRunningAppProcesses();
        Iterator var5 = lists.iterator();

        RunningAppProcessInfo info;
        do {
            if (!var5.hasNext()) {
                return isRunning;
            }

            info = (RunningAppProcessInfo) var5.next();
        } while (!info.processName.equals(proessName));

        isRunning = true;
        return isRunning;
    }

    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (StringUtils.isEmpty(imei)) {
            imei = "";
        }

        return imei;
    }

    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        if (StringUtils.isEmpty(mac)) {
            mac = "";
        }

        return mac;
    }

    public static String getUDID(Context context) {
        String udid = Secure.getString(context.getContentResolver(), "android_id");
        if (StringUtils.isEmpty(udid) || udid.equals("9774d56d682e549c") || udid.length() < 15) {
            SecureRandom random = new SecureRandom();
            udid = (new BigInteger(64, random)).toString(16);
        }

        if (StringUtils.isEmpty(udid)) {
            udid = "";
        }

        return udid;
    }

    public static void vibrate(Context context, long duration) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = new long[]{0L, duration};
        vibrator.vibrate(pattern, -1);
    }

    public static String getLatestCameraPicture(Context context) {
        if (!existSDCard()) {
            return null;
        } else {
            String[] projection = new String[]{"_id", "_data", "bucket_display_name", "datetaken", "mime_type"};
            Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, projection, (String) null, (String[]) null, "datetaken DESC");
            if (cursor.moveToFirst()) {
                String path = cursor.getString(1);
                return path;
            } else {
                return null;
            }
        }
    }

    public static DisplayMetrics getScreenPix(Activity activity) {
        DisplayMetrics displaysMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
        return displaysMetrics;
    }

    @TargetApi(11)
    public static void coptyToClipBoard(Context context, String content) {
        int currentapiVersion = VERSION.SDK_INT;
        if (currentapiVersion >= 11) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", content);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard1 = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard1.setText(content);
        }

    }

    public static List<String> getAppPackageNamelist(Context context) {
        ArrayList packList = new ArrayList();
        PackageManager pm = context.getPackageManager();
        List packinfos = pm.getInstalledPackages(0);
        Iterator var4 = packinfos.iterator();

        while (var4.hasNext()) {
            PackageInfo packinfo = (PackageInfo) var4.next();
            String packname = packinfo.packageName;
            packList.add(packname);
        }

        return packList;
    }

    public static boolean isAppInstall(Context context, String packageName) {
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

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5F);
    }

    /**
    public boolean isSoftKeyAvail(Activity activity) {
        final boolean[] isSoftkey = new boolean[]{false};
        final View activityRootView = activity.getWindow().getDecorView().findViewById(16908290);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int rootViewHeight = activityRootView.getRootView().getHeight();
                int viewHeight = activityRootView.getHeight();
                int heightDiff = rootViewHeight - viewHeight;
                if (heightDiff > 100) {
                    isSoftkey[0] = true;
                }

            }
        });
        return isSoftkey[0];
    }
     **/

    public static int getStatusBarHeight(Context context) {
        int height = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = context.getResources().getDimensionPixelSize(resourceId);
        }

        return height;
    }

    public static int getNavigationBarHeight(Context context) {
        int height = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            height = resources.getDimensionPixelSize(resourceId);
        }

        return height;
    }

    /**
    public static int getTopBarHeight(Activity activity) {
        return activity.getWindow().findViewById(16908290).getTop();
    }
     **/

    @SuppressLint({"NewApi"})
    public static boolean startActivityForPackage(Context context, String packageName) {
        PackageInfo pi = null;

        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (NameNotFoundException var10) {
            var10.printStackTrace();
            return false;
        }

        Intent resolveIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        resolveIntent.addCategory("android.intent.category.LAUNCHER");
        resolveIntent.setFlags(131072);
        resolveIntent.setPackage(pi.packageName);
        List apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
        ResolveInfo ri = (ResolveInfo) apps.iterator().next();
        if (ri != null) {
            String packageName1 = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setFlags(270532608);
            ComponentName cn = new ComponentName(packageName1, className);
            intent.setComponent(cn);
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public static void hideInputSoftFromWindowMethod(Context context, View view) {
        try {
            InputMethodManager e = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            e.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static void showInputSoftFromWindowMethod(Context context, View view) {
        try {
            InputMethodManager e = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            e.showSoftInput(view, 2);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static boolean isActiveSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    public static void goHome(Context context) {
        Intent mHomeIntent = new Intent("android.intent.action.MAIN");
        mHomeIntent.addCategory("android.intent.category.HOME");
        mHomeIntent.addFlags(270532608);
        context.startActivity(mHomeIntent);
    }

    public static int getPhoneType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getPhoneType();
    }

    public static int getNetType(Context context) {
        byte netWorkType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == 1) {
                netWorkType = 1;
            } else if (type == 0) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                switch (telephonyManager.getNetworkType()) {
                    case 1:
                    case 2:
                    case 4:
                    case 7:
                    case 11:
                        return 2;
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                    case 9:
                    case 10:
                    case 12:
                    case 14:
                    case 15:
                        return 3;
                    case 13:
                        return 4;
                    default:
                        return 0;
                }
            }
        }

        return netWorkType;
    }

    public static void callPhone(Context context, String phoneNumber) {
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNumber)));
    }

    public static void callDial(Context context, String phoneNumber) {
        context.startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:" + phoneNumber)));
    }

    public static void sendSms(Context context, String phoneNumber, String content) {
        Uri uri = Uri.parse("smsto:" + (TextUtils.isEmpty(phoneNumber) ? "" : phoneNumber));
        Intent intent = new Intent("android.intent.action.SENDTO", uri);
        intent.putExtra("sms_body", TextUtils.isEmpty(content) ? "" : content);
        context.startActivity(intent);
    }

    public static boolean isPhone(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony.getPhoneType() != 0;
    }
}
