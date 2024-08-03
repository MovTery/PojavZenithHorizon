package com.movtery.pojavzh.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.kdt.mcgui.ProgressLayout
import com.movtery.pojavzh.ui.fragment.preference.PreferenceExperimentalFragment
import com.movtery.pojavzh.ui.fragment.preference.PreferenceLauncherFragment
import com.movtery.pojavzh.ui.subassembly.background.BackgroundType
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import net.kdt.pojavlaunch.BaseActivity
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.lifecycle.ContextExecutor
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceControlFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceJavaFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceMiscellaneousFragment
import net.kdt.pojavlaunch.prefs.screens.LauncherPreferenceVideoFragment
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.services.ProgressServiceKeeper

class SettingsActivity : BaseActivity() {
    private val mTitle: MutableMap<View?, String?> = HashMap()
    private var mSettingsLayout: View? = null
    private var mBackgroundView: View? = null
    private var mReturnButton: ImageButton? = null
    private var mVideoButton: ImageButton? = null
    private var mControlsButton: ImageButton? = null
    private var mJavaButton: ImageButton? = null
    private var mMiscButton: ImageButton? = null
    private var mLauncherButton: ImageButton? = null
    private var mExperimentalButton: ImageButton? = null
    private var mTitleView: TextView? = null
    private var mProgressLayout: ProgressLayout? = null
    private var mProgressServiceKeeper: ProgressServiceKeeper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        bindViews()
        ZHTools.setBackgroundImage(this, BackgroundType.SETTINGS, mBackgroundView)

        mReturnButton!!.setOnClickListener {
            setViewAnim(mReturnButton!!, Techniques.Bounce)
            finish()
        }

        mVideoButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(
                LauncherPreferenceVideoFragment::class.java,
                LauncherPreferenceVideoFragment.TAG
            )
        }
        mControlsButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(
                LauncherPreferenceControlFragment::class.java,
                LauncherPreferenceControlFragment.TAG
            )
        }
        mJavaButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(
                LauncherPreferenceJavaFragment::class.java,
                LauncherPreferenceJavaFragment.TAG
            )
        }
        mMiscButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(
                LauncherPreferenceMiscellaneousFragment::class.java,
                LauncherPreferenceMiscellaneousFragment.TAG
            )
        }
        mLauncherButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(PreferenceLauncherFragment::class.java, PreferenceLauncherFragment.TAG)
        }
        mExperimentalButton!!.setOnClickListener { v: View ->
            onButtonClick(v)
            swapFragment(
                PreferenceExperimentalFragment::class.java,
                PreferenceExperimentalFragment.TAG
            )
        }

        ProgressKeeper.addTaskCountListener((ProgressServiceKeeper(this).also {
            mProgressServiceKeeper = it
        }))
        ProgressKeeper.addTaskCountListener(mProgressLayout)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_MINECRAFT)
        mProgressLayout!!.observe(ProgressLayout.UNPACK_RUNTIME)
        mProgressLayout!!.observe(ProgressLayout.INSTALL_MODPACK)
        mProgressLayout!!.observe(ProgressLayout.AUTHENTICATE_MICROSOFT)
        mProgressLayout!!.observe(ProgressLayout.DOWNLOAD_VERSION_LIST)

        initialize()

        if (LauncherPreferences.PREF_ANIMATION) setViewAnim(mSettingsLayout!!, Techniques.BounceInRight)
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
        mProgressLayout!!.cleanUpObservers()
        ProgressKeeper.removeTaskCountListener(mProgressLayout)
        ProgressKeeper.removeTaskCountListener(mProgressServiceKeeper)
    }

    private fun initialize() {
        mVideoButton!!.isClickable = false
        mVideoButton!!.alpha = 0.4f

        mTitle[mVideoButton] = getString(R.string.preference_category_video)
        mTitle[mControlsButton] = getString(R.string.preference_category_buttons)
        mTitle[mJavaButton] = getString(R.string.preference_category_java_tweaks)
        mTitle[mMiscButton] = getString(R.string.preference_category_miscellaneous)
        mTitle[mLauncherButton] = getString(R.string.zh_preference_category_launcher)
        mTitle[mExperimentalButton] = getString(R.string.zh_preference_category_experimental)
    }

    private fun onButtonClick(view: View) {
        mVideoButton!!.isClickable = view !== mVideoButton
        mControlsButton!!.isClickable = view !== mControlsButton
        mJavaButton!!.isClickable = view !== mJavaButton
        mMiscButton!!.isClickable = view !== mMiscButton
        mLauncherButton!!.isClickable = view !== mLauncherButton
        mExperimentalButton!!.isClickable = view !== mExperimentalButton

        setAnim(mVideoButton, mVideoButton!!.isClickable)
        setAnim(mControlsButton, mControlsButton!!.isClickable)
        setAnim(mJavaButton, mJavaButton!!.isClickable)
        setAnim(mMiscButton, mMiscButton!!.isClickable)
        setAnim(mLauncherButton, mLauncherButton!!.isClickable)
        setAnim(mExperimentalButton, mExperimentalButton!!.isClickable)

        mTitleView?.let { setViewAnim(it, Techniques.Pulse) }
        mTitleView!!.text = if (mTitle[view] != null) mTitle[view] else getString(R.string.preference_category_video)

        YoYo.with(Techniques.Pulse)
            .duration((LauncherPreferences.PREF_ANIMATION_SPEED * 1.2).toLong())
            .playOn(view)
    }

    private fun setAnim(button: View?, clickable: Boolean) {
        if (clickable && button!!.alpha < 1f) {
            button.animate()
                .alpha(1f)
                .setDuration(250)
        } else if (!clickable && button!!.alpha > 0.4f) {
            button.animate()
                .alpha(0.4f)
                .setDuration(250)
        }
    }

    private fun bindViews() {
        mSettingsLayout = findViewById(R.id.scroll_settings_layout)
        mBackgroundView = findViewById(R.id.background_view)

        mReturnButton = findViewById(R.id.settings_return_button)
        mVideoButton = findViewById(R.id.video_settings)
        mControlsButton = findViewById(R.id.controls_settings)
        mJavaButton = findViewById(R.id.java_settings)
        mMiscButton = findViewById(R.id.misc_settings)
        mLauncherButton = findViewById(R.id.launcher_settings)
        mExperimentalButton = findViewById(R.id.experimental_settings)

        mTitleView = findViewById(R.id.zh_settings_title)
        mProgressLayout = findViewById(R.id.zh_settings_progress_layout)
    }

    private fun swapFragment(fragmentClass: Class<out Fragment?>, fragmentTag: String) {
        ZHTools.swapSettingsFragment(this, fragmentClass, fragmentTag, null, false)
    }
}
