package com.example.mp35ptest

import android.os.RemoteException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InsertCartTestScreen(){
    val rowItems:MutableList<String> = mutableListOf(
        "Open",
        "Reset",
        "Check Card",
        "Send APDU",
        "Disconnect",
        "Close"
    )

    val iccard = DeviceServiceManager.iCCardReader
    val ctx = LocalContext.current

    Surface() {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)){
            items(rowItems){
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = {
                    when(it){
                        "Open" -> try {
                            val flag: Boolean = iccard?.open() ?: false
                            if (flag) {
                                Toast.makeText(ctx, "Card Opened Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Card Open Failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: RemoteException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        "Reset" -> try {
                            val data = iccard!!.reset(0x00)
                            if (null != data && data.isNotEmpty()) {
                                Toast.makeText(ctx, "Card Reset Result: ${HexUtil.bcd2str(data)}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Card Reset Failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: RemoteException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        "Check Card" -> try {
                            val flag = iccard!!.isExist
                            if (flag) {
                                Toast.makeText(ctx, "Card Exists", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Card does not exists", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: RemoteException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        "Send APDU" -> {
                            val apdu = HexUtil
                                .hexStringToByte("0000000000")
                            try {
                                val data = iccard!!.apduComm(apdu)
                                if (null != data) {
                                    Toast.makeText(ctx, "Main menu result: ${HexUtil.bcd2str(data)}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(ctx, "Failed to send APDU command", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: RemoteException) {
                                // TODO Auto-generated catch block
                                e.printStackTrace()
                            }
                        }
                        "Disconnect" -> try {
                            val ret = iccard!!.halt()
                            if (ret == 0x00) {
                                Toast.makeText(ctx, "Card disconnected successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Card failed to disconnect", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: RemoteException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }
                        "Close" -> try {
                            val flag = iccard!!.close()
                            if (flag) {
                                Toast.makeText(ctx, "Card closed successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(ctx, "Card failed to close", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: RemoteException) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        }

                }}) {
                    Text(text = it, fontSize = 18.sp)
                }
            }
        }
    }
}
