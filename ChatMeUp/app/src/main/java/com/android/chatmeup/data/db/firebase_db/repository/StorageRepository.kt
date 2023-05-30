package com.android.chatmeup.data.db.firebase_db.repository

import android.net.Uri
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseStorageSource
import java.io.File

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

    fun downloadChatImage(chatID: String, file: File, b: (Result<Nothing>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.downloadChatImage(chatID = chatID, file = file).addOnSuccessListener {
            b.invoke(Result.Success())
        }.addOnFailureListener{
            b.invoke(Result.Error(it.message))
        }.addOnProgressListener {taskSnapshot ->
            b.invoke(
                Result.Progress(
                    (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                )
            )
        }
    }

}