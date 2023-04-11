package com.example.mp35ptest.param;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Creation date：2021/8/27 on 13:39
 * Describe:
 * Author:wangweicheng
 */
public class SysParam {
    private static final String TAG =  SysParam.class.getSimpleName();
    private static SysParam mSysParam;
    private static Context mContext;
    public static synchronized SysParam getInstance(Context context) {
        mContext = context;
        if (mSysParam == null) {
            mSysParam = new SysParam();
            load();
        }
        return mSysParam;
    }
    public static String CUST_TID = "cust_id";
    public static String Cust_MID = "cust_mid";
    public static String Cust_LOGIN = "cust_login";
    public static String PUBLICKEY1 = "public_key1";
    public static String PUBLICKEY2 = "public_key2";
    public static String CUST_INVOICE= "cust_invoice";
    public static String Cust_BATCHNR = "cust_batchnr";
    public static String Cust_STAN = "cust_stan";
    public static String Is_Login = "is_login";
    public static String Is_firstRun = "Is_firstRun";
    public static String key_type = "key_type";

    private static boolean isParamFileExist() {
        String dir = "/data/data/" + mContext.getPackageName() + File.separator + "shared_prefs/"
                + mContext.getPackageName() + "_preferences.xml";
        File file = new File(dir);
        if (file.exists()) {
            return true;
        }
        return false;
    }
    /**
     * Initialization parameters for the first run
     */
    private static void load(){
        if (isParamFileExist()) {
            Log.d(TAG,"sysparm haved ==");
            return;
        }
        Log.d(TAG,"load default parameters===");
        // 设置默认参数值
        Set<String> set = new HashSet<String>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (editor != null){
            editor.putString(CUST_TID, "6006010000");
            editor.putString(Is_Login, Constant.NO);
            editor.putString(Is_firstRun, Constant.NO);
            editor.putString(key_type, "DUKPT");
            //TODO
            editor.commit();
        }
    }

    private static Set<String> stringKeyMap = new HashSet<String>(){
        private static final long serialVersionUID = 1L;

        {
            add(CUST_TID); //
            add(Cust_MID); //
            add(Cust_LOGIN); //

            add(PUBLICKEY1); //
            add(PUBLICKEY2); //
            add(CUST_INVOICE); //
            add(Cust_BATCHNR); //
            add(Cust_STAN); //
            add(Is_Login); //
            add(Is_firstRun); //

            add(key_type);
        }
    };
    public synchronized String get(String name){
        String value = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (stringKeyMap.contains(name)) {
            value = sharedPreferences.getString(name, "");
        }
        return value;
    }
    public synchronized void set(String name, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (stringKeyMap.contains(name)) {
            editor.putString(name, value);
        }
        editor.commit();
    }

    public static class Constant{
        /**
         *
         */
        public static final String YES = "Y";
        //
        public static final String NO = "N";

    }
}
