package com.selbiconsulting.elog.ui.util

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import com.selbiconsulting.elog.R
import com.selbiconsulting.elog.databinding.ItemCircularTiemlineVer2Binding


class UiHelper(private val context: Context) {

    fun disableButtonPrimary(button: Button) {
        button.isEnabled = false
        button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.primary_disabled)
        button.setTextColor(ContextCompat.getColor(context, R.color.surface_disabled))

    }

    fun enableButtonPrimary(button: Button) {
        button.isEnabled = true
        button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.primary_brand)
        button.setTextColor(ContextCompat.getColor(context, R.color.white))

    }

    fun dpToPx(dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun dpToPx(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun pxToDp(px: Float): Float {
        val resources = context.resources
        val density = resources.displayMetrics.density
        return px / density
    }

    fun dpToPxFloat(dp: Int): Float {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toFloat()
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = (context as? Activity)?.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }




    fun decreaseLayoutHeightWithAnim(layout: View, layoutHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(layoutHeight, 0)
        valueAnimator.duration = 300

        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = layout.layoutParams
            layoutParams.height = value
            layout.layoutParams = layoutParams
        }
        valueAnimator.doOnEnd {
            layout.visibility = View.GONE
        }
        valueAnimator.start()
    }

    fun increaseLayoutHeightWithAnim(layout: View, layoutHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(0, layoutHeight)
        valueAnimator.duration = 300
        valueAnimator.doOnStart {
            layout.visibility = View.VISIBLE
        }
        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = layout.layoutParams
            layoutParams.height = value
            layout.layoutParams = layoutParams
        }
        valueAnimator.start()
    }

     fun setMarginsWithAnim(
        view: View,
        leftMargin: Int = 0,
        topMargin: Int = 0,
        rightMargin: Int = 0,
        bottomMargin: Int = 0
    ) {
        val layoutParams = view.layoutParams as LinearLayout.LayoutParams
        val layoutParamsWithMargin =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParamsWithMargin.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedFraction
                val newLayoutParams = view.layoutParams
                layoutParams.setMargins(
                    (layoutParams.leftMargin + (layoutParamsWithMargin.leftMargin - layoutParams.leftMargin) * fraction).toInt(),
                    (layoutParams.topMargin + (layoutParamsWithMargin.topMargin - layoutParams.topMargin) * fraction).toInt(),
                    (layoutParams.rightMargin + (layoutParamsWithMargin.rightMargin - layoutParams.rightMargin) * fraction).toInt(),
                    (layoutParams.bottomMargin + (layoutParamsWithMargin.bottomMargin - layoutParams.bottomMargin) * fraction).toInt()
                )
                view.layoutParams = newLayoutParams
            }
            start()
        }
    }

    fun setActiveCircularProgressIndicator(b: ItemCircularTiemlineVer2Binding) {
        b.root.animate().setDuration(300).scaleY(1.5f)
        b.root.animate().setDuration(300).scaleX(1.5f).withStartAction {
            setMarginsWithAnim(b.root, dpToPx(28), dpToPx(0), dpToPx(28), dpToPx(0))
        }
    }

    fun setInactiveCircularProgressIndicator(b: ItemCircularTiemlineVer2Binding) {
        b.root.animate().setDuration(300).scaleY(1f)
        b.root.animate().setDuration(300).scaleX(1f).withStartAction {
            setMarginsWithAnim(b.root, dpToPx(6), dpToPx(22), dpToPx(6), dpToPx(22))
        }
    }

    fun getWindowWidth(): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.widthPixels
    }

    fun getWindowHeight(): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels

    }

    fun isTablet(): Boolean {
        val orientation = context.resources.configuration.orientation
        return (orientation == Configuration.ORIENTATION_LANDSCAPE && getWindowWidth() > dpToPx(600))
    }

    fun decreaseLayoutWidthWithAnim(layout: View, layoutWidth: Int) {
        val valueAnimator = ValueAnimator.ofInt(layoutWidth, 0)
        valueAnimator.duration = 300

        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = layout.layoutParams
            layoutParams.width = value
            layout.layoutParams = layoutParams
        }
        valueAnimator.doOnEnd {
            layout.visibility = View.GONE
        }
        valueAnimator.start()
    }

    fun increaseLayoutWidthWithAnim(layout: View, startWidth:Int=0, layoutWidth: Int, duration: Long = 300) {
        val valueAnimator = ValueAnimator.ofInt(startWidth, layoutWidth)
        valueAnimator.duration = duration
        valueAnimator.doOnStart {
            layout.visibility = View.VISIBLE
        }
        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            val layoutParams = layout.layoutParams
            layoutParams.width = value
            layout.layoutParams = layoutParams
        }
        valueAnimator.start()

    }
}