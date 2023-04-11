package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlAmex;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.entity.EmvEntity;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.manager.emv.utlis.TransUtlis;
import com.topwise.manager.utlis.DataUtils;

/**
 * 创建日期：2021/6/21 on 11:20
 * 描述:
 * 作者:wangweicheng
 */
public class TransAMexPay extends BaseTrans {
    private static final String TAG = TransRuPay.class.getSimpleName();
    private AidlAmex amexPay = TopUsdkManage.getInstance().getAmexPay();
    @Override
    public EmvResult start() {
        try {
            AppLog.d(TAG, "start TransPurePay =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }

            int res = amexPay.initialize();
            AppLog.d(TAG, "initialize res: " + res);
            res = amexPay.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
            AppLog.d(TAG, "setFinalSelectData res: " + res);
            if (res != TransUtlis.EMV_OK){
                if (res == TransUtlis.EMV_SELECT_NEXT_AID){
                    int ret = entryL2.delCandListCurApp();
                    AppLog.d(TAG, "entryL2 delCandListCurApp ret: " + ret);
                    if (ret == TransUtlis.EMV_OK) {
                        return EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR;
                    } else {
                        return EmvResult.RF_DEL_CAND_LIST_ERR;
                    }
                }else {
                    return EmvResult.RF_AID_FINAL_SELECT_ERR;
                }
            }
            String currentAid = getCurrentRid();
            byte[] aidData = processListener.onSelectAid(currentAid);
            AppLog.d(TAG, "entryL2 onSelectAid ==== ");
            if (aidData == null || aidData.length == 0){
                return EmvResult.RF_MATCH_AIDLIST_ERR;
            }
            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_AMEX, aidData);
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                amexPay.setTLVDataList(kernalData, kernalData.length);
            }
            /*******ljz add 20200709 *********/
            byte[] aucECRC = {(byte) 0x9F, 0x6E, 0x04, (byte)0x9C, (byte)0xA0, 0x00, 0x03};
            amexPay.setTLVDataList(aucECRC, aucECRC.length);
            if (preProcResult != null) {
                String buffer = BytesUtil.bytes2HexString(preProcResult.getAucReaderTTQ());
                if (buffer.contains("00000000")) {
                    preProcResult.setAucReaderTTQ(BytesUtil.hexString2Bytes("3600C000"));
                }
            }
            res = amexPay.setTransData(inputParam.getTransParam(), preProcResult);
            AppLog.d(TAG, "setTransData res: " + res);
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            byte[] dataBuf = new byte[1];
            res = amexPay.gpoProc(dataBuf);
            AppLog.d(TAG, "gpoProc res: " + res);
            if (res == TransUtlis.EMV_SELECT_NEXT_AID) {
                int ret = entryL2.delCandListCurApp();
                AppLog.d(TAG, "entryL2 delCandListCurApp ret: " + ret);
                if (ret == TransUtlis.EMV_OK) {
                    return EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR;
                } else {
                    return EmvResult.RF_DEL_CAND_LIST_ERR;
                }
            }

            res = amexPay.readData();
            AppLog.d(TAG, "readData res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //read card info
            byte[] cardData = new byte[32];
            int[] dataLen = new int[1];
            int res1 = amexPay.getTLVDataList(BytesUtil.hexString2Bytes("57"),1, cardData.length, cardData, dataLen);
            if (res1 == TransUtlis.EMV_OK && cardData != null){
                String track2Data = BytesUtil.bytes2HexString(cardData);
                AppLog.d(TAG, "track2 data: " + track2Data);
                String cardNo =  getPan(track2Data.split("F")[0]);
                if (!DataUtils.isNullString(cardNo)){
                    if (!rfProcessListener.onConfirmCardInfo(cardNo)){
                        return EmvResult.RF_CONFIRM_CARDNO_CANCEL;
                    }
                }
            }
            AppLog.d(TAG, "ucType: " + dataBuf[0]);
            if (dataBuf[0] == PayDataUtil.CLSS_TRANSPATH_EMV) {
                addCapk(currentAid);
                res = amexPay.cardAuth();
                AppLog.d(TAG, "cardAuth " + res);
            } else if (dataBuf[0] == PayDataUtil.CLSS_TRANSPATH_MAG) {
                AppLog.d(TAG, "ucType == PayDataUtil.CLSS_TRANSPATH_MAG");
            } else {
                res = PayDataUtil.CLSS_TERMINATE;
            }

            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_OFFLINE_AUTH_ERR;
            }
            byte[] ucAcType = new byte[1];
            byte[] ucAdviceFlag = new byte[1];
            byte[] ucDelayAuth = new byte[1];
            res = amexPay.startTrans((byte) 0, ucAdviceFlag, ucAcType, ucDelayAuth);
            AppLog.d(TAG, "startTrans res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.ABORT_TERMINATED;
            }

            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = amexPay.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
                    3, outComeBuffer.length, outComeBuffer, bufLen);
            if (res == PayDataUtil.EMV_OK) {
                AppLog.d(TAG, "bufLen: " + bufLen[0]);
                byte[] outData = new byte[bufLen[0]];
                System.arraycopy(outComeBuffer, 0, outData, 0, bufLen[0]);
                AppLog.d(TAG, "real outcome data: " + BytesUtil.bytes2HexString(outData));
                //judge import pin
                EmvEntity emvEntity;
                if (inputParam != null && inputParam.isHavePin()){
                    emvEntity = rfProcessListener.requestImportPin(PayDataUtil.PINTYPE_ONLINE, false, null);
                    if (!emvEntity.isResult()){
                        return EmvResult.RF_INPUT_PIN_CANCEL;
                    }
                }else { //check Cardholder Verification re
                    if ((outData[3] & 0xF0) == PayDataUtil.CLSS_OC_ONLINE_PIN) {
                        //Online enciphered PIN
                        emvEntity = rfProcessListener.requestImportPin(PayDataUtil.PINTYPE_ONLINE,false,null);
                        if (!emvEntity.isResult()){
                            return EmvResult.RF_INPUT_PIN_CANCEL;
                        }
                    }
                }
            }

