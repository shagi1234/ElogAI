package com.selbiconsulting.elog.ui.login.login_page

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.request.RequestLogin
import com.selbiconsulting.elog.data.model.response.ResponseLogin
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentLoginBinding
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.UiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class FragmentLogin : Fragment() {
    private lateinit var b: FragmentLoginBinding
    private val viewModel by activityViewModels<ViewModelLogin>()
    private lateinit var progressDialog: CustomProgressDialog

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val username = b.edtUsername.text.trim()
            val password = b.edtPassword.text.trim()
            b.btnLogin.isEnabled = (username.isNotEmpty() && password.isNotEmpty())
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentLoginBinding.inflate(inflater, container, false)
        progressDialog = CustomProgressDialog(requireContext(), "Loading")
        b.btnReturnLay.visibility = if (viewModel.hasLeftTruck.value == true) View.VISIBLE else View.GONE

        return b.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.loginState.observe(viewLifecycleOwner) { loginState ->
                    when (loginState) {
                        is Resource.Error -> handleError()
                        is Resource.Failure -> showNoConnection()
                        is Resource.Loading -> showLoading()
                        is Resource.Success -> handleLoginSuccess(loginState.data)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.handleLoginState.observe(viewLifecycleOwner) { loginState ->
                    when (loginState) {
                        is Resource.Error -> handleError()
                        is Resource.Failure -> showNoConnection()
                        is Resource.Loading -> showLoading()
                        is Resource.Success -> handleLoginState(loginState.data)
                    }

                }
            }
        }
    }

    private fun handleLoginState(data: ResponseLogin?) {
        if (data == null) return
        if (data.deviceId.isNullOrEmpty()) {
            viewModel.login()
        } else
            navigateToAlreadyLoggedScreen()
    }

    private fun navigateToAlreadyLoggedScreen() {
        progressDialog.dismiss()
        findNavController().navigate(R.id.action_fragmentLogin_to_fragmentAlreadyLoggedIn)
    }

    private fun showLoading() {
        progressDialog.show()
    }

    private fun showNoConnection() {
        progressDialog.dismiss()
        CustomToast.showCustomToastWithContent(
            requireView(),
            requireActivity(),
            ToastStates.ERROR,
            "No internet connection",
            "Check internet connection",
            false
        )
    }

    private fun handleError() {
        progressDialog.dismiss()
        CustomToast.showCustomToastWithContent(
            requireView(),
            requireActivity(),
            ToastStates.ERROR,
            "Error while login",
            "Incorrect username or password",
            false
        )
    }

    private fun navigateToMainActivity() {
        progressDialog.dismiss()
        val intent = Intent(requireActivity(), ActivityMain::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun handleLoginSuccess(response: ResponseLogin?) {
        progressDialog.dismiss()

        if (response == null) return

        lifecycleScope.launch {
            viewModel.saveUserInfo(response)
            viewModel.setHasLeftTruck(false)
            navigateToMainActivity()
        }

    }

    private fun initListeners() {
        b.edtUsername.addTextChangedListener(editTextWatcher)
        b.edtPassword.addTextChangedListener(editTextWatcher)
        b.ivEye.setOnClickListener { showHidePassword(it) }
        b.btnLogin.setOnClickListener {

            val requestLogin = RequestLogin(
                username = b.edtUsername.text.toString().trim(),
                password = b.edtPassword.text.toString().trim(),
                deviceId = sharedPreferencesHelper.deviceID.toString(),
                deviceToken = sharedPreferencesHelper.fcm_token.toString()
            )

            UiHelper(requireContext()).hideKeyboard(requireActivity())
//            viewModel.login(requestLogin)
            viewModel.checkHAsAnotherDevice(requestLogin)

        }

        b.btnReturnToTruck.setOnClickListener {
            val mainIntent = Intent(requireActivity(), ActivityMain::class.java)
            viewModel.setHasLeftTruck(false)
            requireActivity().startActivity(mainIntent)
            requireActivity().finish()
        }

    }

    private fun showHidePassword(view: View) {
        if (b.edtPassword.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
            (view as ImageView).setImageResource(R.drawable.ic_close_eye)
            b.edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()

        } else {
            (view as ImageView).setImageResource(R.drawable.ic_open_eye)
            b.edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        }
        b.edtPassword.setSelection(b.edtPassword.text.length)
    }
}