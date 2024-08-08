package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager.Companion.currentProfile
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.mkdirs
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.fragments.ProfileEditorFragment
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.profiles.ProfileIconCache
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles
import java.io.File

class ProfileManagerFragment : FragmentWithAnim(R.layout.fragment_profile_manager) {
    companion object {
        const val TAG: String = "ProfileManagerFragment"
        const val DELETED_PROFILE: String = "deleted_profile"
    }

    private var mShortcutsLayout: View? = null
    private var mModdedLayout: View? = null
    private var mProfileKey: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        slideInAnim(this)
    }

    private fun init(view: View) {
        val gameDirPath = ZHTools.getGameDirPath(LauncherProfiles.getCurrentProfile().gameDir)
        mProfileKey = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, "")

        mShortcutsLayout = view.findViewById(R.id.shortcuts_layout)
        mModdedLayout = view.findViewById(R.id.modded_layout)

        val modsButton = view.findViewById<Button>(R.id.zh_shortcuts_mods)
        val instanceButton = view.findViewById<Button>(R.id.zh_instance_path)
        val resourceButton = view.findViewById<Button>(R.id.zh_resource_path)
        val worldButton = view.findViewById<Button>(R.id.zh_world_path)
        val logsButton = view.findViewById<Button>(R.id.zh_logs_path)
        val crashReportButton = view.findViewById<Button>(R.id.zh_crash_report_path)

        val editButton = view.findViewById<Button>(R.id.zh_profile_edit)
        val deleteButton = view.findViewById<Button>(R.id.zh_profile_delete)

        modsButton.setOnClickListener {
            val modsPath = File(gameDirPath, "/mods")
            if (!modsPath.exists()) {
                mkdirs(modsPath)
            }

            val bundle = Bundle()
            bundle.putString(ModsFragment.BUNDLE_ROOT_PATH, modsPath.absolutePath)
            ZHTools.swapFragmentWithAnim(this, ModsFragment::class.java, ModsFragment.TAG, bundle)
        }

        instanceButton.setOnClickListener { swapFilesFragment(gameDirPath, gameDirPath) }
        resourceButton.setOnClickListener { swapFilesFragment(gameDirPath, File(gameDirPath, "/resourcepacks")) }
        worldButton.setOnClickListener { swapFilesFragment(gameDirPath, File(gameDirPath, "/saves")) }
        logsButton.setOnClickListener { swapFilesFragment(gameDirPath, File(gameDirPath, "/logs")) }
        crashReportButton.setOnClickListener { swapFilesFragment(gameDirPath, File(gameDirPath, "/crash-reports")) }

        editButton.setOnClickListener { ZHTools.swapFragmentWithAnim(this, ProfileEditorFragment::class.java, ProfileEditorFragment.TAG, null) }
        deleteButton.setOnClickListener {
            TipDialog.Builder(requireContext())
                .setTitle(R.string.zh_warning)
                .setMessage(R.string.zh_profile_manager_delete_message)
                .setConfirmClickListener {
                    if (LauncherProfiles.mainProfileJson.profiles.size > 1) {
                        ProfileIconCache.dropIcon(mProfileKey!!)
                        LauncherProfiles.mainProfileJson.profiles.remove(mProfileKey)
                        LauncherProfiles.write(currentProfile)
                        ExtraCore.setValue(ExtraConstants.REFRESH_VERSION_SPINNER, DELETED_PROFILE)
                    }
                    Tools.removeCurrentFragment(requireActivity())
                }
                .buildDialog()
        }
    }

    private fun swapFilesFragment(lockPath: File, listPath: File) {
        if (!lockPath.exists()) {
            mkdirs(lockPath)
        }
        if (!listPath.exists()) {
            mkdirs(listPath)
        }

        val bundle = Bundle()
        bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, lockPath.absolutePath)
        bundle.putString(FilesFragment.BUNDLE_LIST_PATH, listPath.absolutePath)
        bundle.putBoolean(FilesFragment.BUNDLE_QUICK_ACCESS_PATHS, false)

        ZHTools.swapFragmentWithAnim(this, FilesFragment::class.java, FilesFragment.TAG, bundle)
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mShortcutsLayout!!, Techniques.BounceInRight))
        yoYos.add(setViewAnim(mModdedLayout!!, Techniques.BounceInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mShortcutsLayout!!, Techniques.FadeOutLeft))
        yoYos.add(setViewAnim(mModdedLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
