package com.android.chatmeup.util

import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun isStrongPassword(password: String): Boolean {
    val minLength = 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    return password.length >= minLength && hasUppercase && hasLowercase && hasDigit && hasSpecialChar
}

fun isName(s: String): Int {
    return if (!s.all { it.isLetter() }) {  // check if string contains only letters
        -1
    } else if (s.length < 2) {  // check if string is too short to be a name
        -2
    } else 0
}

@RequiresApi(Build.VERSION_CODES.O)
fun epochToHoursAndMinutes(epoch: Long): String {
    val date =
        Date(epoch)
    val formatter = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )

    return formatter.format(date)
}