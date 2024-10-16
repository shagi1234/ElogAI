package com.selbiconsulting.elog.ui.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator


class CustomItemAnimator : SimpleItemAnimator() {
    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.let { view ->

            val animator = ObjectAnimator.ofFloat(view, "alpha", 1f)
            animator.duration = removeDuration
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                }
            })
            animator.start()
        }
        return false
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        dispatchAddFinished(holder)
        return false
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?, newHolder: RecyclerView.ViewHolder?,
        fromX: Int, fromY: Int, toX: Int, toY: Int
    ): Boolean {
        return false
    }

    override fun runPendingAnimations() {
        // Implement if necessary
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        // Implement if necessary
    }

    override fun endAnimations() {
        // Implement if necessary
    }

    override fun isRunning(): Boolean {
        return false // Implement proper checking if necessary
    }
}