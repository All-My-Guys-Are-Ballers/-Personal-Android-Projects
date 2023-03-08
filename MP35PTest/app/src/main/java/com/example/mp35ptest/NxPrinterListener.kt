package com.example.mp35ptest

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener

object NxPrinterListener: AidlPrinterListener.Stub() {
    var printRunning = false

    override fun onError(p0: Int) {
        Log.e(TAG, "Print error, error code: $p0")
        printRunning = false
    }

    override fun onPrintFinish() {
        printRunning = false
    }

}