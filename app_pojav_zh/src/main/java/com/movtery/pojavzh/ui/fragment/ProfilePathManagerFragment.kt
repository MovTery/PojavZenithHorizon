package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.event.sticky.FileSelectorEvent
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathJsonObject
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager.Companion.save
import com.movtery.pojavzh.feature.log.Logging
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfileItem
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathAdapter
import com.movtery.pojavzh.utils.NewbieGuideUtils
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.databinding.FragmentProfilePathManagerBinding
import org.greenrobot.eventbus.EventBus
import java.util.UUID

class ProfilePathManagerFragment : FragmentWithAnim(R.layout.fragment_profile_path_manager) {
    companion object {
        const val TAG: String = "ProfilePathManagerFragment"
    }

    private lateinit var binding: FragmentProfilePathManagerBinding
    private val mData: MutableList<ProfileItem> = ArrayList()
    private var profilePathAdapter: ProfilePathAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val value = EventBus.getDefault().getStickyEvent(FileSelectorEvent::class.java)?.path

        value?.let {
            if (value.isNotEmpty() && !isAddedPath(value)) {
                EditTextDialog.Builder(requireContext())
                    .setTitle(R.string.profiles_path_create_new_title)
                    .setConfirmListener { editBox: EditText ->
                        val string = editBox.text.toString()
                        if (string.isEmpty()) {
                            editBox.error = getString(R.string.generic_error_field_empty)
                            return@setConfirmListener false
                        }

                        mData.add(ProfileItem(UUID.randomUUID().toString(), string, value))
                        save(this.mData)
                        refresh()
                        true
                    }.buildDialog()
            }
        }

        binding = FragmentProfilePathManagerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshData()

        binding.apply {
            profilePathAdapter = ProfilePathAdapter(this@ProfilePathManagerFragment, recyclerView, mData)
            recyclerView.apply {
                layoutAnimation = LayoutAnimationController(
                    AnimationUtils.loadAnimation(view.context, R.anim.fade_downwards)
                )
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = profilePathAdapter
            }

            refreshButton.setOnClickListener { refresh() }
            createNewButton.setOnClickListener {
                val bundle = Bundle()
                bundle.putBoolean(FilesFragment.BUNDLE_SELECT_FOLDER_MODE, true)
                bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILE, false)
                bundle.putBoolean(FilesFragment.BUNDLE_REMOVE_LOCK_PATH, false)
                ZHTools.swapFragmentWithAnim(this@ProfilePathManagerFragment, FilesFragment::class.java, FilesFragment.TAG, bundle)
            }
            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

            ZHTools.setTooltipText(
                refreshButton,
                createNewButton,
                returnButton
            )

            if (NewbieGuideUtils.showOnlyOne(TAG)) return
            val fragmentActivity = requireActivity()
            TapTargetSequence(fragmentActivity)
                .targets(
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, refreshButton, getString(R.string.generic_refresh), getString(R.string.newbie_guide_general_refresh)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, createNewButton, getString(R.string.profiles_path_create_new), getString(R.string.newbie_guide_profiles_path_create)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, returnButton, getString(R.string.generic_close), getString(R.string.newbie_guide_general_close)))
                .start()
        }
    }

    private fun refresh() {
        refreshData()
        profilePathAdapter?.updateData(this.mData)
    }

    private fun refreshData() {
        mData.clear()
        mData.add(ProfileItem("default", getString(R.string.profiles_path_default), PathAndUrlManager.DIR_GAME_HOME))

        runCatching {
            val json: String
            if (PathAndUrlManager.FILE_PROFILE_PATH.exists()) {
                json = Tools.read(PathAndUrlManager.FILE_PROFILE_PATH)
                if (json.isEmpty()) return
            } else return

            val jsonObject = JsonParser.parseString(json).asJsonObject

            for (key in jsonObject.keySet()) {
                val profilePathId = Gson().fromJson(jsonObject[key], ProfilePathJsonObject::class.java)
                val item = ProfileItem(key, profilePathId.title, profilePathId.path)
                mData.add(item)
            }
        }.getOrElse { e -> Logging.e("refresh profile data", Tools.printToString(e)) }
    }

    private fun isAddedPath(path: String): Boolean {
        for (mDatum in this.mData) {
            if (mDatum.path == path) return true
        }
        return false
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(pathLayout, Animations.BounceInDown))
                .apply(AnimPlayer.Entry(operateLayout, Animations.BounceInLeft))
                .apply(AnimPlayer.Entry(operateView, Animations.FadeInLeft))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(pathLayout, Animations.FadeOutUp))
                .apply(AnimPlayer.Entry(operateLayout, Animations.FadeOutRight))
        }
    }
}
