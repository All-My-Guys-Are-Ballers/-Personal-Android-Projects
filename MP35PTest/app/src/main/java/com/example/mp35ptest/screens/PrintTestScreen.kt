package com.example.mp35ptest.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.RemoteException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.mp35ptest.DeviceServiceManager
import com.example.mp35ptest.NxPrinterListener
import com.example.mp35ptest.NxPrinterListener.printRunning
import com.example.mp35ptest.R
import com.example.mp35ptest.Receipt
import com.topwise.cloudpos.aidl.printer.*


@Composable
fun PrintTestScreen(){
    val ctx = LocalContext.current
    val printerDev = DeviceServiceManager.printManager
    val receiptData = Receipt(
        BitmapFactory.decodeResource(ctx.resources, R.drawable.gtblogo),
        title = "MERCHANT COPY",
        merchantName = "Merchant Acq",
        terminalID = "2KUD6PBR",
        transactionType = "CASH-OUT",
        isReprint = true,
        stan = "592477",
        dateTime = "06/03/2023 12:56:53",
        amount = "100.00",
        cardPan = "539983**********5895",
        expiryDate = "10/24",
        authCode = "",
        rrn = "230306125247",
        responseCode = "00",
        appVersion = "2.3.0"
    )
    Surface() {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                if (printerDev != null) {
                    printReceipt(ctx, printerDev, receiptData)
                }
            }) {
                Text(text = "Print Receipt")
            }

//            TextButton(onClick = { printQRCode() }) {
//
//            }

        }

    }



}


fun printBitmap(printerDev: AidlPrinter, bitmap: Bitmap) {
    if (printRunning) {
        return
    }
    try {
        printerDev.addRuiImage(bitmap, 0)
        printerDev.addRuiText(object : ArrayList<PrintItemObj?>() {
            init {
                add(PrintItemObj("\n\n"))
            }
        })
        printRunning = true
        printerDev.printRuiQueue(NxPrinterListener)
    } catch (e: RemoteException) {
        // TODO Auto-generated catch block
        e.printStackTrace()
    }
}

fun printReceipt(context: Context, printerDev: AidlPrinter, receiptData: Receipt){
    printHeader(context, printerDev, receiptData)
    printBody(context,printerDev,receiptData)

}

fun printHeader(context: Context, printerDev: AidlPrinter, receiptData: Receipt){
    if(!printRunning){
        printerDev.addRuiImage(receiptData.logo, 0) //add logo

        val template = PrintTemplate.getInstance()
        template.init(context, null)
        template.clear()

        if (receiptData.isReprint) template.add(
            TextUnit(
                "Reprint",
                TextUnit.TextSize.NORMAL,
                Align.CENTER
            )
        )
        template.add(TextUnit(receiptData.title, TextUnit.TextSize.NORMAL, Align.CENTER))
        template.add(TextUnit("MERCHANT NAME: ${receiptData.merchantName}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("TERMINAL ID: ${receiptData.terminalID}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit(receiptData.transactionType, TextUnit.TextSize.LARGE, Align.CENTER))
        template.add(TextUnit("STAN: ${receiptData.stan}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(
            TextUnit(
                "DATE/TIME: ${receiptData.dateTime}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(
            TextUnit(
                "AMOUNT: ${receiptData.amount}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(TextUnit(receiptData.cardPan, TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(
            TextUnit(
                "EXPIRY DATE: ${receiptData.expiryDate}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(
            TextUnit(
                "AUTHORIZATION CODE: ${receiptData.authCode}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(TextUnit("RRN: ${receiptData.rrn}", TextUnit.TextSize.NORMAL, Align.LEFT))

        when(receiptData.responseCode){
            "00" -> template.add(TextUnit("APPROVED", TextUnit.TextSize.LARGE, Align.CENTER))
            "06" -> template.add(TextUnit("ERROR", TextUnit.TextSize.LARGE, Align.CENTER))
        }

        template.add(TextUnit("RESPONSE CODE: ${receiptData.responseCode}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("MEANING: ${receiptData.responseCode}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("DEVICE ID: ${receiptData.terminalID}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("MOBILE NO: ${receiptData.phoneNumber}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("APP VERSION: ${receiptData.appVersion}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("*************************************", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(TextUnit("Nomba......Providing affordable Financial Services", TextUnit.TextSize.NORMAL, Align.CENTER))
        template.add(TextUnit("*************************************", TextUnit.TextSize.NORMAL, Align.LEFT))

        printerDev.addRuiImage(template.printBitmap, 0)
        printerDev.addRuiText(object : ArrayList<PrintItemObj?>() {
            init {
                add(PrintItemObj("\n"))
            }
        })

        printRunning = true
        printerDev.printRuiQueue(NxPrinterListener)
    }
}

fun printBody(context: Context, printerDev: AidlPrinter, receiptData: Receipt){
    if(!printRunning){
        val template = PrintTemplate.getInstance()
        template.init(context, null)
        template.clear()

        template.add(TextUnit("STAN: ${receiptData.stan}", TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(
            TextUnit(
                "DATE/TIME: ${receiptData.dateTime}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(
            TextUnit(
                "AMOUNT: ${receiptData.amount}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(TextUnit(receiptData.cardPan, TextUnit.TextSize.NORMAL, Align.LEFT))
        template.add(
            TextUnit(
                "EXPIRY DATE: ${receiptData.expiryDate}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(
            TextUnit(
                "AUTHORIZATION CODE: ${receiptData.authCode}",
                TextUnit.TextSize.NORMAL,
                Align.LEFT
            )
        )
        template.add(TextUnit("RRN: ${receiptData.rrn}", TextUnit.TextSize.NORMAL, Align.LEFT))

        printerDev.addRuiImage(template.printBitmap, 0)
        printerDev.addRuiText(object : ArrayList<PrintItemObj?>() {
            init {
                add(PrintItemObj("\n"))
            }
        })
        printRunning = true
        printerDev.printRuiQueue(NxPrinterListener)
    }
}