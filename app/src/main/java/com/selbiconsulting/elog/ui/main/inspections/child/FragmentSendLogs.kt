package com.selbiconsulting.elog.ui.main.inspections.child

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.selbiconsulting.elog.databinding.FragmentSendLogsBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class FragmentSendLogs : Fragment() {
    private lateinit var b: FragmentSendLogsBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private lateinit var pdfFiles: List<File>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentSendLogsBinding.inflate(inflater, container, false)
        observe()
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun observe() {
        sharedViewModel.logsPdfFiles.observe(viewLifecycleOwner){
            Log.e("PDF_FILES", "observe:$it ", )
            pdfFiles = it
        }
    }

    private fun initListeners() {
        b.icBack.setOnClickListener {
            findNavController().navigateUp()
        }

        b.btnSend.setOnClickListener {
            sendEmailWithAttachments(b.edtEmail.text.toString(), pdfFiles)
        }
    }

//    private fun sendEmailWithAttachment(email: String, pdfFile: File) {
//        val uri: Uri = FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.provider", // Use your app's package name
//            pdfFile
//        )
//
//        val emailIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/pdf"
//            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
//            putExtra(Intent.EXTRA_SUBJECT, "Subject")
//            putExtra(Intent.EXTRA_TEXT, "Body of the email")
//            putExtra(Intent.EXTRA_STREAM, uri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        startActivity(Intent.createChooser(emailIntent, "Send email using"))
//    }

    private fun sendEmailWithAttachments(email: String, pdfFiles: List<File>) {
        val uris: ArrayList<Uri> = ArrayList()
        for (file in pdfFiles) {
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            uris.add(uri)
        }

        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(emailIntent)
        } else {
            // Handle the case where no email app is available
        }
    }

}