package com.selbiconsulting.elog.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    companion object {
        val CHANNEL_ID = "ELogAI"
    }
}