package com.movtery.pojavzh.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.kdt.mcgui.ProgressLayout
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.ui.fragment.preference.PreferenceExperimentalFragment
import com.movtery.pojavzh.ui.fragment.preference.PreferenceLauncherFragment
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import net.kdt.pojavlaunch.BaseActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import net.kdt.pojavlaunch.lifecycle.ContextExecutor
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.services.ProgressServiceKeeper

class SettingsActivity : BaseActivity() {
    companion object {
        private var mCurrentClickButton: Int? = null
    }

    private lateinit var mTitle: Map<View?, String>
    private lateinit var mButtons: Map<Int, ImageButton>
    private lateinit var mFragmentView: FragmentContainerView
    private lateinit var mSettingsLayout: View
    private lateinit var mBackgroundView: View
    private lateinit var mTitleView: TextView
    private lateinit var mProgressLayout: ProgressLayout
    private var mProgressServiceKeeper: ProgressServiceKeeper? = null

    private val mPageOpacityChangeListener = ExtraListener<Boolean> { _: String?, _: Boolean ->
        setPageOpacity()
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        bindViews()
        setPageOpacity()
        ZHTools.setBackgroundImage(this, BackgroundType.SETTINGS, mBackgroundView)

        mButtons.forEach { (index, button) ->
            button.setOnClickListener { v: View ->
                onButtonClick(v)
                swapFragment(getFragmentClassByIndex(index), getFragmentTagByIndex(index))
                mCurrentClickButton = index
            }
        }

        findViewById<ImageButton>(R.id.settings_return_button).apply {
            setOnClickListener {
                setViewAnim(this, Techniques.Pulse)
                finish()
            }
        }

        ProgressKeeper.addTaskCountListener((ProgressServiceKeeper(this).also {
            mProgressServiceKeeper = it
        }))
        ProgressKeeper.addTaskCountListener(mProgressLayout)
        observeProgressLayout()

        ExtraCore.addExtraListener(ZHExtraConstants.PAGE_OPACITY_CHANGE, mPageOpacityChangeListener)

        initialize()

        if (LauncherPreferences.PREF_ANIMATION) setViewAnim(mSettingsLayout, Techniques.BounceInRight)

        mCurrentClickButton?.let { mButtons[it]?.callOnClick() }
    }

    override fun onResume() {
        super.onResume()
        ContextExecutor.setActivity(this)
    }

    override fun onPause() {
        super.onPause()
        ContextExecutor.clearActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUpObservers()
    }

    private fun initialize() {
        mButtons[0]?.apply {
            isClickable = false
            alpha = 0.4f
        }

        mTitle = mapOf(
            mButtons[0] to getString(R.string.preference_category_video),
            mButtons[1] to getString(R.string.preference_category_buttons),
            mButtons[2] to getString(R.string.preference_category_java_tweaks),
            mButtons[3] to getString(R.string.preference_category_miscellaneous),
            mButtons[4] to getString(R.string.zh_preference_category_launcher),
            mButtons[5] to getString(R.string.zh_preference_category_experimental)
        )
    }

    private fun onButtonClick(view: View) {
        mButtons.forEach { (_, button) ->
            button.isClickable = view !== button
            setAnim(button, button.isClickable)
        }

        mTitleView.apply {
            setViewAnim(this, Techniques.Pulse)
            text = mTitle[view] ?: getString(R.string.preference_category_video)
        }

        YoYo.with(Techniques.Pulse)
            .duration((LauncherPreferences.PREF_ANIMATION_SPEED * 1.2).toLong())
            .playOn(view)
    }

    private fun setAnim(button: View, clickable: Boolean) {
        button.animate()
            .alpha(if (clickable) 1f else 0.4f)
            .setDuration(250)
    }

    private fun setPageOpacity() {
        mFragmentView.alpha = LauncherPreferences.PREF_PAGE_OPACITY.toFloat() / 100
    }

    private fun bindViews() {
        mFragmentView = findViewById(R.id.zh_settings_fragment)
        mSettingsLayout = findViewById(R.id.scroll_settings_layout)
        mBackgroundView = findViewById(R.id.background_view)
        mTitleView = findViewById(R.id.zh_settings_title)
        mProgressLayout = findViewById(R.id.zh_settings_progress_layout)

        mButtons = mapOf(
            0 to findViewById(R.id.video_settings),
            1 to findViewById(R.id.controls_settings),
            2 to findViewById(R.id.java_settings),
            3 to findViewById(R.id.misc_settings),
            4 to findViewById(R.id.launcher_settings),
            5 to findViewById(R.id.experimental_settings)
        )
    }

    private fun swapFragment(fragmentClass: Class<out Fragment?>, fragmentTag: String) {
        ZHTools.swapSettingsFragment(this, fragmentClass, fragmentTag, null, false)
    }

    private fun cleanUpObservers() {
        mProgressLayout.cleanUpObservers()
        ProgressKeeper.removeTaskCountListener(mProgressLayout)
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper)
        ExtraCore.removeExtraListenerFromValue(ZHExtraConstants.PAGE_OPACITY_CHANGE, mPageOpacityChangeListener)
    }

    private fun observeProgressLayout() {
        with(mProgressLayout) {
            observe(ProgressLayout.DOWNLOAD_MINECRAFT)
            observe(ProgressLayout.UNPACK_RUNTIME)
            observe(ProgressLayout.INSTALL_MODPACK)
            observe(ProgressLayout.AUTHENTICATE_MICROSOFT)
            observe(ProgressLayout.DOWNLOAD_VERSION_LIST)
        }
    }

    private fun getFragmentClassByIndex(index: Int): Class<out Fragment?> = when (index) {
        0 -> LauncherPreferenceVideoFragment::class.java
        1 -> LauncherPreferenceControlFragment::class.java
        2 -> LauncherPreferenceJavaFragment::class.java
        3 -> LauncherPreferenceMiscellaneousFragment::class.java
        4 -> PreferenceLauncherFragment::class.java
        5 -> PreferenceExperimentalFragment::class.java
        else -> throw IllegalArgumentException("Unknown index: $index")
    }

    private fun getFragmentTagByIndex(index: Int): String = when (index) {
        0 -> LauncherPreferenceVideoFragment.TAG
        1 -> LauncherPreferenceControlFragment.TAG
        2 -> LauncherPreferenceJavaFragment.TAG
        3 -> LauncherPreferenceMiscellaneousFragment.TAG
        4 -> PreferenceLauncherFragment.TAG
        5 -> PreferenceExperimentalFragment.TAG
        else -> throw IllegalArgumentException("Unknown index: $index")
    }
}
