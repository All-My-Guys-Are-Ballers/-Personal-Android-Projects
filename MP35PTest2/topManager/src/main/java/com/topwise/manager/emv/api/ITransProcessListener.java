package com.topwise.manager.emv.api;

import com.topwise.cloudpos.aidl.emv.level2.EmvCandidateItem;
import com.topwise.cloudpos.struct.TlvList;
import com.topwise.manager.emv.entity.Amounts;
import com.topwise.manager.emv.entity.EmvEntity;

/**
 * 创建日期：2021/6/10 on 15:30
 * 描述:
 * 作者:wangweicheng
 */
public interface ITransProcessListener {
    /**请求多应用选择*/
    int requestAidSelect(String[] aids);
    /**
     * contactlsee set TLV List Before GPO
     * @param kernelType
     * @return
     */
    TlvList requestKernalListTLV(byte kernelType);
    /**
     * 回传 卡信息
     * @param emvCandidateItem
     */
    void onUpdateEmvCandidateItem(EmvCandidateItem emvCandidateItem);
    /**
     *update Kernel Type
     * @param kernelType
     */
    void onUpdateKernelType(byte kernelType);
    /**
     *Aid final choice
     */
    boolean finalAidSelect();
    /**Request confirmation of card information ture */
    boolean onConfirmCardInfo(String cardNo) ;
    /** requestImportPin PIN ture 有密码 / false bypass */
    EmvEntity requestImportPin(int type, boolean lasttimeFlag, String amt);
    /** Request authentication */
    boolean requestUserAuth(int certype, String certnumber);
    /**Request online*/
    EmvEntity onRequestOnline();
    /**
     * get the amount
     * @return
     */
    Amounts requestImportAmount();

    /**
     * 0  find card success
     * -1 failed
     * @return
     */
    int onSecondCheckCard();
}
