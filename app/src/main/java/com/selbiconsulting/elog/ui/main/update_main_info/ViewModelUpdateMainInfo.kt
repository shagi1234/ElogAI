package com.selbiconsulting.elog.ui.main.update_main_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.use_case.UseCaseUpdateDriver
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelUpdateMainInfo @Inject constructor(
    private val useCaseUpdateDriver: UseCaseUpdateDriver,
) : ViewModel() {
    private val _driverInfo = SingleLiveEvent<Resource<String>>()
    val driverInfo get() = _driverInfo

    fun updateDriverInfo(requestUpdateDriver: RequestUpdateDriver) {
        _driverInfo.postValue(Resource.Loading())
        viewModelScope.launch(Dispatchers.IO) {
            _driverInfo.postValue(useCaseUpdateDriver.execute(requestUpdateDriver))
        }
    }
}