package com.topwise.manager.emv;

import android.os.RemoteException;
import android.widget.EditText;

import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.EmvCallback;
import com.topwise.cloudpos.aidl.emv.level2.EmvCandidateItem;
import com.topwise.cloudpos.aidl.emv.level2.EmvCapk;
import com.topwise.cloudpos.aidl.emv.level2.EmvKernelConfig;
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.api.ITransProcessListener;
import com.topwise.manager.emv.entity.Amounts;
import com.topwise.manager.emv.entity.EmvEntity;
import com.topwise.manager.emv.entity.EmvTransPraram;
import com.topwise.manager.emv.enums.EOnlineResult;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.impl.TransProcess;
import com.topwise.manager.emv.utlis.EmvDefinition;
import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.manager.emv.utlis.TransUtlis;

import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;
import com.topwise.manager.utlis.DataUtils;
import com.topwise.toptool.api.packer.ITlv;
import com.topwise.toptool.api.packer.TlvException;
import com.topwise.toptool.impl.TopTool;

import java.util.List;

/**
 * 创建日期：2021/6/10 on 16:36
 * 描述:
 * 作者:wangweicheng
 */
public class ContactEmvProcess extends EmvCallback.Stub{
    private static final String TAG = ContactEmvProcess.class.getSimpleName();
    private static ContactEmvProcess instance;
    private static TransProcess emvImpl;
    private ITransProcessListener emvProcessListener;
    private List<EmvAidParam> emvAidParams;
    private List<EmvCapkParam> emvCapkParams;
    private EmvKernelConfig emvKernelConfig;
    private EmvTerminalInfo emvTerminalInfo;
    private EmvTransPraram emvTransData;
    private AidlPinpad pinPad = TopUsdkManage.getInstance().getPinpad(0);
    private AidlEmvL2 emvL2 = TopUsdkManage.getInstance().getEmv();

    private ContactEmvProcess() {
//        jvmInit();
    }

    public static ContactEmvProcess getInstance(TransProcess emvImpl) {
        if (instance == null) {
            instance = new ContactEmvProcess();
            ContactEmvProcess.emvImpl = emvImpl;
        }
        return instance;
    }

    public void setEmvProcessListener(ITransProcessListener emvProcessListener) {
        this.emvProcessListener = emvProcessListener;
    }

