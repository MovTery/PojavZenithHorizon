package com.movtery.pojavzh.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.feature.mod.ModUtils
import com.movtery.pojavzh.ui.dialog.FilesDialog
import com.movtery.pojavzh.ui.dialog.FilesDialog.FilesButton
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.ui.subassembly.filelist.FileIcon
import com.movtery.pojavzh.ui.subassembly.filelist.FileItemBean
import com.movtery.pojavzh.ui.subassembly.filelist.FileSelectedListener
import com.movtery.pojavzh.ui.subassembly.view.SearchViewWrapper
import com.movtery.pojavzh.utils.ZHTools
import com.movtery.pojavzh.utils.anim.AnimUtils.Companion.setVisibilityAnim
import com.movtery.pojavzh.utils.file.FileCopyHandler
import com.movtery.pojavzh.utils.file.FileTools.Companion.copyFileInBackground
import com.movtery.pojavzh.feature.mod.ModToggleHandler
import com.movtery.pojavzh.utils.NewbieGuideUtils
import com.movtery.pojavzh.utils.file.PasteFile
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension
import net.kdt.pojavlaunch.databinding.FragmentModsBinding
import net.kdt.pojavlaunch.fragments.SearchModFragment
import java.io.File
import java.util.function.Consumer

class ModsFragment : FragmentWithAnim(R.layout.fragment_mods) {
    companion object {
        const val TAG: String = "ModsFragment"
        const val BUNDLE_ROOT_PATH: String = "root_path"
    }

