package com.topwise.manager.emv.entity;

import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.cloudpos.struct.TlvList;

/**
 * 创建日期：2021/4/21 on 9:29
 * 描述:
 * 作者:wangweicheng
 */
public class EmvAidParam {
    /**
     * aid, 应用标志
     */
    private String aid;
    /**
     * 选择标志(PART_MATCH 部分匹配 FULL_MATCH 全匹配)
     */
    private int selFlag;
    /**
     * 终端联机PIN支持能力
     */
    private int onlinePin;
    /**
     * 电子现金终端交易限额(9F7F)
     */
    private long ecTTLVal;
    /**
     * 读卡器非接触CVM限制(DF21)
     */
    private long rdCVMLmt;
    /**
     * 读卡器非接触交易限额(DF20)
     */
    private long rdClssTxnLmt;
    /**
     * 读卡器非接触脱机最低限额(DF19)
     */
    private long rdClssFLmt;
    /**
     * TTL存在? 1-存在 电子现金终端交易限额（EC Terminal Transaction Limit）(9F7B)
     */
    private int ecTTLFlg;
    /**
     * 是否存在读卡器非接触脱机最低限额
     */
    private int rdClssFLmtFlg;
    /**
     * 是否存在读卡器非接触交易限额
     */
    private int rdClssTxnLmtFlg;
    /**
     * 是否存在读卡器非接触CVM限额
     */
    private int rdCVMLmtFlg;

    /**
     * 目标百分比数
     */
    private int targetPer;
    /**
     * 最大目标百分比数
     */
    private int maxTargetPer;
    /**
     * 是否检查最低限额
     */
    private int floorlimitCheck;
    /**
     * 是否进行随机交易选择
     */
    private int randTransSel;
    /**
     * 是否进行频度检测
     */
    private int velocityCheck;
    /**
     * 最低限额
     */
    private long floorLimit;
    /**
     * 阀值
     */
    private long threshold;
    /**
     * 终端行为代码(拒绝)
     */
    private String tacDenial;
    /**
     * 终端行为代码(联机)
     */
    private String tacOnline;
    /**
     * 终端行为代码(缺省)
     */
    private String tbcDefualt;
    /**
     * 收单行标志־
     */
    private String acquierId;
    /**
     * 终端缺省DDOL
     */
    private String dDOL;
    /**
     * 终端缺省TDOL
     */
    private String tDOL;
    /**
     * 应用版本
     */
    private String version;
    /**
     * 风险管理数据
     */
    private String riskmanData;


    //9F4E s merchName
    private String merchName;
    //9F15 s merchCateCode
    private String merchCateCode;
    //9F16 s merchId
    private String merchId;
    //9F1C s termId
    private String termId;
    //5F2A s transCurrCode
    private String transCurrCode;
    //5F36 i transCurrExp
    private int transCurrExp;
    //9F3C s referCurrCode
    private String referCurrCode;
    //9F3D byte referCurrExp
    private int referCurrExp;
    //DF8101 int referCurrCon
    private int referCurrCon;

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getSelFlag() {
        return selFlag;
    }

    public void setSelFlag(int selFlag) {
        this.selFlag = selFlag;
    }

    public int getOnlinePin() {
        return onlinePin;
    }

    public void setOnlinePin(int onlinePin) {
        this.onlinePin = onlinePin;
    }

    public long getEcTTLVal() {
        return ecTTLVal;
    }

    public void setEcTTLVal(long ecTTLVal) {
        this.ecTTLVal = ecTTLVal;
    }

    public long getRdCVMLmt() {
        return rdCVMLmt;
    }

    public void setRdCVMLmt(long rdCVMLmt) {
        this.rdCVMLmt = rdCVMLmt;
    }

    public long getRdClssTxnLmt() {
        return rdClssTxnLmt;
    }

    public void setRdClssTxnLmt(long rdClssTxnLmt) {
        this.rdClssTxnLmt = rdClssTxnLmt;
    }

    public long getRdClssFLmt() {
        return rdClssFLmt;
    }

    public void setRdClssFLmt(long rdClssFLmt) {
        this.rdClssFLmt = rdClssFLmt;
    }

    public int getEcTTLFlg() {
        return ecTTLFlg;
    }

    public void setEcTTLFlg(int ecTTLFlg) {
        this.ecTTLFlg = ecTTLFlg;
    }

    public int getRdClssFLmtFlg() {
        return rdClssFLmtFlg;
    }

