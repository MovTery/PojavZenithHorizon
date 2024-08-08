package com.movtery.pojavzh.ui.fragment

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo.YoYoString
import com.movtery.pojavzh.extra.ZHExtraConstants
import com.movtery.pojavzh.feature.mod.modpack.install.InstallExtra
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.slideInAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.fragments.SearchModFragment
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import java.io.File

class SelectModPackFragment : FragmentWithAnim(R.layout.fragment_select_modpack), TaskCountListener {
    companion object {
        const val TAG: String = "SelectModPackFragment"
    }

    private var mMainView: View? = null
    private var openDocumentLauncher: ActivityResultLauncher<Any?>? = null
    private var modPackFile: File? = null
    private var mTasksRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension(null)) { result: Uri? ->
            result?.let{
                if (!mTasksRunning) {
                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(R.layout.view_task_running)
                        .setCancelable(false)
                        .show()
                    PojavApplication.sExecutorService.execute {
                        modPackFile = copyFileInBackground(requireContext(), result, PathAndUrlManager.DIR_CACHE!!.absolutePath)
                        ExtraCore.setValue(ZHExtraConstants.INSTALL_LOCAL_MODPACK,
                            InstallExtra(true, modPackFile!!.absolutePath, dialog))
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainView = view
        ProgressKeeper.addTaskCountListener(this)

        val mReturnButton = view.findViewById<ImageView>(R.id.zh_modpack_return)
        mReturnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        val mSearch = view.findViewById<Button>(R.id.zh_modpack_button_search_modpack)
        mSearch.setOnClickListener {
            if (!mTasksRunning) {
                val bundle = Bundle()
                bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, true)
                bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, null)
                ZHTools.swapFragmentWithAnim(this, SearchModFragment::class.java, SearchModFragment.TAG, bundle)
            } else {
                setViewAnim(mSearch, Techniques.Shake)
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
            }
        }
        val mLocal = view.findViewById<Button>(R.id.zh_modpack_button_local_modpack)
        mLocal.setOnClickListener {
            if (!mTasksRunning) {
                Toast.makeText(requireActivity(), getString(R.string.zh_select_modpack_local_tip), Toast.LENGTH_SHORT).show()
                openDocumentLauncher?.launch(null)
            } else {
                setViewAnim(mLocal, Techniques.Shake)
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
            }
        }

        slideInAnim(this)
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }

    override fun slideIn(): Array<YoYoString?> {
        val yoYoString = setViewAnim(mMainView!!, Techniques.BounceInDown)
        val array = arrayOf(yoYoString)
        super.yoYos = array
        return array
    }

    override fun slideOut(): Array<YoYoString?> {
        val yoYoString = setViewAnim(mMainView!!, Techniques.FadeOutUp)
        val array = arrayOf(yoYoString)
        super.yoYos = array
        return array
    }
}
