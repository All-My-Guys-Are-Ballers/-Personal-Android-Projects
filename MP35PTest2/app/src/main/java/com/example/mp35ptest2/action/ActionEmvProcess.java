package com.example.mp35ptest2.action;

import static com.example.mp35ptest2.MainActivityKt.SHOW;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Time;

import com.example.mp35ptest2.MyApplication;
import com.example.mp35ptest2.core.AAction;
import com.example.mp35ptest2.core.ActionResult;
import com.example.mp35ptest2.emv.EmvTags;
import com.example.mp35ptest2.emv.EmvTransProcessImpl;
import com.example.mp35ptest2.entity.TransData;
import com.example.mp35ptest2.param.AidParam;
import com.example.mp35ptest2.param.CapkParam;
import com.topwise.cloudpos.aidl.emv.level2.EmvKernelConfig;
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.manager.AppLog;
import com.topwise.manager.emv.api.IEmv;
import com.topwise.manager.emv.entity.EinputType;
import com.topwise.manager.emv.entity.EmvTransPraram;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.manager.emv.utlis.EmvDefinition;
import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.toptool.api.convert.IConvert;


/**
 * Creation date：2021/6/23 on 16:28
 * Describe:
 * Author:wangweicheng
 */
public class ActionEmvProcess extends AAction {
    /**
     * 子类构造方法必须调用super设置ActionStartListener
     * The subclass constructor must call super to set ActionStartListener
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionEmvProcess(ActionStartListener listener) {
        super(listener);
    }
    private Context context;
    private TransData transData;
    private Handler handler;
    private boolean isContact;
    private IEmv emv;

    public void setParam(Context context,Handler handler, TransData transData) {
        this.context = context;
        this.handler = handler;
        this.transData = transData;
        this.isContact = false;
    }

    private EinputType einputType;
    private EmvTransPraram emvTransPraram;
    @Override
    protected void process() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                einputType = transData.getEnterMode();

                emv = MyApplication.usdkManage.getEmvHelper();
                emv.init(einputType);

                //Process callback implementation
                EmvTransProcessImpl emvProcessListener = new EmvTransProcessImpl(context,transData,emv,handler);

                emvTransPraram = new EmvTransPraram(EmvTags.checkKernelTransType(transData));
                Time tm = new Time();
                tm.setToNow();
                //set Trans Praram date and time
                String year = String.format("%04d", tm.year);
                emvTransPraram.setData(year.substring(2) + transData.getDate());
                emvTransPraram.setTime(transData.getTime());

                // get aid capk list
                long l = System.currentTimeMillis();
                AidParam aidParam = new AidParam();
                aidParam.init(MyApplication.mApp);
                emv.setAidList(aidParam.saveEmvAidParam());
                CapkParam capkParam = new CapkParam();
                capkParam.init(MyApplication.mApp);
                emv.setCapkList(capkParam.saveEmvCapkParam());

                AppLog.e("ActionEmvProcess","0000000000004 getVersion " +          emv.getVersion());
                if (transData.getEnterMode() ==  EinputType.IC){
                    //set emv EmvKernelConfig and EmvTerminalInfo
//                    emvTransPraram.setSimple(true);
                    emv.setConfig(setEmvKernelConfig(),setEmvTerminalInfo());
                }else {
                    //Whether to force input password for contactless connection
                    //true Compulsory
                    //false check DF8129.cvm
                    //emvTransPraram.setHavePin(true);
                    //set ContactLess TransParamConfig and EmvTerminalInfo
                    emv.setContactLessConfig(setTransParamConfig(EmvTags.checkKernelTransType(transData),emvTransPraram),setEmvTerminalInfo());
                }
                //set callback
                emv.setProcessListener(emvProcessListener);

                //start emv process
                final EmvResult emvResult = emv.emvProcess(emvTransPraram);
                byte[] f55 = EmvTags.getF55(0, emv, false, false);
                byte[] tlv = emv.getTlv(0x57);
                byte[] tlv5a = emv.getTlv(0x5A);
                if (tlv5a !=null){
                    AppLog.e("ActionEmvProcess","TAG5a== " + MyApplication.convert.bcdToStr(tlv5a));
                    transData.setTrack2(MyApplication.convert.bcdToStr(tlv5a));
                }
                if (tlv !=null){
                    AppLog.e("ActionEmvProcess","TAG57== " + MyApplication.convert.bcdToStr(tlv));
                    transData.setTrack2(MyApplication.convert.bcdToStr(tlv));
                }
                if (f55 !=null){
                    AppLog.e("ActionEmvProcess","f55 " + MyApplication.convert.bcdToStr(f55));
                    transData.setSendIccData(MyApplication.convert.bcdToStr(f55));
                }


                //close rf //Avoid MP35P touch issues
                if (MyApplication.POS_MODE == MyApplication.POS_MP35P && einputType == EinputType.RF){
                    try {
                        MyApplication.usdkManage.getRf().close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                Message mmm = new Message();
                mmm.obj = emvResult.toString();
                mmm.what = SHOW;
                handler.sendMessage(mmm);
                emvProcessListener = null;

                if (EmvResult.APPROVED == emvResult
                        || EmvResult.ARQC == emvResult
                        || EmvResult.SIMPLE_FLOW_END == emvResult){

                    setResult(new ActionResult(0,transData));
                }else {
                    //check show
                    setResult(new ActionResult(-1,transData));
                }
            }
        }).start();
    }

    /**
     * set init EmvTerminalInfo
     * @return
     */
    private EmvTerminalInfo setEmvTerminalInfo(){
        EmvTerminalInfo emvTerminalInfo = new EmvTerminalInfo();
        byte[] countryCode = MyApplication.convert.strToBcd(String.format("%04d",Integer.parseInt("356")), IConvert.EPaddingPosition.PADDING_RIGHT);
//        byte[] countryCode = ByteUtil.hexString2Bytes(String.format("%04d",Integer.parseInt("156")));
        emvTerminalInfo.setUnTerminalFloorLimit(20000);
        emvTerminalInfo.setUnThresholdValue(10000);
        String terminalId = "12345678";
        emvTerminalInfo.setAucTerminalID(terminalId);
        emvTerminalInfo.setAucIFDSerialNumber("12345678");
        emvTerminalInfo.setAucTerminalCountryCode(countryCode);
        String mercherId = "132456789012345";
        emvTerminalInfo.setAucMerchantID(mercherId);
        emvTerminalInfo.setAucMerchantCategoryCode(new byte[] {0x00, 0x01});
        emvTerminalInfo.setAucMerchantNameLocation(new byte[] {0x30, 0x30, 0x30, 0x31}); //"0001"
        emvTerminalInfo.setAucTransCurrencyCode(countryCode);
        emvTerminalInfo.setUcTransCurrencyExp((byte) 2);
        emvTerminalInfo.setAucTransRefCurrencyCode(countryCode);
        emvTerminalInfo.setUcTransRefCurrencyExp((byte) 2);
        emvTerminalInfo.setUcTerminalEntryMode((byte) 0x05);

        emvTerminalInfo.setAucTerminalAcquireID("123456");
        emvTerminalInfo.setAucAppVersion(new byte[] {0x00, 0x030});
        emvTerminalInfo.setAucDefaultDDOL(new byte[] {(byte)0x9F, 0x37, 0x04});
        emvTerminalInfo.setAucDefaultTDOL(new byte[] {(byte)0x9F, 0x37, 0x04});

        emvTerminalInfo.setAucTACDenial(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00});
        emvTerminalInfo.setAucTACOnline(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00});
        emvTerminalInfo.setAucTACDefault(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00});

        emvTerminalInfo.setUcTerminalType((byte)0x22);
        //setAucTerminalCapabilities
        //emvTerminalInfo.setAucTerminalCapabilities(new byte[] {(byte)0xE0, (byte)0xF8, (byte)0xC8});
        emvTerminalInfo.setAucTerminalCapabilities(new byte[] {(byte)0xE0, (byte)0xF8, (byte)0xC8});  //E0F8C8
