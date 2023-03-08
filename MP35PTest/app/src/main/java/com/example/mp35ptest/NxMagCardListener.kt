package com.example.mp35ptest

import android.content.ContentValues.TAG
import android.util.Log
import com.example.mp35ptest.screens.SwipeCardStatus
import com.topwise.cloudpos.aidl.magcard.MagCardListener
import com.topwise.cloudpos.aidl.magcard.TrackData
import kotlinx.coroutines.flow.MutableStateFlow

object NxMagCardListener: MagCardListener.Stub() {
    var globalVar = MutableStateFlow(SwipeCardStatus.IDLE) // Flow that holds an Int
    override fun onTimeout() {
        Log.d(TAG, "Credit Card Overtime")
        globalVar.value = SwipeCardStatus.TIMEOUT
    }

    override fun onError(arg0: Int) {
        Log.d(TAG, "Credit Card error, Error code is$arg0")
        globalVar.value = SwipeCardStatus.ERROR
    }

    override fun onCanceled() {
        Log.d(TAG, "Credit Card Swipe is cancelled")
        globalVar.value = SwipeCardStatus.CANCELLED
    }

    override fun onSuccess(trackData: TrackData)
    {
        Log.d(TAG, "Credit Card Success")
        Log.d(TAG, "One Track data" + trackData.firstTrackData)
        Log.d(TAG, "Two Track data" + trackData.secondTrackData)
        Log.d(TAG, "Three track data" + trackData.thirdTrackData)
        Log.d(TAG, "Card number data" + trackData.cardno)
        Log.d(TAG, "The card is valid till" + trackData.expiryDate)
        Log.d(TAG, "Format the track data" + trackData.formatTrackData)
        Log.d(TAG, "Card Service Code" + trackData.serviceCode)
        globalVar.value = SwipeCardStatus.SUCCESS
    }

    override fun onGetTrackFail() {
        Log.d(TAG, "Credit Card failed")
        globalVar.value = SwipeCardStatus.FAILED
    }
}