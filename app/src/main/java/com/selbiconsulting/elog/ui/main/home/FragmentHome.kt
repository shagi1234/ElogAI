package com.selbiconsulting.elog.ui.main.home

import ViolationType
import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.iosix.eldblelib.EldBleConnectionStateChangeCallback
import com.iosix.eldblelib.EldBleDataCallback
import com.iosix.eldblelib.EldBleError
import com.iosix.eldblelib.EldBleScanCallback
import com.iosix.eldblelib.EldBroadcast
import com.iosix.eldblelib.EldBroadcastTypes
import com.iosix.eldblelib.EldBufferRecord
import com.iosix.eldblelib.EldDataRecord
import com.iosix.eldblelib.EldManager
import com.iosix.eldblelib.EldScanObject
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoMainInfo
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.entity.EntityLog
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentHomeBinding
import com.selbiconsulting.elog.databinding.ItemCircularTiemlineVer2Binding
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.main.common.BluetoothDevicesListener
import com.selbiconsulting.elog.ui.main.common.BottomSheetScanBluetooth
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.EldConnectionStatus
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.flow.FragmentFlowDirections
import com.selbiconsulting.elog.ui.main.home.adapter.AdapterLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.fromEntityLogToDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toEntityLogs
import com.selbiconsulting.elog.ui.main.home.adapter.LogsItemListener
import com.selbiconsulting.elog.ui.main.logs_page.BottomSheetLogDetails
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.Const.Companion.WEEKLY_HOURS_IN_MINUTES
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import com.selbiconsulting.elog.ui.util.logout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.EnumSet
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject


