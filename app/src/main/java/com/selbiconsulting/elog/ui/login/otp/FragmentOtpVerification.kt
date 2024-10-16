package com.selbiconsulting.elog.ui.login.otp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.databinding.FragmentOtpVerificationBinding
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FragmentOtpVerification : Fragment() {
    private lateinit var b: FragmentOtpVerificationBinding
    private lateinit var countDownTimer: CountDownTimer
    private val progressDialog: CustomProgressDialog by lazy {
        CustomProgressDialog(
            requireContext(),
            "Loading"
        )
    }

    private val viewModel by viewModels<ViewModelOtpVerification>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableResendText()
        initListeners()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.checkingState.observe(viewLifecycleOwner) { checkingState ->
                    when (checkingState) {
                        is Resource.Error -> handleError()
                        is Resource.Failure -> showNoConnection()
                        is Resource.Loading -> showLoading()
                        is Resource.Success -> navigateToMainActivity()
                    }
                }
            }
        }
    }

    private fun handleError() {
        progressDialog.dismiss()

    }

    private fun showNoConnection() {

    }

    private fun showLoading() {
        progressDialog.show()

    }

    private fun navigateToMainActivity() {
        progressDialog.dismiss()
        val intent = Intent(requireActivity(), ActivityMain::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun setCountdownTimer() {
        countDownTimer = object : CountDownTimer(120 * 1000L + 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (activity==null) return
                disableResendText()
                val hours = (millisUntilFinished / 1000).toInt()
                val minutes = hours / 60
                val seconds = hours % 60
                b.tvCountdownTimer.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                if (activity==null) return
                enableResendText()
                b.tvCountdownTimer.visibility = View.GONE
            }
        }
        countDownTimer.start()
    }

    private fun enableResendText() {
        b.tvResend.isEnabled = true
        b.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_brand))

    }

    private fun disableResendText() {
        b.tvResend.isEnabled = false
        b.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.edtCode.addTextChangedListener {
            b.btnVerify.isEnabled = b.edtCode.text.trim().length == 6
        }

        b.tvResend.setOnClickListener {
            b.tvCountdownTimer.visibility = View.VISIBLE
            setCountdownTimer()
        }

        b.btnVerify.setOnClickListener {
            viewModel.checkOtp(b.edtCode.text.toString().trim())
        }
    }

}