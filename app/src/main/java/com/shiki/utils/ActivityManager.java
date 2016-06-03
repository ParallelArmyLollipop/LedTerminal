package com.shiki.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by Maik on 2016/3/1.
 */
public class ActivityManager {
    private static Stack<Activity> activityStack;
    private static ActivityManager instance;

    private ActivityManager() {
    }

    public static ActivityManager getActivityManager() {
        if (instance == null) {
            instance = new ActivityManager();
        }

        return instance;
    }

    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack();
        }

        activityStack.add(activity);
    }

    public Activity currentActivity() {
        if (activityStack == null) {
            return null;
        } else {
            Activity activity = (Activity) activityStack.lastElement();
            return activity;
        }
    }

    public void finishActivity() {
        if (activityStack != null) {
            Activity activity = (Activity) activityStack.lastElement();
            this.finishActivity(activity);
        }
    }

    public void finishActivity(Activity activity) {
        if (activityStack != null) {
            if (activity != null) {
                activityStack.remove(activity);
                activity.finish();
                activity = null;
            }

        }
    }

    public void finishActivity(Class<?> cls) {
        if (activityStack != null) {
            Iterator iterator = activityStack.iterator();

            while (iterator.hasNext()) {
                Activity activity = (Activity) iterator.next();
                if (activity != null && activity.getClass().equals(cls)) {
                    activity.finish();
                    iterator.remove();
                }
            }

        }
    }

    public void finishAllActivity() {
        if (activityStack != null) {
            Iterator iterator = activityStack.iterator();

            while (iterator.hasNext()) {
                Activity activity = (Activity) iterator.next();
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }

            activityStack.clear();
        }
    }

    public Activity getActivity(String activityName) {
        Iterator iterator = activityStack.iterator();

        Activity activity;
        do {
            if (!iterator.hasNext()) {
                return null;
            }

            activity = (Activity) iterator.next();
        }
        while (activity == null || !TextUtils.equals(activity.getClass().getName(), activityName));

        return activity;
    }

    public void appExit(Context context) {
        try {
            this.finishAllActivity();
            Process.killProcess(Process.myPid());
        } catch (Exception var3) {
            ;
        }

    }
}