    public void setRdClssFLmtFlg(int rdClssFLmtFlg) {
        this.rdClssFLmtFlg = rdClssFLmtFlg;
    }

    public int getRdClssTxnLmtFlg() {
        return rdClssTxnLmtFlg;
    }

    public void setRdClssTxnLmtFlg(int rdClssTxnLmtFlg) {
        this.rdClssTxnLmtFlg = rdClssTxnLmtFlg;
    }

    public int getRdCVMLmtFlg() {
        return rdCVMLmtFlg;
    }

    public void setRdCVMLmtFlg(int rdCVMLmtFlg) {
        this.rdCVMLmtFlg = rdCVMLmtFlg;
    }

    public int getTargetPer() {
        return targetPer;
    }

    public void setTargetPer(int targetPer) {
        this.targetPer = targetPer;
    }

    public int getMaxTargetPer() {
        return maxTargetPer;
    }

    public void setMaxTargetPer(int maxTargetPer) {
        this.maxTargetPer = maxTargetPer;
    }

    public int getFloorlimitCheck() {
        return floorlimitCheck;
    }

    public void setFloorlimitCheck(int floorlimitCheck) {
        this.floorlimitCheck = floorlimitCheck;
    }

    public int getRandTransSel() {
        return randTransSel;
    }

    public void setRandTransSel(int randTransSel) {
        this.randTransSel = randTransSel;
    }

    public int getVelocityCheck() {
        return velocityCheck;
    }

    public void setVelocityCheck(int velocityCheck) {
        this.velocityCheck = velocityCheck;
    }

