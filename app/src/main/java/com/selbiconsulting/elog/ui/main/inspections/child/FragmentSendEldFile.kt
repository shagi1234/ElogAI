package com.selbiconsulting.elog.ui.main.inspections.child

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.FragmentSendEldFileBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates

class FragmentSendEldFile : Fragment() {
    private lateinit var b: FragmentSendEldFileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentSendEldFileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }

        b.edtOutputCommit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                b.btnSend.isEnabled = (s!!.isNotEmpty())
                b.tvLengthCounter.text = "${s?.length}/60"
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        b.btnSend.setOnClickListener {
            CustomToast.showCustomToastWithContent(
                it, requireActivity(), ToastStates.SUCCESS, resources.getString(
                    R.string.logs_were_sent_successfully
                )
            )
            findNavController().navigateUp()
        }
    }
}