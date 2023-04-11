package com.topwise.manager.card.impl;

import android.content.Context;
import android.os.RemoteException;
import android.os.SystemClock;

import com.topwise.cloudpos.aidl.iccard.AidlICCard;
import com.topwise.cloudpos.aidl.magcard.AidlMagCard;
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;
import com.topwise.cloudpos.aidl.shellmonitor.AidlShellMonitor;
import com.topwise.cloudpos.aidl.shellmonitor.InstructionSendDataCallback;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;

import com.topwise.manager.card.api.ICardReader;
import com.topwise.manager.card.entity.CardData;
import com.topwise.manager.utlis.CardTimer;

import java.util.concurrent.CountDownLatch;

/**
 * 创建日期：2021/6/9 on 13:50
 * 描述: 单例实现寻卡
 * 作者:wangweicheng
 */
public class CardReader implements ICardReader {
    private static final String TAG = CardReader.class.getSimpleName();
    private static CardReader instance;
    private Context context;
    private boolean isRunging;
    private CardTimer cardTimer;

    private FindCardThread findCardThread;
    private boolean isMag;
    private boolean isIcc;
    private boolean isRf;
    private CardData cardData;
    private onReadCardListener onReadCardListener;
    public static final int PURE_MAG_CARD = 0X00;
    public static final int ICC_MAG_CARD = 0X58;
    public static final int MSR_TRACK_1 = 0X01;
    public static final int MSR_TRACK_2 = 0X02;
    public static final int MSR_TRACK_3 = 0X03;
    private byte mResultCode;
    private byte[] mResultData;
    private boolean bCloseAll;
    private AidlMagCard magCard = TopUsdkManage.getInstance().getMag();
    private AidlICCard icCard = TopUsdkManage.getInstance().getIcc();
    private AidlRFCard rfCard = TopUsdkManage.getInstance().getRf();
    private AidlShellMonitor aidlShellMonitor = TopUsdkManage.getInstance().getShellMonitor();

    private CardReader() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public synchronized static CardReader getInstance() {
        if (instance == null) {
            instance = new CardReader();
        }
        return instance;
    }

    private boolean openMag(){
        try {
            return magCard.open();
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.e(TAG,   "openMag: false ==============");
            return false;
        }
    }

    private boolean openIc(){
        try {
            return icCard.open();
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.e(TAG,   "openIc: false ==============");
            return false;
        }
    }
    private boolean openRf(){
        try {
            return rfCard.open();
        } catch (RemoteException e) {
            e.printStackTrace();
            AppLog.e(TAG,   "openRf: false ==============");
            return false;
        }
    }

    @Override
    public void startFindCard(boolean isMag, boolean isIcc, boolean isRf, int outtime,
                              onReadCardListener onReadCardListener) {
        AppLog.e(TAG,   "startFindCard: isMag= "+isMag + " isIcc="+isIcc + " isRf=" + isRf + " outtime=" +outtime);
        this.isMag = isMag;
        this.isIcc = isIcc;
        this.isRf  = isRf;
        this.onReadCardListener  = onReadCardListener;

        if (cardTimer != null) {
            cardTimer.cancel();
            cardTimer = null;
        }
        bCloseAll = false;
        cardTimer = new CardTimer(outtime,1);
        cardTimer.setTimeCountListener(new CardTimer.TickTimerListener() {
            @Override
            public void onFinish() {
                AppLog.e(TAG,   "CardTimer: onFinish ============== ");
                if (bCloseAll)  closeDevice(false,false,false,true);
                setResult(new CardData(CardData.EReturnType.OTHER_ERR));
                return;
            }

            @Override
            public void onTick(long leftTime) {
                if (findCardThread != null) AppLog.e(TAG,   "FindCardThread ID onTick ==============" +findCardThread.getId() + "  isInterrupted " + findCardThread.isInterrupted());
                AppLog.e(TAG,   "CardTimer: onTick ============== " +leftTime);
                if (leftTime == 1) bCloseAll = true;
            }
        });
        cardTimer.start();

        isRunging = true;
        findCardThread = new FindCardThread();
        findCardThread.start();
        AppLog.e(TAG,   "FindCardThread ID ==============" +findCardThread.getId());
    }

