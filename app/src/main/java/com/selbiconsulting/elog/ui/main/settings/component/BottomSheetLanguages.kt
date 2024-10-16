package com.selbiconsulting.elog.ui.main.settings.component

import android.app.Dialog
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.selbiconsulting.elog.databinding.BottomSheetLanguagesBinding
import java.util.Locale

const val LANG_EN = "en"
const val LANG_TK = "tk"
const val LANG_RU = "ru"

class BottomSheetLanguages(private val context: Context) : BottomSheetDialogFragment() {
    private lateinit var b: BottomSheetLanguagesBinding

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
        b = BottomSheetLanguagesBinding.inflate(LayoutInflater.from(context), container, false)
        Log.e("DEF_LANG", "onCreateView: ${getLanguageTag()}")
        Log.e("DEF_LANG", "onCreateView: ${Locale.getDefault().language}")
        setSelectedLanguage()
        initListeners()
        return b.root
    }

    private fun initListeners() {
        b.tvTurkmenLang.setOnClickListener {
            setActiveSelectedLayout(it)
            setLocalization(LANG_TK)
        }
        b.tvRussianLang.setOnClickListener {
            setActiveSelectedLayout(it)
            setLocalization(LANG_RU)
        }
        b.tvEnglishLang.setOnClickListener {
            setActiveSelectedLayout(it)
            setLocalization(LANG_EN)
        }
    }


    private fun setActiveSelectedLayout(layout: View) {
        inactiveAllLayouts()
        layout.isSelected = true
        dismiss()
    }

    private fun inactiveAllLayouts() {
        b.tvEnglishLang.isSelected = false
        b.tvRussianLang.isSelected = false
        b.tvTurkmenLang.isSelected = false
    }

    private fun setSelectedLanguage() {
        b.tvEnglishLang.isSelected = (getLanguageTag() == LANG_EN)
        b.tvTurkmenLang.isSelected = (getLanguageTag() == LANG_TK)
        b.tvRussianLang.isSelected = (getLanguageTag() == LANG_RU)
    }

    private fun setLocalization(locate: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(locate)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(locate)
            )
        }
    }

    private fun getLanguageTag(): String {
        val languageTag: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags()
        else AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return languageTag.ifEmpty { getDefaultLanguageTag() }
    }

    private fun getDefaultLanguageTag(): String {
        return Locale.getDefault().language
    }
}