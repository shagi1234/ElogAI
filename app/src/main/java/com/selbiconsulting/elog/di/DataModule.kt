package com.selbiconsulting.elog.di

import android.content.Context
import android.content.SharedPreferences
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.repository.RepositoryUserInfoImpl
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryUserInfo
import com.selbiconsulting.elog.ui.util.AlarmScheduler
import com.selbiconsulting.elog.ui.util.AlarmSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences {
        return applicationContext.getSharedPreferences(
            SharedPrefsKeys.KEY_APP,
            Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(@ApplicationContext applicationContext: Context): AlarmScheduler {
        return AlarmSchedulerImpl(applicationContext)
    }
}