package com.selbiconsulting.elog.domain.use_case

import com.selbiconsulting.elog.domain.repository.RepositoryLocalVariables
import javax.inject.Inject

class UseCaseGetVarFromSharedPrefs @Inject constructor(
    val repositoryLocalVariables: RepositoryLocalVariables
) {
    inline fun <reified T> getVariable(key: String, defValue: T): T {
        return when (T::class) {
            Boolean::class ->
                repositoryLocalVariables.getBooleanVariable(key, defValue as Boolean) as T

            String::class ->
                repositoryLocalVariables.getStringVariable(key, defValue as String) as T

            Int::class ->
                repositoryLocalVariables.getIntegerVariable(key, defValue as Int) as T

            else -> throw IllegalArgumentException("Unsupported type: ${T::class.java.simpleName}")
        }
    }

}