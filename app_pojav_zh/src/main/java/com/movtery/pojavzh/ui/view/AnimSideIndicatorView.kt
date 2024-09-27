package com.movtery.pojavzh.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.movtery.pojavzh.setting.AllSettings
import net.kdt.pojavlaunch.R

class AnimSideIndicatorView : View {
    private var targetView: View? = null
    private var currentAnimSet: AnimatorSet? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        setBackgroundResource(R.drawable.settings_category_selected)
        isClickable = false
    }

    /**
     * 设置被选中的View，并启动动画
     * @param selectedView 被选中的 View
     */
    fun setSelectedView(selectedView: View?, xOffset: Int = 0, yOffset: Int = 0) {
        if (selectedView == null || selectedView === this || selectedView === targetView) return

        stopAnimations()

        this.targetView = selectedView
        val parent = targetView!!.parent as ViewGroup

        //确保在相同的父布局中，否则会发生奇妙的事情
        if (parent != getParent()) {
            parent.addView(this)
        }

        layoutParams.height = targetView!!.height
        requestLayout()

        val x = targetView!!.left - width + xOffset
        val y = targetView!!.top + yOffset
        animateToPosition(x, y)
    }

    private fun animateToPosition(x: Int, y: Int) {
        val translateX = ObjectAnimator.ofFloat(this, "translationX", x.toFloat())
        val translateY = ObjectAnimator.ofFloat(this, "translationY", y.toFloat())

        val animSpeed = (AllSettings.animationSpeed * 0.2).toLong()
        translateX.setDuration(animSpeed)
        translateY.setDuration(animSpeed)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateX, translateY)

        translateX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                currentAnimSet = null
            }
        })

        animatorSet.start()
    }

    private fun stopAnimations() {
        currentAnimSet?.apply { if (isRunning) cancel() }
    }
}