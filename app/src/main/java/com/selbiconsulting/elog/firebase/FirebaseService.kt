package com.selbiconsulting.elog.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.ui.main.activity_main.ActivityMain
import com.selbiconsulting.elog.ui.splash.ActivitySplash
import javax.inject.Inject

class FirebaseService : FirebaseMessagingService() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sharedPreferencesHelper.fcm_token = token
        val intent = Intent(this, ActivitySplash::class.java)
        startActivity(intent)
        ActivityMain().finish()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        Log.d(TAG, "From: ${remoteMessage.from}")

    }

    companion object {
        const val TAG = "FIREBASE_SERVICE"
    }
}
