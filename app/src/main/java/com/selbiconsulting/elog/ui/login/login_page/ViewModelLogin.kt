package com.selbiconsulting.elog.ui.login.login_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseCheckHasAnotherDevice
import com.selbiconsulting.elog.domain.use_case.UseCaseLogin
import com.selbiconsulting.elog.domain.use_case.UseCaseSaveUserInfo
import com.selbiconsulting.elog.domain.use_case.UseCaseSetVarToSharedPrefs
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelLogin @Inject constructor(
    private val useCaseLogin: UseCaseLogin,
    private val useCaseCheckHasAnotherDevice: UseCaseCheckHasAnotherDevice,
    private val useCaseUserInfo: UseCaseSaveUserInfo,
    private val useCaseSetVarToSharedPrefs: UseCaseSetVarToSharedPrefs,
    private val useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs,
) : ViewModel() {
    private val _loginState: SingleLiveEvent<Resource<ResponseLogin>> by lazy { SingleLiveEvent() }
    val loginState: SingleLiveEvent<Resource<ResponseLogin>> get() = _loginState
    private val _handleLoginState = SingleLiveEvent<Resource<ResponseLogin>>()
    val handleLoginState get() = _handleLoginState

    var requestLogin: RequestLogin = RequestLogin("", "")


    val hasLeftTruck: MutableLiveData<Boolean> = MutableLiveData(
        useCaseGetVarFromSharedPrefs.getVariable(SharedPrefsKeys.LEAVE_TRUCK, false)
    )

    fun checkHAsAnotherDevice(requestLogin: RequestLogin) {
        this.requestLogin = requestLogin
        viewModelScope.launch {
            _handleLoginState.postValue(Resource.Loading())

            _handleLoginState.postValue(useCaseCheckHasAnotherDevice.execute(requestLogin))
        }
    }

    fun login() {
        viewModelScope.launch(Dispatchers.IO) {
            _loginState.postValue(Resource.Loading())
            val result = useCaseLogin.execute(requestLogin)
            _loginState.postValue(result)
        }
    }

    fun saveUserInfo(responseLogin: ResponseLogin) {
        useCaseUserInfo.saveUserInfo(responseLogin, requestLogin)

    }

    fun setHasLeftTruck(hasLeftTruck: Boolean) {
        useCaseSetVarToSharedPrefs.setVariable(SharedPrefsKeys.LEAVE_TRUCK, hasLeftTruck)
    }
}