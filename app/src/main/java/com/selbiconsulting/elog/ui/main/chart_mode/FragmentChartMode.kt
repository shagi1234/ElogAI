package com.selbiconsulting.elog.ui.main.chart_mode

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.databinding.FragmentChartModeBinding
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.main.common.EldConnectionStatus
import com.selbiconsulting.elog.ui.main.home.ViewModelHome
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class FragmentChartMode : Fragment() {
    private lateinit var b: FragmentChartModeBinding

    private lateinit var uiHelper: UiHelper
    private val viewModel: ViewModelHome by viewModels()
    private val viewModelChangeStatus: ViewModelChangeStatus by activityViewModels()


    private val getCurrentTimeJob = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentChartModeBinding.inflate(inflater, container, false)
        uiHelper = UiHelper(requireContext())
        viewModelChangeStatus.calculateTimeByStatuses()
        viewModelChangeStatus.resumeShift()

        changeUI()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        observeTimeByStatus()
        initListeners()

    }

    private fun observeTimeByStatus() {
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeOff.collect {
                timeIndicatorBreak()
            }
        }
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeSB.collect {
                timeIndicatorBreak()
            }
        }
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeOn.collect {
                timeIndicatorShift()
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.timerValue.observe(viewLifecycleOwner) {
                viewModelChangeStatus.calculateTimeByStatuses()

            }
        }
        lifecycleScope.launch {
            viewModelChangeStatus.totalTimeDR.collect {
                Log.e("DRIVE_TIME", "CHART: $it")
                setDriveTimeIndicator()
            }
        }

    }


    private fun observe() {
        lifecycleScope.launch {
            viewModelChangeStatus.currentDutyStatus.collect { currentStatus ->
                if (currentStatus == null) return@collect
                timeIndicatorBreak()
                setDriveTimeIndicator()
                timeIndicatorShift()
            }
        }

        viewModelChangeStatus.timerValue.observe(viewLifecycleOwner) { time ->
            timeIndicatorBreak()
            setDriveTimeIndicator()
            timeIndicatorShift()
        }


        lifecycleScope.launch {
            viewModelChangeStatus.eldStatus.collect {
                setEldStatus(it)
            }
        }
    }

    fun getCurrentTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun initListeners() {
        b.icBack.setOnClickListener {
//            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.DR) return@setOnClickListener
            findNavController().navigateUp()
        }

        b.themeSwitch.setOnClickListener {
            viewModel.changeTheme()
        }
    }

    private fun changeUI() {
        val windowHeight = uiHelper.getWindowHeight()
        val orientation = requireContext().resources.configuration.orientation
        val progressIndicatorSize =
            if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                ((uiHelper.pxToDp(windowHeight.toFloat()).toInt() - 48) / 3)
            else ((uiHelper.pxToDp(windowHeight.toFloat()).toInt() + 120) / 6)

        b.driveProgressIndicator.indicatorSize = uiHelper.dpToPx(progressIndicatorSize)
        b.shiftProgressIndicator.indicatorSize = uiHelper.dpToPx(progressIndicatorSize)
        b.breakProgressIndicator.indicatorSize = uiHelper.dpToPx(progressIndicatorSize)

        timeIndicatorBreak()
        setDriveTimeIndicator()
        timeIndicatorShift()

        lifecycleScope.launch {
            while (isActive) {
                b.tvTime.text = getCurrentTimeFormatted()
                delay(60 * 1000) // Update every minute
            }
        }


    }

    private fun setActiveProgressIndicator(view: View) {
        val orientation = requireContext().resources.configuration.orientation
        val horizontalMargin =
            if (uiHelper.isTablet() || orientation == Configuration.ORIENTATION_LANDSCAPE)
                (view.width / 4 + uiHelper.dpToPx(8)) else 0
        val verticalMargin = (view.width / 4 + uiHelper.dpToPx(8))
        view.animate().setDuration(300).scaleY(1.5f)
        view.animate().setDuration(300).scaleX(1.5f).withStartAction {
            uiHelper.setMarginsWithAnim(
                view,
                horizontalMargin,
                verticalMargin,
                horizontalMargin,
                verticalMargin
            )
        }
    }

    private fun setInactiveProgressIndicator(view: View) {
        val orientation = requireContext().resources.configuration.orientation
        val horizontalMargin =
            if (uiHelper.isTablet() || orientation == Configuration.ORIENTATION_LANDSCAPE) uiHelper.dpToPx(
                8
            ) else 0
        val verticalMargin =
            if (uiHelper.isTablet() || orientation == Configuration.ORIENTATION_LANDSCAPE) 0 else uiHelper.dpToPx(
                8
            )
        view.animate().setDuration(300).scaleY(1f)
        view.animate().setDuration(300).scaleX(1f).withStartAction {
            uiHelper.setMarginsWithAnim(
                view,
                horizontalMargin,
                verticalMargin,
                horizontalMargin,
                verticalMargin
            )
        }

//        val indicator = (view as FrameLayout).getChildAt(0) as CircularProgressIndicator
//        indicator.progress = 0
    }

    private fun setDriveTimeIndicator() {

        val remainedTime =
            if (checkDriveTimeMoreThanShift()) viewModelChangeStatus.calculateShiftTime() else viewModelChangeStatus.calculateDriveTime()

        val progressColor =
            if (HelperFunctions.shared.isLessThan30Minutes(remainedTime)) R.color.error_on
            else if (HelperFunctions.shared.isProgressEnded(remainedTime)) R.color.stroke_secondary
            else R.color.success_on
        b.apply {
            drivingTimeRemaining.text = remainedTime
            driveProgressIndicator.setIndicatorColor(
                ContextCompat.getColor(
                    requireContext(),
                    progressColor
                )
            )
            b.driveProgressIndicator.max =
                HelperFunctions.shared.calculateProgressInMinutes(Const.driveTime.divide(Const.divider))
            b.driveProgressIndicator.setProgress(
                HelperFunctions.shared.calculateProgressInMinutes(remainedTime),
                true
            )

//            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.DR) {
//                lifecycleScope.launch {
//                    delay(1000)
//                    setActiveProgressIndicator(b.driveProgressLay)
//                }
//            } else {
//                setInactiveProgressIndicator(b.driveProgressLay)
//            }

        }
    }

    private fun checkDriveTimeMoreThanShift(): Boolean {
        return HelperFunctions.shared.convertToMinutes(viewModelChangeStatus.calculateDriveTime()) >= HelperFunctions.shared.convertToMinutes(
            viewModelChangeStatus.calculateShiftTime()
        )
    }

    private fun timeIndicatorShift() {

        val currentStatusRemainingTime = viewModelChangeStatus.calculateShiftTime()

        b.apply {
            shiftTimeRemaining.text = currentStatusRemainingTime
            shiftProgressIndicator.max = calculateProgress("14:00")
            shiftProgressIndicator.setProgress(
                calculateProgress(currentStatusRemainingTime), true
            )
            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.ON_DUTY) {

                shiftProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        updateStatusViolation(
                            shiftProgressIndicator.progress,
                            shiftProgressIndicator.max
                        )
                    )
                )
            } else {
                setInactiveProgressIndicator(b.shiftProgressLay)
            }

        }
    }

    private fun timeIndicatorBreak() {
        val currentStatusRemainingTime = viewModelChangeStatus.calculateBreakTime()

        b.apply {
            breakTimeRemaining.text = currentStatusRemainingTime
            breakProgressIndicator.max = calculateProgress("3:00")
            breakProgressIndicator.setProgress(
                calculateProgress(currentStatusRemainingTime), true
            )

            if (viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.SB ||
                viewModelChangeStatus.currentDutyStatus.value?.status == DutyStatus.OFF_DUTY
            ) {

                breakProgressIndicator.setIndicatorColor(
                    ContextCompat.getColor(
                        requireContext(),
                        updateStatusViolation(
                            breakProgressIndicator.progress,
                            breakProgressIndicator.max
                        )
                    )
                )
            } else {
                setInactiveProgressIndicator(b.breakProgressLay)
            }
        }

    }

    private fun calculateProgress(time: String): Int {
        val parts = time.split(":")
        val hours = parts[0].toLongOrNull() ?: 0
        val minutes = parts[1].toLongOrNull() ?: 0
        return (hours * 60 + minutes).toInt()
    }

    private fun setEldStatus(status: EldConnectionStatus) {
        when (status) {
            EldConnectionStatus.Connected -> {
                b.tvEldStatus.text = resources.getString(R.string.eld_connected)
                b.tvEldStatus.setTextColor(resources.getColor(R.color.success_on))
                b.tvEldStatus.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.success_container)
                b.tvEldStatus.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_egl_connected
                    ), null, null, null
                )
            }


            else -> {
                b.tvEldStatus.text = resources.getString(R.string.eld_not_connected)
                b.tvEldStatus.setTextColor(resources.getColor(R.color.error_on))
                b.tvEldStatus.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.error_container)
                b.tvEldStatus.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_disconnected_state
                    ), null, null, null
                )
            }
        }
    }


    private fun updateStatusViolation(
        progress: Int,
        max: Int,
    ): Int {
        var color = R.color.success_on

        if (progress == 0) {
            color = R.color.error_on
        } else if ((progress.toDouble() / max.toDouble()) * 100 <= 30) {
            color = R.color.warning_on
        }

        return color
    }

    override fun onDestroy() {
        super.onDestroy()
        getCurrentTimeJob.cancel()
    }


}