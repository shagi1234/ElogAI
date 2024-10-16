package com.selbiconsulting.elog.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import javax.inject.Inject

class RepositoryLocalVariablesImpl @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val sharedPreferences: SharedPreferences,
) : RepositoryLocalVariables {
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    override fun getAppTheme() = sharedPreferencesHelper.appThemeMode

    override fun setAppTheme(appTheme: Int) {
        sharedPreferencesHelper.appThemeMode = appTheme
    }


    override fun getStringVariable(key: String, defValue: String): String {
        return sharedPreferences.getString(key, defValue)!!
    }

    override fun getBooleanVariable(key: String, defValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }

    override fun getIntegerVariable(key: String, defValue: Int): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    override fun setStringVariable(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    override fun setBooleanVariable(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.commit()
    }

    override fun setIntegerVariable(key: String, value: Int) {
        editor.putInt(key, value)
        editor.commit()
    }

}