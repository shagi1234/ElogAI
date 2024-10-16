package com.selbiconsulting.elog.domain.use_case

import android.util.Log
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.common.ResponseMessage
import com.selbiconsulting.elog.data.model.request.RequestLocation
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.client.features.ResponseException
import java.io.IOException
import javax.inject.Inject


/*
 * Created by shagi on 23.03.2024 22:46
 */

class UseCaseHandleResponse<T, Q>   {
    suspend fun handleResponse(
        request: T,
        apiCall: suspend (T) -> Q
    ): Resource<Q> {
        return try {
            val result = apiCall.invoke(request)
            Resource.Success(data = result)
        } catch (ex: ResponseException) {
            val statusCode = ex.response.status.value

//            401 - unauthorized
//            428 - logout

            println("Error: $statusCode ${ex.response.status.description}")
            Resource.Error(responseMessage = ResponseMessage(stringMessage = ex.message), statusCode = statusCode)
        } catch (e: IOException) {
            println("Error: ${e.message}")
            Resource.Failure(ResponseMessage(intMessage = R.string.check_internet_connection))
        } catch (e: HttpRequestTimeoutException) {
            println("Error: ${e.message}")
            Resource.Failure(ResponseMessage(intMessage = R.string.check_internet_connection))
        }
    }
}