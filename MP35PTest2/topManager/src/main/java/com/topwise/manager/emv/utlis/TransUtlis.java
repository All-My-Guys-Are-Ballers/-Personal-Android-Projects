package com.topwise.manager.emv.utlis;

import com.topwise.manager.utlis.DataUtils;

/**
 * 创建日期：2021/6/17 on 10:23
 * 描述:
 * 作者:wangweicheng
 */
public class TransUtlis {
    //EMV error code definition
    public static final int EMV_OK = 0;
    public static final int EMV_APPROVED = 1;
    public static final int EMV_FORCE_APPROVED = 2;
    public static final int EMV_DECLINED = 3;
    public static final int EMV_NOT_ALLOWED = 5;
    public static final int EMV_NO_ACCEPTED = 6;
    public static final int EMV_TERMINATED = 7;
    public static final int EMV_CARD_BLOCKED = 8;
    public static final int EMV_APP_BLOCKED = 9;
    public static final int EMV_NO_APP = 10;
    public static final int EMV_FALLBACK = 11;
    public static final int EMV_CAPK_EXPIRED = 12;
    public static final int EMV_CAPK_CHECKSUM_ERROR = 13;
    public static final int EMV_AID_DUPLICATE = 14;
    public static final int EMV_CERTIFICATE_RECOVER_FAILED = 15;
    public static final int EMV_DATA_AUTH_FAILED = 16;
    public static final int EMV_UN_RECOGNIZED_TAG = 17;
    public static final int EMV_DATA_NOT_EXISTS = 18;
    public static final int EMV_DATA_LENGTH_ERROR = 19;
    public static final int EMV_INVALID_TLV = 20;
    public static final int EMV_INVALID_RESPONSE = 21;
    public static final int EMV_DATA_DUPLICATE = 22;
    public static final int EMV_MEMORY_NOT_ENOUGH = 23;
    public static final int EMV_MEMORY_OVERFLOW = 24;
    public static final int EMV_PARAMETER_ERROR = 25;
    public static final int EMV_ICC_ERROR = 26;
    public static final int EMV_NO_MORE_DATA = 27;
    public static final int EMV_CAPK_NO_FOUND = 28;
    public static final int EMV_AID_NO_FOUND = 29;
    public static final int EMV_FORMAT_ERROR = 30;
    public static final int EMV_ONLINE_REQUEST = 31;//online request -by wfh20190805
    public static final int EMV_SELECT_NEXT_AID = 32;//Select next AID
    public static final int EMV_TRY_AGAIN = 33;//Try Again. ICC read failed.
    public static final int EMV_SEE_PHONE = 34;//Status Code returned by IC card is 6986, please see phone. GPO 6986 CDCVM.
    public static final int EMV_TRY_OTHER_INTERFACE = 35;//Try other interface -by wfh20190805
    public static final int EMV_ICC_ERR_LAST_RECORD = 36;
    public static final int EMV_CANCEL = 254;
    public static final int EMV_OTHER_ERROR = 255;

    public static final byte PINTYPE_OFFLINE = 0x01;
    public static final byte PINTYPE_OFFLINE_LASTTIME = 0x02;
    public static final byte PINTYPE_ONLINE = 0x03;


    public static final int CLSS_USE_CONTACT = -23;
    public static final int CLSS_REFER_CONSUMER_DEVICE = -40;  //caixh added
    public static final int ENTRY_KERNEL_6A82_ERR = -105;
    public static final int CLSS_RESELECT_APP = -35;
    public static final int CLSS_TERMINATE = -25;
    public static final int ICC_CMD_ERR = -2;
    public static final int EMV_APP_BLOCK = -5;


    public static String convertorVale(String indata ,int len, char c){
        if (DataUtils.isNullString(indata) || len == 0) return null;
        StringBuffer stringBuffer = new StringBuffer();
        if (indata.length() >= len) return indata;
        int l = len - indata.length();
        for (int i = 0; i<l;i++){
            stringBuffer.append(c);
        }
        stringBuffer.append(indata);
        return stringBuffer.toString();
    }
}
