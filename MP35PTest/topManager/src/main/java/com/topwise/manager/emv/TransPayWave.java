package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlPaywave;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
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
 * 创建日期：2021/6/21 on 9:19
 * 描述:Visa
 * 作者:wangweicheng
 */
public class TransPayWave extends BaseTrans{
    private static final String TAG = TransPayWave.class.getSimpleName();
    private AidlPaywave paywave = TopUsdkManage.getInstance().getPaywave();
    @Override
    public EmvResult start() {
        try {
            AppLog.d(TAG, "start Paywave =========== ");
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }
            paywave.initialize();

            int res = paywave.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
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

            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_VISA, aidData);
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                paywave.setTLVDataList(kernalData, kernalData.length);
            }
            if (preProcResult != null) { //if null update ttq
                String buffer = BytesUtil.bytes2HexString(preProcResult.getAucReaderTTQ());
                AppLog.d(TAG, "preProcResult.getAucReaderTTQ: " + buffer);
                if (buffer.contains("00000000")) {
                    preProcResult.setAucReaderTTQ(BytesUtil.hexString2Bytes("3600C000"));
                }
            }
            TransParam transParam = inputParam.getTransParam();
            AppLog.d(TAG, "setTransData ========== ");
            res = paywave.setTransData(transParam, preProcResult);
            AppLog.d(TAG, "setTransData res: " + res);
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            //set ttq
            byte[] TTQTlv = new byte[7];
            TTQTlv[0] = (byte)0x9F;
            TTQTlv[1] = (byte)0x66;
            TTQTlv[2] = (byte)0x04;
            System.arraycopy(preProcResult.getAucReaderTTQ(), 0, TTQTlv, 3, 4);
            AppLog.d(TAG, "TTQTlv: " + BytesUtil.bytes2HexString(TTQTlv));
            paywave.setTLVDataList(TTQTlv, TTQTlv.length);

            //gpo
            byte[] dataBuf = new byte[1];
            res = paywave.gpoProc(dataBuf);
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
            //read data
            byte[] ucAcType1 = new byte[1];
            res = paywave.readData(ucAcType1);
            AppLog.d(TAG, "readData res: " + res);
            if (res != TransUtlis.EMV_OK ) {
                return EmvResult.RF_READ_DATA;
            }
            //read card info
            byte[] cardData = new byte[32];
            int[] dataLen = new int[1];
            AppLog.d(TAG, "paywave get TAG 57 ===");
            int res1 = paywave.getTLVDataList(BytesUtil.hexString2Bytes("57"), 1, cardData.length, cardData, dataLen);
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
            //ucAcType1 == PayDataUtil.AC_TC   ----TO DO

            byte[] ucAcType = new byte[1];
            AppLog.d(TAG, "gpo return ucType: " + dataBuf[0]);
            if (dataBuf[0] == PayDataUtil.CLSS_TRANSPATH_EMV) {
                addCapk(currentAid);
                byte[] ucODDAResultFlg = new byte[1];
                res = paywave.cardAuth(ucAcType, ucODDAResultFlg);
            } else if (dataBuf[0] == PayDataUtil.CLSS_TRANSPATH_MAG) {
                AppLog.d(TAG, "ucType == PayDataUtil.CLSS_TRANSPATH_MAG");
            } else {
                res = PayDataUtil.CLSS_TERMINATE;
            }
            AppLog.d(TAG, "trans proc res: " + res + ";ucAcType: " + ucAcType[0]);
            if (res != TransUtlis.EMV_OK){
                return EmvResult.RF_OFFLINE_AUTH_ERR;
            }

            //DF8129 tag  --is need pwd
            byte[] outComeBuffer = new byte[17];
            int[] bufLen = new int[1];
            res = paywave.getTLVDataList(new byte[]{(byte) 0xDF, (byte) 0x81, 0x29},
                    3, outComeBuffer.length, outComeBuffer, bufLen);
            if (res == PayDataUtil.EMV_OK) {
                AppLog.d(TAG, "DF8129 bufLen: " + bufLen[0]);
                byte[] outData = new byte[bufLen[0]];
                System.arraycopy(outComeBuffer, 0, outData, 0, bufLen[0]);
                AppLog.d(TAG, "real outcome data: " + BytesUtil.bytes2HexString(outData));
                EmvEntity emvEntity;
                //judge import pin
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
        }catch(Exception e){
            e.getMessage();
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
            paywave.delAllRevocList();
            paywave.delAllCAPK();
            int[] realLen = new int[1];
            byte[] aucAid = new byte[17];
            int res = paywave.getTLVDataList(new byte[]{0x4F}, 1,
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
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return aid;
    }

    /**
     * 添加CAPK到交易库
     */
    private void addCapk(String aid) {
        AppLog.d(TAG, "addCapk(),  aid: " + aid);
        try {
            paywave.delAllRevocList();
            paywave.delAllCAPK();
            byte[] index = new byte[1];
            int[] realLen = new int[1];
            AppLog.emvd(TAG, "aid: " + aid);
            int res = paywave.getTLVDataList(new byte[]{(byte) 0x8F},
                    1, 1, index, realLen);
            AppLog.d(TAG, "capk addCapk: " + res);

//            index[0] = 0x01;
            if (res == PayDataUtil.EMV_OK) {
                EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
                if (emvCapk != null) {
                    res = paywave.addCAPK(emvCapk);
                    AppLog.d(TAG, "add capk res: " + res);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
