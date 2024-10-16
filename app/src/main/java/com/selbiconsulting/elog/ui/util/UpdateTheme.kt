package com.selbiconsulting.elog.ui.util

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper

    @SuppressLint("ResourceAsColor")
     fun updateTheme(sharedPreferences: SharedPreferencesHelper) {
        if (sharedPreferences.getAppTheme() == SharedPreferencesHelper.THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            sharedPreferences.setAppTheme(SharedPreferencesHelper.THEME_DARK)
        } else if (sharedPreferences.getAppTheme() == SharedPreferencesHelper.THEME_DARK) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            sharedPreferences.setAppTheme(SharedPreferencesHelper.THEME_LIGHT)
        }
    }
