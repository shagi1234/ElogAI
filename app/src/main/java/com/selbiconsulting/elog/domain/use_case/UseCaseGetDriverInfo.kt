package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import javax.inject.Inject
import javax.inject.Provider

class UseCaseGetDriverInfo @Inject constructor(
    private val repositoryDriverProvider: Provider<RepositoryDriver>
) {

    private val repositoryDriver = repositoryDriverProvider.get()
    private val handleResponse: UseCaseHandleResponse<String, EntityDriver> =
        UseCaseHandleResponse()

    suspend fun execute(contactId: String): Resource<EntityDriver> {
        return handleResponse.handleResponse(contactId) {
            repositoryDriver.getDriverInfo(it)
        }
    }
}