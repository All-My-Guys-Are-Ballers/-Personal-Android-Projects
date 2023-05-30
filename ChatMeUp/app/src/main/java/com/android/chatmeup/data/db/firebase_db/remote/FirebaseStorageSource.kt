package com.android.chatmeup.data.db.firebase_db.remote

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Task based
class FirebaseStorageSource {
    companion object{
        private val storageInstance = FirebaseStorage.getInstance()
    }

    private fun refToPath(path: String): StorageReference {
        return storageInstance.reference.child(path)
    }
    fun uploadUserImage(userID: String, bArr: ByteArray): Task<Uri> {
        val path = "user_photos/$userID/profile_image"
        val ref = storageInstance.reference.child(path)

        return ref.putBytes(bArr).continueWithTask {
            ref.downloadUrl
        }
    }

    fun uploadChatImage(chatID: String, bArr: ByteArray): Task<Uri> {
        val path = "$chatID/${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
        val ref = storageInstance.reference.child(path)

        return ref.putBytes(bArr).continueWithTask {
            ref.downloadUrl
        }
    }

    fun downloadChatImage(chatID: String, file: File): FileDownloadTask {
        return refToPath(chatID).getFile(file)
    }
}