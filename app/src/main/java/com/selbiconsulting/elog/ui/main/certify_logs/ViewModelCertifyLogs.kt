package com.selbiconsulting.elog.ui.main.certify_logs

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.domain.use_case.ResponseCertifyLogs
import com.selbiconsulting.elog.domain.use_case.UseCaseCertifyLogs
import com.selbiconsulting.elog.domain.use_case.UseCaseSetVarToSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseUploadFile
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class ViewModelCertifyLogs @Inject constructor(
    private var useCaseCertifyLogs: UseCaseCertifyLogs,
    private val useCaseUploadFile: UseCaseUploadFile,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val useCaseSetVarToSharedPrefs: UseCaseSetVarToSharedPrefs,
    private val repositoryDriver: RepositoryDriver,
    private var repositoryLogs: RepositoryLogs
) : ViewModel() {

    private val _driverSignatureFileState = SingleLiveEvent<Resource<ResponseUploadFile>>()
    val driverSignatureFileState get() = _driverSignatureFileState

    private val _certifyLogsState = SingleLiveEvent<Resource<ResponseCertifyLogs>>()
    val certifyLogsState get() = _certifyLogsState


   private val _driverData: MutableStateFlow<EntityDriver?> = MutableStateFlow(null)
    val driverData get() = _driverData





    fun uploadDriverSignature(requestUploadFile: RequestUploadFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _driverSignatureFileState.postValue(Resource.Loading())
            requestUploadFile.contactId = sharedPreferencesHelper.contactId
            _driverSignatureFileState.postValue(useCaseUploadFile.execute(requestUploadFile))
        }
    }
    fun certifyLogs(requestCertifyLogs: RequestCertifyLogs,date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = useCaseCertifyLogs.execute(requestCertifyLogs)) {
                is Resource.Error -> {

                }
                is Resource.Failure -> {}
                is Resource.Loading -> {}
                is Resource.Success -> {
                    repositoryLogs.certifyDailyLogs(date)
                }
            }

        }
    }

    fun saveDriverSignatureIdToSharedPref(signatureId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            useCaseSetVarToSharedPrefs.setVariable(SharedPrefsKeys.DRIVER_SIGNATURE_ID, signatureId)
        }
    }

    fun getDriverDataLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            _driverData.emit(repositoryDriver.getDriverById(sharedPreferencesHelper.contactId ?: ""))
        }
    }
}