package com.selbiconsulting.elog.ui.main.user_manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.FragmentReadPdfBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FragmentReadPDF : Fragment() {
    private lateinit var b: FragmentReadPdfBinding
    private val args: FragmentReadPDFArgs by navArgs()
    private lateinit var pdfFile: File


    companion object {
        const val INSPECTION = 0
        const val USER_MANUAL = 1
        const val INSTALLATION = 2
        const val MALFUNCTION = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentReadPdfBinding.inflate(inflater, container, false)
        setHeaderTitle()
        pdfFile = when (args.pdfType) {
            INSPECTION -> File.createTempFile("inspection", ".pdf", requireContext().cacheDir)
            USER_MANUAL -> File.createTempFile("user_manual", ".pdf", requireContext().cacheDir)
            INSTALLATION -> File.createTempFile("installation", ".pdf", requireContext().cacheDir)
            else -> File.createTempFile("malfunction", ".pdf", requireContext().cacheDir)
        }

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPdfAsync()
        initListeners()
    }

    private fun initListeners() {
        b.icBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setHeaderTitle() {
        b.tvHeaderTitle.text = args.headerTitle
    }

    private fun loadPdfAsync() {
        lifecycleScope.launch(Dispatchers.IO) {
            val pdfId = when (args.pdfType) {
                INSPECTION -> R.raw.inspection
                MALFUNCTION -> R.raw.malfunction
                USER_MANUAL -> R.raw.user_manual
                else -> R.raw.installation
            }


            val file = try {
                val inputStream = resources.openRawResource(pdfId)
                pdfFile.writeBytes(inputStream.readBytes())
                pdfFile
            } catch (e: Exception) {
                throw e
            }

            withContext(Dispatchers.Main) {
                b.pdfView
                    .fromFile(file)
                    .show()
            }
        }
    }
}