    public long getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(long floorLimit) {
        this.floorLimit = floorLimit;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public String getTacDenial() {
        return tacDenial;
    }

    public void setTacDenial(String tacDenial) {
        this.tacDenial = tacDenial;
    }

    public String getTacOnline() {
        return tacOnline;
    }

    public void setTacOnline(String tacOnline) {
        this.tacOnline = tacOnline;
    }

    public String getTbcDefualt() {
        return tbcDefualt;
    }

    public void setTbcDefualt(String tbcDefualt) {
        this.tbcDefualt = tbcDefualt;
    }

    public String getAcquierId() {
        return acquierId;
    }

    public void setAcquierId(String acquierId) {
        this.acquierId = acquierId;
    }

    public String getdDOL() {
        return dDOL;
    }

    public void setdDOL(String dDOL) {
        this.dDOL = dDOL;
    }

    public String gettDOL() {
        return tDOL;
    }

    public void settDOL(String tDOL) {
        this.tDOL = tDOL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRiskmanData() {
        return riskmanData;
    }

    public void setRiskmanData(String riskmanData) {
        this.riskmanData = riskmanData;
    }

    public String getMerchName() {
        return merchName;
    }

    public void setMerchName(String merchName) {
        this.merchName = merchName;
    }

    public String getMerchCateCode() {
        return merchCateCode;
    }

    public void setMerchCateCode(String merchCateCode) {
        this.merchCateCode = merchCateCode;
    }

    public String getMerchId() {
        return merchId;
    }

    public void setMerchId(String merchId) {
        this.merchId = merchId;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getTransCurrCode() {
        return transCurrCode;
    }

    public void setTransCurrCode(String transCurrCode) {
        this.transCurrCode = transCurrCode;
    }

    public int getTransCurrExp() {
        return transCurrExp;
    }

    public void setTransCurrExp(int transCurrExp) {
        this.transCurrExp = transCurrExp;
    }

    public String getReferCurrCode() {
        return referCurrCode;
    }

    public void setReferCurrCode(String referCurrCode) {
        this.referCurrCode = referCurrCode;
    }

    public int getReferCurrExp() {
        return referCurrExp;
    }

    public void setReferCurrExp(int referCurrExp) {
        this.referCurrExp = referCurrExp;
    }

    public int getReferCurrCon() {
        return referCurrCon;
    }

    public void setReferCurrCon(int referCurrCon) {
        this.referCurrCon = referCurrCon;
    }

    @Override
    public String toString() {
        return "EmvAidParam{" +
                "aid='" + aid + '\'' +
                ", selFlag=" + selFlag +
                ", onlinePin=" + onlinePin +
                ", ecTTLVal=" + ecTTLVal +
                ", rdCVMLmt=" + rdCVMLmt +
                ", rdClssTxnLmt=" + rdClssTxnLmt +
                ", rdClssFLmt=" + rdClssFLmt +
                ", ecTTLFlg=" + ecTTLFlg +
                ", rdClssFLmtFlg=" + rdClssFLmtFlg +
                ", rdClssTxnLmtFlg=" + rdClssTxnLmtFlg +
                ", rdCVMLmtFlg=" + rdCVMLmtFlg +
                ", targetPer=" + targetPer +
                ", maxTargetPer=" + maxTargetPer +
                ", floorlimitCheck=" + floorlimitCheck +
                ", randTransSel=" + randTransSel +
                ", velocityCheck=" + velocityCheck +
                ", floorLimit=" + floorLimit +
                ", threshold=" + threshold +
                ", tacDenial='" + tacDenial + '\'' +
                ", tacOnline='" + tacOnline + '\'' +
                ", tbcDefualt='" + tbcDefualt + '\'' +
                ", acquierId='" + acquierId + '\'' +
                ", dDOL='" + dDOL + '\'' +
                ", tDOL='" + tDOL + '\'' +
                ", version='" + version + '\'' +
                ", riskmanData='" + riskmanData + '\'' +
                ", merchName='" + merchName + '\'' +
                ", merchCateCode='" + merchCateCode + '\'' +
                ", merchId='" + merchId + '\'' +
                ", termId='" + termId + '\'' +
                ", transCurrCode='" + transCurrCode + '\'' +
                ", transCurrExp=" + transCurrExp +
                ", referCurrCode='" + referCurrCode + '\'' +
                ", referCurrExp=" + referCurrExp +
                ", referCurrCon=" + referCurrCon +
                '}';
    }
    public static TlvList getTlvList(EmvAidParam emvAidParam){
        final String TAG = "Aid,getTlvList";

        TlvList tlvList = new TlvList();
        tlvList.addTlv("9F06",emvAidParam.getAid());
        tlvList.addTlv("DF01",String.format("%02d",emvAidParam.getSelFlag()));
        tlvList.addTlv("DF17",String.format("%02d",emvAidParam.getTargetPer()));
        tlvList.addTlv("DF16",String.format("%02d",emvAidParam.getMaxTargetPer()));
        tlvList.addTlv("9F1B", BytesUtil.int2Bytes(Integer.valueOf(emvAidParam.getFloorLimit() + ""),true));
        tlvList.addTlv("DF19", BytesUtil.hexString2Bytes(String.format("%012d", emvAidParam.getRdClssFLmt())));
        tlvList.addTlv("DF20", BytesUtil.hexString2Bytes(String.format("%012d", emvAidParam.getRdClssTxnLmt())));
        tlvList.addTlv("DF21", BytesUtil.hexString2Bytes(String.format("%012d", emvAidParam.getRdCVMLmt())));
        tlvList.addTlv("DF15", BytesUtil.int2Bytes(Integer.valueOf(emvAidParam.getThreshold() + ""),true));
        tlvList.addTlv("DF13",emvAidParam.getTacDenial());
        tlvList.addTlv("DF12",emvAidParam.getTacOnline());
        tlvList.addTlv("DF11",emvAidParam.getTbcDefualt());
        tlvList.addTlv("9F01",emvAidParam.getAcquierId());
        tlvList.addTlv("DF14",emvAidParam.getdDOL());
        tlvList.addTlv("DF8102",emvAidParam.gettDOL());
        tlvList.addTlv("9F09",emvAidParam.getVersion());
        tlvList.addTlv("9F1D",emvAidParam.getRiskmanData());
        tlvList.addTlv("9F4E",emvAidParam.getMerchName());
        tlvList.addTlv("9F15",emvAidParam.getMerchCateCode());
        tlvList.addTlv("9F16",emvAidParam.getTermId());
        tlvList.addTlv("9F1C",emvAidParam.getTermId());
        tlvList.addTlv("5F2A",emvAidParam.getTransCurrCode());
        tlvList.addTlv("5F36",String.format("%02d",emvAidParam.getTransCurrExp()));
        tlvList.addTlv("9F3C",emvAidParam.getReferCurrCode());
        tlvList.addTlv("9F3D",String.format("%02d",emvAidParam.getReferCurrExp()));
        tlvList.addTlv("DF8101", BytesUtil.int2Bytes(emvAidParam.getReferCurrCon(),true));
        tlvList.addTlv("9F7B",String.format("%02d",emvAidParam.getEcTTLVal()));
        return tlvList;
    }
}
