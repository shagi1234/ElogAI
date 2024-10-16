package com.selbiconsulting.elog.domain.repository

import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile

interface RepositoryFile {
    suspend fun uploadFile(requestUploadFile: RequestUploadFile): ResponseUploadFile
}