package com.example.mp35ptest.device;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.aidl.system.AidlSystem;
import com.topwise.emvtest.MyApplication;
import com.topwise.manager.AppLog;

/**
 * Creation date：2021/8/24 on 11:12
 * Describe:
 * Author:wangweicheng
 */
public class Device {
    private static final String TAG =   Device.class.getSimpleName();

    public static void enableHomeAndRecent(boolean i){
        try {
            AppLog.e(TAG,"enableHomeAndRecent i = " + i);
            AidlSystem systemManager = MyApplication.usdkManage.getSystem();
            boolean b = systemManager.enableHomeButton(i);
            AppLog.e(TAG,"enableHomeButton " + b);
            b = systemManager.enableRecentAppButton(i);
            AppLog.e(TAG,"enableRecentAppButton " + b);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Inject PIN BDK
     * @param pinValue
     * @param ksn
     * @return
     */
    public static boolean writeBdkPin(int index,byte[] pinValue,byte[] ksn){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            return pinpadManager.loadDukptBDK(index,pinValue,ksn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Inject DATA BDK
     * @param dataValue
     * @param ksn
     * @return
     */
    public static boolean writeBdkData(int index,byte[] dataValue,byte[] ksn){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            return pinpadManager.loadDukptBDK(index,dataValue,ksn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Automatically increase ksn before each use
     * @return
     */
    public static String autoAddPinKsn(){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            byte[] dukptKsn = pinpadManager.getDUKPTKsn(ConfiUtils.pinIndex, true);
            if (dukptKsn == null || dukptKsn.length == 0) return null;
            return MyApplication.convert.bcdToStr(dukptKsn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Automatically increase ksn before each use
     * @return
     */
    public static String autoAddDataKsn(){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            byte[] dukptKsn = pinpadManager.getDUKPTKsn(ConfiUtils.tdkIndex, true);
            if (dukptKsn == null || dukptKsn.length == 0) return null;
            return MyApplication.convert.bcdToStr(dukptKsn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean injectMain(int mMainKey, byte[] key){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            return pinpadManager.loadMainkey(mMainKey,key,null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean injectPIK(int KEYTYPE_PEK, int mMainKey, int mWorkKey, byte[] key){
        final AidlPinpad pinpadManager = MyApplication.usdkManage.getPinpad(0);
        try {
            return pinpadManager.loadWorkKey(KEYTYPE_PEK, mMainKey, mWorkKey,key,null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }
}
