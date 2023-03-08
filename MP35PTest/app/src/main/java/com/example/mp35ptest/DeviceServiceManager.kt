package com.example.mp35ptest

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.topwise.cloudpos.aidl.AidlDeviceService
import com.topwise.cloudpos.aidl.buzzer.AidlBuzzer
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCode
import com.topwise.cloudpos.aidl.cpucard.AidlCPUCard
import com.topwise.cloudpos.aidl.decoder.AidlDecoderManager
import com.topwise.cloudpos.aidl.emv.level2.*
import com.topwise.cloudpos.aidl.iccard.AidlICCard
import com.topwise.cloudpos.aidl.led.AidlLed
import com.topwise.cloudpos.aidl.magcard.AidlMagCard
import com.topwise.cloudpos.aidl.pedestal.AidlPedestal
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad
import com.topwise.cloudpos.aidl.printer.AidlPrinter
import com.topwise.cloudpos.aidl.psam.AidlPsam
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard
import com.topwise.cloudpos.aidl.serialport.AidlSerialport
import com.topwise.cloudpos.aidl.shellmonitor.AidlShellMonitor
import com.topwise.cloudpos.aidl.system.AidlSystem

@SuppressLint("StaticFieldLeak")
object DeviceServiceManager {
    @SuppressLint("StaticFieldLeak")
    private var mContext: Context? = null
    private var mDeviceService: AidlDeviceService? = null
    var isBind = false
        private set

    @SuppressLint("StaticFieldLeak")
    fun bindDeviceService(context: Context?): Boolean {
        Log.i(TAG, "")
        if (context != null) {
            mContext = context.applicationContext
        }
        val intent = Intent()
        intent.action = ACTION_DEVICE_SERVICE
        intent.setClassName(DEVICE_SERVICE_PACKAGE_NAME, DEVICE_SERVICE_CLASS_NAME)
        try {
            val bindResult = mContext!!.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
            Log.i(TAG, "bindResult = $bindResult")
            return bindResult
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun unBindDeviceService() {
        Log.i(TAG, "unBindDeviceService")
        try {
            mContext!!.unbindService(mConnection)
        } catch (e: Exception) {
            Log.i(
                TAG,
                "unbind DeviceService service failed : $e"
            )
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mDeviceService = AidlDeviceService.Stub.asInterface(service)
            Log.d(
                TAG,
                "gz mDeviceService$mDeviceService"
            )
            isBind = true
            Log.i(
                TAG,
                "onServiceConnected  :  $mDeviceService"
            )
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(
                TAG,
                "onServiceDisconnected  :  $mDeviceService"
            )
            mDeviceService = null
            isBind = false
        }
    }
    val systemManager: AidlSystem?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlSystem.Stub.asInterface(mDeviceService!!.systemService)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val buzzer: AidlBuzzer?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlBuzzer.Stub.asInterface(mDeviceService!!.buzzer)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val decoder: AidlDecoderManager?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlDecoderManager.Stub.asInterface(mDeviceService!!.decoder)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val led: AidlLed?
        get() {
            try {
                if (mDeviceService != null) {
                    Log.d(TAG, "Help Me")
                    return AidlLed.Stub.asInterface(mDeviceService!!.led)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }

    fun getPinpadManager(devid: Int): AidlPinpad? {
        try {
            if (mDeviceService != null) {
                return AidlPinpad.Stub.asInterface(mDeviceService!!.getPinPad(devid))
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    val printManager: AidlPrinter?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlPrinter.Stub.asInterface(mDeviceService!!.printer)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val iCCardReader: AidlICCard?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlICCard.Stub.asInterface(mDeviceService!!.insertCardReader)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val rfCardReader: AidlRFCard?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlRFCard.Stub.asInterface(mDeviceService!!.rfidReader)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }

    fun getPsamCardReader(devid: Int): AidlPsam? {
        try {
            if (mDeviceService != null) {
                return AidlPsam.Stub.asInterface(mDeviceService!!.getPSAMReader(devid))
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    val magCardReader: AidlMagCard?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlMagCard.Stub.asInterface(mDeviceService!!.magCardReader)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val cPUCardReader: AidlCPUCard?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlCPUCard.Stub.asInterface(mDeviceService!!.cpuCard)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }

    fun getSerialPort(port: Int): AidlSerialport? {
        try {
            if (mDeviceService != null) {
                return AidlSerialport.Stub.asInterface(mDeviceService!!.getSerialPort(port))
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    val shellMonitor: AidlShellMonitor?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlShellMonitor.Stub.asInterface(mDeviceService!!.shellMonitor)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val pedestal: AidlPedestal?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlPedestal.Stub.asInterface(mDeviceService!!.pedestal)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val emvL2: AidlEmvL2?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlEmvL2.Stub.asInterface(mDeviceService!!.l2Emv)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Pure: AidlPure?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlPure.Stub.asInterface(mDeviceService!!.l2Pure)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Paypass: AidlPaypass?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlPaypass.Stub.asInterface(mDeviceService!!.l2Paypass)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Paywave: AidlPaywave?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlPaywave.Stub.asInterface(mDeviceService!!.l2Paywave)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Entry: AidlEntry?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlEntry.Stub.asInterface(mDeviceService!!.l2Entry)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Amex: AidlAmex?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlAmex.Stub.asInterface(mDeviceService!!.l2Amex)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val l2Qpboc: AidlQpboc?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlQpboc.Stub.asInterface(mDeviceService!!.l2Qpboc)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }
    val cameraManager: AidlCameraScanCode?
        get() {
            try {
                if (mDeviceService != null) {
                    return AidlCameraScanCode.Stub.asInterface(mDeviceService!!.cameraManager)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            return null
        }

    fun expandFunction(param: Bundle?): Bundle? {
        try {
            if (mDeviceService != null) {
                return mDeviceService!!.expandFunction(param)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }


    private val TAG = "NxDeviceServiceManager"
    private val DEVICE_SERVICE_PACKAGE_NAME = "com.android.topwise.topusdkservice"
    private val DEVICE_SERVICE_CLASS_NAME =
        "com.android.topwise.topusdkservice.service.DeviceService"
    private val ACTION_DEVICE_SERVICE = "topwise_cloudpos_device_service"
//    var instance: DeviceServiceManager? = null
//        get() {
//            Log.d(TAG, "getInstance()")
//            if (null == field) {
//                synchronized(DeviceServiceManager::class.java) {
//                    field = DeviceServiceManager()
//                }
//            }
//            return field
//        }
//        private set

}