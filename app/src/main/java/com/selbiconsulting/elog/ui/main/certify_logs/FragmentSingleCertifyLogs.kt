package com.selbiconsulting.elog.ui.main.certify_logs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentSingleDayCertifyLogBinding
import com.selbiconsulting.elog.domain.use_case.DateEntry
import com.selbiconsulting.elog.domain.use_case.RequestCertifyLogs
import com.selbiconsulting.elog.ui.main.certify_logs.components.BottomSheetCertifyLogs
import com.selbiconsulting.elog.ui.main.certify_logs.components.BottomSheetCertifyLogs.Companion.OPENED_FROM_SINGLE_LOG
import com.selbiconsulting.elog.ui.main.certify_logs.components.BottomSheetSuccessModal
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.fromEntityLogToDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toDataLogs
import com.selbiconsulting.elog.ui.main.inspections.child.AdapterDailyLogs
import com.selbiconsulting.elog.ui.main.logs.ViewModelLogs
import com.selbiconsulting.elog.ui.util.HelperFunctions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FragmentSingleCertifyLogs : Fragment(), OnCertifyLogsClicked {

    private var bottomSheetCertifyLogs: BottomSheetCertifyLogs? = null
    private lateinit var b: FragmentSingleDayCertifyLogBinding
    private val viewModelCertifyLogs: ViewModelCertifyLogs by viewModels()
    private val viewModelLogs: ViewModelLogs by viewModels()
    private lateinit var adapterDailyLogs: AdapterDailyLogs
    private  var formattedDate =""

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    private val args by navArgs<FragmentSingleCertifyLogsArgs>()

    private lateinit var graphViewHelper: GraphViewHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelCertifyLogs.getDriverDataLocal()

        val dateString = args.date
        val date = LocalDate.parse(dateString)

        // Get day of the week
        val dayOfWeek = date.dayOfWeek
        val dayOfWeekShort = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH)

        // Get formatted date
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
         formattedDate = date.format(dateFormatter)
        val selectedDate = DtoDate(
            dayOfWeek = dayOfWeekShort,
            day = formattedDate,
            formattedDate = args.date
        )
        Log.e("UNFORMATTED_DATE", "onCreate: $selectedDate", )
        viewModelLogs.setDate(selectedDate)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentSingleDayCertifyLogBinding.inflate(inflater, container, false)
        graphViewHelper = GraphViewHelper(
            requireContext(),
            b.graphView
        )
        b.driverInfoPdfView.tvLogDate.text = args.date
        b.headerTitle.text = formattedDate
        setRecyclerDailyLogs()
        return b.root
    }

    private fun setRecyclerDailyLogs() {
        adapterDailyLogs = AdapterDailyLogs(requireContext(), sharedPreferences.vehicle.toString())
        b.rvDailyLog.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterDailyLogs
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()

    }

    private fun observe() {
        viewModelCertifyLogs.driverSignatureFileState.observe(viewLifecycleOwner) { driverSignature ->
            when (driverSignature) {
                is Resource.Error -> {

                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Failure -> {

                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    handleUploadSignatureSuccess(driverSignature)
                }
            }

        }

        viewModelCertifyLogs.certifyLogsState.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error -> {
                    bottomSheetCertifyLogs?.dismiss()
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )

                }

                is Resource.Failure -> {
                    bottomSheetCertifyLogs?.dismiss()
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )

                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    bottomSheetCertifyLogs?.dismiss()

                    val bottomSheetSuccessModal =
                        BottomSheetSuccessModal(
                            openedFrom = OPENED_FROM_SINGLE_LOG,
                            sharedPreferences
                        )
                    bottomSheetSuccessModal.show(parentFragmentManager, bottomSheetSuccessModal.tag)

                }
            }
        }

        lifecycleScope.launch {
            viewModelCertifyLogs.driverData.collect {
                setDriverInfo(it)
            }
        }

        lifecycleScope.launch {
            viewModelLogs.seriesLiveData.observe(viewLifecycleOwner) { series ->
                graphViewHelper.removeAllSeries()
                graphViewHelper.setSeriesSolid(series)
            }
        }

        lifecycleScope.launch {
            viewModelLogs.dailyLogs.collect { dailyLogs ->
                if (dailyLogs.isEmpty()) return@collect

                val dataLogs = dailyLogs.fromEntityLogToDataLogs()
                b.driverInfoPdfView.tvCurrentLocation.text = dailyLogs.last().location
                viewModelLogs.getLineGraphSeries(dataLogs)

                Log.e("DAILY_LOGS", "observe: $dailyLogs", )

                adapterDailyLogs.logs = dailyLogs

            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModelLogs.selectedDate.collect { updatedDate ->
                b.driverInfoPdfView.tvLogDate.text = updatedDate.formattedDate
                if (updatedDate.position < viewModelLogs.daysIsExistLog.value.size &&
                    updatedDate.isCertified
                ) {
                    viewModelLogs.daysIsExistLog.value[updatedDate.position].isCertified = true
                    sharedPreferences.certifiedDate = updatedDate.formattedDate
                    viewModelLogs.daysIsExistLog.value[updatedDate.position].isCertified = true

                }

            }
        }
    }

    private fun handleUploadSignatureSuccess(driverSignature: Resource.Success<ResponseUploadFile>) {

        viewModelCertifyLogs.saveDriverSignatureIdToSharedPref(
            driverSignature.data?.id ?: ""
        )
        //yyyy-MM-dd
        val startOfDay = formatStartOfDayUTC(args.date)
        val endOfDay = formatEndOfDayUTC(args.date)

        val dateEntry = DateEntry(
            startOfDay ?: "",
            endOfDay ?: ""
        )

        val requestCertifyLogs = RequestCertifyLogs(
            sharedPreferences.contactId ?: "",
            driverSignature.data?.id ?: "",
            mutableListOf(dateEntry)
        )

        val bottomSheetSuccessModal =
            BottomSheetSuccessModal(openedFrom = OPENED_FROM_SINGLE_LOG, sharedPreferences)
        bottomSheetSuccessModal.show(parentFragmentManager, bottomSheetSuccessModal.tag)


        onCertified(requestCertifyLogs, args.date)
    }

    private fun setDriverInfo(driver: EntityDriver?) {
        if (driver == null) return

        val driverInfoView = b.driverInfoPdfView

        driverInfoView.tvDriverName.text = driver.name
        driverInfoView.tvDriverId.text = driver.driverId
        driverInfoView.tvCoDriverId.text = driver.co_driver
        driverInfoView.tvDlNumber.text = "???"
        driverInfoView.tvTimeZone.text = "???"
        driverInfoView.tvEldId.text = "???"
        driverInfoView.tvTimeZoneOffset.text = "???"
        driverInfoView.tvEldProvide.text = "???"
        driverInfoView.tvCarrier.text = driver.carrier
        driverInfoView.tvVehiclesVin.text = driver.vehicleId
        driverInfoView.tvUsdot.text = "???"
        driverInfoView.tvMainOffice.text = driver.mainTerminal
        driverInfoView.tvHomeTerminal.text = driver.mainTerminal
        driverInfoView.tvDistance.text = "???"
        driverInfoView.tvTrailer.text = driver.trailerId
        driverInfoView.tvShippingDocs.text = "???"
        driverInfoView.tvMalfunctionIndicators.text = "???"
        driverInfoView.tvDataDiagnosticIndicators.text = "???"
        driverInfoView.tvDriverState.text = "STATE"
        driverInfoView.tvCurrentLocation.text = sharedPreferences.lastLocation
        driverInfoView.tvUnidentifiedDriverRecords.text = "???"
    }


    private fun initListeners() {
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.btnCertifyLogs.setOnClickListener {
            openBottomSheetCertifyLogs()
        }
    }

    private fun openBottomSheetCertifyLogs() {
        bottomSheetCertifyLogs =
            BottomSheetCertifyLogs(this/*openedFrom = OPENED_FROM_SINGLE_LOG*/)
        bottomSheetCertifyLogs?.show(parentFragmentManager, bottomSheetCertifyLogs?.tag)
    }

    private fun onCertified(requestCertifyLogs: RequestCertifyLogs, date: String) {
        viewModelCertifyLogs.certifyLogs(requestCertifyLogs, date)
    }

    override fun uploadSignature(requestUploadFile: RequestUploadFile) {
        viewModelCertifyLogs.uploadDriverSignature(requestUploadFile)
    }

    private fun formatStartOfDayUTC(dateStr: String): String? {
        return try {
            // Parse the input date string
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)

            // Start of the day: set time to 00:00:00
            val startOfDay = LocalDateTime.of(date, LocalTime.MIN)

            // Format the start of the day to desired format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            formatter.format(startOfDay.atZone(ZoneOffset.UTC).withNano(0))
        } catch (e: Exception) {
            // Handle parsing exceptions or invalid date format
            e.printStackTrace()
            null
        }
    }

    private fun formatEndOfDayUTC(dateStr: String): String? {
        return try {
            // Parse the input date string
            val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)

            // End of the day: set time to 23:59:59.999 and adjust nanoseconds to zero
            val endOfDay = LocalDateTime.of(date, LocalTime.MAX).withNano(0)

            // Format the end of the day to desired format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            formatter.format(endOfDay.atZone(ZoneOffset.UTC))
        } catch (e: Exception) {
            // Handle parsing exceptions or invalid date format
            e.printStackTrace()
            null
        }
    }

}

interface OnCertifyLogsClicked {
    fun uploadSignature(requestUploadFile: RequestUploadFile)
}

fun String.adjustByTimezoneUTC(): String? {
    try {
        // Parse the input string to a ZonedDateTime object (considering it's in ISO8601 format and UTC)
        val zonedDateTime = ZonedDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC")))

        // Get the current timezone of the device
        val deviceTimezone = ZoneId.systemDefault()

        // Convert the UTC datetime to the device's timezone
        val deviceZonedDateTime = zonedDateTime.withZoneSameInstant(deviceTimezone)

        // Format the new date back to ISO8601 format with timezone information
        return DateTimeFormatter.ISO_DATE_TIME.withZone(deviceTimezone).format(deviceZonedDateTime)
    } catch (e: DateTimeParseException) {
        // Handle parsing exceptions
        e.printStackTrace()
        return null
    }
}