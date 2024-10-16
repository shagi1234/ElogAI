package com.selbiconsulting.elog.ui.main.inspections.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ViewModelLogReport @Inject constructor(
    private val repositoryLogs: RepositoryLogs
) : ViewModel() {

    val day: MutableStateFlow<DtoDate> = MutableStateFlow(getDate())

    private val _daysIsExistLog = MutableStateFlow<MutableList<DtoDate>>(mutableListOf())
    val daysIsExistLog get() = _daysIsExistLog

//    fun getDailyLog() {
//        repositoryLogs.getDailyLog()
//    }

    private fun getDate(): DtoDate {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val months = today.month.toString().dropLast(2).lowercase().capitalize()
        val dayOfMonths = today.dayOfMonth.toString()
        val formattedDate = today.format(dateFormatter)

        return DtoDate(
            day = " $months $dayOfMonths",
            formattedDate = formattedDate
        )
    }

    fun setDate(date: DtoDate) {
        viewModelScope.launch {
            day.emit(date)
        }
    }

    fun checkThisDateExistLogs(dates: List<DtoDate>) {
        _daysIsExistLog.value = mutableListOf()
        for (d in dates) {
            viewModelScope.launch(Dispatchers.IO) {
                repositoryLogs.getDailyLog(d.formattedDate).collect {
                    if (it.isNotEmpty()) {
                        _daysIsExistLog.value.add(d)
                    }
                }
            }
        }
    }
}