package com.selbiconsulting.elog.ui.main.ym_and_pc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.FragmentYmAndPcStatusesBinding
import com.selbiconsulting.elog.ui.extentions.divide
import com.selbiconsulting.elog.ui.util.Const
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FragmentYmAndPcStatuses : Fragment() {
    private lateinit var b: FragmentYmAndPcStatusesBinding
    private val args by navArgs<FragmentYmAndPcStatusesArgs>()
    private val viewModel by viewModels<ViewModelYmAndPcStatuses>()

    companion object {
        const val PC_STATUS = 5
        const val YM_STATUS = 4
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentYmAndPcStatusesBinding.inflate(inflater, container, false)

        updateUi()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        lifecycleScope.launch {
            delay(Const.ymAndPcStatusDurationsInMillis.toInt().divide(Const.divider).toLong())
            navigateToCountdownScreen()
        }
    }

    private fun navigateToCountdownScreen() {
        val action =
            FragmentYmAndPcStatusesDirections.actionFragmentYmAndPcStatusesToFragmentCounter(
                args.statusMode,
                args.isPcAllowed
            )
        findNavController().navigate(action)

    }

    private fun initListeners() {
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

        b.icThemeSwitch.setOnClickListener {
            viewModel.changeTheme()
        }

        b.btnChangeStatus.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun updateUi() {
        val helper = UiHelper(requireContext())
        val windowWidth = helper.getWindowWidth()
        if (!helper.isTablet()) {
            val progressIndicatorsSize = ((helper.pxToDp(windowWidth.toFloat()).toInt() - 120) / 2)
            b.driveProgressIndicator.indicatorSize = helper.dpToPx(progressIndicatorsSize)
            b.shiftProgressIndicator.indicatorSize = helper.dpToPx(progressIndicatorsSize)

            val statusLayParams = b.statusLayOval.layoutParams
            statusLayParams.height = (windowWidth.toFloat()).toInt() / 5 * 3
            statusLayParams.width = (windowWidth.toFloat()).toInt() / 5 * 3

            b.statusLayOval.layoutParams = statusLayParams

        } else {
            val progressIndicatorsSize = (helper.pxToDp(windowWidth.toFloat()).toInt()) / 6
            b.driveProgressIndicator.indicatorSize = helper.dpToPx(progressIndicatorsSize)
            b.shiftProgressIndicator.indicatorSize = helper.dpToPx(progressIndicatorsSize)

            val statusLayParams = b.statusLayOval.layoutParams
            statusLayParams.height = (helper.pxToDp(windowWidth.toFloat()).toInt()) / 2
            statusLayParams.width = (helper.pxToDp(windowWidth.toFloat()).toInt()) / 2

            b.statusLayOval.layoutParams = statusLayParams
        }


        if (args.statusMode == YM_STATUS) {
            b.statusLayOval.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.status_ym_overlay)
            b.tvShortStatus.setTextColor(resources.getColor(R.color.status_ym_color))
            b.tvLongStatus.setTextColor(resources.getColor(R.color.status_ym_color))
            b.tvShortStatus.text = resources.getText(R.string.ym)
            b.tvLongStatus.text = resources.getText(R.string.yard_move)
        } else {
            b.statusLayOval.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.status_pc_overlay)
            b.tvShortStatus.setTextColor(resources.getColor(R.color.status_pc_color))
            b.tvLongStatus.setTextColor(resources.getColor(R.color.status_pc_color))
            b.tvShortStatus.text = resources.getText(R.string.pc)
            b.tvLongStatus.text = resources.getText(R.string.personal_conveyance)
        }

    }

}