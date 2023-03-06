package com.example.mp35ptest

import android.content.ContentValues.TAG
import android.util.Log
import com.topwise.cloudpos.aidl.magcard.MagCardListener
import com.topwise.cloudpos.aidl.magcard.TrackData

class NxMagCardListener(var isSwipeCard: Boolean = false): MagCardListener.Stub() {
    override fun onTimeout() {
        Log.d(TAG, "Credit Card Overtime")
        isSwipeCard = false
    }

    override fun onError(arg0: Int) {
        isSwipeCard = false
        Log.d(TAG, "Credit Card error, Error code is$arg0")
    }

    override fun onCanceled() {
        isSwipeCard = false
        Log.d(TAG, "Credit Card Swipe is cancelled")
    }

    override fun onSuccess(trackData: TrackData)
    {
            isSwipeCard = false
            Log.d(TAG, "Credit Card Success")
            Log.d(TAG, "One Track data" + trackData.firstTrackData)
            Log.d(TAG, "Two Track data" + trackData.secondTrackData)
            Log.d(TAG, "Three track data" + trackData.thirdTrackData)
            Log.d(TAG, "Card number data" + trackData.cardno)
            Log.d(TAG, "The card is valid till" + trackData.expiryDate)
            Log.d(TAG, "Format the track data" + trackData.formatTrackData)
            Log.d(TAG, "Card Service Code" + trackData.serviceCode)
    }

    override fun onGetTrackFail() {
        isSwipeCard = false
        Log.d(TAG, "Credit Card failed")
    }
}