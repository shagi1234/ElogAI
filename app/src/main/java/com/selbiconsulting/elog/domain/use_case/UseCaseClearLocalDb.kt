package com.selbiconsulting.elog.domain.use_case

import android.content.SharedPreferences
import com.selbiconsulting.elog.data.storage.local.AppDatabase
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import javax.inject.Inject

class UseCaseClearLocalDb @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
) {

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun execute() {
        editor.clear().apply()
        sharedPreferencesHelper.editor.clear().apply()
        appDatabase.clearAllTables()
    }
}