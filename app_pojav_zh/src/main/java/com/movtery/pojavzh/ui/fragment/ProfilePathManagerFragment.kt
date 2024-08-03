package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathJsonObject
import com.movtery.pojavzh.feature.customprofilepath.ProfilePathManager.save
import com.movtery.pojavzh.ui.dialog.EditTextDialog
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfileItem
import com.movtery.pojavzh.ui.subassembly.customprofilepath.ProfilePathAdapter
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.slideInAnim
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import java.util.UUID

class ProfilePathManagerFragment : FragmentWithAnim(R.layout.fragment_profile_path_manager) {
    companion object {
        const val TAG: String = "ProfilePathManagerFragment"
    }

    private val mData: MutableList<ProfileItem> = ArrayList()
    private var mPathLayout: View? = null
    private var mOperateLayout: View? = null
    private var mOperateView: View? = null
    private var adapter: ProfilePathAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val value = ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR) as String?

        value?.let {
            if (value.isNotEmpty() && !isAddedPath(value)) {
                EditTextDialog.Builder(requireContext())
                    .setTitle(R.string.zh_profiles_path_create_new_title)
                    .setConfirmListener { editBox: EditText ->
                        val string = editBox.text.toString()
                        if (string.isEmpty()) {
                            editBox.error = getString(R.string.global_error_field_empty)
                            return@setConfirmListener false
                        }

                        mData.add(ProfileItem(UUID.randomUUID().toString(), string, value))
                        save(this.mData)
                        refresh()
                        true
                    }.buildDialog()
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshData()

        mPathLayout = view.findViewById(R.id.path_layout)
        mOperateLayout = view.findViewById(R.id.operate_layout)
        mOperateView = view.findViewById(R.id.operate_view)

        val pathList = view.findViewById<RecyclerView>(R.id.zh_profile_path)
        val refreshButton = view.findViewById<ImageButton>(R.id.zh_profile_path_refresh_button)
        val createNewButton = view.findViewById<ImageButton>(R.id.zh_profile_path_create_new_button)
        val returnButton = view.findViewById<ImageButton>(R.id.zh_profile_path_return_button)

        ZHTools.setTooltipText(refreshButton, refreshButton.contentDescription)
        ZHTools.setTooltipText(createNewButton, createNewButton.contentDescription)
        ZHTools.setTooltipText(returnButton, returnButton.contentDescription)

        adapter = ProfilePathAdapter(this, pathList, this.mData)
        pathList.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(view.context, R.anim.fade_downwards)
        )
        pathList.layoutManager = LinearLayoutManager(requireContext())
        pathList.adapter = adapter

        refreshButton.setOnClickListener { refresh() }
        createNewButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(FilesFragment.BUNDLE_SELECT_FOLDER_MODE, true)
            bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILE, false)
            bundle.putBoolean(FilesFragment.BUNDLE_REMOVE_LOCK_PATH, false)
            bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, Environment.getExternalStorageDirectory().absolutePath)
            ZHTools.swapFragmentWithAnim(this, FilesFragment::class.java, FilesFragment.TAG, bundle)
        }
        returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

        slideInAnim(this)
    }

    private fun refresh() {
        refreshData()
        adapter!!.updateData(this.mData)
    }

    private fun refreshData() {
        mData.clear()
        mData.add(ProfileItem("default", getString(R.string.zh_profiles_path_default), Tools.DIR_GAME_HOME))

        try {
            val json: String
            if (ZHTools.FILE_PROFILE_PATH.exists()) {
                json = Tools.read(ZHTools.FILE_PROFILE_PATH)
                if (json.isEmpty()) return
            } else return

            val jsonObject = JsonParser.parseString(json).asJsonObject

            for (key in jsonObject.keySet()) {
                val profilePathId = Gson().fromJson(jsonObject[key], ProfilePathJsonObject::class.java)
                val item = ProfileItem(key, profilePathId.title, profilePathId.path)
                mData.add(item)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun isAddedPath(path: String): Boolean {
        for (mDatum in this.mData) {
            if (mDatum.path == path) return true
        }
        return false
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mPathLayout!!, Techniques.BounceInDown))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.BounceInLeft))
        yoYos.add(setViewAnim(mOperateView!!, Techniques.FadeInLeft))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYos: MutableList<YoYoString?> = ArrayList()
        yoYos.add(setViewAnim(mPathLayout!!, Techniques.FadeOutUp))
        yoYos.add(setViewAnim(mOperateLayout!!, Techniques.FadeOutRight))
        val array = yoYos.toTypedArray()
        super.yoYos = array
        return array
    }
}
