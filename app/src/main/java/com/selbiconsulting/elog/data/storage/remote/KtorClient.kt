package com.selbiconsulting.elog.data.storage.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.headers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/*
 * Created by shagi on 23.03.2024 22:27
 */

class KtorClient     {
    private val client = createClient()

    private fun createClient(): HttpClient {
        return HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = false
                        prettyPrint = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
                socketTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
                connectTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createClientWithToken(token: String): HttpClient {
        return HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        prettyPrint = true
                    })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
                socketTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
                connectTimeoutMillis = TimeUnit.SECONDS.toMillis(20)
            }

            // Add token to requests
            defaultRequest {
                headers {
                    append("Authorization", "Bearer $token")
                }
            }
        }
    }

    // Use this method to get the client instance without token
    val getInstance: HttpClient
        get() = client

    // Use this method to get the client instance with token
    fun getInstanceWithToken(token: String): HttpClient {
        return createClientWithToken(token)
    }

}
