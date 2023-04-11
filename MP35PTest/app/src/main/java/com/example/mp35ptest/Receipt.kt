package com.example.mp35ptest

import android.graphics.Bitmap

data class Receipt(
    val logo: Bitmap,
    val title: String,
    val merchantName: String,
    val transactionType: String,
    val terminalID: String,
    val isReprint: Boolean,
    val stan: String,
    val dateTime: String,
    val amount: String,
    val cardPan: String,
    val expiryDate: String,
    val authCode: String,
    val rrn: String,
    val responseCode: String,
    val phoneNumber: String = "01-8885008",
    val appVersion: String

)
