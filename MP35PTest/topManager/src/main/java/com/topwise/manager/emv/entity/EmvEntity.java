package com.topwise.manager.emv.entity;

import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.manager.emv.enums.EOnlineResult;

import java.util.Arrays;

/**
 * 创建日期：2021/6/11 on 14:39
 * 描述:
 * 作者:wangweicheng
 */
public class EmvEntity {
    private boolean result;
    private String pinData; //
    private EOnlineResult eOnlineResult = EOnlineResult.ABORT; //online Result
    byte[] authCode; //89 Authorisation Code
    byte[] authRespCode; //8A Authorisation Response Code
    byte[] issueAuthData; //91 Issuer Authentication Data
    byte[] issueScript71; //71 Issuer Script
    byte[] issueScript72; //72 Issuer Script

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getPinData() {
        return pinData;
    }

    public void setPinData(String pinData) {
        this.pinData = pinData;
    }

    public EOnlineResult geteOnlineResult() {
        return eOnlineResult;
    }

    public void seteOnlineResult(EOnlineResult eOnlineResult) {
        this.eOnlineResult = eOnlineResult;
    }

    public byte[] getAuthCode() {
        return authCode;
    }

    public void setAuthCode(byte[] authCode) {
        this.authCode = authCode;
    }

    public byte[] getAuthRespCode() {
        return authRespCode;
    }

    public void setAuthRespCode(byte[] authRespCode) {
        this.authRespCode = authRespCode;
    }

    public byte[] getIssueAuthData() {
        return issueAuthData;
    }

    public void setIssueAuthData(byte[] issueAuthData) {
        this.issueAuthData = issueAuthData;
    }

    public byte[] getIssueScript71() {
        return issueScript71;
    }

    public void setIssueScript71(byte[] issueScript71) {
        this.issueScript71 = issueScript71;
    }

    public byte[] getIssueScript72() {
        return issueScript72;
    }

    public void setIssueScript72(byte[] issueScript72) {
        this.issueScript72 = issueScript72;
    }

    @Override
    public String toString() {
        return "EmvEntity{" +
                "result=" + result +
                ", pinData='" + pinData + '\'' +
                ", eOnlineResult=" + eOnlineResult +
                ", authCode=" + BytesUtil.bytes2HexString(authCode) +
                ", authRespCode=" + BytesUtil.bytes2HexString(authRespCode) +
                ", issueAuthData=" + BytesUtil.bytes2HexString(issueAuthData) +
                ", issueScript71=" + BytesUtil.bytes2HexString(issueScript71) +
                ", issueScript72=" + BytesUtil.bytes2HexString(issueScript72) +
                '}';
    }
}
