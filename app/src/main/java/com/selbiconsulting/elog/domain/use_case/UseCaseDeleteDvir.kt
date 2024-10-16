package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

class UseCaseDeleteDvir @Inject constructor(
    private val repositoryDvir: RepositoryDvir
) {
    private val useCaseHandleResponse =
        UseCaseHandleResponse<RequestCreateDvir, JsonElement>()

    suspend fun execute(request: RequestCreateDvir): Resource<JsonElement> {
        return useCaseHandleResponse.handleResponse(request) {
            repositoryDvir.deleteDvirByRemoteId(it)
        }
    }
}