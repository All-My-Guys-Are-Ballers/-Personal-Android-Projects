package com.topwise.manager.emv.enums;

/**
 * 创建日期：2021/6/15 on 11:18
 * 描述:
 * 作者:wangweicheng
 */
public enum EOnlineResult {
    APPROVE((byte) 0), //联机批准
    FAILED((byte) 1),  //联机失败
    REFER((byte) 2),
    DENIAL((byte) 3), //联机拒绝
    ABORT((byte) 4); ////联机终止

    private byte onlineResult;

    EOnlineResult(byte onlineResult) {
        this.onlineResult = onlineResult;
    }

    public byte getOnlineResult() {
        return this.onlineResult;
    }

    public byte index() {
        return (byte)ordinal();
    }
}
