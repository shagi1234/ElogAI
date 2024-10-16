package com.selbiconsulting.elog.ui.main.defects

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.dto.DtoDefect
import com.selbiconsulting.elog.databinding.FragmentDefectsBinding
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentDefects() : Fragment() {
    private lateinit var binding: FragmentDefectsBinding
    private lateinit var adapterDefectsFragmentPager: AdapterDefectsFragmentPager
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val checkedUnitDefects = mutableListOf<DtoDefect>()
    private val checkedTrailerDefects = mutableListOf<DtoDefect>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDefectsBinding.inflate(inflater, container, false)
        setViewPager()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()

    }

    private fun observe() {
        sharedViewModel.checkedDefectsCount.observe(viewLifecycleOwner) { checkedDefectsCount ->
            if (checkedDefectsCount == 0)
                binding.btnSave.text = requireActivity().resources.getString(R.string.save)
            else
                binding.btnSave.text =
                    "${requireActivity().resources.getString(R.string.save)} ($checkedDefectsCount)"
        }

        sharedViewModel.checkedUnitDefects.observe(viewLifecycleOwner) {
            checkedUnitDefects.clear()
            checkedUnitDefects.addAll(it)
        }


        sharedViewModel.checkedTrailerDefects.observe(viewLifecycleOwner) {
            checkedTrailerDefects.clear()
            checkedTrailerDefects.addAll(it)
        }
    }

    private fun initListeners() {
        binding.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            sharedViewModel.savedUnitDefects.value = checkedUnitDefects
            sharedViewModel.savedTrailerDefects.value = checkedTrailerDefects
            findNavController().navigateUp()
        }

    }


    private fun setViewPager() {
        val tabs = listOf(
            requireContext().resources.getString(R.string.unit),
            requireContext().resources.getString(R.string.trailers)
        )

        adapterDefectsFragmentPager =
            AdapterDefectsFragmentPager(childFragmentManager, lifecycle, tabs, requireContext())

        binding.viewPager.adapter = adapterDefectsFragmentPager
        binding.viewPager.offscreenPageLimit = 2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()


    }

    override fun onDestroy() {
        super.onDestroy()
        sharedViewModel.checkedDefectsCount.value = 0
    }
}
