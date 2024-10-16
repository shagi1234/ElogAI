package com.selbiconsulting.elog.ui.main.update_duty_status

import android.os.Bundle
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.request.RequestSendLogs
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentUpdateDutyStatusBinding
import com.selbiconsulting.elog.ui.main.certify_logs.adjustByTimezoneUTC
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetCheckLocation
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetSaveChangedStatusStatus
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetSaveDutyStatusListener
import com.selbiconsulting.elog.ui.main.change_duty_status.component.LocationListener
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.main.common.ToastStates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import javax.inject.Inject

@AndroidEntryPoint
class FragmentUpdateDutyStatus : Fragment(), BottomSheetSaveDutyStatusListener, LocationListener {
    private lateinit var b: FragmentUpdateDutyStatusBinding
    private lateinit var graphViewHelper: GraphViewHelper
    private val args: FragmentUpdateDutyStatusArgs by navArgs()

    val viewModel: ViewModelEditLog by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentUpdateDutyStatusBinding.inflate(inflater, container, false)
        init()
        updateUI()
        makeLog()
        observe()

        return b.root
    }

    private fun makeLog() {
        viewModel.dataLog = args.dataLog
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.updateState.observe(viewLifecycleOwner) {
                when (it) {
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
                            stateTitle = resources.getString(R.string.no_internet_connection),
                            enableClearIcon = false
                        )
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
                        findNavController().navigate(R.id.action_fragmentUpdateDutyStatus_to_fragmentFlow)

                        CustomToast.showCustomToastWithContent(
                            v = requireView(),
                            activity = requireActivity(),
                            state = ToastStates.SUCCESS,
                            stateTitle = resources.getString(R.string.successfully_edited),
                            enableClearIcon = false
                        )

                        viewModel.updateLocalLog()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.location.collect { locationName ->
                b.edtLocation.setText(locationName)
                b.ivLocationStatus.visibility =
                    if (locationName.isEmpty() || locationName == "N/A") View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateUI() {
        setActiveSelectedStatusLayout()

        b.tvNotesLengthCounter.text = ("${b.edtNote.text.length}/60")
        b.tvHoursLengthCounter.text = ("${b.edtEngineHours.text.length}/40")
        b.tvOdometerLengthCounter.text = ("${b.edtOdometer.text.length}/40")

        b.edtNote.setText(args.dataLog.note)
        b.edtEngineHours.setText(args.dataLog.engineHours)
        b.edtLocation.setText(args.dataLog.location)
        b.edtEndTime.setText(args.dataLog.endTime?.adjustByTimezoneUTC())
        b.edtStartTime.setText(args.dataLog.timeStart?.adjustByTimezoneUTC())
        b.edtOdometer.setText(args.dataLog.odometer)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        b.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        addTextWatcher(b.edtOdometer, b.tvOdometerLengthCounter, 40)
        addTextWatcher(b.edtEngineHours, b.tvHoursLengthCounter, 40)
        addTextWatcher(b.edtNote, b.tvNotesLengthCounter, 60)
        addTextWatcher(b.edtLocation, TextView(context), 60)

        b.statusOnLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusOnLay,
                b.tvStatusOn,
                b.ivStatusOn,
                R.color.status_on_color,
                R.color.status_on_overlay
            )
            viewModel.dataLog.statusShort = DutyStatus.ON_DUTY
        }
        b.statusOfLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusOfLay,
                b.tvStatusOff,
                b.ivStatusOff,
                R.color.status_off_color,
                R.color.status_off_overlay

            )
            viewModel.dataLog.statusShort = DutyStatus.OFF_DUTY

        }
        b.statusSbLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusSbLay,
                b.tvStatusSb,
                b.ivStatusSb,
                R.color.status_sb_color,
                R.color.status_sb_overlay

            )
            viewModel.dataLog.statusShort = DutyStatus.SB

        }
        b.statusYmLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusYmLay,
                b.tvStatusYm,
                b.ivStatusYm,
                R.color.status_ym_color,
                R.color.status_ym_overlay
            )
            viewModel.dataLog.statusShort = DutyStatus.YM

        }
        b.statusPcLay.setOnClickListener {
            setInactiveAllStatusLayouts()
            setActiveStatusLayout(
                b.statusPcLay,
                b.tvStatusPc,
                b.ivStatusPc,
                R.color.status_pc_color,
                R.color.status_pc_overlay

            )
            viewModel.dataLog.statusShort = DutyStatus.PC

        }

        b.ivLocationStatus.setOnClickListener {
            val bottomSheetCheckLocation = BottomSheetCheckLocation(
                context = requireContext(),
                listener = this,
                locationName = viewModel.location
            )
            bottomSheetCheckLocation.show(parentFragmentManager, bottomSheetCheckLocation.tag)
        }

        b.btnSave.setOnClickListener {

            val bottomSheetSaveChangedStatusStatus =
                BottomSheetSaveChangedStatusStatus(requireContext(), this)
            bottomSheetSaveChangedStatusStatus.show(
                parentFragmentManager, bottomSheetSaveChangedStatusStatus.tag
            )
        }
    }

    private fun init() {
        graphViewHelper = GraphViewHelper(
            requireContext(), b.graphView
        )
    }

    private fun addTextWatcher(editText: EditText, counterTextView: TextView, maxLength: Int) {
        editText.addTextChangedListener {
            counterTextView.text = "${it?.length}/$maxLength"
        }

        when (editText.id) {
            R.id.edt_odometer -> {
                viewModel.dataLog.odometer = editText.text.toString()
            }

            R.id.edt_engine_hours -> {
                viewModel.dataLog.engineHours = editText.text.toString()
            }

            R.id.edt_location -> {
                viewModel.dataLog.location = editText.text.toString()
            }

            R.id.edt_note -> {
                viewModel.dataLog.note = editText.text.toString()
            }

        }


    }

    private fun setActiveStatusLayout(
        statusLayout: LinearLayout,
        statusTV: TextView,
        statusIV: ImageView,
        @ColorRes statusColor: Int,
        @ColorRes overlayColor: Int
    ) {
        statusLayout.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), statusColor)
        statusTV.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        statusIV.imageTintList = ContextCompat.getColorStateList(requireContext(), statusColor)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.white)
        setOverlayToGraphView(overlayColor)

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
        statusIV.imageTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)
        statusIV.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.surface)
    }

    private fun setActiveSelectedStatusLayout() {
        when (args.dataLog.statusShort) {
            DutyStatus.ON_DUTY -> {
                setActiveStatusLayout(
                    b.statusOnLay,
                    b.tvStatusOn,
                    b.ivStatusOn,
                    R.color.status_on_color,
                    R.color.status_on_overlay

                )

                viewModel.dataLog.statusShort = DutyStatus.ON_DUTY
            }

            DutyStatus.OFF_DUTY -> {
                setActiveStatusLayout(
                    b.statusOfLay,
                    b.tvStatusOff,
                    b.ivStatusOff,
                    R.color.status_off_color,
                    R.color.status_off_overlay
                )

                viewModel.dataLog.statusShort = DutyStatus.OFF_DUTY
            }

            DutyStatus.YM -> {
                setActiveStatusLayout(
                    b.statusYmLay,
                    b.tvStatusYm,
                    b.ivStatusYm,
                    R.color.status_ym_color,
                    R.color.status_ym_overlay
                )

                viewModel.dataLog.statusShort = DutyStatus.YM
            }

            DutyStatus.SB -> {
                setActiveStatusLayout(
                    b.statusSbLay,
                    b.tvStatusSb,
                    b.ivStatusSb,
                    R.color.status_sb_color,
                    R.color.status_sb_overlay

                )

                viewModel.dataLog.statusShort = DutyStatus.SB
            }

            DutyStatus.PC -> {
                setActiveStatusLayout(
                    b.statusPcLay,
                    b.tvStatusPc,
                    b.ivStatusPc,
                    R.color.status_pc_color,
                    R.color.status_pc_overlay

                )

                viewModel.dataLog.statusShort = DutyStatus.PC
            }

            else -> {

            }
        }
    }

    private fun setOverlayToGraphView(@ColorRes overlayColor: Int) {
        b.statusOverlay.setBackgroundColor(ContextCompat.getColor(requireContext(), overlayColor))
    }

    override fun onSaveButtonClicked() {
        viewModel.dataLog.odometer = b.edtOdometer.text.toString()
        viewModel.dataLog.engineHours = b.edtEngineHours.text.toString()
        viewModel.dataLog.location = b.edtLocation.text.toString()
        viewModel.dataLog.note = b.edtNote.text.toString()

        viewModel.updateLog()
    }

    override fun updateLocation() {
        viewModel.getLocation()

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearLocation()
    }

}