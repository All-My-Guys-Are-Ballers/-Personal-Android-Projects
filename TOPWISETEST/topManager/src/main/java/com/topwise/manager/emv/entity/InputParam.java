package com.topwise.manager.emv.entity;

import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
import com.topwise.cloudpos.struct.BytesUtil;

import java.util.Arrays;

/**
 * 创建日期：2021/6/16 on 11:02
 * 描述:
 * 作者:wangweicheng
 */
public class InputParam {
    private byte kernType;
    private int selectLen;
    private byte[] selectData;
    private PreProcResult preProcResult;
    private TransParam transParam;
    private boolean havePin; //是否强制联机 ture 强制
    private boolean simple; //Is it a simple process

    public boolean isHavePin() {
        return havePin;
    }

    public void setHavePin(boolean havePin) {
        this.havePin = havePin;
    }

    public byte getKernType() {
        return kernType;
    }

    public void setKernType(byte kernType) {
        this.kernType = kernType;
    }

    public int getSelectLen() {
        return selectLen;
    }

    public void setSelectLen(int selectLen) {
        this.selectLen = selectLen;
    }

    public byte[] getSelectData() {
        return selectData;
    }

    public void setSelectData(byte[] selectData) {
        this.selectData = selectData;
    }

    public PreProcResult getPreProcResult() {
        return preProcResult;
    }

    public void setPreProcResult(PreProcResult preProcResult) {
        this.preProcResult = preProcResult;
    }

    public TransParam getTransParam() {
        return transParam;
    }

    public void setTransParam(TransParam transParam) {
        this.transParam = transParam;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    @Override
    public String toString() {
        return "InputParam{" +
                "kernType=" + kernType +
                ", selectLen=" + selectLen +
                ", selectData=" + Arrays.toString(selectData) +
                ", preProcResult=" + preProcResult +
                ", transParam=" + transParam +
                ", havePin=" + havePin +
                ", simple=" + simple +
                '}';
    }
}