    /**
     *
     * @return
     */
    public EmvResult init(){
        AppLog.d(TAG,"init");
        int emvRest = -1;
        try {
            emvRest = emvL2.EMV_Initialize();
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init filed res = " + emvRest);
                return EmvResult.IC_INIT_ERR;
            }
            emvRest = emvL2.EMV_SetKernelType((byte) 0x00);
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init SetKernelType filed res = " + emvRest);
                return EmvResult.IC_SET_KCONFIG_ERR;
            }
            emvRest = emvL2.EMV_SetCallback(this);
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init SetCallback filed res = " + emvRest);
                return EmvResult.IC_INIT_ERR;
            }

            emvRest = emvL2.EMV_SetKernelConfig(emvKernelConfig);
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init SetKernelConfig filed res = " + emvRest);
                return EmvResult.IC_SET_KCONFIG_ERR;
            }
            emvRest = emvL2.EMV_SetTerminalInfo(emvTerminalInfo);
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init SetTerminalInfo filed res = " + emvRest);
                return EmvResult.IC_SET_TERMINAL_INFO_ERR;
            }
            emvRest = emvL2.EMV_SetSupport_PBOC((byte)0, (byte)0, 0);
            if (emvRest != TransUtlis.EMV_OK) {
                AppLog.d(TAG,"init SetSupport_PBOC filed res = " + emvRest);
                return EmvResult.IC_SET_SUPPORT_PBOC_ERR;
            }
            //
            if (emvAidParams != null && emvAidParams.size() > 0){
                emvL2.EMV_DelAllAIDList();
                for (EmvAidParam aid : emvAidParams){
                    AppLog.d(TAG, "init AddAIDList EmvAidParam: " + aid.getAid());
                    byte[] aucAid = BytesUtil.hexString2Bytes(aid.getAid());
                    emvL2.EMV_AddAIDList(aucAid, (byte) aucAid.length, (byte) 1);
                }
            }else {
                return EmvResult.IC_AIDS_LIST_NULL_ERR;
            }

            //Building the Candidate List
            //Create a list of ICC applications that are supported by the terminal.
            emvRest = emvL2.EMV_AppCandidateBuild((byte) 0);
            if (emvRest != TransUtlis.EMV_OK) { ////visa 卡 ADVT case 8 fallback 返回码是 10
                AppLog.d(TAG,"init AppCandidateBuild filed res = " + emvRest);
                if (EmvDefinition.EMV_NO_APP == emvRest){
                    return EmvResult.IC_EMV_FALL_BACK;
                }else {
                    return EmvResult.IC_BUILD_CANDIDATE_ERR;
                }
            }
            return EmvResult.OK;
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.d(TAG,"init Exception = " + e.getMessage());
            return EmvResult.IC_INIT_ERR;
        }
    }

    /**
     *
     * @param emvkernelConfig
     * @param emvterminalInfo
     * @return
     */
    public void setKernelConfig(EmvKernelConfig emvkernelConfig, EmvTerminalInfo emvterminalInfo){
        this.emvKernelConfig = emvkernelConfig;
        this.emvTerminalInfo = emvterminalInfo;
        AppLog.d(TAG,"setKernelConfig === " );
    }

    /**
     *
     * @param emvTransData
     * @return
     */
    public EmvResult emvProcess(EmvTransPraram emvTransData){
        this.emvTransData = emvTransData;
        AppLog.d(TAG,"emvProcess= " + emvTransData.toString());
        try {
            int emvRest = -1;
            while (true){
                int candListCount = 0;
                int selectedAppIndex = 0;
                candListCount = emvL2.EMV_AppGetCandListCount();
                if ((candListCount > 1) && (1 == emvKernelConfig.getbCardHolderConfirm())){
                    String strDisplayName[] = new String[candListCount];
                    for (int i = 0; i < candListCount; i++) {
                        EmvCandidateItem emvCandidateItem = emvL2.EMV_AppGetCandListItem(i);
                        if (emvCandidateItem != null ){
                            strDisplayName[i] = new String(emvCandidateItem.getAucDisplayName());
                            AppLog.d(TAG,"AppGetCandListCount= " + strDisplayName[i]);
                        }
                    }
                    //Cardholder selects application from candidate list
                    int AidIndex = emvProcessListener.requestAidSelect(strDisplayName);
                    AppLog.d(TAG,"requestAidSelect AidIndex= " + AidIndex);
                    if ((AidIndex < 0) || (AidIndex >= candListCount)){
                        return EmvResult.IC_SELECT_AIDS_ERR;
                    }else {
                        selectedAppIndex = AidIndex;
                    }
                }else {
                    selectedAppIndex = 0;
                }
                EmvCandidateItem emvCandidateItem = emvL2.EMV_AppGetCandListItem(selectedAppIndex);
                if (emvCandidateItem == null){
                    return EmvResult.IC_GET_CARD_LIST_ERR;
                }
                AppLog.d(TAG,"onUpdateEmvCandidateItem = ");
                emvProcessListener.onUpdateEmvCandidateItem(emvCandidateItem);
                //The terminal issues the SELECT command
                AppLog.d(TAG, "emvProcess check EMV_AppFinalSelect ");
                emvRest = emvL2.EMV_AppFinalSelect(emvCandidateItem);
                AppLog.d(TAG, "EMV_AppFinalSelect emvRet : " + emvRest);
                if ((EmvDefinition.EMV_APP_BLOCKED == emvRest)
                        || (EmvDefinition.EMV_NO_APP == emvRest)
                        || (EmvDefinition.EMV_INVALID_RESPONSE == emvRest)
                        || (EmvDefinition.EMV_INVALID_TLV == emvRest)
                        || (EmvDefinition.EMV_DATA_NOT_EXISTS == emvRest)) {
                    candListCount = emvL2.EMV_AppGetCandListCount();
                    AppLog.d(TAG, "emvProcess  EMV_AppGetCandListCount " + candListCount);
                    if (candListCount > 1) {
                        emvL2.EMV_AppDelCandListItem(selectedAppIndex);
                        continue;
                    }else {
                        return getAppEmvtransResult(emvRest);
                    }
                } else if (emvRest != EmvDefinition.EMV_OK) {
                    return EmvResult.IC_AID_FINAL_SELECT_ERR;
                }
                AppLog.d(TAG, "emvProcess check onUpdateKernelType ");
                emvProcessListener.onUpdateKernelType((byte) 0x00);
                AppLog.d(TAG, "emvProcess check requestImportAmount ");
                Amounts amounts = emvProcessListener.requestImportAmount();
                if (amounts == null){
                    AppLog.d(TAG,"emvProcess requestImportAmount is null " );
                    return EmvResult.IC_INPUT_AMOUNT_ERR;
                }
                String transAmount = amounts.getTransAmount();
                String cashBackAmount = amounts.getCashBackAmount();

//                if (DataUtils.isNullString(transAmount)) transAmount= "000000000015";
                if (DataUtils.isNullString(cashBackAmount)) cashBackAmount= "000000000000";
                //Set amount , other amount
                String s9F02 = String.format("%012d", Long.valueOf(transAmount));
                String s9F03 = String.format("%012d", Long.valueOf(cashBackAmount));
                String s81 = String.format("%08X", Long.valueOf(transAmount));
                String s9F04 = String.format("%08X", Long.valueOf(cashBackAmount));

                AppLog.d(TAG, "s9F02:========= " + s9F02);
                AppLog.d(TAG, "s9F03:========= " + s9F03);
                AppLog.d(TAG, "s81: =========" + s81);
                AppLog.d(TAG, "s9F04:========= " + s9F04);

                emvL2.EMV_SetTLVData(0x9F02, BytesUtil.hexString2Bytes(s9F02));
                emvL2.EMV_SetTLVData(0x9F03, BytesUtil.hexString2Bytes(s9F03));
                emvL2.EMV_SetTLVData(0x81, BytesUtil.hexString2Bytes(s81));
                emvL2.EMV_SetTLVData(0x9F04, BytesUtil.hexString2Bytes(s9F04));
                //=================================end
                //Set some transaction data. Transaction Type , Transaction Date , Transaction Time
                AppLog.d(TAG, "emvProcess check setTransData ");
                emvRest = setTransData();
                if (emvRest != TransUtlis.EMV_OK){
                    AppLog.d(TAG,"emvProcess setTransData is error " );
                    return EmvResult.IC_SET_TRANS_DATA_ERR;
                }
                //Set parameters according to each AID. Terminal floor limit, Trans currency code   ... ...
                AppLog.d(TAG, "emvProcess check setTransDataFromAid ");
                emvRest = setTransDataFromAid();
                if (emvRest != TransUtlis.EMV_OK){
                    AppLog.d(TAG,"emvProcess setTransDataFromAid is error " );
                    return EmvResult.IC_SET_AID_PARAMS_ERR;
                }
                //Callback after final Aid Select, We Can set the TLV parameters
                AppLog.d(TAG, "emvProcess check finalAidSelect ");
                boolean finalAid = emvProcessListener.finalAidSelect();
                if (!finalAid){ //The default return is true, return false to exit the transaction
                    AppLog.d(TAG,"emvProcess finalAidSelect is error " );
                    return EmvResult.IC_FINAL_AID_SELECT_ERR;
                }
                //Initiate Application Processing
                //The terminal issues the GET PROCESSING OPTIONS command
                AppLog.d(TAG, "emvProcess check EMV_GPOProc ");
                emvRest = emvL2.EMV_GPOProc();
                AppLog.d(TAG,"emvProcess PGO res " + emvRest );
                if (emvRest != TransUtlis.EMV_OK){
                    int lastSW = emvL2.EMV_GetLastStatusWord();
                    AppLog.d(TAG,"emvProcess GPO GetLastStatusWord lastSW " +lastSW );
                    if (lastSW != 0x9000) {
                        candListCount = emvL2.EMV_AppGetCandListCount();
                        AppLog.d(TAG,"emvProcess GPO GetCandListCount  " +candListCount );
                        if (candListCount > 1) {
                            emvL2.EMV_AppDelCandListItem(selectedAppIndex);
                            continue;
                        }else {
                            return getAppEmvtransResult(emvRest);
                        }
                    } else {
                        return getAppEmvtransResult(emvRest);
                    }
                }
                break;
            }
            //Read Application Data
            //The terminal shall read the files and records indicated in the AFL using the
            //READ RECORD command identifying the file by its SFI.
            AppLog.d(TAG, "emvProcess check EMV_ReadRecordData ");
            emvRest = emvL2.EMV_ReadRecordData();
            AppLog.d(TAG, "emvProcess ReadRecordData : " + emvRest);
            if (emvRest != TransUtlis.EMV_OK){
                return getAppEmvtransResult(emvRest);
            }

            //Wait for the cardholder to confirm the card number
            String cardNo = getCardNo();
            AppLog.i(TAG, "emvProcess cardNo " + cardNo);
            if (!DataUtils.isNullString(cardNo)){
                AppLog.d(TAG, "emvProcess check onConfirmCardInfo  ");
                boolean confirmCard = emvProcessListener.onConfirmCardInfo(cardNo);
                AppLog.d(TAG,"emvProcess onConfirmCardInfo = " + confirmCard);
                if (!confirmCard){
                    return EmvResult.IC_CONFIRM_PAN_CANCEL;
                }
                //                return EmvResult.IC_GET_PAN_ERR;
            }
            //After read Record Data
            AppLog.d(TAG, "emvProcess check Simple ");
            if (emvTransData.isSimple()){
                return EmvResult.SIMPLE_FLOW_END;
            }
            //Offline Data Authentication
            //The terminal uses the RID and index to retrieve the terminal-stored CAPK
            AppLog.d(TAG, "emvProcess check retrieveCAPK ");
            emvRest = retrieveCAPK();
            if (emvRest != TransUtlis.EMV_OK){
                return getAppEmvtransResult(emvRest);
            }
            AppLog.d(TAG, "emvProcess check EMV_OfflineDataAuth ");
            emvRest = emvL2.EMV_OfflineDataAuth();
            AppLog.d(TAG, "emvProcess  EMV_OfflineDataAuth emvRet : " + emvRest);
            if ((emvRest == TransUtlis.EMV_ICC_ERROR) || (emvRest == TransUtlis.EMV_TERMINATED)){
                return getAppEmvtransResult(emvRest);
            }
            //Terminal Risk Management
            AppLog.d(TAG, "emvProcess check EMV_TerminalRiskManagement  ");
            emvRest = emvL2.EMV_TerminalRiskManagement();
            AppLog.d(TAG, "emvProcess EMV_TerminalRiskManagement emvRet : " + emvRest);
            if (emvRest != TransUtlis.EMV_OK) {
                return getAppEmvtransResult(emvRest);
            }
            //Processing Restrictions
            AppLog.d(TAG, "emvProcess check EMV_ProcessingRestrictions  ");
            emvRest = emvL2.EMV_ProcessingRestrictions();
            AppLog.d(TAG, "emvProcess EMV_ProcessingRestrictions emvRet : " + emvRest);
            if (emvRest != TransUtlis.EMV_OK) {
                return getAppEmvtransResult(emvRest);
            }
            AppLog.d(TAG, "emvProcess check EMV_CardHolderVerify  ");
            emvRest = emvL2.EMV_CardHolderVerify();
            AppLog.d(TAG, "emvProcess EMV_CardHolderVerify emvRet : " + emvRest);
            if (emvRest != TransUtlis.EMV_OK){
                return getAppEmvtransResult(emvRest);
            }
            //Terminal Action Analysis
            AppLog.d(TAG, "emvProcess check EMV_TermActionAnalyze  ");
            emvRest = emvL2.EMV_TermActionAnalyze();
            AppLog.d(TAG, "emvProcess EMV_TermActionAnalyze emvRet : " + emvRest);
            if (emvRest != TransUtlis.EMV_ONLINE_REQUEST){
                return getAppEmvtransResult(emvRest);
            }
            int onlineResult = EmvDefinition.EMV_ONLINE_CONNECT_FAILED;
            byte[] authCode = null; //89 Authorisation Code
            byte[] authRespCode = new byte[2]; //8A Authorisation Response Code
            byte[] issueAuthData = null; //91 Issuer Authentication Data
            byte[] issueScript71 = null; //71 Issuer Script
            byte[] issueScript72 = null; //72 Issuer Script
            AppLog.d(TAG, "emvProcess check EMV_OnlineTransEx  ");
            emvRest = emvL2.EMV_OnlineTransEx();
            AppLog.d(TAG, "emvProcess EMV_OnlineTransEx emvRet : " + emvRest);
            if (emvRest == EmvDefinition.EMV_OK){
                AppLog.d(TAG, "emvProcess check onRequestOnline  ");
                EmvEntity emvEntity = emvProcessListener.onRequestOnline();
                AppLog.d(TAG, "emvProcess onRequestOnline emvEntity : " + emvEntity.toString());

                //authRespCode=3030
                if (!emvTransData.isSecnodGac()){ //add by wwc 2022 01 20 增加是否试下2GAC判断
                    String authRespCodes = BytesUtil.bytes2HexString(emvEntity.getAuthRespCode());
                    if ("3030".equals(authRespCodes)){
                        return EmvResult.APPROVED;
                    }else {
                        return EmvResult.ONLINE_CARD_DENIED;
                    }
                }
                //can check icc isExist
                AppLog.d(TAG, "emvProcess check isExist  ");
                boolean exist = TopUsdkManage.getInstance().getIcc().isExist();
                AppLog.d(TAG, "emvProcess  ICc isExist : " + exist);
                if (!exist){
                    //先检卡 判断下
                    if (EOnlineResult.APPROVE == emvEntity.geteOnlineResult()){
                        return EmvResult.ONLINE_CARD_DENIED;
                    }else {
                        return EmvResult.ABORT_TERMINATED;
                    }
                }
                EOnlineResult eOnlineResult = emvEntity.geteOnlineResult();
                AppLog.d(TAG, "emvProcess check eOnlineResult  "+eOnlineResult.toString());
                switch (eOnlineResult){
                    case APPROVE:
                        onlineResult = EmvDefinition.EMV_ONLINE_APPROVED;
                        break;
                    case FAILED:
                        onlineResult = EmvDefinition.EMV_ONLINE_REJECT;
                        break;
                    case REFER:
                        onlineResult = EmvDefinition.EMV_ONLINE_VOICE_PREFER;
                        break;
                    case DENIAL:
                        onlineResult = EmvDefinition.EMV_ONLINE_ERROR;
                        break;
                    default:
                        onlineResult = EmvDefinition.EMV_ONLINE_ERROR;
                        break;
                }

                AppLog.d(TAG, "emvProcess onlineResult : " + onlineResult);
                if(emvEntity != null && emvEntity.getAuthRespCode()!=null){
                    authRespCode = emvEntity.getAuthRespCode();
                    AppLog.d(TAG, "emvProcess getAuthRespCode" + BytesUtil.bytes2HexString(authRespCode));
                }


                if(emvEntity != null && emvEntity.getIssueAuthData()!=null){
                    issueAuthData = emvEntity.getIssueAuthData();
                    AppLog.d(TAG, "emvProcess getIssueAuthData" + BytesUtil.bytes2HexString(issueAuthData));
                }


                if(emvEntity != null && emvEntity.getAuthCode()!=null){
                    authCode = emvEntity.getAuthCode();
                    AppLog.d(TAG, "emvProcess authCode" + BytesUtil.bytes2HexString(authCode));
                }
                AppLog.d(TAG, "emvProcess check EMV_ProcessOnlineRespData  ");
                emvRest = emvL2.EMV_ProcessOnlineRespData(onlineResult, issueAuthData, authRespCode, authCode);

                AppLog.d(TAG, "emvProcess EMV_ProcessOnlineRespData Result : " + emvRest);

                if(emvEntity != null && emvEntity.getIssueScript71()!=null){

                    issueScript71 = emvEntity.getIssueScript71();
                    AppLog.d(TAG, "emvProcess getIssueScript71" + BytesUtil.bytes2HexString(issueScript71));
                }

                if(emvEntity != null && emvEntity.getIssueScript72()!=null){
                    issueScript72 = emvEntity.getIssueScript72();
                    AppLog.d(TAG, "emvProcess getIssueScript72" + BytesUtil.bytes2HexString(issueScript72));
                }

            }

            AppLog.d(TAG, "emvProcess EMV_ProcessOnlineRespData/EMV_OnlineTransEx  emvRest: " + emvRest);
            AppLog.d(TAG, "emvProcess EMV_OnlineTransEx onlineResult : " + onlineResult);


            if (emvRest != TransUtlis.EMV_TERMINATED && issueScript71!=null) {
                int i = emvL2.EMV_IssueToCardScript((byte) 1, issueScript71);
                AppLog.d(TAG, "emvProcess EMV_IssueToCardScript i : " + i);
            }
            AppLog.d(TAG, "emvProcess check emvRest " +emvRest);
            if (emvRest == EmvDefinition.EMV_OK) {
                if (onlineResult == EmvDefinition.EMV_ONLINE_APPROVED) {
                    AppLog.d(TAG, "emvProcess EMV_ONLINE_APPROVED EMV_Completion 1  ");
                    emvRest = emvL2.EMV_Completion((byte) 1);
                    AppLog.d(TAG, "emvProcess EMV_Completion EMV_ONLINE_APPROVED emvRet : " + emvRest);
                } else if (onlineResult == EmvDefinition.EMV_ONLINE_VOICE_PREFER) {
                    AppLog.d(TAG, "emvProcess EMV_ONLINE_VOICE_PREFER EMV_Completion 1  ");
                    emvRest = emvL2.EMV_Completion((byte) 1);
                    AppLog.d(TAG, "emvProcess EMV_Completion EMV_ONLINE_VOICE_PREFER emvRet : " + emvRest);
                } else {
                    AppLog.d(TAG, "emvProcess EMV_Completion 0  ");
                    emvRest = emvL2.EMV_Completion((byte) 0);
                    AppLog.d(TAG, "emvProcess EMV_Completion else emvRet : " + emvRest);
                }
            } else if (emvRest == EmvDefinition.EMV_DECLINED) {
                AppLog.d(TAG, "emvProcess EMV_DECLINED EMV_Completion 0  ");
                emvRest = emvL2.EMV_Completion((byte) 0);
                AppLog.d(TAG, "emvProcess EMV_Completion EMV_DECLINED emvRet : " + emvRest);
            } else if (emvRest == EmvDefinition.EMV_APPROVED) {
                AppLog.d(TAG, "emvProcess EMV_APPROVED EMV_Completion 1 ");
                emvRest = emvL2.EMV_Completion((byte) 1);
                AppLog.d(TAG, "emvProcess EMV_Completion EMV_APPROVED emvRet : " + emvRest);
            }

            if (emvRest != EmvDefinition.EMV_TERMINATED && issueScript72!=null) {  //tlv
                AppLog.d(TAG, "emvProcess check EMV_IssueToCardScript  ");
                int i = emvL2.EMV_IssueToCardScript((byte) 0, issueScript72);
                AppLog.d(TAG, "emvProcess EMV_Completion EMV_TERMINATED i : " + i);
            }
            //
            AppLog.d(TAG, "emvProcess check EMV_GetScriptResult  " + emvRest );
            byte[] scriptResult = emvL2.EMV_GetScriptResult();
            if (scriptResult != null && scriptResult.length > 0){
                AppLog.d(TAG,"emvProcess check EMV_GetScriptResult 9F5B " + BytesUtil.bytes2HexString(scriptResult));
                emvL2.EMV_SetTLVData(0x9F5B,scriptResult);
            }
            return getAppEmvtransResult(emvRest);
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.d(TAG,"emvProcess Exception = " + e.getMessage());
            return EmvResult.IC_EMV_PROCESS_ERR;
        }
    }
    private EmvResult getAppEmvtransResult(int emvkernelRetCode) {
        AppLog.d(TAG,"emvProcess getAppEmvtransResult = " + emvkernelRetCode);
        switch (emvkernelRetCode)
        {
            case EmvDefinition.EMV_APPROVED:
            case EmvDefinition.EMV_FORCE_APPROVED:
                return EmvResult.APPROVED;
            case EmvDefinition.EMV_DECLINED:
                return EmvResult.TRANS_DENIED;
            case EmvDefinition.EMV_CANCEL:
                return EmvResult.ABORT_TERMINATED;
            case EmvDefinition.EMV_NO_APP:
                return EmvResult.IC_EMV_NO_APP;
            case EmvDefinition.EMV_APP_BLOCKED:
                return EmvResult.IC_EMV_APP_BLOCKED;
            case EmvDefinition.EMV_INVALID_RESPONSE:
                return EmvResult.IC_EMV_INVALID_RESPONSE;
            case EmvDefinition.EMV_INVALID_TLV:
                return EmvResult.IC_EMV_INVALID_TLV;
            case EmvDefinition.EMV_DATA_NOT_EXISTS:
                return EmvResult.IC_EMV_DATA_NOT_EXISTS;
            default:
                return EmvResult.TRANS_STOP;
        }
    }
    /**
     * The terminal uses the RID and index to retrieve the terminal-stored CAPK
     * @return
     */
    private int retrieveCAPK(){
        int emvRet = 0;
        AppLog.d(TAG, "emvProcess retrieveCAPK()================");
        try {
            byte[] aid = emvL2.EMV_GetTLVData(0x9F06);

            if ((aid == null) || (aid.length < 5) || (aid.length > 16)) {
                AppLog.d(TAG, "emvProcess retrieveCAPK Get aid(9F06) failed!");
                return -1;
            }
            AppLog.d(TAG, "emvProcess retrieveCAPK aid(9F06): " + BytesUtil.bytes2HexString(aid));

            byte[] index = emvL2.EMV_GetTLVData(0x8F);
            if ((index == null) || (index.length != 1)) {
                AppLog.d(TAG, "emvProcess retrieveCAPK Get CAPK index(8F) failed!");
                return 0;
            }
            AppLog.d(TAG, "emvProcess retrieveCAPK CAPK index(8F): " + BytesUtil.bytes2HexString(index));

            byte[] rid = new byte[5];
            System.arraycopy(aid, 0, rid, 0, 5);

            String ridindex = new StringBuffer(BytesUtil.bytes2HexString(rid))
                    .append(Integer.toHexString(index[0] & 0xFF)).toString().toUpperCase();

            EmvCapk emvCapk = null;
            for (EmvCapkParam capk: emvCapkParams) {

                if (ridindex.equals(capk.getRIDKeyID())){
                    AppLog.d(TAG, "emvProcess retrieveCAPK onSelectCapk return emvCapk: " + capk.toString());
                    emvCapk = new EmvCapk();
                    emvCapk.setRID(BytesUtil.hexString2Bytes(capk.getRID()));
                    emvCapk.setKeyID(capk.getKeyID());

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

                    AppLog.d(TAG, "emvProcess retrieveCAPK EmvCapkParam tempExpDate(): " + BytesUtil.bytes2HexString(tempExpDate));
                    emvCapk.setExpDate(tempExpDate);

                    emvCapk.setHashInd(capk.getHashInd());
                    emvCapk.setArithInd(capk.getArithInd());
                    emvCapk.setCheckSum(BytesUtil.hexString2Bytes(capk.getCheckSum()));

                    byte[] orgData = BytesUtil.hexString2Bytes(capk.getModul());
                    if (orgData != null) {
                        emvCapk.setModul(orgData);
                    }
                    orgData = BytesUtil.hexString2Bytes(capk.getExponent());
                    if (orgData != null) {
                        emvCapk.setExponent(orgData);
                    }
                    break;
                }
            }
            if (null == emvCapk) {
                AppLog.d(TAG, "emvProcess retrieveCAPK findByRidIndex failed!");
                return 0;
            }
            emvL2.EMV_DelAllCAPK();
            emvRet = emvL2.EMV_AddCAPK(emvCapk);
            AppLog.d(TAG, "emvProcess retrieveCAPK EMV_AddCAPK emvRet : " + emvRet);
            if (emvRet != 0) {
                AppLog.d(TAG, "emvProcess retrieveCAPK EMV_AddCAPK failed!");
                return 0;
            }
            return  0;
        } catch (RemoteException e) {
            e.printStackTrace();
            return  -1;
        }
    }
    /**
     *
     * @return
     */
    private String getCardNo() {
        AppLog.d(TAG, "Into getCardNo()");
        try {
            byte[] PAN = emvL2.EMV_GetTLVData(0x5A);
            if (PAN == null) {
                AppLog.emvd(TAG, "emvProcess Get AID(5A) failed!");
                return null;
            }

            String cardNo = BytesUtil.bytes2HexString(PAN);
            if (cardNo == null) {
                AppLog.d(TAG, "emvProcess CardNo is null");
                return null;
            }
            cardNo = cardNo.toUpperCase().replace("F", "");
            AppLog.d(TAG, "emvProcess getCardNo(): " + cardNo);
            return cardNo;
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.d(TAG,"emvProcess getCardNo Exception = " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * @return
     */
    private int setTransData() {
        AppLog.d(TAG,"emvProcess setTransData = ");
        try {
            //Transaction Type
            byte[] transType = new byte[1];
            transType[0] = emvTransData.getTransType();
            emvL2.EMV_SetTLVData(0x9C, transType);
            //The getRandom function returns a fixed 8 byte random number
            byte[] random = pinPad.getRandom();
            byte[] unpredictableNum = new byte[4];
            System.arraycopy(random, 0, unpredictableNum, 0, 4);
            emvL2.EMV_SetTLVData(0x9F37, unpredictableNum);
            //Transaction Sequence Counter
            int tsc = (int) DataUtils.getSerialNumber();
            String tag9f41 = String.format("%08d",tsc);
            AppLog.emvd(TAG, "setTransData TAG9F41== " + tag9f41);
            emvL2.EMV_SetTLVData(0x9F41, BytesUtil.hexString2Bytes(tag9f41));
            //emvL2.EMV_SetTLVData(0x9F41, BytesUtil.int2Bytes(tsc, true));
            ///Transaction Date YYMMDD
            String date = emvTransData.getData();
            emvL2.EMV_SetTLVData(0x9A, BytesUtil.hexString2Bytes(date));
            //Transaction Time HHMMSS
            String time = emvTransData.getTime();
            emvL2.EMV_SetTLVData(0x9F21, BytesUtil.hexString2Bytes(time));
            return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 匹配到AID参数读取信息，再设置到EMV 内核
     * @return
     */
    private int setTransDataFromAid(){
        AppLog.d(TAG,"emvProcess setTransDataFromAid = ");
        try {
            int emvRet = 0;
            byte[] aid = emvL2.EMV_GetTLVData(0x9F06);
            if ((aid == null) || (aid.length < 5) || (aid.length > 16)) {
                AppLog.e(TAG,"emvProcess getaid(9F06)  failed! ");
                return -1;
            }
            AppLog.d(TAG, "Get AID(9F06)" + BytesUtil.bytes2HexString(aid));
            EmvAidParam eaidParam = null;
            String strAid = BytesUtil.bytes2HexString(aid);
            if (emvAidParams == null){
                AppLog.e(TAG,"emvProcess emvAidParams  is null! ");
                return -1;
            }
            for (EmvAidParam e: emvAidParams) {
                if(strAid.startsWith(e.getAid())){
                    eaidParam = e;
                    AppLog.d(TAG, "emvProcess select AidParams== " + e.toString());
                }
            }
            if(eaidParam == null){
                AppLog.e(TAG, "emvProcess select AID from database failed!");
                return -1;
            }
            //终端应用版本 form aid param
            byte[] tag9F09 = BytesUtil.hexString2Bytes(eaidParam.getVersion());
            if (tag9F09 != null && tag9F09.length > 0){
                AppLog.d(TAG, "emvProcess set aidParams getVersion = " + BytesUtil.bytes2HexString(tag9F09));
                emvL2.EMV_SetTLVData(0x9F09,tag9F09);
            }


            //终端最低限额
            byte[] tag9F1B = BytesUtil.int2Bytes(Integer.parseInt(eaidParam.getFloorLimit() +""), true);
            if (tag9F09 != null && tag9F09.length > 0){
                AppLog.d(TAG, "emvProcess set aidParams getFloorLimit(bytes) tag9F1B = " + BytesUtil.bytes2HexString(tag9F1B));
                emvL2.EMV_SetTLVData(0x9F1B, tag9F1B);
            }

            //Set terminal info from the AID parameter stored at the terminal
            //先获初始化时的默认参数
            EmvTerminalInfo emvTerminalInfo = emvL2.EMV_GetTerminalInfo();
            AppLog.d(TAG, "emvProcess  default emvTerminalInfo = " + emvTerminalInfo.toString());

            AppLog.d(TAG, "emvProcess set aidParams getFloorLimit()(int) = " + eaidParam.getFloorLimit());
            emvTerminalInfo.setUnTerminalFloorLimit(Integer.parseInt(eaidParam.getFloorLimit() +""));

            int threshold= Integer.parseInt(eaidParam.getThreshold() + "");
            AppLog.d(TAG, "emvProcess set threshold = " + threshold);
            emvTerminalInfo.setUnThresholdValue(threshold);

            String termId = eaidParam.getTermId();
            AppLog.d(TAG, "emvProcess set TermId = " + termId);
            if (!DataUtils.isNullString(termId) && (termId.length() == 8)) {
                emvTerminalInfo.setAucTerminalID(termId);
            }

            String merchId = eaidParam.getMerchId();
            AppLog.d(TAG, "emvProcess set merchId = " + merchId);
            if (!DataUtils.isNullString(merchId) && (merchId.length() == 15)) {
                emvTerminalInfo.setAucMerchantID(merchId);
            }

            String merchCateCode = eaidParam.getMerchCateCode();
            AppLog.d(TAG, "emvProcess set merchCateCode = " + merchCateCode);
            if (!DataUtils.isNullString(merchCateCode) && (merchCateCode.length() == 2)) {
                emvTerminalInfo.setAucMerchantCategoryCode(BytesUtil.hexString2Bytes(merchCateCode));
            }

            String merchName = eaidParam.getMerchName();
            AppLog.d(TAG, "emvProcess set merchName = " + merchName);
            byte[] merchNamebytes = BytesUtil.hexString2Bytes(merchName);
            if (!DataUtils.isNullString(merchName)) {
                emvTerminalInfo.setAucMerchantNameLocation(merchNamebytes);
            }

            String transCurrCode = eaidParam.getTransCurrCode();
            AppLog.d(TAG, "emvProcess set transCurrCode = " + transCurrCode);
            byte[] transCurrCodebytes = BytesUtil.hexString2Bytes(transCurrCode);
            if ((transCurrCodebytes != null) && (transCurrCodebytes.length == 2)) {
                emvTerminalInfo.setAucTransCurrencyCode(transCurrCodebytes);
            }


            int transCurrExp = eaidParam.getTransCurrExp();
            AppLog.d(TAG, "emvProcess set getTransCurrExp = " + transCurrExp);
            if (transCurrExp > 0) {
                emvTerminalInfo.setUcTransCurrencyExp((byte) transCurrExp);
            }

            String referCurrCode = eaidParam.getReferCurrCode();
            AppLog.d(TAG, "emvProcess set referCurrCode = " +referCurrCode);
            byte[] referCurrCodebytes = BytesUtil.hexString2Bytes(referCurrCode);
            if ((referCurrCodebytes != null) && (referCurrCodebytes.length == 2)) {
                emvTerminalInfo.setAucTransRefCurrencyCode(referCurrCodebytes);
            }

            int referCurrExp = eaidParam.getReferCurrExp();
            AppLog.d(TAG, "emvProcess set referCurrExp = " + referCurrExp);
            if (referCurrExp > 0) {
                emvTerminalInfo.setUcTransRefCurrencyExp((byte) referCurrExp);
            }

            String acquierId = eaidParam.getAcquierId();
            AppLog.d(TAG, "emvProcess set acquierId = " + acquierId);
            if ((acquierId!= null) && (acquierId.length() > 6)) {
                emvTerminalInfo.setAucTerminalAcquireID(acquierId);
            }

            AppLog.d(TAG, "emvProcess set AucAppVersion tag9F09 = " + tag9F09);
            if ((tag9F09 != null) && (tag9F09.length == 2)) {
                emvTerminalInfo.setAucAppVersion(tag9F09);
            }

            String ddol = eaidParam.getdDOL();
            AppLog.d(TAG, "emvProcess set dDol = " + ddol);
            byte[] ddolbytes = BytesUtil.hexString2Bytes(ddol);
            if ((ddolbytes != null) && (ddolbytes.length > 0)) {
                emvTerminalInfo.setAucDefaultDDOL(ddolbytes);
            }

            String tdol = eaidParam.gettDOL();
            AppLog.d(TAG, "emvProcess set tDol= " + tdol);
            byte[] tdolbytes = BytesUtil.hexString2Bytes(tdol);
            if ((tdolbytes != null) && (tdolbytes.length > 0)) {
                emvTerminalInfo.setAucDefaultTDOL(tdolbytes);
            }

            final String tacDenial = eaidParam.getTacDenial();
            AppLog.d(TAG, "emvProcess set  tacDenial = " + tacDenial);
            byte[] tacDenialbytes = BytesUtil.hexString2Bytes(tacDenial);
            if ((tacDenialbytes!= null) && (tacDenialbytes.length == 5)) {
                emvTerminalInfo.setAucTACDenial(tacDenialbytes);
            }

            String tacOnline = eaidParam.getTacOnline();
            AppLog.d(TAG, "emvProcess set  tacOnline = " + tacOnline);
            byte[] tacOnlinebytes = BytesUtil.hexString2Bytes(tacOnline);
            if ((tacOnlinebytes != null) && (tacOnlinebytes.length == 5)) {
                emvTerminalInfo.setAucTACOnline(tacOnlinebytes);
            }

            String tbcDefualt = eaidParam.getTbcDefualt();
            AppLog.d(TAG, "emvProcess set tbcDefualt = " + tbcDefualt);
            byte[] tbcDefualtbytes = BytesUtil.hexString2Bytes(tbcDefualt);
            if ((tbcDefualtbytes != null) && (tbcDefualtbytes.length == 5)) {
                emvTerminalInfo.setAucTACDefault(tbcDefualtbytes);
            }


            int targetPer = eaidParam.getTargetPer();
            AppLog.d(TAG, "emvProcess set targetPer = " + targetPer);
            emvTerminalInfo.setUcTargetPercentage((byte) targetPer);

            int maxTargetPer = eaidParam.getMaxTargetPer();
            AppLog.d(TAG, "emvProcess set maxTargetPer = " + maxTargetPer);
            emvTerminalInfo.setUcMaxTargetPercentage((byte) maxTargetPer);

            AppLog.d(TAG, "emvProcess set  emvTerminalInfo== " + emvTerminalInfo.toString());

            emvL2.EMV_SetTerminalInfo(emvTerminalInfo);
            return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public void setEmvAidParams(List<EmvAidParam> emvAidParams) {
        this.emvAidParams = emvAidParams;
    }

    public void setEmvCapkParams(List<EmvCapkParam> emvCapkParams) {
        this.emvCapkParams = emvCapkParams;
    }

//    class emvCalBack()
    //=====================================
    private int pinRetryTimes = 0;
    /***
     *  Require online PIN processing
     * @param b : [IN] Is allow PIN entry bypass ?
     * @param bytes : [IN] PAN, The value of 5A
     * @param i : [IN ] PAN length,  The length of 5A
     * @param booleans : [OUT]  PIN entry bypassed ?
     * @return 1: Bypassed or Successfully entered
     *         0: No entry PIN or PIN pad is malfunctioning
     * @throws RemoteException
     */
    @Override
    public int cGetOnlinePin(boolean b, byte[] bytes, int i, boolean[] booleans) throws RemoteException {
        AppLog.d(TAG, "cGetOnlinePin Is allow PIN entry bypass: " + b);
        AppLog.d(TAG, "cGetOnlinePin PAN: " + BytesUtil.bytes2HexString(bytes));
        AppLog.d(TAG, "cGetOnlinePin PAN length: " + i);

        EmvEntity pinRes = emvProcessListener.requestImportPin(TransUtlis.PINTYPE_ONLINE, false, null);
        AppLog.d(TAG, "cGetOnlinePin requestImportPin: " + pinRes.isResult());
        if (pinRes.isResult()){ //Bypassed or Successfully entered
            if (EmvDefinition.BYPASS.equals(pinRes.getPinData())){
                booleans[0] = true;  //Bypassed
            }else {
                booleans[0] = false;
            }
            return 1;
        }else {
            booleans[0] = true;
            return 0;
        }
    }
    /***
     * Require offline PIN processing
     * @param b :  [IN] Is allow PIN entry bypass ?
     * @param bytes : [OUT] The plaintext offline PIN block
     * @param i : [IN] PIN block buffer size
     * @param booleans : [OUT]  PIN entry bypassed ?
     * @return 1: Bypassed or Successfully entered
     *         0: No entry PIN or PIN pad is malfunctioning
     * @throws RemoteException
     */
    @Override
    public int cGetPlainTextPin(boolean b, byte[] bytes, int i, boolean[] booleans) throws RemoteException {
        AppLog.emvd(TAG,"cGetPlainTextPin");

        int reqPinType = PayDataUtil.PINTYPE_OFFLINE;

        AppLog.emvd(TAG, "cGetPlainTextPin pinRetryTimes = " + pinRetryTimes);
        byte[] pinTryCnt = emvL2.EMV_GetTLVData(0x9F17);
        boolean lastTimeFlag =false;
        if (0 == pinRetryTimes || 1 == pinRetryTimes) {
            lastTimeFlag=true;
        }else if(pinTryCnt==null||pinTryCnt.length!=1){
            lastTimeFlag=true;
        }else if (pinTryCnt!=null&&(1 == pinTryCnt.length)&&(0x00 == pinTryCnt[0] || 0x01 == pinTryCnt[0])) {
            AppLog.emvd(TAG, "pinTryCnt = " + BytesUtil.bytes2HexString(pinTryCnt));
            lastTimeFlag=true;
        } else {
            lastTimeFlag=false;
        }

        EmvEntity pinRes = emvProcessListener.requestImportPin(reqPinType, lastTimeFlag, null);
        AppLog.d(TAG, "cGetPlainTextPin requestImportPin: " + pinRes.isResult());
        if (!pinRes.isResult()){
            booleans[0] = true;
            AppLog.d(TAG, "cGetPlainTextPin calcel(): ");
            return 0;
        }else { //Bypassed or Successfully entered
            if (EmvDefinition.BYPASS.equals(pinRes.getPinData())){
                booleans[0] = true;//Bypassed
            }else {
                booleans[0] = false;
                byte[] pinBlock = getOfflinePinBlock(pinRes.getPinData());
                if (pinBlock == null){
                    booleans[0] = true;
                    return 0;
                }
                System.arraycopy(pinBlock, 0, bytes, 0, pinBlock.length);
                AppLog.d(TAG, "cGetPlainTextPin getOfflinePinBlock(): " + BytesUtil.bytes2HexString(pinBlock));
            }
            return 1;
        }
    }

    @Override
    public int cDisplayPinVerifyStatus(int i) throws RemoteException {
        AppLog.d(TAG,"cDisplayPinVerifyStatus");
        AppLog.d(TAG, "The number of remaining PIN tries: " + i);
        pinRetryTimes = i;
        return 1;
    }

    @Override
    public int cCheckCredentials(int i, byte[] bytes, int i1, boolean[] booleans) throws RemoteException {
        return 0;
    }

    @Override
    public int cIssuerReferral(byte[] bytes, int i) throws RemoteException {
        return 0;
    }

    @Override
    public int cGetTransLogAmount(byte[] bytes, int i, int i1) throws RemoteException {
        return 0;
    }

    @Override
    public int cCheckExceptionFile(byte[] bytes, int i, int i1) throws RemoteException {
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

    @Override
    public int cRFU3() throws RemoteException {
        return 0;
    }

    @Override
    public int cRFU4() throws RemoteException {
        return 0;
    }

    /**
     *
     * @param pin
     * @return
     */
    private byte[] getOfflinePinBlock(String pin) {
        AppLog.d(TAG, "Into getOfflinePinBlock()");

        if (pin == null) {
            return null;
        }

        if ((pin.length() == 0) || (pin.length() > 14)) {
            return null;
        }

        for (int i = 0 ; i < pin.length(); i++)
        {
            if ((pin.charAt(i) < '0') || (pin.charAt(i) > '9')) {
                return null;
            }
        }

        AppLog.d(TAG, "pin: " + pin);

        String strBlock = new String();
        strBlock += "2";
        strBlock += String.format("%X", pin.length());
        strBlock += pin;

        AppLog.emvd(TAG, "strBlock: " + strBlock);

        while (strBlock.length() < 16)
        {
            strBlock = strBlock.concat("F");
        }

        AppLog.d(TAG, "strBlock: " + strBlock);
        return BytesUtil.hexString2Bytes(strBlock);
    }


   public EmvResult readCardTransLog(EmvTransPraram emvTransData,ITlv.ITlvDataObjList logs){
       this.emvTransData = emvTransData;
       int emvRest = -1;
       try {
            //gpo
           int candListCount = 0;
           int selectedAppIndex = 0;
           candListCount = emvL2.EMV_AppGetCandListCount();
           if ((candListCount > 1) && (1 == emvKernelConfig.getbCardHolderConfirm())){
               String strDisplayName[] = new String[candListCount];
               for (int i = 0; i < candListCount; i++) {
                   EmvCandidateItem emvCandidateItem = emvL2.EMV_AppGetCandListItem(i);
                   if (emvCandidateItem != null ){
                       strDisplayName[i] = new String(emvCandidateItem.getAucDisplayName());
                       AppLog.d(TAG,"AppGetCandListCount= " + strDisplayName[i]);
                   }
               }
               //Cardholder selects application from candidate list
               int AidIndex = emvProcessListener.requestAidSelect(strDisplayName);
               AppLog.d(TAG,"requestAidSelect AidIndex= " + AidIndex);
               if ((AidIndex < 0) || (AidIndex >= candListCount)){
                   return EmvResult.IC_SELECT_AIDS_ERR;
               }else {
                   selectedAppIndex = AidIndex;
               }
           }else {
               selectedAppIndex = 0;
           }
           EmvCandidateItem emvCandidateItem = emvL2.EMV_AppGetCandListItem(selectedAppIndex);
           if (emvCandidateItem == null){
               return EmvResult.IC_GET_CARD_LIST_ERR;
           }
           AppLog.d(TAG,"onUpdateEmvCandidateItem = ");
           emvProcessListener.onUpdateEmvCandidateItem(emvCandidateItem);
           //The terminal issues the SELECT command
           emvRest = emvL2.EMV_AppFinalSelect(emvCandidateItem);
           AppLog.d(TAG, "EMV_AppFinalSelect emvRet : " + emvRest);
           if ((EmvDefinition.EMV_APP_BLOCKED == emvRest)
                   || (EmvDefinition.EMV_NO_APP == emvRest)
                   || (EmvDefinition.EMV_INVALID_RESPONSE == emvRest)
                   || (EmvDefinition.EMV_INVALID_TLV == emvRest)
                   || (EmvDefinition.EMV_DATA_NOT_EXISTS == emvRest)) {
               candListCount = emvL2.EMV_AppGetCandListCount();
               if (candListCount > 1) {
                   emvL2.EMV_AppDelCandListItem(selectedAppIndex);
               }else {
                   return getAppEmvtransResult(emvRest);
               }
           } else if (emvRest != EmvDefinition.EMV_OK) {
               return EmvResult.IC_AID_FINAL_SELECT_ERR;
           }

           emvProcessListener.onUpdateKernelType((byte) 0x00);

           Amounts amounts = emvProcessListener.requestImportAmount();
           if (amounts == null){
               AppLog.d(TAG,"emvProcess requestImportAmount is null " );
               return EmvResult.IC_INPUT_AMOUNT_ERR;
           }
           String transAmount = amounts.getTransAmount();
           String cashBackAmount = amounts.getCashBackAmount();

    //                if (DataUtils.isNullString(transAmount)) transAmount= "000000000015";
           if (DataUtils.isNullString(cashBackAmount)) cashBackAmount= "000000000000";
           //Set amount , other amount
           String s9F02 = String.format("%012d", Long.valueOf(transAmount));
           String s9F03 = String.format("%012d", Long.valueOf(cashBackAmount));
           String s81 = String.format("%08X", Long.valueOf(transAmount));
           String s9F04 = String.format("%08X", Long.valueOf(cashBackAmount));

           AppLog.d(TAG, "s9F02:========= " + s9F02);
           AppLog.d(TAG, "s9F03:========= " + s9F03);
           AppLog.d(TAG, "s81: =========" + s81);
           AppLog.d(TAG, "s9F04:========= " + s9F04);

           emvL2.EMV_SetTLVData(0x9F02, BytesUtil.hexString2Bytes(s9F02));
           emvL2.EMV_SetTLVData(0x9F03, BytesUtil.hexString2Bytes(s9F03));
           emvL2.EMV_SetTLVData(0x81, BytesUtil.hexString2Bytes(s81));
           emvL2.EMV_SetTLVData(0x9F04, BytesUtil.hexString2Bytes(s9F04));
           //=================================end
           //Set some transaction data. Transaction Type , Transaction Date , Transaction Time
           emvRest = setTransData();
           if (emvRest != TransUtlis.EMV_OK){
               AppLog.d(TAG,"readCardTransLog setTransData is error " );
               return EmvResult.IC_SET_TRANS_DATA_ERR;
           }
           //Set parameters according to each AID. Terminal floor limit, Trans currency code   ... ...
           emvRest = setTransDataFromAid();
           if (emvRest != TransUtlis.EMV_OK){
               AppLog.d(TAG,"readCardTransLog setTransDataFromAid is error " );
               return EmvResult.IC_SET_AID_PARAMS_ERR;
           }
           //Callback after final Aid Select, We Can set the TLV parameters
           boolean finalAid = emvProcessListener.finalAidSelect();
           if (!finalAid){ //The default return is true, return false to exit the transaction
               AppLog.d(TAG,"readCardTransLog finalAidSelect is error " );
               return EmvResult.IC_FINAL_AID_SELECT_ERR;
           }
           //Initiate Application Processing
           //The terminal issues the GET PROCESSING OPTIONS command
           emvRest = emvL2.EMV_GPOProc();
           AppLog.d(TAG,"readCardTransLog PGO res " + emvRest );
           if (emvRest != TransUtlis.EMV_OK){
               int lastSW = emvL2.EMV_GetLastStatusWord();
               AppLog.d(TAG,"readCardTransLog GPO GetLastStatusWord lastSW " +lastSW );
               if (lastSW != 0x9000) {
                   candListCount = emvL2.EMV_AppGetCandListCount();
                   AppLog.d(TAG,"readCardTransLog GPO GetCandListCount  " +candListCount );
                   if (candListCount > 1) {
                       emvL2.EMV_AppDelCandListItem(selectedAppIndex);

                   }
               } else {
                   return EmvResult.IC_GPO_ERR;
               }
           }
           //0x9F4D
           byte[] byte9F4D = emvL2.EMV_GetTLVData(0x9F4D);
           if (byte9F4D == null){
               return EmvResult.IC_EMV_NO_ACCEPTED;
           }
           AppLog.d(TAG,"readCardTransLog byte9F4D " +BytesUtil.bytes2HexString(byte9F4D) );

           //0B0A
           int uiRecordCount = byte9F4D[1];
           AppLog.d(TAG,"readCardTransLog uiRecordCount " +uiRecordCount );
           if (uiRecordCount == 0){
               return EmvResult.IC_EMV_NO_MORE_DATA;
           }

           // 0x9F4F
           emvRest = emvL2.EMV_GetDataFromICC(0x9F4F);
           if (emvRest != TransUtlis.EMV_OK){
               return EmvResult.IC_EMV_ICC_ERROR;
           }
            //9A 03 9F21 03 9F02 06 9F03 06 9F1A 02 5F2A 02 9F4E 14 9C 01 9F36 02
           byte[] byte9F4F = emvL2.EMV_GetTLVData(0x9F4F);
           if (byte9F4F == null){
               return EmvResult.IC_EMV_ICC_ERROR;
           }
           AppLog.d(TAG,"readCardTransLog byte9F4F " +BytesUtil.bytes2HexString(byte9F4F) );

           int i = 0x01 ;
           ITlv tlv = TopTool.getInstance().getPacker().getTlv();
           ITlv.ITlvDataObjList unpack9F4F = null;

           try {
               unpack9F4F = tlv.unpackDDol(byte9F4F);
               for (int ii = 0;ii <unpack9F4F.getSize();ii++){
                   AppLog.d(TAG,"readCardTransLog unpack9F4F " + unpack9F4F.getIndexTag(ii).toString());
               }
           } catch (TlvException e) {
               e.printStackTrace();
           }
           while(i <= uiRecordCount){

               byte[] readCardLog = emvL2.EMV_ReadCardLog(byte9F4D, i);
               AppLog.d(TAG,"readCardTransLog readCardLog " +BytesUtil.bytes2HexString(readCardLog) );
               int lenOffset = 0;
               for (int ii = 0;ii <unpack9F4F.getSize();ii++){
                   ITlv.ITlvDataObj indexTag = unpack9F4F.getIndexTag(ii);
                   byte[] v = new byte[indexTag.getLength()];
                   System.arraycopy(readCardLog, lenOffset, v, 0, indexTag.getLength());
                   indexTag.setValue(v);
                   lenOffset += indexTag.getLength();
                   logs.addDataObj(indexTag);
                   AppLog.d(TAG,"readCardTransLog indexTag " +indexTag.toString() );
               }
               i++;
           }
           return EmvResult.OK;
       } catch (RemoteException e) {
            e.printStackTrace();
           return EmvResult.ABORT_TERMINATED;
        }
    }
}
