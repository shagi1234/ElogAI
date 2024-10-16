package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import javax.inject.Inject

class UseCaseCreateDvir @Inject constructor(
    private val repositoryDvir: RepositoryDvir
) {
    private val useCaseHandleResponse: UseCaseHandleResponse<RequestCreateDvir, List<ResponseCreateDvir>> =
        UseCaseHandleResponse()

    suspend fun execute(requestCreateDvir: RequestCreateDvir): Resource<List<ResponseCreateDvir>> {
        return useCaseHandleResponse.handleResponse(requestCreateDvir) {
            repositoryDvir.createDvirRemote(it)
        }
    }
}