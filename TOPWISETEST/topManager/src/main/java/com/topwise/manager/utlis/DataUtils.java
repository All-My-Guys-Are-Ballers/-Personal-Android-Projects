package com.topwise.manager.utlis;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;

import com.topwise.manager.AppLog;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * 创建日期：2021/4/26 on 11:23
 * 描述:
 * 作者:wangweicheng
 */
public class DataUtils {
    private  static long sn = 0;
    public  static long getSerialNumber() {
        if(sn > 999999) {
            sn = 0;
        }
        return ++sn;
    }
    /**
     * 判断字符串是否为空 为空即true
     *
     * @param str 字符串
     * @return
     */
    public static boolean isNullString(@Nullable String str) {
        return str == null || str.length() == 0 || "null".equals(str);
    }
    /**
     * 判断对象是否为空
     *
     * @param obj 对象
     * @return {@code true}: 为空<br>{@code false}: 不为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String && obj.toString().length() == 0) {
            return true;
        }
        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return true;
        }
        if (obj instanceof Collection && ((Collection) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof Map && ((Map) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof SparseArray && ((SparseArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseBooleanArray && ((SparseBooleanArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseIntArray && ((SparseIntArray) obj).size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return obj instanceof SparseLongArray && ((SparseLongArray) obj).size() == 0;
        }
        return false;
    }

    /**
     * 判断字符串是否是整数
     */
    public static boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取交易时间
     *
     * @param type
     * @return
     */
    public static String getTransDateTime(int type) {
        Date date = new Date();
        String result = "";
        SimpleDateFormat format = null;
        switch (type) {
            case 0:
                format = new SimpleDateFormat("yyMMdd", Locale.CHINA);
                result = format.format(date);
                break;
            case 1:
                format = new SimpleDateFormat("hhmmss", Locale.CHINA);
                result = format.format(date);
                break;
            default:
                break;
        }
        AppLog.emvd("getTransDateTime: " + result);
        return result;
    }
}
