package com.example.mp35ptest.param;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.topwise.cloudpos.struct.BytesUtil;
import com.topwise.emvtest.MyApplication;
import com.topwise.manager.AppLog;
import com.topwise.manager.emv.entity.EmvAidParam;
import com.topwise.manager.emv.entity.EmvCapkParam;
import com.topwise.manager.emv.utlis.PayDataUtil;
import com.topwise.toptool.api.convert.IConvert;
import com.topwise.toptool.api.packer.ITlv;
import com.topwise.toptool.api.packer.TlvException;

import java.util.List;

/**
 * Creation date：2021/4/21 on 10:02
 * Describe: The logic can read files from XML to the database, or it can be loaded into the database separately
 * Author:wangweicheng
 */
public abstract class LoadParam<T> {
    protected static final String TAG = LoadParam.class.getSimpleName();

    protected List<String> list;

    /**
     * 保存到数据库 / save to database
     * @return
     */
    public abstract List<EmvAidParam> saveEmvAidParam();
    /**
     * 保存到数据库 / save to database
     * @return
     */
    public abstract List<EmvCapkParam> saveEmvCapkParam();

    /**
     * 从xml读取list / read the list from XML
     * @param context
     * @return
     */
    public abstract List<String> init(Context context);



    /**
     * 解析保存 / parsing save
     * @param aid
     * @return
     */
    protected EmvAidParam saveAid(String aid){
        ITlv tlv = MyApplication.packer.getTlv();
        IConvert convert = MyApplication.convert;
        ITlv.ITlvDataObjList aidTlvList;
        ITlv.ITlvDataObj tlvDataObj;
        EmvAidParam aidParam;
        byte[] value = null;
        byte[] bytes = convert.strToBcd(aid, IConvert.EPaddingPosition.PADDING_LEFT);
        try {
            aidTlvList = tlv.unpack(bytes);
            aidParam = new EmvAidParam();
            // 9f06 AID
            tlvDataObj = aidTlvList.getByTag(0x9f06);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setAid(convert.bcdToStr(value));
                }
            }
            // DF01
            tlvDataObj = aidTlvList.getByTag(0xDF01);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setSelFlag(value[0]);
                }
            }
            // 9F08
            tlvDataObj = aidTlvList.getByTag(0x9f08);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setVersion(convert.bcdToStr(value));
                }
            }

            // DF11
            tlvDataObj = aidTlvList.getByTag(0xDF11);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTbcDefualt(convert.bcdToStr(value));
                }
            }

            // DF12
            tlvDataObj = aidTlvList.getByTag(0xDF12);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTacOnline(convert.bcdToStr(value));
                }
            }

            // DF13
            tlvDataObj = aidTlvList.getByTag(0xDF13);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTacDenial(convert.bcdToStr(value));
                }
            }

            // 9F1B
            tlvDataObj = aidTlvList.getByTag(0x9F1B);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setFloorLimit(Long.parseLong(convert.bcdToStr(value)));
                    aidParam.setFloorlimitCheck(1);
                }
            }

            // DF15
            tlvDataObj = aidTlvList.getByTag(0xDF15);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setThreshold(Long.parseLong(convert.bcdToStr(value)));
                }
            }

            // DF16
            tlvDataObj = aidTlvList.getByTag(0xDF16);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setMaxTargetPer(Integer.parseInt(convert.bcdToStr(value)));
                }
            }

            // DF17
            tlvDataObj = aidTlvList.getByTag(0xDF17);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTargetPer(Integer.parseInt(convert.bcdToStr(value)));
                }
            }

            // DF14
            tlvDataObj = aidTlvList.getByTag(0xDF14);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setdDOL(convert.bcdToStr(value));
                }
            }

            // DF18
            tlvDataObj = aidTlvList.getByTag(0xDF18);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setOnlinePin(value[0] & 0x01);
                }
            }

            // 9F7B
            tlvDataObj = aidTlvList.getByTag(0x9F7B);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setEcTTLVal(Long.parseLong(convert.bcdToStr(value)));
                    aidParam.setEcTTLFlg(1);
                }
            }

            // DF19
            tlvDataObj = aidTlvList.getByTag(0xDF19);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setRdClssFLmt(Long.parseLong(convert.bcdToStr(value)));
                    aidParam.setRdClssFLmtFlg(1);
                }
            }

            // DF20
            tlvDataObj = aidTlvList.getByTag(0xDF20);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setRdClssTxnLmt(Long.parseLong(convert.bcdToStr(value)));
                    aidParam.setRdClssTxnLmtFlg(1);
                }
            }

            // DF21
            tlvDataObj = aidTlvList.getByTag(0xDF21);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setRdCVMLmt(Long.parseLong(convert.bcdToStr(value)));
                    aidParam.setRdCVMLmtFlg(1);
                }
            }

            //DF8102 tDol
            tlvDataObj = aidTlvList.getByTag(0xDF8102);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.settDOL(convert.bcdToStr(value));

                }
            }
            //9F1D riskManData
            tlvDataObj = aidTlvList.getByTag(0x9F1D);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setRiskmanData(convert.bcdToStr(value));
                }
            }
            //9F01 s acquierId
            tlvDataObj = aidTlvList.getByTag(0x9F01);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setAcquierId(convert.bcdToStr(value));
                }
            }
            //9F4E s merchName
            tlvDataObj = aidTlvList.getByTag(0x9F4E);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setMerchName(convert.bcdToStr(value));
                }
            }
            //9F15 s merchCateCode
            tlvDataObj = aidTlvList.getByTag(0x9F15);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setMerchCateCode(convert.bcdToStr(value));
                }
            }
            //9F16 s merchId
            tlvDataObj = aidTlvList.getByTag(0x9F16);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setMerchId(convert.bcdToStr(value));
                }
            }
            //9F1C s termId
            tlvDataObj = aidTlvList.getByTag(0x9F1C);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTermId(convert.bcdToStr(value));
                }
            }
            //5F2A s transCurrCode
            tlvDataObj = aidTlvList.getByTag(0x5F2A);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTransCurrCode(convert.bcdToStr(value));
                }
            }
            //5F36 i transCurrExp
            tlvDataObj = aidTlvList.getByTag(0xDF8101);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setTransCurrExp(Integer.valueOf(convert.bcdToStr(value)));
                }
            }
            //9F3C s referCurrCode
            tlvDataObj = aidTlvList.getByTag(0x9F3C);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setReferCurrCode(convert.bcdToStr(value));
                }
            }
            //9F3D byte referCurrExp
            tlvDataObj = aidTlvList.getByTag(0x9F3D);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setReferCurrExp(Integer.valueOf(convert.bcdToStr(value)));
                }
            }
            //DF8101 int referCurrCon
            tlvDataObj = aidTlvList.getByTag(0xDF8101);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    aidParam.setReferCurrCon(Integer.valueOf(convert.bcdToStr(value)));
                }
            }
            AppLog.e(TAG,"mUAidDaoUtils uAid  ==" + aidParam.toString());

            return aidParam;
        } catch (TlvException e) {
            e.printStackTrace();
            AppLog.e(TAG,"mUAidDaoUtils TlvException ==");
            return null;
        }

    }

    /**
     *
     * @param capk
     * @return
     */
    protected EmvCapkParam saveCapk(String capk){
        try {
            ITlv tlv = MyApplication.packer.getTlv();
            IConvert convert = MyApplication.convert;
            ITlv.ITlvDataObjList capkTlvList;
            ITlv.ITlvDataObj tlvDataObj;
            EmvCapkParam capkParam;
            byte[] value = null;
            AppLog.d(TAG,"Capk==" + capk);
            byte[] bytes = convert.strToBcd(capk, IConvert.EPaddingPosition.PADDING_LEFT);
            capkTlvList = tlv.unpack(bytes);
            capkParam = new EmvCapkParam();
            // 9f06 RID
            tlvDataObj = capkTlvList.getByTag(0x9f06);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setRID( convert.bcdToStr(value));
                }
            }
            // 9F2201
            tlvDataObj = capkTlvList.getByTag(0x9f22);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setKeyID(value[0]&0xFF);
                }
            }
            // DF02
            tlvDataObj = capkTlvList.getByTag(0xDF02);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setModul( convert.bcdToStr(value));
                }
            }
            // DF03
            tlvDataObj = capkTlvList.getByTag(0xDF03);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setCheckSum( convert.bcdToStr(value));
                }
            }
            // DF06
            tlvDataObj = capkTlvList.getByTag(0xDF06);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setHashInd(value[0]);
                }
            }
            // DF04
            tlvDataObj = capkTlvList.getByTag(0xDF04);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setExponent( convert.bcdToStr(value));
                }
            }
            // DF05
            tlvDataObj = capkTlvList.getByTag(0xDF05);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
    //                        String expDate = ;
    //                        if (value.length == 4) {
    //                            expDate =  convert.bcdToStr(value).substring(2, 8);
    //                        } else {
    //                            expDate = new String(value);
    //                            expDate = expDate.substring(2, 8);
    //                        }
                    capkParam.setExpDate(convert.bcdToStr(value));
                }
            }
            // DF07
            tlvDataObj = capkTlvList.getByTag(0xDF07);
            if (tlvDataObj != null) {
                value = tlvDataObj.getValue();
                if (value != null && value.length > 0) {
                    capkParam.setArithInd(value[0]);
                }
            }

            if (!TextUtils.isEmpty(capkParam.getRID()) && capkParam.getKeyID() >=0){
                String ridindex =  capkParam.getRID() +Integer.toHexString( capkParam.getKeyID()) ;
                AppLog.d(TAG,"ridindex =" + ridindex);
                capkParam.setRIDKeyID(ridindex.toUpperCase());
            }
            AppLog.d(TAG,"ridindex =" + capkParam.toString());
            return capkParam;
        } catch (TlvException e) {
            e.printStackTrace();
            return null;
        }
    }


    protected static byte[] getCAPKChecksum(EmvCapkParam capk) {

        byte[] Modulbytes = BytesUtil.hexString2Bytes(capk.getModul());
        byte[] Exponentbytes = BytesUtil.hexString2Bytes(capk.getExponent());

        byte[] data = new byte[5 + 1 + Modulbytes.length + Exponentbytes.length];
        System.arraycopy(BytesUtil.hexString2Bytes(capk.getRID()), 0, data, 0, 5);
        data[5] = (byte)capk.getKeyID();
        System.arraycopy(Modulbytes, 0, data, 6, Modulbytes.length);
        System.arraycopy(Exponentbytes, 0, data, 6 + Modulbytes.length, Exponentbytes.length);
        byte[] sha1 = PayDataUtil.getSHA1(data, 0, data.length);
        Log.d(TAG, "onSelectCapk getCAPKChecksum, sha1: " + BytesUtil.bytes2HexString(sha1));
        return sha1;
    }
}
