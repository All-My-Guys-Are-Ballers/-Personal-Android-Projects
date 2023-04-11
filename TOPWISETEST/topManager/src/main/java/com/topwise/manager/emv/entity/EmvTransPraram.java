package com.topwise.manager.emv.entity;

/**
 * 创建日期：2021/6/15 on 19:38
 * 描述:
 * 作者:wangweicheng
 */
public class EmvTransPraram {
    private byte transType;
    private String data;
    private String time;
    private boolean simple; //Is it a simple process
    private boolean havePin;//Force password  ture yes
    private boolean secnodGac ;//

    public boolean isSecnodGac() {
        return secnodGac;
    }

    public void setSecnodGac(boolean secnodGac) {
        this.secnodGac = secnodGac;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    public boolean isHavePin() {
        return havePin;
    }

    public void setHavePin(boolean havePin) {
        this.havePin = havePin;
    }

    /**
     * default
     * @param transType
     */
    public EmvTransPraram(byte transType) {
        this.transType = transType;
        this.simple = false;
        this.havePin = true;
        this.secnodGac = true;
    }

    public byte getTransType() {
        return transType;
    }

    public void setTransType(byte transType) {
        this.transType = transType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "EmvTransPraram{" +
                "transType=" + transType +
                ", data='" + data + '\'' +
                ", time='" + time + '\'' +
                ", simple=" + simple +
                ", havePin=" + havePin +
                ", secnodGac=" + secnodGac +
                '}';
    }
}
