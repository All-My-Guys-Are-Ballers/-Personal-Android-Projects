package com.android.chatmeup.data


sealed class Result<out R> {
    data class Success<out T>(val data: T? = null, val msg: String? = null) : Result<T>()
    data class Changed<out T>(val data: T? = null, val msg: String? = null) : Result<T>()
    class Error(val msg: String? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
