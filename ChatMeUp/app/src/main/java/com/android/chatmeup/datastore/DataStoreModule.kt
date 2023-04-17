package com.android.chatmeup.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(CmuDataStorePreferenceKeys.CMU_DATASTORE_PREFERENCES)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providesPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideCmuDataStoreRepository(dataStore: DataStore<Preferences>): CmuDataStoreRepository {
        return CmuDataStoreRepositoryImpl(dataStore)
    }

//    @Binds
//    @Singleton
//    abstract fun bindsCmuDataStoreRepository(impl: CmuDataStoreRepositoryImpl): CmuDataStoreRepositoryImpl
}