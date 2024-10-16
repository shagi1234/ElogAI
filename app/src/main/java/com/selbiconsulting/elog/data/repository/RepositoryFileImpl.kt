package com.selbiconsulting.elog.data.repository

import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.remote.service.ServiceFile
import com.selbiconsulting.elog.domain.repository.RepositoryFile
import kotlinx.serialization.serializer
import javax.inject.Inject

class RepositoryFileImpl @Inject constructor(
    val service: ServiceFile
) : RepositoryFile {
    override suspend fun uploadFile(requestUploadFile: RequestUploadFile): ResponseUploadFile {
        return service.uploadFile(requestUploadFile)

    }
}