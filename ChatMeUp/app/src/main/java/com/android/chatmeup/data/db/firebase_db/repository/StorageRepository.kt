package com.android.chatmeup.data.db.firebase_db.repository

import android.content.Context
import android.net.Uri
import com.android.chatmeup.data.Result
import com.android.chatmeup.data.db.firebase_db.remote.FirebaseStorageSource
import com.google.firebase.storage.StorageException
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

    fun downloadChatImage(chatID: String, timeStamp: String, file: File, b: (Result<Nothing>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.downloadChatImage(chatID = chatID, timeStamp = timeStamp, file = file).addOnSuccessListener {
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

    fun downloadProfileImage(userID: String, context:Context, path:String, b: (Result<Nothing>) -> Unit) {
        b.invoke(Result.Loading)
        firebaseStorageService.downloadProfileImage(userID = userID, context = context, path = path).addOnSuccessListener {
            b.invoke(Result.Success())
        }.addOnFailureListener{ exception ->
            if (exception is StorageException && exception.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND
            ) {
                //This is in a case where the profile does not have a profile image it should still return successful
                b.invoke(Result.Success())
                File(context.filesDir, path).delete()
                return@addOnFailureListener
            }
            b.invoke(Result.Error(exception.message))
        }.addOnProgressListener {taskSnapshot ->
            b.invoke(
                Result.Progress(
                    (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                )
            )
        }
    }

}