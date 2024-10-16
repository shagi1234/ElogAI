package com.selbiconsulting.elog.di

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.selbiconsulting.elog.data.repository.RepositoryDriverImpl
import com.selbiconsulting.elog.data.repository.RepositoryDvirImpl
import com.selbiconsulting.elog.data.repository.RepositoryFileImpl
import com.selbiconsulting.elog.data.repository.RepositoryLocalVariablesImpl
import com.selbiconsulting.elog.data.repository.RepositoryLocationImpl
import com.selbiconsulting.elog.data.repository.RepositoryLoginImp
import com.selbiconsulting.elog.data.repository.RepositoryLogsImpl
import com.selbiconsulting.elog.data.repository.RepositoryMessageImpl
import com.selbiconsulting.elog.data.repository.RepositoryUserInfoImpl
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.KtorClientProvider
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDvir
import com.selbiconsulting.elog.data.storage.remote.service.ServiceMessage
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDriver
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDriverImpl
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDvirImpl
import com.selbiconsulting.elog.data.storage.remote.service.ServiceFile
import com.selbiconsulting.elog.data.storage.remote.service.ServiceFileImpl
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLocation
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLocationImpl
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLogs
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLogsImpl
import com.selbiconsulting.elog.data.storage.remote.service.ServiceMessageImpl
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import com.selbiconsulting.elog.domain.repository.RepositoryFile
import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import com.selbiconsulting.elog.domain.repository.RepositoryLocation
import com.selbiconsulting.elog.domain.repository.RepositoryLogin
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import com.selbiconsulting.elog.domain.repository.RepositoryUserInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.utils.io.concurrent.shared
import java.util.Locale
import javax.inject.Provider
import javax.inject.Singleton

/*
 * Created by shagi on 23.03.2024 22:42
 */
@Module
@InstallIn(SingletonComponent::class)
class DomainModule {
    @Provides
    @Singleton
    fun provideRepositoryLogin(): RepositoryLogin {
        return RepositoryLoginImp()
    }

    @Provides
    @Singleton
    fun provideRepositoryUserInfo(sharedPreferencesHelper: SharedPreferencesHelper): RepositoryUserInfo {
        return RepositoryUserInfoImpl(sharedPreferencesHelper)
    }

    @Provides
    @Singleton
    fun provideRepositoryLocation(
        serviceLocation: ServiceLocation, appDatabase: AppDatabase
    ): RepositoryLocation {
        return RepositoryLocationImpl(serviceLocation, appDatabase)
    }

    @Provides
    @Singleton
    fun provideRepositoryDriver(
        appDatabase: AppDatabase,
        serviceDriver: ServiceDriver,
        sharedPreferencesHelper: SharedPreferencesHelper
    ): RepositoryDriver {
        return RepositoryDriverImpl(appDatabase, serviceDriver, sharedPreferencesHelper)
    }


    @Provides
    @Singleton
    fun provideRepositoryLocalVariables(
        sharedPreferencesHelper: SharedPreferencesHelper, sharedPreferences: SharedPreferences
    ): RepositoryLocalVariables {
        return RepositoryLocalVariablesImpl(sharedPreferencesHelper, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideRepositoryLogs(
        appDatabase: AppDatabase,
        serviceLogs: ServiceLogs,
        sharedPreferencesHelper: SharedPreferencesHelper
    ): RepositoryLogs {
        return RepositoryLogsImpl(appDatabase, serviceLogs, sharedPreferencesHelper)
    }

    @Provides
    @Singleton
    fun provideLocationService(sharedPreferencesHelper: SharedPreferencesHelper): ServiceLocation {
        val serviceLocation = ServiceLocationImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceLocation.refreshClient() }
        return serviceLocation
    }

    @Provides
    @Singleton
    fun provideServiceLogs(sharedPreferencesHelper: SharedPreferencesHelper): ServiceLogs {
        val serviceLogs = ServiceLogsImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceLogs.refreshClient() }
        return serviceLogs
    }

    @Provides
    @Singleton
    fun provideMessageService(sharedPreferencesHelper: SharedPreferencesHelper): ServiceMessage {
        val serviceMessages = ServiceMessageImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceMessages.refreshClient() }
        return serviceMessages
    }


    @Provides
    @Singleton
    fun provideServiceDvir(sharedPreferencesHelper: SharedPreferencesHelper): ServiceDvir {
        val serviceDvir = ServiceDvirImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceDvir.refreshClient() }
        return serviceDvir
    }

    @Provides
    @Singleton
    fun provideServiceFile(sharedPreferencesHelper: SharedPreferencesHelper): ServiceFile {
        val serviceFile = ServiceFileImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceFile.refreshClient() }
        return serviceFile
    }

//    @Provides
//    @Singleton
//    fun provideServiceDriver(sharedPreferencesHelper: SharedPreferencesHelper): ServiceDriver {
//        return ServiceDriver.create { sharedPreferencesHelper.token ?: "" }
//    }

    @Provides
    @Singleton
    fun provideServiceDriver(sharedPreferencesHelper: SharedPreferencesHelper): ServiceDriver {
        val serviceDriver = ServiceDriverImpl { sharedPreferencesHelper.token ?: "" }
        sharedPreferencesHelper.addTokenUpdateListener { serviceDriver.refreshClient() }
        return serviceDriver
    }

    @Provides
    @Singleton
    fun provideRepositoryMessages(
        service: ServiceMessage,
        appDatabase: AppDatabase,
        sharedPreferencesHelper: SharedPreferencesHelper
    ): RepositoryMessage {
        return RepositoryMessageImpl(
            service, appDatabase,
            sharedPreferencesHelper
        )
    }

    @Provides
    @Singleton
    fun provideRepositoryDvir(
        appDatabase: AppDatabase,
        serviceDvir: ServiceDvir,
        sharedPreferencesHelper: SharedPreferencesHelper
    ): RepositoryDvir {
        return RepositoryDvirImpl(appDatabase, serviceDvir, sharedPreferencesHelper)
    }

    @Provides
    @Singleton
    fun provideRepositoryFile(
        serviceFile: ServiceFile
    ): RepositoryFile {
        return RepositoryFileImpl(serviceFile)
    }


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "ElogAI"
        ).build()
    }


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferencesHelper {
        return SharedPreferencesHelper(context)
    }


    @Provides
    @Singleton
    fun provideUiModeManage(@ApplicationContext context: Context): UiModeManager {
        return context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    }


    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
        return Geocoder(context, Locale.getDefault())
    }

}