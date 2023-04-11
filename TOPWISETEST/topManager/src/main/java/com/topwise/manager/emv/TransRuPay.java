package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlRupay;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.entity.EmvEntity;
import com.topwise.manager.emv.enums.EOnlineResult;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.manager.emv.utlis.TransUtlis;
import com.topwise.manager.utlis.DataUtils;
import com.topwise.toptool.impl.Utils;

/**
 * 创建日期：2021/6/21 on 11:19
 * 描述: rupay card
 * 作者:wangweicheng
 */
public class TransRuPay extends BaseTrans {
    private static final String TAG = TransRuPay.class.getSimpleName();
    private AidlRupay rupay = TopUsdkManage.getInstance().getRupay();
    @Override
    public EmvResult start() {

        try {
            AppLog.d(TAG, "start TransRuPay =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }
            if (preProcResult != null) {
                String buffer = BytesUtil.bytes2HexString(preProcResult.getAucReaderTTQ());
                AppLog.d(TAG, "preProcResult.getAucReaderTTQ: " + buffer);
                if (buffer.contains("00000000")) {
                    preProcResult.setAucReaderTTQ(BytesUtil.hexString2Bytes("3600C000"));
                }
            }
            int res = rupay.initialize();
            AppLog.d(TAG, "initialize  " + res);
            res = rupay.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
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

            String currentRid = getCurrentRid();
            if (DataUtils.isNullString(currentRid)){
                return EmvResult.RF_GET_AID_ERR;
            }
            byte[] aidData = processListener.onSelectAid(currentRid);
            AppLog.d(TAG, "entryL2 onSelectAid ==== ");
            if (aidData == null || aidData.length == 0){
                return EmvResult.RF_MATCH_AIDLIST_ERR;
            }
            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_RUPAY, aidData);
            //((unsigned char*)"\xDF\x81\x0C", 3, (unsigned char*)"\x0D", 1)
            AppLog.d(TAG, "Add Kernel ID  Kernel ==== ");
            kernalList.addTlv("DF810C","0D"); //Name: Kernel ID  Kernel:
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                rupay.setTLVDataList(kernalData, kernalData.length);
            }
            //callback app final Select aid
            rfProcessListener.finalAidSelect();

            AppLog.emvd(TAG, "gpoProc=========: " );
            res = rupay.gpoProc();
            AppLog.emvd(TAG, "gpoProc res: " + res);
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
            AppLog.emvd(TAG, "readData=========: " );
            res = rupay.readData();
            AppLog.emvd(TAG, "readData res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //read card info
            byte[] cardData = new byte[32];
            int[] dataLen = new int[1];
            AppLog.d(TAG, "paywave get TAG 57 ===");
            int res1 = rupay.getTLVDataList(BytesUtil.hexString2Bytes("57"),
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
            //Add Capk
            AppLog.emvd(TAG, "Add Capk =========: " +  currentRid);
            addCapk(currentRid);
            AppLog.emvd(TAG, "cardAuth=========: " );
            res = rupay.cardAuth();
            AppLog.emvd(TAG, "pure.cardAuth: res=" + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.ABORT_TERMINATED;
            }
            res = rupay.transProc((byte) 0); //黑名单
            AppLog.emvd(TAG, "transProc res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.ABORT_TERMINATED;
            }
            AppLog.emvd(TAG, "startTrans=========: " );
            res = rupay.startTrans();
            AppLog.emvd(TAG, "startTrans res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.ABORT_TERMINATED;
            }

            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = rupay.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
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
                    return onlineAndscriptProcess();
                case PayDataUtil.CLSS_OC_DECLINED:
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

    private EmvResult onlineAndscriptProcess() throws RemoteException {
        AppLog.d(TAG, "onlineAndscriptProcess ============= ");
        EmvEntity emvEntity = rfProcessListener.onRequestOnline();
        AppLog.d(TAG, "onlineAndscriptProcess onRequestOnline ============= " +emvEntity.toString());
        byte[] issueScript71 = emvEntity.getIssueScript71();
        byte[] issueScript72 = emvEntity.getIssueScript72();
        byte[] issueAuthData91 = emvEntity.getIssueAuthData();
        byte[] authCode89 = emvEntity.getAuthCode();
        byte[] authRespCode8A = emvEntity.getAuthRespCode();
        EOnlineResult eOnlineResult = emvEntity.geteOnlineResult();
        if (EOnlineResult.APPROVE  == eOnlineResult) {
            int sAppVerCard = -1;
            int sAppVerTM = -1;

            byte[] aucAppVerCard = emvL2.EMV_GetTLVData(0x9F08);
            byte[] aucAppVerTM = emvL2.EMV_GetTLVData(0x9F09);
            if (aucAppVerCard != null){
                sAppVerCard = Integer.valueOf(BytesUtil.bytes2HexString(aucAppVerCard));
            }
            if (aucAppVerTM != null){
                sAppVerTM = Integer.valueOf(BytesUtil.bytes2HexString(aucAppVerTM));
            }
            AppLog.d(TAG,"onlineAndscriptProcess 0x9F08 " + sAppVerCard);
            AppLog.d(TAG,"onlineAndscriptProcess 0x9F09 " + sAppVerTM);
            byte [] srcipt = null;
            if (issueScript71 != null && issueScript72 != null  ){
                AppLog.d(TAG,"onlineAndscriptProcess issueScript71 != null and issueScript72 != null " );
                srcipt = new byte[issueScript71.length + issueScript72.length];
                System.arraycopy(issueScript71, 0, srcipt, 0, issueScript71.length);
                System.arraycopy(issueScript72, 0, srcipt, issueScript71.length, issueScript72.length);
            }else if (issueScript71 != null && issueScript72 == null){
                AppLog.d(TAG,"onlineAndscriptProcess issueScript71 != null and issueScript72 == null " );
                srcipt = new byte[issueScript71.length];
                System.arraycopy(issueScript71, 0, srcipt, 0, issueScript71.length);
            }else if (issueScript72 != null && issueScript71 == null){
                AppLog.d(TAG,"onlineAndscriptProcess issueScript71 == null and issueScript72 == null " );
                srcipt = new byte[issueScript72.length];
                System.arraycopy(issueScript72, 0, srcipt, 0, issueScript72.length);
            }
            if (sAppVerCard >= 2 || sAppVerTM >= 2)
            {
                if ((srcipt != null && srcipt.length > 0) ||
                        (issueAuthData91 != null
                                &&issueAuthData91.length >6
                                && (( issueAuthData91[6] & 0x80) == 0x80  ||
                                    ( issueAuthData91[6] & 0x40) == 0x40)))
                {
                    AppLog.d(TAG,"onlineAndscriptProcess onSecondCheckCard ");
                    int retCheckCard = rfProcessListener.onSecondCheckCard();
                    if (retCheckCard != 0)
                    {
                        return  EmvResult.ONLINE_CARD_DENIED;
                    }
                }
            }
            byte[] srciptout = new byte[256];
            byte[] ucACTypeOut = new byte[256];
            int[] gl_unScriptRstOutLen = new int[10];
            int srciptlen = -1;
            int completeTransRet = -1;
            if (srcipt != null){
                srciptlen = srcipt.length;
                AppLog.d(TAG,"onlineAndscriptProcess srcipt " +BytesUtil.bytes2HexString(srcipt)  +" srciptlen " +srciptlen);
                completeTransRet = rupay.completeTrans(0,srcipt,srciptlen,srciptout,gl_unScriptRstOutLen,ucACTypeOut);
            }else {
                srciptlen = 0;
                AppLog.d(TAG,"onlineAndscriptProcess srcipt is null srciptlen " +srciptlen );
                completeTransRet = rupay.completeTrans(0,new byte[16],srciptlen,srciptout,gl_unScriptRstOutLen,ucACTypeOut);
            }

            AppLog.d(TAG,"onlineAndscriptProcess completeTrans cRet " +completeTransRet );
            if (srciptout != null && gl_unScriptRstOutLen[0] > 0){
                byte[] scriptVale = new byte[gl_unScriptRstOutLen[0]];
                System.arraycopy(srciptout, 0, scriptVale, 0,gl_unScriptRstOutLen[0]);
                AppLog.d(TAG,"onlineAndscriptProcess completeTrans 9F5B " + BytesUtil.bytes2HexString(scriptVale));
                emvL2.EMV_SetTLVData(0x9F5B,srciptout);
            }
            if (completeTransRet == PayDataUtil.EMV_OK){
                return  EmvResult.APPROVED;
            }
            return  EmvResult.TRANS_DENIED;
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
            //delete
            rupay.delAllRevocList();
            rupay.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = rupay.getTLVDataList(new byte[]{0x4F}, 1,
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
            rupay.delAllRevocList();
            rupay.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.d(TAG, "aid: " + aid);
            int res = rupay.getTLVDataList(new byte[]{(byte) 0x8F},
                    1, 1, index, realLen);
            AppLog.d(TAG, "res:==== " + res);
            if (res == PayDataUtil.EMV_OK) {
                AppLog.d(TAG, "capk index: " + index[0]);
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = rupay.addCAPK(emvCapk);
                    AppLog.d(TAG, "add capk res: " + res);
                }
            }

//            if (res == PayDataUtil.EMV_OK) {
//                AppLog.emvd(TAG, "capk index: " + index[0]);
//                //import capk
//                EmvCapk emvCapk = getCurrentCapk(BytesUtil.hexString2Bytes(aid), index);
//                AppLog.emvd(TAG, "add capk: " + emvCapk);
//                if (emvCapk != null) {
//                    res = rupay.addCAPK(emvCapk);
//                    AppLog.emvd(TAG, "add capk res: " + res);
//
//                }
//                index[0] = 0x6A;
//                emvCapk = getCurrentCapk(BytesUtil.hexString2Bytes(aid), index);
//                AppLog.emvd(TAG, "add capk: " + emvCapk);
//                if (emvCapk != null) {
//                    res = rupay.addCAPK(emvCapk);
//                    AppLog.emvd(TAG, "add capk 6A res: " + res);
//                }
//                index[0] = 0x6C;
//                emvCapk = getCurrentCapk(BytesUtil.hexString2Bytes(aid), index);
//                AppLog.emvd(TAG, "add capk: " + emvCapk);
//                if (emvCapk != null) {
//                    res = rupay.addCAPK(emvCapk);
//                    AppLog.emvd(TAG, "add capk 0x6C res: " + res);
//                }
//
////                index[0] = 0x6D;
////                emvCapk = getCurrentCapk(BytesUtil.hexString2Bytes(aid), index);
////                AppLog.emvd(TAG, "add capk: " + emvCapk);
////                if (emvCapk != null) {
////                    res = rupay.addCAPK(emvCapk);
////                    AppLog.emvd(TAG, "add capk 0x6D res: " + res);
////                }

//            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

}
