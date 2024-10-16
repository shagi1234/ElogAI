package com.selbiconsulting.elog.ui.main.logs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class ViewModelLogs @Inject constructor(
    private val repositoryLogs: RepositoryLogs
) : ViewModel() {

    val selectedDate: MutableStateFlow<DtoDate> = MutableStateFlow(getDate())

    val daysIsExistLog: MutableStateFlow<MutableList<DtoDate>> = MutableStateFlow(mutableListOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyLogs: StateFlow<List<EntityLog>> = selectedDate.flatMapLatest { currentDay ->
        repositoryLogs.getDailyLog(currentDay.formattedDate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    private val _seriesLiveData = MutableLiveData<LineGraphSeries<DataPoint>>()
    val seriesLiveData: LiveData<LineGraphSeries<DataPoint>> = _seriesLiveData

    val totalTimeOn: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeOff: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeSB: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeDR: MutableStateFlow<String> = MutableStateFlow("00:00")


    fun calculateTimeByStatuses() {
        viewModelScope.launch {
            totalTimeOff.emit(calculateTotalTimeByStatus(status = DutyStatus.OFF_DUTY))
            totalTimeOn.emit(calculateTotalTimeByStatus(status = DutyStatus.ON_DUTY))
            totalTimeSB.emit(calculateTotalTimeByStatus(status = DutyStatus.SB))
            totalTimeDR.emit(calculateTotalTimeByStatus(status = DutyStatus.DR))
        }

    }

    private fun calculateTotalTimeByStatus(status: DutyStatus): String {
        val filteredLogs = dailyLogs.value.filter { it.status == status }
        var totalDuration = Duration.ZERO
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        filteredLogs.forEach { log ->
            val start = LocalDateTime.parse(log.startTime, formatter)
            val end = LocalDateTime.parse(log.endTime, formatter)
            val duration = Duration.between(start, end)
            totalDuration = totalDuration.plus(duration)
        }
        val hours = totalDuration.toHours()
        val minutes = totalDuration.minusHours(hours).toMinutes()
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getLineGraphSeries(logList: List<DataLog>) {
        val dataPointsArray = makeDataGraphSeries(logList)

        val sortedDataPointsArray =
            dataPointsArray.sortedBy { it.x } // assuming x is the field to sort by
        _seriesLiveData.value = LineGraphSeries(sortedDataPointsArray.toTypedArray())
    }

    suspend fun checkDailyLogsIsCertified(dateReal: String): Boolean {
        return repositoryLogs.checkDayLogsAllVerified(dateReal)
    }

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
            selectedDate.emit(date)
        }
    }

    fun checkThisDateExistLogs(dates: List<DtoDate>) {
        val updatedList = mutableListOf<DtoDate>()

        dates.forEachIndexed { index, d ->
            viewModelScope.launch(Dispatchers.IO) {
                repositoryLogs.getDailyLog(d.formattedDate).collect { logs ->
                    if (logs.isNotEmpty()) {
                        synchronized(updatedList) {
                            updatedList.add(d)
                        }
                    }
                    // Check if this is the last date processed
                    if (index == dates.size - 1) {
                        // Ensure that sorting happens after all additions
                        synchronized(updatedList) {
                            updatedList.sortBy { it.position }
                        }

                        daysIsExistLog.emit(updatedList) // Create a copy to avoid potential issues
                    }
                }
            }
        }
    }

    private fun getHour(createdAt: String, formattedDate: String): String {
        return try {
            // Define the input format for the createdAt string
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val createdDateTime = LocalDateTime.parse(createdAt, inputFormatter)

            // Convert from UTC to the system's default timezone
            val zoneId = ZoneId.systemDefault()
            val createdDateTimeInCurrentZone = createdDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId)
                .toLocalDateTime()

            // Parse the selected date
            val selectedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedLocalDate = LocalDate.parse(formattedDate, selectedDateFormatter)

            // Compare createdAt date with the selected date
            when {
                createdDateTimeInCurrentZone.toLocalDate() > selectedLocalDate -> {
                    "23:59:58"
                }
                createdDateTimeInCurrentZone.toLocalDate() < selectedLocalDate -> {
                    "00:00:00"
                }
                else -> {
                    // If createdDateTimeInCurrentZone is on the same day as selectedLocalDate, return the time part
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    createdDateTimeInCurrentZone.format(timeFormatter)
                }
            }
        } catch (e: Exception) {
            // Handle any errors during parsing or conversion
            println("Error parsing or converting date: ${e.message}")
            "00:00:00"
        }
    }

    private fun makeDataGraphSeries(logList: List<DataLog>): List<DataPoint> {
        val dataPoints = mutableListOf<DataPoint>()


        logList.forEach { dataLog ->
            // Convert startTime and endTime to X-axis values (hours)
            val startX = getXFromTime(getHour(dataLog.timeStart ?: "00:00:00", selectedDate.value.formattedDate))
            var endX: Double? = null

            if (!dataLog.endTime.isNullOrEmpty()) {
                endX = getXFromTime(getHour(dataLog.endTime!!, selectedDate.value.formattedDate))
            }

            val status = when (dataLog.statusShort) {
                DutyStatus.OFF_DUTY -> 3.5
                DutyStatus.ON_DUTY -> 0.5
                DutyStatus.SB -> 2.5
                DutyStatus.DR -> 1.5
                DutyStatus.YM -> 1.5
                DutyStatus.PC -> 3.5
                DutyStatus.RESET -> null
            }

            status?.let {
                dataPoints.add(DataPoint(startX, status))
                endX?.let { DataPoint(it, status) }?.let { dataPoints.add(it) }
            }

        }

        return dataPoints
    }

    private fun getXFromTime(currentTime: String): Double {
        val timeComponents = currentTime.split(":") // Splitting the time string by colon

        return if (timeComponents.size == 3) {
            val hours = BigDecimal(timeComponents[0]) // Extracting hours
            val minutes = BigDecimal(timeComponents[1]) // Extracting minutes
            val seconds = BigDecimal(timeComponents[2]) // Extracting seconds
            val totalHours =
                hours + (minutes.divide(BigDecimal(60), 5, RoundingMode.HALF_UP)) + (seconds.divide(
                    BigDecimal(3600), 5, RoundingMode.HALF_UP
                )) // Converting to total hours

            totalHours.setScale(5, RoundingMode.HALF_UP).toDouble()
        } else {
            Log.e("getXFromTime", "Invalid time format: $currentTime")
            0.0
        }
    }


}