@AndroidEntryPoint
class FragmentHome : Fragment(), BluetoothDevicesListener, LogsItemListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var graphViewHelper: GraphViewHelper
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val viewModelChangeStatus: ViewModelChangeStatus by activityViewModels()
    private lateinit var mEldManager: EldManager
    private val autoThread: AutoThread = AutoThread()
    private var isStatusChanging = false
    private var bottomSheetScanBluetooth: BottomSheetScanBluetooth? = null

    // Variable to store the previous odometer reading
    private var previousOdometerReading: Double = 0.0

    // Variable to accumulate the distance traveled
    private var accumulatedDistance: Double = 0.0

    private var time: Long = 0

    private val helperFunctions: HelperFunctions by lazy { HelperFunctions() }
    private val uiHelper: UiHelper by lazy { UiHelper(requireContext()) }
    private var adapterLogs: AdapterLogs? = null

    companion object {
        private const val REQUEST_BASE = 100
        const val REQUEST_BT_ENABLE = REQUEST_BASE + 1
    }

    private val subscribedRecords: Set<EldBroadcastTypes> = EnumSet.of(
        EldBroadcastTypes.ELD_BUFFER_RECORD,
        EldBroadcastTypes.ELD_CACHED_RECORD,
        EldBroadcastTypes.ELD_FUEL_RECORD,
        EldBroadcastTypes.ELD_DATA_RECORD,
        EldBroadcastTypes.ELD_DRIVER_BEHAVIOR_RECORD,
        EldBroadcastTypes.ELD_EMISSIONS_RECORD,
        EldBroadcastTypes.ELD_ENGINE_RECORD,
        EldBroadcastTypes.ELD_TRANSMISSION_RECORD
    )

    var automodeenabled = false
    var autoinprogress = false
    var automoderequestsize = 10
    var reqstart: Int = 0
    var reqend: Int = 0


    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    @Inject
    lateinit var uiModeManager: UiModeManager

    private val bleScanCallback: EldBleScanCallback = object : EldBleScanCallback() {

        override fun onScanResult(device: EldScanObject) {

            val strDevice: String = device.deviceId

            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(), "FOUND ELD $strDevice, CONNECTING", Toast.LENGTH_SHORT
                ).show()

                bottomSheetScanBluetooth?.showAvailableDevices(listOf(device))

            }
        }

        override fun onScanResult(deviceList: ArrayList<EldScanObject>?) {
            val strDevice: String
            val so: EldScanObject

            bottomSheetScanBluetooth?.showAvailableDevices(deviceList ?: emptyList())


            if (deviceList != null) {
                so = deviceList[0]
                strDevice = so.deviceId

                bottomSheetScanBluetooth?.showAvailableDevices(listOf(so))
            } else {
                bottomSheetScanBluetooth?.showNotFoundLay()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mEldManager = EldManager.GetEldManager(context, "123456789A")

        if (sharedPreferences.firstTimeAfterLogin) {
            viewModelChangeStatus.fetchLogs()
            sharedPreferences.firstTimeAfterLogin = false
        } else {
            viewModelChangeStatus.getDailyLogs()
            viewModelChangeStatus.getLogsCurrentShift()
        }

        viewModelChangeStatus.resumeShift()

        Log.e("CONTACT_ID", "onCreate: ${sharedPreferences.contactId}")
        Log.e("CONTACT_ID", "onCreate: ${sharedPreferences.token}")

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        init()
        getDriverInfo()
        setRecyclerLogs()
        changeUI()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTimeByStatus()
        observe()
        initListeners()
    }

    private fun init() {
        viewModelChangeStatus.fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        graphViewHelper = GraphViewHelper(
            requireContext(), binding.graphView
        )

        bottomSheetScanBluetooth = BottomSheetScanBluetooth(this, bleScanCallback, mEldManager)

        lifecycleScope.launch {
            viewModelChangeStatus.updateWeeklyHours()
        }
    }

    private fun getDriverInfo() {
        lifecycleScope.launch {
            if (sharedPreferences.vehicle.isNullOrEmpty())
                viewModelChangeStatus.getDriverInfo()
            else
                viewModelChangeStatus.getDriverDataLocal()
        }
    }

    private fun changeUI() {
        viewModelChangeStatus.calculateTimeByStatuses()

        setDriveTimeIndicator()
        setShiftTimeIndicator()
        setBreakTimeIndicator()

        setMainInfo()

        lifecycleScope.launch {
            viewModelChangeStatus.setViolation()
        }


    }

    private fun observeTimeByStatus() {
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeOff.collect {
                binding.valTotalTimeOff.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.OFF_DUTY) {
                    binding.valTotalTimeLast.text = it
                }
            }
        }
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeSB.collect {
                binding.valTotalTimeSb.text = it

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.SB) {
                    binding.valTotalTimeLast.text = it
                }

            }
        }
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeOn.collect {
                binding.valTotalTimeOn.text = it

                setShiftTimeIndicator()

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.ON_DUTY) {
                    binding.valTotalTimeLast.text = it
                }


            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeDR.collect {
                binding.valTotalTimeDr.text = it
                setDriveTimeIndicator()
                setBreakTimeIndicator()

                lifecycleScope.launch {
                    viewModelChangeStatus.setRemainingDriveViolation()
                    viewModelChangeStatus.setRemainingBreakViolation()
                    viewModelChangeStatus.setSmallBreakViolation()

                }

                if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.DR) {
                    binding.valTotalTimeLast.text = it
                }
            }
        }
    }

    private fun checkDriveTimeMoreThanShift(): Boolean {
        return HelperFunctions.shared.convertToMinutes(viewModelChangeStatus.calculateDriveTime()) >= HelperFunctions.shared.convertToMinutes(
            viewModelChangeStatus.calculateShiftTime()
        )
    }

    private fun observe() {
        lifecycleScope.launch {
            sharedViewModel.isPcAllowed.observe(viewLifecycleOwner) {
                if (it) {
                    binding.cardPc.visibility = View.VISIBLE
                } else {
                    binding.cardPc.visibility = View.INVISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.driveViolationState.collect { violation ->
                updateDriveViolationMessage(violation.state, violation.message)
                if (violation.state == ToastStates.ERROR) {
                    addViolationToCurrentLog(violation = violation.serverName)
                    if (adapterLogs == null || adapterLogs!!.itemCount == 0) return@collect
                    delay(3000)
                    adapterLogs!!.notifyItemChanged((adapterLogs!!.itemCount - 1))
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.shiftViolationState.collect { violation ->
                updateDriveViolationMessage(violation.state, violation.message)
                if (violation.state == ToastStates.ERROR) {
                    addViolationToCurrentLog(violation = violation.serverName)
                    if (adapterLogs == null || adapterLogs!!.itemCount == 0) return@collect
                    delay(3000)
                    adapterLogs!!.notifyItemChanged((adapterLogs!!.itemCount - 1))
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.smallBreakViolationState.collect { violation ->
                updateSmallBreakViolationMessage(violation.state, violation.message)
                if (violation.state == ToastStates.ERROR) {
                    addViolationToCurrentLog(violation = violation.serverName)
                    if (adapterLogs == null || adapterLogs!!.itemCount == 0) return@collect
                    delay(3000)
                    adapterLogs!!.notifyItemChanged((adapterLogs!!.itemCount - 1))
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.weeklyHours.collect { hours ->
                setCycleTimeIndicator(hours)
                viewModelChangeStatus.setShiftViolation()
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.ptiViolationState.collect { violation ->
                updatePTIViolationMessage(state = violation.state, message = violation.message)
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.warningViolationState.collect { violation ->
                updateViolationMessage(state = violation.state, message = violation.message)
                if (violation.state == ToastStates.ERROR) {
                    addViolationToCurrentLog(violation = violation.serverName)
                    if (adapterLogs == null || adapterLogs!!.itemCount == 0) return@collect
                    delay(1500)
                    adapterLogs!!.notifyItemChanged((adapterLogs!!.itemCount - 1))
                }
            }
        }

        viewModelChangeStatus.seriesLiveData.observe(viewLifecycleOwner) { series ->
            graphViewHelper.setSeriesSolid(series)
        }

        lifecycleScope.launch {
            viewModelChangeStatus.driverData.collect {
                updateDriverInfo(it)
            }
        }

        viewModelChangeStatus.driverState.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    when (it.statusCode) {
                        Resource.LOGOUT_CODE -> logout()
                        Resource.LOGIN_CODE -> reLogin()
                    }
                }

                else -> { //Success
                    if (it.data == null) return@observe
                    sharedPreferences.trailer = it.data.trailerId
                    sharedPreferences.vehicle = it.data.vehicleId
                    viewModelChangeStatus.upsertDriverInfo(it.data)
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.currentDutyStatus.collect { currentStatus ->
                if (currentStatus == null) return@collect

                setDriveTimeIndicator()
                setShiftTimeIndicator()
                setBreakTimeIndicator()

                lifecycleScope.launch {
                    viewModelChangeStatus.setViolation()
                }

                handleStatusButtons(currentStatus.status)
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.todayLogs.collect { statuses ->
                if (statuses.isEmpty()) return@collect
                viewModelChangeStatus.getLineGraphSeries()
                binding.recStatus.visibility = if (statuses.isEmpty()) View.GONE else View.VISIBLE
                adapterLogs?.updateData(statuses.fromEntityLogToDataLogs())
                if (statuses.last().status == DutyStatus.RESET) viewModelChangeStatus.clearAllViolations()


            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.timerValue.observe(viewLifecycleOwner) {
                viewModelChangeStatus.updateEndTimeLastLog(HelperFunctions.shared.getCreatedAt())

                viewModelChangeStatus.updateLastPointGraphView()

                viewModelChangeStatus.calculateTimeByStatuses()

                viewModelChangeStatus.checkResetLog()
                viewModelChangeStatus.checkCycleReset()
                viewModelChangeStatus.checkSmallBreak()
                viewModelChangeStatus.updateWeeklyHours()

                lifecycleScope.launch {
//                    viewModelChangeStatus.setDrivingExceed11HoursViolation()
                    viewModelChangeStatus.setUninterruptedBreakViolation()
                    viewModelChangeStatus.setRemainingShiftViolation()
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.eldStatus.collect {
                setEldStatus(it)
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.logsResult.observe(viewLifecycleOwner) {
                val progressDialog = CustomProgressDialog(requireContext())

                when (it) {
                    is Resource.Error -> {
                        progressDialog.dismiss()
                        when (it.statusCode) {
                            Resource.LOGOUT_CODE -> logout()
                            Resource.LOGIN_CODE -> {
                                reLogin()
                                lifecycleScope.launch {
                                    delay(2000)
                                    viewModelChangeStatus.fetchLogs()
                                }
                            }

                            else -> {
                                CustomToast.showCustomToastWithContent(
                                    v = requireView(),
                                    activity = requireActivity(),
                                    state = ToastStates.ERROR,
                                    stateTitle = it.responseMessage?.stringMessage ?: "",
                                    enableClearIcon = false
                                )
                            }
                        }

                    }

                    is Resource.Failure -> {
                        progressDialog.dismiss()

                        CustomToast.showCustomToastWithContent(
                            v = requireView(),
                            activity = requireActivity(),
                            state = ToastStates.ERROR,
                            stateTitle = resources.getString(R.string.no_internet_connection),
                            enableClearIcon = false
                        )
                    }

                    is Resource.Loading -> {
                        progressDialog.show()
                    }

                    is Resource.Success -> {

                        it.data?.let { logs ->
                            viewModelChangeStatus.deleteAllLogs()
                            viewModelChangeStatus.checkIsExistReset(
                                logs
                            )
                        }

                        progressDialog.dismiss()

                        CustomToast.showCustomToastWithContent(
                            v = requireView(),
                            activity = requireActivity(),
                            state = ToastStates.SUCCESS,
                            stateTitle = resources.getString(R.string.the_reset_was_completed_succesfully),
                            enableClearIcon = false
                        )

                        viewModelChangeStatus.getDailyLogs()
                        viewModelChangeStatus.getLogsCurrentShift()
                    }

                }
            }
        }
    }

//    private fun setCycleTimeIndicator(hours: Double) {
//        val cycleTimeIndicator = binding.indicatorCycle
//
//        cycleTimeIndicator.max = (ViolationType.WEEKLY_HOURS_IN_MINUTES)
//        cycleTimeIndicator.progress = (ViolationType.WEEKLY_HOURS_IN_MINUTES) - (hours * 60).toInt()
//
//        Log.e(
//            "CYCLE_PROGRESS",
//            "setCycleTimeIndicator: ${(ViolationType.WEEKLY_HOURS_IN_MINUTES) - (hours * 60).toInt()}",
//        )
//
//        val totalMinutes = (hours * 60).toInt()
//        val totalHours = totalMinutes / 60
//        val minutes = totalMinutes % 60
//        binding.tvCycleProgress?.text = String.format("%02d:%02d", totalHours % 24, minutes)
//
//    }

    private fun setCycleTimeIndicator(hours: Double) {
        val totalMinutes = WEEKLY_HOURS_IN_MINUTES.divide(Const.divider)
        val totalHours = totalMinutes / 60
        val minutes = totalMinutes % 60
        binding.tvTotalCycleTime.text = String.format("%02d:%02d", totalHours, minutes)
        val cycleTimeIndicator = binding.indicatorCycle

        cycleTimeIndicator.max = totalMinutes

        val elapsedMinutes = (hours * 60).toInt()

        val remainingMinutes = totalMinutes - elapsedMinutes

        cycleTimeIndicator.progress = remainingMinutes

        cycleTimeIndicator.max = (WEEKLY_HOURS_IN_MINUTES.divide(Const.divider))
        cycleTimeIndicator.progress =
            (WEEKLY_HOURS_IN_MINUTES.divide(Const.divider)) - (hours * 60).toInt()

        val remainingHours = if (remainingMinutes >= 0) remainingMinutes / 60 else 0
        val remainingMinutesFormatted = if (remainingMinutes >= 0) remainingMinutes % 60 else 0

        binding.tvCycleProgress.text =
            String.format("%02d:%02d", remainingHours, remainingMinutesFormatted)
    }
    private fun setDriveTimeIndicator() {
        val driveStatusIndicator = binding.driveStatus

        val remainedTime =
            if (checkDriveTimeMoreThanShift()) viewModelChangeStatus.calculateShiftTime() else viewModelChangeStatus.calculateDriveTime()

        val progressColor =
            if (helperFunctions.isLessThan30Minutes(remainedTime)) R.color.error_on
            else if (helperFunctions.isProgressEnded(remainedTime)) R.color.stroke_secondary
            else R.color.success_on

        driveStatusIndicator.apply {
            tvValue.text = remainedTime
            tvTitle.text = resources.getString(R.string.drive)
            timelineIndicator.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    progressColor
                )
            )
            timelineIndicator.max =
                helperFunctions.calculateProgressInMinutes(Const.driveTime.divide(Const.divider))
            timelineIndicator.setProgress(
                helperFunctions.calculateProgressInMinutes(remainedTime),
                true
            )

            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.DR) {
                uiHelper.setActiveCircularProgressIndicator(driveStatusIndicator)
            } else {
                uiHelper.setInactiveCircularProgressIndicator(driveStatusIndicator)
            }
        }

    }

    private fun setShiftTimeIndicator() {
        val shiftTimeIndicator = binding.shiftStatus

        val remainedTime = viewModelChangeStatus.calculateShiftTime()
        val progressColor =
            if (helperFunctions.isLessThan30Minutes(remainedTime)) R.color.error_on
            else if (helperFunctions.isProgressEnded(remainedTime)) R.color.stroke_secondary
            else R.color.primary_brand

        shiftTimeIndicator.apply {
            tvValue.text = remainedTime
            tvTitle.text = resources.getString(R.string.shift)

            timelineIndicator.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    progressColor
                )
            )
            timelineIndicator.max =
                helperFunctions.calculateProgressInMinutes(Const.shiftTime.divide(Const.divider))
            timelineIndicator.setProgress(
                helperFunctions.calculateProgressInMinutes(remainedTime),
                true
            )

            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.ON_DUTY) {
                uiHelper.setActiveCircularProgressIndicator(shiftTimeIndicator)
            } else {
                uiHelper.setInactiveCircularProgressIndicator(shiftTimeIndicator)
            }
        }
    }

    private fun setBreakTimeIndicator() {
        val breakStatusIndicator = binding.breakStatus

        val remainedTime = viewModelChangeStatus.calculateBreakTime()

        Log.e(
            "BREAK_TIME",
            "setBreakTimeIndicator: ${helperFunctions.calculateProgressInMinutes(remainedTime)}",
        )
        val progressColor =
            if (helperFunctions.isLessThan30Minutes(remainedTime)) R.color.error_on
            else if (helperFunctions.isProgressEnded(remainedTime)) R.color.stroke_secondary
            else R.color.warning_on

        breakStatusIndicator.apply {
            tvValue.text = remainedTime
            tvTitle.text = resources.getString(R.string._break)


            timelineIndicator.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    progressColor
                )
            )
            timelineIndicator.max =
                helperFunctions.calculateProgressInMinutes(Const.breakTime.divide(Const.divider))
            timelineIndicator.setProgress(
                helperFunctions.calculateProgressInMinutes(remainedTime),
                true
            )

            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.SB ||
                viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.OFF_DUTY
            ) {
                uiHelper.setActiveCircularProgressIndicator(breakStatusIndicator)
            } else {
                uiHelper.setInactiveCircularProgressIndicator(breakStatusIndicator)
            }
        }

    }

    private fun reLogin() {
        viewModelChangeStatus.reLogin()
    }

    private fun logout() {
        requireActivity().logout()

        viewModelChangeStatus.clearLocalDb()

    }

    private fun updateDriverInfo(driver: EntityDriver?) {
        if (driver == null) return

        val doc = if (driver.document.isNullOrEmpty()) "N/A" else driver.document
        val trailer = if (driver.trailerId.isNullOrEmpty()) "Bobtail" else driver.trailerId
        val note = if (driver.note.isNullOrEmpty()) "N/A" else driver.note

        binding.tvDocName.text = doc
        binding.tvTrailerName.text = trailer
        binding.tvNote.text = note
    }

    private inline fun collectAndDisplayStatus(
        flow: Flow<EntityLog?>, crossinline action: (EntityLog?) -> Unit
    ) {
        lifecycleScope.launch {
            flow.collect { dataStatus ->
                action(dataStatus)
            }
        }
    }

    private fun handleStatusButtons(statusShort: DutyStatus) {
        setInactiveAllStatusLayouts()

        when (statusShort) {
            DutyStatus.ON_DUTY -> {
                setActiveStatusLayout(
                    binding.layOnDuty, binding.tvOnDuty, binding.icOnDuty, R.color.success_on
                )
            }

            DutyStatus.OFF_DUTY -> {
                setActiveStatusLayout(
                    binding.layOffDuty,
                    binding.tvOffDuty,
                    binding.icOffDuty,
                    R.color.error_on
                )

            }

            DutyStatus.YM -> {
                setActiveStatusLayout(
                    binding.layYm, binding.tvYm, binding.icYm, R.color.status_ym_color
                )

            }

            DutyStatus.SB -> {
                setActiveStatusLayout(
                    binding.laySb, binding.tvSb, binding.icSb, R.color.status_sb_color
                )
            }

            DutyStatus.PC -> {
                setActiveStatusLayout(
                    binding.layPc, binding.tvPc, binding.icPc, R.color.status_pc_color
                )
            }

            else -> {

            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun initListeners() {
        binding.themeSwitch.setOnClickListener {
            createFakeLOg(DutyStatus.DR)
//            viewModelHome.changeTheme()
        }

        binding.icChartMode.setOnClickListener {
//            createFakeLOg(DutyStatus.DR)
            requireActivity().findNavController(R.id.fragment_container_main)
                .navigate(R.id.action_fragmentFlow_to_fragmentChartMode)

        }

        binding.icReset.setOnClickListener {
            lifecycleScope.launch {
                val dataStatus = DataLog().apply {
                    statusShort = DutyStatus.RESET
                    isDrivingTimeResetLog = true.toString()
                    location = "N/A"
                }


                viewModelChangeStatus.changeDutyStatus(dataStatus)
            }
        }
        binding.layOnDuty.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                it, binding.tvOnDuty, binding.icOnDuty, R.color.success_on
            )
            navigateToChangeStatusFragment(DutyStatus.ON_DUTY.ordinal)
        }

        binding.layOffDuty.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                it, binding.tvOffDuty, binding.icOffDuty, R.color.error_on
            )
            navigateToChangeStatusFragment(DutyStatus.OFF_DUTY.ordinal)
        }

        binding.layYm.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(it, binding.tvYm, binding.icYm, R.color.status_ym_color)
            navigateToChangeStatusFragment(DutyStatus.YM.ordinal)
        }

        binding.laySb.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(it, binding.tvSb, binding.icSb, R.color.status_sb_color)
            navigateToChangeStatusFragment(DutyStatus.SB.ordinal)
        }

        binding.layPc.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(it, binding.tvPc, binding.icPc, R.color.status_pc_color)
            navigateToChangeStatusFragment(DutyStatus.PC.ordinal)
        }

        binding.btnCertifyLogs.setOnClickListener {
            // Get today's date in yyyy-MM-dd format
            val currentDate = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = currentDate.format(dateFormatter)

            val action = FragmentFlowDirections.actionFragmentFlowToFragmentSingleCertifyLogs(
                date = formattedDate
            )
            requireActivity().findNavController(R.id.fragment_container_main).navigate(action)

            /*
            val bottomSheetMultipleCertification = BottomSheetMultipleCertification()
            bottomSheetMultipleCertification.show(
                parentFragmentManager, bottomSheetMultipleCertification.tag
            )
            */
        }

        //todo  FATAL EXCEPTION: main
        //                                                                                                    Process: com.selbiconsulting.elog, PID: 3167
        //                                                                                                    java.lang.IllegalArgumentException: Navigation action/destination com.selbiconsulting.elog:id/action_fragmentFlow_to_fragmentFullScreenGraph cannot be found from the current destination Destination(com.selbiconsulting.elog:id/fragmentFullScreenGraph) label=FragmentFullScreenGraph class=com.selbiconsulting.elog.ui.main.full_screen_graph.FragmentFullScreenGraph
        //                                                                                                    	at androidx.navigation.NavController.navigate(NavController.kt:1691)
        //                                                                                                    	at androidx.navigation.NavController.navigate(NavController.kt:1609)
        //                                                                                                    	at androidx.navigation.NavController.navigate(NavController.kt:1591)
        //                                                                                                    	at androidx.navigation.NavController.navigate(NavController.kt:1574)
        //                                                                                                    	at com.selbiconsulting.elog.ui.main.home.FragmentHome.initListeners$lambda$13(FragmentHome.kt:813)

        binding.btnConnectEld.setOnClickListener {
            bottomSheetScanBluetooth?.show(parentFragmentManager, bottomSheetScanBluetooth?.tag)
        }

        binding.graphView.setOnClickListener {
            requireActivity().findNavController(R.id.fragment_container_main)
                .navigate(R.id.action_fragmentFlow_to_fragmentFullScreenGraph)
        }

        binding.docsLay.setOnClickListener {
            val action = FragmentFlowDirections.actionFragmentFlowToFragmentUpdateMainInfo(
                binding.tvDocName.text.toString().trim(),
                binding.tvTrailerName.text.toString().trim(),
                binding.tvNote.text.toString().trim()
            )
            requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
        }
        binding.trailerLay.setOnClickListener {
            val action = FragmentFlowDirections.actionFragmentFlowToFragmentUpdateMainInfo(
                binding.tvDocName.text.toString().trim(),
                binding.tvTrailerName.text.toString().trim(),
                binding.tvNote.text.toString().trim()
            )
            requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
        }

        binding.notesLay.setOnClickListener {
            val action = FragmentFlowDirections.actionFragmentFlowToFragmentUpdateMainInfo(
                binding.tvDocName.text.toString().trim(),
                binding.tvTrailerName.text.toString().trim(),
                binding.tvNote.text.toString().trim()
            )
            requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
        }


    }

    private fun createFakeLOg(status: DutyStatus) {
        lifecycleScope.launch {
            val dataStatus = DataLog().apply {
                statusShort = status
                location = "N/A"
            }


            viewModelChangeStatus.changeDutyStatus(dataStatus)
        }
    }

    private fun setEldStatus(status: EldConnectionStatus) {
        when (status) {
            EldConnectionStatus.Connected -> {
                binding.tvEldStatus.text = resources.getString(R.string.eld_connected)
                binding.tvEldStatus.setTextColor(resources.getColor(R.color.success_on))
                binding.tvEldStatus.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.success_container)
                binding.tvEldStatus.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_egl_connected
                    ), null, null, null
                )
                binding.btnConnectEld.visibility = View.GONE
            }


            else -> {
                binding.tvEldStatus.text = resources.getString(R.string.eld_not_connected)
                binding.tvEldStatus.setTextColor(resources.getColor(R.color.error_on))
                binding.tvEldStatus.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.error_container)
                binding.tvEldStatus.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_disconnected_state
                    ), null, null, null
                )
                binding.btnConnectEld.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToChangeStatusFragment(statusOrder: Int) {
        val action = FragmentFlowDirections.actionFragmentFlowToFragmentChangeStatus(
            sharedViewModel.isPcAllowed.value ?: false,
            statusOrder
        )
        requireActivity().findNavController(R.id.fragment_container_main).navigate(action)
    }

    private fun setActiveStatusLayout(
        statusLayout: View, statusTV: TextView, statusIV: ImageView, @ColorRes statusColor: Int
    ) {
        statusLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), statusColor)
        statusTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//        statusIV.imageTintList = ContextCompat.getColorStateList(requireContext(), statusColor)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
    }

    private fun setInactiveAllStatusLayouts() {
        setInactiveStatusLayout(binding.layOnDuty, binding.tvOnDuty, binding.icOnDuty)
        setInactiveStatusLayout(binding.layOffDuty, binding.tvOffDuty, binding.icOffDuty)
        setInactiveStatusLayout(binding.layPc, binding.tvPc, binding.icPc)
        setInactiveStatusLayout(binding.layYm, binding.tvYm, binding.icYm)
        setInactiveStatusLayout(binding.laySb, binding.tvSb, binding.icSb)
    }

    private fun setInactiveStatusLayout(
        statusLayout: View, statusTV: TextView, statusIV: ImageView
    ) {
        statusLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.background)
        statusTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
