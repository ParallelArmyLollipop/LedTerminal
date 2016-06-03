package com.shiki.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Maik on 2016/3/1.
 */
public class DateUtils {
    public static Date date = null;
    public static DateFormat dateFormat = null;
    public static Calendar calendar = null;

    public static Date parseDate(String dateStr, String format) {
        try {
            dateFormat = new SimpleDateFormat(format);
            String dt = dateStr.replaceAll("-", "/");
            if ((!dt.equals("")) && (dt.length() < format.length())) {
                dt = dt + format.substring(dt.length()).replaceAll("[YyMmDdHhSs]", "0");
            }

            date = dateFormat.parse(dt);
        } catch (Exception localException) {
        }
        return date;
    }

    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, "yyyy/MM/dd");
    }

    public static String format(Date date, String format) {
        String result = "";
        try {
            if (date != null) {
                dateFormat = new SimpleDateFormat(format);
                result = dateFormat.format(date);
            }
        } catch (Exception localException) {
        }
        return result;
    }

    public static String format(Date date) {
        return format(date, "yyyy/MM/dd");
    }

    public static int getYear(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(1);
    }

    public static int getMonth(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(2) + 1;
    }

    public static int getDay(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(5);
    }

    public static int getHour(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(11);
    }

    public static int getMinute(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(12);
    }

    public static int getSecond(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(13);
    }

    public static long getMillis(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getTimeInMillis();
    }

    public static String getDate(Date date) {
        return format(date, "yyyy/MM/dd");
    }

    public static String getTime(Date date) {
        return format(date, "HH:mm:ss");
    }

    public static String getDateTime(Date date) {
        return format(date, "yyyy/MM/dd HH:mm:ss");
    }

    public static Date addDate(Date date, int day) {
        calendar = Calendar.getInstance();
        long millis = getMillis(date) + day * 24L * 3600L * 1000L;
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }

    public static int diffDate(Date date, Date date1) {
        return (int) ((getMillis(date) - getMillis(date1)) / 86400000L);
    }

    public static String getMonthBegin(String strdate) {
        date = parseDate(strdate);
        return format(date, "yyyy-MM") + "-01";
    }

    public static String getMonthEnd(String strdate) {
        date = parseDate(getMonthBegin(strdate));
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(2, 1);
        calendar.add(6, -1);
        return formatDate(calendar.getTime());
    }

    public static String formatDate(Date date) {
        return formatDateByFormat(date, "yyyy-MM-dd");
    }

    public static String formatDateByFormat(Date date, String format) {
        String result = "";
        if (date != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                result = sdf.format(date);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String getMonthDayWeek(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(1);
        int month = c.get(2) + 1;
        int day = c.get(5);
        int week = c.get(7);

        String weekStr = null;

        switch (week) {
            case 1:
                weekStr = "周日";
                break;
            case 2:
                weekStr = "周一";
                break;
            case 3:
                weekStr = "周二";
                break;
            case 4:
                weekStr = "周三";
                break;
            case 5:
                weekStr = "周四";
                break;
            case 6:
                weekStr = "周五";
                break;
            case 7:
                weekStr = "周六";
        }

        return month + "月" + day + "日" + "(" + weekStr + ")";
    }

    public static Date formatStringByFormat(String date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTimeInterval(String d) {
        Date date = formatStringByFormat(d, "yyyy-MM-dd HH:mm:ss");
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        int nowYear = now.get(1);
        int nowMonth = now.get(2);
        int nowWeek = now.get(4);
        int nowDay = now.get(7);
        int nowHour = now.get(11);
        int nowMinute = now.get(12);

        Calendar ca = Calendar.getInstance();
        if (date != null)
            ca.setTime(date);
        else
            ca.setTime(new Date());
        int year = ca.get(1);
        int month = ca.get(2);
        int week = ca.get(4);
        int day = ca.get(7);
        int hour = ca.get(11);
        int minute = ca.get(12);
        if (year != nowYear) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            return sdf.format(date);
        }
        if (month != nowMonth) {
            SimpleDateFormat sdf = new SimpleDateFormat("M月dd日");
            return sdf.format(date);
        }
        if (week != nowWeek) {
            SimpleDateFormat sdf = new SimpleDateFormat("M月dd日");
            return sdf.format(date);
        }
        if (day != nowDay) {
            if (day + 1 == nowDay) {
                return "昨天" + formatDateByFormat(date, "HH:mm");
            }
            if (day + 2 == nowDay) {
                return "前天" + formatDateByFormat(date, "HH:mm");
            }

            SimpleDateFormat sdf = new SimpleDateFormat("M月dd日");
            return sdf.format(date);
        }

        int hourGap = nowHour - hour;
        if (hourGap == 0) {
            if (nowMinute - minute < 1) {
                return "刚刚";
            }
            return nowMinute - minute + "分钟前";
        }
        if ((hourGap >= 1) && (hourGap <= 12)) {
            return hourGap + "小时前";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("今天 HH:mm");
        return sdf.format(date);
    }

    public static String reformatTime(String date, String pattern) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simple = new SimpleDateFormat(pattern);
        Date old = parseToDate(date, fmt);
        return simple.format(old);
    }

    public static Date parseToDate(String dateString, String pattern) {
        if ((pattern == null) || ("".equals(pattern))) {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }

        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            return formatter.parse(dateString);
        } catch (ParseException localParseException) {
        }
        return new Date();
    }

    public static String getTimeInterval(Date d) {
        String date = format(d, "yyyy-MM-dd HH:mm:ss");
        return getTimeInterval(date);
    }
}
