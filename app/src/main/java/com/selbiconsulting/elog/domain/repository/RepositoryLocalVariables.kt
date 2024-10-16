package com.selbiconsulting.elog.domain.repository

import android.app.UiModeManager

interface RepositoryLocalVariables {
    fun getAppTheme(): Int
    fun setAppTheme(appTheme: Int)

    fun getStringVariable(key: String, defValue: String): String
    fun getBooleanVariable(key: String, defValue: Boolean): Boolean
    fun getIntegerVariable(key: String, defValue: Int): Int

    fun setStringVariable(key: String, value: String)
    fun setBooleanVariable(key: String, value: Boolean)
    fun setIntegerVariable(key: String, value: Int)
}