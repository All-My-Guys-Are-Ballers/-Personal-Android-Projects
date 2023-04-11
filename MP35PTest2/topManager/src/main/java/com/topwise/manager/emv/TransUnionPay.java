package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlQpboc;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.aidl.emv.level2.QpbocCallback;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.cloudpos.struct.Tlv;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.entity.EmvEntity;
import com.topwise.manager.emv.enums.EmvResult;

import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.manager.emv.utlis.TransUtlis;
import com.topwise.manager.utlis.DataUtils;

import java.util.Map;

/**
 * 创建日期：2021/6/16 on 10:52
 * 描述: Union Pay card
 * 作者:wangweicheng
 */
public class TransUnionPay extends BaseTrans{
    private static final String TAG = TransUnionPay.class.getSimpleName();
    private AidlQpboc aidlQpboc = TopUsdkManage.getInstance().getUnionPay();
    /**
     * start Contactless process
     * @return EmvResult
     */
    @Override
    public EmvResult start() {
        int res = 0;
        try {
            PreProcResult preProcResult = inputParam.getPreProcResult();
            if (preProcResult == null){
                AppLog.d(TAG, "startPay preProcResult null ");
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }
            res = aidlQpboc.initialize(PayDataUtil.KERNTYPE_QPBOC, (byte)0);
            AppLog.d(TAG, "aidlQpboc initialize res: " + res);
            if (res != TransUtlis.EMV_OK){
                return EmvResult.RF_KERNAL_INIT_ERR;
            }
            res = aidlQpboc.setCallback(new QpbocCallback.Stub() {
                @Override
                public int cCheckExceptionFile(byte[] bytes, int i, byte b) throws RemoteException {
                    AppLog.d(TAG,"Call Back cCheckExceptionFile");
                    AppLog.d(TAG, "PAN: " + BytesUtil.bytes2HexString(bytes));
                    AppLog.d(TAG, "PAN length: " + i);
                    AppLog.d(TAG, "PAN sequence no: " + b);
                    return 0;
                }
                @Override
                public int cRFU1() throws RemoteException {
                    return 0;
                }
                @Override
                public int cRFU2() throws RemoteException {
                    return 0;
                }
            });
            AppLog.d(TAG, "aidlQpboc.setCallback res: " + res);
            if (res != TransUtlis.EMV_OK){
                return EmvResult.RF_SET_CALL_BACK_ERR;
            }
            res = aidlQpboc.setFinalSelectData(inputParam.getSelectData(), inputParam.getSelectLen());
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
            String currentAid = getCurrentAid();
            if (DataUtils.isNullString(currentAid)){
                return EmvResult.RF_GET_AID_ERR;
            }
            byte[] aidData = processListener.onSelectAid(currentAid);
            if (aidData == null || aidData.length == 0){
                return EmvResult.RF_MATCH_AIDLIST_ERR;
            }
            TlvList kernalList = processListener.setKernalData(PayDataUtil.KERNTYPE_QPBOC, aidData);
            AppLog.d(TAG, "entryL2 setKernalData ==== ");
            byte [] kernalData = kernalList.getBytes();
            if (kernalData != null ){
                setTLVDataList(kernalData,kernalData.length);
            }
            //callback app final Select aid
            if (rfProcessListener != null)
                rfProcessListener.finalAidSelect();

            aidlQpboc.setTLVData(0x9F39, new byte[]{0x07});

            //Set TTQ
            byte[] TTQ = new byte[4];
            byte[] getTTQ = preProcResult.getAucReaderTTQ();
            if (getTTQ == null) {
                AppLog.d(TAG, "getAucReaderTTQ = null");
                return EmvResult.RF_PRE_TTQ_ERR;
            }
            AppLog.d(TAG, "getAucReaderTTQ: " + BytesUtil.bytes2HexString(getTTQ));
            System.arraycopy(getTTQ, 0, TTQ, 0, 4);
            //Topwise qPBOC Terminal Transaction Qualifiers
            //Byte1 Bit7 : 0 – Full transaction flow in Contactless interface Not Support
            //Byte1 Bit1 : 0 – Offline Data Authentication for Online Authorisation Not Supported
            TTQ[0] &= (byte)0x3E; //0011 1110
            //Byte3 Bit7 : 1- Consumer Device CVM Supported
            TTQ[2] = 0x40; //0100 0000
            //Byte4 Bit8 : 1 - fDDA v1.0 Supported
            TTQ[3] = (byte)0x80; //1000 0000
            AppLog.d(TAG, "Set TTQ: " + BytesUtil.bytes2HexString(TTQ));
            aidlQpboc.setTLVData(0x9F66, TTQ);

            //Application Initialization (GPO)
            byte[] transPath = new byte[1];
            res = aidlQpboc.gpoProc(transPath);
            AppLog.d(TAG, "aidlQpboc.gpoProc res=====: " + res);
            if (res == TransUtlis.EMV_SELECT_NEXT_AID) {
                int ret = entryL2.delCandListCurApp();
                AppLog.d(TAG, "entryL2 delCandListCurApp ret: " + ret);
                if (ret == TransUtlis.EMV_OK) {
                    return EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR;
                } else {
                    return EmvResult.RF_DEL_CAND_LIST_ERR;
                }
            } else if (res == TransUtlis.EMV_SEE_PHONE) {
                return EmvResult.RF_CDCVM_SECOND_READ_CARD;
            } else if (res == TransUtlis.EMV_TRY_AGAIN) {
                return EmvResult.RF_TRANS_AGAIN_CHECK_CARD;
            } else if (res != TransUtlis.EMV_APPROVED && res != TransUtlis.EMV_DECLINED && res != TransUtlis.EMV_ONLINE_REQUEST) {
                return EmvResult.ABORT_TERMINATED;
            }
            AppLog.d(TAG, "aidlQpboc.gpoProc transPath =====: " + transPath[0]);
            if (transPath[0] != PayDataUtil.CLSS_TRANSPATH_EMV) {
                return EmvResult.ABORT_TERMINATED;
            }
            //read data
            //Read Application Data
            res = aidlQpboc.readData();
            AppLog.d(TAG, "aidlQpboc.readData res: " + res);
            if (res != TransUtlis.EMV_APPROVED && res != TransUtlis.EMV_DECLINED
                    && res != TransUtlis.EMV_ONLINE_REQUEST) {
                return EmvResult.RF_READ_DATA;
            }

            //unionpay refund
            if (inputParam.isSimple() && res ==TransUtlis.EMV_ONLINE_REQUEST){
                AppLog.d(TAG, "Simple process return : " + res);
                return EmvResult.ARQC;
            }


            byte[] tlvData = aidlQpboc.getTLVData(0x57);
            if (tlvData != null){
                String track2Data = BytesUtil.bytes2HexString(tlvData);
                AppLog.d(TAG, "track2 data: " + track2Data);
                String cardNo =  getPan(track2Data.split("F")[0]);
                if (!DataUtils.isNullString(cardNo)){
                    if (!rfProcessListener.onConfirmCardInfo(cardNo)){
                        return EmvResult.RF_CONFIRM_CARDNO_CANCEL;
                    }
                }
            }

            //Offline Data Authentication
            if (res == TransUtlis.EMV_APPROVED) {
                addCapk(currentAid);
                res = aidlQpboc.cardAuth();
                AppLog.d(TAG, "aidlQpboc.readData res: " + res);
            }
            //Cardholder Verification 持卡人验证结果
            byte[] cvmResBuf = aidlQpboc.getTLVData(0x9F34);
            if (cvmResBuf != null && cvmResBuf.length > 0) {
                AppLog.d(TAG, "cvmResBuf: " + BytesUtil.bytes2HexString(cvmResBuf));
                EmvEntity emvEntity;
                if (inputParam != null && inputParam.isHavePin()){
                    emvEntity = rfProcessListener.requestImportPin(PayDataUtil.PINTYPE_ONLINE, false, null);
                    if (!emvEntity.isResult()){
                        return EmvResult.RF_INPUT_PIN_CANCEL;
                    }
                }else { //check Cardholder Verification re
                    if (cvmResBuf[0] == 0x02) {
                        //Online enciphered PIN
                        emvEntity = rfProcessListener.requestImportPin(PayDataUtil.PINTYPE_ONLINE,false,null);
                        if (!emvEntity.isResult()){
                            return EmvResult.RF_INPUT_PIN_CANCEL;
                        }
                    }
                }
            }
            switch (res) {
                case TransUtlis.EMV_APPROVED:
                    //offline success 脱机批准
                    AppLog.d(TAG, "TC offline success");
                    return EmvResult.OFFLINE_APPROVED;
                case TransUtlis.EMV_ONLINE_REQUEST:
                    //online success 联机请求
                    AppLog.d(TAG, "ARQC online success");
                    return EmvResult.ARQC;
                case TransUtlis.EMV_DECLINED:
                    //transaction reject 交易拒绝
                    AppLog.d(TAG, "AAC transaction reject");
                    return EmvResult.TRANS_DENIED;
                default:
                    return EmvResult.ABORT_TERMINATED;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return EmvResult.ABORT_TERMINATED;
    }

    /**
     * set the kernal data
     * @param pTLVDatas
     * @param iDataLen
     * @return
     */
    private int setTLVDataList(byte[] pTLVDatas, int iDataLen) {
        AppLog.d(TAG, "setTLVDataList()");

        TlvList tlvlist = new TlvList();
        byte[] tlvsBuf = new byte[iDataLen];

        System.arraycopy(pTLVDatas, 0, tlvsBuf, 0, iDataLen);
        AppLog.d(TAG, "pTLVDatas: " + BytesUtil.bytes2HexString(tlvsBuf));

        tlvlist.fromBytes(tlvsBuf);
        if (tlvlist.getList() != null && tlvlist.getList().size() > 0) {
            for (Map.Entry<String, Tlv> entry : tlvlist.getList().entrySet()) {

                byte[] bTag = BytesUtil.hexString2Bytes(entry.getValue().getTag());
                AppLog.d(TAG, "bTag: " + BytesUtil.bytes2HexString(bTag));

                byte[] bTag4Bytes = new byte[4];
                java.util.Arrays.fill(bTag4Bytes, (byte)0);
                System.arraycopy(bTag, 0, bTag4Bytes, bTag4Bytes.length - bTag.length, bTag.length);
                AppLog.d(TAG, "bTag4Bytes: " + BytesUtil.bytes2HexString(bTag4Bytes));

                //The first parameter of 'BytesUtil.bytes2Int' must be 4 bytes
                int iTag = BytesUtil.bytes2Int(bTag4Bytes, true);
                AppLog.d(TAG, "iTag: " + iTag);

                AppLog.d(TAG, "Value: " + BytesUtil.bytes2HexString(entry.getValue().getValue()));

                try {
                    aidlQpboc.setTLVData(iTag, entry.getValue().getValue());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    private void addCapk(String aid) {
        AppLog.d(TAG, "addCapk(),  aid: " + aid);
        try {
            aidlQpboc.delAllRevoIPK();
            aidlQpboc.delAllCAPK();
            byte[] index = aidlQpboc.getTLVData(0x8F);
            if (index == null) {
                AppLog.d(TAG, "aidlQpboc.getTLVData capk index == null ");
                index = new byte[1];
                index[0] = 0x00;
            }
            AppLog.d(TAG, "capk index: " + index[0]);
            EmvCapk emvCapk = processListener.onSelectCapk(BytesUtil.hexString2Bytes(aid), index);
            if (emvCapk != null) {
                int res = aidlQpboc.addCAPK(emvCapk);
                AppLog.d(TAG, "add capk res: " + res);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
