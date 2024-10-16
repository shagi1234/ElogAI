package com.selbiconsulting.elog.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ViewModelMainLogin @Inject constructor(
    private val useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs,
) : ViewModel() {

    val hasLeftTruck: MutableLiveData<Boolean> = MutableLiveData(
        useCaseGetVarFromSharedPrefs.getVariable(SharedPrefsKeys.LEAVE_TRUCK, false)
    )

}