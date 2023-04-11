package com.topwise.manager.emv.entity;

/**
 * 创建日期：2021/4/26 on 14:20
 * 描述:
 * 作者:wangweicheng
 */
public class Amounts {
    private int retCode = 0;
    //9f02
    private String transAmount = "000000000015";
    //9f03
    private String cashBackAmount = "000000000000";

    public int getRetCode() {
        return this.retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getTransAmount() {
        return this.transAmount;
    }

    public void setTransAmount(String transAmount) {
        this.transAmount = transAmount;
    }

    public String getCashBackAmount() {
        return this.cashBackAmount;
    }

    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }

    @Override
    public String toString() {
        return "Amounts{" +
                "retCode=" + retCode +
                ", transAmount='" + transAmount + '\'' +
                ", cashBackAmount='" + cashBackAmount + '\'' +
                '}';
    }
}
