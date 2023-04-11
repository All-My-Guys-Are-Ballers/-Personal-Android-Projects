package com.nomba.topwisetest

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.topwise.cloudpos.aidl.emv.level2.EmvCandidateItem
import com.topwise.cloudpos.struct.BytesUtil
import com.topwise.cloudpos.struct.TlvList
import com.topwise.manager.AppLog
import com.topwise.manager.emv.api.IEmv
import com.topwise.manager.emv.api.ITransProcessListener
import com.topwise.manager.emv.entity.Amounts
import com.topwise.manager.emv.entity.EmvEntity
import java.io.UnsupportedEncodingException

class EmvTransProcessListener(context: Context, transData: TransactionData, emv: IEmv): ITransProcessListener {
    var context: Context? = null
    var transactionData: TransactionData
    private var emv: IEmv

    init {
        this.context = context
        this.transactionData = transData
        this.emv = emv
    }
    override fun requestAidSelect(aids: Array<out String>?): Int {
        return 0
    }

    override fun requestKernalListTLV(kernelType: Byte): TlvList {
        val allList = TlvList()
        val cvmLimit = "000000500000"
        allList.addTlv("DF4D", cvmLimit) //rupay

        allList.addTlv("DF8126", cvmLimit) //MS


        AppLog.i(
            TAG,
            "requestKernalListTLV  $allList"
        )
        return allList
    }

    override fun onUpdateEmvCandidateItem(emvCandidateItem: EmvCandidateItem?) {
        if (emvCandidateItem != null && emvCandidateItem.aucDisplayName != null) {
            var s: String? = null
            try {
                s = String(emvCandidateItem.aucDisplayName, charset("gbk")).trim { it <= ' ' }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            AppLog.i(
                TAG,
                "onUpdateEmvCandidateItem  $s"
            )
        }
    }

    override fun onUpdateKernelType(kernelType: Byte) {
        transactionData.kernelType = kernelType
    }

    override fun finalAidSelect(): Boolean {
        AppLog.d(TAG, "finalAidSelect = ")
        val aucAid: ByteArray
        var aid: String? = null
        val aucRandom: ByteArray
        var random: String? = null
        aucAid = emv.getTlv(0x4F)
        aucRandom = emv.getTlv(0x9f37)
        if (aucAid != null) {
            aid = BytesUtil.bytes2HexString(aucAid)
            random = BytesUtil.bytes2HexString(aucRandom)
            transactionData.aid = aid
            transactionData.Random = random
            Log.d(TAG, "aid: $aid")
        } else {
            return false
        }
        return true
    }

    override fun onConfirmCardInfo(cardNo: String?): Boolean {
        transactionData.pan = cardNo
        return true
    }

    override fun requestImportPin(type: Int, lasttimeFlag: Boolean, amt: String?): EmvEntity {
        val emvEntity = EmvEntity()
        transactionData.hasPin = true
        transactionData.field52 = "6412"
        emvEntity.pinData = "6412"
        return emvEntity
    }

    override fun requestUserAuth(certype: Int, certnumber: String?): Boolean {
        return false
    }

    override fun onRequestOnline(): EmvEntity {
        Log.d(TAG, "onRequestOnline")
        return EmvEntity()
    }

    override fun requestImportAmount(): Amounts {
        val amounts = Amounts()
        amounts.transAmount = transactionData.amount
        return amounts
    }

    override fun onSecondCheckCard(): Int {
        return 0
    }

}