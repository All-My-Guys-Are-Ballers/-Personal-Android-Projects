package com.android.chatmeup

import android.app.Activity
import android.app.Application
import com.android.chatmeup.util.SharedPreferencesUtil
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CmuApplication: Application() {
    private val tag: String = "ChatMeUp Application"

    private var mCurrentActivity: Activity? = null

    override fun onCreate() {

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("Application Started")

        super.onCreate()
    }

    fun getCurrentActivity(): Activity? {
        return mCurrentActivity
    }

    fun setCurrentActivity(mCurrentActivity: Activity?) {
        this.mCurrentActivity = mCurrentActivity
    }

    var myUserID: String = ""
        get() {
            field = SharedPreferencesUtil.getUserID(this.applicationContext).orEmpty()
            return field
        }
        private set

    fun setUserId (){

    }
}