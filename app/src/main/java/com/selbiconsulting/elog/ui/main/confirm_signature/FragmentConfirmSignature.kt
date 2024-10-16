package com.selbiconsulting.elog.ui.main.confirm_signature

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.gcacace.signaturepad.views.SignaturePad
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoFile
import com.selbiconsulting.elog.data.model.entity.EntityDvir
import com.selbiconsulting.elog.data.model.enums.MessageType
import com.selbiconsulting.elog.data.model.request.DvirStatus
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseCreateDvir
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentConfirmSignatureBinding
import com.selbiconsulting.elog.ui.extentions.toBase64
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.create_dvir.ViewModelCreateDvir
import com.selbiconsulting.elog.ui.util.EncodeDecodeBitmap.Companion.decodeBitmap
import com.selbiconsulting.elog.ui.util.EncodeDecodeBitmap.Companion.encodeBitmap
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class FragmentConfirmSignature : Fragment() {
    companion object {
        const val CREATE_DVIR = 0
        const val EDIT_DVIR = 1
    }

    private lateinit var b: FragmentConfirmSignatureBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    private val args by navArgs<FragmentConfirmSignatureArgs>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private val viewModelCreateDvir by viewModels<ViewModelCreateDvir>()

    private var isNewDriverSignature: Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentConfirmSignatureBinding.inflate(inflater, container, false)
        updateUi()
        return b.root
    }

    private fun updateUi() {
        if (args.type == CREATE_DVIR) {
            b.btnUseExistSignature.visibility =
                if (sharedPreferences.encodedDriverSignature.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            b.checkbox1.isChecked = true
            b.checkbox2.isChecked = false
            b.mechanicSignatureLay.visibility = View.INVISIBLE
        } else {
            setDriverSignature()
            b.checkbox1.isChecked = false
            b.checkbox2.isChecked = true
            isNewDriverSignature = false
            b.mechanicSignatureLay.visibility = View.VISIBLE
            b.btnUseExistSignature.visibility = View.INVISIBLE
        }


    }

    private fun setDriverSignature() {
        if (sharedPreferences.encodedDriverSignature.isNullOrEmpty()) return
        b.driverSignatureView.clear()
        b.driverSignatureView.signatureBitmap =
            decodeBitmap(sharedPreferences.encodedDriverSignature!!)
        isNewDriverSignature = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
    }

    private fun observe() {
        viewModelCreateDvir.createDvirState.observe(viewLifecycleOwner) { dvirState ->
            when (dvirState) {
                is Resource.Error -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    viewModelCreateDvir.deleteDvirByLocalId()
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Failure -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    viewModelCreateDvir.deleteDvirByLocalId()

                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.check_internet_connection),
                        enableClearIcon = false
                    )
                }

                is Resource.Loading -> {
                    b.progressBar?.visibility = View.VISIBLE
                    b.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    handleCreateDvirSuccess(dvirState.data?.get(0))
                }
            }
        }

        viewModelCreateDvir.driverSignatureFileState.observe(viewLifecycleOwner) { driverSignature ->
            when (driverSignature) {
                is Resource.Error -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Failure -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Loading -> {
                    b.progressBar?.visibility = View.VISIBLE
                    b.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    handleUploadSignatureSuccess(driverSignature)
                }
            }

        }

        viewModelCreateDvir.mechanicSignatureFileState.observe(viewLifecycleOwner) { mechanicSignatureState ->
            when (mechanicSignatureState) {
                is Resource.Error -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Failure -> {
                    b.progressBar?.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )
                }

                is Resource.Loading -> {
                    b.progressBar?.visibility = View.VISIBLE
                    b.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    val dvir = args.dvir ?: EntityDvir()
                    dvir.driverSignature = viewModelCreateDvir.driverSignatureId
                    dvir.mechanicSignature = mechanicSignatureState.data?.id
                    createDvir(dvir)
                }
            }

        }

    }

    private fun handleUploadSignatureSuccess(driverSignature: Resource.Success<ResponseUploadFile>) {
        viewModelCreateDvir.saveDriverSignatureIdToSharedPref(
            driverSignature.data?.id ?: ""
        )
        sharedPreferences.encodedDriverSignature =
            encodeBitmap(b.driverSignatureView.transparentSignatureBitmap)
        if (b.mechanicSignatureView.isEmpty) {
            val dvir = args.dvir ?: EntityDvir()
            dvir.driverSignature = viewModelCreateDvir.driverSignatureId
            createDvir(dvir)
        } else
            uploadMechanicSignature()
    }

    private fun handleCreateDvirSuccess(response: ResponseCreateDvir?) {
        b.progressBar?.visibility = View.GONE
        b.btnSave.isEnabled = true
        if (response == null) return

        val dvir = args.dvir ?: EntityDvir()
        dvir.driverSignature = viewModelCreateDvir.driverSignatureId
        dvir.localId = viewModelCreateDvir.dvirLocalId.value
        dvir.id = response.id

        viewModelCreateDvir.updateDvir(dvir)

        findNavController().navigate(R.id.action_fragmentConfirmSignature_to_fragmentFlow)

        sharedViewModel.isDvirCreated.value = true
        sharedViewModel.savedUnitDefects.value = emptyList()
        sharedViewModel.savedTrailerDefects.value = emptyList()
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.btnClearDriverSignature.setOnClickListener {
            clearDriverSignature()
        }

        b.btnClearMechanicSignature.setOnClickListener {
            b.mechanicSignatureView.clear()
        }

        b.checkbox1.setOnCheckedChangeListener { _, isChecked ->
            b.checkbox2.isChecked = !isChecked
            if (isChecked) {
                b.mechanicSignatureLay.visibility = View.GONE
                b.driverSignatureView.clear()
            }
        }


        b.checkbox2.setOnCheckedChangeListener { _, isChecked ->
            b.checkbox1.isChecked = !isChecked
            if (isChecked) {
                b.driverSignatureView.clear()
                b.mechanicSignatureLay.visibility = View.VISIBLE
            }
        }

        b.btnUseExistSignature.setOnClickListener {
            setDriverSignature()
        }

        b.driverSignatureView.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                isNewDriverSignature = true
                b.btnSave.isEnabled = true
            }

            override fun onSigned() {
                b.btnSave.isEnabled = true
            }

            override fun onClear() {
                b.btnSave.isEnabled = false
            }
        })

        b.btnSave.setOnClickListener {
            handleSaveButtonClick()
        }
    }

    private fun clearDriverSignature() {
        isNewDriverSignature = true
        b.driverSignatureView.clear()
        b.btnUseExistSignature.visibility =
            if (sharedPreferences.encodedDriverSignature.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
    }

    private fun handleSaveButtonClick() {
        if (viewModelCreateDvir.driverSignatureId.isEmpty() || isNewDriverSignature) //  hasn't driver signature
            uploadDriverSignature()
        else if (!b.mechanicSignatureView.isEmpty)
            uploadMechanicSignature()
        else {
            val dvir = args.dvir ?: EntityDvir()
            dvir.driverSignature = viewModelCreateDvir.driverSignatureId
            createDvir(dvir)
        }
    }


    private fun uploadDriverSignature() {
        val driverSignatureBitmap = b.driverSignatureView.signatureBitmap
        val driverSignatureFileName = "driver_signature.png"
        val driverSignatureFile = bitmapToFile(driverSignatureBitmap, driverSignatureFileName)
        val driverSignatureBase64 = driverSignatureFile.toBase64()
        val driverSignatureDto = DtoFile(
            data = driverSignatureBase64,
            filename = driverSignatureFileName,
            fileSize = driverSignatureFile.length().toInt(),
            contentType = MessageType.IMAGE.value
        )

        val requestUploadFile = RequestUploadFile(
            file = driverSignatureDto
        )


        viewModelCreateDvir.uploadDriverSignature(requestUploadFile)


    }

    private fun uploadMechanicSignature() {
        val mechanicSignatureBitmap = b.mechanicSignatureView.signatureBitmap
        val mechanicSignatureFileName = "mechanic_signature.png"
        val mechanicSignatureFile = bitmapToFile(mechanicSignatureBitmap, mechanicSignatureFileName)
        val mechanicSignatureBase64 = mechanicSignatureFile.toBase64()

        val mechanicSignatureDto = DtoFile(
            data = mechanicSignatureBase64,
            filename = mechanicSignatureFileName,
            fileSize = mechanicSignatureFile.length().toInt(),
            contentType = MessageType.IMAGE.value
        )

        val requestUploadFile = RequestUploadFile(
            file = mechanicSignatureDto
        )

        viewModelCreateDvir.uploadMechanicSignature(requestUploadFile)
    }
    private fun createDvir(dvir: EntityDvir) {
        dvir.status = if (b.mechanicSignatureView.isEmpty) DvirStatus.WORKING.value else DvirStatus.FIXED.value
        viewModelCreateDvir.createDvir(dvir = dvir)
    }

    private fun bitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File(requireActivity().cacheDir, filename)
        file.createNewFile()

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()

        return file
    }


}
