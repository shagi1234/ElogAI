package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.entity.EntityLocation
import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.response.ResponseLocation
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.remote.service.ServiceLocation
import com.selbiconsulting.elog.domain.repository.RepositoryLocation
import javax.inject.Inject


/*
 * Created by shagi on 26.03.2024 22:53
 */

class RepositoryLocationImpl @Inject constructor(
    private val serviceLocation: ServiceLocation,
    private val appDatabase: AppDatabase,
) : RepositoryLocation {
    override suspend fun postLocation(requestLocation: List<RequestLocation>): ResponseLocation {
        return serviceLocation.postLocation(requestLocation)
    }

    override suspend fun saveLocationLocal(entityLocation: EntityLocation) {
        appDatabase.locationDao().insert(entityLocation)
    }

    override suspend fun getLocationLocal(): String {

        return "a"
    }
}