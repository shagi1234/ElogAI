package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDriver
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import javax.inject.Inject
import javax.inject.Provider

class RepositoryDriverImpl @Inject constructor(
    val appDatabase: AppDatabase,
    private val serviceDriver : ServiceDriver,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
) : RepositoryDriver {


    override suspend fun getDriverInfo(contactId: String): EntityDriver {
        return serviceDriver.getDriverInfo(contactId, sharedPreferencesHelper.deviceID.toString())
    }

    override suspend fun getDriverById(id: String): EntityDriver? {
        return appDatabase.driverDao().getDriverById(id)
    }

    override suspend fun upsert(entityDriver: EntityDriver): String {
        return appDatabase.driverDao().upsertDriver(entityDriver)
    }

    override suspend fun updateDriver(requestUpdateDriver: RequestUpdateDriver): String {
        return serviceDriver.updateDriver(requestUpdateDriver)
    }

}