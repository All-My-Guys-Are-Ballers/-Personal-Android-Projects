package com.topwise.manager.emv;

import android.os.RemoteException;

import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.AidlEntry;
import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.manager.AppLog;
import com.topwise.manager.TopUsdkManage;
import com.topwise.manager.emv.api.ITransProcessListener;
import com.topwise.manager.emv.entity.InputParam;
import com.topwise.manager.emv.enums.EmvResult;
import com.topwise.toptool.api.convert.IConvert;
import com.topwise.toptool.api.packer.ITlv;
import com.topwise.toptool.api.packer.TlvException;
import com.topwise.toptool.impl.TopTool;

/**
 * 创建日期：2021/6/16 on 10:04
 * 描述:
 * 作者:wangweicheng
 */
public abstract class BaseTrans {
    private static final String TAG = BaseTrans.class.getSimpleName();
    protected AidlEntry entryL2 = TopUsdkManage.getInstance().getEntry();
    protected AidlEmvL2 emvL2 = TopUsdkManage.getInstance().getEmv();
    protected ContactLessProcess.ProcessListener processListener;
    protected ITransProcessListener rfProcessListener;
    protected InputParam inputParam;

    public void setInputParam(InputParam inputParam) {
        this.inputParam = inputParam;
    }
    public void setProcessListener(ContactLessProcess.ProcessListener processListener) {
        this.processListener = processListener;
    }

    public void setRfProcessListener(ITransProcessListener rfProcessListener) {
        this.rfProcessListener = rfProcessListener;
    }

    /**
     * start Contactless process
     * @return EmvResult
     */
    public abstract EmvResult start();


    protected String getCurrentAid() {
        AppLog.d(TAG, "getCurrentAid()");
        String aid = null;
        try {
            byte[] aucAid = emvL2.EMV_GetTLVData(0x9F06);
            if (aucAid != null) {
                aid = BytesUtil.bytes2HexString(aucAid);
                AppLog.d(TAG,"aid: " + aid);
            } else {
                AppLog.d(TAG, "getTLVData aucAid == null" );
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return aid;
    }
    protected String getPan(String track) {
        if (track == null)
            return null;

        int len = track.indexOf('=');
        if (len < 0) {
            len = track.indexOf('D');
            if (len < 0)
                return null;
        }

        if ((len < 10) || (len > 19))
            return null;
        return track.substring(0, len);
    }

    protected  byte[] setTlvTagData(String [] data,int[] tag){
        AppLog.d(TAG," getTlvTagData lists =====" + tag +"  " +data );

        ITlv tlv = TopTool.getInstance().getPacker().getTlv();
        try {
            ITlv.ITlvDataObj tlvDataObject = tlv.createTlvDataObject();
            for (int i = 0; i < tag.length; i++) {
                tlvDataObject.setTag(tag[i]);
                byte[] bytes = TopTool.getInstance().getConvert().strToBcd(data[i], IConvert.EPaddingPosition.PADDING_LEFT);
                tlvDataObject.setValue(bytes);
            }

            return tlv.pack(tlvDataObject);
        } catch (TlvException e) {
            e.printStackTrace();
        }
        return null;
    }
}
