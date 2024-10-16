package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.entity.EntityLocation
import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.response.ResponseLocation


/*
 * Created by shagi on 26.03.2024 22:50
 */

interface RepositoryLocation {
    suspend fun postLocation(requestLocation: List<RequestLocation>): ResponseLocation

    suspend fun saveLocationLocal(entityLocation: EntityLocation)

    suspend fun getLocationLocal(): String

}