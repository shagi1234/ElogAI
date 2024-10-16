package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
interface RepositoryDriver {
    suspend fun getDriverInfo(contactId: String): EntityDriver
    suspend fun getDriverById(id: String): EntityDriver?

    suspend fun upsert(entityDriver: EntityDriver): String
    suspend fun updateDriver(requestUpdateDriver: RequestUpdateDriver): String
}