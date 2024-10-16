package com.selbiconsulting.elog.data.storage.remote.service

import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.data.storage.remote.KtorClient

interface ServiceFile {


    suspend fun getFileUrl(requestGetMessage: RequestGetMessage): ByteArray
    suspend fun uploadFile(requestUploadFile: RequestUploadFile): ResponseUploadFile
}