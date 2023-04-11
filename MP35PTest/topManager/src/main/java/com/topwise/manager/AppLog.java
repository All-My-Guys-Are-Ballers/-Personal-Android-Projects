package com.topwise.manager;

import android.util.Log;

public class AppLog {

    /**
     * 控制android.util.Log.v是否输出
     */
    public static boolean DEBUG_V = true;
    /**
     * 控制android.util.Log.d是否输出
     */
    public static boolean DEBUG_D = true;
    /**
     * 控制android.util.Log.i是否输出
     */
    public static boolean DEBUG_I = true;
    /**
     * 控制android.util.Log.w是否输出
     */
    public static boolean DEBUG_W = true;
    /**
     * 控制android.util.Log.e是否输出
     */
    public static boolean DEBUG_E = true;

    public enum EDebugLevel {
        DEBUG_V,
        DEBUG_D,
        DEBUG_I,
        DEBUG_W,
        DEBUG_E,
    }

    /**
     * 同时控制V/D/I/W/E 5种输出开关
     *
     * @param debugFlag
     *            开关, true打开, false关闭
     */
    public static void debug(boolean debugFlag) {
        DEBUG_V = debugFlag;
        DEBUG_D = debugFlag;
        DEBUG_I = debugFlag;
        DEBUG_W = debugFlag;
        DEBUG_E = debugFlag;
    }

    /**
     * 分别控制V/D/I/W/E 5种输出开关
     *
     * @param debugFlag
     *            开关, true打开, false关闭
     */
    public static void debug(EDebugLevel debugLevel, boolean debugFlag) {
        if (debugLevel == EDebugLevel.DEBUG_V) {
            DEBUG_V = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_D) {
            DEBUG_D = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_I) {
            DEBUG_I = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_W) {
            DEBUG_W = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_E) {
            DEBUG_E = debugFlag;
        }
    }

    /**
     * 输出v级别log, 内部根据设置的开关决定是否真正输出log
     *
     * @param tag
     *            同系统android.util.log的tag定义
     * @param msg
     *            待输出的信息
     */
    public static void v(String tag, String msg) {
        if (DEBUG_V) {
            String[] infos = getAutoJumpLogInfos();
            Log.v(tag, infos[1] + infos[2] + "= " +msg );
        }
    }

    /**
     * 输出d级别log, 内部根据设置的开关决定是否真正输出log
     *
     * @param tag
     *            同系统android.util.log的tag定义
     * @param msg
     *            待输出的信息
     */
    public static void d(String tag, String msg) {
        if (DEBUG_D) {
            String[] infos = getAutoJumpLogInfos();
            Log.d(tag, infos[1] + infos[2] + "= " +msg );
        }
    }
    public static void emvd(String tag, String msg) {
        if (DEBUG_D) {
            String[] infos = getAutoJumpLogInfos();
            Log.d("emv=="+tag, infos[1] + infos[2] + "= " +msg );
        }
    }
    public static void emvd( String msg) {
        if (DEBUG_D) {
            String[] infos = getAutoJumpLogInfos();
            Log.d("emv==", infos[1] + infos[2] + "= " +msg );
        }
    }

    /**
     * 输出i级别log, 内部根据设置的开关决定是否真正输出log
     *
     * @param tag
     *            同系统android.util.log的tag定义
     * @param msg
     *            待输出的信息
     */
    public static void i(String tag, String msg) {
        if (DEBUG_I) {
            String[] infos = getAutoJumpLogInfos();
            Log.i(tag, infos[1] + infos[2] + "= " +msg );
        }
    }

    /**
     * 输出w级别log, 内部根据设置的开关决定是否真正输出log
     *
     * @param tag
     *            同系统android.util.log的tag定义
     * @param msg
     *            待输出的信息
     */
    public static void w(String tag, String msg) {
        if (DEBUG_W) {
            String[] infos = getAutoJumpLogInfos();
            Log.w(tag, infos[1] + infos[2] + "= " +msg );
        }
    }

    /**
     * 输出e级别log, 内部根据设置的开关决定是否真正输出log
     *
     * @param tag
     *            同系统android.util.log的tag定义
     * @param msg
     *            待输出的信息
     */
    public static void e(String tag, String msg) {
        if (DEBUG_E) {
            String[] infos = getAutoJumpLogInfos();
            Log.e(tag, infos[1] + infos[2] + "= " +msg );
        }
    }


    /**
     * 获取打印信息所在方法名，行号等信息
     * @return
     */
    private static String[] getAutoJumpLogInfos() {
        String[] infos = new String[] { "", "", "" };
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 5) {
            Log.e("AppLog", "Stack is too shallow!!!");
            return infos;
        } else {
            infos[0] = elements[4].getClassName().substring(
                    elements[4].getClassName().lastIndexOf(".") + 1);
            infos[1] = elements[4].getMethodName() + "()";
            infos[2] = " at (" + elements[4].getClassName() + ".java:"
                    + elements[4].getLineNumber() + ")";
            return infos;
        }
    }
//————————————————
//    版权声明：本文为CSDN博主「DylanAndroid」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/linglongxin24/article/details/37880997

}
