package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlDpas;
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
 * 创建日期：2021/6/21 on 11:33
 * 描述:
 * 作者:wangweicheng
 */
public class TransDpasPay extends BaseTrans{
    private static final String TAG = TransDpasPay.class.getSimpleName();
    private AidlDpas dpasPay = TopUsdkManage.getInstance().getDpasPay();
    @Override
    public EmvResult start() {
        try {
            AppLog.d(TAG, "start TransPurePay =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }

            int res = dpasPay.initialize();
            AppLog.d(TAG, "initialize res: " + res);
            res = dpasPay.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
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
            AppLog.d(TAG, "Add Kernel ID  Kernel ==== ");
            // App_SetTLVDataOneTag((unsigned char*)"\xDF\x81\x0C", 3, (unsigned char*)"\x06", 1);//Kernel ID -DPAS 06
            kernalList.addTlv("DF810C","06"); //Name:		Kernel ID  Kernel:
            // 9F66 Terminal Transaction Qualifiers (TTQ)
            // B620C000 TTQ Online and Offline :Should be B6 00 C0 00
            kernalList.addTlv("9F66","B620C000");
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                dpasPay.setTLVDataList(kernalData, kernalData.length);
            }
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            byte[] dataBuf = new byte[1];
            res = dpasPay.gpoProc(dataBuf);
            AppLog.d(TAG, "gpoProc res: " + res);
            AppLog.d(TAG, "gpoProc dataBuf res: " + dataBuf[0]);
            if (res == TransUtlis.EMV_SELECT_NEXT_AID) {
                int ret = entryL2.delCandListCurApp();
                AppLog.d(TAG, "entryL2 delCandListCurApp ret: " + ret);
                if (ret == TransUtlis.EMV_OK) {
                    return EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR;
                } else {
                    return EmvResult.RF_DEL_CAND_LIST_ERR;
                }
            }

            res = dpasPay.readData();
            AppLog.d(TAG, "readData res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //read card info
            String cardNo = "";
            byte[] cardData = new byte[32];
            int[] dataLen = new int[1];
            int res1 = dpasPay.getTLVDataList(BytesUtil.hexString2Bytes("57"),1, cardData.length, cardData, dataLen);
            if (res1 == TransUtlis.EMV_OK && cardData != null){
                String track2Data = BytesUtil.bytes2HexString(cardData);
                AppLog.d(TAG, "track2 data: " + track2Data);
                cardNo =  getPan(track2Data.split("F")[0]);
                if (!DataUtils.isNullString(cardNo)){
                    if (!rfProcessListener.onConfirmCardInfo(cardNo)){
                        return EmvResult.RF_CONFIRM_CARDNO_CANCEL;
                    }
                }
            }
            if (dataBuf[0] == PayDataUtil.CLSS_TRANSPATH_EMV) {
                addCapk(currentAid);
            }

            res =  dpasPay.transProc((byte)0);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = dpasPay.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
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
            //DF8129
            //Byte 1  b8-5
            // 0001 APPROVED
            // 0010 DECLIEND
            // 0011 ONLINE REQUEST
            // 0100 END APPLICATION
            // 0101 SELECE NEXT
            // 0111 TRY AGAIN
            // 1111 N/A
            AppLog.d(TAG, "DF2129 byte 0 = : " + outComeBuffer[0]);
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
                case PayDataUtil.CLSS_OC_SELECT_NEXT:
                    AppLog.d(TAG, "SELECE NEXT");
                    return EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR;
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
            dpasPay.delAllRevocList();
            dpasPay.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = dpasPay.getTLVDataList(new byte[]{0x4F}, 1,aucAid.length, aucAid, realLen);
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
            dpasPay.delAllRevocList();
            dpasPay.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.emvd(TAG, "aid: " + aid);
            int res = dpasPay.getTLVDataList(new byte[]{(byte) 0x8F},1, 1, index, realLen);

            if (res == PayDataUtil.EMV_OK) {
                AppLog.emvd(TAG, "capk index: " + index[0]);
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = dpasPay.addCAPK(emvCapk);
                    AppLog.emvd(TAG, "add capk res: " + res);
                }
            }

        }catch(RemoteException e){
            e.printStackTrace();
        }
    }
}