//        emvTerminalInfo.setAucAddtionalTerminalCapabilities(new byte[] {(byte)0xFF, (byte)0x00, (byte)0xF0, (byte)0xA0, 0x01});
        //paynext FF 80 F0 00 01
        emvTerminalInfo.setAucAddtionalTerminalCapabilities(new byte[] {(byte)0xFF, (byte)0x80, (byte)0xF0, (byte)0x00, 0x01});

        emvTerminalInfo.setUcTargetPercentage((byte) 20);
        emvTerminalInfo.setUcMaxTargetPercentage((byte) 50);
        emvTerminalInfo.setUcAccountType((byte) 0);
        emvTerminalInfo.setUcIssuerCodeTableIndex((byte) 0);
        return emvTerminalInfo;
    }

    /**
     * set init EmvKernelConfig
     * @return
     */
    private EmvKernelConfig setEmvKernelConfig(){
        EmvKernelConfig emvKernelConfig = new EmvKernelConfig();
        emvKernelConfig.setbPSE((byte) 1);
        emvKernelConfig.setbCardHolderConfirm((byte) 1);
        emvKernelConfig.setbPreferredDisplayOrder((byte) 0);
        emvKernelConfig.setbLanguateSelect((byte) 1);
        emvKernelConfig.setbRevocationOfIssuerPublicKey((byte) 1);
        emvKernelConfig.setbDefaultDDOL((byte) 1);
        emvKernelConfig.setbBypassPINEntry((byte) 1);
        emvKernelConfig.setbSubBypassPINEntry((byte) 1);
        emvKernelConfig.setbGetdataForPINTryCounter((byte) 1);
        emvKernelConfig.setbFloorLimitCheck((byte) 1);
        emvKernelConfig.setbRandomTransSelection((byte) 1);
        emvKernelConfig.setbVelocityCheck((byte) 1);
        emvKernelConfig.setbTransactionLog((byte) 1);
        emvKernelConfig.setbExceptionFile((byte) 1);
        emvKernelConfig.setbTerminalActionCode((byte) 1);
        emvKernelConfig.setbDefaultActionCodeMethod((byte) EmvDefinition.EMV_DEFAULT_ACTION_CODE_AFTER_GAC1);
        emvKernelConfig.setbTACIACDefaultSkipedWhenUnableToGoOnline((byte) 0);
        emvKernelConfig.setbCDAFailureDetectedPriorTerminalActionAnalysis((byte) 1);
        emvKernelConfig.setbCDAMethod((byte) EmvDefinition.EMV_CDA_MODE1);
        emvKernelConfig.setbForcedOnline((byte) 0);
        emvKernelConfig.setbForcedAcceptance((byte) 0);
        emvKernelConfig.setbAdvices((byte) 0);
        emvKernelConfig.setbIssuerReferral((byte) 1);
        emvKernelConfig.setbBatchDataCapture((byte) 0);
        emvKernelConfig.setbOnlineDataCapture((byte) 1);
        emvKernelConfig.setbDefaultTDOL((byte) 1);
        emvKernelConfig.setbTerminalSupportAccountTypeSelection((byte) 1);

        return emvKernelConfig;
    }

    private TransParam setTransParamConfig(byte transType, EmvTransPraram emvPraram){

        //pre processing
        TransParam  mTransParam = new TransParam();
        String amount = String.format("%012d",Long.valueOf(transData.getAmount()));

        AppLog.i("ActionEmvProcess","setTransParamConfig" + amount);

        mTransParam.setAucAmount(BytesUtil.hexString2Bytes(amount));
        mTransParam.setAucAmountOther(null);

        mTransParam.setAucTransDate(BytesUtil.hexString2Bytes(emvPraram.getData()));
        mTransParam.setAucTransTime(BytesUtil.hexString2Bytes(emvPraram.getTime()));
        mTransParam.setAucRFU(null);
        mTransParam.setAucUnNumber(PayDataUtil.getHexRandom(4));
        mTransParam.setUlTransNo(PayDataUtil.getSerialNumber());
        //Currency code needs to set kernel data 货币代码需要设置内核数据
        byte[] transCurCode = BytesUtil.hexString2Bytes(String.format("%04d",Integer.parseInt("356")));
        mTransParam.setAucTransCurCode(transCurCode);
        mTransParam.setUcTransType(transType);
        AppLog.i("ActionEmvProcess","setTransParamConfig TransParam" + mTransParam.toString());
        return mTransParam;
    }


}
