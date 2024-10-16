package com.selbiconsulting.elog.ui.util

import ViolationType
import android.content.Context
import android.location.Geocoder
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoLogs
import com.selbiconsulting.elog.data.model.dto.DtoLogs.Companion.toListOfEntityLog
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.entity.calculateDurationInMinutes
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.request.RequestLocation
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseSendLogs
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryDriver
import com.selbiconsulting.elog.domain.repository.RepositoryLogs
import com.selbiconsulting.elog.domain.use_case.UseCaseClearLocalDb
import com.selbiconsulting.elog.domain.use_case.UseCaseGetDriverInfo
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLastLocation
import com.selbiconsulting.elog.domain.use_case.UseCaseGetLogs
import com.selbiconsulting.elog.domain.use_case.UseCaseLogin
import com.selbiconsulting.elog.domain.use_case.UseCasePostLocation
import com.selbiconsulting.elog.domain.use_case.UseCaseSendLogs
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.main.common.EldConnectionStatus
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.fromEntityLogToDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toDataLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toEntityLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toEntityLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toRequestSendLogs
import com.selbiconsulting.elog.ui.main.home.adapter.toArrayList
import com.selbiconsulting.elog.ui.main.home.adapter.toCommaSeparatedString
import com.selbiconsulting.elog.ui.util.Const.Companion.PTI_15_MINUTE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.log

/*
 * Created by shagi on 03.04.2024 00:50
 */

