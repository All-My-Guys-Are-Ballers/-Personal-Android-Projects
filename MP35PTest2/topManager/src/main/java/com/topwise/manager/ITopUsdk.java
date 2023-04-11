package com.topwise.manager;

import android.content.Context;

import com.topwise.cloudpos.aidl.buzzer.AidlBuzzer;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCode;
import com.topwise.cloudpos.aidl.cpucard.AidlCPUCard;
import com.topwise.cloudpos.aidl.emv.level2.AidlAmex;
import com.topwise.cloudpos.aidl.emv.level2.AidlDpas;
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.AidlEntry;
import com.topwise.cloudpos.aidl.emv.level2.AidlMir;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaypass;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaywave;
import com.topwise.cloudpos.aidl.emv.level2.AidlPure;
import com.topwise.cloudpos.aidl.emv.level2.AidlQpboc;
import com.topwise.cloudpos.aidl.emv.level2.AidlRupay;
import com.topwise.cloudpos.aidl.iccard.AidlICCard;
import com.topwise.cloudpos.aidl.led.AidlLed;
import com.topwise.cloudpos.aidl.magcard.AidlMagCard;
import com.topwise.cloudpos.aidl.pedestal.AidlPedestal;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.psam.AidlPsam;
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;
import com.topwise.cloudpos.aidl.serialport.AidlSerialport;
import com.topwise.cloudpos.aidl.shellmonitor.AidlShellMonitor;
import com.topwise.cloudpos.aidl.system.AidlSystem;
import com.topwise.manager.card.api.ICardReader;
import com.topwise.manager.emv.api.IEmv;
import com.topwise.manager.emv.entity.EinputType;


/**
 * 创建日期：2021/4/15 on 8:57
 * 描述:
 * 作者:  wangweicheng
 */
public interface ITopUsdk {
    String getVersion();
    void init(Context mContext,TopUsdkManage.InitListener initListener);
    void onClose();
    //pay
    AidlEmvL2 getEmv();
    AidlPure getPurePay();
    AidlPaypass getPaypass();
    AidlPaywave getPaywave();
    AidlEntry getEntry();
    AidlAmex getAmexPay();
    AidlQpboc getUnionPay();
    AidlRupay getRupay();
    AidlMir   getMirPay();
    AidlDpas getDpasPay();
    //sys
    AidlSystem getSystem();
    AidlCameraScanCode getCameraScan();
    //pinpad
    AidlPinpad getPinpad(int type);
    //device
    AidlLed    getLed();
    AidlBuzzer getBuzzer();
    AidlPrinter getPrinter();
    AidlShellMonitor getShellMonitor();
    AidlICCard getIcc();
    AidlRFCard getRf();
    AidlMagCard getMag();
    AidlPsam getPsam(int devid);
    AidlSerialport getSerialport(int port);
    AidlPedestal getPedestal();
    AidlCPUCard getCpu();
//    AidlPedestalSerialManager getPedestalSerialManager();
    //add
    ICardReader getCardReader();
    IEmv getEmvHelper();


}
