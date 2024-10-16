package com.selbiconsulting.elog.data.storage.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.selbiconsulting.elog.data.model.enums.ThemeModes

class SharedPreferencesHelper(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(KEY_APP_THEME, Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()
    private val tokenUpdateListeners = mutableListOf<() -> Unit>()

    fun addTokenUpdateListener(listener: () -> Unit) {
        tokenUpdateListeners.add(listener)
    }

    companion object {
        const val KEY_APP_THEME = "APP_THEME"
        const val KEY_APP = "ELOG_AI"
        const val KEY_APP_THEME_MODE = "APP_THEME_MODE"
        const val ELD_CONNECTION_KEY = "_ELD_CONNECTION_KEY"
        const val PREF_TOKEN = "pref_token"
        const val FCM_TOKEN = "fcm_token"
        const val CURRENT_VOL = "current_vol"
        const val DEVICE_ID = "device_id"
        const val PREF_CURRENT_DRIVER_ID = "current_driver_id"
        const val PREF_USERNAME = "pref_username"
        const val PREF_LAST_LATITUDE = "last_latitude"
        const val PREF_LAST_LONGITUDE = "last_longitude"
        const val PREF_PASSWORD = "pref_password"
        const val PREF_CONTACT_ID = "contact_id"
        const val PREF_LAST_LOCATION = "last_location"
        const val PREF_VEHICLE = "vehicle"
        const val PREF_ODOMETER = "odometer"
        const val PREF_DOCUMENT = "_document"
        const val PREF_ENGINE_HOURS = "engine_hours"
        const val PREF_TRAILER = "trailer"
        const val CERTIFIED_LOG = "certified_log_2"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
        const val FIRST_TIME_AFTER_LOGIN = "FIRST_TIME_AFTER_LOGIN"
        const val ENCODED_DRIVER_SIGNATURE = "_ENCODED_DRIVER_SIGNATURE"
        const val KEEP_SCREEN_MODE = "_KEEP_SCREEN_MODE"
    }

    fun getAppTheme(): String {
        val userSelectedTheme = sharedPreferences.getString(KEY_APP_THEME, null)

        // If the user has explicitly selected a theme, use it
        if (!userSelectedTheme.isNullOrEmpty()) {
            return userSelectedTheme
        }
        // If no user preference, use the system default theme
        val systemDefaultTheme = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> THEME_DARK
            else -> THEME_LIGHT
        }
        return systemDefaultTheme
    }

    fun setAppTheme(theme: String) {
        sharedPreferences.edit().putString(KEY_APP_THEME, theme).apply()
    }

    var token: String?
        get() = sharedPreferences.getString(PREF_TOKEN, "")
        set(token) {
            editor.putString(PREF_TOKEN, token)
            editor.commit()
            tokenUpdateListeners.forEach { it() }
        }

    var fcm_token: String?
        get() = sharedPreferences.getString(FCM_TOKEN, "")
        set(token) {
            editor.putString(FCM_TOKEN, token)
            editor.commit()
        }


    var currentVolume: Int
        get() = sharedPreferences.getInt(CURRENT_VOL, 0)
        set(currentVolume) {
            editor.putInt(CURRENT_VOL, currentVolume)
            editor.commit()
        }

    var deviceID: String?
        get() = sharedPreferences.getString(DEVICE_ID, "")
        set(deviceID) {
            editor.putString(DEVICE_ID, deviceID)
            editor.commit()
        }

    var username: String
        get() = sharedPreferences.getString(PREF_USERNAME, "") ?: ""
        set(token) {
            editor.putString(PREF_USERNAME, token)
            editor.commit()
        }

    var lastLatitude: Float
        get() = sharedPreferences.getFloat(PREF_LAST_LATITUDE, 0f)
        set(lastLatitude) {
            editor.putFloat(PREF_LAST_LATITUDE, lastLatitude)
            editor.commit()
        }
    var lastLongitude: Float
        get() = sharedPreferences.getFloat(PREF_LAST_LONGITUDE, 0f)
        set(lastLongitude) {
            editor.putFloat(PREF_LAST_LONGITUDE, lastLongitude)
            editor.commit()
        }
    var password: String
        get() = sharedPreferences.getString(PREF_PASSWORD, "") ?: ""
        set(token) {
            editor.putString(PREF_PASSWORD, token)
            editor.commit()
        }

    var contactId: String?
        get() = sharedPreferences.getString(PREF_CONTACT_ID, "")
        set(token) {
            editor.putString(PREF_CONTACT_ID, token)
            editor.commit()
        }

    var lastLocation: String?
        get() = sharedPreferences.getString(PREF_LAST_LOCATION, "N/A")
        set(token) {
            editor.putString(PREF_LAST_LOCATION, token)
            editor.commit()
        }

    var vehicle: String?
        get() = sharedPreferences.getString(PREF_VEHICLE, "")
        set(token) {
            editor.putString(PREF_VEHICLE, token)
            editor.commit()
        }

    var odometer: String?
        get() = sharedPreferences.getString(PREF_ODOMETER, "0.0")
        set(token) {
            editor.putString(PREF_ODOMETER, token)
            editor.commit()
        }

    var documentId: String?
        get() = sharedPreferences.getString(PREF_DOCUMENT, "")
        set(documentId) {
            editor.putString(PREF_DOCUMENT, documentId)
            editor.commit()
        }

    var engineHours: String?
        get() = sharedPreferences.getString(PREF_ENGINE_HOURS, "0.0")
        set(token) {
            editor.putString(PREF_ENGINE_HOURS, token)
            editor.commit()
        }


    var trailer: String?
        get() = sharedPreferences.getString(PREF_TRAILER, "")
        set(token) {
            editor.putString(PREF_TRAILER, token)
            editor.commit()
        }

    var certifiedDate: String?
        get() = sharedPreferences.getString(CERTIFIED_LOG, "")
        set(token) {
            editor.putString(CERTIFIED_LOG, token)
            editor.commit()
        }

    var appThemeMode: Int
        get() = sharedPreferences.getInt(KEY_APP_THEME_MODE, ThemeModes.SYSTEM.ordinal)
        set(appTheme) {
            editor.putInt(KEY_APP_THEME_MODE, appTheme)
            editor.commit()
        }


    var isEldConnected: Boolean
        get() = sharedPreferences.getBoolean(ELD_CONNECTION_KEY, false)
        set(isEldConnected) {
            editor.putBoolean(ELD_CONNECTION_KEY, isEldConnected)
            editor.commit()
        }

    var encodedDriverSignature: String?
        get() = sharedPreferences.getString(ENCODED_DRIVER_SIGNATURE, "")
        set(encodedDriverSignature) {
            editor.putString(ENCODED_DRIVER_SIGNATURE, encodedDriverSignature)
            editor.commit()
        }

    var firstTimeAfterLogin: Boolean
        get() = sharedPreferences.getBoolean(FIRST_TIME_AFTER_LOGIN, true)
        set(firstTimeAfterLogin) {
            editor.putBoolean(FIRST_TIME_AFTER_LOGIN, firstTimeAfterLogin)
            editor.commit()
        }
}
