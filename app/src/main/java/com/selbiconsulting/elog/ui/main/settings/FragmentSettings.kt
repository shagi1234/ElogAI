package com.selbiconsulting.elog.ui.main.settings

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.data.model.enums.ThemeModes
import com.selbiconsulting.elog.data.storage.local.SharedPreferencesHelper
import com.selbiconsulting.elog.databinding.FragmentSettingsBinding
import com.selbiconsulting.elog.ui.login.ActivityLogin
import com.selbiconsulting.elog.ui.main.common.CustomProgressDialog
import com.selbiconsulting.elog.ui.main.common.CustomToast
import com.selbiconsulting.elog.ui.main.common.ToastStates
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetChangeTheme
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetDiagnosis
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetLanguages
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetLeaveTruck
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetLogout
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetRateUs
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetRequiredPermissions
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetRestartEld
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetSendFeedback
import com.selbiconsulting.elog.ui.main.settings.component.BottomSheetSwitchDriver
import com.selbiconsulting.elog.ui.main.settings.component.LeaveTruckListener
import com.selbiconsulting.elog.ui.main.settings.component.LogoutListener
import com.selbiconsulting.elog.ui.main.settings.component.ResetEldListener
import com.selbiconsulting.elog.ui.main.settings.component.SendFeedbackListener
import com.selbiconsulting.elog.ui.main.settings.component.ThemeModesListener
import com.selbiconsulting.elog.ui.splash.ActivitySplash
import com.selbiconsulting.elog.ui.util.SharedViewModel
import com.selbiconsulting.elog.ui.util.logout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class FragmentSettings : Fragment(), ThemeModesListener, SendFeedbackListener, ResetEldListener,
    LeaveTruckListener, LogoutListener {
    private lateinit var binding: FragmentSettingsBinding
    private var isExpandProfileInfo = true

    private val viewModel by viewModels<ViewModelSettings>()
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var audioManager: AudioManager
    private var maxVolume = 0

    @Inject
    lateinit var uiModeManager: UiModeManager

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private val progressDialog: CustomProgressDialog by lazy {
        CustomProgressDialog(context = requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchDriverData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        initAudioParams()
        return binding.root
    }

    private fun initAudioParams() {

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        sharedPreferencesHelper.currentVolume = currentVolume

        binding.volumeSeekbar?.progress = sharedPreferencesHelper.currentVolume ?: 0
        binding.volumeSeekbar?.max = maxVolume

        binding.tvVolumeValue?.text = sharedPreferencesHelper.currentVolume.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observe()

    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.isKeepScreenEnabled.observe(viewLifecycleOwner) { isKeepScreenEnabled ->
                    if (isKeepScreenEnabled)
                        enableKeepScreenMode()
                    else
                        disableKeepScreenMode()
                }
            }

        }

        lifecycleScope.launch {
            viewModel.driverInfo.collect {
                it.let {
                    binding.valLicence.text = it?.license
                    binding.valDriverNum.text = it?.driverId
                    binding.driverEmail.text = it?.email
                    binding.driverName.text = it?.name
                }
            }
        }

        sharedViewModel.volume.observe(viewLifecycleOwner) {
            binding.tvVolumeValue.text = it.toString()
            binding.volumeSeekbar.progress = it
            changeDeviceVolume(it)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun initListeners() {
        binding.crResetIld.setOnClickListener {
            val bottomSheetRestartEld =
                BottomSheetRestartEld(listener = this)
            bottomSheetRestartEld.show(parentFragmentManager, bottomSheetRestartEld.tag)
        }

        binding.crFeedback.setOnClickListener {
            val bottomSheet = BottomSheetSendFeedback(requireContext(), this)
            bottomSheet.show(parentFragmentManager, "SendFeedback")
        }

        binding.crSwitchDriver.setOnClickListener {
            val bottomSheet = BottomSheetSwitchDriver()
            bottomSheet.show(parentFragmentManager, "Switch")
        }


        binding.crInspections.setOnClickListener {
            requireActivity().findNavController(R.id.fragment_container_main)
                .navigate(R.id.action_fragmentFlow_to_fragmentInspections)
        }
        binding.crTheme.setOnClickListener {
            val bottomSheetChangeTheme = BottomSheetChangeTheme(requireContext(), this)
            bottomSheetChangeTheme.show(parentFragmentManager, bottomSheetChangeTheme.tag)
        }

        binding.crKeepScreen.setOnClickListener {
            viewModel.handleKeepScreenMode()
        }

        binding.btnDetails.setOnClickListener {
            if (isExpandProfileInfo) {
                collapse(binding.additionalInfo)
                binding.btnDetails.animate().setDuration(300).rotation(90f)


            } else {
                expand(binding.additionalInfo)
                binding.btnDetails.animate().setDuration(300).rotation(-90f)

            }
            isExpandProfileInfo = !isExpandProfileInfo
        }

        binding.permissionsLay.setOnClickListener {
            val bottomSheetRequiredPermissions = BottomSheetRequiredPermissions()
            bottomSheetRequiredPermissions.show(
                parentFragmentManager,
                bottomSheetRequiredPermissions.tag
            )
        }

        binding.userManualLay.setOnClickListener {
            requireActivity().findNavController(R.id.fragment_container_main)
                .navigate(R.id.action_fragmentFlow_to_fragmentUserManual2)
        }

        binding.diagnosisLay.setOnClickListener {
            val bottomSheetDiagnosis = BottomSheetDiagnosis(requireContext())
            bottomSheetDiagnosis.show(parentFragmentManager, bottomSheetDiagnosis.tag)
        }
        binding.languagesLay.setOnClickListener {
            val bottomSheetLanguages = BottomSheetLanguages(requireContext())
            bottomSheetLanguages.show(parentFragmentManager, bottomSheetLanguages.tag)
        }

        binding.checkUpdateLay.setOnClickListener {
            openAppInPlayStore()
        }

        binding.rateUsLay.setOnClickListener {
            val bottomSheetRateUs = BottomSheetRateUs(requireContext())
            bottomSheetRateUs.show(childFragmentManager, bottomSheetRateUs.tag)
        }
        binding.crLeaveTruck.setOnClickListener {
            val bottomSheetLeaveTruck = BottomSheetLeaveTruck(requireContext(), this)
            bottomSheetLeaveTruck.show(parentFragmentManager, bottomSheetLeaveTruck.tag)
        }

        binding.btnLogOut.setOnClickListener {
            val bottomSheetLogout = BottomSheetLogout(this)
            bottomSheetLogout.show(parentFragmentManager, bottomSheetLogout.tag)
        }

        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.tvVolumeValue.text = progress.toString()
                sharedPreferencesHelper.currentVolume = progress
                changeDeviceVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something when touch is started
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something when touch is stopped
            }
        })


    }


    private fun enableKeepScreenMode() {
        binding.icKeepScreen.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.white_only
            )
        )
        binding.icKeepScreen.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.warning_on))
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    private fun changeDeviceVolume(volume: Int) {
        val adjustedVolume = volume.coerceIn(0, maxVolume)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            adjustedVolume,
            AudioManager.FLAG_SHOW_UI,
        )
    }

    private fun disableKeepScreenMode() {
        binding.icKeepScreen.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_secondary
            )
        )
        binding.icKeepScreen.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.surface))
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun expand(view: View) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE

        val animation = ValueAnimator.ofInt(0, targetHeight)
        animation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        animation.duration = 300
        animation.start()

    }

    private fun collapse(view: View) {
        val initialHeight = view.measuredHeight

        val animation = ValueAnimator.ofInt(initialHeight, 0)
        animation.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        animation.duration = 300
        animation.start()
    }

    private fun openAppInPlayStore() {
        val playStorePackageName = "com.selbiconsulting.elog"
        val playStoreUrl =
            "https://play.google.com/store/apps/details?id=com.selbiconsulting.elog"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$playStorePackageName")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
        }
    }

    override fun onLightModeSelected() {
        viewModel.changeAppTheme(ThemeModes.LIGHT.ordinal)
    }

    override fun onDarkModeSelected() {
        viewModel.changeAppTheme(ThemeModes.DARK.ordinal)
    }

    override fun onSystemModeSelected() {
        viewModel.changeAppTheme(ThemeModes.SYSTEM.ordinal)
    }

    override fun onFeedbackClicked() {
        CustomToast.showCustomToastWithContent(
            v = requireView(),
            activity = requireActivity(),
            state = ToastStates.SUCCESS,
            stateTitle = "Your feedback was sent succesfully",
            enableClearIcon = false
        )

    }

    override fun onResetEldClicked() {
        lifecycleScope.launch {
            progressDialog.show()
            delay(2000)
            progressDialog.dismiss()

            CustomToast.showCustomToastWithContent(
                v = requireView(),
                activity = requireActivity(),
                state = ToastStates.SUCCESS,
                stateTitle = resources.getString(R.string.the_reset_was_completed_succesfully),
                enableClearIcon = false
            )
        }
    }


    override fun onLeaveTruckClicked() {
        val loginIntent = Intent(requireActivity(), ActivityLogin::class.java)
        viewModel.leaveTruck()
        startActivity(loginIntent)
        requireActivity().finish()
    }

    override fun onLogoutClicked() {
        sharedPreferencesHelper.firstTimeAfterLogin = true
        requireActivity().logout()
        viewModel.logout()
    }

}