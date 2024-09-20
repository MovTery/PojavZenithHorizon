package com.movtery.anim

import android.animation.Animator
import android.animation.AnimatorSet
import android.view.View
import com.movtery.anim.animations.Animations
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class AnimPlayer {
    private var mAnimatorSet: AnimatorSet = AnimatorSet()
    private var mAnimEntries: MutableList<Entry> = ArrayList()
    private var mOnStartCallback: AnimCallback? = null
    private var mOnEndCallback: AnimCallback? = null
    private var mDuration: Long? = null
    private var mDelay: Long? = null

    fun clearEntries() {
        mAnimEntries.clear()
    }

    fun apply(entry: Entry): AnimPlayer {
        mAnimEntries.add(entry)
        return this
    }

    fun duration(long: Long): AnimPlayer {
        mDuration = long
        return this
    }

    fun delay(long: Long): AnimPlayer {
        mDelay = long
        return this
    }

    fun setOnStart(callback: AnimCallback): AnimPlayer {
        mOnStartCallback = callback
        return this
    }

    fun setOnEnd(callback: AnimCallback): AnimPlayer {
        mOnEndCallback = callback
        return this
    }

    fun start() {
        if (mAnimatorSet.isStarted || mAnimatorSet.isRunning) {
            stop()
        }

        mOnStartCallback?.call()
        val finalAnimators: MutableList<Animator> = ArrayList()

        for (entry in mAnimEntries) {
            finalAnimators.addAll(entry.animations.animator.getAnimators(entry.target))
        }

        mAnimatorSet.apply {
            duration = mDuration ?: LauncherPreferences.PREF_ANIMATION_SPEED.toLong()
            startDelay = mDelay ?: 0

            removeAllListeners()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mOnStartCallback?.call()
                }

                override fun onAnimationEnd(animation: Animator) {
                    mOnEndCallback?.call()
                    clearState()
                }

                override fun onAnimationCancel(animation: Animator) {
                    clearState()
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            })
            playTogether(finalAnimators)
        }
        mAnimatorSet.start()
    }

    fun stop() {
        if (mAnimatorSet.isRunning) {
            mAnimatorSet.cancel()
            clearState()
        }
    }

    //清理动画状态，防止重复调用问题
    private fun clearState() {
        mAnimatorSet.removeAllListeners()
        mAnimatorSet.end()
        mAnimatorSet = AnimatorSet()
    }

    companion object {
        fun play(): AnimPlayer {
            return AnimPlayer()
        }
    }

    data class Entry(val target: View, val animations: Animations)
}
