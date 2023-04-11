package com.example.mp35ptest2;

import android.app.Application;
import android.widget.Toast;

import com.example.mp35ptest2.device.Device;
import com.example.mp35ptest2.param.SysParam;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.toptool.api.ITool;
import com.topwise.toptool.api.convert.IConvert;
import com.topwise.toptool.api.packer.IPacker;
import com.topwise.toptool.impl.TopTool;


/**
 * Creation date：2021/6/23 on 14:05
 * Describe:
 * Author:wangweicheng
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();

    public static final int POS_T1   = 0x01;  //0x01 T1  0x02 MP35P
    public static final int POS_MP35P = 0x02;
    public static int POS_MODE = POS_MP35P;

    public static MyApplication mApp;
    public static TopUsdkManage usdkManage;
    public static IConvert convert;
    public static IPacker packer;
    public static ITool topTool;
    public static SysParam sysParam;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.mApp = this;
        POS_MODE = POS_MP35P;
        init();
    }


    private void init(){
        topTool = TopTool.getInstance();
        convert = topTool.getConvert();
        packer = topTool.getPacker();

        sysParam = SysParam.getInstance(MyApplication.mApp);

        usdkManage = TopUsdkManage.getInstance();
        AppLog.i(TAG,"init TopUsdkManage" +POS_MODE);
        usdkManage.init(this, new TopUsdkManage.InitListener() {
            @Override
            public void OnConnection(boolean ret) {

                AppLog.i(TAG,"init OnConnection " + ret);
                if (ret){
                    if (POS_MODE == POS_T1)
                    {
                        Device.enableHomeAndRecent(true);
                    }
                }else {
                    Toast.makeText(MyApplication.this,"SDK Bind Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
