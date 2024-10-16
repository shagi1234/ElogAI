package com.selbiconsulting.elog.ui.main.change_duty_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentConfirmChangedStatusBinding
import com.selbiconsulting.elog.ui.main.flow.FragmentFlow
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.EncodeDecodeBitmap
import com.selbiconsulting.elog.ui.util.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentConfirmChangedStatus : Fragment() {
    private lateinit var b: FragmentConfirmChangedStatusBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentConfirmChangedStatusBinding.inflate(inflater, container, false)
        updateUi()
        return b.root
    }

    private fun updateUi() {
        b.btnUseExistSignature.visibility =
            if (sharedPreferences.encodedDriverSignature.isNullOrEmpty()) View.GONE else View.VISIBLE
        UiHelper(requireContext()).disableButtonPrimary(b.btnSubmit)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.icBack.setOnClickListener {
            findNavController().navigateUp()
        }
        b.btnUseExistSignature.setOnClickListener {
            b.signatureView.clear()
            b.signatureView.signatureBitmap =
                EncodeDecodeBitmap.decodeBitmap(sharedPreferences.encodedDriverSignature!!)
        }

        b.btnClearSignature.setOnClickListener {
            b.signatureView.clear()
        }

        b.btnSubmit.setOnClickListener {
            val progressDialog = CustomProgressDialog(requireContext(), "Updating")

            lifecycleScope.launch {
                progressDialog.show()
                delay(2000)
                progressDialog.dismiss()
                CustomToast.showCustomToastWithContent(
                    b.root,
                    requireActivity(),
                    ToastStates.SUCCESS,
                    "Successfully Edited"
                )
            }
        }

        b.signatureView.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                UiHelper(requireContext()).enableButtonPrimary(b.btnSubmit)

            }

            override fun onSigned() {
                UiHelper(requireContext()).enableButtonPrimary(b.btnSubmit)

            }

            override fun onClear() {
                UiHelper(requireContext()).disableButtonPrimary(b.btnSubmit)
            }
        })
    }




}


