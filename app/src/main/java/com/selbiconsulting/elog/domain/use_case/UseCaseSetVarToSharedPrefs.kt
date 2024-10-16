package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import javax.inject.Inject

class UseCaseSetVarToSharedPrefs @Inject constructor(
    private val repositoryLocalVariables: RepositoryLocalVariables
) {
    fun <T> setVariable(key: String, value: T) {
        when (value) {
            is Boolean -> repositoryLocalVariables.setBooleanVariable(key, value)
            is String -> repositoryLocalVariables.setStringVariable(key, value)
            is Int -> repositoryLocalVariables.setIntegerVariable(key, value)
        }
    }
}