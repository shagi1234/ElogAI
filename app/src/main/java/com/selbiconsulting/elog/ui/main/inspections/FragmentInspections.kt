package com.selbiconsulting.elog.ui.main.inspections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.FragmentInspectionsBinding


class FragmentInspections : Fragment() {

    private lateinit var binding: FragmentInspectionsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentInspectionsBinding.inflate(inflater, container, false)
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnBeginInspection.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentInspections_to_fragmentLogReport)
        }

        binding.btnSendLogs.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentInspections_to_fragmentSendLogs2)
        }

        binding.btnSendEld.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentInspections_to_fragmentSendEldFile)
        }
    }

}