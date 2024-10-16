package com.selbiconsulting.elog.ui.main.ym_and_pc

import androidx.lifecycle.ViewModel
import com.selbiconsulting.elog.domain.use_case.UseCaseChangeTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ViewModelYmAndPcStatuses
@Inject constructor(
    private val useCaseChangeTheme: UseCaseChangeTheme
) : ViewModel() {
    fun changeTheme() {
        useCaseChangeTheme.execute()
    }
}