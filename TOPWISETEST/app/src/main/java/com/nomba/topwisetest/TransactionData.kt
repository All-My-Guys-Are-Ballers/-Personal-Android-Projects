package com.nomba.topwisetest

import com.topwise.manager.emv.entity.EinputType

data class TransactionData(
    val orderNo : String? = null,
    val orderNodis: String? = null,
    val amount // 交易金额  //单位是元 a
            : String? = null,
    val cardAmount // card Amount卡交易金额
            : String? = null,
    val cashAmount // cash Amount现金交易金额
            : String? = null,
    val tipAmount // tip Amount小费金额
            : String? = null,
    val balance // balance 余额
            : String? = null,
    val balanceFlag // 余额标识C/D
            : String? = null,

    //emv
    val emvResult // emv ResultEMV交易的执行状态
            : Byte = 0,
    val interOrgCode // 国际组织代码
            : String? = null,

    val transNo // pos流水号
            : Long = 0,
    val origTransNo // 原pos流水号
            : Long = 0,
    val batchNo // 批次号
            : Long = 0,
    val origBatchNo // 原批次号
            : Long = 0,
    val transType: Int // 交易类型
            = 0,
    val origTransType // 原交易类型
            : String? = null,
    val merchID: String? = null,

    val isUpload: Boolean // 是否已批上送
            = false,
    val transState // 交易状态
            : String? = null,
    val oper: String? = null,
    val track1 // 磁道一信息
            : String? = null,
    val track2 // 磁道二数据
            : String? = null,
    val track3 // 磁道三数据
            : String? = null,
    val isEncTrack: Boolean // 磁道是否加密
            = false,
    val reason // 冲正原因
            : String? = null,
    val reserved // 63域附加域
            : String? = null,
    val datetime // 交易时间
            : String? = null,
    val time // 交易时间
            : String? = null,
    val date // 交易日期
            : String? = null,
    val termID: String? = null,
    var pan // 主账号
            : String? = null,
    val expDate // 卡有效期
            : String? = null,
    val cardSerialNo // 23 域，卡片序列号
            : String? = null,
    val enterMode // 输入模式
            : EinputType? = null,
    var hasPin: Boolean // 是否有输密码
            = false,
    val sendIccData // IC卡信息,55域
            : String? = null,
    val scriptTag // IC卡信息tag 9F5B
            : String? = null,
    val dupIccData // IC卡冲正信息,55域
            : String? = null,
    val isOnlineTrans: Boolean // 是否为联机交易
            = false,
    val origDate // 原交易日期
            : String? = null,
    val origTime // 原交易日期
            : String? = null,
    val isserCode // 发卡行标识码
            : String? = null,
    val acqCode // 收单机构标识码
            : String? = null,
    val isSupportBypass: Boolean = false,
    var Random: String? = null,

    val issuerResp // 发卡方保留域
            : String? = null,
    val centerResp // 中国银联保留域
            : String? = null,
    val recvBankResp // 受理机构保留域
            : String? = null,

    //     String TVR;
    //     String TSI;
    val ICPositiveData: String? = null,

    /**
     * 响应码
     */
     val responseCode: String? = null,

    /**
     * 相应码对应的错误信息
     */
     val responseMsg: String? = null,
    val settleDate // 清算日期
            : String? = null,
    val acqCenterCode // 受理方标识码,pos中心号(返回包时用)
            : String? = null,
    val refNo // 系统参考号
            : String? = null,
    val origRefNo // 原系统参考号
            : String? = null,
    val authCode // 授权码
            : String? = null,
    val origAuthCode // 原授权码
            : String? = null,
    val tc // IC卡交易证书(TC值)tag9f26,(BIN)
            : String? = null,
    val arqc // 授权请求密文(ARQC)
            : String? = null,
    val arpc // 授权响应密文(ARPC)
            : String? = null,
    val tvr // 终端验证结果(TVR)值tag95
            : String? = null,
    var aid // 应用标识符AID
            : String? = null,
    val emvAppLabel // 应用标签
            : String? = null,
    val emvAppName // 应用首选名称
            : String? = null,
    val tsi // 交易状态信息(TSI)tag9B
            : String? = null,
    val atc // 应用交易计数器(ATC)值tag9f36
            : String? = null,
    val origProcCode //原消息码
            : String? = null,
    val qrCode //二维码数据
            : String? = null,
    val origQrCode //原二维码数据
            : String? = null,
    val qrVoucher: String? = null,
    val origQrVoucher: String? = null,
    val pinKsn // pinKsn
            : String? = null,
    val dataKsn // dataKsn
            : String? = null,
    var field52 //打印提示
            : String? = null,
    val field58 //打印数据
            : String? = null,
    val field22 //22域
            : String? = null,
    val procCode //消息码
            : String? = null,
    val cardHolderName //持卡人姓名
            : String? = null,
    //card type
//    public static final byte KERNTYPE_MC = 0x02;
//    public static final byte KERNTYPE_VISA = 0x03;
//    public static final byte KERNTYPE_AMEX = 0x04;
//    public static final byte KERNTYPE_JCB = 0x05;
//    public static final byte KERNTYPE_ZIP = 0x06; //Discover ZIP or 16
//    public static final byte KERNTYPE_DPAS = 0x06;//Discover DPAS
//    public static final byte KERNTYPE_QPBOC = 0x07;
//    public static final byte KERNTYPE_RUPAY = 0x0D;
//    public static final byte  KERNTYPE_PURE = 0x12; //add wwc

    //card type
    //    public static final byte KERNTYPE_MC = 0x02;
    //    public static final byte KERNTYPE_VISA = 0x03;
    //    public static final byte KERNTYPE_AMEX = 0x04;
    //    public static final byte KERNTYPE_JCB = 0x05;
    //    public static final byte KERNTYPE_ZIP = 0x06; //Discover ZIP or 16
    //    public static final byte KERNTYPE_DPAS = 0x06;//Discover DPAS
    //    public static final byte KERNTYPE_QPBOC = 0x07;
    //    public static final byte KERNTYPE_RUPAY = 0x0D;
    //    public static final byte  KERNTYPE_PURE = 0x12; //add wwc
    var kernelType: Byte // 0-emv 2- MC 3 -VISA 4 AMEX 5-JCB 6-_ZIP 7-QPBOC  13 -RUPAY 18 -PURE
            = 0,

    val needScript: Boolean = false,
    val tag71: String? = null,
    val tag72: String? = null,
    val tag8A: String? = null,
    val tag91: String? = null,

    val recvIccData: String? = null,

    )
