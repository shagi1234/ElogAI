package com.selbiconsulting.elog.ui.main.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.selbiconsulting.elog.R

class LocationAlertDialog(private val activity: Activity) :
    AlertDialog(activity) {
    init {
        setTitle(context.getString(R.string.enable_location))
        setMessage(context.getString(R.string.your_location_is_turned_off_please_enable_it_to_use_this_feature))
        setButton(
            BUTTON_POSITIVE,
            context.getString(R.string.location_settings)
        ) { _, _ ->
            activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
            activity.finish()
        }

    }
}