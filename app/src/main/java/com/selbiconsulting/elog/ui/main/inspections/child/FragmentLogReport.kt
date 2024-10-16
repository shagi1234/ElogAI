package com.selbiconsulting.elog.ui.main.inspections.child

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.selbiconsulting.elog.data.model.dto.DtoDate
import com.selbiconsulting.elog.data.model.entity.EntityDriver
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentLogReportBinding
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.fromEntityLogToDataLogs
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog.Companion.toDataLogs
import com.selbiconsulting.elog.ui.main.logs.ViewModelLogs
import com.selbiconsulting.elog.ui.main.logs_page.AdapterDateLogs
import com.selbiconsulting.elog.ui.main.logs_page.DateItemListener
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.utils.io.concurrent.shared
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class FragmentLogReport : Fragment(), DateItemListener {
    private lateinit var b: FragmentLogReportBinding
    private lateinit var adapterDateLogs: AdapterDateLogs
    private lateinit var adapterDailyLogs: AdapterDailyLogs
    private val viewModelChangeStatus by activityViewModels<ViewModelChangeStatus>()
    private val viewModelLogs by activityViewModels<ViewModelLogs>()
    private lateinit var graphViewHelper: GraphViewHelper
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val logsList = mutableListOf<File>()


    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelChangeStatus.getDriverDataLocal()

        sharedViewModel.logsPdfFiles.value = emptyList()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentLogReportBinding.inflate(inflater, container, false)
        graphViewHelper = GraphViewHelper(
            requireContext(),
            b.graphView
        )
        setRecyclerDates()
        setRecyclerDailyLogs()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        getDates()
        observe()


//        Log.e("PDF_VIEW", "onViewCreated: ${pdfFile?.absolutePath}", )
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModelLogs.daysIsExistLog.collect { days ->
                days.sortBy { it.formattedDate }
                withContext(Dispatchers.Main) {
                    adapterDateLogs.dates = days
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.driverData.collect {
                setDriverInfo(it)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            viewModelLogs.selectedDate.collect { updatedDate ->
                b.driverInfoPdfView.tvLogDate.text = updatedDate.formattedDate
                lifecycleScope.launch {
                    if (updatedDate.position < viewModelLogs.daysIsExistLog.value.size &&
                        updatedDate.isCertified
                    ) {
                        viewModelLogs.daysIsExistLog.value[updatedDate.position].isCertified = true
                        sharedPreferencesHelper.certifiedDate = updatedDate.formattedDate

                        withContext(Dispatchers.Main) {
                            adapterDateLogs.notifyItemChanged(updatedDate.position)
                        }

                    }

                    delay(500)
                    val logsPdf =
                        HelperFunctions.shared.createPdfFromView(requireContext(), b.mainView)
                    Log.e("PDF_FILES", "log_report: $logsPdf")
                    logsPdf?.let { logsList.add(it) }

                    sharedViewModel.logsPdfFiles.value = logsList
                }

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

                adapterDailyLogs.logs = dailyLogs

            }
        }
    }

    private fun setRecyclerDailyLogs() {
        adapterDailyLogs = AdapterDailyLogs(
            context = requireContext(),
            vehicle = sharedPreferencesHelper.vehicle.toString()
        )
        b.rvDailyLog.apply {
            adapter = adapterDailyLogs
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getDates() {
        lifecycleScope.launch {
            val dates = mutableListOf<DtoDate>()

            // Get today's date
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Formatter for day of the week
            val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

            // Formatter for day of the month
            val dayOfMonthFormatter = DateTimeFormatter.ofPattern("MMM d")


            // Get the last 8 days
            for (i in 0 until 8) {
                val date = today.minusDays(i.toLong())
                val dayOfWeek = date.format(dayOfWeekFormatter)
                val dayOfMonth = date.format(dayOfMonthFormatter)
                val dateReal = date.format(dateFormatter)

                dates.add(
                    DtoDate(
                        dayOfWeek,
                        dayOfMonth,
                        dateReal,
                        position = i,
                        isCertified = viewModelLogs.checkDailyLogsIsCertified(dateReal)
                    )
                )
            }

            viewModelLogs.checkThisDateExistLogs(dates.reversed())
        }
    }

    private fun setDriverInfo(driver: EntityDriver?) {
        if (driver == null) return

        val driverInfoView = b.driverInfoPdfView

        driverInfoView.tvDriverName.text = driver.name
        driverInfoView.tvDriverId.text = driver.driverId
        driverInfoView.tvCoDriverId.text = driver.co_driver
        driverInfoView.tvDlNumber.text = "123456789"
        driverInfoView.tvTimeZone.text = "GMT +5"
        driverInfoView.tvEldId.text = "123456789"
        driverInfoView.tvTimeZoneOffset.text = ""
        driverInfoView.tvEldProvide.text = "Some provider"
        driverInfoView.tvCarrier.text = driver.carrier
        driverInfoView.tvVehiclesVin.text = driver.vehicleId
        driverInfoView.tvUsdot.text = "usdot"
        driverInfoView.tvMainOffice.text = driver.mainTerminal
        driverInfoView.tvHomeTerminal.text = driver.mainTerminal
        driverInfoView.tvDistance.text = "44555"
        driverInfoView.tvTrailer.text = driver.trailerId
        driverInfoView.tvShippingDocs.text = "12345678"
        driverInfoView.tvMalfunctionIndicators.text = "some indicator"
        driverInfoView.tvDataDiagnosticIndicators.text = "some indicator"
        driverInfoView.tvDriverState.text = "STATE"
        driverInfoView.tvCurrentLocation.text = sharedPreferencesHelper.lastLocation
        driverInfoView.tvUnidentifiedDriverRecords.text = "some records"
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
    }


    private fun setRecyclerDates() {
        adapterDateLogs = AdapterDateLogs(requireContext(), this)
        b.recViewDates.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = adapterDateLogs
            if (adapterDateLogs.itemCount == 0) return
            scrollToPosition(adapterDateLogs.itemCount - 1)
        }

    }

    override fun onDateClicked(date: DtoDate) {
        viewModelLogs.setDate(date)
        Log.e("UNFORMATTED_DATE", "onDateClicked: $date")
    }
}