package com.shiki.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maik on 2016/3/1.
 */
public class StringUtils {
    public StringUtils() {
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0) && isBlank(str);
    }

    public static int length(CharSequence str) {
        return str == null ? 0 : str.length();
    }

    public static String format(String str) {
        return str == null ? "" : str.trim();
    }

    public static String nullStrToEmpty(Object str) {
        return str == null ? "" : (str instanceof String ? (String) str : str.toString());
    }

    public static String nullStrToEmpty(String str) {
        return str == null ? "" : str;
    }

    public static String capitalizeFirstLetter(String str) {
        if (isEmpty(str)) {
            return str;
        } else {
            char c = str.charAt(0);
            return Character.isLetter(c) && !Character.isUpperCase(c) ? (new StringBuilder(str.length())).append(Character.toUpperCase(c)).append(str.substring(1)).toString() : str;
        }
    }

    public static String utf8Encode(String str) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException var2) {
                throw new RuntimeException("UnsupportedEncodingException occurred. ", var2);
            }
        } else {
            return str;
        }
    }

    public static String utf8Encode(String str, String defultReturn) {
        if (!isEmpty(str) && str.getBytes().length != str.length()) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException var3) {
                return defultReturn;
            }
        } else {
            return str;
        }
    }

    public static String getHrefInnerHtml(String href) {
        if (isEmpty(href)) {
            return "";
        } else {
            String hrefReg = ".*<[\\s]*a[\\s]*.*>(.+?)<[\\s]*/a[\\s]*>.*";
            Pattern hrefPattern = Pattern.compile(hrefReg, 2);
            Matcher hrefMatcher = hrefPattern.matcher(href);
            return hrefMatcher.matches() ? hrefMatcher.group(1) : href;
        }
    }

    public static String htmlEscapeCharsToString(String source) {
        return isEmpty(source) ? source : source.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
    }

    public static String fullWidthToHalfWidth(String s) {
        if (isEmpty(s)) {
            return s;
        } else {
            char[] source = s.toCharArray();

            for (int i = 0; i < source.length; ++i) {
                if (source[i] == 12288) {
                    source[i] = 32;
                } else if (source[i] >= '！' && source[i] <= '～') {
                    source[i] -= 'ﻠ';
                } else {
                    source[i] = source[i];
                }
            }

            return new String(source);
        }
    }

    public static String halfWidthToFullWidth(String s) {
        if (isEmpty(s)) {
            return s;
        } else {
            char[] source = s.toCharArray();

            for (int i = 0; i < source.length; ++i) {
                if (source[i] == 32) {
                    source[i] = 12288;
                } else if (source[i] >= 33 && source[i] <= 126) {
                    source[i] += 'ﻠ';
                } else {
                    source[i] = source[i];
                }
            }

            return new String(source);
        }
    }

    public static String sqliteEscape(String keyWord) {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("\'", "\'\'");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }
}