            switch (ucAcType[0]) {
                case PayDataUtil.AC_TC:
                    //offline success
                    AppLog.d(TAG, "TC offline success");
                    return EmvResult.OFFLINE_APPROVED;
                case PayDataUtil.AC_ARQC:
                    //online success
                    AppLog.d(TAG, "ARQC online success");
                    return EmvResult.ARQC;
                case PayDataUtil.AC_AAC:
                    //transaction reject
                    AppLog.d(TAG, "AAC transaction reject");
                    return EmvResult.TRANS_DENIED;
                default:
                    return EmvResult.ABORT_TERMINATED;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            return EmvResult.ABORT_TERMINATED;
        }

    }
    /**
     * 获取当前rid
     *
     * @return 当前应用rid
     */
    private String getCurrentRid() {
        String aid = null;
        try {
            amexPay.delAllRevocList();
            amexPay.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = amexPay.getTLVDataList(new byte[]{0x4F}, 1,aucAid.length, aucAid, realLen);
            AppLog.d(TAG, "getTLVDataList capk aid res: " + res);
            if (res == PayDataUtil.EMV_OK) {
                if (realLen[0] > 0) {
                    byte[] aidData = new byte[realLen[0]];
                    System.arraycopy(aucAid, 0, aidData, 0, realLen[0]);
                    aid = BytesUtil.bytes2HexString(aidData);
                    AppLog.d(TAG, "aid len: " + realLen[0] + ";aid: " + aid);
                }
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
        return aid;
    }

    /**
     * 添加CAPK到交易库
     */
    private void addCapk(String aid) {
        try{
            amexPay.delAllRevocList();
            amexPay.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.emvd(TAG, "aid: " + aid);
            int res = amexPay.getTLVDataList(new byte[]{(byte) 0x8F},1, 1, index, realLen);

            if (res == PayDataUtil.EMV_OK) {
                AppLog.emvd(TAG, "capk index: " + index[0]);
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = amexPay.addCAPK(emvCapk);
                    AppLog.emvd(TAG, "add capk res: " + res);
                }
            }

        }catch(RemoteException e){
            e.printStackTrace();
        }
    }
}
