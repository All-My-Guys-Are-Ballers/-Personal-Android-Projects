package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlPure;
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
 * 创建日期：2021/6/21 on 11:16
 * 描述:
 * 作者:wangweicheng
 */
public class TransPurePay extends BaseTrans{
    private static final String TAG = TransPurePay.class.getSimpleName();
    private AidlPure purePay = TopUsdkManage.getInstance().getPurePay();
    @Override
    public EmvResult start() {
        try {
            AppLog.d(TAG, "start TransPurePay =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }
            int res = purePay.initialize();
            AppLog.d(TAG, "initialize  " + res);
            res = purePay.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
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
            if (DataUtils.isNullString(currentAid)){
                return EmvResult.RF_GET_AID_ERR;
            }
            byte[] aidData = processListener.onSelectAid(currentAid);
            AppLog.d(TAG, "entryL2 onSelectAid ==== ");
            if (aidData == null || aidData.length == 0){
                return EmvResult.RF_MATCH_AIDLIST_ERR;
            }
            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_PURE, aidData);
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                purePay.setTLVDataList(kernalData, kernalData.length);
            }
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            //PURE_Config_01
            byte[] transTypeAuthAppli = {(byte)0xFF, (byte)0x81, 0x35, 0x01, (byte)0x90};
            purePay.setTLVDataList(transTypeAuthAppli, transTypeAuthAppli.length);

            byte[] ATOL = {(byte)0xFF, (byte)0x81, 0x30, 0x28,
                    (byte)0x9F, 0x02, (byte)0x9F, 0x03, (byte)0x9F, 0x26, (byte)0x82, (byte)0x9F, 0x36, (byte)0x9F,
                    0x27, (byte)0x9F, 0x10, (byte)0x9F, 0x1A, (byte)0x95, 0x5F, 0x2A, (byte)0x9A, (byte)0x9C,
                    (byte)0x9F, 0x37, (byte)0x9F, 0x35, 0x57, (byte)0x9F, 0x34, (byte)0x84, 0x5F, 0x34,
                    0x5A, (byte)0xC7, (byte)0x9F, 0x33, (byte)0x9F, 0x73, (byte)0x9F, 0x77, (byte)0x9F, 0x45};
            purePay.setTLVDataList(ATOL, ATOL.length);

            byte[] MTOL = {(byte)0xFF, (byte)0x81, 0x31, 0x02,
                    (byte)0x8C, 0x57};
            purePay.setTLVDataList(MTOL, MTOL.length);

            byte[] ATDTOL = {(byte)0xFF, (byte)0x81, 0x32, 0x05,
                    (byte)0x82, (byte)0x95, (byte)0x9F, (byte)0x77, (byte)0x84};
            purePay.setTLVDataList(ATDTOL, ATDTOL.length);

            byte[] appCapabilities = {(byte)0xFF, (byte)0x81, 0x33, 0x05,
                    0x36, 0x00, 0x60, 0x43, (byte)0xF9};
            purePay.setTLVDataList(appCapabilities, appCapabilities.length);

            byte[] implOption = {(byte)0xFF, (byte)0x81, 0x34, 0x01, (byte)0xFF};
            purePay.setTLVDataList(implOption, implOption.length);

            byte[] defaultDDOL = {(byte)0xFF, (byte)0x81, 0x36, 0x03,
                    (byte)0x9F, 0x37, 0x04};
            purePay.setTLVDataList(defaultDDOL, defaultDDOL.length);

            byte[] posTimeout = {(byte)0xDF, (byte)0x81, 0x27, 0x02,
                    0x10, 0x00};
            purePay.setTLVDataList(posTimeout, posTimeout.length);

            byte[] envelope2 = {(byte)0x9F, 0x76, 0x09,
                    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
            purePay.setTLVDataList(envelope2, envelope2.length);

            if (preProcResult != null) {
                String buffer = BytesUtil.bytes2HexString(preProcResult.getAucReaderTTQ());
                AppLog.d(TAG, "preProcResult.getAucReaderTTQ: " + buffer);
                if (buffer.contains("00000000")) {
                    preProcResult.setAucReaderTTQ(BytesUtil.hexString2Bytes("3600C000"));
                }
            }

            byte[] C7Tlv = new byte[7];
            byte[] C7Value = new byte[5];
            int[] C7Len = new int[1];
            byte[] Tlv95 = new byte[7];
            byte[] Value95 = new byte[5];
            int[] Len95 = new int[1];
            if ((preProcResult.getUcRdCLFLmtExceed() == 1) ||
                    (preProcResult.getUcTermFLmtExceed() == 1)) {
                purePay.getTLVDataList(new byte[]{(byte)0xC7}, 1, C7Value.length, C7Value, C7Len);
                //Byte2 Bit8
                C7Value[1] |= (byte)0x80;
                C7Tlv[0] = (byte)0xC7;
                C7Tlv[1] = 0x05;
                System.arraycopy(C7Value, 0, C7Tlv, 2, C7Value.length);
                purePay.setTLVDataList(C7Tlv, C7Tlv.length);

                purePay.getTLVDataList(new byte[]{(byte)0x95}, 1, Value95.length, Value95, Len95);
                //Byte4 Bit8
                Value95[3] |= (byte)0x80;
                Tlv95[0] = (byte)0x95;
                Tlv95[1] = 0x05;
                System.arraycopy(Value95, 0, Tlv95, 2, Value95.length);
                purePay.setTLVDataList(Tlv95, Tlv95.length);
            }
            if (preProcResult.getUcRdCVMLmtExceed() == 1) {
                purePay.getTLVDataList(new byte[]{(byte)0xC7}, 1, C7Value.length, C7Value, C7Len);

                //Byte2 Bit7
                C7Value[1] |= (byte)0x40;
                C7Tlv[0] = (byte)0xC7;
                C7Tlv[1] = 0x05;
                System.arraycopy(C7Value, 0, C7Tlv, 2, C7Value.length);
                purePay.setTLVDataList(C7Tlv, C7Tlv.length);
            }
            if (preProcResult.getUcStatusCheckFlg() == 1) {
                purePay.getTLVDataList(new byte[]{(byte)0xC7}, 1, C7Value.length, C7Value, C7Len);

                //Byte2 Bit6
                C7Value[1] |= (byte)0x20;
                C7Tlv[0] = (byte)0xC7;
                C7Tlv[1] = 0x05;
                System.arraycopy(C7Value, 0, C7Tlv, 2, C7Value.length);
                purePay.setTLVDataList(C7Tlv, C7Tlv.length);
            }
            if (preProcResult.getUcZeroAmtFlg() == 1) {
                purePay.getTLVDataList(new byte[]{(byte)0xC7}, 1, C7Value.length, C7Value, C7Len);

                //Byte2 Bit5
                C7Value[1] |= (byte)0x10;
                C7Tlv[0] = (byte)0xC7;
                C7Tlv[1] = 0x05;
                System.arraycopy(C7Value, 0, C7Tlv, 2, C7Value.length);
                purePay.setTLVDataList(C7Tlv, C7Tlv.length);
            }
            //gpo
            res = purePay.gpoProc();
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
            res = purePay.readData();
            AppLog.d(TAG, "readData res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //read card info
            byte[] cardData = new byte[32];
            int[] dataLen = new int[1];
            AppLog.d(TAG, "paywave get TAG 57 ===");
            int res1 = purePay.getTLVDataList(BytesUtil.hexString2Bytes("57"),
                    1, cardData.length, cardData, dataLen);
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


            res = purePay.startTrans((byte)0);
            AppLog.emvd(TAG, "startTrans res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.ABORT_TERMINATED;
            }
            //Add Capk
            addCapk(currentAid);
            res = purePay.cardAuth();
            AppLog.emvd(TAG, "pure.cardAuth: res=" + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_OFFLINE_AUTH_ERR;
            }

            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = purePay.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
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

            switch (outComeBuffer[0]) {
                case PayDataUtil.CLSS_OC_APPROVED:
                    //offline success
                    AppLog.d(TAG, "TC offline success");
                    return EmvResult.OFFLINE_APPROVED;
                case PayDataUtil.CLSS_OC_ONLINE_REQUEST:
                    //online success
                    AppLog.d(TAG, "ARQC online success");
                    return EmvResult.ARQC;
                case PayDataUtil.CLSS_OC_DECLINED:
                    //transaction reject
                    AppLog.d(TAG, "AAC transaction reject");
                    return EmvResult.TRANS_DENIED;
                default:
                    return EmvResult.ABORT_TERMINATED;
            }
        }catch (Exception e){
            e.getMessage();

        }
        return EmvResult.ABORT_TERMINATED;
    }
    /**
     * 获取当前rid
     *
     * @return 当前应用rid
     */
    private String getCurrentRid() {
        String aid = null;
        try {
            purePay.delAllRevocList();
            purePay.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = purePay.getTLVDataList(new byte[]{0x4F}, 1,
                    aucAid.length, aucAid, realLen);
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
        try {
            purePay.delAllRevocList();
            purePay.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.d(TAG, "aid: " + aid);
            int res = purePay.getTLVDataList(new byte[]{(byte) 0x8F},
                    1, 1, index, realLen);
            if (res == PayDataUtil.EMV_OK) {
                AppLog.d(TAG, "capk index: " + index[0]);
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = purePay.addCAPK(emvCapk);
                    AppLog.d(TAG, "add capk res: " + res);
                }
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }
}
