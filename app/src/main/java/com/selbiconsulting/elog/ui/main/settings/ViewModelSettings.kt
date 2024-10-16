package com.selbiconsulting.elog.ui.main.settings

import android.app.UiModeManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import com.selbiconsulting.elog.domain.use_case.UseCaseClearLocalDb
import com.selbiconsulting.elog.domain.use_case.UseCaseSetAppTheme
import com.selbiconsulting.elog.domain.use_case.UseCaseSetVarToSharedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelSettings @Inject constructor(
    private val useCaseSetAppTheme: UseCaseSetAppTheme,
    private val uiModeManager: UiModeManager,
    private val repositoryDriver: RepositoryDriver,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs,
    private val useCaseSetVarToSharedPrefs: UseCaseSetVarToSharedPrefs,
    private val useCaseClearLocalDb: UseCaseClearLocalDb,
) : ViewModel() {
    val driverInfo: MutableStateFlow<EntityDriver?> = MutableStateFlow(null)
    val isKeepScreenEnabled: MutableLiveData<Boolean> = MutableLiveData(
        useCaseGetVarFromSharedPrefs.getVariable(SharedPrefsKeys.KEEP_SCREEN_MODE, false)
    )

    fun fetchDriverData() {
        viewModelScope.launch(Dispatchers.IO) {
            driverInfo.emit(repositoryDriver.getDriverById(sharedPreferencesHelper.contactId ?: ""))
        }
    }
    fun changeAppTheme(appTheme: Int){
        useCaseSetAppTheme.execute(appTheme)
    }

    fun handleKeepScreenMode() {
        isKeepScreenEnabled.value = !isKeepScreenEnabled.value!!
        useCaseSetVarToSharedPrefs.setVariable(
            SharedPrefsKeys.KEEP_SCREEN_MODE,
            isKeepScreenEnabled.value
        )
    }

    fun leaveTruck() {
        useCaseSetVarToSharedPrefs.setVariable(SharedPrefsKeys.LEAVE_TRUCK, true)
    }

    fun logout(){
        viewModelScope.launch(Dispatchers.IO) {
            useCaseClearLocalDb.execute()
        }
    }
}