package com.selbiconsulting.elog.ui.login.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.domain.use_case.UseCaseCheckOtp
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelOtpVerification @Inject constructor(
    private val useCaseCheckOtp: UseCaseCheckOtp
) : ViewModel() {
    private val _checkingState: SingleLiveEvent<Resource<Boolean>> by lazy { SingleLiveEvent() }

    val checkingState: SingleLiveEvent<Resource<Boolean>> get() = _checkingState
    fun checkOtp(otpCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            checkingState.postValue(Resource.Loading())
            delay(500)
            val result = useCaseCheckOtp.execute(otpCode)
            _checkingState.postValue(result)
        }
    }
}