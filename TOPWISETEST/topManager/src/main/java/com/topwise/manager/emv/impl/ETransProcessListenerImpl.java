package com.topwise.manager.emv.impl;

import com.topwise.cloudpos.aidl.emv.level2.EmvCandidateItem;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.emv.api.ITransProcessListener;
import com.topwise.manager.emv.entity.Amounts;
import com.topwise.manager.emv.entity.EmvEntity;

/**
 * 创建日期：2021/6/10 on 16:42
 * 描述:
 * 作者:wangweicheng
 */
public class ETransProcessListenerImpl implements ITransProcessListener {
    @Override
    public int requestAidSelect(String[] aids) {
        return 0;
    }

    @Override
    public TlvList requestKernalListTLV(byte kernelType) {
        return null;
    }

    @Override
    public void onUpdateEmvCandidateItem(EmvCandidateItem emvCandidateItem) {

    }

    @Override
    public void onUpdateKernelType(byte kernelType) {

    }

    @Override
    public boolean finalAidSelect() {
        return true;
    }

    @Override
    public boolean onConfirmCardInfo(String cardNo) {
        return false;
    }

    @Override
    public EmvEntity requestImportPin(int type, boolean lasttimeFlag, String amt) {
        return new EmvEntity();
    }

    @Override
    public boolean requestUserAuth(int certype, String certnumber) {
        return false;
    }

    @Override
    public EmvEntity onRequestOnline()  {
        return new EmvEntity();
    }

    @Override
    public Amounts requestImportAmount() {
        return new Amounts();
    }

    @Override
    public int onSecondCheckCard() {
        return 0;
    }

}
