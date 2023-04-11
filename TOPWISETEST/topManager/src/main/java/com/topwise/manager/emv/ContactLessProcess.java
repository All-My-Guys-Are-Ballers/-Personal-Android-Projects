package com.topwise.manager.emv;

import android.os.RemoteException;
import android.text.TextUtils;

import com.topwise.cloudpos.aidl.emv.level2.AidlEntry;
import com.topwise.cloudpos.aidl.emv.level2.Combination;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo;
import com.topwise.cloudpos.aidl.emv.level2.PreProcResult;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.cloudpos.struct.Tlv;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.api.ITransProcessListener;
import com.topwise.manager.emv.entity.Amounts;
import com.topwise.manager.emv.entity.EmvTransPraram;
import com.topwise.manager.emv.entity.InputParam;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.impl.TransProcess;

import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.manager.emv.utlis.TransUtlis;

import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;
import com.topwise.manager.utlis.DataUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 创建日期：2021/6/10 on 16:37
 * 描述:
 * 作者:wangweicheng
 */
public class ContactLessProcess {
    private static final String TAG = ContactLessProcess.class.getSimpleName();
    private static ContactLessProcess instance;
    private static TransProcess emvImpl;
    private ITransProcessListener rfProcessListener;
    private List<EmvAidParam> emvAidParams;
    private List<EmvCapkParam> emvCapkParams;
    private TransParam mTransParam;
    private EmvTerminalInfo emvTerminalInfo;
    private Amounts amounts;
    private EmvTransPraram emvTransData;
    private AidlPinpad pinPad = TopUsdkManage.getInstance().getPinpad(0);
    private AidlEntry entryL2 =  TopUsdkManage.getInstance().getEntry();

    private ContactLessProcess() {
//        jvmInit();
    }
    public static ContactLessProcess getInstance(TransProcess emvImpl) {
        if (instance == null) {
            instance = new ContactLessProcess();
            ContactLessProcess.emvImpl = emvImpl;
        }
        return instance;
    }

    public void setRfProcessListener(ITransProcessListener rfProcessListener) {
        this.rfProcessListener = rfProcessListener;
        AppLog.d(TAG, "setRfProcessListener===");
    }

    public void setEmvAidParams(List<EmvAidParam> emvAidParams) {
        this.emvAidParams = emvAidParams;
        AppLog.d(TAG, "setEmvAidParams===");
    }

    public void setEmvCapkParams(List<EmvCapkParam> emvCapkParams) {
        this.emvCapkParams = emvCapkParams;
        AppLog.d(TAG, "setEmvCapkParams===");
    }

