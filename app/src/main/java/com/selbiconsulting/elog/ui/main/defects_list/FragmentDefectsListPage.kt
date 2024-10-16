package com.selbiconsulting.elog.ui.main.defects_list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DefectsType
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.databinding.FragmentDefectListBinding
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.utils.io.concurrent.shared

const val DEFECT_TYPE = "_DEFECT_TYPE"

@AndroidEntryPoint
class FragmentDefectsListPage(private val _context: Context) : Fragment(),
    OnDefectsItemCheckedListener {


    companion object {
        @JvmStatic
        fun newInstance(context: Context, defectsType: DefectsType) =
            FragmentDefectsListPage(context).apply {
                arguments = Bundle().apply {
                    putString(DEFECT_TYPE, defectsType.name)
                }
            }
    }

    private var defectsType: String? = ""

    private lateinit var binding: FragmentDefectListBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val checkedUnitDefects = mutableListOf<DtoDefect>()
    private val checkedTrailerDefects = mutableListOf<DtoDefect>()
    private val savedUnitDefects = mutableListOf<DtoDefect>()
    private val savedTrailerDefects = mutableListOf<DtoDefect>()
    private val unitDefects = listOf(
        DtoDefect(_context.resources.getString(R.string.battery)),
        DtoDefect(_context.resources.getString(R.string.brake_accessories)),
        DtoDefect(_context.resources.getString(R.string.defroster_heater)),
        DtoDefect(_context.resources.getString(R.string.brakes_service)),
    )

    private val trailerDefects = listOf(
        DtoDefect(_context.resources.getString(R.string.coupling_devices)),
        DtoDefect(_context.resources.getString(R.string.landing_gear)),
        DtoDefect(_context.resources.getString(R.string.lights_all)),
        DtoDefect(_context.resources.getString(R.string.suspension_system)),
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            defectsType = it.getString(DEFECT_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDefectListBinding.inflate(inflater, container, false)
        setRecyclerDefects()
        return binding.root
    }


    private fun setRecyclerDefects() {
        binding.rvDefectList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvDefectList.adapter = AdapterDefectList(
            context = requireContext(),
            defectsType = defectsType ?: "",
            defects = if (defectsType == DefectsType.UnitDefect.name) unitDefects else trailerDefects,
            listener = this,
            checkedDefects = if (defectsType == DefectsType.UnitDefect.name) sharedViewModel.checkedUnitDefects.value
                ?: emptyList() else sharedViewModel.checkedTrailerDefects.value ?: emptyList()

        )
    }

    override fun onCheckedChange(isChecked: Boolean, defectsType: String, defect: DtoDefect) {
        val checkedDefectsCount = sharedViewModel.checkedDefectsCount.value ?: 0
        if (isChecked) {
            sharedViewModel.checkedDefectsCount.value = checkedDefectsCount + 1
            if (defectsType == DefectsType.UnitDefect.name) {
                checkedUnitDefects.add(defect)
                sharedViewModel.checkedUnitDefects.value = checkedUnitDefects
                Log.e(
                    "selectedUnitDefects",
                    "updateSharedViewModel: ${sharedViewModel.checkedUnitDefects.value}",
                )
            } else {
                checkedTrailerDefects.add(defect)
                sharedViewModel.checkedTrailerDefects.value = checkedTrailerDefects
                Log.e(
                    "selectedTrailerDefects",
                    "updateSharedViewModel: ${sharedViewModel.checkedTrailerDefects.value}",
                )
            }
        } else {
            if (checkedDefectsCount > 0)
                sharedViewModel.checkedDefectsCount.value = checkedDefectsCount - 1

            if (defectsType == DefectsType.UnitDefect.name) {
                checkedUnitDefects.remove(defect)
                sharedViewModel.checkedUnitDefects.value = checkedUnitDefects

                Log.e(
                    "selectedUnitDefects",
                    "updateSharedViewModel: ${sharedViewModel.checkedUnitDefects.value}",
                )
            } else {
                checkedTrailerDefects.remove(defect)
                sharedViewModel.checkedTrailerDefects.value = checkedTrailerDefects

                Log.e(
                    "selectedUnitDefects",
                    "updateSharedViewModel: ${sharedViewModel.checkedTrailerDefects.value}",
                )

            }
        }
    }

    override fun onDefectChecked(defectsType: String, defect: DtoDefect) {
        if (defectsType == DefectsType.UnitDefect.name) {
            checkedUnitDefects.add(defect)
            sharedViewModel.checkedUnitDefects.value = checkedUnitDefects

            savedUnitDefects.add(defect)
            sharedViewModel.savedUnitDefects.value = savedUnitDefects
        } else {
            checkedTrailerDefects.add(defect)
            sharedViewModel.checkedTrailerDefects.value = checkedTrailerDefects

            savedTrailerDefects.add(defect)
            sharedViewModel.savedTrailerDefects.value = savedTrailerDefects

        }
    }

    override fun onDefectUnchecked(defectsType: String, defect: DtoDefect) {
        if (defectsType == DefectsType.UnitDefect.name) {
            checkedUnitDefects.remove(defect)
            sharedViewModel.checkedUnitDefects.value = checkedUnitDefects

            savedUnitDefects.remove(defect)
            sharedViewModel.savedUnitDefects.value = savedUnitDefects
        } else {
            checkedTrailerDefects.remove(defect)
            sharedViewModel.checkedTrailerDefects.value = checkedTrailerDefects

            savedTrailerDefects.remove(defect)
            sharedViewModel.savedTrailerDefects.value = savedTrailerDefects

        }
    }

}



