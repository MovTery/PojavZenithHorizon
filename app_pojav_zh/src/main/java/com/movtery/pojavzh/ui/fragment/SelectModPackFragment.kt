package com.movtery.pojavzh.ui.fragment

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.event.value.InstallLocalModpackEvent
import com.movtery.pojavzh.feature.mod.modpack.install.InstallExtra
import com.movtery.pojavzh.utils.PathAndUrlManager
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.databinding.FragmentSelectModpackBinding
import net.kdt.pojavlaunch.fragments.SearchModFragment
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.progresskeeper.TaskCountListener
import org.greenrobot.eventbus.EventBus
import java.io.File

class SelectModPackFragment : FragmentWithAnim(R.layout.fragment_select_modpack), TaskCountListener {
    companion object {
        const val TAG: String = "SelectModPackFragment"
    }

    private lateinit var binding: FragmentSelectModpackBinding
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
                        modPackFile = copyFileInBackground(requireContext(), result, PathAndUrlManager.DIR_CACHE.absolutePath)
                        EventBus.getDefault().post(InstallLocalModpackEvent(InstallExtra(true, modPackFile!!.absolutePath, dialog)))
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectModpackBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ProgressKeeper.addTaskCountListener(this)

        binding.returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        binding.searchButton.setOnClickListener {
            if (!mTasksRunning) {
                val bundle = Bundle()
                bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, true)
                bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, null)
                ZHTools.swapFragmentWithAnim(this, SearchModFragment::class.java, SearchModFragment.TAG, bundle)
            } else {
                setViewAnim(binding.searchButton, Animations.Shake)
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
            }
        }
        binding.localButton.setOnClickListener {
            if (!mTasksRunning) {
                Toast.makeText(requireActivity(), getString(R.string.select_modpack_local_tip), Toast.LENGTH_SHORT).show()
                openDocumentLauncher?.launch(null)
            } else {
                setViewAnim(binding.localButton, Animations.Shake)
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        mTasksRunning = taskCount != 0
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, Animations.BounceInDown))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, Animations.FadeOutUp))
    }
}
