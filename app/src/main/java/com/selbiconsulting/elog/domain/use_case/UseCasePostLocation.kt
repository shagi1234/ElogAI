package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.response.ResponseLocation
import com.selbiconsulting.elog.domain.repository.RepositoryLocation
import javax.inject.Inject

class UseCasePostLocation @Inject constructor(
    private val repositoryLocation: RepositoryLocation
) {
    private val handleResponse: UseCaseHandleResponse<List<RequestLocation>, ResponseLocation> =
        UseCaseHandleResponse()

    suspend fun execute(location: List<RequestLocation>): Resource<ResponseLocation> {
        return handleResponse.handleResponse(location) {
            repositoryLocation.postLocation(it)
        }
    }
}