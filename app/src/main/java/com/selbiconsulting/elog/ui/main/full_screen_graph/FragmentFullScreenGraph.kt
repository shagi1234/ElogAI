package com.selbiconsulting.elog.ui.main.full_screen_graph

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.databinding.FragmentFullScreenGraphBinding
import com.selbiconsulting.elog.ui.main.common.GraphViewHelper
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentFullScreenGraph : Fragment() {
    private lateinit var b: FragmentFullScreenGraphBinding
    private lateinit var graphViewHelper: GraphViewHelper
    private val viewModel by viewModels<ViewModelFullScreenGraph>()
    private val viewModelChangeStatus by activityViewModels<ViewModelChangeStatus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigationBar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentFullScreenGraphBinding.inflate(inflater, container, false)
        graphViewHelper = GraphViewHelper(
            requireContext(),
            b.graphView
        )

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        initListeners()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    private fun observe() {
        viewModelChangeStatus.seriesLiveData.observe(viewLifecycleOwner) { series ->
            graphViewHelper.setSeriesSolid(series)
        }
    }


    override fun onResume() {
        super.onResume()
        hideNavigationBar()
    }

    private fun hideNavigationBar() {
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.themeSwitch.setOnClickListener { viewModel.changeTheme() }
    }

}