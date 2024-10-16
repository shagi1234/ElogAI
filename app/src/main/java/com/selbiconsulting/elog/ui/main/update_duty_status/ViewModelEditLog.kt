package com.selbiconsulting.elog.ui.main.update_duty_status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.enums.getShortDutyStatus
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLastLocation
import com.selbiconsulting.elog.domain.use_case.UseCaseSendLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toEntityLog
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelEditLog @Inject constructor(
    private val useCaseSendLogs: UseCaseSendLogs,
    private val repositoryLogs: RepositoryLogs,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val useCaseGetLastLocation: UseCaseGetLastLocation,
) : ViewModel() {

    private val _updateState = SingleLiveEvent<Resource<List<ResponseSendLogs>>>()
    val updateState get() = _updateState

    var dataLog: DataLog = DataLog()


    private val _location = MutableStateFlow("N/A")
    val location get() = _location

    fun updateLog() {
        val request = RequestSendLogs(
            localId = dataLog.localId.toString(),
            logId = dataLog.id ?: "",
            signatureId = dataLog.signatureId ?: "",
            isVerified = false,
            vehicle = dataLog.vehicle,
            contactId = sharedPreferencesHelper.contactId ?: "",
            status = getShortDutyStatus(dataLog.statusShort),
            document = dataLog.document ?: "",
            startTime = dataLog.timeStart ?: "",
            endTime = dataLog.endTime ?: "",
            trailer = dataLog.trailer ?: "",
            note = dataLog.note ?: "test",
            odometer = dataLog.odometer,
            engineHours = dataLog.engineHours,
            createdDate = dataLog.createdAt ?: "",
            location = dataLog.location ?: "",
            isDrivingTimeResetLog = dataLog.isDrivingTimeResetLog.toBoolean(),
            violationTypes = dataLog.violationTypes ?: "",
        )

        viewModelScope.launch {
            _updateState.value = useCaseSendLogs.execute(mutableListOf(request))
        }
    }

    fun updateLocalLog() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryLogs.updateLog(dataLog.toEntityLog())
        }
    }

    fun getLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _location.emit(useCaseGetLastLocation.execute())
        }
    }

    fun clearLocation() {
        viewModelScope.launch {
            _location.emit("N/A")
        }
    }
}