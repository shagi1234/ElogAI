package com.selbiconsulting.elog.domain.use_case


import android.app.UiModeManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.model.enums.ThemeModes
import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import javax.inject.Inject

class UseCaseSetAppTheme @Inject constructor(
    private val repositoryLocalVariables: RepositoryLocalVariables
) {

    fun execute(appTheme: Int) {
        when (appTheme) {
            ThemeModes.LIGHT.ordinal -> setLightTheme()
            ThemeModes.DARK.ordinal -> setDarkTheme()
            ThemeModes.SYSTEM.ordinal -> setDefaultTheme()
        }
    }

    private fun setLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        repositoryLocalVariables.setAppTheme(ThemeModes.LIGHT.ordinal)
    }

    private fun setDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        repositoryLocalVariables.setAppTheme(ThemeModes.DARK.ordinal)
    }

    private fun setDefaultTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        repositoryLocalVariables.setAppTheme(ThemeModes.SYSTEM.ordinal)
    }


}