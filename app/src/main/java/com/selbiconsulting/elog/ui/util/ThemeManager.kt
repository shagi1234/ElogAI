package com.selbiconsulting.elog.ui.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.model.enums.ThemeModes
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper

class ThemeManager(
    context: Context,
) {
    private val sharedPreferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper(context)
    private val appThemeMode = sharedPreferencesHelper.appThemeMode

    fun setTheme() {
        when (appThemeMode) {
            ThemeModes.LIGHT.ordinal -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeModes.DARK.ordinal -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeModes.SYSTEM.ordinal -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        }

        saveTheme(appThemeMode)

    }


    private fun saveTheme(theme: Int) {
        sharedPreferencesHelper.appThemeMode = theme
    }
}