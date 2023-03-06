package com.example.mp35ptest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.buzzer.AidlBuzzer;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCode;
import com.topwise.cloudpos.aidl.cpucard.AidlCPUCard;
import com.topwise.cloudpos.aidl.decoder.AidlDecoderManager;
import com.topwise.cloudpos.aidl.emv.level2.AidlAmex;
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.AidlEntry;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaypass;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaywave;
import com.topwise.cloudpos.aidl.emv.level2.AidlPure;
import com.topwise.cloudpos.aidl.emv.level2.AidlQpboc;
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

public class DeviceServiceManager {
    private static String TAG = "DeviceServiceManager";

    private static String DEVICE_SERVICE_PACKAGE_NAME = "com.android.topwise.topusdkservice";
    private static String DEVICE_SERVICE_CLASS_NAME = "com.android.topwise.topusdkservice.service.DeviceService";
    private static String ACTION_DEVICE_SERVICE = "topwise_cloudpos_device_service";

    private static DeviceServiceManager instance;
    private Context mContext;
    private AidlDeviceService mDeviceService;
    private boolean isBind = false;

    public static DeviceServiceManager getInstance() {
        Log.d(TAG,"getInstance()");
        if (null == instance) {
            synchronized (DeviceServiceManager.class) {
                instance = new DeviceServiceManager();
            }
        }
        return instance;
    }

    public boolean isBind() {
        return isBind;
    }

    public boolean bindDeviceService(Context context) {
        Log.i(TAG,"");

        this.mContext = context;
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_SERVICE);
        intent.setClassName(DEVICE_SERVICE_PACKAGE_NAME, DEVICE_SERVICE_CLASS_NAME);

        try {
            boolean bindResult = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.i(TAG,"bindResult = " + bindResult);
            return bindResult;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void unBindDeviceService() {
        Log.i(TAG,"unBindDeviceService");
        try {
            mContext.unbindService(mConnection);
        } catch (Exception e) {
            Log.i(TAG,"unbind DeviceService service failed : " + e);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mDeviceService = AidlDeviceService.Stub.asInterface(service);
            Log.d(TAG,"gz mDeviceService" + mDeviceService);
            isBind = true;
            Log.i(TAG,"onServiceConnected  :  " + mDeviceService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG,"onServiceDisconnected  :  " + mDeviceService);
            mDeviceService = null;
            isBind = false;
        }
    };

    public AidlSystem getSystemManager() {
        try {
            if (mDeviceService != null) {
                return AidlSystem.Stub.asInterface(mDeviceService.getSystemService());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public AidlDecoderManager getDecoder() {
        try {
            if (mDeviceService != null) {
                return AidlDecoderManager.Stub.asInterface(mDeviceService.getDecoder());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlLed getLed() {
        try {
            if (mDeviceService != null) {
                Log.d(TAG, "Help Me");
                return AidlLed.Stub.asInterface(mDeviceService.getLed());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPinpad getPinpadManager(int devid) {
        try {
            if (mDeviceService != null) {
                return AidlPinpad.Stub.asInterface(mDeviceService.getPinPad(devid));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPrinter getPrintManager() {
        try {
            if (mDeviceService != null) {
                return AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlICCard getICCardReader() {
        try {
            if (mDeviceService != null) {
                return AidlICCard.Stub.asInterface(mDeviceService.getInsertCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlRFCard getRfCardReader() {
        try {
            if (mDeviceService != null) {
                return AidlRFCard.Stub.asInterface(mDeviceService.getRFIDReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPsam getPsamCardReader(int devid) {
        try {
            if (mDeviceService != null) {
                return AidlPsam.Stub.asInterface(mDeviceService.getPSAMReader(devid));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlMagCard getMagCardReader() {
        try {
            if (mDeviceService != null) {
                return AidlMagCard.Stub.asInterface(mDeviceService.getMagCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlCPUCard getCPUCardReader() {
        try {
            if (mDeviceService != null) {
                return AidlCPUCard.Stub.asInterface(mDeviceService.getCPUCard());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlSerialport getSerialPort(int port) {
        try {
            if (mDeviceService != null) {
                return AidlSerialport.Stub.asInterface(mDeviceService.getSerialPort(port));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    public AidlEmvL2 getEmvL2() {
        try {
            if (mDeviceService != null) {
                return AidlEmvL2.Stub.asInterface(mDeviceService.getL2Emv());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPure getL2Pure() {
        try {
            if (mDeviceService != null) {
                return AidlPure.Stub.asInterface(mDeviceService.getL2Pure());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPaypass getL2Paypass() {
        try {
            if (mDeviceService != null) {
                return AidlPaypass.Stub.asInterface(mDeviceService.getL2Paypass());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPaywave getL2Paywave() {
        try {
            if (mDeviceService != null) {
                return AidlPaywave.Stub.asInterface(mDeviceService.getL2Paywave());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlEntry getL2Entry() {
        try {
            if (mDeviceService != null) {
                return AidlEntry.Stub.asInterface(mDeviceService.getL2Entry());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlAmex getL2Amex() {
        try {
            if (mDeviceService != null) {
                return AidlAmex.Stub.asInterface(mDeviceService.getL2Amex());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlQpboc getL2Qpboc() {
        try {
            if (mDeviceService != null) {
                return AidlQpboc.Stub.asInterface(mDeviceService.getL2Qpboc());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlCameraScanCode getCameraManager() {
        try {
            if (mDeviceService != null) {
                return AidlCameraScanCode.Stub.asInterface(mDeviceService.getCameraManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bundle expandFunction(Bundle param) {
        try {
            if (mDeviceService != null) {
                return mDeviceService.expandFunction(param);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

}