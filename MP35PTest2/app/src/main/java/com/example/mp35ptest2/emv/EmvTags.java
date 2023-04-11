package com.example.mp35ptest2.emv;

import com.example.mp35ptest2.MyApplication;
import com.example.mp35ptest2.entity.TransData;
import com.topwise.manager.emv.api.IEmv;
import com.topwise.toptool.api.packer.ITlv;
import com.topwise.toptool.api.packer.TlvException;


/**
 * Creation date：2021/6/23 on 16:39
 * Describe:
 * Author:wangweicheng
 */
public class EmvTags {
    /**
     * SALE
     */
    public static final int[] TAGS_SALE_BYTE = { 0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63 ,0x57};

    /**
     * reverse
     */
    public static final int[] TAGS_DUP = { 0x95, 0x9F10, 0x9F1E, 0xDF31 };
    //========================end

    public static byte[] getF55(int transType, IEmv iEmv, boolean isDup, boolean isEC){
        switch (transType){
            case 0:
                if (isDup) {
                    return getValueList(TAGS_DUP, iEmv);
                }
                return getValueList(TAGS_SALE_BYTE, iEmv);
            default:
                break;
        }
        return null;
    }

    private static byte[] getValueList(int[] tags, IEmv emv) {
        if (tags == null || tags.length == 0) {
            return null;
        }

        ITlv tlv = MyApplication.packer.getTlv();
        ITlv.ITlvDataObjList tlvList = tlv.createTlvDataObjectList();
        for (int tag : tags) {
            try {
                byte[] value = emv.getTlv(tag);
                if (value == null || value.length == 0) {
                    if (tag == 0x9f03) {
                        value = new byte[6];
                    } else {
                        continue;
                    }
                }
                ITlv.ITlvDataObj obj = tlv.createTlvDataObject();
                obj.setTag(tag);
                obj.setValue(value);
                tlvList.addDataObj(obj);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        try {
            return tlv.pack(tlvList);
        } catch (TlvException e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * transtype:Transaction type, definition
     * SALE 0x00
     * @param transData
     * @return
     */

    public static byte checkKernelTransType(TransData transData){
        switch (transData.getTransType()){
            case 0:
                return 0x00;
            default:
                return 0x00;
        }
    }
}