    class FindCardThread extends Thread{
        @Override
        public void run() {
            //check and open
            if (isMag && !openMag()){
                if (onReadCardListener != null){
                    setResult(new CardData(CardData.EReturnType.OPEN_MAG_ERR));
                    closeDevice(false,false,false,true);
                    return;
                }
            }
            if (isIcc && !openIc()){
                if (onReadCardListener != null){
                    setResult(new CardData(CardData.EReturnType.OPEN_IC_ERR));
                    closeDevice(false,false,false,true);
                    return;
                }
            }
            if (isRf && !openRf()){
                if (onReadCardListener != null){
                    setResult(new CardData(CardData.EReturnType.OPEN_RF_ERR));
                    closeDevice(false,false,false,true);
                    return;
                }
            }
            while (true && !isInterrupted()){
                if (!isRunging){
                    break;
                }
                //mag
                if (isMag){
                    try {
                        byte startRead = readMag();
                        if (startRead == ICC_MAG_CARD || startRead == PURE_MAG_CARD){
                            byte[] firstTlvArray = readData((byte) MSR_TRACK_1);
                            byte[] secondTlvArray = readData((byte)MSR_TRACK_2);
                            byte[] thirdTlvArray = readData((byte)MSR_TRACK_3);
                            if (firstTlvArray == null && secondTlvArray == null &&
                                    thirdTlvArray == null ){
                                AppLog.e(TAG,   "Read mag Exception ==============" );
                                cardData = new CardData(CardData.EReturnType.OPEN_MAG_RESET_ERR);
                                setResult(cardData);
                                closeDevice(false,false,false,true);
                                return;
                            }
                            cardData = new CardData(CardData.EReturnType.OK, CardData.ECardType.MAG);
                            if (firstTlvArray != null){
                                AppLog.e(TAG,   "Read mag firstTlvArray ==============" + new String(firstTlvArray));
                                int realFirstLen = firstTlvArray.length - 2;
                                if (realFirstLen > 0) {
                                    byte[] realFirstByte = new byte[realFirstLen];
                                    System.arraycopy(firstTlvArray, 1, realFirstByte, 0, realFirstLen);
                                    cardData.setTrack1( new String(realFirstByte));
                                }
                            }
                            if (secondTlvArray != null){
                                AppLog.e(TAG,   "Read mag secondTlvArray ==============" + new String(secondTlvArray));
                                int realSecondLen = secondTlvArray.length - 2;
                                if (realSecondLen > 0) {
                                    byte[] realSecondByte = new byte[realSecondLen];
                                    System.arraycopy(secondTlvArray, 1, realSecondByte, 0, realSecondLen);
                                    cardData.setTrack2(new String(realSecondByte));
                                }

                            }
                            if (thirdTlvArray != null){
                                AppLog.e(TAG,   "Read mag thirdTlvArray ==============" + new String(thirdTlvArray));
                                int realThirdLen = thirdTlvArray.length - 2;
                                if (realThirdLen > 0) {
                                    byte[] realThirdByte = new byte[realThirdLen];
                                    System.arraycopy(thirdTlvArray, 1, realThirdByte, 0, realThirdLen);
                                    cardData.setTrack3(new String(realThirdByte));
                                }

                            }
                            setResult(cardData);
                            closeDevice(false,false,false,true);
                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLog.e(TAG,   "Read mag Exception ==============" + e.getMessage());
                        cardData = new CardData(CardData.EReturnType.OPEN_MAG_RESET_ERR);
                        setResult(cardData);
                        closeDevice(false,false,false,true);
                        return;
                    }
                }
                //ic
                if (isIcc ){
                    try {
                        if (icCard.isExist()){
                            byte[] data = icCard.reset(0);
                            if (data != null && data.length > 0) {
                                AppLog.e(TAG,   "Read Icc SUCC==============");
                                cardData = new CardData(CardData.EReturnType.OK, CardData.ECardType.IC);
                                setResult(cardData);
                                closeDevice(true,false,false,false);
                                return;
                            }else {
                                AppLog.e(TAG,   "Read Icc reset fail ==============");
                                cardData =  new CardData(CardData.EReturnType.OPEN_IC_RESET_ERR);
                                setResult(cardData);
                                closeDevice(false,false,false,true);
                                return;
                            }
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        AppLog.e(TAG,   "Read Icc Exception ==============" + e.getMessage());
                        cardData = new CardData(CardData.EReturnType.OPEN_IC_RESET_ERR);
                        setResult(cardData);
                        closeDevice(false,false,false,true);
                        return;
                    }
                }
                //rf
                if (isRf){
                    try {
                        if (rfCard.isExist()){
                            byte[] data = rfCard.reset(0);
                            if (data != null && data.length > 0) {
                                AppLog.e(TAG,   "Read Rf SUCC ==============");
                                cardData = new CardData(CardData.EReturnType.OK, CardData.ECardType.RF);
                                setResult(cardData);
                                closeDevice(false,true,false,false);
                                return;
                            }else {
                                AppLog.e(TAG,   "Read Rf reset fail ==============");
                                cardData = new CardData(CardData.EReturnType.OPEN_RF_RESET_ERR);
                                setResult(cardData);
                                closeDevice(false,false,false,true);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppLog.e(TAG,   "Read Rf Exception ==============" + e.getMessage());
                        cardData = new CardData(CardData.EReturnType.OPEN_RF_RESET_ERR);
                        setResult(cardData);
                        closeDevice(false,false,false,true);
                        return;
                    }
                }
                SystemClock.sleep(20);
            }
        }
    }

    /**
     *
     * @return
     * @throws Exception
     */
    private byte readMag() throws Exception {
        AppLog.e(TAG,   "Read readMag ==============" );
        byte[] mBuff = new byte[]{(byte) 0x0b, (byte) 0xb8};
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        aidlShellMonitor.sendIns(6, (byte)0x68, (byte)0x04, (byte) 0x02, mBuff, new InstructionSendDataCallback.Stub() {
            @Override
            public void onReceiveData(byte resultCode, byte[] tlvArray) throws RemoteException {

                mResultCode = resultCode;
                mResultData = tlvArray;
                if (countDownLatch != null) countDownLatch.countDown();
            }
        });
        if (countDownLatch != null) countDownLatch.await();
        AppLog.e(TAG,   "Read readMag resultCode ============== "  + mResultCode);
        return mResultCode;
    }

    /**
     *
     * @param inByte
     * @return
     * @throws Exception
     */
    private byte [] readData(byte inByte) throws Exception{
        AppLog.e(TAG,   "Read readData ==============" );
        byte[] mBuff = new byte[1];
        mBuff[0] = inByte;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        aidlShellMonitor.sendIns(6, (byte)0x68, (byte)0x05, (byte) 0x01, mBuff, new InstructionSendDataCallback.Stub() {
            @Override
            public void onReceiveData(byte resultCode, byte[] tlvArray) throws RemoteException {
                mResultCode = resultCode;
                mResultData = tlvArray;
                if (countDownLatch != null) countDownLatch.countDown();
            }
        });
        if (countDownLatch != null) countDownLatch.await();
        AppLog.e(TAG,   "Read readData resultCode ============== "  + mResultCode);
        return mResultCode == (byte) 0x00 ? mResultData : null;
    }

    @Override
    public void close(boolean closeDevice) {
        AppLog.e(TAG,   "close ==== " + closeDevice);
        if (closeDevice)
            closeDevice(false,false,false,true);

        if (cardTimer != null){
            cardTimer.cancel();
            AppLog.e(TAG,   "close  cardTimer.cancel ==== ");
            cardTimer = null;
        }
        if (findCardThread != null && !findCardThread.isInterrupted()){
            findCardThread.interrupt();
            AppLog.e(TAG,   "close   findCardThread.interrupt ==== ");
            findCardThread = null;
        }
        isRunging = false;
        instance = null;
    }
    private void setResult(CardData cardData){
        isRunging = false;
        if (cardTimer != null){
            cardTimer.cancel();
            AppLog.e(TAG,   "setResult  cardTimer.cancel ==== ");
            cardTimer = null;
        }
        if (findCardThread != null && !findCardThread.isInterrupted()){
            findCardThread.interrupt();
            AppLog.e(TAG,   "setResult   findCardThread.interrupt ==== ");
            findCardThread = null;
        }
        onReadCardListener.getReadState(cardData);
    }
    private void closeDevice(boolean isICC,boolean isRF,boolean isMAG,boolean all) {
        AppLog.e(TAG,   "closeDevice in== isMag=" +isMag + " isIcc="+isIcc+" isRf=" + isRf);
        AppLog.e(TAG,   "closeDevice in== isMAG=" +isMAG + " isICC="+isICC+" isRF=" + isRF + " ALL=" + all);
        try {
            if (isMag && isMAG){
                if (isMag && magCard != null) magCard.close();
                if (isIcc && icCard != null) icCard.close();
                if (isRf && rfCard != null) rfCard.close();
            }
            if (isIcc && isICC){
                if (isMag && magCard != null) magCard.close();
                if (isIcc && rfCard != null) rfCard.close();
            }
            if (isRf && isRF){
                if (isMag && magCard != null) magCard.close();
                if (isIcc && icCard != null) icCard.close();
            }
            if (all){
                if (isMag && magCard != null) magCard.close();
                if (isIcc && icCard != null) icCard.close();
                if (isRf && rfCard != null) rfCard.close();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public interface onReadCardListener{
        void getReadState(CardData cardData);
    }
}