    public void setConfig(TransParam transParam,EmvTerminalInfo emvTerminalInfo){
        this.mTransParam = transParam;
        this.emvTerminalInfo = emvTerminalInfo;
        AppLog.d(TAG, "setConfig===");
        AppLog.d(TAG, "setConfig=== transParam " +mTransParam.toString());
        AppLog.d(TAG, "setConfig=== emvTerminalInfo " +emvTerminalInfo.toString());
    }
    public EmvResult init(){
        try {
            int emvRest = -1;
            byte[] version = new byte[64];
            int i = 0;
            int ret = entryL2.getVersion(version, i);
            if (ret == PayDataUtil.EMV_OK) {
                    String buffer = BytesUtil.bytes2HexString(version);
                    if (buffer.contains("00")) {
                        version = BytesUtil.hexString2Bytes(buffer.split("00")[0]);
                    }
                    AppLog.d(TAG, "init entryLib version: " + new String(version, "gbk"));
            }
            //
            emvRest =  entryL2.initialize();
            AppLog.d(TAG, "init initialize: " + emvRest);
            entryL2.delAllCombination();
            if (emvAidParams == null || emvAidParams.size() == 0 ){
                return EmvResult.RF_AIDS_LIST_NULL_ERR;
            }
            AppLog.d(TAG, "init emvAidParams size : " + emvAidParams.size());
            // init aids
            for (EmvAidParam emvAid:emvAidParams) {
                AppLog.d(TAG, "EmvAidParam: " + emvAid.getAid());
                Combination combination = new Combination();
                combination.setUcAidLen(emvAid.getAid().length()/2);
                combination.setAucAID(BytesUtil.hexString2Bytes(emvAid.getAid()));
                combination.setUcPartMatch(1);

            //Kernel Identifier (Kernel ID) 81 06 43,  Russia Terminal Country Code:0643 defined by ISO 4217.
                if( emvAid.getAid().contains("A000000658")){  //MIR  9F2A 81 06 43
                    combination.setUcKernIDLen(3);
                    combination.setAucKernelID(new byte[]{(byte)0x81,0x06,0x43});
                }else{
                    combination.setUcKernIDLen(1);
                    combination.setAucKernelID(new byte[]{0x00});
                }


                //Byte 1
                //bit 6: 1 = EMV mode supported
                //bit 5: 1 = EMV contact chip supported
                //bit 3: 1 = Online PIN supported
                //bit 2: 1 = Signature supported
                //Byte 3
                //bit 8: 1 = Issuer Update Processing supported
                //bit 7: 1 = Consumer Device CVM supported
                byte[] TTQ = new byte[]{0x36, 0x00, (byte) 0xC0, 0x00}; ;
                combination.setAucReaderTTQ(TTQ);
                AppLog.d(TAG, "init AidParams FloorlimitCheck: " + emvAid.getFloorlimitCheck());
//                if (emvAid.getFloorlimitCheck() == 1) {
                if (1 == emvAid.getFloorlimitCheck() ) {
                    combination.setUcTermFLmtFlg(PayDataUtil.CLSS_TAG_EXIST_WITHVAL);
                    combination.setUlTermFLmt(emvAid.getFloorLimit());
                } else {
                    combination.setUcTermFLmtFlg(PayDataUtil.CLSS_TAG_NOT_EXIST);
                }


                AppLog.d(TAG, "init AidParams RdCVMLmtFlg: " + emvAid.getRdCVMLmtFlg());
                //if (aid.isRdCVMLimitFlg()) {
                if (1 == emvAid.getRdCVMLmtFlg()) {
                    combination.setUcRdCVMLmtFlg(PayDataUtil.CLSS_TAG_EXIST_WITHVAL);
                    combination.setAucRdCVMLmt(BytesUtil.hexString2Bytes(String.format("%012d", emvAid.getRdCVMLmt())));
                    AppLog.d(TAG, "initData aid.getRdCVMLimit(): " + String.format("%012d", emvAid.getRdCVMLmt()));
                } else {
                    combination.setUcRdCVMLmtFlg(PayDataUtil.CLSS_TAG_NOT_EXIST);
                }
                AppLog.d(TAG, "init AidParams RdClssTxnLmtFlg: " + emvAid.getRdClssTxnLmtFlg());
                //if (aid.isRdClssTxnLimitFlg()) {
                if (1 == emvAid.getRdClssTxnLmtFlg()) {
                    combination.setUcRdClssTxnLmtFlg(PayDataUtil.CLSS_TAG_EXIST_WITHVAL);
                    if (emvAid.getAid().startsWith("A000000004" )){
                        //For Mastercard - Please set to only Greater than amount will be rejected
                        String temp = String.format("%012d",(emvAid.getRdClssTxnLmt() + 1));
                        combination.setAucRdClssTxnLmt(BytesUtil.hexString2Bytes(temp));
                        AppLog.emvd(TAG, "initData Mastercard RdClssTxnLimit(): " + temp);
                    }else {
                        combination.setAucRdClssTxnLmt(BytesUtil.hexString2Bytes(String.format("%012d", emvAid.getRdClssTxnLmt())));
                        AppLog.d(TAG, "initData aid.getRdClssTxnLimit(): " + String.format("%012d", emvAid.getRdClssTxnLmt()));
                    }

                } else {
                    combination.setUcRdClssTxnLmtFlg(PayDataUtil.CLSS_TAG_NOT_EXIST);
                }

                AppLog.d(TAG, "init AidParams RdClssFLmtFlg: " + emvAid.getRdClssFLmtFlg());
                //if (aid.isRdClssFloorLimitFlg()) {
                if (1 == emvAid.getRdClssFLmtFlg()) {
                    combination.setUcRdClssFLmtFlg(PayDataUtil.CLSS_TAG_EXIST_WITHVAL);
                    combination.setAucRdClssFLmt(BytesUtil.hexString2Bytes(String.format("%012d", emvAid.getRdClssFLmt())));
                    AppLog.d(TAG, "initData aid.getRdClssFloorLimit(): " + String.format("%012d", emvAid.getRdClssFLmt()));
                } else {
                    combination.setUcRdClssFLmtFlg(PayDataUtil.CLSS_TAG_NOT_EXIST);
                }

                combination.setUcZeroAmtNoAllowed(0);
                combination.setUcStatusCheckFlg(0);
                combination.setUcCrypto17Flg(1);
                combination.setUcExSelectSuppFlg(0);

                AppLog.d(TAG, "initData combination.getAucRdClssTxnLmt(): " + BytesUtil.bytes2HexString(combination.getAucRdClssTxnLmt()));
                AppLog.d(TAG, "initData combination.getAucRdClssFLmt(): " + BytesUtil.bytes2HexString(combination.getAucRdClssFLmt()));
                AppLog.d(TAG, "initData combination.getAucRdCVMLmt(): " + BytesUtil.bytes2HexString(combination.getAucRdCVMLmt()));
                emvRest = entryL2.addCombination(combination);
                AppLog.d(TAG, "init addCombination: " + emvRest);
            }
            //==
            AppLog.d(TAG, "init check mTransParam ");
            if (mTransParam == null){
                AppLog.d(TAG, "init mTransParam is null");
                return EmvResult.RF_TRANS_PARAM_NULL_ERR;
            }
            AppLog.d(TAG, "init preProcessing check mTransParam ");
            emvRest = entryL2.preProcessing(mTransParam);
            AppLog.d(TAG, "init preProcessing: " + emvRest);
            if (emvRest != TransUtlis.EMV_OK){
                //Use other interface
                AppLog.e(TAG, "init ErrorCode: " +    entryL2.getErrorCode());

                if (emvRest == TransUtlis.CLSS_USE_CONTACT){
                    return EmvResult.RF_CHECK_OTHER_CONTACT;
                }
                return EmvResult.RF_PRE_PROCESS_ERR;
            }
            emvRest = entryL2.buildCandidate(0, 0);
            AppLog.d(TAG, "init buildCandidate: " + emvRest);
            if (emvRest != TransUtlis.EMV_OK){
                AppLog.d(TAG, "init buildCandidate fail, error code: " + emvRest);
                // need to handle 6A82 error
                int errorCode = entryL2.getErrorCode();
                AppLog.d(TAG, "init buildCandidate getErrorCode: " + errorCode);
                return EmvResult.RF_BUILD_CANDIDATA_ERR;
            }

            return EmvResult.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return EmvResult.RF_INIT_ERR;
        }

    }

