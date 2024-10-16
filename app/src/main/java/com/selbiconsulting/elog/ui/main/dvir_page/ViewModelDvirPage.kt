package com.selbiconsulting.elog.ui.main.dvir_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.entity.EntityDvir.Companion.toRequestCreateDvir
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryDvir
import com.selbiconsulting.elog.domain.use_case.UseCaseDeleteDvir
import com.selbiconsulting.elog.domain.use_case.UseCaseGetAllDvir
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLastLocation
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

@HiltViewModel
class ViewModelDvirPage @Inject constructor(
    private val repositoryDvir: RepositoryDvir,
    private val useCaseDeleteDvir: UseCaseDeleteDvir,
    private val useCaseGetAllDvir: UseCaseGetAllDvir,
    private val useCaseGetLastLocation: UseCaseGetLastLocation,
    private val sharedPreferences: SharedPreferencesHelper,
) : ViewModel() {
    private val _dvirList = SingleLiveEvent<List<EntityDvir>>()
    val dvirList get() = _dvirList

    private val _deleteDvirState = SingleLiveEvent<Resource<JsonElement>>()
    val deleteDvirState get() = _deleteDvirState

    private val _getAllDvirState =
        MutableStateFlow<Resource<List<RequestCreateDvir>>>(Resource.Loading())
    val getAllDvirState get() = _getAllDvirState

    private val _locationName = SingleLiveEvent<String>()
    val locationNAme get() = _locationName

    private val currentDvir = SingleLiveEvent<EntityDvir>()

    fun getAllDvirLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            _dvirList.postValue(repositoryDvir.getAllDvirLocal())
        }
    }

    fun upsertDvirList(dvirList: List<EntityDvir>) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDvir.upsertDvirList(dvirList)
            _dvirList.postValue(repositoryDvir.getAllDvirLocal())
        }
    }

    fun getAllDvir() {
        if (_getAllDvirState.value !is Resource.Loading) return
        viewModelScope.launch(Dispatchers.IO) {
            _getAllDvirState.emit(useCaseGetAllDvir.execute(sharedPreferences.contactId ?: ""))
        }
    }

    fun deleteDvir() {
        viewModelScope.launch(Dispatchers.IO) {
            currentDvir.value?.let { repositoryDvir.deleteDvir(dvir = it) }
            _dvirList.postValue(repositoryDvir.getAllDvirLocal())
        }
    }

    fun deleteDvirByRemoteId(entityDvir: EntityDvir) {
        viewModelScope.launch(Dispatchers.IO) {
            currentDvir.postValue(entityDvir)
            deleteDvirState.postValue(Resource.Loading())
            val requestCreateDvir = entityDvir.toRequestCreateDvir()
            requestCreateDvir.contactId = sharedPreferences.contactId
            deleteDvirState.postValue(useCaseDeleteDvir.execute(requestCreateDvir))
        }
    }

    fun getLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _locationName.postValue(useCaseGetLastLocation.execute())
        }
    }


}