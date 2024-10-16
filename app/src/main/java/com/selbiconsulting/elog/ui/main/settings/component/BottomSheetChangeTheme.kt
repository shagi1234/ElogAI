package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.data.model.enums.ThemeModes.*
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.BottomSheetChangeThemeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetChangeTheme(
    private val context: Context,
    private val listener: ThemeModesListener
) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetChangeThemeBinding

   private val sharedPreferences: SharedPreferencesHelper  by lazy { SharedPreferencesHelper(context) }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = BottomSheetChangeThemeBinding.inflate(LayoutInflater.from(context), container, false)
        setSelectedTheme()
        initListeners()
        return b.root
    }

    private fun setSelectedTheme() {
        when (sharedPreferences.appThemeMode) {
            LIGHT.ordinal -> b.lightMode.isSelected = true
            DARK.ordinal -> b.darkMode.isSelected = true
            SYSTEM.ordinal -> b.systemMode.isSelected = true
        }
    }

    private fun initListeners() {
        b.lightMode.setOnClickListener {
            setSelectedMode(it)
            listener.onLightModeSelected()
            dismiss()
        }

        b.darkMode.setOnClickListener {
            setSelectedMode(it)
            listener.onDarkModeSelected()
            dismiss()
        }

        b.systemMode.setOnClickListener {
            setSelectedMode(it)
            listener.onSystemModeSelected()
            dismiss()
        }
    }


    private fun setSelectedMode(view: View) {
        b.lightMode.isSelected = false
        b.darkMode.isSelected = false
        b.systemMode.isSelected = false

        view.isSelected = true
    }
}

interface ThemeModesListener {
    fun onLightModeSelected()
    fun onDarkModeSelected()
    fun onSystemModeSelected()
}