package com.selbiconsulting.elog.ui.main.create_dvir

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentCreateDvirBinding
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetCheckLocation
import com.selbiconsulting.elog.ui.main.change_duty_status.component.BottomSheetSaveDutyStatusListener
import com.selbiconsulting.elog.ui.main.change_duty_status.component.LocationListener
import com.selbiconsulting.elog.ui.main.confirm_signature.FragmentConfirmSignature
import com.selbiconsulting.elog.ui.main.defects.FragmentDefects
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.valuesOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class FragmentCreateDvir : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var binding: FragmentCreateDvirBinding

    private val savedUnitDefects = mutableListOf<DtoDefect>()
    private val savedTrailerDefects = mutableListOf<DtoDefect>()

    private val viewModel by viewModels<ViewModelCreateDvir>()
    private val args by navArgs<FragmentCreateDvirArgs>()

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateDvirBinding.inflate(inflater, container, false)
        updateUi()
        viewModel.getDriverInfo()
        return binding.root
    }


    private fun updateUi() {
        binding.unitDefectsLay.visibility =
            if (sharedViewModel.savedUnitDefects.value.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.trailerDefectsLay.visibility =
            if (sharedViewModel.savedTrailerDefects.value.isNullOrEmpty()) View.GONE else View.VISIBLE

        binding.tvCreatedDate.text = HelperFunctions().formattedDateString("dd.MM.yyyy")
        binding.tvLocationName.text = args.locationName
        binding.tvOdometerValue.text = sharedPreferencesHelper.odometer ?: "0"

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()

    }

    private fun observe() {
        sharedViewModel.savedUnitDefects.observe(viewLifecycleOwner) {
            savedUnitDefects.clear()
            savedUnitDefects.addAll(it)

            updateUi()
            addUnitDefectsChips(savedUnitDefects)
        }
        sharedViewModel.savedTrailerDefects.observe(viewLifecycleOwner) {
            savedTrailerDefects.clear()
            savedTrailerDefects.addAll(it)

            updateUi()
            addTrailerDefects(savedTrailerDefects)
        }

        viewModel.driverInfo.observe(viewLifecycleOwner) { driver ->
            binding.tvVehicleName.text = driver.vehicleId
            binding.tvTrailerNumber.setText(driver.trailerId)
        }
        viewModel.locationName.observe(viewLifecycleOwner) { locationName ->
            binding.tvLocationName.text = locationName
            updateUi()

        }
    }


    private fun addTrailerDefects(trailerDefects: List<DtoDefect>) {
        trailerDefects.forEach { trailerDefect ->
            val unitDefectsChip = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_selected_defect_chip,
                binding.cpTrailerDefects,
                false
            ) as Chip
            unitDefectsChip.text = trailerDefect.name
            binding.cpTrailerDefects.addView(unitDefectsChip)


            unitDefectsChip.setOnCloseIconClickListener {
                binding.cpTrailerDefects.removeView(it)
                savedTrailerDefects.remove(trailerDefect)
                if (savedTrailerDefects.isEmpty()) binding.trailerDefectsLay.visibility = View.GONE
            }
        }
    }

    private fun addUnitDefectsChips(unitDefects: List<DtoDefect>) {
        unitDefects.forEach { unitDefect ->
            val unitDefectsChip = LayoutInflater.from(requireContext()).inflate(
                R.layout.item_selected_defect_chip,
                binding.cpUnitDefects,
                false
            ) as Chip
            unitDefectsChip.text = unitDefect.name
            binding.cpUnitDefects.addView(unitDefectsChip)

            unitDefectsChip.setOnCloseIconClickListener {
                binding.cpUnitDefects.removeView(it)
                savedUnitDefects.remove(unitDefect)
                if (savedUnitDefects.isEmpty()) binding.unitDefectsLay.visibility = View.GONE
            }

        }

    }

    private fun initListeners() {
        binding.icBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvTrailerNumber.addTextChangedListener {
            binding.btnCreateDvir.isEnabled = !it.isNullOrEmpty()
        }

        binding.btnAddDefects.setOnClickListener {
            sharedViewModel.checkedUnitDefects.value = savedUnitDefects
            sharedViewModel.checkedTrailerDefects.value = savedTrailerDefects
            findNavController().navigate(R.id.action_fragmentCreateDvir2_to_fragmentDefects2)
        }

        binding.btnCreateDvir.setOnClickListener {
            val currentDvir = EntityDvir(
                vehicle = binding.tvVehicleName.text.toString().trim(),
                trailer = binding.tvTrailerNumber.text.toString().trim(),
                location = binding.tvLocationName.text.toString().trim(),
                createdAt = HelperFunctions().getCurrentDate(),
                odometer = binding.tvOdometerValue.text.toString().trim(),
                unitDefects = savedUnitDefects,
                trailerDefects = savedTrailerDefects
            )

            navigateToSignatureScreen(dvir = currentDvir)
        }

    }

    private fun navigateToSignatureScreen(dvir: EntityDvir) {
        val action =
            FragmentCreateDvirDirections.actionFragmentCreateDvir2ToFragmentConfirmSignature(
                dvir,
                FragmentConfirmSignature.CREATE_DVIR
            )
        findNavController().navigate(action)
    }

}
