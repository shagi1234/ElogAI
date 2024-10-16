package com.selbiconsulting.elog.ui.main.change_duty_status

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.enums.DutyStatus.OFF_DUTY
import com.selbiconsulting.elog.data.model.enums.DutyStatus.ON_DUTY
import com.selbiconsulting.elog.data.model.enums.DutyStatus.PC
import com.selbiconsulting.elog.data.model.enums.DutyStatus.SB
import com.selbiconsulting.elog.data.model.enums.DutyStatus.YM
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentChangeDutyStatusBinding
import com.selbiconsulting.elog.databinding.ItemCircularTiemlineVer2Binding
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetCheckLocation
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetQuickNotes
import com.selbiconsulting.elog.ui.main.change_duty_status.component.LocationListener
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.ym_and_pc.FragmentYmAndPcStatuses
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentChangeStatus : Fragment(), LocationListener, OnNotesSelected {
    private lateinit var b: FragmentChangeDutyStatusBinding
    private var selectedStatus: DutyStatus = ON_DUTY

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val args by navArgs<FragmentChangeStatusArgs>()
    private val viewModelChangeStatus: ViewModelChangeStatus by activityViewModels()
    private val helperFunctions: HelperFunctions by lazy { HelperFunctions() }
    private val uiHelper: UiHelper by lazy { UiHelper(requireContext()) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.checkedNotes.value = emptyList()
        viewModelChangeStatus.fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        viewModelChangeStatus.getDriverDataLocal()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentChangeDutyStatusBinding.inflate(inflater, container, false)
        viewModelChangeStatus.context = requireActivity()
        changeUI()
        setActiveSelectedStatusLayout()
        return b.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
    }

    private fun setActiveSelectedStatusLayout() {
        when (args.statusOrder) {
            ON_DUTY.ordinal -> {
                setActiveStatusLayout(
                    b.statusOnLay, b.tvStatusOn, b.ivStatusOn, R.color.success_on
                )
                selectedStatus = ON_DUTY
            }

            OFF_DUTY.ordinal -> {
                setActiveStatusLayout(
                    b.statusOfLay, b.tvStatusOff, b.ivStatusOff, R.color.error_on
                )
                selectedStatus = OFF_DUTY
            }

            YM.ordinal -> {
                setActiveStatusLayout(
                    b.statusYmLay, b.tvStatusYm, b.ivStatusYm, R.color.status_ym_color
                )
                selectedStatus = YM
            }

            SB.ordinal -> {
                setActiveStatusLayout(
                    b.statusSbLay, b.tvStatusSb, b.ivStatusSb, R.color.status_sb_color
                )
                selectedStatus = SB
            }

            PC.ordinal -> {
                setActiveStatusLayout(
                    b.statusPcLay, b.tvStatusPc, b.ivStatusPc, R.color.status_pc_color
                )
                selectedStatus = PC
            }
        }
    }

    private fun observe() {
        sharedViewModel.checkedNotes.observe(viewLifecycleOwner) { checkedNotes ->
            val checkedNotesText = checkedNotes.joinToString(separator = "; ") { it.name }
            b.edtNote.setText(checkedNotesText)
        }

        lifecycleScope.launch {
            viewModelChangeStatus.driverData.collect {
                it.let {

                    val doc = if (it?.document.isNullOrEmpty()) "N/A" else it?.document
                    val trailer =
                        if (it?.trailerId.isNullOrEmpty()) "Bobtail" else it?.trailerId

                    b.edtTrailer.setText(trailer)
                    b.edtDocument.setText(doc)
                }
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.location.collect { locationName ->
                sharedPreferences.lastLocation = locationName
                b.edtLocation.setText(locationName)
                b.ivLocationStatus.visibility =
                    if (locationName.isEmpty() || locationName == "N/A") View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModelChangeStatus.currentDutyStatus.collect { currentStatus ->
                if (currentStatus == null) return@collect
                setDriveTimeIndicator()
                setShiftTimeIndicator()
                setBreakTimeIndicator()

            }
        }

        viewModelChangeStatus.timerValue.observe(viewLifecycleOwner) {
            setDriveTimeIndicator()
            setShiftTimeIndicator()
            setBreakTimeIndicator()
        }
    }

    private fun changeUI() {
        setDriveTimeIndicator()
        setShiftTimeIndicator()
        setBreakTimeIndicator()

        b.tvNotesLengthCounter.text = ("${b.edtNote.text.length}/60")
        b.tvDocumentsLengthCounter.text = ("${b.edtDocument.text.length}/60")
        b.tvTrailersLengthCounter.text = ("${b.edtTrailer.text.length}/60")

        b.edtLocation.setText(sharedPreferences.lastLocation)

        b.statusPcLay.visibility = if (args.isPcAllowed) View.VISIBLE else View.GONE
    }


    private fun setDriveTimeIndicator() {
        val driveStatusIndicator = b.driveStatus

        val remainedTime = viewModelChangeStatus.calculateDriveTime()
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
        val shiftTimeIndicator = b.shiftStatus

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
        val breakStatusIndicator = b.breakStatus

        val remainedTime = viewModelChangeStatus.calculateBreakTime()

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


    private fun initListeners() {

        b.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        b.edtTrailer.addTextChangedListener {
            onTextChange(it)
        }
        b.edtDocument.addTextChangedListener {
            onTextChange(it)
        }

        b.statusOnLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusOnLay, b.tvStatusOn, b.ivStatusOn, R.color.success_on
            )
            selectedStatus = ON_DUTY
        }

        b.statusOfLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusOfLay, b.tvStatusOff, b.ivStatusOff, R.color.error_on
            )
            selectedStatus = OFF_DUTY
        }

        b.statusSbLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusSbLay, b.tvStatusSb, b.ivStatusSb, R.color.status_sb_color
            )
            selectedStatus = SB
        }

        b.statusYmLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusYmLay, b.tvStatusYm, b.ivStatusYm, R.color.status_ym_color
            )
            selectedStatus = YM

        }

        b.statusPcLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusPcLay, b.tvStatusPc, b.ivStatusPc, R.color.status_pc_color
            )
            selectedStatus = PC
        }

        b.edtNote.addTextChangedListener {
            b.tvNotesLengthCounter.text = "${it?.length}/60"
        }

        addTextWatcher(b.edtNote, b.tvNotesLengthCounter)
        addTextWatcher(b.edtTrailer, b.tvTrailersLengthCounter)
        addTextWatcher(b.edtDocument, b.tvDocumentsLengthCounter)

        b.btnSave.setOnClickListener {
            handleSaveButtonClick(selectedStatus)
        }

        b.btnQuickNote.setOnClickListener {
            val bottomSheetQuickNotes =
                BottomSheetQuickNotes(requireContext(), selectedStatus, true, this)
            bottomSheetQuickNotes.show(parentFragmentManager, bottomSheetQuickNotes.tag)
        }

        b.ivLocationStatus.setOnClickListener {
            val bottomSheetCheckLocation = BottomSheetCheckLocation(
                context = requireContext(),
                listener = this,
                locationName = viewModelChangeStatus.location
            )
            bottomSheetCheckLocation.show(parentFragmentManager, bottomSheetCheckLocation.tag)
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }

    }

    private fun onTextChange(it: Editable?) {
        b.btnSave.isEnabled = !it.isNullOrEmpty()
    }

    private fun handleSaveButtonClick(dutyStatus: DutyStatus) {
        if (b.edtTrailer.text.isNullOrEmpty()) {
            CustomToast.showCustomToastWithContent(
                v = requireView(),
                activity = requireActivity(),
                state = ToastStates.WARNING,
                stateTitle = "Trailer is empty",
//                enableClearIcon = false
            )
            return
        }

        when (dutyStatus) {
            ON_DUTY -> {
                if (b.edtNote.text.isNullOrEmpty()) {
                    val bottomSheetQuickNotes = BottomSheetQuickNotes(
                        context = requireContext(),
                        status = ON_DUTY,
                        isQuickNoteClicked = false,
                        this
                    )

                    bottomSheetQuickNotes.show(parentFragmentManager, bottomSheetQuickNotes.tag)

                } else {
                    changeDutyStatus()
                    findNavController().navigate(R.id.action_fragmentChangeStatus_to_fragmentFlow)
                }


            }

            YM -> {
                val action =
                    FragmentChangeStatusDirections.actionFragmentChangeStatusToFragmentYmAndPcStatuses(
                        FragmentYmAndPcStatuses.YM_STATUS,
                        sharedViewModel.isPcAllowed.value ?: false
                    )

                changeDutyStatus()
                findNavController().navigate(action)

            }

            PC -> {
                val action =
                    FragmentChangeStatusDirections.actionFragmentChangeStatusToFragmentYmAndPcStatuses(
                        FragmentYmAndPcStatuses.PC_STATUS,
                        sharedViewModel.isPcAllowed.value ?: false
                    )

                changeDutyStatus()
                findNavController().navigate(action)


            }

            else -> {
                changeDutyStatus()
                findNavController().navigate(R.id.action_fragmentChangeStatus_to_fragmentFlow)
            }

        }


    }

    private fun changeDutyStatus() {
        val dataStatus = DataLog()
        dataStatus.trailer = b.edtTrailer.text.toString()
        dataStatus.document = b.edtDocument.text.toString()
        dataStatus.statusShort = selectedStatus
        dataStatus.note = b.edtNote.text.toString()

        viewModelChangeStatus.changeDutyStatus(dataStatus)
    }

    private fun addTextWatcher(editText: EditText, counterTextView: TextView) {
        editText.addTextChangedListener {
            counterTextView.text = "${it?.length}/60"
        }
    }

    private fun setActiveStatusLayout(
        statusLayout: LinearLayout,
        statusTV: TextView,
        statusIV: ImageView,
        @ColorRes statusColor: Int
    ) {
        statusLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), statusColor)
        statusTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//        statusIV.imageTintList = ContextCompat.getColorStateList(requireContext(), statusColor)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)

        sharedViewModel.checkedNotes.value = emptyList()
    }

    private fun setInactiveAllStatusLayouts() {
        setInactiveStatusLayout(b.statusOnLay, b.tvStatusOn, b.ivStatusOn)
        setInactiveStatusLayout(b.statusOfLay, b.tvStatusOff, b.ivStatusOff)
        setInactiveStatusLayout(b.statusSbLay, b.tvStatusSb, b.ivStatusSb)
        setInactiveStatusLayout(b.statusYmLay, b.tvStatusYm, b.ivStatusYm)
        setInactiveStatusLayout(b.statusPcLay, b.tvStatusPc, b.ivStatusPc)
    }

    private fun setInactiveStatusLayout(
        statusLayout: LinearLayout, statusTV: TextView, statusIV: ImageView
    ) {
        statusLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.background)
        statusTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
//        statusIV.imageTintList =
//            ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.surface)
    }


    override fun updateLocation() {
        viewModelChangeStatus.getLocation()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelChangeStatus.clearLocation()
    }

    override fun onSelected() {
        changeDutyStatus()
    }
}

interface OnNotesSelected {
    fun onSelected()
}