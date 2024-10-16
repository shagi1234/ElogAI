package com.selbiconsulting.elog.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.ui.util.ThemeManager
import com.selbiconsulting.elog.ui.util.audio_player.AudioPlayer
import com.selbiconsulting.elog.ui.util.audio_player.AudioPlayerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.utils.io.concurrent.shared
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class UiModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

}