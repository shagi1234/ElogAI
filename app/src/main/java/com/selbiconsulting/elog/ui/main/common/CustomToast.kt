package com.selbiconsulting.elog.ui.main.common

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.CustomToastLayoutBinding


class CustomToast {
    companion object {
        fun showCustomToastWithContent(
            v: View,
            activity: Activity,
            state: ToastStates,
            stateTitle: String,
            stateDescription: String? = null,
            enableClearIcon: Boolean? = true,
        ) {
            val inflater = activity.layoutInflater
            val binding = CustomToastLayoutBinding.inflate(inflater, null, false)

            val stateInfo = when (state) {
                ToastStates.SUCCESS -> {
                    StateInfo(R.drawable.ic_success_state, R.color.success_on)
                }

                ToastStates.WARNING -> {
                    StateInfo(R.drawable.ic_warning_state, R.color.warning_on)
                }

                ToastStates.ERROR -> {
                    StateInfo(R.drawable.ic_error_state, R.color.error_on)
                }
            }

            binding.tvStateTitle.text = stateTitle

            binding.ivStatus.setImageResource(stateInfo.iconRes)
            binding.statusBg.backgroundTintList =
                ContextCompat.getColorStateList(
                    activity.applicationContext,
                    stateInfo.backgroundColor
                )



            if (stateDescription.isNullOrEmpty())
                binding.tvStateDescription.visibility = View.GONE
            else
                binding.tvStateDescription.text = stateDescription

            if (enableClearIcon == false)
                binding.icClear.visibility = View.GONE
            else
                binding.icClear.visibility = View.VISIBLE

            val toastDuration =
                if (enableClearIcon == true) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_SHORT

            val snackBar = Snackbar.make(v, "", toastDuration)
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            (snackBar.view as FrameLayout).addView(binding.root)
            snackBar.show()

            binding.icClear.setOnClickListener {
                snackBar.dismiss()
            }
        }
    }
}

enum class ToastStates {
    SUCCESS,
    WARNING,
    ERROR
}

sealed class EldConnectionStatus {
    data object Connected : EldConnectionStatus()
    data object NotConnected : EldConnectionStatus()
}

data class StateInfo(
    val iconRes: Int,
    val backgroundColor: Int
)