    private InputParam inputParam;
    /**
     *
     * @param emvTransData
     * @return
     */
    public EmvResult emvProcess(EmvTransPraram emvTransData){
        this.emvTransData = emvTransData;
        AppLog.d(TAG, "emvProcess===");
        try {
            EmvResult emvResult = EmvResult.ABORT_TERMINATED;
            AppLog.d(TAG, "emvProcess requestImportAmount " +emvTransData.toString());
            amounts = rfProcessListener.requestImportAmount();
            for (;;){  //if return select aid again will continue
                AppLog.d(TAG, "emvProcess startFinalSelect");
                emvResult = finalSelectAid();
                AppLog.d(TAG, "emvProcess finalSelectAid emvResult " + emvResult.toString());
                if (emvResult != EmvResult.OK){
                    return emvResult;
                }
                //start select kernal
                BaseTrans baseTrans = checkKernalType(inputParam.getKernType());
                if (baseTrans == null){
                    return EmvResult.RF_NOT_SUPPORT_KERNAL_ERR;
                }
                //set listener
                baseTrans.setProcessListener(new ProcessListener() {
                    @Override
                    public TlvList setKernalData(byte aucType, byte[] aidData) {
                        AppLog.d(TAG, "setKernalData aucType: " + aucType);
                        return setTransKernelData(aucType,aidData);
                    }
                    @Override
                    public byte[] onSelectAid(String rid) {
                        AppLog.d(TAG, "onSelectAid rid: " + rid);
                        return checkAid(rid);
                    }
                    @Override
                    public EmvCapk onSelectCapk(byte[] rid, byte[] index) {
                        AppLog.d(TAG, "onSelectCapk ======" );
                        return selectCapk(rid,index);
                    }
                });
                //set l3App listener
                baseTrans.setRfProcessListener(rfProcessListener);
                //set input Params
                baseTrans.setInputParam(inputParam);
                //start Trans process
                emvResult =  baseTrans.start();
                AppLog.d(TAG, "emvProcess start return " + emvResult.toString());
                if (EmvResult.RF_AID_FINAL_SELECT_AGAIN_ERR == emvResult){
                    AppLog.d(TAG, "emvProcess Select aid again ");
                    continue;
                }else {
                    return emvResult;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return EmvResult.RF_PROCESS_ERR;
        }
    }

    /**
     * check select aid
     * @param rid
     * @return
     */
    private byte[] checkAid(String rid){
        AppLog.d(TAG, "checkAid rid: " + rid);
        for (EmvAidParam e: emvAidParams) {
            if (rid.startsWith(e.getAid())){
                AppLog.d(TAG, "onSelectAid rid: === " + e.toString());
                return EmvAidParam.getTlvList(e).getBytes();
            }
        }
        return null;
    }
    /**
     *
     * @param kernalType
     * @return
     */
    private BaseTrans checkKernalType(byte kernalType){
        AppLog.d(TAG, " checkKernalType kernalType==== " + kernalType);
        switch (kernalType){
            case PayDataUtil.KERNTYPE_QPBOC:
                return new TransUnionPay();
            case PayDataUtil.KERNTYPE_MC:
                return new TransPayPass();
            case PayDataUtil.KERNTYPE_VISA:
                return new TransPayWave();
            case PayDataUtil.KERNTYPE_PURE:
                return new TransPurePay();
            case PayDataUtil.KERNTYPE_AMEX:
                return new TransAMexPay();
            case PayDataUtil.KERNTYPE_DPAS:
                return new TransDpasPay();
            case PayDataUtil.KERNTYPE_MIR:
                return new TransMirPay();
            case PayDataUtil.KERNTYPE_RUPAY:
                return new TransRuPay();
            default:
                return null;
        }
    }
    /**
     * Select Aid Again
     * @return
     */
    private EmvResult finalSelectAid(){
        try {
            int emvRest = -1;
            AppLog.d(TAG, " finalSelect Again");
            byte[] ucKernType = new byte[1];
            byte[] outData = new byte[300];
            byte[] data = null;
            int[] len = new int[1];
            boolean isSelectOk = false;
            int count = 0;
            String sSelectAid;
            while (count++ < emvAidParams.size()){
                Arrays.fill(outData, (byte) 0x00);
                sSelectAid = "";
                emvRest = entryL2.finalSelect(ucKernType, outData, len);
                AppLog.d(TAG, "finalSelect Again: " + emvRest);
                if (emvRest != PayDataUtil.EMV_OK) {
                    isSelectOk = false;
                    if (emvRest == PayDataUtil.CLSS_USE_CONTACT || emvRest == PayDataUtil.ICC_CMD_ERR) {
                        break;
                    } else {
                        AppLog.d(TAG, "finalSelect Again fail, error code: " + emvRest);
                        int errorCode = entryL2.getErrorCode();
                        AppLog.d(TAG, "finalSelect Again  getErrorCode: " + errorCode);
                        emvRest = entryL2.delCandListCurApp();
                        AppLog.d(TAG, "finalSelect Again  delCandListCurApp : " + emvRest);
                    }
                } else {
                    isSelectOk = true;
                    if (len[0] > 0) {
                        data = new byte[len[0]];
                        System.arraycopy(outData, 0, data, 0, len[0]);
                    }
                    break;
                }
            }
            if (!isSelectOk){
                return EmvResult.RF_AID_FINAL_SELECT_ERR;
            }
            inputParam = new InputParam();

            PreProcResult preProcResult = new PreProcResult();
            emvRest = entryL2.getPreProcResult(preProcResult);
            AppLog.d(TAG, "emvProcess getPreProcResult ret: " + emvRest);
            if (emvRest != TransUtlis.EMV_OK){
                return EmvResult.RF_PRE_PROCESS_RETURN_ERR;
            }
            sSelectAid = BytesUtil.bytes2HexString(preProcResult.getAucAID());
            AppLog.d(TAG, "emvProcess preProcResult.getAucReaderTTQ(): " + BytesUtil.bytes2HexString(preProcResult.getAucReaderTTQ()));
            AppLog.d(TAG, "emvProcess preProcResult.getAucAID: " + sSelectAid);
            if( sSelectAid.contains("A000000658"))  // when is mir kernel and set kernel id 0x14
            {
                ucKernType[0] = 0x14;
            }
            AppLog.d(TAG, "emvProcess Aid outData: " + BytesUtil.bytes2HexString(data));
            AppLog.d(TAG, "emvProcess onUpdateKernelType: " + ucKernType[0]);

            //update kernal Type
            rfProcessListener.onUpdateKernelType(ucKernType[0]);

            //set input Param
            inputParam.setSelectLen(len[0]);
            inputParam.setSelectData(data);
            inputParam.setPreProcResult(preProcResult);
            inputParam.setTransParam(mTransParam);
            inputParam.setKernType(ucKernType[0]);

            if (emvTransData !=null){
                inputParam.setHavePin(emvTransData.isHavePin());
                inputParam.setSimple(emvTransData.isSimple());
            }


            return EmvResult.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return EmvResult.RF_AID_FINAL_SELECT_ERR;
        }
    }
    /**
     * assets 文件保存
     */
    private TlvList handleKernalData(byte[] aid,byte ucType) {
        PayDataUtil dataUtil = new PayDataUtil();
        //获取默认参数
        TlvList list = dataUtil.getDefaultKernal();
        //Amount, Authorised (Numeric)
        AppLog.d(TAG, "handleKernalData amount: " + amounts.toString());
        list.addTlv("9F02",amounts.getTransAmount());
        //add other amount wwc
        list.addTlv("9F03",amounts.getCashBackAmount());
        //Transaction Type
        String tradeType = BytesUtil.bytes2HexString(new byte[]{emvTransData.getTransType()});
        AppLog.d(TAG, "handleKernalData tradeType: " + tradeType);
        list.addTlv("9C",tradeType);

        //Transaction Sequence Counter
        String sequenceCounter = dataUtil.getSequenceCounter();
        list.addTlv("9F41",sequenceCounter);
        AppLog.d(TAG, "9F41 Transaction Sequence Counter: " + sequenceCounter);
        //Transaction Date
        String date = emvTransData.getData();
        list.addTlv("9A",date);
        AppLog.d(TAG, "9A Transaction Date: " + date);
        //Transaction Time
        String time = emvTransData.getTime();
        AppLog.d(TAG, "9F21 Transaction Time: " + time);
        list.addTlv("9F21",time);

        //The getRandom function returns a fixed 8 byte random number
        byte[] random = new byte[0];
        try {
            random = pinPad.getRandom();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        byte[] unpredictableNum = new byte[4];
        System.arraycopy(random, 0, unpredictableNum, 0, 4);
        list.addTlv("9F37", unpredictableNum);
        AppLog.d(TAG, "9F37 Random: " + BytesUtil.bytes2HexString(unpredictableNum));

        //rupay TLV
        //Max Fill Volume
        list.addTlv("DF16","02");

        //==========

        //  ((unsigned char*)"\x9F\x09", 2, (unsigned char*)"\x00\x02", 2);//Application Version Number
        byte[] aucAppVersion = emvTerminalInfo.getAucAppVersion();
        if (aucAppVersion != null){
            String sAppVersion = BytesUtil.bytes2HexString(aucAppVersion);
            AppLog.d(TAG,"9F09 Application Version Number= " + sAppVersion);
            list.addTlv("9F09",sAppVersion);
        }else {
            list.addTlv("9F09","0002");
        }

        //  ((unsigned char*)"\x9F\x15", 2, (unsigned char*)"\x41\x31", 2);//Merchant Category Code
        byte[] aucMerchantCategoryCode = emvTerminalInfo.getAucMerchantCategoryCode();
        if (aucMerchantCategoryCode !=null){
            String sMerchantCategoryCode = BytesUtil.bytes2HexString(aucMerchantCategoryCode);
            AppLog.d(TAG,"9F15 Merchant Category Code= " + sMerchantCategoryCode);
            list.addTlv("9F15",sMerchantCategoryCode);
        }

        //     ((unsigned char*)"\x9F\x1A", 2, (unsigned char*)"\x03\x56", 2);//Terminal Country Code
        byte[] aucTransCurCode = emvTerminalInfo.getAucTerminalCountryCode();
        String sTransCurCode = BytesUtil.bytes2HexString(aucTransCurCode);
        AppLog.d(TAG,"9F1A Terminal Country Code= " + sTransCurCode);
        list.addTlv("9F1A",sTransCurCode);
        //  ((unsigned char*)"\x9F\x1C", 2, (unsigned char*)"\x31\x32\x33\x34\x35\x36\x37\x38", 8);//Terminal ID
        String aucTerminalID = emvTerminalInfo.getAucTerminalID();
        AppLog.d(TAG,"9F1C Terminal ID= " + aucTerminalID);
        list.addTlv("9F1C",aucTerminalID);
        //     ((unsigned char*)"\x9F\x33", 2, (unsigned char*)"\xE0\x68\xC8", 3);//Terminal Capabilities
//        list.addTlv("9F33","E068C8"); Terminal Capabilities
        byte[] aucTerminalCapabilities = emvTerminalInfo.getAucTerminalCapabilities();
        if (aucTerminalCapabilities != null){
            String sTerminalCapabilities = BytesUtil.bytes2HexString(aucTerminalCapabilities);
            AppLog.d(TAG,"9F33 Terminal Capabilities= " + sTerminalCapabilities);
            list.addTlv("9F33",sTerminalCapabilities); //E0F8C8
        }


        byte[] aucAddtionalTerminalCapabilities = emvTerminalInfo.getAucAddtionalTerminalCapabilities();
        if (aucAddtionalTerminalCapabilities != null){
            String AddtionalTerminalCapabilities = BytesUtil.bytes2HexString(aucAddtionalTerminalCapabilities);
            //((unsigned char*)"\x9F\x40", 2, (unsigned char*)"\xFF\xC0\xF0\xA0\x01", 5);//DV_122_00_02 ask B2b7=1 and DF3A B2b7=0
            //        list.addTlv("9F40","FFC0F0A001"); Additional Terminal Capabilities
            list.addTlv("9F40",AddtionalTerminalCapabilities); //paynext FF80F00001
            AppLog.d(TAG,"9F40 Additional Terminal Capabilities= " + AddtionalTerminalCapabilities);
        }

        //((unsigned char*)"\xDF\x3A", 2, (unsigned char*)"\x00\x40\x00\x00\x00", 5);
        list.addTlv("DF3A","0040000000");
        //((unsigned char*)"\x9F\x35", 2, (unsigned char*)"\x22", 1); Terminal Type
        byte ucTerminalType = emvTerminalInfo.getUcTerminalType();
        AppLog.d(TAG,"9F35 Terminal Type= " + Integer.toHexString(ucTerminalType));
        list.addTlv("9F35",Integer.toHexString(ucTerminalType));
        //((unsigned char*)"\x5F\x2A", 2, (unsigned char*)"\x03\x56", 2);//Transaction Currency Code
        AppLog.d(TAG,"Transaction Currency Code= " + sTransCurCode);
        list.addTlv("5F2A",sTransCurCode);
        //((unsigned char*)"\x5F\x36", 2, (unsigned char*)"\x02", 1);//Transaction Currency Exponent
        byte ucTransCurrencyExp = emvTerminalInfo.getUcTransCurrencyExp();
        AppLog.d(TAG,"5F36 Transaction Currency Exponent= " + Integer.toHexString(ucTransCurrencyExp));
        list.addTlv("5F36",Integer.toHexString(ucTransCurrencyExp));
        //((unsigned char*)"\xDF\x81\x31", 3, (unsigned char*)"\x05", 1);//Max Target Percentage. Tag defined by self
        byte ucMaxTargetPercentage = emvTerminalInfo.getUcMaxTargetPercentage();
        AppLog.d(TAG,"DF8131 Max Target Percentage= " + Integer.toHexString(ucMaxTargetPercentage));
        list.addTlv("DF8131",Integer.toHexString(ucMaxTargetPercentage));
        //(unsigned char*)"\xDF\x81\x32", 3, (unsigned char*)"\x00", 1);//Target Percentage
        byte ucTargetPercentage = emvTerminalInfo.getUcTargetPercentage();
        AppLog.d(TAG,"DF8132 Target Percentage= " + Integer.toHexString(ucTargetPercentage));
        list.addTlv("DF8132",Integer.toHexString(ucTargetPercentage));
        //((unsigned char*)"\xDF\x81\x33", 3, (unsigned char*)"\x00\x00\x00\x00\x05\x00", 6);// Threshold Value
        int unThresholdValue = emvTerminalInfo.getUnThresholdValue();
        String sThresholdValue = String.format("%012d", unThresholdValue);
        AppLog.d(TAG,"DF8133 Threshold Value= " + sThresholdValue);
        list.addTlv("DF8133",sThresholdValue);

        //update 9F1E /9F35 /9F1A /5F2A /9F33 /9F40
        String aucIFDSerialNumber = emvTerminalInfo.getAucIFDSerialNumber();
        if (!DataUtils.isNullString(aucIFDSerialNumber) && aucIFDSerialNumber.length() == 8) {
            AppLog.d(TAG,"9F1E Interface Device (IFD) Serial Number= " + sThresholdValue);
            list.addTlv("9F1E", aucIFDSerialNumber.getBytes());
        }
        //Dpas 9F01============================
        String aucTerminalAcquireID = emvTerminalInfo.getAucTerminalAcquireID();
        if (!DataUtils.isNullString(aucTerminalAcquireID) ) {
            AppLog.d(TAG,"9F01 Acquirer Identifier= " + aucTerminalAcquireID);
            list.addTlv("9F01", aucTerminalAcquireID.getBytes());
        }
        //9F4E Merchant Name and Location
        byte[] aucMerchantNameLocation = emvTerminalInfo.getAucMerchantNameLocation();
        if (aucMerchantNameLocation != null && aucMerchantNameLocation.length >0 ) {
            AppLog.d(TAG,"9F4E Merchant Name and Location= " + aucMerchantNameLocation);
            list.addTlv("9F4E", aucMerchantNameLocation);
        }
        //============================
    //    aid = null;
        TlvList aidList = new TlvList();
        aidList.fromBytes(aid);
        //Reader Contactless Floor Limit 非接触读写器脱机最低限额
        //9F1B 是EMV接触的 floor limit. DF8123 非接最低脱机限额
        String data = null;
        if(aidList.getTlv("DF19")!=null){
            data = aidList.getTlv("DF19").getHexValue();
        }
        AppLog.d(TAG, "DF8123(DF19) Reader Contactless Floor Limit : " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8123",data);
            String tag9F1B = Long.toHexString(Long.valueOf(data));
            String stag9F1B = TransUtlis.convertorVale(tag9F1B, 8, '0');
            list.addTlv("9F1B",stag9F1B); //IC 卡消费时终端允许的最低脱机限额
            AppLog.d(TAG, "9F1B Floor Limit : " + stag9F1B);

            list.addTlv("DF51",data); //mir
            AppLog.d(TAG, "DF51 Floor Limit : " + data);
        } else {
            list.addTlv("DF8123","000000030000");
            list.addTlv("9F1B","00002710");
            list.addTlv("DF51","000000030000"); //mir
        }
        //Reader Contactless Transaction Limit (No On-device CVM) M card 非接触读写器交易限额
        data = null;
        if(aidList.getTlv("DF20")!=null){
            data = aidList.getTlv("DF20").getHexValue();
        }

        AppLog.d(TAG, "DF8124(DF20)/DF8125/DF4C Reader Contactless Transaction Limit: " + data);
        AppLog.d(TAG, "DF8125(DF20) Reader Contactless Transaction Limit: " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8124",data);
            list.addTlv("DF8125",data); //Reader Contactless Transaction Limit (On-device CVM) M card 手机pay的限额
            list.addTlv("DF4C",data); //rupay tag
            list.addTlv("DF53",data); //mir
        } else {
            list.addTlv("DF8124","000099999999");
            list.addTlv("DF8125","000099999999"); //Reader Contactless Transaction Limit (On-device CVM) M card 手机pay的限额
            list.addTlv("DF4C","000999999999");
            list.addTlv("DF53","000999999999");
        }

        //Reader CVM Required Limit  读写器持卡人验证方法（CVM）所需限制
        data = null;
        if(aidList.getTlv("DF21")!=null){
            data = aidList.getTlv("DF21").getHexValue();
        }
        AppLog.d(TAG, "DF8126(DF21)/DF4D Reader CVM Required Limit: " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8126",data);
            list.addTlv("DF4D",data); // rupay
            list.addTlv("DF52",data); // mir
        } else {
            list.addTlv("DF8126","000000030000");
            list.addTlv("DF4D","000000500000"); // rupay
            list.addTlv("DF52","000000500000"); // mir
        }
        /**
         * Visa TACs	TAC Denial-0010000000
         * 	            TAC Online-DC4004F800
         * 	            TAC Default-DC4000A800
         * MasterCard	TAC Denial-0010000000
         * 	            TAC Online-FE50BCF800
         * 	            TAC Default-FE50BCA000
         * Masetro	TAC Denial-0018000000
         * 	        TAC Online-FE50BCF800
         * 	        TAC Default-FE50BCA000
         */
        data = null;
        if(aidList.getTlv("DF11")!=null){ //TAC Default
            data = aidList.getTlv("DF11").getHexValue();
        }
        AppLog.d(TAG, "DF8120(DF11) TAC Default: " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8120",data);
        } else {
            byte[] aucTACDefault = emvTerminalInfo.getAucTACDefault();
            if (aucTACDefault != null){
                String sTACDefault = BytesUtil.bytes2HexString(aucTACDefault);
                AppLog.d(TAG, "DF8120 TerminalInfo TAC Default: " + sTACDefault);
                list.addTlv("DF8120",sTACDefault);
            }else {
                list.addTlv("DF8120","0000000000");
            }
        }
        //TAC-Online
        data = null;
        if(aidList.getTlv("DF12")!=null){
            data = aidList.getTlv("DF12").getHexValue();
        }

        AppLog.d(TAG, "DF8122(DF12) TAC-Online: " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8122",data);
        } else {
            byte[] aucTACOnline = emvTerminalInfo.getAucTACOnline();
            if (aucTACOnline != null){
                String sTACOnline = BytesUtil.bytes2HexString(aucTACOnline);
                AppLog.d(TAG, "DF8122 TerminalInfo TAC-Online: " + sTACOnline);
                list.addTlv("DF8122",sTACOnline);
            }else {
                list.addTlv("DF8122","0000000000");
            }
        }
        //TAC-Denial
        data = null;
        if(aidList.getTlv("DF13")!=null){
            data = aidList.getTlv("DF13").getHexValue();
        }
        AppLog.d(TAG, "DF8121(DF13) TAC-Denial: " + data);
        if (!TextUtils.isEmpty(data)) {
            list.addTlv("DF8121",data);
        } else {
            byte[] aucTACDenial = emvTerminalInfo.getAucTACDenial();
            if (aucTACDenial != null){
                String sTACDenial = BytesUtil.bytes2HexString(aucTACDenial);
                AppLog.d(TAG, "DF8121 TerminalInfo TAC-Denial: " + sTACDenial);
                list.addTlv("DF8121",sTACDenial);
            }else {
                list.addTlv("DF8121","0000000000");
            }
        }
        //

        //update form App
        AppLog.d(TAG, "handleKernalData check requestKernalListTLV" );
        TlvList AppList = rfProcessListener.requestKernalListTLV(ucType);
        if (AppList != null && AppList.getList() != null && AppList.getList().size() > 0) {
            AppLog.d(TAG,"set app tlv " +AppList.toString());
            for (Map.Entry<String, Tlv> entry : AppList.getList().entrySet()) {
                AppLog.d(TAG, "AppList settlv out: " + entry.getValue().toHex());
                list.addTlv(entry.getValue());
            }
            AppList.clear();
        }
        AppLog.d(TAG, "handleKernalData list : " + list.toString());
        return list;
    }
    private TlvList setTransKernelData(byte ucType, byte[] aid){
        AppLog.d(TAG,"setTransKernelData " + ucType +" /aid "+ BytesUtil.bytes2HexString(aid));
        switch (ucType){
            case PayDataUtil.KERNTYPE_MC:
            case PayDataUtil.KERNTYPE_VISA:
            case PayDataUtil.KERNTYPE_PURE:
            case PayDataUtil.KERNTYPE_AMEX:
            case PayDataUtil.KERNTYPE_QPBOC:
            case PayDataUtil.KERNTYPE_RUPAY:
            case PayDataUtil.KERNTYPE_MIR:
                return handleKernalData(aid,ucType);
            default:
               return null;
        }
    }

    /**
     *
     * @param rid
     * @param index
     * @return
     */
    private EmvCapk selectCapk(byte[] rid, byte[] index){
        AppLog.d(TAG, "onSelectCapk rid: " + BytesUtil.bytes2HexString(rid));
        AppLog.d(TAG, "onSelectCapk ridindex: " + Integer.toHexString(index[0] & 0xFF));
        byte[] ridData = new byte[5];
        System.arraycopy(rid, 0, ridData, 0, ridData.length);
        String ridindex = new StringBuffer(BytesUtil.bytes2HexString(ridData))
                .append(Integer.toHexString(index[0] & 0xFF)).toString().toUpperCase();
        AppLog.d(TAG, "onSelectCapk ridindex: " + ridindex);
        EmvCapk emvCapk = null;
        for (EmvCapkParam capk: emvCapkParams) {
            if (ridindex.equals(capk.getRIDKeyID())){
                AppLog.d(TAG, "onSelectCapk capk: " + capk.toString());
                emvCapk = new EmvCapk();
                emvCapk.setRID(rid);
                emvCapk.setKeyID(index[0]);

                byte[] tempExpDate = new byte[3]; //YYMMDD
                byte[] bcdExpDate =  BytesUtil.hexString2Bytes(capk.getExpDate());
                if (4 == bcdExpDate.length) { //2009123
                    System.arraycopy(bcdExpDate, 1, tempExpDate, 0, 3);
                } else if (8 == bcdExpDate.length) {
                    byte[] bcdExpDatea =  BytesUtil.hexString2Bytes(new String(bcdExpDate));
                    System.arraycopy(bcdExpDatea, 1, tempExpDate, 0, 3);
                } else {  // Default period of validity
                    //301231
                    tempExpDate[0] = 0x30;
                    tempExpDate[1] = 0x12;
                    tempExpDate[2] = 0x31;
                }
                AppLog.d(TAG, "onSelectCapk tempExpDate(): " + BytesUtil.bytes2HexString(tempExpDate));
                emvCapk.setExpDate(tempExpDate);
                emvCapk.setHashInd(capk.getHashInd());
                emvCapk.setArithInd(capk.getArithInd());
                emvCapk.setCheckSum(PayDataUtil.getCAPKChecksum(capk));

                byte[] orgData = BytesUtil.hexString2Bytes(capk.getModul());
                if (orgData != null) {
                    emvCapk.setModul(orgData);
                }
                orgData = BytesUtil.hexString2Bytes(capk.getExponent());
                if (orgData != null) {
                    emvCapk.setExponent(orgData);
                }
                AppLog.d(TAG, "onSelectCapk return emvCapk: " + emvCapk.toString());
                return emvCapk;
            }
        }
        return null;
    }

    //
    public interface ProcessListener{

        /**
         *
         * @param aucType
         * @param aidData
         * @return
         */
        TlvList setKernalData(byte aucType, byte[] aidData);

        /**
         * aid 选择
         * @param rid 卡片aid
         * @return aid参数
         */
        byte[] onSelectAid(String rid);
        /**
         *
         * @param rid 9F06
         * @param index 9F22
         * @return EmvCapk
         */
        EmvCapk onSelectCapk(byte[] rid, byte[] index);
    }
}
