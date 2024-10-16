package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.domain.repository.RepositoryFile
import javax.inject.Inject

class UseCaseUploadFile @Inject constructor(val repository: RepositoryFile) {

    private val useCaseHandleResponse: UseCaseHandleResponse<RequestUploadFile, ResponseUploadFile> =
        UseCaseHandleResponse()

    suspend fun execute(requestUploadFile: RequestUploadFile): Resource<ResponseUploadFile> {
        return useCaseHandleResponse.handleResponse(requestUploadFile) {
            repository.uploadFile(it)
        }
    }

}