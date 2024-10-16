package com.selbiconsulting.elog.ui.main.certify_logs.components

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoFile
import com.selbiconsulting.elog.data.model.enums.MessageType
import com.selbiconsulting.elog.data.model.request.RequestUploadFile
import com.selbiconsulting.elog.data.model.response.ResponseUploadFile
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.BottomSheetCertifySignatureBinding
import com.selbiconsulting.elog.ui.extentions.toBase64
import com.selbiconsulting.elog.ui.main.certify_logs.OnCertifyLogsClicked
import com.selbiconsulting.elog.ui.main.certify_logs.ViewModelCertifyLogs
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.EncodeDecodeBitmap
import com.selbiconsulting.elog.ui.util.EncodeDecodeBitmap.Companion.encodeBitmap
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetCertifyLogs(
    private val onCertifyLogsClicked: OnCertifyLogsClicked,
) : BottomSheetDialogFragment() {
    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    private var _binding: BottomSheetCertifySignatureBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()


    companion object {
        const val OPENED_FROM_FLOW = 0
        const val OPENED_FROM_SINGLE_LOG = 1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding.signatureView.clear()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCertifySignatureBinding.inflate(inflater, container, false)
        updateUi()

        return binding.root
    }

    private fun updateUi() {
        binding.btnUseExistSignature.visibility =
            if (sharedPreferences.encodedDriverSignature.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.tvSignHere.visibility =
            if (binding.signatureView.isEmpty) View.VISIBLE else View.GONE

        UiHelper(requireContext()).disableButtonPrimary(binding.btnAccept)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun uploadDriverSignature() {
        val driverSignatureBitmap = binding.signatureView.signatureBitmap
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

        onCertifyLogsClicked.uploadSignature(requestUploadFile)

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

    private fun initListeners() {
        binding.btnAccept.setOnClickListener {
            lifecycleScope.launch {
                sharedViewModel.encodedSignature.value = encodeBitmap(binding.signatureView.transparentSignatureBitmap)

                uploadDriverSignature()
            }

        }

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.btnClear.setOnClickListener {
            binding.signatureView.clear()
        }

        binding.btnUseExistSignature.setOnClickListener {
            binding.signatureView.clear()
            binding.signatureView.signatureBitmap =
                EncodeDecodeBitmap.decodeBitmap(sharedPreferences.encodedDriverSignature!!)
            binding.tvSignHere.visibility = View.GONE
        }

        binding.signatureView.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                binding.tvSignHere.visibility = View.GONE
                UiHelper(requireContext()).enableButtonPrimary(binding.btnAccept)

            }

            override fun onSigned() {
                UiHelper(requireContext()).enableButtonPrimary(binding.btnAccept)

            }

            override fun onClear() {
                UiHelper(requireContext()).disableButtonPrimary(binding.btnAccept)
                binding.tvSignHere.visibility = View.VISIBLE
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
