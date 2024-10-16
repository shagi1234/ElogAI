package com.selbiconsulting.elog.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper

class MyApplication : Application() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

   override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        setThemeFromPreferences()
    }
    private fun setThemeFromPreferences() {
        when (sharedPreferencesHelper.getAppTheme()) {
            SharedPreferencesHelper.THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            SharedPreferencesHelper.THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

        }
    }
}
