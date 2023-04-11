package com.topwise.manager.emv.impl;

import android.content.Context;
import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.PCardLoadLog;
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.EmvKernelConfig;
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.ContactEmvProcess;
import com.topwise.manager.emv.ContactLessProcess;
import com.topwise.manager.emv.api.ITransProcessListener;
import com.topwise.manager.emv.api.IEmv;
import com.topwise.manager.emv.entity.EinputType;
import com.topwise.manager.emv.entity.EmvTransPraram;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;
import com.topwise.toptool.api.packer.ITlv;

import java.util.List;

/**
 * 创建日期：2021/6/10 on 16:26
 * 描述: 单例
 * 作者:wangweicheng
 */
public class TransProcess implements IEmv {

    private static final String TAG = TransProcess.class.getSimpleName();
    private static TransProcess instance;
    private static EinputType emvType;
    private final static String version = "version V1.0.0_20210610";
    private AidlEmvL2 emvL2 = TopUsdkManage.getInstance().getEmv();

    public TransProcess() {
        AppLog.d(TAG,version);
    }

    public static synchronized TransProcess getInstance(){
        if (instance == null){
            instance = new TransProcess();
        }
        return instance;
    }

    @Override
    public void init(EinputType einputType) {
        TransProcess.emvType = einputType;
        AppLog.d(TAG, "init===: EinputType" + einputType.toString());
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setProcessListener(ITransProcessListener ITransProcessListener) {
        AppLog.d(TAG, "setProcessListener===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.IC == TransProcess.emvType){
            ContactEmvProcess.getInstance(instance).setEmvProcessListener(ITransProcessListener);
        }else {
            ContactLessProcess.getInstance(instance).setRfProcessListener(ITransProcessListener);
        }
    }


    @Override
    public void setConfig(EmvKernelConfig emvKernelConfig, EmvTerminalInfo emvTerminalInfo) {
        AppLog.d(TAG, "setConfig===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.IC == TransProcess.emvType){
           ContactEmvProcess.getInstance(instance).setKernelConfig(emvKernelConfig,emvTerminalInfo);
        }
    }

    @Override
    public void setContactLessConfig(TransParam contactLessConfig,EmvTerminalInfo emvTerminalInfo) {
        AppLog.d(TAG, "setContactLessConfig===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.RF == TransProcess.emvType){
           ContactLessProcess.getInstance(instance).setConfig(contactLessConfig,emvTerminalInfo);
        }
    }

    @Override
    public int setContactlessParameter(TlvList tlvList) {
        return 0;
    }

    @Override
    public void setCapkList(List<EmvCapkParam> paramList) {
        AppLog.d(TAG, "setCapkList===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.IC == TransProcess.emvType){
            ContactEmvProcess.getInstance(instance).setEmvCapkParams(paramList);
        }else {
            ContactLessProcess.getInstance(instance).setEmvCapkParams(paramList);
        }
    }

    @Override
    public void setAidList(List<EmvAidParam> paramList) {
        AppLog.d(TAG, "setAidList===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.IC == TransProcess.emvType){
            ContactEmvProcess.getInstance(instance).setEmvAidParams(paramList);
        }else {
            ContactLessProcess.getInstance(instance).setEmvAidParams(paramList);
        }
    }

    @Override
    public byte[] getTlv(int paramInt) {
        try {
            AppLog.e(TAG,"getTlv TAG= " + paramInt );
            return emvL2.EMV_GetTLVData(paramInt);
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.e(TAG,"getTlv Exception TAG= " + paramInt + " Exception= " + e.getMessage());
            return null;
        }
    }

    @Override
    public void setTlv(int paramInt, byte[] paramArrayOfbyte) {
        try {
            emvL2.EMV_SetTLVData(paramInt,paramArrayOfbyte);
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.e(TAG,"setTlv Exception TAG= " + paramInt + " Exception= " + e.getMessage());
        }
    }

    @Override
    public EmvResult emvProcess(EmvTransPraram emvTransData) {
        AppLog.d(TAG, "emvProcess===: EinputType" + TransProcess.emvType.toString());
        if (EinputType.IC == TransProcess.emvType){
            EmvResult init = ContactEmvProcess.getInstance(instance).init();
            AppLog.d(TAG, "ContactEmvProcess===: init" + init.toString());
            if (init != EmvResult.OK){
                return init;
            }
            return ContactEmvProcess.getInstance(instance).emvProcess(emvTransData);
        }else {
            EmvResult init = ContactLessProcess.getInstance(instance).init();
            AppLog.d(TAG, "ContactLessProcess===: init " + init.toString());
            if (init != EmvResult.OK){
                return init;
            }
            return ContactLessProcess.getInstance(instance).emvProcess(emvTransData);
        }
    }

    @Override
    public TlvList readEcCurrencyBalance() {
        return null;
    }

    @Override
    public byte[] verifyPin(int paramInt, byte[] paramArrayOfbyte) {
        return new byte[0];
    }

    @Override
    public void onReadCardOffLineBalance(String moneyCode, String balance, String secondMoneyCode, String secondBalance) throws RemoteException {

    }

    @Override
    public EmvResult onReadCardTransLog(EmvTransPraram emvTransData, ITlv.ITlvDataObjList logs) throws RemoteException {
        if (EinputType.IC == TransProcess.emvType){
            return ContactEmvProcess.getInstance(instance).readCardTransLog(emvTransData,logs);
        }else {
//            return ContactLessProcess.getInstance(instance).emvProcess(emvTransData);
        }
        return null;
    }

    @Override
    public void onReadCardLoadLog(String atc, String checkCode, PCardLoadLog[] logs) throws RemoteException {

    }

}
