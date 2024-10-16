package com.selbiconsulting.elog.domain.repository

import androidx.room.Transaction
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import kotlinx.serialization.json.JsonElement


interface RepositoryDvir {
    fun getAllDvirLocal(): List<EntityDvir>
    suspend fun getAllDvir(contactId: String): List<RequestCreateDvir>
    suspend fun createDvirLocal(dvir: EntityDvir)
    suspend fun insertAndGetLocalId(dvir: EntityDvir): Long
    suspend fun createDvirRemote(requestCreateDvir: RequestCreateDvir): List<ResponseCreateDvir>
    suspend fun deleteDvir(dvir: EntityDvir)
    suspend fun deleteDvirByLocalId(localId: Long)
    suspend fun deleteDvirByRemoteId(requestCreateDvir: RequestCreateDvir): JsonElement
    suspend fun updateDvir(dvir: EntityDvir)
    suspend fun upsertDvirList(dvirList: List<EntityDvir>)



}