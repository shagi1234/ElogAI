package com.selbiconsulting.elog.ui.main.counter_screen

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selbiconsulting.elog.domain.use_case.UseCaseChangeTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViewModelCounter @Inject constructor(
    private val useCaseChangeTheme: UseCaseChangeTheme,
) : ViewModel() {
    private val _progressValue = MutableStateFlow(100)
    val progressValue get() = _progressValue

    private val _progressCounterTime = MutableStateFlow("01:00")
    val progressCounterTime get() = _progressCounterTime

    private val _isProgressEnded = MutableStateFlow(false)
    val isProgressEnded get() = _isProgressEnded

    private lateinit var countDownTimer: CountDownTimer

    fun changeTheme() {
        useCaseChangeTheme.execute()
        countDownTimer.cancel()
    }

    fun startCounter() {
        countDownTimer = object : CountDownTimer(60 * 10L * _progressValue.value + 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / 1000).toInt()
                val minutes = hours / 60
                val seconds = hours % 60

                val passedSeconds = 60 - seconds
                val passedProgress = passedSeconds * (100f / 60f)
                val progressValuePerSecond = 100f - passedProgress

                if (progressValuePerSecond.toInt() == 0) return

                viewModelScope.launch {
                    _progressCounterTime.emit(String.format("%02d:%02d", minutes, seconds))
                    _progressValue.emit(progressValuePerSecond.toInt())
                }
            }

            override fun onFinish() {
                viewModelScope.launch {
                    _progressCounterTime.emit(String.format("%02d:%02d", 0, 0))

                    _progressValue.emit(0)
                    _isProgressEnded.emit(true)
                }

            }
        }
        countDownTimer.start()
    }
}