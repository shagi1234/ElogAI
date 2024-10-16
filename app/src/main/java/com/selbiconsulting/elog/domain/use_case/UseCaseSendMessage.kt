package com.selbiconsulting.elog.domain.use_case

import android.util.Log
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.common.ResponseMessage
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.model.response.ResponseGetMessage
import com.selbiconsulting.elog.data.model.response.ResponseSendMessage
import com.selbiconsulting.elog.domain.repository.RepositoryMessage
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.client.features.ResponseException
import java.io.IOException
import javax.inject.Inject


class UseCaseSendMessage @Inject constructor(private val repositoryMessage: RepositoryMessage) {
    suspend fun execute(requestSendMessage: RequestSendMessage): Resource<ResponseSendMessage> {
        return  try {

            val result = repositoryMessage.sendMessageToServer(requestSendMessage)
             Resource.Success(result)
        }
        catch (ex: ResponseException) {
            println("Error: ${ex.response.status.value} ${ex.response.status.description}")
            Resource.Error(ResponseMessage(intMessage = R.string.something_went_wrong))
        } catch (e: IOException) {
            println("Error: ${e.message}")
            Resource.Failure(ResponseMessage(intMessage = R.string.check_internet_connection))
        } catch (e: HttpRequestTimeoutException) {
            println("Error: ${e.message}")
            Resource.Failure(ResponseMessage(intMessage = R.string.check_internet_connection))
        }

    }
}