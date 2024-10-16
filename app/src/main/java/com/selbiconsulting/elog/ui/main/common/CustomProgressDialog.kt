package com.selbiconsulting.elog.ui.main.common

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.selbiconsulting.elog.R

class CustomProgressDialog(private val context: Context, private val title: String? = null) :
    Dialog(context, R.style.SheetDialog) {
    private var view: View

    init {
        setCancelable(false)
        view = LayoutInflater.from(context).inflate(R.layout.progress_dialog_layout, null)
        setProgressTitle(title)
        setContentView(view)
    }

    private fun setProgressTitle(title: String?) {
        val tvTitle = view.findViewById<TextView>(R.id.tv_progress_title)
        if (title.isNullOrEmpty()) {
            tvTitle.visibility = View.GONE
            return
        }

        tvTitle.text = title
    }
}