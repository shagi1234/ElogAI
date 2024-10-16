package com.selbiconsulting.elog.ui.main.create_dvir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.entity.EntityDvir.Companion.toRequestCreateDvir
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import com.selbiconsulting.elog.domain.use_case.UseCaseCreateDvir
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLastLocation
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseSetVarToSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseUploadFile
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelCreateDvir @Inject constructor(
    private val repositoryDvir: RepositoryDvir,
    private val repositoryDriver: RepositoryDriver,
    private val sharedPreferences: SharedPreferencesHelper,
    private val useCaseCreateDvir: UseCaseCreateDvir,
    private val useCaseUploadFile: UseCaseUploadFile,
    private val useCaseGetLocation: UseCaseGetLastLocation,
    private val useCaseSetVarToSharedPrefs: UseCaseSetVarToSharedPrefs,
    private val useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs,
) : ViewModel() {
    val driverSignatureId
        get() = useCaseGetVarFromSharedPrefs.getVariable(
            SharedPrefsKeys.DRIVER_SIGNATURE_ID, ""
        )

    private val _driverInfo = SingleLiveEvent<EntityDriver>()
    val driverInfo get() = _driverInfo

    private val _createDvirState = SingleLiveEvent<Resource<List<ResponseCreateDvir>>>()
    val createDvirState get() = _createDvirState

    private val _driverSignatureFileState = SingleLiveEvent<Resource<ResponseUploadFile>>()
    val driverSignatureFileState get() = _driverSignatureFileState

    private val _mechanicSignatureFileState = SingleLiveEvent<Resource<ResponseUploadFile>>()
    val mechanicSignatureFileState get() = _mechanicSignatureFileState

    private val _dvirLocalId = SingleLiveEvent<Long>()
    val dvirLocalId get() = _dvirLocalId

    private val _locationName = SingleLiveEvent<String>()
    val locationName get() = _locationName


    fun createDvir(dvir: EntityDvir) {
        viewModelScope.launch(Dispatchers.IO) {
            dvir.localId = repositoryDvir.insertAndGetLocalId(dvir)
            _dvirLocalId.postValue(dvir.localId ?: 0)
            val requestCreateDvir = dvir.toRequestCreateDvir()
            requestCreateDvir.contactId = sharedPreferences.contactId
            createDvirRemote(requestCreateDvir = requestCreateDvir)
        }
    }

    fun getLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _locationName.postValue(useCaseGetLocation.execute())
        }
    }


    private fun createDvirRemote(requestCreateDvir: RequestCreateDvir) {
        viewModelScope.launch(Dispatchers.IO) {
            _createDvirState.postValue(Resource.Loading())
            _createDvirState.postValue(useCaseCreateDvir.execute(requestCreateDvir))
        }
    }

    fun uploadDriverSignature(requestUploadFile: RequestUploadFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _driverSignatureFileState.postValue(Resource.Loading())
            requestUploadFile.contactId = sharedPreferences.contactId
            _driverSignatureFileState.postValue(useCaseUploadFile.execute(requestUploadFile))
        }
    }

    fun uploadMechanicSignature(requestUploadFile: RequestUploadFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _mechanicSignatureFileState.postValue(Resource.Loading())
            requestUploadFile.contactId = sharedPreferences.contactId
            _mechanicSignatureFileState.postValue(useCaseUploadFile.execute(requestUploadFile))
        }
    }

    fun editDvir(dvir: EntityDvir) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDvir.updateDvir(dvir = dvir)
        }
    }

    fun getDriverInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            driverInfo.postValue(
                repositoryDriver.getDriverById(
                    id = sharedPreferences.contactId ?: ""
                )
            )
        }
    }

    fun deleteDvirByLocalId() {
        viewModelScope.launch(Dispatchers.IO) {
            _dvirLocalId.value?.let { repositoryDvir.deleteDvirByLocalId(it) }
        }
    }

    fun updateDvir(entityDvir: EntityDvir) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDvir.updateDvir(entityDvir)
        }
    }

    fun saveDriverSignatureIdToSharedPref(signatureId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            useCaseSetVarToSharedPrefs.setVariable(SharedPrefsKeys.DRIVER_SIGNATURE_ID, signatureId)
        }
    }


}