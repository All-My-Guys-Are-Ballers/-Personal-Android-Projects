package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlMir;
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
 * 创建日期：2021/6/21 on 11:34
 * 描述:
 * 作者:wangweicheng
 */
public class TransMirPay extends BaseTrans{
    private static final String TAG = TransMirPay.class.getSimpleName();
    private AidlMir mirPay = TopUsdkManage.getInstance().getMirPay();
    @Override
    public EmvResult start() {
        try {
            byte [] gl_ucTransPath = new byte[1];;
            AppLog.d(TAG, "start TransMirPay =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }

            int res = mirPay.initialize();
            AppLog.d(TAG, "initialize res: " + res);
            res = mirPay.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen(),gl_ucTransPath);
            AppLog.d(TAG, "setFinalSelectData res: " + res);
            AppLog.d(TAG, "setFinalSelectData gl_ucTransPath: " + gl_ucTransPath[0]);
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
            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_MIR, aidData);
//            App_SetTLVDataOneTag((unsigned char*)"\xDF\x81\x0C", 3, (unsigned char*)"\x81", 1);//Kernel ID -MIR 81 06 43
            kernalList.addTlv("DF810C","81");
            //Data Exchange Tag List**recommended minimum set of tags
            kernalList.addTlv("FF04","5A");
            //Transaction Recovery Limit
            kernalList.addTlv("FF56","03");
            //68 or E8
            kernalList.addTlv("FF55","E8");
            // 9F66 Terminal Transaction Qualifiers (TTQ)
            // /\xB6\x20\xC0\x00TTQ Online and Offline :Should be  B6 00 C0 00
            kernalList.addTlv("9F66","B620C000");

            //TAC-Online
            kernalList.addTlv("FF02","0000000000");
            //TAC Default
            kernalList.addTlv("FF03","0000048000");
            //TAC Denial
            kernalList.addTlv("FF01","0000000000");

            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                mirPay.setTLVDataList(kernalData, kernalData.length);
            }
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            if (0x01 == gl_ucTransPath[0])
            {
                AppLog.d(TAG, "execute MIR_PROTOCOL1" );
                res = mirPay.gpoProc();
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
                mirPay.readData();
                AppLog.d(TAG, "readData res: " + res);
                if (res != TransUtlis.EMV_OK ) {
                    return EmvResult.RF_READ_DATA;
                }
                //read card info
                byte[] cardData = new byte[32];
                int[] dataLen = new int[1];
                int res1 = mirPay.getTLVDataList(BytesUtil.hexString2Bytes("57"),1, cardData.length, cardData, dataLen);
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
                addCapk(currentAid);
                res = mirPay.cardAuth();
                AppLog.d(TAG, "cardAuth res: " + res);
                if (res != TransUtlis.EMV_OK ) {
                    return EmvResult.RF_OFFLINE_AUTH_ERR;
                }

                res = mirPay.startTrans((byte) 0x00);
                AppLog.d(TAG, "cardAuth res: " + res);
                if (res != TransUtlis.EMV_OK ) {
                    return EmvResult.RF_CHECK_BACK_ERR;
                }
            }else {
                AppLog.d(TAG, "execute MIR_PROTOCOL2" );
                res = mirPay.transInitiate2();
                AppLog.d(TAG, "transInitiate2 res: " + res);
                if (res != TransUtlis.EMV_OK){
                    return EmvResult.TRANS_DENIED;
                }

                res = mirPay.transProcess2();
                AppLog.d(TAG, "transProcess2 res: " + res);
                if (res != TransUtlis.EMV_OK){
                    return EmvResult.TRANS_DENIED;
                }
                //ODA
                addCapk(currentAid);

                res = mirPay.transComplete2();
                AppLog.d(TAG, "transComplete2 res: " + res);
                if (res != TransUtlis.EMV_OK){
                    return EmvResult.TRANS_DENIED;
                }
            }

            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = mirPay.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
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
            mirPay.delAllRevocList();
            mirPay.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = mirPay.getTLVDataList(new byte[]{0x4F}, 1,aucAid.length, aucAid, realLen);
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
            mirPay.delAllRevocList();
            mirPay.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.emvd(TAG, "aid: " + aid);
            int res = mirPay.getTLVDataList(new byte[]{(byte) 0x8F},1, 1, index, realLen);

            if (res == PayDataUtil.EMV_OK) {
                AppLog.emvd(TAG, "capk index: " + index[0]);
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = mirPay.addCAPK(emvCapk);
                    AppLog.emvd(TAG, "add capk res: " + res);
                }
            }

        }catch(RemoteException e){
            e.printStackTrace();
        }
    }
}
