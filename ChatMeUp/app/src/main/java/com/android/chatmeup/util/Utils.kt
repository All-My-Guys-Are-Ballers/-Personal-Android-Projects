package com.android.chatmeup.util

import android.text.TextUtils

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