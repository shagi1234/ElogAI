package com.selbiconsulting.elog.ui.login.already_logged_in

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.selbiconsulting.elog.databinding.FragmentAlreadyLoggedInBinding
import com.selbiconsulting.elog.ui.login.login_page.ViewModelLogin
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentAlreadyLoggedIn : Fragment() {
    private lateinit var b: FragmentAlreadyLoggedInBinding
    private val viewModelLogin by activityViewModels<ViewModelLogin>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentAlreadyLoggedInBinding.inflate(inflater, container, false)
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
                viewModelLogin.loginState.observe(viewLifecycleOwner) { loginState ->
                    when (loginState) {
                        is Resource.Error -> {
                            CustomToast.showCustomToastWithContent(
                                requireView(),
                                requireActivity(),
                                ToastStates.ERROR,
                                getString(R.string.error_while_login),
                                getString(R.string.something_went_wrong),
                                false
                            )
                        }

                        is Resource.Failure -> {
                            CustomToast.showCustomToastWithContent(
                                requireView(),
                                requireActivity(),
                                ToastStates.ERROR,
                                getString(R.string.error_while_login),
                                getString(R.string.check_internet_connection),
                                false
                            )
                        }

                        is Resource.Loading -> {

                        }

                        is Resource.Success -> handleLoginSuccess(loginState.data)
                    }
                }
            }
        }
    }

    private fun handleLoginSuccess(response: ResponseLogin?) {

        if (response == null) return

        lifecycleScope.launch {
            viewModelLogin.saveUserInfo(response)
            viewModelLogin.setHasLeftTruck(false)
            navigateToMainActivity()
        }

    }

    private fun initListeners() = with(b) {
        btnReturn.setOnClickListener {
            findNavController().navigateUp()
        }
        b.btnContinue.setOnClickListener {
            viewModelLogin.login()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), ActivityMain::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }
}