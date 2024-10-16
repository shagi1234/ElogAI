package com.selbiconsulting.elog.data.storage.remote

import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import io.ktor.client.HttpClient
import javax.inject.Inject

class KtorClientProvider @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {
    private var client = createClient()

    init {
        sharedPreferencesHelper.addTokenUpdateListener { refreshClient() }
    }

    private fun createClient(): HttpClient {
        return KtorClient().getInstanceWithToken(sharedPreferencesHelper.token ?: "")
    }

    private fun refreshClient() {
        client = createClient()
    }

    fun getClient() = client
}