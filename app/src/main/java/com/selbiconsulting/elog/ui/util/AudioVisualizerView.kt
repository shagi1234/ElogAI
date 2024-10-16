package com.selbiconsulting.elog.ui.util

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.selbiconsulting.elog.R

class AudioVisualizerView(
    private val context: Context,
    private val amps: List<Float>,
    private val parentLayout: ViewGroup,
    @ColorRes private val backgroundColor: Int,
    @ColorRes private val progressColor: Int,
    private val progressDuration: Long,
    private val maxColumnHeight: Int = UiHelper(context).dpToPx(20)
) {
    private val uiHelper = UiHelper(context)

    fun create() {

        parentLayout.removeAllViews()
        val visualizerView = visualizerView(
            context = context,
            amps = amps,
            colorId = backgroundColor,
            layoutWidth = WRAP_CONTENT,
            maxColumnHeight = maxColumnHeight
        )
        parentLayout.addView(visualizerView)
    }


    fun startProgress() {
        val parentWidth = parentLayout.width
        val startWidth = parentLayout.getChildAt(1)?.width ?: 0
        val progressSpeed = (parentWidth.toDouble() / progressDuration.toDouble())

        val remainedWidth = parentWidth - startWidth
        val remainedDuration = remainedWidth / progressSpeed
        clearProgress()
        val updatedVisualizerView = visualizerView(
            context = context,
            amps = amps,
            colorId = progressColor,
            layoutWidth = startWidth,
            maxColumnHeight = maxColumnHeight
        )
        uiHelper.increaseLayoutWidthWithAnim(
            layout = updatedVisualizerView,
            startWidth = startWidth,
            layoutWidth = parentLayout.width,
            duration = remainedDuration.toLong()
        )
        parentLayout.addView(updatedVisualizerView)
    }

    fun pausePlaying() {
        val progressWidth = parentLayout.getChildAt(1)?.width ?: 0
        clearProgress()
        val updatedVisualizerView = visualizerView(
            context = context,
            amps = amps,
            colorId = progressColor,
            layoutWidth = progressWidth,
            maxColumnHeight = maxColumnHeight
        )
//        uiHelper.increaseLayoutWidthWithAnim(
//            layout = updatedVisualizerView,
//            layoutWidth = parentLayout.width,
//            duration = progressDuration
//        )
        parentLayout.addView(updatedVisualizerView)
    }


    fun clearProgress() {
        parentLayout.getChildAt(1)?.let {
            parentLayout.removeView(it)
        }
    }

    private fun visualizerView(
        context: Context,
        amps: List<Float>,
        @ColorRes colorId: Int,
        layoutWidth: Int,
        maxColumnHeight: Int
    ): LinearLayout {
        val parentLayout = LinearLayout(context)
        val parentLayParams = FrameLayout.LayoutParams(
            layoutWidth,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        parentLayout.gravity = Gravity.CENTER_VERTICAL
        parentLayout.layoutParams = parentLayParams

        val maxAmplitude = amps.maxOrNull() ?: 1f

        amps.forEach { amplitude ->
            val scaledHeight =
                if (amplitude >= 924) (amplitude / maxAmplitude * maxColumnHeight).toInt()
                else UiHelper(context).dpToPx(4)


            val columnView = amplitudesColumn(
                context = context,
                colorId = colorId,
                ampsHeight = scaledHeight
            )
            parentLayout.addView(columnView)
        }

        return parentLayout
    }

    private fun amplitudesColumn(
        context: Context,
        @ColorRes colorId: Int,
        ampsHeight: Int
    ): LinearLayout {
        val helper = UiHelper(context)
        val childLayout = LinearLayout(context)
        val childLayParams = LinearLayout.LayoutParams(helper.dpToPx(4), ampsHeight)

        childLayParams.marginEnd = helper.dpToPx(3)
        childLayout.background = ContextCompat.getDrawable(context, R.drawable.bg_corner_10)
        childLayout.backgroundTintList = ContextCompat.getColorStateList(context, colorId)
        childLayout.layoutParams = childLayParams

        return childLayout
    }


}

