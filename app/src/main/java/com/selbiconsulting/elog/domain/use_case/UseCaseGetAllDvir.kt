package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import javax.inject.Inject

class UseCaseGetAllDvir @Inject constructor(
    private val repositoryDvir: RepositoryDvir
) {
    private val useCaseHandleResponse = UseCaseHandleResponse<String, List<RequestCreateDvir>>()

    suspend fun execute(contactId: String): Resource<List<RequestCreateDvir>> {
        return useCaseHandleResponse.handleResponse(contactId) {
            repositoryDvir.getAllDvir(it)
        }
    }
}