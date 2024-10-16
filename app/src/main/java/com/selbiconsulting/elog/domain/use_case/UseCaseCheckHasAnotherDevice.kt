package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.common.ResponseMessage
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.domain.repository.RepositoryLogin
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.client.features.ResponseException
import java.io.IOException
import javax.inject.Inject

class UseCaseCheckHasAnotherDevice @Inject constructor(
   private val repositoryLogin: RepositoryLogin
) {
    suspend fun execute(requestLogin: RequestLogin): Resource<ResponseLogin> {
        return try {
            val result = repositoryLogin.hasAnotherDevice(requestLogin)

                Resource.Success(data = result)

//            if (result.accessToken.isNullOrEmpty() || result.tokenType.isNullOrEmpty())
//                Resource.Error(ResponseMessage(intMessage = R.string.wrong_username_or_password))
//            else
//                Resource.Success(data = result)
        } catch (ex: ResponseException) {
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