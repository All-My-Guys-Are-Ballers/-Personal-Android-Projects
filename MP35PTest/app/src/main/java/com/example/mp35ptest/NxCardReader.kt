package com.example.mp35ptest

import android.content.ContentValues.TAG
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.mp35ptest.entity.TransData
import com.example.mp35ptest.utlis.PanUtlis
import com.topwise.manager.card.api.ICardReader
import com.topwise.manager.card.entity.CardData
import com.topwise.manager.card.impl.CardReader
import com.topwise.manager.emv.entity.EinputType

class NxReadCardListener(val iCardReader: ICardReader, private val transData: TransData) : CardReader.onReadCardListener {
    override fun getReadState(cardData: CardData?) {
        if (dialogLoading != null) {
            dialogLoading.dismiss()
            dialogLoading.tickTimerStop()
        }

        Log.d(TAG, "cardData" + cardData.toString())
        iCardReader.close(false)
        if (cardData != null && CardData.EReturnType.OK === cardData.geteReturnType()) {
            when (cardData.geteCardType()) {
                IC -> {
//                    Toast.makeText("IC CARD")
                    transData.setEnterMode(EinputType.IC)
                    myHandler.sendEmptyMessage(NEXT_IC_PROCESS)
                }
                RF -> {
                    transData.setEnterMode(EinputType.RF)
//                    sendShow("RF CARD")
                    myHandler.sendEmptyMessage(NEXT_RF_PROCESS)
                }
                MAG -> {
                    val pan: String = PanUtlis.getPan(cardData.getTrack2())
                    transData.setPan(pan)
                    if (!TextUtils.isEmpty(pan)) sendShow("PAN: " + transData.getPan())
                    myHandler.sendEmptyMessage(NEXT_CHECK_PIN)
                }
                else -> {}
            }
        } else {
//            sendShow(cardData.toString())
        }
    }
}