package com.shiki.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Maik on 2016/3/1.
 */
public class ExternalStorage {
    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";

    public ExternalStorage() {
    }

    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) || "mounted_ro".equals(state);
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state);
    }

    public static Map<String, File> getAllStorageLocations() {
        HashMap map = new HashMap(10);
        ArrayList mMounts = new ArrayList(10);
        ArrayList mVold = new ArrayList(10);
        mMounts.add("/mnt/sdcard");
        mVold.add("/mnt/sdcard");

        File mountHash;
        Scanner mount;
        String mount1;
        String[] root;
        String list;
        try {
            mountHash = new File("/proc/mounts");
            if (mountHash.exists()) {
                mount = new Scanner(mountHash);

                while (mount.hasNext()) {
                    mount1 = mount.nextLine();
                    if (mount1.startsWith("/dev/block/vold/")) {
                        root = mount1.split(" ");
                        list = root[1];
                        if (!list.equals("/mnt/sdcard")) {
                            mMounts.add(list);
                        }
                    }
                }
            }
        } catch (Exception var14) {
            var14.printStackTrace();
        }

        try {
            mountHash = new File("/system/etc/vold.fstab");
            if (mountHash.exists()) {
                mount = new Scanner(mountHash);

                while (mount.hasNext()) {
                    mount1 = mount.nextLine();
                    if (mount1.startsWith("dev_mount")) {
                        root = mount1.split(" ");
                        list = root[2];
                        if (list.contains(":")) {
                            list = list.substring(0, list.indexOf(":"));
                        }

                        if (!list.equals("/mnt/sdcard")) {
                            mVold.add(list);
                        }
                    }
                }
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        for (int var15 = 0; var15 < mMounts.size(); ++var15) {
            String var16 = (String) mMounts.get(var15);
            if (!mVold.contains(var16)) {
                mMounts.remove(var15--);
            }
        }

        mVold.clear();
        ArrayList var17 = new ArrayList(10);
        Iterator var18 = mMounts.iterator();

        while (true) {
            File var19;
            do {
                do {
                    do {
                        if (!var18.hasNext()) {
                            mMounts.clear();
                            if (map.isEmpty()) {
                                map.put("sdCard", Environment.getExternalStorageDirectory());
                            }

                            return map;
                        }

                        mount1 = (String) var18.next();
                        var19 = new File(mount1);
                    } while (!var19.exists());
                } while (!var19.isDirectory());
            } while (!var19.canWrite());

            File[] var20 = var19.listFiles();
            String hash = "[";
            if (var20 != null) {
                File[] key = var20;
                int var10 = var20.length;

                for (int var11 = 0; var11 < var10; ++var11) {
                    File f = key[var11];
                    hash = hash + f.getName().hashCode() + ":" + f.length() + ", ";
                }
            }

            hash = hash + "]";
            if (!var17.contains(hash)) {
                String var21 = "sdCard_" + map.size();
                if (map.size() == 0) {
                    var21 = "sdCard";
                } else if (map.size() == 1) {
                    var21 = "externalSdCard";
                }

                var17.add(hash);
                map.put(var21, var19);
            }
        }
    }
}
