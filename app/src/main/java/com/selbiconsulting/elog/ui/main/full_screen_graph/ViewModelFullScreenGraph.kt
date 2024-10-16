package com.selbiconsulting.elog.ui.main.full_screen_graph

import androidx.lifecycle.ViewModel
import com.selbiconsulting.elog.domain.use_case.UseCaseChangeTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ViewModelFullScreenGraph @Inject constructor(
    private val useCaseChangeTheme: UseCaseChangeTheme
) : ViewModel() {
    fun changeTheme() {
        useCaseChangeTheme.execute()
    }
}