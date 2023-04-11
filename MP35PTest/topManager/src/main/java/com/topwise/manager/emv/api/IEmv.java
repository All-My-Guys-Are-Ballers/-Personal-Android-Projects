package com.topwise.manager.emv.api;

import com.topwise.cloudpos.aidl.emv.level2.EmvKernelConfig;
import com.topwise.cloudpos.aidl.emv.level2.EmvTerminalInfo;
import com.topwise.cloudpos.aidl.emv.level2.TransParam;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.emv.entity.EinputType;
import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;
import com.topwise.manager.emv.entity.EmvTransPraram;
import com.topwise.manager.emv.enums.EmvResult;

import com.topwise.toptool.api.packer.ITlv;

import java.util.List;

/**
 * 创建日期：2021/6/10 on 16:09
 * 描述:
 * 作者:wangweicheng
 */
public interface IEmv {
    void init(EinputType einputType);
    /**
     * 获取 version
     * @return
     */
    String getVersion();

    /**
     *
     * @param ITransProcessListener
     */
    void setProcessListener(ITransProcessListener ITransProcessListener);

    /**
     * 设置预处理参数
     * @param emvKernelConfig
     * @param emvTerminalInfo
     */
    void setConfig(EmvKernelConfig emvKernelConfig, EmvTerminalInfo emvTerminalInfo);

    /**
     *
     * @param contactLessConfig
     */
    void setContactLessConfig(TransParam contactLessConfig,EmvTerminalInfo emvTerminalInfo);
    /**
     * 设置非接参数
     * @param tlvList
     * @return
     */
    int setContactlessParameter(TlvList tlvList);

    /**
     * 设置CAPK LIST
     * @param paramList
     */
    void setCapkList(List<EmvCapkParam> paramList);

    /**
     * 设置AID LIST
     * @param paramList
     */
    void setAidList(List<EmvAidParam> paramList);

    /**
     * get TLV
     * @param paramInt
     * @return
     */
    byte[] getTlv(int paramInt);

    /**
     * set TLV
     * @param paramInt
     * @param paramArrayOfbyte
     */
    void setTlv(int paramInt, byte[] paramArrayOfbyte) ;

    /**
     *
     */
    EmvResult emvProcess(EmvTransPraram emvTransData);
    /**
     *
     * @return
     */
    TlvList readEcCurrencyBalance() ;

    /**
     * 验证密码
     * @param paramInt
     * @param paramArrayOfbyte
     * @return
     */
    byte[] verifyPin(int paramInt, byte[] paramArrayOfbyte);

    /**返回读取卡片脱机余额结果*/
    void onReadCardOffLineBalance(String moneyCode, String balance, String secondMoneyCode, String secondBalance) throws android.os.RemoteException;
    /**返回读取卡片交易日志结果*/
    EmvResult onReadCardTransLog(EmvTransPraram emvTransData, ITlv.ITlvDataObjList logs) throws android.os.RemoteException;
    /**返回读取卡片圈存日志结果*/
    void onReadCardLoadLog(String atc, String checkCode, com.topwise.cloudpos.aidl.emv.PCardLoadLog[] logs) throws android.os.RemoteException;

}
