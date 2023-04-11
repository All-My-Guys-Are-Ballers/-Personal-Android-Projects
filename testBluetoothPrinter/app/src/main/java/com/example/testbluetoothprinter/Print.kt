package com.example.testbluetoothprinter

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.core.content.res.ResourcesCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.textparser.PrinterTextParserImg

class Print(
    private val context: Context
) {
    fun print() {
        val printer = EscPosPrinter(
            BluetoothPrintersConnections.selectFirstPaired(),
            203,
            48f,
            32
        )

        val formattedText = createFormattedText(printer = printer)

        printer.printFormattedText(formattedText,100)
    }

    private fun createFormattedText(printer: EscPosPrinter): String {
        return "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(
            printer,
            ResourcesCompat.getDrawable(context.resources, R.drawable.ic_nomba_logo,
                null
            )
        )+"</img>\n" +
                "[L]\n" +
                "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                "[L]\n" +
                "[C]================================\n" +
                "[L]\n" +
                "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                "[L]  + Size : S\n" +
                "[L]\n" +
                "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                "[L]  + Size : 57/58\n" +
                "[L]\n" +
                "[C]--------------------------------\n" +
                "[L]TOTAL PRICE :[R]34.98e\n" +
                "[R]TAX :[R]4.23e\n" +
                "[L]\n" +
                "[C]================================\n" +
                "[L]\n" +
                "[L]<font size='tall'>Customer :</font>\n" +
                "[L]Raymond DUPONT\n" +
                "[L]5 rue des girafes\n" +
                "[L]31547 PERPETES\n" +
                "[L]Tel : +33801201456\n" +
                "[L]\n" +
                "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"

    }
}