package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import kotlinx.serialization.Serializable
import java.io.Serial
import javax.inject.Inject

class UseCaseCertifyLogs @Inject constructor(
    private val repositoryLogs: RepositoryLogs
) {
    private val useCaseHandleResponse: UseCaseHandleResponse<RequestCertifyLogs, ResponseCertifyLogs> =
        UseCaseHandleResponse()

    suspend fun execute(requestCreateDvir: RequestCertifyLogs): Resource<ResponseCertifyLogs> {
        return useCaseHandleResponse.handleResponse(requestCreateDvir) {
            repositoryLogs.certifyDailyLogsServer(it)
        }
    }

}

@Serializable
data class RequestCertifyLogs(
    val contactId: String,
    val contentId: String,
    val dates: List<DateEntry>
)
@Serializable
data class DateEntry(
    val startDatetime: String,
    val endDatetime: String
)
@Serializable
data class ResponseCertifyLogs(
    val message: String
)
