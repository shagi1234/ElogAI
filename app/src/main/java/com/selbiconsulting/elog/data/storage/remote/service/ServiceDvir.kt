package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.KtorClient
import kotlinx.serialization.json.JsonElement

interface ServiceDvir {


    suspend fun createDvir(requestCreateDvir: RequestCreateDvir):List<ResponseCreateDvir>
    suspend fun getAllDvir(contactId:String, deviceId:String):List<RequestCreateDvir>
    suspend fun deleteDvir(requestCreateDvir: RequestCreateDvir): JsonElement
}