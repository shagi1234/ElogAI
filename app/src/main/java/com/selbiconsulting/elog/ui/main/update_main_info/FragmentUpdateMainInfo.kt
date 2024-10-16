package com.selbiconsulting.elog.ui.main.update_main_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoMainInfo
import com.selbiconsulting.elog.data.model.request.RequestUpdateDriver
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentUpdateMainInfoBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentUpdateMainInfo : Fragment() {
    private lateinit var b: FragmentUpdateMainInfoBinding
    private val args by navArgs<FragmentUpdateMainInfoArgs>()
    private val viewModel by viewModels<ViewModelUpdateMainInfo>()

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentUpdateMainInfoBinding.inflate(inflater, container, false)
        updateUi()
        return b.root
    }

    private fun updateUi() {
        b.edtDocs.setText(args.docsName)
        b.edtTrailer.setText(args.trailerName)
        b.edtNote.setText(args.notes)

        b.tvDocsLengthCounter.text = "${b.edtDocs.text.length}/60"
        b.tvTrailerLengthCounter.text = "${b.edtTrailer.text.length}/60"
        b.tvNoteLengthCounter.text = "${b.edtNote.text.length}/60"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()
    }

    private fun observe() {
        viewModel.driverInfo.observe(viewLifecycleOwner) { driverInfoState ->
            when (driverInfoState) {
                is Resource.Error -> {
                    b.progressBar.visibility = View.GONE
                    b.btnSave.isEnabled = true

                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.something_went_wrong),
                        enableClearIcon = false
                    )

                }

                is Resource.Failure -> {
                    b.progressBar.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    CustomToast.showCustomToastWithContent(
                        v = requireView(),
                        activity = requireActivity(),
                        state = ToastStates.ERROR,
                        stateTitle = resources.getString(R.string.no_internet_connection),
                        enableClearIcon = false
                    )
                }

                is Resource.Loading -> {
                    b.progressBar.visibility = View.VISIBLE
                    b.btnSave.isEnabled = false
                }

                is Resource.Success -> {
                    b.progressBar.visibility = View.GONE
                    b.btnSave.isEnabled = true
                    findNavController().navigate(R.id.action_fragmentUpdateMainInfo_to_fragmentFlow)
                }
            }

        }
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
        b.edtDocs.addTextChangedListener {
            b.tvDocsLengthCounter.text = "${it!!.length}/60"
        }

        b.edtTrailer.addTextChangedListener {
            b.tvTrailerLengthCounter.text = "${it!!.length}/60"
        }
        b.edtNote.addTextChangedListener {
            b.tvNoteLengthCounter.text = "${it!!.length}/60"
        }


        b.btnSave.setOnClickListener {
            val requestUpdateDriver = RequestUpdateDriver(
                contactId = sharedPreferencesHelper.contactId ?: "",
                ducumentId = b.edtDocs.text.toString(),
                trailerId = b.edtTrailer.text.toString(),
                note = b.edtNote.text.toString()
            )
            viewModel.updateDriverInfo(requestUpdateDriver)
        }
        b.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}