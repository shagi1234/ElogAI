package com.selbiconsulting.elog.ui.main.counter_screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.DutyStatus
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentCounterBinding
import com.selbiconsulting.elog.ui.main.home.adapter.DataLog
import com.selbiconsulting.elog.ui.main.ym_and_pc.FragmentYmAndPcStatuses
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.HelperFunctions
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.utils.io.concurrent.shared
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class FragmentCounter : Fragment() {
    private lateinit var b: FragmentCounterBinding
    private val viewModel by viewModels<ViewModelCounter>()
    private val args by navArgs<FragmentCounterArgs>()
    private val viewModelChangeStatus by activityViewModels<ViewModelChangeStatus>()

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentCounterBinding.inflate(inflater, container, false)
        updateUi()
        viewModel.startCounter()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initListeners()
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModel.progressValue.collect { progressValue ->
                if (progressValue > b.counterProgress.progress && progressValue < 100)
                    b.counterProgress.progress = progressValue
                else
                    b.counterProgress.setProgress(progressValue, true)
            }
        }

        lifecycleScope.launch {
            viewModel.progressCounterTime.collect { progressCounterTime ->
                b.tvCounter.text = progressCounterTime
            }
        }
        lifecycleScope.launch {
            viewModel.isProgressEnded.collect { isProgressEnded ->
                if (isProgressEnded) {
                    navigateToHomeScreen()
                }
            }
        }
    }

    private fun navigateToHomeScreen() {
        val dataStatus = DataLog().apply {
            statusShort =
                if (args.statusMode == FragmentYmAndPcStatuses.YM_STATUS) DutyStatus.ON_DUTY
                else DutyStatus.OFF_DUTY
            document = sharedPreferencesHelper.documentId
            note = if (args.statusMode == FragmentYmAndPcStatuses.YM_STATUS) Const.ymNote
            else Const.pcNote
        }

        viewModelChangeStatus.changeDutyStatus(dataStatus)

        findNavController().navigate(R.id.action_fragmentCounter_to_fragmentFlow)
    }

    private fun initListeners() {
        b.icThemeSwitch.setOnClickListener { viewModel.changeTheme() }

        b.icGps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            val availableMapsApps = mapIntent.resolveActivity(requireActivity().packageManager)

            if (availableMapsApps != null) {
                startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps")
                val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                startActivity(browserIntent)
            }
        }

        b.btnContinue.setOnClickListener { findNavController().navigateUp() }

        b.btnChangeStatus.setOnClickListener {
            navigateToChangeStatusScreen()
        }

    }

    private fun navigateToChangeStatusScreen() {
        val action = FragmentCounterDirections.actionFragmentCounterToFragmentChangeStatus(
            args.isPcAllowed,
            args.statusMode,
        )
        findNavController().navigate(action)
    }


    private fun updateUi() {
        val helper = UiHelper(requireContext())
        val windowWidth = helper.getWindowWidth()
        val progressIndicatorsSize = if (helper.isTablet())
            ((helper.pxToDp(windowWidth.toFloat()).toInt() - 32) / 4) else
            ((helper.pxToDp(windowWidth.toFloat()).toInt() - 32) / 3 * 2)

        b.counterProgress.indicatorSize = helper.dpToPx(progressIndicatorsSize)

    }

}