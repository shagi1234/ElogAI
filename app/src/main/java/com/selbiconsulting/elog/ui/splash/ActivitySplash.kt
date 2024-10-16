package com.selbiconsulting.elog.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.SharedPrefsKeys
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.domain.use_case.UseCaseGetVarFromSharedPrefs
import com.selbiconsulting.elog.firebase.FirebaseService
import com.selbiconsulting.elog.ui.login.ActivityLogin
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.util.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ActivitySplash : AppCompatActivity() {

    @Inject
    lateinit var useCaseGetVarFromSharedPrefs: UseCaseGetVarFromSharedPrefs

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    private val needsDeviceID: Boolean by lazy { sharedPreferencesHelper.deviceID.isNullOrEmpty() }
    private val needsFcmToken: Boolean by lazy { sharedPreferencesHelper.fcm_token.isNullOrEmpty() }
    private val isLogin: Boolean by lazy {
        (sharedPreferencesHelper.username.isNotEmpty() && sharedPreferencesHelper.password.isNotEmpty())
    }

    private val hasLeftTruck: Boolean by lazy {
        useCaseGetVarFromSharedPrefs.getVariable(
            SharedPrefsKeys.LEAVE_TRUCK,
            false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(FirebaseService.TAG, "FCM token: ${sharedPreferencesHelper.fcm_token}")

        if (needsDeviceID) getDeviceID()
        else navigateToNewActivity()
    }


    private fun getDeviceID() {
        val deviceId = UUID.randomUUID().toString()
        sharedPreferencesHelper.deviceID = deviceId
        if (needsFcmToken)
            getFcmToken()
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(FirebaseService.TAG, "FAILED", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.e(FirebaseService.TAG, "FCM token: $token")
            sharedPreferencesHelper.fcm_token = token
            navigateToNewActivity()
        }
    }

    private fun navigateToNewActivity() {
        if (!isLogin || hasLeftTruck)
            navigateToLoginActivity()
        else
            navigateToMainActivity()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, ActivityLogin::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, ActivityMain::class.java)
        startActivity(intent)
        finish()
    }
}