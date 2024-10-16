package com.selbiconsulting.elog.ui.main.messages

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.marginEnd
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.common.Resource
import com.selbiconsulting.elog.data.model.dto.DtoFile
import com.selbiconsulting.elog.data.model.entity.ChatItem
import com.selbiconsulting.elog.data.model.entity.EntityMessage
import com.selbiconsulting.elog.data.model.enums.MessageType
import com.selbiconsulting.elog.data.model.request.RequestGetMessage
import com.selbiconsulting.elog.data.model.request.RequestSendMessage
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentMessagesBinding
import com.selbiconsulting.elog.ui.extentions.toBase64
import com.selbiconsulting.elog.ui.main.flow.FragmentFlow
import com.selbiconsulting.elog.ui.util.FileManager
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.UiHelper
import com.selbiconsulting.elog.ui.util.audio_recorder.AudioRecorderImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
import javax.inject.Inject


private const val PICK_PDF_FILE = 1000
private const val REQUEST_IMAGE_CAPTURE = 1001
private const val REQUEST_AUDIO_RECORDING = 1002

@AndroidEntryPoint
class FragmentMessages : Fragment(), ReceivedVoiceListener,
    AlertMessageListener {
    private lateinit var b: FragmentMessagesBinding

    @Inject
    lateinit var exoPlayer: ExoPlayer
    private lateinit var adapterMessages: AdapterMessages

    private val viewModelMessage: ViewModelMessage by activityViewModels<ViewModelMessage>()

    @Inject
    lateinit var sharedPref: SharedPreferencesHelper

    private var keyboardVisible = false
    private var sendFilesLayVisible = false
    private val fileManager = FileManager()
    private val keyboardLayoutListener by lazy {
        ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            requireView().getWindowVisibleDisplayFrame(rect)
            val screenHeight = requireView().rootView.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                hideBottomNav()
            } else {
                showBottomNAv()
            }
        }
    }
    private lateinit var currentPhotoPath: String


    private var timerJob: Job? = null
    private var blinkJob: Job? = null


    private val recorder by lazy {
        AudioRecorderImpl(requireContext())
    }


    private var audioFile: File? = null

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                for (photoUri in uris) {
                    val file = File(photoUri.path.toString())

                    val fileDto = DtoFile(
                        file.toBase64(),
                        file.path,
                        "", // You can fill this field with appropriate data if needed
                        file.length().toInt()
                    )

                    FileManager.shared.saveFileToInternalStorage(
                        requireContext(),
                        file,
                        file.name + file.extension.removePrefix(".")
                    )

                    val requestSendMessage = RequestSendMessage(
                        sharedPref.contactId ?: "",
                        message = file.path,
                        messageType = MessageType.IMAGE.value,
                        file = fileDto, // Assign the fileDto object here
                        fileSize = file.length()
                    )

                    sendMessage(requestSendMessage)
                }
            } else {
                Log.e("PhotoPicker", "No media selected")
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentMessagesBinding.inflate(inflater, container, false)
        setRecyclerMessages()
        setInChat()

        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        observe()
        initListeners()
    }


    private fun setInChat() {
        viewModelMessage.setInChat(1)
        viewModelMessage.markAllMessagesAsRead()
        exoPlayer.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModelMessage.setInChat(0)
    }

    private fun observe() {
        lifecycleScope.launch {
            viewModelMessage.messages.collect { messages ->
                if (messages.isEmpty()) return@collect
                adapterMessages.updateData(messages)

                b.rvMessages.scrollToPosition((messages.count() - 1))
                viewModelMessage.markAllMessagesAsRead()

            }
        }

        lifecycleScope.launch {
            viewModelMessage.lastReadDate.collect {
                viewModelMessage.updateMessagesStatus(it)
            }
        }

        lifecycleScope.launch {
            viewModelMessage.fileUrlState.observe(viewLifecycleOwner) { fileUrlState ->
                if (fileUrlState is Resource.Success) {
                    lifecycleScope.launch {
                        fileUrlState.data?.let { writeFileFromByteArray(it) }
                    }
                }
            }

        }
    }


    private fun writeFileFromByteArray(byteArray: ByteArray) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "audio${timeStamp}.mp3"
        try {
            val file = File(requireActivity().cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(byteArray)
            }

            lifecycleScope.launch {
                viewModelMessage.updateMessageByRemoteId(
                    id = viewModelMessage.currentMessage.value?.id.toString(),
                    content = file.absolutePath
                )

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setRecyclerMessages() {
        adapterMessages = AdapterMessages(
            context = requireContext(),
            receivedVoiceListener = this,
            alertMessageListener = this
        )
        b.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterMessages
            itemAnimator = null
        }



    }



    private fun initListeners() = with(b) {
        edtChat.setOnFocusChangeListener { _, hasFocus ->
            lifecycleScope.launch {
                if (hasFocus)
                    view?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
                else view?.viewTreeObserver?.removeOnGlobalLayoutListener(keyboardLayoutListener)
            }
        }
        btnSendFiles.setOnClickListener {
            if (sendFilesLayVisible) hideSendFilesLayout()
            else showSendFilesLayout()
        }

        openGalleryLay.setOnClickListener {
            openPhotoPicker()
        }
        openCameraLay.setOnClickListener {
            openCamera()
        }

        openFilesLay.setOnClickListener {
            openExplorer()
        }

        rvMessages.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                if (b.rvMessages.adapter!!.itemCount == 0) return@addOnLayoutChangeListener
                b.rvMessages.smoothScrollToPosition((b.rvMessages.adapter!!.itemCount - 1))
            }
        }

        btnSendMessage.setOnClickListener {

            if (edtChat.text.isNullOrEmpty()) return@setOnClickListener

            val requestSendMessage = RequestSendMessage(
                contactId = sharedPref.contactId ?: "",
                messageType = MessageType.TEXT.value,
                message = edtChat.text.toString(),
                fileSize = 0,
                file = null
            )

            sendMessage(requestSendMessage)

            edtChat.text.clear()
        }

        edtChat.addTextChangedListener {
            btnVoiceRecord.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
            btnSendMessage.visibility = if (it.isNullOrEmpty()) View.GONE else View.VISIBLE

        }

        btnVoiceRecord.setOnTouchListener(
            object : OnTouchListener {
                private var newX = 0f
                private var deltaX = 0f
                private var lastAction = 0

                private val screenWidth = UiHelper(requireContext()).getWindowWidth().toFloat()
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    val v = buttonsLay
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)


                            deltaX = v.x - event.rawX
                            lastAction = MotionEvent.ACTION_DOWN

                            startRecording()
                            if (selectFilesLay.height != 0)
                                hideSendFilesLayout()

                        }

                        MotionEvent.ACTION_MOVE -> {
                            newX = event.rawX + deltaX

                            if (newX < (screenWidth - v.width) / 5 * 2) {
                                val x = screenWidth - v.width - v.marginEnd
                                if (btnVoiceOverlay.visibility == View.VISIBLE) {
                                    v.animate().alpha(1f).x(x).setDuration(0)
                                        .withStartAction {
                                            v.alpha = 0f
                                            deleteVoiceRecord()
                                        }
                                        .start()
                                    showMessageInputLay()
                                }
                            } else {
                                v.animate().x(newX).setDuration(0).start()
                            }
                        }

                        MotionEvent.ACTION_UP -> {

                            val x = screenWidth - v.width - v.marginEnd

                            if (newX > (screenWidth - v.width) / 5 * 2) {
                                if (btnVoiceOverlay.visibility == View.VISIBLE)
                                    sendRecordedVoice()
                            }

                            showMessageInputLay()
                            stopRecordingTimer()

                            v.animate()
                                .x(x)
                                .setDuration(100)
                                .start()

                        }


                        else -> return false
                    }
                    return true
                }
            })


    }

    private fun sendRecordedVoice() {
        recorder.stop()

        if (audioFile == null || fileManager.getAudioFileLength(audioFile!!) <= 0)
            return

        val dtoFile = DtoFile(
            data = audioFile!!.toBase64(),
            filename = audioFile!!.path,
            contentType = "",
            fileSize = audioFile!!.length().toInt()
        )

        val requestSendMessage = RequestSendMessage(
            sharedPref.contactId ?: "",
            dtoFile.filename,
            MessageType.VOICE.value,
            fileSize = dtoFile.fileSize.toLong(),
            file = dtoFile
        )


        sendMessage(requestSendMessage)
    }

    private fun deleteVoiceRecord() {
        recorder.stop()
        lifecycleScope.launch {
            b.icSendFiles.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_delete
                )
            )
            b.icSendFiles.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.error_on)
            val shakeAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake);
            b.icSendFiles.startAnimation(shakeAnim)
            b.icSendFiles.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            delay(700)
            b.icSendFiles.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_attach
                )
            )
            b.icSendFiles.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.text_secondary)

        }


    }

    private fun showVoiceRecordingLay() {
        b.btnVoiceOverlay.visibility = View.VISIBLE
        b.inputMessageLay.visibility = View.GONE
        b.voiceRecordingLay.visibility = View.VISIBLE
    }

    private fun showMessageInputLay() {
        b.btnVoiceOverlay.visibility = View.GONE
        b.voiceRecordingLay.visibility = View.GONE
        b.inputMessageLay.visibility = View.VISIBLE
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_AUDIO_RECORDING
            )
        } else {
            UiHelper(requireContext()).hideKeyboard(requireActivity())
            showBottomNAv()

            showVoiceRecordingLay()
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            File(requireActivity().cacheDir, "audio${timeStamp}.mp3").also {
                recorder.start(it)
                audioFile = it
            }
            showRecordingTimer()
        }

    }

    private fun showRecordingTimer() {
        timerJob?.cancel()
        blinkJob?.cancel()
        timerJob = lifecycleScope.launch {
            var seconds = 0
            while (isActive) {
                val minutes = seconds % 3600 / 60
                val secs = seconds % 60
                val time = String.format("%02d:%02d", minutes, secs)
                b.tvRecordingDuration.text = time
                delay(1000)
                seconds += 1
            }
        }
        blinkJob = lifecycleScope.launch {
            while (isActive) {
                b.icRecording.visibility = if (b.icRecording.visibility == View.VISIBLE) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
                delay(500)
            }
        }
    }

    private fun stopRecordingTimer() {
        timerJob?.cancel()
        blinkJob?.cancel()
        timerJob = null
        blinkJob = null
        b.icRecording.visibility = View.VISIBLE
    }

    private fun sendMessage(requestSendMessage: RequestSendMessage) {
        lifecycleScope.launch {
            viewModelMessage.sendMessageToServer(requestSendMessage)
        }
    }

    private fun openExplorer() {
        val chooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(chooseFile, PICK_PDF_FILE)
    }

    private fun openCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoIntent.resolveActivity(requireActivity().packageManager)
        val photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            throw IOException(ex.message)
        }

        val photoUri = FileProvider.getUriForFile(
            requireContext(), "com.selbiconsulting.fileProvider", photoFile
        )
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "com.selbiconsulting_${timeStamp}_", ".jpg", storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openPhotoPicker() {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun hideSendFilesLayout() {
        UiHelper(requireContext()).decreaseLayoutHeightWithAnim(
            b.selectFilesLay, UiHelper(requireContext()).dpToPx(176)
        )
        b.icSendFiles.animate().rotation(0f).setDuration(300).withEndAction {
            b.icSendFiles.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_attach
                )
            )
        }
        sendFilesLayVisible = false
    }

    private fun showSendFilesLayout() {
        if (keyboardVisible) {
            UiHelper(requireContext()).hideKeyboard(requireActivity())
        }

        UiHelper(requireContext()).increaseLayoutHeightWithAnim(
            b.selectFilesLay, UiHelper(requireContext()).dpToPx(176)
        )

        b.icSendFiles.animate().rotation(90f).setDuration(300).withStartAction {
            b.icSendFiles.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.chevron_right_small
                )
            )
        }

        sendFilesLayVisible = true
    }

    private fun hideBottomNav() {
        if (keyboardVisible) return
        keyboardVisible = true
        UiHelper(requireContext()).decreaseLayoutHeightWithAnim(
            FragmentFlow.bottomNav!!, UiHelper(requireContext()).dpToPx(56)
        )
        if (sendFilesLayVisible) hideSendFilesLayout()
    }

    private fun showBottomNAv() {
        if (!keyboardVisible) return
        keyboardVisible = false
        UiHelper(requireContext()).increaseLayoutHeightWithAnim(
            FragmentFlow.bottomNav!!, UiHelper(requireContext()).dpToPx(56)
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                requireActivity().contentResolver.query(uri, null, null, null, null).use { cursor ->
                    val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)

                    if (nameIndex != null && sizeIndex != null && cursor.moveToFirst()) {
                        val fileName = cursor.getString(nameIndex)
                        val fileSize = cursor.getLong(sizeIndex)
                        val inputStream = context?.contentResolver?.openInputStream(uri)
                        val filePath = FileManager.shared.getFilePathFromUri(requireContext(), uri)


                        inputStream?.use { input ->
                            val bytes = input.readBytes()

                            val fileDto = DtoFile(
                                Base64.getEncoder().encodeToString(bytes),
                                filePath ?: fileName,
                                "", // You can fill this field with appropriate data if needed
                                fileSize.toInt()
                            )

                            val requestSendMessage = RequestSendMessage(
                                contactId = sharedPref.contactId ?: "",
                                messageType = MessageType.FILE.value,
                                message = fileDto.filename,
                                fileSize = fileSize,
                                file = fileDto
                            )

                            sendMessage(requestSendMessage)
                        }
                    }


                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photoFile = File(currentPhotoPath)

            val fileDto = DtoFile(
                photoFile.toBase64(),
                photoFile.path,
                "", // You can fill this field with appropriate data if needed
                photoFile.length().toInt()
            )

            val requestSendMessage = RequestSendMessage(
                contactId = sharedPref.contactId ?: "",
                messageType = MessageType.IMAGE.value,
                message = photoFile.path,
                fileSize = photoFile.length(),
                file = fileDto
            )

            sendMessage(requestSendMessage)
        }

        if (requestCode == REQUEST_AUDIO_RECORDING) {
            if (resultCode == RESULT_OK) {
                startRecording()
            }
        } else {
            showMessageInputLay()
            stopRecordingTimer()
        }
    }

    override fun onVoiceReceived(message: EntityMessage) {
        val requestGetMessage =
            RequestGetMessage(contactId = sharedPref.contactId ?: "", fileId = message.content)
        viewModelMessage.getFileUrl(requestGetMessage = requestGetMessage)
        viewModelMessage.insertCurrentMessage(message)
    }

    override fun onAlertCompleted(message: EntityMessage) {
        val playedAlert = message
        playedAlert.isPlayedAlarm = true
        viewModelMessage.updateMessage(playedAlert)
    }
}
