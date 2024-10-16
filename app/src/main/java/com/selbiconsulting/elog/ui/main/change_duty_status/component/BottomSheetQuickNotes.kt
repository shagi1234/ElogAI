package com.selbiconsulting.elog.ui.main.change_duty_status.component

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.model.dto.DtoNote
import com.selbiconsulting.elog.databinding.BottomSheetQuickNoteBinding
import com.selbiconsulting.elog.ui.main.change_duty_status.OnNotesSelected
import com.selbiconsulting.elog.ui.main.change_duty_status.adapter.AdapterQuickNotes
import com.selbiconsulting.elog.ui.main.change_duty_status.adapter.NotesItemListener
import com.selbiconsulting.elog.ui.util.SharedViewModel
import kotlinx.coroutines.launch

class BottomSheetQuickNotes(
    private val context: Context,
    private val status: DutyStatus,
    private val isQuickNoteClicked: Boolean,
    private val onNotesSelected: OnNotesSelected,

    ) :
    BottomSheetDialogFragment(), NotesItemListener {

    private lateinit var b: BottomSheetQuickNoteBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private val checkedNotes: MutableList<DtoNote> = mutableListOf()

    private lateinit var adapterQuickNotes: AdapterQuickNotes


    private val onDutyNotes = listOf(
        DtoNote(context.resources.getString(R.string.pti)),
        DtoNote(context.resources.getString(R.string.scale)),
        DtoNote(context.resources.getString(R.string.dot)),
        DtoNote(context.resources.getString(R.string.repairs)),
        DtoNote(context.resources.getString(R.string.pick_up)),
        DtoNote(context.resources.getString(R.string.delivery)),
        DtoNote(context.resources.getString(R.string.fuel)),
        DtoNote(context.resources.getString(R.string.drop)),
        DtoNote(context.resources.getString(R.string.hook)),
        DtoNote(context.resources.getString(R.string.other)),
    )

    private val offDutyNotes = listOf(
        DtoNote(context.resources.getString(R.string._break)),
        DtoNote(context.resources.getString(R.string.rest)),
        DtoNote(context.resources.getString(R.string.shower)),
        DtoNote(context.resources.getString(R.string.restroom)),
        DtoNote(context.resources.getString(R.string.breakfast)),
        DtoNote(context.resources.getString(R.string.dinner)),
        DtoNote(context.resources.getString(R.string.lunch)),
        DtoNote(context.resources.getString(R.string.sleep)),
        DtoNote(context.resources.getString(R.string.other)),
    )
    private val pcNotes = listOf(
        DtoNote(context.resources.getString(R.string.shopping)),
        DtoNote(context.resources.getString(R.string.restaurant)),
        DtoNote(context.resources.getString(R.string.home)),
        DtoNote(context.resources.getString(R.string.office)),
        DtoNote(context.resources.getString(R.string.gym)),
        DtoNote(context.resources.getString(R.string.truck_stop)),
    )
    private val sbNotes = offDutyNotes
    private val ymNotes = onDutyNotes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel.checkedNotes.value?.let { checkedNotes.addAll(it) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = BottomSheetQuickNoteBinding.inflate(LayoutInflater.from(context), container, false)
        setRecyclerNotes()
        addNotes()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.btnCancel.setOnClickListener { dismiss() }
        b.btnSave.setOnClickListener {
            lifecycleScope.launch {
                sharedViewModel.checkedNotes.value = checkedNotes

                if (!isQuickNoteClicked) {
                    onNotesSelected.onSelected()
                    findNavController().navigate(R.id.action_fragmentChangeStatus_to_fragmentFlow)

                }
                dismiss()
            }
        }

    }


    private fun addNotes() {
        adapterQuickNotes.notes =
            when (status) {
                DutyStatus.ON_DUTY -> onDutyNotes
                DutyStatus.OFF_DUTY -> offDutyNotes
                DutyStatus.SB -> sbNotes
                DutyStatus.YM -> ymNotes
                else -> pcNotes
            }
    }

    private fun setRecyclerNotes() {
        adapterQuickNotes =
            AdapterQuickNotes(
                context = context,
                listener = this,
                checkedNotes = sharedViewModel.checkedNotes.value
            )
        val orientation = resources.configuration.orientation
        val spanCount = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
        b.rvNotes.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = adapterQuickNotes
        }

    }

    override fun onNotesItemCheckedChanged(isChecked: Boolean, note: DtoNote) {
        if (isChecked) {
            if (checkedNotes.contains(note))
                checkedNotes.remove(note)
            checkedNotes.add(note)
        } else checkedNotes.remove(note)

        b.btnSave.isEnabled = checkedNotes.isNotEmpty()
    }
}