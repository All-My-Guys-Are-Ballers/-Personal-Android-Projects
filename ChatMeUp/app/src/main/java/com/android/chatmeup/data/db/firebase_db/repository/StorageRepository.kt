package com.android.chatmeup.data.db.firebase_db.repository

import android.net.Uri
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseStorageSource

class StorageRepository {
    private val firebaseStorageService = FirebaseStorageSource()

    fun updateUserProfileImage(userID: String, byteArray: ByteArray, b: (Result<Uri>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.uploadUserImage(userID, byteArray).addOnSuccessListener {
            b.invoke((Result.Success(it)))
        }.addOnFailureListener {
            b.invoke(Result.Error(it.message))
        }
    }

    fun uploadChatImage(chatID: String, byteArray: ByteArray, b: (Result<Uri>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.uploadChatImage(chatID, byteArray).addOnSuccessListener {
            b.invoke((Result.Success(it)))
        }.addOnFailureListener {
            b.invoke(Result.Error(it.message))
        }
    }
}