package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.service.ServiceDvir
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import io.ktor.utils.io.concurrent.shared
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class RepositoryDvirImpl @Inject constructor(
    val appDatabase: AppDatabase,
    val service: ServiceDvir,
    val sharedPreferencesHelper: SharedPreferencesHelper
) : RepositoryDvir {
    override fun getAllDvirLocal(): List<EntityDvir> {
        return appDatabase.dvirDao().getAllDvir()
    }

    override suspend fun getAllDvir(contactId: String): List<RequestCreateDvir> {
        return service.getAllDvir(contactId, sharedPreferencesHelper.deviceID.toString())
    }

    override suspend fun createDvirLocal(dvir: EntityDvir) {
        appDatabase.dvirDao().insert(dvir)
    }

    override suspend fun insertAndGetLocalId(dvir: EntityDvir): Long {
        return appDatabase.dvirDao().insert(dvir)
    }

    override suspend fun createDvirRemote(requestCreateDvir: RequestCreateDvir): List<ResponseCreateDvir> {
        return service.createDvir(requestCreateDvir)
    }

    override suspend fun deleteDvir(dvir: EntityDvir) {
        appDatabase.dvirDao().delete(dvir)
    }

    override suspend fun deleteDvirByLocalId(localId: Long) {
        appDatabase.dvirDao().deleteDvirByLocalId(localId)
    }

    override suspend fun deleteDvirByRemoteId(requestCreateDvir: RequestCreateDvir): JsonElement {
        return service.deleteDvir(requestCreateDvir)
    }

    override suspend fun updateDvir(dvir: EntityDvir) {
        appDatabase.dvirDao().update(dvir)
    }

    override suspend fun upsertDvirList(dvirList: List<EntityDvir>) {
        for (dvir in dvirList) {
            val existingDvir = appDatabase.dvirDao().getDvirByRemoteId(dvir.id ?: "")

            if (existingDvir != null)
                dvir.localId = existingDvir.localId

            appDatabase.dvirDao().insertOrUpdate(dvir)
        }
    }
}