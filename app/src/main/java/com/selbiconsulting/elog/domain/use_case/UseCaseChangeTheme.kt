package com.selbiconsulting.elog.domain.use_case

import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.model.enums.ThemeModes
import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import javax.inject.Inject

class UseCaseChangeTheme @Inject constructor(
    private val repositoryLocalVariables: RepositoryLocalVariables
) {
    fun execute() {
        if (repositoryLocalVariables.getAppTheme() == ThemeModes.DARK.ordinal) setLightTheme()
        else setDarkTheme()
    }

    private fun setLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        repositoryLocalVariables.setAppTheme(ThemeModes.LIGHT.ordinal)
    }

    private fun setDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        repositoryLocalVariables.setAppTheme(ThemeModes.DARK.ordinal)
    }

}