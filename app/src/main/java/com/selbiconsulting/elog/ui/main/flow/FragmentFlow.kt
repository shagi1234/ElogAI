package com.selbiconsulting.elog.ui.main.flow

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer.MessageType
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.Constants.MessageTypes
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentFlowBinding
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.messages.ViewModelMessage
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.ViewModelChangeStatus
import com.selbiconsulting.elog.ui.util.logout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentFlow : Fragment() {
    private lateinit var b: FragmentFlowBinding
    private val sharedViewModel by activityViewModels<SharedViewModel>()
    private val viewModelMessage by activityViewModels<ViewModelMessage>()
    private val viewModelChangeStatus by activityViewModels<ViewModelChangeStatus>()

    private var fetchMessagesJob: Job? = null

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startFetchingMessages()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentFlowBinding.inflate(inflater, container, false)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setNavigation()
        return b.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
    }

    private fun observe() {
        viewModelMessage.unreadMessageCount.observe(viewLifecycleOwner) { count ->
            updateMessageBadge(count)
        }

        lifecycleScope.launch {
            viewModelMessage.messagesState.collect { state ->
                when (state) {
                    is Resource.Error -> {
                        when (state.statusCode) {
                            Resource.LOGIN_CODE -> {
                                viewModelMessage.reLogin()
                            }

                            Resource.LOGOUT_CODE -> {
                                requireActivity().logout()
                                viewModelMessage.clearLocalDb()
                            }
                        }
                    }

                    is Resource.Success -> {
                        if (state.data == null) return@collect
                        viewModelMessage.handleMessagesSuccess(state.data)

                        if (state.data.isLogEdited == true) {
                            viewModelChangeStatus.fetchLogs()
                        }

                        sharedViewModel.isPcAllowed.value = state.data.isPcAllowed ?: false
                    }

                    else -> {}
                }


            }

        }
        lifecycleScope.launch {
            viewModelMessage.messages.collect { messages ->
                if (messages.isNotEmpty() && messages.last().messageType == com.selbiconsulting.elog.data.model.enums.MessageType.ALARM.value)
                    playAlarm(messages.last())
            }
        }

        sharedViewModel.isDvirCreated.observe(viewLifecycleOwner) {
            if (it) {
                b.bottomNav.selectedItemId = R.id.nav_logs
            }
        }
    }

    private fun playAlarm(message: EntityMessage) {
        if (message.isPlayedAlarm == false) {
            val alarmId =
                if (message.fileSize.toInt() == 1) R.raw.battleship_alarm else if (message.fileSize.toInt() == 2) R.raw.alert_siren else R.raw.wave_alarm
            val audioUri =
                Uri.parse("android.resource://${requireContext().packageName}/${alarmId}")
            val mediaItem = MediaItem.fromUri(audioUri)

            exoPlayer.apply {
                stop()
                setMediaItem(mediaItem)
                prepare()
                play()
            }

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        message.isPlayedAlarm = true
                        message.isRead = true
                        viewModelMessage.updateMessage(message)
                    }
                }
            })
        }

    }

    private fun setNavigation() {
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.fragment_container_flow) as NavHostFragment
        val navController = navHostFragment.navController

        b.bottomNav.setupWithNavController(navController)
        bottomNav = b.bottomNav


    }

    private fun startFetchingMessages() {
        fetchMessagesJob = lifecycleScope.launch {
            while (isActive) {
                fetchMessages()
                delay(3000) // 3 seconds
            }
        }
    }


    private fun fetchMessages() {
        val requestGetMessage = RequestGetMessage(
            sharedPreferencesHelper.contactId ?: "",
            viewModelMessage.inChat.value ?: 0
        )
        lifecycleScope.launch {
            viewModelMessage.getMessagesFromServer(requestGetMessage)
        }
    }

    private fun updateMessageBadge(count: Int) {
        val badge = b.bottomNav.getOrCreateBadge(R.id.nav_message)
        if (count > 0) {
            badge.isVisible = true
            badge.number = count
        } else {
            badge.isVisible = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
    }

    companion object {
        var bottomNav: BottomNavigationView? = null
    }

}