    private lateinit var binding: FragmentModsBinding
    private lateinit var mSearchViewWrapper: SearchViewWrapper
    private var openDocumentLauncher: ActivityResultLauncher<Any>? = null
    private var mRootPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension("jar")) { result: Uri? ->
            result?.let{
                Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()

                PojavApplication.sExecutorService.execute {
                    copyFileInBackground(requireContext(), result, mRootPath)
                    Tools.runOnUiThread { Toast.makeText(requireContext(), getString(R.string.profile_mods_added_mod), Toast.LENGTH_SHORT).show()
                        binding.fileRecyclerView.refreshPath()
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
        binding = FragmentModsBinding.inflate(layoutInflater)
        mSearchViewWrapper = SearchViewWrapper(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        parseBundle()

        binding.apply {
            fileRecyclerView.apply {
                setShowFiles(true)
                setShowFolders(false)

                setFileSelectedListener(object : FileSelectedListener() {
                    override fun onFileSelected(file: File?, path: String?) {
                        file?.let {
                            if (it.isFile) {
                                val fileName = it.name

                                val filesButton = FilesButton()
                                filesButton.setButtonVisibility(true, true, true, true, true,
                                    (fileName.endsWith(ModUtils.JAR_FILE_SUFFIX) || fileName.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)))
                                filesButton.setMessageText(if (it.isDirectory) getString(R.string.file_folder_message) else getString(R.string.file_message))

                                if (fileName.endsWith(ModUtils.JAR_FILE_SUFFIX)) filesButton.setMoreButtonText(getString(R.string.profile_mods_disable))
                                else if (fileName.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) filesButton.setMoreButtonText(getString(R.string.profile_mods_enable))

                                val filesDialog = FilesDialog(requireContext(), filesButton,
                                    { Tools.runOnUiThread { refreshPath() } },
                                    fullPath, it
                                )

                                filesDialog.setCopyButtonClick { visibility = View.VISIBLE }

                                //检测后缀名，以设置正确的按钮
                                if (fileName.endsWith(ModUtils.JAR_FILE_SUFFIX)) {
                                    filesDialog.setFileSuffix(ModUtils.JAR_FILE_SUFFIX)
                                    filesDialog.setMoreButtonClick {
                                        ModUtils.disableMod(it)
                                        refreshPath()
                                        filesDialog.dismiss()
                                    }
                                } else if (fileName.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) {
                                    filesDialog.setFileSuffix(ModUtils.DISABLE_JAR_FILE_SUFFIX)
                                    filesDialog.setMoreButtonClick {
                                        ModUtils.enableMod(it)
                                        refreshPath()
                                        filesDialog.dismiss()
                                    }
                                }

                                filesDialog.show()
                            }
                        }
                    }

                    override fun onItemLongClick(file: File?, path: String?) {
                    }
                })

                setOnMultiSelectListener { itemBeans: List<FileItemBean> ->
                    if (itemBeans.isNotEmpty()) {
                        PojavApplication.sExecutorService.execute {}
                        //取出全部文件
                        val selectedFiles: MutableList<File> = ArrayList()
                        itemBeans.forEach(Consumer { value: FileItemBean ->
                            val file = value.file
                            file?.apply { selectedFiles.add(this) }
                        })
                        val filesButton = FilesButton()
                        filesButton.setButtonVisibility(true, true, false, false, true, true)
                        filesButton.setDialogText(
                            getString(R.string.file_multi_select_mode_title),
                            getString(R.string.file_multi_select_mode_message, itemBeans.size),
                            getString(R.string.profile_mods_disable_or_enable)
                        )
                        Tools.runOnUiThread {
                            val filesDialog = FilesDialog(requireContext(), filesButton, {
                                Tools.runOnUiThread {
                                    closeMultiSelect()
                                    refreshPath()
                                }
                            }, fullPath, selectedFiles)
                            filesDialog.setCopyButtonClick { operateView.pasteButton.visibility = View.VISIBLE }
                            filesDialog.setMoreButtonClick {
                                ModToggleHandler(requireContext(), selectedFiles) {
                                    Tools.runOnUiThread {
                                        closeMultiSelect()
                                        refreshPath()
                                    }
                                }.start()
                            }
                            filesDialog.show()
                        }
                    }
                }

                setRefreshListener {
                    val show = itemCount == 0
                    setVisibilityAnim(nothingLayout, show)
                }
            }

            multiSelectFiles.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                selectAll.apply {
                    this.isChecked = false
                    visibility = if (isChecked) View.VISIBLE else View.GONE
                }
                fileRecyclerView.adapter.setMultiSelectMode(isChecked)
                mSearchViewWrapper.let { if (mSearchViewWrapper.isVisible()) mSearchViewWrapper.setVisibility(!isChecked) }
            }

            operateView.apply {
                selectAll.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
                    fileRecyclerView.adapter.selectAllFiles(isChecked)
                }

                returnButton.setOnClickListener {
                    closeMultiSelect()
                    ZHTools.onBackPressed(requireActivity())
                }

                addFileButton.setOnClickListener {
                    closeMultiSelect()
                    val suffix = ".jar"
                    Toast.makeText(
                        requireActivity(),
                        String.format(getString(R.string.file_add_file_tip), suffix),
                        Toast.LENGTH_SHORT
                    ).show()
                    openDocumentLauncher?.launch(suffix)
                }

                pasteButton.setOnClickListener {
                    PasteFile.getInstance().pasteFiles(
                        requireActivity(),
                        fileRecyclerView.fullPath,
                        object : FileCopyHandler.FileExtensionGetter {
                            override fun onGet(file: File?): String? {
                                return file?.let { it1 -> getFileSuffix(it1) }
                            }
                        }
                    ) {
                        Tools.runOnUiThread {
                            closeMultiSelect()
                            pasteButton.visibility = View.GONE
                            fileRecyclerView.refreshPath()
                        }
                    }
                }

                createFolderButton.setOnClickListener { goDownloadMod() }

                downloadOptifine.setOnClickListener {
                    TipDialog.Builder(requireContext())
                        .setMessage(R.string.profile_manager_download_optifine_message)
                        .setConfirmClickListener {
                            val bundle = Bundle()
                            bundle.putBoolean(DownloadOptiFineFragment.BUNDLE_DOWNLOAD_MOD, true)
                            ZHTools.swapFragmentWithAnim(
                                this@ModsFragment,
                                DownloadOptiFineFragment::class.java,
                                DownloadOptiFineFragment.TAG,
                                bundle
                            )
                        }.buildDialog()
                }

                searchButton.setOnClickListener {
                    closeMultiSelect()
                    mSearchViewWrapper.setVisibility()
                }

                refreshButton.setOnClickListener {
                    closeMultiSelect()
                    fileRecyclerView.refreshPath()
                }
            }

            goDownloadText.setOnClickListener{ goDownloadMod() }

            fileRecyclerView.lockAndListAt(mRootPath?.let { File(it) }, mRootPath?.let { File(it) })
        }

        startNewbieGuide()
    }

    private fun startNewbieGuide() {
        if (NewbieGuideUtils.showOnlyOne(TAG)) return
        binding.operateView.apply {
            val fragmentActivity = requireActivity()
            TapTargetSequence(fragmentActivity)
                .targets(
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, refreshButton, getString(R.string.generic_refresh), getString(R.string.newbie_guide_general_refresh)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, searchButton, getString(R.string.generic_search), getString(R.string.newbie_guide_mod_search)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, addFileButton, getString(R.string.profile_mods_add_mod), getString(R.string.newbie_guide_mod_import)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, createFolderButton, getString(R.string.profile_mods_download_mod), getString(R.string.newbie_guide_mod_download)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, returnButton, getString(R.string.generic_close), getString(R.string.newbie_guide_general_close)))
                .start()
        }
    }

    private fun closeMultiSelect() {
        //点击其它控件时关闭多选模式
        binding.apply {
            multiSelectFiles.isChecked = false
            selectAll.visibility = View.GONE
        }
    }

    private fun getFileSuffix(file: File): String {
        val name = file.name
        if (name.endsWith(ModUtils.DISABLE_JAR_FILE_SUFFIX)) {
            return ModUtils.DISABLE_JAR_FILE_SUFFIX
        } else if (name.endsWith(ModUtils.JAR_FILE_SUFFIX)) {
            return ModUtils.JAR_FILE_SUFFIX
        } else {
            val dotIndex = file.name.lastIndexOf('.')
            return if (dotIndex == -1) "" else file.name.substring(dotIndex)
        }
    }

    private fun goDownloadMod() {
        closeMultiSelect()
        val bundle = Bundle()
        bundle.putBoolean(SearchModFragment.BUNDLE_SEARCH_MODPACK, false)
        bundle.putString(SearchModFragment.BUNDLE_MOD_PATH, mRootPath)
        ZHTools.swapFragmentWithAnim(
            this,
            SearchModFragment::class.java,
            SearchModFragment.TAG,
            bundle
        )
    }

    private fun parseBundle() {
        val bundle = arguments ?: return
        mRootPath = bundle.getString(BUNDLE_ROOT_PATH, mRootPath)
    }

    private fun initViews() {
        binding.apply {
            mSearchViewWrapper.apply {
                setSearchListener(object : SearchViewWrapper.SearchListener {
                    override fun onSearch(string: String?, caseSensitive: Boolean): Int {
                        return fileRecyclerView.searchFiles(string, caseSensitive)
                    }
                })
                setShowSearchResultsListener(object : SearchViewWrapper.ShowSearchResultsListener {
                    override fun onSearch(show: Boolean) {
                        fileRecyclerView.setShowSearchResultsOnly(show)
                    }
                })
            }

            fileRecyclerView.setFileIcon(FileIcon.MOD)

            operateView.apply {
                addFileButton.setContentDescription(getString(R.string.profile_mods_add_mod))
                createFolderButton.setContentDescription(getString(R.string.profile_mods_download_mod))
                createFolderButton.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_download
                    )
                )
                pasteButton.setVisibility(if (PasteFile.getInstance().pasteType != null) View.VISIBLE else View.GONE)

                ZHTools.setTooltipText(
                    returnButton,
                    addFileButton,
                    pasteButton,
                    createFolderButton,
                    searchButton,
                    refreshButton
                )
            }
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(modsLayout, Animations.BounceInDown))
                .apply(AnimPlayer.Entry(operateLayout, Animations.BounceInLeft))
                .apply(AnimPlayer.Entry(operateButtonsLayout, Animations.FadeInLeft))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(modsLayout, Animations.FadeOutUp))
                .apply(AnimPlayer.Entry(operateLayout, Animations.FadeOutRight))
                .apply(AnimPlayer.Entry(operateButtonsLayout, Animations.BounceShrink))
        }
    }
}

