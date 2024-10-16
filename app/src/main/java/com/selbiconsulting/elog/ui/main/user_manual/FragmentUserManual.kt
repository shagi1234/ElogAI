package com.selbiconsulting.elog.ui.main.user_manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.databinding.FragmentUserManualBinding

class FragmentUserManual : Fragment() {

    private lateinit var b: FragmentUserManualBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentUserManualBinding.inflate(inflater, container, false)
        initListeners()
        return b.root
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.inspectionsLay.setOnClickListener {
            navigateToReadPdfFragment(
                b.tvInspections.text.toString(),
                FragmentReadPDF.INSPECTION
            )
        }
        b.userManualLay.setOnClickListener {
            navigateToReadPdfFragment(
                b.tvUserManual.text.toString(),
                FragmentReadPDF.USER_MANUAL
            )
        }
        b.installationLay.setOnClickListener {
            navigateToReadPdfFragment(
                b.tvInstallation.text.toString(),
                FragmentReadPDF.INSTALLATION
            )

        }
        b.malfuctionLay.setOnClickListener {
            navigateToReadPdfFragment(
                b.tvMalfunction.text.toString(),
                FragmentReadPDF.MALFUNCTION
            )

        }
    }

     fun navigateToReadPdfFragment(title: String, pdfType: Int) {
        val action =
            FragmentUserManualDirections.actionFragmentUserManual2ToFragmentReadPDF(title, pdfType)
        findNavController().navigate(action)

    }

}