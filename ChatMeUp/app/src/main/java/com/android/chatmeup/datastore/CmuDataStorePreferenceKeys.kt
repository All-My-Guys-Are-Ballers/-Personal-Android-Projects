package com.android.chatmeup.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object CmuDataStorePreferenceKeys {
    const val CMU_DATASTORE_PREFERENCES = "chatmeup_datastore_prefs"

    val LOGIN_CREDENTIALS = stringPreferencesKey("login_credentials")

}