package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.data.model.common.Resource
import javax.inject.Inject

class UseCaseCheckOtp @Inject constructor() {
    fun execute(otpCode: String): Resource<Boolean> {
        return if (otpCode == "123456")
            Resource.Success(data = true)
        else Resource.Error()
    }
}