package com.topwise.manager.card.api;


import com.topwise.manager.card.impl.CardReader;

/**
 * 创建日期：2021/6/9 on 13:40
 * 描述:
 * 作者:wangweicheng
 */
public interface ICardReader {
    /**
     *
     * @param isMag
     * @param isIcc
     * @param isRf
     * @param outtime 单位秒/Unit second
     * @param onReadCardListener
     */
    void startFindCard(boolean isMag, boolean isIcc, boolean isRf, int outtime, CardReader.onReadCardListener onReadCardListener);

    /**
     * Whether to power off, if it is a callback return, there is a power off operation inside the interface, if it is a card search, press cancel to return, you need to power off
     * 是否下电，如果是回调返回，接口内部有下电操作，如果是寻卡按取消返回，需要下电
     * @param closeDevice
     */
    void close(boolean closeDevice);
}