//        statusIV.imageTintList =
//            ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.surface)
    }

    private fun setRecyclerLogs() {
        adapterLogs = AdapterLogs(requireContext(), this@FragmentHome)
        binding.recStatus.apply {
            layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, true
            )
            adapter = adapterLogs
        }
    }

    private fun setMainInfo() {
        val mainInfo = sharedViewModel.mainInfo.value ?: DtoMainInfo()
        val doc = if (mainInfo.docName.isNullOrEmpty()) "N/A" else mainInfo.docName
        val note = if (mainInfo.note.isNullOrEmpty()) "N/A" else mainInfo.note
        val trailer = if (mainInfo.trailerName.isNullOrEmpty()) "N/A" else mainInfo.trailerName

        binding.tvDocName.text = doc
        binding.tvTrailerName.text = trailer
        binding.tvNote.text = note
    }

    private fun setInactiveProgressIndicator(
        statusIndicatorItem: ItemCircularTiemlineVer2Binding?,
        remainingTime: String
    ) {
        statusIndicatorItem?.let {
            UiHelper(requireContext()).setInactiveCircularProgressIndicator(
                it
            )
        }

        val progressColor =
            if (helperFunctions.isLessThan30Minutes(remainingTime)) R.color.error_on
            else {
                when (statusIndicatorItem) {
                    binding.driveStatus -> R.color.success_on
                    binding.breakStatus -> R.color.warning_on
                    else -> R.color.primary_brand
                }
            }
        statusIndicatorItem?.timelineIndicator?.setIndicatorColor(
            ContextCompat.getColor(
                requireContext(),
                progressColor
            )
        )
    }


    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun updatePTIViolationMessage(state: ToastStates, message: String? = "") {
        val textColor: Int
        val containerColor: Int
        val startDrawable: Int

        if (state == ToastStates.SUCCESS) binding.ptiViolationFrame.visibility = View.GONE
        else binding.ptiViolationFrame.visibility = View.VISIBLE
        when (state) {
            ToastStates.SUCCESS -> {
                textColor = R.color.success_on
                containerColor = R.color.success_container
                startDrawable = R.drawable.shield_check
            }

            ToastStates.WARNING -> {
                textColor = R.color.warning_on
                containerColor = R.color.warning_container
                startDrawable = R.drawable.ic_warning
            }

            ToastStates.ERROR -> {
                textColor = R.color.error_on
                containerColor = R.color.error_container
                startDrawable = R.drawable.ic_alert
            }
        }

        binding.ptiViolationFrame.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), containerColor)
        binding.tvPtiViolation.setTextColor(
            ContextCompat.getColor(
                requireContext(), textColor
            )
        )
        binding.tvPtiViolation.compoundDrawableTintList =
            ContextCompat.getColorStateList(requireContext(), textColor)
        binding.tvPtiViolation.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                requireContext(), startDrawable
            ), null, null, null
        )
        binding.tvPtiViolation.text = message
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun updateViolationMessage(state: ToastStates, message: String? = "") {
        val textColor: Int
        val containerColor: Int
        val startDrawable: Int

        if (state == ToastStates.SUCCESS)
            binding.violationFrame.visibility = View.GONE
        else
            binding.violationFrame.visibility = View.VISIBLE

        when (state) {
            ToastStates.SUCCESS -> {
                textColor = R.color.success_on
                containerColor = R.color.success_container
                startDrawable = R.drawable.shield_check
            }

            ToastStates.WARNING -> {
                textColor = R.color.warning_on
                containerColor = R.color.warning_container
                startDrawable = R.drawable.ic_warning
            }

            ToastStates.ERROR -> {
                textColor = R.color.error_on
                containerColor = R.color.error_container
                startDrawable = R.drawable.ic_alert
            }
        }

        binding.violationFrame.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), containerColor)
        binding.tvViolation.setTextColor(
            ContextCompat.getColor(
                requireContext(), textColor
            )
        )
        binding.tvViolation.compoundDrawableTintList =
            ContextCompat.getColorStateList(requireContext(), textColor)
        binding.tvViolation.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                requireContext(), startDrawable
            ), null, null, null
        )
        binding.tvViolation.text = message
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun updateSmallBreakViolationMessage(state: ToastStates, message: String? = "") {
        val textColor: Int
        val containerColor: Int
        val startDrawable: Int

        if (state == ToastStates.SUCCESS)
            binding.smallBreakViolationFrame?.visibility = View.GONE
        else
            binding.smallBreakViolationFrame?.visibility = View.VISIBLE

        when (state) {
            ToastStates.SUCCESS -> {
                textColor = R.color.success_on
                containerColor = R.color.success_container
                startDrawable = R.drawable.shield_check
            }

            ToastStates.WARNING -> {
                textColor = R.color.warning_on
                containerColor = R.color.warning_container
                startDrawable = R.drawable.ic_warning
            }

            ToastStates.ERROR -> {
                textColor = R.color.error_on
                containerColor = R.color.error_container
                startDrawable = R.drawable.ic_alert
            }
        }

        binding.smallBreakViolationFrame.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), containerColor)
        binding.tvSmallBreakViolation.setTextColor(
            ContextCompat.getColor(
                requireContext(), textColor
            )
        )
        binding.tvSmallBreakViolation.compoundDrawableTintList =
            ContextCompat.getColorStateList(requireContext(), textColor)
        binding.tvSmallBreakViolation.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                requireContext(), startDrawable
            ), null, null, null
        )
        binding.tvSmallBreakViolation.text = message
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun updateDriveViolationMessage(state: ToastStates, message: String? = "") {
        val textColor: Int
        val containerColor: Int
        val startDrawable: Int

        if (state == ToastStates.SUCCESS)
            binding.driveViolationFrame.visibility = View.GONE
        else
            binding.driveViolationFrame.visibility = View.VISIBLE

        when (state) {
            ToastStates.SUCCESS -> {
                textColor = R.color.success_on
                containerColor = R.color.success_container
                startDrawable = R.drawable.shield_check
            }

            ToastStates.WARNING -> {
                textColor = R.color.warning_on
                containerColor = R.color.warning_container
                startDrawable = R.drawable.ic_warning
            }

            ToastStates.ERROR -> {
                textColor = R.color.error_on
                containerColor = R.color.error_container
                startDrawable = R.drawable.ic_alert
            }
        }

        binding.driveViolationFrame.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), containerColor)
        binding.tvDriveViolation.setTextColor(
            ContextCompat.getColor(
                requireContext(), textColor
            )
        )
        binding.tvDriveViolation.compoundDrawableTintList =
            ContextCompat.getColorStateList(requireContext(), textColor)
        binding.tvDriveViolation.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(
                requireContext(), startDrawable
            ), null, null, null
        )
        binding.tvDriveViolation.text = message
    }

    private fun addViolationToCurrentLog(violation: String) {
        viewModelChangeStatus.addViolationToCurrentLog(violation)
    }

    override fun onBluetoothDeviceItemClick(get: EldScanObject?) {
        viewModelChangeStatus.eldStatus.value = EldConnectionStatus.Connected

        val strDevice = get?.deviceId

        val res: EldBleError = mEldManager.ConnectToEld(
            bleDataCallback, subscribedRecords, bleConnectionStateChangeCallback, strDevice
        )

        if (res != EldBleError.SUCCESS) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "CONNECTION FAILED", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private val bleConnectionStateChangeCallback: EldBleConnectionStateChangeCallback =
        object : EldBleConnectionStateChangeCallback() {
            override fun onConnectionStateChange(newState: Int) {
                requireActivity().runOnUiThread {
                    when (newState) {
                        0 -> {
                            Toast.makeText(
                                requireContext(), "DISCONNECTED FROM ELD", Toast.LENGTH_SHORT


                            ).show()
                            lifecycleScope.launch {
                                viewModelChangeStatus.eldStatus.emit(EldConnectionStatus.NotConnected)
                                sharedPreferences.isEldConnected = false
                            }
                        }

                        2 -> {
                            Toast.makeText(
                                requireContext(),
                                "CONNECTED TO ELD: " + mEldManager.EldGetSerial(),
                                Toast.LENGTH_SHORT
                            ).show()

                            lifecycleScope.launch {
                                viewModelChangeStatus.eldStatus.emit(EldConnectionStatus.Connected)
                                sharedPreferences.isEldConnected = true
                            }
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(), "New State of connection " + newState.toString(
                                    10
                                ) + "\n", Toast.LENGTH_SHORT
                            ).show()
                            sharedPreferences.isEldConnected = false

                        }
                    }
                }
            }
        }

    private val bleDataCallback: EldBleDataCallback = object : EldBleDataCallback() {
        override fun OnDataRecord(dataRec: EldBroadcast, recordType: EldBroadcastTypes) {
            autoThread.pushData(dataRec)

            if (dataRec is EldBufferRecord) {

            } else if (recordType == EldBroadcastTypes.ELD_DATA_RECORD) {

                val rpm = (dataRec as EldDataRecord).rpm

                val speed = dataRec.speed ?: 0.0
                val longitude = dataRec.longitude ?: 0.0
                val latitude = dataRec.latitude ?: 0.0
                val course = dataRec.course ?: 0
                val odometer = dataRec.odometer ?: 0.0
                val odometerMile = odometer / 1.6
                val engineHours = dataRec.engineHours ?: 0.0

                sharedPreferences.odometer = odometerMile.toString()
                sharedPreferences.engineHours = engineHours.toString()

                handleData(
                    latitude,
                    longitude,
                    course,
                    speed,
                    odometerMile, // Pass the current odometer reading to handleData
                    engineHours
                )

            }
        }
    }

    private fun handleData(
        latitude: Double?,
        longitude: Double?,
        course: Int?,
        speed: Double,
        currentOdometer: Double,
        engineHours: Double
    ) {
        val dataStatusCurrent = viewModelChangeStatus.currentDutyStatus.value

        sharedPreferences.lastLatitude = latitude?.toFloat() ?: 0f
        sharedPreferences.lastLongitude = longitude?.toFloat() ?: 0f

        Log.e(
            "eld_receiving_data",
            "handleData: latitude: $latitude, " + "\nlongitude: $longitude," + "\ncourse: $course," + "\nspeed: $speed," + "\ncurrentOdometerMile: $currentOdometer," + "\nengineHours: $engineHours," + "\ncurrentLog: ${dataStatusCurrent?.status}"
        )

        try {
            // Calculate the distance traveled since the last update
            val distanceTraveled = currentOdometer - previousOdometerReading

            // Update the accumulator with the distance traveled
            accumulatedDistance += distanceTraveled

            // Check if the accumulated distance is 1 mile or more
            if (accumulatedDistance >= 0.10) {
                postLocation(latitude, longitude, course ?: 0, speed)
                // Reset the accumulator
                accumulatedDistance = 0.0
            }

            // Update the previous odometer reading to the current reading
            previousOdometerReading = currentOdometer

            val lat = latitude ?: 0.0
            val lon = longitude ?: 0.0
            val currCourse = course ?: 0

            fun updateDutyStatus(status: DutyStatus) {
                val dataStatus = DataLog().apply {
                    statusShort = status
                    location =
                        viewModelChangeStatus.getLocationName(LatLng(lat, lon), requireContext())
                }

                viewModelChangeStatus.changeDutyStatus(dataStatus)
            }

            if (speed > 5) {
                addLocation(lat, lon, currCourse, speed)
                if (dataStatusCurrent?.status != DutyStatus.DR && !isStatusChanging) {
                    isStatusChanging = true
                    updateDutyStatus(DutyStatus.DR)
                    time = System.currentTimeMillis()
                    requireActivity().findNavController(R.id.fragment_container_main)
                        .navigate(R.id.action_fragmentFlow_to_fragmentChartMode)

                } else if (dataStatusCurrent?.status == DutyStatus.DR) {
                    isStatusChanging = false
                }

            } else {
                if (dataStatusCurrent?.status == DutyStatus.DR && getTimeDifference(time) && !isStatusChanging) {
                    isStatusChanging = true
                    postLocation(lat, lon, currCourse, speed)
                    updateDutyStatus(DutyStatus.ON_DUTY)

                    requireActivity().onBackPressed()

                } else if (dataStatusCurrent?.status == DutyStatus.ON_DUTY) {
                    isStatusChanging = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun addLocation(
        latitude: Double?, longitude: Double?, degree: Int, speed: Double
    ) {
        if (speed == 0.0 || (latitude == 0.0 && longitude == 0.0)) return

        viewModelChangeStatus.addLocation(
            latitude ?: 0.0, longitude ?: 0.0, degree = degree.toString(), speed.toString()
        )
    }

    private fun postLocation(
        latitude: Double?, longitude: Double?, degree: Int, speed: Double
    ) {
        //i need to get if odometer 14.0 or x.0 can cast it
        if (speed == 0.0 || (latitude == 0.0 && longitude == 0.0)) return
        lifecycleScope.launch {
            viewModelChangeStatus.postLocation(
                latitude ?: 0.0,
                longitude ?: 0.0,
                degree = degree.toString(),
                speed.toString(),
                requireContext()
            )
        }
    }

    private fun getTimeDifference(time: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val difference = currentTime - time
        val halfMinuteInMillis = 30_000 // 0.5 minutes in milliseconds
        return difference > halfMinuteInMillis
    }

    override fun onLogsItemClicked(currentData: DataLog) {
        val bottomSheetLogDetails = BottomSheetLogDetails(requireContext(), currentData)
        bottomSheetLogDetails.show(childFragmentManager, bottomSheetLogDetails.tag)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BT_ENABLE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(
                    requireContext(),
                    "Bluetooth enabled - now scanning for ELD\n",
                    Toast.LENGTH_SHORT
                ).show()
                mEldManager.ScanForElds(bleScanCallback)
            } else {
                Toast.makeText(
                    requireContext(), "Unable to enable bluetooth\n", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    inner class AutoThread : Thread() {
        private val dataQueue: BlockingQueue<EldBroadcast> = LinkedBlockingQueue(10)
        fun pushData(dataRec: EldBroadcast) {
            if (automodeenabled) {
                dataQueue.add(dataRec)
            }
        }

        override fun run() {
            while (true) {
                var dataRec: EldBroadcast? = null
                try {
                    dataRec = dataQueue.take()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                // automatically request and delete any records that come in
                if (dataRec is EldBufferRecord && automodeenabled) {
                    // this will send every 10 seconds. we recommend 15-20, but this is easier
                    mEldManager.ResetBleWatchdog()
                    if (!autoinprogress) {
                        reqstart = dataRec.startSeqNo
                        reqend = (reqstart + automoderequestsize - 1).coerceAtMost(dataRec.endSeqNo)
                        if (reqstart != -1 && reqend != -1) {
                            Log.d("TESTING", "auto request $reqstart - $reqend")
                            if (mEldManager.RequestRecord(
                                    reqstart, reqend
                                ) == EldBleError.SUCCESS
                            ) {
                                // wait for records to be received
                                autoinprogress = true
                            }
                        }
                    }
                }
            }
        }
    }

}