@HiltViewModel
class ViewModelChangeStatus @Inject constructor(
    private val repositoryLogs: RepositoryLogs,
    private val useCasePostLocation: UseCasePostLocation,
    private val repositoryDriver: RepositoryDriver,
    private val useCaseGetDriverInfo: UseCaseGetDriverInfo,
    private var useCaseSendLogs: UseCaseSendLogs,
    private var useCaseGetLogs: UseCaseGetLogs,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val useCaseGetLastLocation: UseCaseGetLastLocation,
    private val useCaseClearLocalDb: UseCaseClearLocalDb,
    private val useCaseLogin: UseCaseLogin
) : ViewModel() {
    var fusedLocationClient: FusedLocationProviderClient? = null
    private var countDownTimer: CountDownTimer? = null

    val eldStatus: MutableStateFlow<EldConnectionStatus> =
        MutableStateFlow(EldConnectionStatus.NotConnected)

    private val _logsResult = SingleLiveEvent<Resource<List<DtoLogs>>>()
    val logsResult get() = _logsResult

    var context: FragmentActivity? = null

    private val helperFunctions: HelperFunctions by lazy { HelperFunctions() }
    val todayLogs: MutableStateFlow<List<EntityLog>> = MutableStateFlow(emptyList())

    private val logsCurrentShift: MutableStateFlow<List<EntityLog>> = MutableStateFlow(emptyList())

    val driverData: MutableStateFlow<EntityDriver?> = MutableStateFlow(null)

    private val _driverState = SingleLiveEvent<Resource<EntityDriver>>()

    val driverState get() = _driverState

    private val _ptiViolationState = MutableStateFlow(ViolationType.NON_VIOLATION)
    val ptiViolationState get() = _ptiViolationState

    private val _warningViolationState = MutableStateFlow(ViolationType.NON_VIOLATION)
    val warningViolationState get() = _warningViolationState

    private val _smallBreakViolationState = MutableStateFlow(ViolationType.NON_VIOLATION)
    val smallBreakViolationState get() = _smallBreakViolationState

    private val _driveViolationState = MutableStateFlow(ViolationType.NON_VIOLATION)
    val driveViolationState get() = _driveViolationState

    private val _shiftViolationState = MutableStateFlow(ViolationType.NON_VIOLATION)
    val shiftViolationState get() = _shiftViolationState

    private val _drivingLogsAfterBreak = MutableStateFlow<List<EntityLog>>(emptyList())
    val drivingLogsAfterBreak get() = _drivingLogsAfterBreak


    private val _weeklyHours = MutableStateFlow(0.0)
    val weeklyHours get() = _weeklyHours

    private val logsHelperFunctions: LogsHelperFunctions by lazy {
        LogsHelperFunctions(
            repositoryLogs
        )
    }

    fun updateWeeklyHours() {
        viewModelScope.launch(Dispatchers.IO) {
            val hours = repositoryLogs.calculateWeeklyShiftHours()
            Log.e("CYCLE_HOURS", "observe: $hours")
            _weeklyHours.emit(hours)
        }
    }


    val currentDutyStatus: StateFlow<EntityLog?> = todayLogs.map { logs ->
        logs.lastOrNull()
    }.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = null
    )

    private val _seriesLiveData = MutableLiveData<LineGraphSeries<DataPoint>>()
    val seriesLiveData: LiveData<LineGraphSeries<DataPoint>> = _seriesLiveData

    private val _timerValue = MutableLiveData<String>()
    val timerValue: LiveData<String> = _timerValue

    val totalTimeOn: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeOff: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeSB: MutableStateFlow<String> = MutableStateFlow("00:00")
    val totalTimeDR: MutableStateFlow<String> = MutableStateFlow("00:00")

    private var shiftTimeOn: String = "00:00"
    private var shiftTimeOff: String = "00:00"
    private var shiftTimeSB: String = "00:00"
    private var shiftTimeDR: String = "00:00"

    private var locations: MutableList<RequestLocation> = mutableListOf()
    private val _durationLiveData = MutableLiveData<String>()

    init {
        refreshBreakTimer()
    }

    private val _location = MutableStateFlow("N/A")
    val location get() = _location

    fun getLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            _location.emit(useCaseGetLastLocation.execute())
        }
    }

    fun clearLocalDb() {
        viewModelScope.launch(Dispatchers.IO) {
            useCaseClearLocalDb.execute()
        }
    }

    fun reLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            val requestLogin = RequestLogin(
                username = sharedPreferencesHelper.username,
                password = sharedPreferencesHelper.password,
                deviceId = sharedPreferencesHelper.deviceID,
                deviceToken = sharedPreferencesHelper.fcm_token
            )
            val result = useCaseLogin.execute(requestLogin)
            if (result is Resource.Success) {
                sharedPreferencesHelper.token = result.data?.accessToken
                getDriverInfo()

            }
        }
    }

    fun clearLocation() {
        viewModelScope.launch {
            _location.emit("N/A")
        }
    }

    private fun refreshBreakTimer() {
        viewModelScope.launch {
            repositoryLogs.getDurationAfterIsDrivingResetTrue().onStart { emit("00:00") }
                .collect { duration ->
                    _durationLiveData.value = duration
                }
        }
    }

    fun calculateTimeByStatuses() {
        viewModelScope.launch {
            totalTimeOff.emit(
                calculateTotalTimeByStatus(
                    status = DutyStatus.OFF_DUTY, todayLogs.value
                )
            )
            totalTimeOn.emit(
                calculateTotalTimeByStatus(
                    status = DutyStatus.ON_DUTY, todayLogs.value
                )
            )
            totalTimeSB.emit(calculateTotalTimeByStatus(status = DutyStatus.SB, todayLogs.value))
            totalTimeDR.emit(calculateTotalTimeByStatus(status = DutyStatus.DR, todayLogs.value))
        }

        viewModelScope.launch {
            shiftTimeOff =
                calculateTotalTimeByStatus(status = DutyStatus.OFF_DUTY, logsCurrentShift.value)
            shiftTimeOn =
                calculateTotalTimeByStatus(status = DutyStatus.ON_DUTY, logsCurrentShift.value)
            shiftTimeDR = calculateTotalTimeByStatus(status = DutyStatus.DR, logsCurrentShift.value)
            shiftTimeSB = calculateTotalTimeByStatus(status = DutyStatus.SB, logsCurrentShift.value)
        }

    }

    private fun calculateTotalTimeByStatus(status: DutyStatus, logs: List<EntityLog>): String {
        val filteredLogs = logs.filter { it.status == status }
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

    fun getDailyLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryLogs.getDailyLog(getCurrentDate()).collectLatest { logs ->
                todayLogs.value = logs
            }
        }
    }

    fun getLogsCurrentShift() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryLogs.getLogsAfterReset().collectLatest { logs ->
                logsCurrentShift.value = logs
            }
        }
    }

    fun fetchLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            _logsResult.postValue(useCaseGetLogs.execute(sharedPreferencesHelper.contactId ?: ""))
        }
    }

    fun deleteAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryLogs.deleteAllLog()
        }
    }

    private fun insertAllLogs(logs: List<EntityLog>) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryLogs.addLogs(logs)
        }
    }


    fun getDriverDataLocal() {
        viewModelScope.launch(Dispatchers.IO) {
            driverData.emit(repositoryDriver.getDriverById(sharedPreferencesHelper.contactId ?: ""))
        }
    }

    fun upsertDriverInfo(driverInfo: EntityDriver) {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDriver.upsert(driverInfo)
            getDriverDataLocal()
        }
    }

    suspend fun getDriverInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            _driverState.postValue(
                useCaseGetDriverInfo.execute(
                    sharedPreferencesHelper.contactId ?: ""
                )
            )

        }
    }

    private fun getHour(createdAt: String): String {
        return try {
            // Define the input format for the createdAt string
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val createdDateTime = LocalDateTime.parse(createdAt, inputFormatter)

            // Convert from UTC to the system's default timezone
            val zoneId = ZoneId.systemDefault()
            val createdDateTimeInCurrentZone =
                createdDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(zoneId)
                    .toLocalDateTime()

            // Parse the selected date
            val selectedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedLocalDate = LocalDate.parse(getCurrentDate(), selectedDateFormatter)

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


    private fun getCurrentDate(): String {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return today.format(dateFormatter)
    }

    fun updateLastPointGraphView() {
        val dataPointsArray = makeDataGraphSeries()
        val listOfPoints = dataPointsArray.toMutableList()

        if (listOfPoints.isNotEmpty()) {
            val lastIndex = listOfPoints.lastIndex
            listOfPoints[lastIndex] = getDataPoint(listOfPoints[lastIndex])
            val sortedPoints = listOfPoints.sortedBy { it.x }
            _seriesLiveData.value = LineGraphSeries(sortedPoints.toTypedArray())
        }
    }

    private fun getDataPoint(dataPoint: DataPoint): DataPoint {
        return DataPoint(
            getXFromTime(getHour(HelperFunctions().getCreatedAt())) ?: dataPoint.x, dataPoint.y
        )
    }


    fun getLineGraphSeries() {
        val dataPointsArray = makeDataGraphSeries()
        val sortedDataPointsArray = dataPointsArray.sortedBy { it.x }
        // assuming x is the field to sort by
        dataPointsArray.sortedBy { it.x } // assuming x is the field to sort by

        _seriesLiveData.value = LineGraphSeries(sortedDataPointsArray.toTypedArray())
    }

    private fun makeDataGraphSeries(): List<DataPoint> {
        val logsList = todayLogs.value
        val dataPoints = mutableListOf<DataPoint>()

        logsList.forEach { dataLog ->
            val startX = getXFromTime(getHour(dataLog.startTime))
            val endX = getXFromTime(getHour(dataLog.endTime))

            val status = when (dataLog.status) {
                DutyStatus.OFF_DUTY -> 3.5
                DutyStatus.ON_DUTY -> 0.5
                DutyStatus.SB -> 2.5
                DutyStatus.DR -> 1.5
                DutyStatus.YM -> 1.5
                DutyStatus.PC -> 3.5
                DutyStatus.RESET -> null // Skip adding data points for RESET status
            }

            // Only add data points if status is not null
            status?.let {
                startX?.let { x -> dataPoints.add(DataPoint(x, it)) }
                endX?.let { x -> dataPoints.add(DataPoint(x, it)) }
            }
        }

        return dataPoints
    }

    private fun getXFromTime(currentTime: String): Double? {
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
            null
        }
    }

    private fun startTimer(duration: String) {
        val parts = duration.split(":")
        if (parts.size != 2) {
            // Handle incorrect format
            return
        }

        val hours = parts[0].toLongOrNull() ?: 0
        val minutes = parts[1].toLongOrNull() ?: 0

        val durationInMillis =
            (hours * 3600000) + (minutes * 60000) // Convert hours to milliseconds and add to minutes
        countDownTimer?.cancel()
        countDownTimer = object :
            CountDownTimer(durationInMillis, 3000) { // tick every minute (60000 milliseconds)
            override fun onTick(millisUntilFinished: Long) {
                val hoursLeft = millisUntilFinished / 3600000
                val minutesLeft = (millisUntilFinished % 3600000) / 60000
                Log.e("TAG_timer_blat", "onTick: " + "$hoursLeft:$minutesLeft")
                _timerValue.value = "$hoursLeft:$minutesLeft"
            }

            override fun onFinish() {
                _timerValue.value = "00:00"
            }
        }.start()
    }

    fun addLocation(
        latitude: Double, longitude: Double, degree: String, speed: String
    ) {
        locations.add(
            RequestLocation(
                sharedPreferencesHelper.contactId ?: "",
                "No Location",
                locationType = "truck",
                latitude = latitude.toString(),
                longitude = longitude.toString(),
                degree = degree,
                speed = speed,
                HelperFunctions().getCreatedAt()
            )
        )
    }

    suspend fun postLocation(
        latitude: Double, longitude: Double, degree: String, speed: String, context: Context?
    ) {
        val requestLocation = RequestLocation(
            sharedPreferencesHelper.contactId ?: "",
            getLocationName(LatLng(latitude, longitude), context),
            locationType = "truck",
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            degree = degree,
            speed = speed,
            HelperFunctions.shared.getCreatedAt()
        )

        locations.add(requestLocation)

        val locationsCopy = ArrayList(locations)

        when (val result = useCasePostLocation.execute(locationsCopy)) {
            is Resource.Error -> {

                when (result.statusCode) {
                    Resource.LOGIN_CODE -> reLogin()
                    Resource.LOGOUT_CODE -> {
                        this.context?.logout()
                        clearLocalDb()
                    }
                }
            }

            is Resource.Failure -> {

            }

            is Resource.Loading -> {

            }

            is Resource.Success -> {
                Log.d("Location_eld", "postLocation: " + result.data)
                locations.clear()
            }

        }
    }

    fun getLocationName(latLng: LatLng, context: Context?): String {
        context?.let { contextIn ->
            try {
                val geoCoder = Geocoder(contextIn, Locale.getDefault())
                val addressList = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 3)

                return if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]

                    val fullAddress = address?.getAddressLine(0)
                    sharedPreferencesHelper.lastLocation = "$fullAddress"
                    Log.e("TAG_location", "getLocationName:  $fullAddress")
                    "$fullAddress"
                } else {
                    Log.e("TAG_location", "No address found for the given coordinates")
                    "No address found"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return "Location not found"
            }
        }
        return "Location not found"

    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    fun addViolationToCurrentLog(violation: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (currentDutyStatus.value == null || currentDutyStatus.value!!.status == DutyStatus.RESET) return@launch
            val currentLog = currentDutyStatus.value!!
            val currentLogViolations = currentLog.violationTypes.toArrayList()

            if (currentLogViolations.none { it.equals(violation, ignoreCase = true) }) {
                currentLogViolations.add(violation)

                currentLog.violationTypes = currentLogViolations.toCommaSeparatedString()
                currentLog.isUpdated = 0

                repositoryLogs.updateLog(currentLog)
            }
        }
    }


    private suspend fun addStatuses(newDutyStatus: DataLog): Long {
        return repositoryLogs.addLog(newDutyStatus.toEntityLog())
    }

    private suspend fun getNotSentLogs(): List<EntityLog> {
        return repositoryLogs.getNotSentLogs()
    }

    private suspend fun sendLogs(newDutyStatus: List<DataLog>) {
        val requestSendLogs =
            newDutyStatus.toRequestSendLogs(sharedPreferencesHelper.contactId ?: "")

        Log.e("TAG_SEND_LOGS", "request:$requestSendLogs ")

        if (requestSendLogs.isNotEmpty()) {
            when (val result = useCaseSendLogs.execute(requestSendLogs)) {
                is Resource.Error -> {
                    Log.e("TAG_SEND_LOGS", "response:${result.responseMessage} ")
                }

                is Resource.Failure -> {
                    Log.e("TAG_SEND_LOGS", "response:${result.responseMessage} ")
                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    result.data?.let { data ->
                        Log.e("TAG_SEND_LOGS", "response:$data ")

                        viewModelScope.launch(Dispatchers.IO) {
                            updateLogs(newDutyStatus, data)
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateLogs(
        localDutyStatuses: List<DataLog>, logs: List<ResponseSendLogs>
    ) {
        // Create a map of localId to the corresponding ResponseSendLogs for quick lookup
        val logMap = logs.associateBy { it.localId }

        // Create a list to hold the updated duty statuses
        for (newDutyStatus in localDutyStatuses) {
            // Check if there is a corresponding log for the current duty status
            val log = logMap[newDutyStatus.localId.toString()]

            if (log != null) {
                // Create a copy of the newDutyStatus to avoid modifying the original object
                viewModelScope.launch(Dispatchers.IO) {
                    val updatedStatus = newDutyStatus.copy().apply {
                        id = log.logId
                        isUpdated = 1
                        isSent = 1
                    }

                    repositoryLogs.updateLog(updatedStatus.toEntityLog())
                }
            }
        }
    }

    fun changeDutyStatus(dataStatus: DataLog) {
        viewModelScope.launch(Dispatchers.IO) {
            val newDutyStatus = DataLog(statusShort = dataStatus.statusShort)
            newDutyStatus.timeStart = HelperFunctions.shared.getCreatedAt()
            newDutyStatus.endTime = HelperFunctions.shared.getCreatedAt()
            newDutyStatus.createdAt = HelperFunctions.shared.getCreatedAt()
            newDutyStatus.document = dataStatus.document ?: "1234"
            newDutyStatus.trailer = sharedPreferencesHelper.trailer
            newDutyStatus.note = dataStatus.note
            newDutyStatus.location = sharedPreferencesHelper.lastLocation
            newDutyStatus.odometer = sharedPreferencesHelper.odometer
            newDutyStatus.engineHours = sharedPreferencesHelper.engineHours
            newDutyStatus.vehicle = sharedPreferencesHelper.vehicle

            if (newDutyStatus.statusShort == DutyStatus.DR) {
                if (repositoryLogs.findPTILogs().isNotEmpty()) {
                    val lastPTILog = repositoryLogs.findPTILogs().last()
                    if (lastPTILog.calculateDurationInMinutes() < PTI_15_MINUTE) {
                        val ptiViolation = ViolationType.PTI.serverName
                        val currentLogViolations = newDutyStatus.violationTypes.toArrayList()


                        if (currentLogViolations.none {
                                it.equals(
                                    ptiViolation, ignoreCase = true
                                )
                            }) {
                            currentLogViolations.add(ptiViolation)

                            newDutyStatus.violationTypes =
                                currentLogViolations.toCommaSeparatedString()
                            newDutyStatus.isUpdated = 0

                        }
                    }
                } else {
                    val ptiViolation = ViolationType.PTI.serverName
                    val currentLogViolations = newDutyStatus.violationTypes.toArrayList()


                    if (currentLogViolations.none {
                            it.equals(
                                ptiViolation, ignoreCase = true
                            )
                        }) {
                        currentLogViolations.add(ptiViolation)

                        newDutyStatus.violationTypes = currentLogViolations.toCommaSeparatedString()
                        newDutyStatus.isUpdated = 0

                    }
                }
            }

            updateEndTimeLastLog(newDutyStatus.timeStart ?: HelperFunctions().getCreatedAt())

            newDutyStatus.localId = addStatuses(newDutyStatus)

            checkIsPickup(currentDutyStatus.value?.toDataLog())

            val logs = getNotSentLogs().fromEntityLogToDataLogs()

            sendLogs(logs)
        }
    }

    fun updateEndTimeLastLog(endTime: String) {

        viewModelScope.launch(Dispatchers.IO) {

            if (todayLogs.value.isNotEmpty()) {
                todayLogs.value.last().endTime = endTime
                todayLogs.value.last().isUpdated = 0

                repositoryLogs.updateLog(todayLogs.value.last())
            }

            if (logsCurrentShift.value.isNotEmpty()) {
                logsCurrentShift.value.last().endTime = endTime
            }

            currentDutyStatus.value?.endTime = HelperFunctions.shared.getCreatedAt()
        }
    }

    private fun checkIsPickup(dataStatus: DataLog?) {
        viewModelScope.launch {
            if (dataStatus?.statusShort == DutyStatus.ON_DUTY && dataStatus.note == "Pick up") {
                postLocation(
                    sharedPreferencesHelper.lastLatitude.toDouble(),
                    sharedPreferencesHelper.lastLongitude.toDouble(),
                    "0",
                    "0",
                    context
                )
            }
        }
    }

    private suspend fun setPtiViolation() {
        if (currentDutyStatus.value == null || repositoryLogs.findPTILogs().isEmpty()) return

        val lastPTILog = repositoryLogs.findPTILogs().lastOrNull()
        val currentLog = currentDutyStatus.value!!
        if (lastPTILog?.calculateDurationInMinutes()!! < PTI_15_MINUTE) {
            when (currentLog.status) {
                DutyStatus.DR -> ptiViolationState.emit(ViolationType.PTI)
                DutyStatus.ON_DUTY -> {
                    ptiViolationState.emit(ViolationType.WARNING_PTI)
                }

                else -> ptiViolationState.emit(ViolationType.NON_VIOLATION)
            }
        } else {
            ptiViolationState.emit(ViolationType.NON_VIOLATION)
        }
    }

    suspend fun setUninterruptedBreakViolation() {
        if (helperFunctions.convertToMinutes(calculateShiftTime()) <= 0) {
            _driveViolationState.emit(
                ViolationType.ON_DUTY_WITHOUT_UNINTERRUPTED_BREAK
            )
            delay(Const.minute15InMillis)

            _driveViolationState.emit(ViolationType.NON_VIOLATION)
        } else _driveViolationState.emit(ViolationType.NON_VIOLATION)
    }

    suspend fun setRemainingDriveViolation() {
        if (currentDutyStatus.value == null) return
        val currentLog = currentDutyStatus.value!!
        viewModelScope.launch(Dispatchers.IO) {
            if (helperFunctions.isLessThan30Minutes(calculateDriveTime()) && currentLog.status == DutyStatus.DR) _warningViolationState.emit(
                ViolationType.WARNING_30_MINUTE_BEFORE_DRIVING_ENDS
            )
        }
    }

    suspend fun setRemainingBreakViolation() {
        if (currentDutyStatus.value == null) return
        val currentLog = currentDutyStatus.value!!
        viewModelScope.launch(Dispatchers.IO) {
            if (helperFunctions.isLessThan30Minutes(calculateBreakTime()) && currentLog.status == DutyStatus.DR) _warningViolationState.emit(
                ViolationType.WARNING_30_MINUTE_BEFORE_BREAK_ENDS
            )
        }
    }

    suspend fun setRemainingShiftViolation() {
        if (currentDutyStatus.value == null) return
        val currentLog = currentDutyStatus.value!!
        viewModelScope.launch(Dispatchers.IO) {
            if (helperFunctions.isLessThan30Minutes(calculateShiftTime()) && currentLog.status != DutyStatus.OFF_DUTY) _warningViolationState.emit(
                ViolationType.WARNING_30_MINUTE_BEFORE_SHIFT_ENDS
            )
        }
    }

    suspend fun setSmallBreakViolation() {
        if (currentDutyStatus.value == null) return
        val currentLog = currentDutyStatus.value!!

        if (helperFunctions.calculateProgressInMinutes(calculateBreakTime()) <= 0 && currentLog.status == DutyStatus.DR) {
            _smallBreakViolationState.emit(ViolationType.DRIVING_WITHOUT_30_MINUTE_BREAK)
            delay(Const.minute15InMillis)
            _smallBreakViolationState.emit(ViolationType.NON_VIOLATION)
        } else {
            _smallBreakViolationState.emit(ViolationType.NON_VIOLATION)
        }
    }

    suspend fun setDrivingExceed11HoursViolation() {
        if (helperFunctions.calculateProgressInMinutes(calculateDriveTime()) <= 0) {
            _driveViolationState.emit(ViolationType.DRIVING_EXCEEDS_11_HOURS)
            delay(Const.minute15InMillis)
            _driveViolationState.emit(ViolationType.NON_VIOLATION)
        } else _driveViolationState.emit(ViolationType.NON_VIOLATION)
    }

    suspend fun setShiftViolation() {
        if (_weeklyHours.value >= Const.WEEKLY_HOURS_IN_MINUTES.divide(Const.divider)) {
            _shiftViolationState.emit(ViolationType.EXCEEDED_70_HOUR_SHIFT)
            delay(Const.minute15InMillis)
            _shiftViolationState.emit(ViolationType.NON_VIOLATION)
        } else _shiftViolationState.emit(ViolationType.NON_VIOLATION)
    }

    suspend fun setViolation() {
        if (currentDutyStatus.value == null) return
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                setPtiViolation()
                delay(5000)
            }
        }
    }

    fun clearAllViolations() {
        viewModelScope.launch {
            _ptiViolationState.emit(ViolationType.NON_VIOLATION)
            _driveViolationState.emit(ViolationType.NON_VIOLATION)
            _shiftViolationState.emit(ViolationType.NON_VIOLATION)
            _warningViolationState.emit(ViolationType.NON_VIOLATION)
            _smallBreakViolationState.emit(ViolationType.NON_VIOLATION)
        }
    }

    fun resumeShift() {
        startTimer(Const.shiftTime)
    }

    fun calculateBreakTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        val spentTimeParsed =
            LocalTime.parse(Const.breakTime.divide(Const.divider), formatter) // "14:00"

        var totalTimeParsed = LocalTime.parse(shiftTimeDR, formatter)

        val timeR = getTimeDrivingAfterLastBreak()

        if (timeR != null) {
            totalTimeParsed = LocalTime.parse(timeR, formatter)
        }

        val duration = Duration.between(LocalTime.MIN, spentTimeParsed)
        val totalDurationSpent = Duration.between(LocalTime.MIN, totalTimeParsed)

        // Calculate remaining time
        val remainingTime = duration.minus(totalDurationSpent)

        // Check if the remaining time is negative
        if (remainingTime.isNegative) {
            return "00:00"
        }

        // Convert remaining time back to LocalTime
        val remainingHours = remainingTime.toHours()
        val remainingMinutes = remainingTime.minus(remainingHours, ChronoUnit.HOURS).toMinutes()

        // Format the remaining time as "hh:mm"
        return String.format("%d:%02d", remainingHours, remainingMinutes)
    }

    private fun getTimeDrivingAfterLastBreak(): String? {
        val logs = logsCurrentShift.value

        var lastResetIndex: Int? = null

        // Find the last driving time reset log
        for (i in logs.indices.reversed()) {

            if (logs[i].isDrivingTimeResetLog == "true") {
                lastResetIndex = i
                break
            }
        }

        if (lastResetIndex == null) {
            return null
        }

        var totalDrivingTime = 0L

        for (i in lastResetIndex + 1 until logs.size) {
            if (logs[i].status == DutyStatus.DR) {
                totalDrivingTime += calculateDuration(logs[i].startTime, logs[i].endTime)
            }
        }

        return formatDuration(totalDrivingTime)
    }


    private fun calculateDuration(startTime: String?, endTime: String?): Long {
        if (startTime == null || endTime == null) {
            return 0L
        }

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val start: Date? = dateFormat.parse(startTime)
            val end: Date? = dateFormat.parse(endTime)

            if (start != null && end != null) {
                end.time - start.time
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    private fun parseDuration(time: String): Duration {
        val parts = time.split(":")
        val hours = parts[0].toLong()
        val minutes = parts[1].toLong()
        return Duration.ofHours(hours).plusMinutes(minutes)
    }

    fun calculateShiftTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        try {
            val shiftTimeParsed =
                LocalTime.parse(Const.shiftTime.divide(Const.divider), formatter) // "14:00"
            val shiftDuration = Duration.between(LocalTime.MIN, shiftTimeParsed)

            val totalTimeOnDuration = parseDuration(shiftTimeOn)
            val totalTimeDRDuration = parseDuration(shiftTimeDR)

            val totalTimeShift = totalTimeOnDuration.plus(totalTimeDRDuration)
            val remainingTimeShift = shiftDuration.minus(totalTimeShift)

            // Check if the remaining time is negative
            if (remainingTimeShift.isNegative) {
                return "00:00"
            }

            // Convert remaining time back to hours and minutes
            val remainingHours = remainingTimeShift.toHours()
            val remainingMinutes = remainingTimeShift.minusHours(remainingHours).toMinutes()

            // Format the remaining time as "hh:mm"
            return String.format("%d:%02d", remainingHours, remainingMinutes)
        } catch (e: DateTimeParseException) {
            // Handle the exception, e.g., return a default value or rethrow
            e.printStackTrace()
            return "Invalid time format"
        }
    }

    fun calculateDriveTime(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        try {
            val spentTimeParsed =
                LocalTime.parse(Const.driveTime.divide(Const.divider), formatter) // "14:00"
            val duration = Duration.between(LocalTime.MIN, spentTimeParsed)

            val totalDurationSpent = parseDuration(shiftTimeDR)

            // Calculate remaining time
            val remainingTime = duration.minus(totalDurationSpent)

            // Check if the remaining time is negative
            if (remainingTime.isNegative) {
                return "00:00"
            }

            // Convert remaining time back to hours and minutes
            val remainingHours = remainingTime.toHours()
            val remainingMinutes = remainingTime.minusHours(remainingHours).toMinutes()

            // Format the remaining time as "hh:mm"
            return String.format("%d:%02d", remainingHours, remainingMinutes)
        } catch (e: DateTimeParseException) {
            // Handle the exception, e.g., return a default value or rethrow
            e.printStackTrace()
            return "Invalid time format"
        }
    }

    private fun calculateResetTime(): String? {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

        // Check if the current duty status is OFF_DUTY
        if (currentDutyStatus.value?.status == DutyStatus.OFF_DUTY) {
            val endTimeStr = currentDutyStatus.value?.endTime
            val startTimeStr = currentDutyStatus.value?.startTime

            if (endTimeStr != null && startTimeStr != null) {
                try {
                    // Determine if the strings are time or date-time and parse accordingly
                    val endTime = try {
                        LocalTime.parse(endTimeStr, timeFormatter)
                    } catch (e: Exception) {
                        ZonedDateTime.parse(endTimeStr, dateTimeFormatter).toLocalTime()
                    }

                    val startTime = try {
                        LocalTime.parse(startTimeStr, timeFormatter)
                    } catch (e: Exception) {
                        ZonedDateTime.parse(startTimeStr, dateTimeFormatter).toLocalTime()
                    }

                    // Calculate the duration between startTime and endTime
                    val duration = Duration.between(startTime, endTime)
                    val hours = duration.toHours()
                    val minutes = duration.toMinutes() % 60

                    // Return the duration formatted as HH:mm
                    return String.format("%02d:%02d", hours, minutes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return "Invalid time format"
                }
            }
        }
        return null
    }


    private fun calculateSmallBreak(): String {
        val dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

        val currentDutyStatus = currentDutyStatus.value

        // Check if the current log status is one of the specified statuses
        if (currentDutyStatus?.status == DutyStatus.YM ||
            currentDutyStatus?.status == DutyStatus.SB ||
            currentDutyStatus?.status == DutyStatus.OFF_DUTY ||
            (currentDutyStatus?.status == DutyStatus.ON_DUTY &&
                    (currentDutyStatus.note == Const.dotInspection || currentDutyStatus.note == Const.fuel))
        ) {

            try {
                // Parse start time and end time
                val startTime = Instant.from(dateTimeFormatter.parse(currentDutyStatus.startTime))
                val endTime = Instant.from(dateTimeFormatter.parse(currentDutyStatus.endTime))

                // Calculate the duration between start time and end time
                val duration = Duration.between(startTime, endTime)

                // Check if the duration is negative or zero
                if (duration.isNegative || duration.isZero) {
                    return "00:00"
                }

                // Convert duration to total minutes
                val totalMinutes = duration.toMinutes()
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60

                // Format the duration as "HH:mm"
                return String.format("%02d:%02d", hours, minutes)
            } catch (e: DateTimeParseException) {
                e.printStackTrace()
                return "00:00"
            } catch (e: Exception) {
                e.printStackTrace()
                return "00:00"
            }
        }
        return "00:00"
    }

    fun checkResetLog() {
        if (calculateResetTime() != null && calculateResetTime() == Const.resetTime.divide(Const.divider)) {
            viewModelScope.launch(Dispatchers.IO) {
                val oldDutyStatus = currentDutyStatus.value

                val dataStatusContinue = DataLog()
                dataStatusContinue.trailer = oldDutyStatus?.trailer
                dataStatusContinue.document = oldDutyStatus?.document
                dataStatusContinue.statusShort = oldDutyStatus?.status ?: DutyStatus.OFF_DUTY
                dataStatusContinue.note = oldDutyStatus?.note

                val reset = DataLog()
                reset.trailer = ""
                reset.document = ""
                reset.statusShort = DutyStatus.RESET
                reset.note = "reset"
                reset.isDrivingTimeResetLog = "true"
                reset.trailer = sharedPreferencesHelper.trailer

                updateEndTimeLastLog(reset.timeStart ?: HelperFunctions().getCreatedAt())
                updateEndTimeLastLog(
                    dataStatusContinue.timeStart ?: HelperFunctions().getCreatedAt()
                )

                addStatuses(reset)
                addStatuses(dataStatusContinue)

                sendLogs(getNotSentLogs().fromEntityLogToDataLogs())
            }
        }
    }

    fun checkSmallBreak() {
        if (calculateSmallBreak() == Const.smallBreakTime.divide(
                Const.divider
            )
        ) {


            logsCurrentShift.value.lastOrNull()?.isDrivingTimeResetLog = true.toString()

            currentDutyStatus.value?.isDrivingTimeResetLog = Const.isDrivingTimeReset
            currentDutyStatus.value?.isUpdated = 0

            currentDutyStatus.value?.toDataLog()?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    repositoryLogs.updateLog(
                        it.toEntityLog()
                    )
                }
            }

            refreshBreakTimer()
        }
    }

    fun checkIsExistReset(listOfLogs: List<DtoLogs>) {
        val logs = listOfLogs.toDataLogs().toMutableList()

        val reset = DataLog()
        reset.statusShort = DutyStatus.RESET
        reset.isDrivingTimeResetLog = true.toString()
        reset.timeStart = HelperFunctions.shared.getCreatedAt()
        reset.trailer = sharedPreferencesHelper.trailer
        reset.endTime = HelperFunctions.shared.getCreatedAt()
        reset.createdAt = HelperFunctions.shared.getCreatedAt()

        val cycleReset = DataLog()
        cycleReset.statusShort = DutyStatus.RESET
        cycleReset.note = "cycle"
        reset.trailer = sharedPreferencesHelper.trailer
        cycleReset.isDrivingTimeResetLog = true.toString()
        cycleReset.timeStart = HelperFunctions.shared.getCreatedAt()
        cycleReset.endTime = HelperFunctions.shared.getCreatedAt()
        cycleReset.createdAt = HelperFunctions.shared.getCreatedAt()

        if (logs.isEmpty()) {
            logs.add(cycleReset)

            insertAllLogs(logs.toEntityLogs())
            return
        }
        logs.sortBy { it.timeStart }

        reset.timeStart = logs[0].timeStart
        reset.endTime = logs[0].timeStart
        reset.createdAt = logs[0].timeStart

        cycleReset.timeStart = logs[0].timeStart
        cycleReset.endTime = logs[0].timeStart
        cycleReset.createdAt = logs[0].timeStart

        val hasReset = logs.any { it.statusShort == DutyStatus.RESET }

        val hasCycleReset = logs.any { it.statusShort == DutyStatus.RESET && it.note == "cycle" }

        val allDrivingTimeResetLogFalse = logs.all { it.isDrivingTimeResetLog == false.toString() }

        when {
            !hasReset && !hasCycleReset -> {
                logs.add(0, cycleReset) // Add cycle reset log if neither exists
            }

            !hasReset -> {
                logs.add(0, reset) // Add reset log if only reset does not exist
            }

            !hasCycleReset -> {
                logs.add(0, cycleReset) // Add cycle reset log if only cycle reset does not exist
            }
        }

        if (allDrivingTimeResetLogFalse && logs.isNotEmpty()) {
            logs[0].isDrivingTimeResetLog =
                true.toString() // Update the first log's isDrivingTimeResetLog to true
        }

        insertAllLogs(logs.toEntityLogs())

    }

    fun checkCycleReset() {
        viewModelScope.launch(Dispatchers.IO) {
            if (repositoryLogs.checkResetCycleLog()) {
                val cycleReset = DataLog().apply {
                    statusShort = DutyStatus.RESET
                    note = "cycle"
                    isDrivingTimeResetLog = true.toString()
                    timeStart = HelperFunctions.shared.getCreatedAt()
                    endTime = HelperFunctions.shared.getCreatedAt()
                    createdAt = HelperFunctions.shared.getCreatedAt()
                }

                repositoryLogs.addLog(cycleReset.toEntityLog())
                sendLogs(getNotSentLogs().fromEntityLogToDataLogs())
            }
        }

    }

}