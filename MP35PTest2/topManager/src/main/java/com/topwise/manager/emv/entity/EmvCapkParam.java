package com.topwise.manager.emv.entity;

/**
 * 创建日期：2021/4/21 on 9:30
 * 描述:
 * 作者:wangweicheng
 */
public class EmvCapkParam {
    private String RIDKeyID;
    // 应用注册服务商ID
    private String RID;
    // 密钥索引
    private int KeyID;
    // HASH算法标志
    private int HashInd;
    // RSA算法标志
    private int arithInd;
    // 模
    private String modul;
    // 指数
    private String Exponent;
    // 有效期(YYMMDD)
    private String expDate;
    // 密钥校验和
    private String checkSum;

    public String getRIDKeyID() {
        return RIDKeyID;
    }

    public void setRIDKeyID(String RIDKeyID) {
        this.RIDKeyID = RIDKeyID;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public int getKeyID() {
        return KeyID;
    }

    public void setKeyID(int keyID) {
        KeyID = keyID;
    }

    public int getHashInd() {
        return HashInd;
    }

    public void setHashInd(int hashInd) {
        HashInd = hashInd;
    }

    public int getArithInd() {
        return arithInd;
    }

    public void setArithInd(int arithInd) {
        this.arithInd = arithInd;
    }

    public String getModul() {
        return modul;
    }

    public void setModul(String modul) {
        this.modul = modul;
    }

    public String getExponent() {
        return Exponent;
    }

    public void setExponent(String exponent) {
        Exponent = exponent;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    @Override
    public String toString() {
        return "EmvCapkParam{" +
                "RIDKeyID='" + RIDKeyID + '\'' +
                ", RID='" + RID + '\'' +
                ", KeyID=" + KeyID +
                ", HashInd=" + HashInd +
                ", arithInd=" + arithInd +
                ", modul='" + modul + '\'' +
                ", Exponent='" + Exponent + '\'' +
                ", expDate='" + expDate + '\'' +
                ", checkSum='" + checkSum + '\'' +
                '}';
    }
}
