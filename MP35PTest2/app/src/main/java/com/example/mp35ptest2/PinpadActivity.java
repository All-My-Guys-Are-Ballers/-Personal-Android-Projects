package com.example.mp35ptest2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.example.mp35ptest2.core.AAction;
import com.example.mp35ptest2.core.ActionResult;
import com.example.mp35ptest2.core.TransContext;
import com.example.mp35ptest2.device.ConfiUtils;
import com.example.mp35ptest2.entity.TransResult;
import com.example.mp35ptest2.param.SysParam;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.aidl.pinpad.GetPinListener;
import com.topwise.cloudpos.data.PinpadConstant;
import com.topwise.manager.AppLog;
import com.topwise.manager.utlis.DataUtils;

/**
 * Creation date：2021/8/30 on 16:20
 * Describe:
 * Author:wangweicheng
 */
public class PinpadActivity extends Activity {
    protected static final String TAG = PinpadActivity.class.getSimpleName();
    private TextView textViewPinblock;
    private TextView textViewAmount;
    private TextView textViewPinblockShow;
    private TextView textViewPinblockpan;
    private AidlPinpad mPinpad = MyApplication.usdkManage.getPinpad(0);
    private String navTitle;
    private String panBlock;
    private String amount;
    private String cashAmount;
    private int enterPinType;
    private pinPadThread pinPadThread;
    public static SysParam sysParam;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad);
        sysParam =SysParam.getInstance(this);
        textViewAmount = (TextView)findViewById(R.id.tv_amount_vale);
        textViewPinblock = (TextView)findViewById(R.id.tv_pinblock_vale);
        textViewPinblockShow = (TextView)findViewById(R.id.tv_pinblock_show);
        textViewPinblockpan = (TextView)findViewById(R.id.tv_pinblock_pan);

        navTitle = getIntent().getStringExtra("NAV_TITLE");
        panBlock = getIntent().getStringExtra("PANBLOCK");
        amount = getIntent().getStringExtra("TRANS_AMOUNT");
        cashAmount = getIntent().getStringExtra("TRANS_AMOUNT_CASH");
        enterPinType =  getIntent().getIntExtra("ENTERPINTYPE",0);
        if (0 == enterPinType){
            textViewPinblockShow.setText("ONLINE PASSWORD:");
        }else {
            textViewPinblockShow.setText("OFFLINE PASSWORD:");
        }

        if (!TextUtils.isEmpty(panBlock)){
            textViewPinblockpan.setText(panBlock);
        }
        if (!TextUtils.isEmpty(amount)){
            textViewAmount.setText(amount);
        }
    }
    private boolean isShow = false;
    @Override
    protected void onStart() {
        super.onStart();
        AppLog.i(TAG,"onStart========");
        if (mPinpad != null && !isShow){
            isShow = true;
            pinPadThread = new pinPadThread(panBlock,amount);
            pinPadThread.start();
            AppLog.i(TAG,"pinPadThread start========");
        }
    }
    private boolean isConfirm = false;
    private static final int ONKEY_SHOW = 0x01;
    private static final int ONKEY_ONERR = 0x02;
    private static final int ONKEY_COMFIRM = 0x03;
    private static final int ONKEY_CANCEL = 0x04;
    private static final int ONKEY_COMFIRM_NULL = 0x05;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            ActionResult result;
            switch (msg.what){
                case ONKEY_SHOW:
                    String showPin = (String)msg.obj;
                    if (!DataUtils.isNullString(showPin)){
                        textViewPinblock.setText(showPin);
                    }else {
                        textViewPinblock.setText(showPin);
                    }

                    break;
                case ONKEY_ONERR:

                    int errorCode = (int)msg.obj;

                    result = new ActionResult(TransResult.ERR_ABORTED, null);
                    finishPinpad(result);
                    break;
                case ONKEY_COMFIRM:

                    byte[] pin = ( byte[])msg.obj;
                    result = new ActionResult(TransResult.SUCC, MyApplication.convert.bcdToStr(pin));
                    finishPinpad(result);
                    break;
                case ONKEY_COMFIRM_NULL:

                    result = new ActionResult(TransResult.SUCC, "");
                    finishPinpad(result);
                    break;
                case ONKEY_CANCEL:
                    result = new ActionResult(TransResult.ERR_ABORTED, null);
                    finishPinpad(result);
                    break;
            }

        }
    };


    class pinPadThread extends Thread{
        private String cardNo;
        private String amount;

        public pinPadThread(String cardNo, String amount) {
            this.cardNo = cardNo;
            this.amount = amount;
        }

        @Override
        public void run() {
            try {
                mPinpad.setPinKeyboardMode(1);
                mPinpad.getPin(getParam(cardNo, amount), new GetPinListener.Stub() {
                    @Override
                    public void onInputKey(int len, String msg) throws RemoteException {
                        Log.i(TAG, "showPinpadActivity onInputKey");

                        if (isConfirm){
                            return;
                        }
                        Message message = new Message();
                        message.what = ONKEY_SHOW;
                        if (0 == enterPinType){
                            message.obj = msg;
                        }else {
                            message.obj = filling(msg,"*");;
                        }
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onError(int errorCode) throws RemoteException {
                        Log.i(TAG, "showPinpadActivity onError " + errorCode);
                        Message message = new Message();
                        message.obj = errorCode;
                        message.what = ONKEY_ONERR;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onConfirmInput(byte[] pin) throws RemoteException {
                        isConfirm = true;
                        if (pin == null || pin.length == 0){
                            handler.sendEmptyMessage(ONKEY_COMFIRM_NULL);
                        }else {
                            Log.i(TAG, "showPinpadActivity onConfirmInput " + MyApplication.convert.bcdToStr(pin));
                            Message message = new Message();
                            message.obj = pin;
                            message.what = ONKEY_COMFIRM;
                            handler.sendMessage(message);
                        }

                    }

                    @Override
                    public void onCancelKeyPress() throws RemoteException {
                        Log.i(TAG, "showPinpadActivity onCancelKeyPress ");
                        handler.sendEmptyMessage(ONKEY_CANCEL);
                    }

                    @Override
                    public void onStopGetPin() throws RemoteException {
                        Log.i(TAG, "showPinpadActivity onStopGetPin ");
                    }

                   @Override
                   public void onTimeout() throws RemoteException {
                       Log.i(TAG, "get  onTimeout  ");

                   }
                });

            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private Bundle getParam(String cardNo, String amount) {
        String key_type;
        key_type = sysParam.get("key_type");
        AppLog.i(TAG, "getParam()" + enterPinType);
//        cardNo = "5399830243429808";
        AppLog.i(TAG, "cardNo()" + cardNo);
//        0x00:Online PIN
//        0x01:Offline  PIN
        final Bundle param = new Bundle();
        param.putInt("wkeyid", ConfiUtils.pinIndex);
        param.putInt("keytype", enterPinType);
        if (key_type == "DUKPT") {
            param.putInt("key_type", PinpadConstant.KeyType.KEYTYPE_DUKPT_DES); //ipek
        }
        param.putByteArray("random", null);
        param.putInt("inputtimes", 1);
        param.putString("input_pin_mode", "0,4,5,6,7,8,9,10,11,12");
        param.putString("pan", cardNo);
        param.putString("tips", "RMB:" + amount);
        param.putBoolean("is_lkl", false);
        return param;
    }
    private String filling(String msg,String f){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < msg.length(); i++) {
            stringBuffer.append(f);
        }
        return stringBuffer.toString();
    }
    private void closePinpad(){

        if (MyApplication.POS_MODE == MyApplication.POS_T1 && mPinpad != null) { //Only T1 supports this interface
            try {
                mPinpad.stopGetPin();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            AppLog.i(TAG,"closePinpad stopGetPin");
            SystemClock.sleep(500); //tp switch need to add 500 ms
        }
        if (pinPadThread != null){
            AppLog.i(TAG,"pinPadThread interrupt========");
            pinPadThread.interrupt();
            pinPadThread = null;
        }

    }
    boolean hasfinish = false;
    public void finish(ActionResult result) {
        if (hasfinish) {
            return;
        }
        hasfinish = true;
        AAction action = TransContext.getInstance().getCurrentAction();
        if (action != null) {
            action.setResult(result);
            finish();
        } else {
            finish();
        }
    }

    private void finishPinpad(ActionResult result){
        closePinpad();
        finish(result);
    }
}
