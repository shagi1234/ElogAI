package com.selbiconsulting.elog.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.domain.use_case.UseCaseChangeTheme
import com.selbiconsulting.elog.domain.use_case.UseCaseClearLocalDb
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLastLocation
import com.selbiconsulting.elog.ui.main.common.EldConnectionStatus
import com.selbiconsulting.elog.ui.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelHome @Inject constructor(
    private val useCaseChangeTheme: UseCaseChangeTheme,
    private val useCaseClearLocalDb: UseCaseClearLocalDb,
) : ViewModel() {

    fun changeTheme() {
        useCaseChangeTheme.execute()
    }


    fun clearLocalDb(){
        viewModelScope.launch(Dispatchers.IO) {
            useCaseClearLocalDb.execute()

        }
    }


}