package com.topwise.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.topwise.cloudpos.aidl.AidlDeviceService;
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
import com.topwise.manager.card.impl.CardReader;
import com.topwise.manager.emv.api.IEmv;
import com.topwise.manager.emv.entity.EinputType;
import com.topwise.manager.emv.impl.TransProcess;



/**
 * 创建日期：2021/4/12 on 10:04
 * 描述:
 * 作者:  wangweicheng
 */
public class TopUsdkManage implements ITopUsdk{

    private static final String TAG =  TopUsdkManage.class.getSimpleName();

    private static String DEVICE_SERVICE_PACKAGE_NAME = "com.android.topwise.topusdkservice";
    private static String DEVICE_SERVICE_CLASS_NAME = "com.android.topwise.topusdkservice.service.DeviceService";
    private static String ACTION_DEVICE_SERVICE = "topwise_cloudpos_device_service";

    private static TopUsdkManage mService; // 单例


    private static AidlDeviceService mDeviceService;

    private TopUsdkManage() {
        Log.e(TAG,"TopUsdkManage version= " + version);
    }
    /**
     * 单例
     **/
    public static TopUsdkManage getInstance() {
        if (mService == null) {
            synchronized (TopUsdkManage.class) {
                if (mService == null) {
                    mService = new TopUsdkManage();
                }
            }
        }
        return mService;
    }
    private static final String version = "V1.0.10";
    private InitListener initListener;
    private Context mContext;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDeviceService = AidlDeviceService.Stub.asInterface(service);
            Log.e(TAG,"onServiceConnected======");
            if (initListener != null){
                initListener.OnConnection(true);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDeviceService = null;
            Log.e(TAG,"onServiceDisconnected======");
            if (initListener != null){
                initListener.OnConnection(false);
            }
        }
    };
    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void init(Context mContext,final InitListener initListener) {
        Log.e(TAG,"bindDeviceService");
        this.initListener = initListener;
        this.mContext = mContext;
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_SERVICE);
        intent.setClassName(DEVICE_SERVICE_PACKAGE_NAME, DEVICE_SERVICE_CLASS_NAME);
        mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClose() {
        try {
            if (serviceConnection != null){
                Log.e(TAG,"unBindDeviceService");
                mContext.unbindService(serviceConnection);
            }
        } catch (Exception e) {
            Log.e(TAG,"unbind DeviceService service failed : " + e);
        }
    }





    @Override
    public AidlSystem getSystem() {
        try {
            if (mDeviceService != null) {
                return AidlSystem.Stub.asInterface(mDeviceService.getSystemService());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlCameraScanCode getCameraScan() {
        try {
            if (mDeviceService != null) {
                return AidlCameraScanCode.Stub.asInterface(mDeviceService.getCameraManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPinpad getPinpad(int type) {
        try {
            if (mDeviceService != null) {
                return AidlPinpad.Stub.asInterface(mDeviceService.getPinPad(type));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlLed getLed() {
        try {
            if (mDeviceService != null) {
                return AidlLed.Stub.asInterface(mDeviceService.getLed());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public AidlBuzzer getBuzzer() {
        try {
            if (mDeviceService != null) {
                return AidlBuzzer.Stub.asInterface(mDeviceService.getBuzzer());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPrinter getPrinter() {
        try {
            if (mDeviceService != null) {
                return AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlShellMonitor getShellMonitor() {
        try {
            if (mDeviceService != null) {
                return AidlShellMonitor.Stub.asInterface(mDeviceService.getShellMonitor());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlICCard getIcc() {
        try {
            if (mDeviceService != null) {
                return AidlICCard.Stub.asInterface(mDeviceService.getInsertCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlRFCard getRf() {
        try {
            if (mDeviceService != null) {
                return AidlRFCard.Stub.asInterface(mDeviceService.getRFIDReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlMagCard getMag() {
        try {
            if (mDeviceService != null) {
                return AidlMagCard.Stub.asInterface(mDeviceService.getMagCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlEmvL2 getEmv() {
        try {
            if (mDeviceService != null) {
                return AidlEmvL2.Stub.asInterface(mDeviceService.getL2Emv());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPure getPurePay() {
        try {
            if (mDeviceService != null) {
                return AidlPure.Stub.asInterface(mDeviceService.getL2Pure());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPaypass getPaypass() {
        try {
            if (mDeviceService != null) {
                return AidlPaypass.Stub.asInterface(mDeviceService.getL2Paypass());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPaywave getPaywave() {
        try {
            if (mDeviceService != null) {
                return AidlPaywave.Stub.asInterface(mDeviceService.getL2Paywave());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public  AidlEntry getEntry() {
        try {
            if (mDeviceService != null) {
                return AidlEntry.Stub.asInterface(mDeviceService.getL2Entry());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlAmex getAmexPay() {
        try {
            if (mDeviceService != null) {
                return AidlAmex.Stub.asInterface(mDeviceService.getL2Amex());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlQpboc getUnionPay() {
        try {
            if (mDeviceService != null) {
                return AidlQpboc.Stub.asInterface(mDeviceService.getL2Qpboc());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlRupay getRupay() {
        try {
            if (mDeviceService != null) {
                return AidlRupay.Stub.asInterface(mDeviceService.getL2Rupay());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlMir getMirPay() {
        try {
            if (mDeviceService != null) {
                return AidlMir.Stub.asInterface(mDeviceService.getL2Mir());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlDpas getDpasPay() {
        try {
            if (mDeviceService != null) {
                return AidlDpas.Stub.asInterface(mDeviceService.getL2Dpas());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPsam getPsam(int devid) {
        try {
            if (mDeviceService != null) {
                return AidlPsam.Stub.asInterface(mDeviceService.getPSAMReader(devid));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlSerialport getSerialport(int port) {
        try {
            if (mDeviceService != null) {
                return AidlSerialport.Stub.asInterface(mDeviceService.getSerialPort(port));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlPedestal getPedestal() {
        try {
            if (mDeviceService != null) {
                return AidlPedestal.Stub.asInterface(mDeviceService.getPedestal());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AidlCPUCard getCpu() {
        try {
            if (mDeviceService != null) {
                return AidlCPUCard.Stub.asInterface(mDeviceService.getCPUCard());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ICardReader getCardReader() {
        CardReader cardReader = CardReader.getInstance();
        return cardReader;
    }

    @Override
    public IEmv getEmvHelper() {
        return TransProcess.getInstance();
    }
    /**
     * Binding service callback
     */
    public  interface InitListener{
        void OnConnection(boolean ret);
    }
}
