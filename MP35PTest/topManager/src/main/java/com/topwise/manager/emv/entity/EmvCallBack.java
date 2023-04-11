package com.topwise.manager.emv.entity;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.EmvCallback;

/**
 * 创建日期：2021/6/11 on 16:32
 * 描述:
 * 作者:wangweicheng
 */
public class EmvCallBack extends EmvCallback.Stub{
    @Override
    public int cGetOnlinePin(boolean b, byte[] bytes, int i, boolean[] booleans) throws RemoteException {
        return 0;
    }

    @Override
    public int cGetPlainTextPin(boolean b, byte[] bytes, int i, boolean[] booleans) throws RemoteException {
        return 0;
    }

    @Override
    public int cDisplayPinVerifyStatus(int i) throws RemoteException {
        return i;
    }

    @Override
    public int cCheckCredentials(int i, byte[] bytes, int i1, boolean[] booleans) throws RemoteException {
        return 0;
    }

    @Override
    public int cIssuerReferral(byte[] bytes, int i) throws RemoteException {
        return 0;
    }

    @Override
    public int cGetTransLogAmount(byte[] bytes, int i, int i1) throws RemoteException {
        return 0;
    }

    @Override
    public int cCheckExceptionFile(byte[] bytes, int i, int i1) throws RemoteException {
        return 0;
    }

    @Override
    public int cRFU1() throws RemoteException {
        return 0;
    }

    @Override
    public int cRFU2() throws RemoteException {
        return 0;
    }

    @Override
    public int cRFU3() throws RemoteException {
        return 0;
    }

    @Override
    public int cRFU4() throws RemoteException {
        return 0;
    }
}
