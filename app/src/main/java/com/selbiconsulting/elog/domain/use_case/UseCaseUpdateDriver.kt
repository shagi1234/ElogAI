package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import javax.inject.Inject

class UseCaseUpdateDriver @Inject constructor(
    private val repositoryDriver: RepositoryDriver
) {
    private val useCaseHandleResponse = UseCaseHandleResponse<RequestUpdateDriver, String>()
    suspend fun execute(requestUpdateDriver: RequestUpdateDriver): Resource<String> {
        return useCaseHandleResponse.handleResponse(requestUpdateDriver) {
            repositoryDriver.updateDriver(it)
        }
    }

}