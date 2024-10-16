package com.selbiconsulting.elog.ui.main.dvir_page

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.request.RequestCreateDvir.Companion.toEntityDvir
import com.selbiconsulting.elog.databinding.FragmentChildDvirBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.confirm_signature.FragmentConfirmSignature
import com.selbiconsulting.elog.ui.main.flow.FragmentFlowDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale


@AndroidEntryPoint
class FragmentDvirPage : Fragment(), CreatedDVIRItemListener, BottomSheetDeleteDVIRListener {

    private lateinit var binding: FragmentChildDvirBinding
    private lateinit var adapterCreatedDVIR: AdapterCreatedDVIR
    private val viewModel by viewModels<ViewModelDvirPage>()
    private lateinit var bottomSheetDeleteDVIR: BottomSheetDeleteDVIR
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChildDvirBinding.inflate(inflater, container, false)
        viewModel.getAllDvir()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setRecyclerDvir()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initListeners()
    }

    private fun observe() {
        viewModel.dvirList.observe(viewLifecycleOwner) { localDvirList ->
            if (localDvirList.isEmpty())
                showNoContentLay()
            else {
                hideNoContentLay()
                adapterCreatedDVIR.updateData(dvirList = localDvirList)
            }
        }

        viewModel.deleteDvirState.observe(viewLifecycleOwner) { deleteDvirState ->
            when (deleteDvirState) {
                is Resource.Error -> {
                    bottomSheetDeleteDVIR.getProgressBar().visibility = View.GONE
                    bottomSheetDeleteDVIR.getDeleteButton().isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                    bottomSheetDeleteDVIR.dismiss()
                }

                is Resource.Failure -> {
                    bottomSheetDeleteDVIR.getProgressBar().visibility = View.GONE
                    bottomSheetDeleteDVIR.getDeleteButton().isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.check_internet_connection),
                        enableClearIcon = false
                    )

                    bottomSheetDeleteDVIR.dismiss()

                }

                is Resource.Loading -> {
                    bottomSheetDeleteDVIR.getProgressBar().visibility = View.VISIBLE
                    bottomSheetDeleteDVIR.getDeleteButton().isEnabled = false
                }

                is Resource.Success -> {
                    bottomSheetDeleteDVIR.getProgressBar().visibility = View.GONE
                    bottomSheetDeleteDVIR.getDeleteButton().isEnabled = true
                    viewModel.deleteDvir()
                    bottomSheetDeleteDVIR.dismiss()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.getAllDvirState.collect { dvirState ->
                    when (dvirState) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.noContentLay.root.visibility = View.GONE
                            binding.createdDvirLay.visibility = View.GONE
                        }

                        is Resource.Success -> {
                            binding.progressBar.visibility = View.VISIBLE
                            if (dvirState.data.isNullOrEmpty()) {
                                showNoContentLay()
                            } else {
                                val response = dvirState.data
                                viewModel.upsertDvirList(response.map { it.toEntityDvir() })
                            }
                        }

                        else -> viewModel.getAllDvirLocal()
                    }
                }
            }
        }

        viewModel.locationNAme.observe(viewLifecycleOwner){locationName ->
            navigateToCreateDvirScreen(locationName)
        }

    }

    private fun hideNoContentLay() {
        binding.noContentLay.root.visibility = View.GONE
        binding.createdDvirLay.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun showNoContentLay() {
        binding.noContentLay.root.visibility = View.VISIBLE
        binding.createdDvirLay.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun setRecyclerDvir() {
        adapterCreatedDVIR = AdapterCreatedDVIR(context = requireContext(), listener = this)
        binding.rvCreatedDvirs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterCreatedDVIR
        }
    }

    private fun initListeners() {
        binding.btnCreateDvir.setOnClickListener {
            viewModel.getLocation()
//            navigateToCreateDvirScreen(locationName?:"")

        }
        binding.noContentLay.btnCreateDvir.setOnClickListener {
            viewModel.getLocation()
//            navigateToCreateDvirScreen(locationName?:"")
        }


    }

    private fun navigateToCreateDvirScreen(locationName:String) {
        val action =
            FragmentFlowDirections.actionFragmentFlowToFragmentCreateDvir2(locationName)
        requireActivity().findNavController(R.id.fragment_container_main)
            .navigate(action)
    }


    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        lifecycleScope.launch {
                            getLocationName(latitude = it.latitude, longitude = it.longitude)
                        }
                    }
                }


        }
    }


    private suspend fun getLocationName(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressString = address.getAddressLine(0) // Get full address
                    withContext(Dispatchers.Main) {
                        locationName = addressString
                        Log.e("LOCATION_NAME", "Location Name: $addressString")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.e("LOCATION_NAME", "No address found for the location")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("LOCATION_NAME", "Geocoding failed", e)
                }
            }
        }
    }


    override fun onDeleteButtonClicked(dvir: EntityDvir) {
        bottomSheetDeleteDVIR = BottomSheetDeleteDVIR(currentDvir = dvir, listener = this)
        bottomSheetDeleteDVIR.show(childFragmentManager, bottomSheetDeleteDVIR.tag)
    }

    override fun onEditButtonClicked(dvir: EntityDvir) {
        val action = FragmentFlowDirections.actionFragmentFlowToFragmentConfirmSignature2(
            dvir,
            FragmentConfirmSignature.EDIT_DVIR
        )
        requireActivity().findNavController(R.id.fragment_container_main)
            .navigate(action)
    }

    override fun onDeleteClicked(dvir: EntityDvir) {
        viewModel.deleteDvirByRemoteId(dvir)
    }
}
