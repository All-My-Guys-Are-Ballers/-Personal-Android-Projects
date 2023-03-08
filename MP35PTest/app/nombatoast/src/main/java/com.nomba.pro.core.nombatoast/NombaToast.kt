/*
 * Copyright (c) 2022. Nomba Financial Services
 *
 * author: Victor Shoaga
 * email: victor.shoaga@nomba.com
 * github: @inventvictor
 *
 */
package com.nomba.pro.core.nombatoast

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import com.nomba.pro.core.nombatoast.R

enum class NombaToastDuration {
    SHORT,
    LONG
}

enum class NombaToastStyle {
    INFO,
    WARNING,
    ERROR
}

class NombaToast {
    companion object {

        fun createFancyToast(
            context: Context,
            activity: Activity?,
            title: String,
            message: String,
            style: NombaToastStyle,
            duration: NombaToastDuration,
        ){
            if (activity != null) {
                MotionToast.createColorToast(
                    activity,
                    title,
                    message,

                    when (style) {
                        NombaToastStyle.INFO -> MotionToastStyle.INFO
                        NombaToastStyle.WARNING -> MotionToastStyle.WARNING
                        else -> MotionToastStyle.ERROR
                    },

                    MotionToast.GRAVITY_BOTTOM,

                    if (duration == NombaToastDuration.SHORT) MotionToast.SHORT_DURATION
                    else MotionToast.LONG_DURATION,

                    ResourcesCompat.getFont(activity, R.font.inter_regular)
                )
            } else {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}